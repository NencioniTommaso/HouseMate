package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.assignments.PopupAssignmentController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.ChoreStatus;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TabAssignmentsController {

    @FXML private FlowPane searchFiltersPanel;
    @FXML private Button btnPrevWeek, btnNextWeek;
    @FXML private VBox vboxMonday, vboxTuesday, vboxWednesday, vboxThursday, vboxFriday, vboxSaturday, vboxSunday;
    @FXML private VBox detailsPane;
    @FXML private Label lblDetailTitle, lblDetailDesc, lblDetailUser, lblDetailDate, lblDetailStatus;
    @FXML private Button btnComplete, btnDelete;

    private StackPane popupAddAssignment;

    private AppServices services;
    private final MainController mainController;

    private PopupAssignmentController popupAssignmentController;

    private List<ChoreAssignmentResponseDTO> currentWeekAssignments = new ArrayList<>();

    public TabAssignmentsController(AppServices services, MainController mainController) {

        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

        loadPopups();

        currentWeekAssignments = fetchAssignmentsData();

        displayAssignmentsData();

        setupAssignmentListeners();

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
    private void handleAddAssignment() {
        popupAssignmentController.fetchChoresData();
        popupAssignmentController.reloadMemberSelection();
        mainController.openPopup(popupAddAssignment);
    }

    @FXML
    private void handleClearFilters() {

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

        btnComplete.setOnAction(e -> markAsComplete(assignment));
        btnDelete.setOnAction(e -> deleteAssignment(assignment));

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




    }

    private void deleteAssignment(ChoreAssignmentResponseDTO assignment){

    }
}








