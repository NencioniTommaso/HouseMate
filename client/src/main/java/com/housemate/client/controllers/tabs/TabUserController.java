package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.concurrent.CompletableFuture;

public class TabUserController {

    @FXML private VBox cardYouSpend;
    @FXML private VBox cardCompletedAssignments;
    @FXML private VBox cardExpiredAssignments;
    @FXML private HBox hboxEditActions;
    @FXML private Button btnEditProfile;
    @FXML private Label lblFirstName;
    @FXML private TextField txtFirstName;
    @FXML private Label lblLastName;
    @FXML private TextField txtLastName;
    @FXML private Label lblEmail;
    @FXML private TextField txtEmail;
    @FXML private Label lblPaymentLink;
    @FXML private TextField txtPaymentLink;
    @FXML private Label lblIban;
    @FXML private TextField txtIban;

    @FXML private Button btnLeaveHousehold;

    private final AppServices services;
    private final MainController mainController;
    private final Runnable logoutHandler;

    public TabUserController(AppServices services, MainController mainController,  Runnable logoutHandler) {
        this.services = services;
        this.mainController = mainController;
        this.logoutHandler = logoutHandler;
    }

    @FXML
    public void initialize() {
    }

    @FXML
    public void handleLogout() {
        mainController.requestConfirmForAction("Are you sure you want to log out?", logoutHandler);
    }

    @FXML
    public void handleLeaveCurrentHousehold() {

        mainController.requestConfirmForAction("Are you sure you want to leave your current household?", () -> {

            CompletableFuture.runAsync(() -> {
                try {
                    services.getHouseholdClientService().leaveHousehold();
                    services.setCurrentHousehold(null);

                    Platform.runLater(() -> {
                        mainController.showToast("You have left your household.", com.housemate.shared.enums.MessageType.SUCCESS);
                        mainController.reloadApplicationState();
                    });

                } catch (RuntimeException e) {
                    Platform.runLater(() -> {
                        mainController.showToast("Failed to leave household: " + e.getMessage(), com.housemate.shared.enums.MessageType.ERROR);
                    });
                }
            });


        });
    }

    @FXML
    public void handleSaveProfile() {
        //call client service
        setEditMode(false);
    }

    @FXML
    public void handleEditProfile() {
        setEditMode(true);
    }

    @FXML
    public void handleCancelEdit() {
        setEditMode(false);
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        txtIban.clear();
        txtPaymentLink.clear();
    }

    private void setEditMode(boolean editing) {

        btnEditProfile.setVisible(!editing);
        btnEditProfile.setManaged(!editing);

        hboxEditActions.setVisible(editing);
        hboxEditActions.setManaged(editing);

        lblFirstName.setVisible(!editing);
        txtFirstName.setVisible(editing);

        lblLastName.setVisible(!editing);
        txtLastName.setVisible(editing);

        lblEmail.setVisible(!editing);
        txtEmail.setVisible(editing);

        lblIban.setVisible(!editing);
        txtIban.setVisible(editing);

        lblPaymentLink.setVisible(!editing);
        txtPaymentLink.setVisible(editing);
    }

    public void updateHouseholdState(boolean hasHousehold) {
        btnLeaveHousehold.setVisible(hasHousehold);
        btnLeaveHousehold.setManaged(hasHousehold);

        cardYouSpend.setVisible(hasHousehold);
        cardYouSpend.setManaged(hasHousehold);

        cardCompletedAssignments.setVisible(hasHousehold);
        cardCompletedAssignments.setManaged(hasHousehold);

        cardExpiredAssignments.setVisible(hasHousehold);
        cardExpiredAssignments.setManaged(hasHousehold);
    }
}

