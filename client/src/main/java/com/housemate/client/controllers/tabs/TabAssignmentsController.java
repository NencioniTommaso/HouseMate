package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.assignments.PopupAssignmentController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TabAssignmentsController {

    @FXML private FlowPane searchFiltersPanel;
    @FXML private Button btnPrevWeek, btnNextWeek;
    @FXML private VBox vboxMonday, vboxTuesday, vboxWednesday, vboxThursday, vboxFriday, vboxSaturday, vboxSunday;
    @FXML private VBox detailsPane;
    @FXML private Label lblDetailTitle, lblDetailDesc, lblDetailUser, lblDetailDate, lblDetailStatus;

    private StackPane popupAddAssignment;

    private AppServices services;
    private final MainController mainController;

    public TabAssignmentsController(AppServices services, MainController mainController) {

        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

        setupAssignmentListeners();

        loadPopups();
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
                    clazz -> new PopupAssignmentController(this.services, this.mainController, this));
            popupAddAssignment = loaderAddAssignment.load();
            mainController.addPopupToLayer(popupAddAssignment);
            popupAddAssignment.setVisible(false);
            popupAddAssignment.setManaged(false);
        } catch (IOException e) {
            throw new RuntimeException("Error loading popup: " + e.getMessage(), e);
        }
    }
}








