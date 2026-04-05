package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.assignments.PopupAssignmentController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.ChoreStatus;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TabAssignmentsController {

    @FXML private CheckBox chkPending, chkCompleted, chkOverdue;
    @FXML private ComboBox<UserResponseDTO> cmbUserFilter;
    @FXML private TextField txtSearch;
    @FXML private Label lblAssignmentOverview;
    @FXML private Button btnPrevWeek, btnNextWeek;
    @FXML private VBox vboxMonday, vboxTuesday, vboxWednesday, vboxThursday, vboxFriday, vboxSaturday, vboxSunday;
    @FXML private VBox searchFiltersPanel;
    @FXML private VBox detailsPane;
    @FXML private Label lblDetailTitle, lblDetailUser, lblDetailDate, lblDetailStatus;
    @FXML private Button btnComplete, btnDelete;

    private StackPane popupAddAssignment;

    private final AppServices services;
    private final MainController mainController;

    private PopupAssignmentController popupAssignmentController;

    private List<ChoreAssignmentResponseDTO> currentWeekAssignments;

    public TabAssignmentsController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
        this.currentWeekAssignments = new ArrayList<>();
    }

    @FXML
    public void initialize() {

        fetchAndDisplayAssignmentsOverview();

        loadPopups();

        //this tab separates fetching and displaying data because they are both really complex
        //compared to the rest of the application
        currentWeekAssignments = fetchAssignmentsData();
        displayAssignmentsData();

        setupAssignmentListeners();

        cmbUserFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserResponseDTO user) {
                if (user == null) return "Select user...";
                return user.name() + " " + user.surname();
            }
            @Override
            public UserResponseDTO fromString(String string) {
                return null; //non-editable combo boxes do not require this
            }
        });
    }

    @FXML
    public void handleCloseDetails() {
        detailsPane.setVisible(false);
        detailsPane.setManaged(false);

        searchFiltersPanel.setVisible(true);
        searchFiltersPanel.setManaged(true);

        mainController.enableNavigationButtons(true);
        btnPrevWeek.setDisable(false);
        btnNextWeek.setDisable(false);
    }

    @FXML
    public void handleAddAssignment() {
        popupAssignmentController.fetchChoresData();
        popupAssignmentController.reloadMemberSelection();
        mainController.openPopup(popupAddAssignment);
    }

    @FXML
    public void handleClearFilters() {
        chkPending.setSelected(false);
        chkCompleted.setSelected(false);
        chkOverdue.setSelected(false);
        cmbUserFilter.getSelectionModel().clearSelection();
        txtSearch.clear();

        currentWeekAssignments = fetchAssignmentsData();
        displayAssignmentsData();
    }

    public void fetchAndDisplayAssignmentsOverview() {

        if(services.getCurrentHousehold() == null){
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                var overview = services.getChoreClientService().getHouseholdAssignmentOverview(services.getCurrentHousehold().id());

                Platform.runLater(() -> {
                    lblAssignmentOverview.setText("Overview: " + overview.pendingAssignments() + " pending, " + overview.overdueAssignments() + " overdue");
                });

            }catch (RuntimeException e){
                e.printStackTrace();
                Platform.runLater(() -> {
                    mainController.showToast("Failed to load assignments: " + e.getMessage(), MessageType.ERROR);
                });
            }
        });
    }

    public void reloadMemberSelection() {
        cmbUserFilter.getItems().clear();
        List<UserResponseDTO> members = services.getCurrentHousehold().members();
        for(var member : members){
            cmbUserFilter.getItems().add(member);
        }
    }

    private void setupAssignmentListeners() {

        vboxTuesday.setOnMouseClicked(e -> {
            btnPrevWeek.setDisable(true);
            btnNextWeek.setDisable(true);
            searchFiltersPanel.setVisible(false);
            searchFiltersPanel.setManaged(false);
            mainController.enableNavigationButtons(false);
            detailsPane.setVisible(true);
            detailsPane.setManaged(true);
        });

    }

    private void loadPopups() {
        try {
            FXMLLoader loaderAddAssignment = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/assignments/popup_assignment.fxml"));
            loaderAddAssignment.setControllerFactory(
                    clazz -> new PopupAssignmentController(this.services, this.mainController));
            popupAddAssignment = loaderAddAssignment.load();
            popupAssignmentController = loaderAddAssignment.getController();
            mainController.addPopupToLayer(popupAddAssignment);
            popupAddAssignment.setVisible(false);
            popupAddAssignment.setManaged(false);
        } catch (IOException e) {
            throw new RuntimeException("Error loading popup: " + e.getMessage(), e);
        }
    }

    private void showDetails(ChoreAssignmentResponseDTO assignment){

        lblDetailTitle.setText(assignment.choreDescription());
        lblDetailDate.setText("Due: " + assignment.dueDate().toString());
        lblDetailUser.setText("Assigned to: " + assignment.assignedUserName());
        lblDetailStatus.setText("Status: " + assignment.status().name());

        btnComplete.setOnAction(e -> {
            mainController.requestConfirmForAction(
                    "Are you sure you want to mark this assignment as complete?",
                    () -> markAsComplete(assignment)
            );
        });

        btnDelete.setOnAction(e ->  {
            mainController.requestConfirmForAction(
                    "Are you sure you want to delete this assignment?",
                    () -> deleteAssignment(assignment)
            );
        });

        btnComplete.setDisable(assignment.status() == ChoreStatus.COMPLETED);

        btnPrevWeek.setDisable(true);
        btnNextWeek.setDisable(true);
        searchFiltersPanel.setVisible(false);
        searchFiltersPanel.setManaged(false);
        mainController.enableNavigationButtons(false);
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);
    }

    //this only calls the backend to retrieve the data
    private List<ChoreAssignmentResponseDTO> fetchAssignmentsData(){

        return null;
    }

    //this takes the data retrieved from the backend from the class attribute and displays it in the UI
    private void displayAssignmentsData(){

    }

    private void markAsComplete(ChoreAssignmentResponseDTO assignment){
        CompletableFuture.runAsync(() -> {
            try {
                services.getChoreClientService().updateChoreAssignmentStatus(assignment.assignmentId(),
                        new ChoreStatusUpdateRequestDTO(ChoreStatus.COMPLETED)
                );
                Platform.runLater(() -> {
                    mainController.showToast("Assignment marked as complete!", MessageType.SUCCESS);
                    fetchAndDisplayAssignmentsOverview();
                    currentWeekAssignments = fetchAssignmentsData();
                    displayAssignmentsData();
                    handleCloseDetails();
                });
            }catch (RuntimeException e){
                e.printStackTrace();
                Platform.runLater(() -> {
                    mainController.showToast("Failed to mark assignment as complete: " + e.getMessage(), MessageType.ERROR);
                });
            }
        });
    }

    private void deleteAssignment(ChoreAssignmentResponseDTO assignment){
        CompletableFuture.runAsync(() -> {
            try {
                services.getChoreClientService().deleteChoreAssignment(assignment.assignmentId());
                Platform.runLater(() -> {
                    mainController.showToast("Assignment deleted!", MessageType.SUCCESS);
                    fetchAndDisplayAssignmentsOverview();
                    currentWeekAssignments = fetchAssignmentsData();
                    displayAssignmentsData();
                    handleCloseDetails();
                });
            }catch (RuntimeException e){
                e.printStackTrace();
                Platform.runLater(() -> {
                    mainController.showToast("Failed to delete assignment: " + e.getMessage(), MessageType.ERROR);
                });
            }
        });

    }
}








