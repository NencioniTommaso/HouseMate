package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.assignments.PopupCreateAssignmentController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.chore.request.ChoreAssignmentFilterRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.ChoreStatus;
import com.housemate.shared.enums.MessageType;
import com.housemate.shared.utils.types.DateRange;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TabAssignmentsController {

    @FXML private CheckBox chkPending, chkCompleted, chkOverdue;
    @FXML private ComboBox<UserResponseDTO> cmbUserFilter;
    @FXML private TextField txtSearch;
    @FXML private Label lblAssignmentOverview;
    @FXML private Label lblCurrentWeek;
    @FXML private Button btnPrevWeek, btnNextWeek;
    @FXML private VBox vboxMonday, vboxTuesday, vboxWednesday, vboxThursday, vboxFriday, vboxSaturday, vboxSunday;
    @FXML private VBox searchFiltersPanel;
    @FXML private VBox detailsPane;
    @FXML private Label lblDetailTitle, lblDetailUser, lblDetailDate, lblDetailStatus;
    @FXML private Button btnComplete, btnDelete;

    private StackPane popupAddAssignment;

    private final AppServices services;
    private final MainController mainController;

    private PopupCreateAssignmentController popupAssignmentController;

    //this is useful to prevent multiple simultaneous backend calls
    private final PauseTransition searchTimer = new PauseTransition(Duration.millis(300));

    private List<ChoreAssignmentResponseDTO> currentWeekAssignments;
    private DateRange selectedWeek;

    @Setter
    private boolean isAdminMode;

    public TabAssignmentsController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
        this.selectedWeek = new DateRange(
                LocalDateTime.now().minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1)
                                   .truncatedTo(java.time.temporal.ChronoUnit.DAYS),

                LocalDateTime.now().plusDays(7 - LocalDateTime.now().getDayOfWeek().getValue())
                                   .withHour(23).withMinute(59).withSecond(59)
        );
    }

    @FXML
    public void initialize() {

        currentWeekAssignments = new ArrayList<>();
        searchTimer.setOnFinished(event -> fetchAndDisplayAssignmentsData());

        loadPopups();
        fetchAndDisplayAssignmentsOverview();
        triggerFetchAndDisplay();

        lblCurrentWeek.setText(formatCurrentWeekString());

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

        btnPrevWeek.setOnAction(e -> {
            selectedWeek = new DateRange(selectedWeek.startDate().minusDays(7), selectedWeek.endDate().minusDays(7));
            lblCurrentWeek.setText(formatCurrentWeekString());
            triggerFetchAndDisplay();
        });

        btnNextWeek.setOnAction(e -> {
            selectedWeek = new DateRange(selectedWeek.startDate().plusDays(7), selectedWeek.endDate().plusDays(7));
            lblCurrentWeek.setText(formatCurrentWeekString());
            triggerFetchAndDisplay();
        });

        chkPending.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> triggerFetchAndDisplay());
        chkCompleted.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> triggerFetchAndDisplay());
        chkOverdue.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> triggerFetchAndDisplay());
        cmbUserFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> triggerFetchAndDisplay());
        txtSearch.textProperty().addListener((obs, oldText, newText) -> triggerFetchAndDisplay());

    }

    @FXML
    public void handleCloseDetails() {
        detailsPane.setVisible(false);
        detailsPane.setManaged(false);

        searchFiltersPanel.setVisible(true);
        searchFiltersPanel.setManaged(true);

        mainController.enableNavigationButtons(true);
        mainController.disableRefreshButton(false);

        btnPrevWeek.setDisable(false);
        btnNextWeek.setDisable(false);
    }

    @FXML
    public void handleOpenAddAssignment() {
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

        triggerFetchAndDisplay();
    }

    public void fetchAndDisplayAssignmentsOverview() {

        if(services.getCurrentHousehold() == null){
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                var overview = services.getChoreClientService().getHouseholdAssignmentOverview();

                Platform.runLater(() -> lblAssignmentOverview.setText("Overview: " + overview.pendingAssignments() + " pending, " + overview.overdueAssignments() + " overdue"));

            }catch (RuntimeException e){
                Platform.runLater(() -> mainController.showToast("Failed to load assignments: " + e.getMessage(), MessageType.ERROR));
            }
        });
    }

    public void reloadMemberSelection() {
        cmbUserFilter.getItems().clear();
        List<UserResponseDTO> members = services.getCurrentHouseholdMembers();
        for(var member : members){
            cmbUserFilter.getItems().add(member);
        }
    }

    private void loadPopups() {
        try {
            FXMLLoader loaderAddAssignment = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/assignments/popup_create_assignment.fxml"));
            loaderAddAssignment.setControllerFactory(
                    clazz -> new PopupCreateAssignmentController(this.services, this.mainController));
            popupAddAssignment = loaderAddAssignment.load();
            popupAssignmentController = loaderAddAssignment.getController();
            mainController.addPopupToLayer(popupAddAssignment);
            popupAddAssignment.setVisible(false);
            popupAddAssignment.setManaged(false);
        } catch (IOException e) {
            throw new RuntimeException("Error loading popup: " + e.getMessage(), e);
        }
    }

    public void fetchAndDisplayAssignmentsData(){

        if(services.getCurrentHousehold() == null){
            return;
        }

        currentWeekAssignments = new ArrayList<>();

        List<ChoreStatus> statuses = new ArrayList<>();
        if(chkPending.isSelected()) statuses.add(ChoreStatus.PENDING);
        if(chkCompleted.isSelected()) statuses.add(ChoreStatus.COMPLETED);
        if(chkOverdue.isSelected()) statuses.add(ChoreStatus.OVERDUE);

        UUID assigneeId = cmbUserFilter.getSelectionModel().getSelectedItem() != null ?
                cmbUserFilter.getSelectionModel().getSelectedItem().id() : null;

        CompletableFuture.runAsync(() -> {
            try {
                currentWeekAssignments = services.getChoreClientService().getFilteredChoreAssignments(
                        new ChoreAssignmentFilterRequestDTO(
                                statuses,
                                assigneeId,
                                txtSearch.getText(),
                                selectedWeek
                        )
                );
                Platform.runLater(this::displayAssignmentsData);
            }catch (RuntimeException e){
                Platform.runLater(() -> mainController.showToast("Failed to load assignments: " + e.getMessage(), MessageType.ERROR));
            }
        });
    }

    private void showDetails(ChoreAssignmentResponseDTO assignment){

        mainController.disableRefreshButton(true);

        lblDetailTitle.setText(assignment.choreDescription());
        lblDetailDate.setText("Due: " + assignment.dueDate().format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")));
        lblDetailUser.setText("Assigned to: " + assignment.assignedUser().name() + " " + assignment.assignedUser().surname());

        lblDetailStatus.setText("Status: " + assignment.status().name());
        lblDetailStatus.getStyleClass().clear();
        lblDetailStatus.getStyleClass().add("detail-status");
        lblDetailStatus.getStyleClass().add("detail-" + assignment.status().name().toLowerCase());

        btnComplete.setOnAction(e -> mainController.requestConfirmForAction(
                "Are you sure you want to mark this assignment as complete?",
                () -> markAsComplete(assignment)
        ));

        btnDelete.setOnAction(e -> mainController.requestConfirmForAction(
                "Are you sure you want to delete this assignment?",
                () -> deleteAssignment(assignment)
        ));
        btnDelete.setDisable(isAdminMode);

        btnComplete.setDisable(
                assignment.status() != ChoreStatus.PENDING ||
                        !Objects.equals(assignment.assignedUser().id(), services.getCurrentUser().id())
        );

        btnPrevWeek.setDisable(true);
        btnNextWeek.setDisable(true);
        searchFiltersPanel.setVisible(false);
        searchFiltersPanel.setManaged(false);
        mainController.enableNavigationButtons(false);
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);
    }

    private void displayAssignmentsData(){

        VBox[] weekDaysBoxes = {vboxMonday, vboxTuesday, vboxWednesday, vboxThursday, vboxFriday, vboxSaturday, vboxSunday};

        for (VBox box : weekDaysBoxes) {
            box.getChildren().clear();
        }

        if(currentWeekAssignments == null || currentWeekAssignments.isEmpty()){
            return;
        }

        //populate boxes with current data
        for (ChoreAssignmentResponseDTO assignment : currentWeekAssignments) {

            VBox assignmentContainer = new VBox();
            assignmentContainer.setAlignment(Pos.CENTER);
            assignmentContainer.getStyleClass().add("task-box");
            assignmentContainer.setOnMouseClicked(e -> showDetails(assignment));

            assignmentContainer.getStyleClass().add("task-box-" + assignment.status().toString().toLowerCase());

            Label lblDescription = new Label(assignment.choreDescription());
            lblDescription.setWrapText(true);
            lblDescription.getStyleClass().add("task-title");

            Label lblAssignee = new Label(assignment.assignedUser().name() + " " + assignment.assignedUser().surname());
            lblAssignee.setWrapText(true);
            lblAssignee.getStyleClass().add("task-assignee");

            assignmentContainer.getChildren().addAll(lblDescription, lblAssignee);
            weekDaysBoxes[assignment.dueDate().getDayOfWeek().getValue() - 1].getChildren().add(assignmentContainer);
        }
    }

    private void markAsComplete(ChoreAssignmentResponseDTO assignment){
        CompletableFuture.runAsync(() -> {
            try {
                services.getChoreClientService().updateChoreAssignmentStatus(assignment.assignmentId(),
                        new ChoreStatusUpdateRequestDTO(ChoreStatus.COMPLETED)
                );
                Platform.runLater(() -> {
                    mainController.showToast("Assignment marked as complete!", MessageType.SUCCESS);
                    mainController.disableRefreshButton(false);
                    fetchAndDisplayAssignmentsOverview();
                    fetchAndDisplayAssignmentsData();
                    handleCloseDetails();
                });
            }catch (RuntimeException e){
                Platform.runLater(() -> mainController.showToast("Failed to mark assignment as complete: " + e.getMessage(), MessageType.ERROR));
            }
        });
    }

    private void deleteAssignment(ChoreAssignmentResponseDTO assignment){
        CompletableFuture.runAsync(() -> {
            try {
                services.getChoreClientService().deleteChoreAssignment(assignment.assignmentId());
                Platform.runLater(() -> {
                    mainController.showToast("Assignment deleted!", MessageType.SUCCESS);
                    mainController.disableRefreshButton(false);
                    fetchAndDisplayAssignmentsOverview();
                    fetchAndDisplayAssignmentsData();
                    handleCloseDetails();
                });
            }catch (RuntimeException e){
                Platform.runLater(() -> mainController.showToast("Failed to delete assignment: " + e.getMessage(), MessageType.ERROR));
            }
        });
    }

    private String formatCurrentWeekString() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

        String startWeek = selectedWeek.startDate().format(formatter);
        String endWeek = selectedWeek.endDate().format(formatter);

        String year = Objects.equals(selectedWeek.startDate().getYear(), selectedWeek.endDate().getYear()) ?
                String.valueOf(selectedWeek.startDate().getYear()) :
                selectedWeek.startDate().getYear() + "/" + selectedWeek.endDate().getYear();

        return startWeek + " - " + endWeek + " " + year;
    }

    private void triggerFetchAndDisplay() {
        searchTimer.playFromStart();
    }
}