package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.chore.response.AssignmentOverviewDTO;
import com.housemate.shared.dto.user.request.UserUpdateRequestDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

public class TabUserController {

    @FXML private VBox cardYouSpend, cardCompletedAssignments, cardExpiredAssignments;
    @FXML private Label lblAmountSpent, lblPendingAssignments, lblOverdueAssignments;
    @FXML private HBox boxEditActions;
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
        fetchAndDisplayUserData();
        fetchAndDisplayCardsData();
    }

    @FXML
    public void handleLogout() {
        mainController.requestConfirmForAction("Are you sure you want to log out?", logoutHandler);
    }

    @FXML
    public void handleLeaveCurrentHousehold() {
        mainController.requestConfirmForAction(
                "Are you sure you want to leave your current household?",
                () -> CompletableFuture.runAsync(() -> {
            try {
                services.getHouseholdClientService().leaveHousehold();
                services.setCurrentHousehold(null);

                Platform.runLater(() -> {
                    mainController.showToast("You have left your household.", MessageType.SUCCESS);
                    mainController.refreshDataAndReload();
                });

            } catch (RuntimeException e) {
                Platform.runLater(() -> mainController.showToast("Failed to leave household: " + e.getMessage(), MessageType.ERROR));
            }
        }));
    }

    @FXML
    public void handleSaveProfile() {

        CompletableFuture.runAsync(() -> {
            try{
                services.getUserClientService().updateCurrentUser(
                        new UserUpdateRequestDTO(
                                txtFirstName.getText(),
                                txtLastName.getText(),
                                txtEmail.getText(),
                                txtIban.getText().isBlank() ? null : txtIban.getText(),
                                txtPaymentLink.getText().isBlank() ? null : txtPaymentLink.getText()
                        )
                );

                Platform.runLater(() -> {
                    mainController.showToast("Profile updated successfully.", MessageType.SUCCESS);
                    setEditMode(false);
                    fetchAndDisplayUserData();
                });

            } catch (RuntimeException e) {
                Platform.runLater(() -> mainController.showToast("Failed to update profile: " + e.getMessage(), MessageType.ERROR));
            }
        });
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

        boxEditActions.setVisible(editing);
        boxEditActions.setManaged(editing);

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

        txtFirstName.setText(editing ? lblFirstName.getText() : "");
        txtLastName.setText(editing ? lblLastName.getText() : "");
        txtEmail.setText(editing ? lblEmail.getText() : "");
        txtIban.setText(editing ? lblIban.getText() : "");
        txtPaymentLink.setText(editing ? lblPaymentLink.getText() : "");
    }

    public void fetchAndDisplayUserData() {
        CompletableFuture.runAsync(() -> {
            try{
                var currentUser = services.getUserClientService().getCurrentUser();

                Platform.runLater(() -> {
                    lblFirstName.setText(currentUser.name());
                    lblLastName.setText(currentUser.surname());
                    lblEmail.setText(currentUser.email());
                    lblIban.setText(currentUser.iban() != null ? currentUser.iban() : "");
                    lblPaymentLink.setText(currentUser.paymentLink() != null ? currentUser.paymentLink() : "");
                });

            } catch (RuntimeException e) {
                Platform.runLater(() -> mainController.showToast("Failed to load user data: " + e.getMessage(), MessageType.ERROR));
            }
        });
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

    public void fetchAndDisplayCardsData() {

        if(services.getCurrentHousehold() == null){
            return;
        }

        CompletableFuture.runAsync(() -> {
           try{
               var currentMonthNetOverview = services.getExpenseClientService().getCurrentMonthUserNetOverview();

               AssignmentOverviewDTO currentUserOverview = services.getChoreClientService().getUserAssignmentOverview();

               Platform.runLater(() -> {
                   lblAmountSpent.setText("€ " + currentMonthNetOverview.actualCashFlowAmount().setScale(2, RoundingMode.HALF_UP));
                   lblPendingAssignments.setText(String.valueOf(currentUserOverview.pendingAssignments()));
                   lblOverdueAssignments.setText(String.valueOf(currentUserOverview.overdueAssignments()));
               });

           }catch (RuntimeException e){
               Platform.runLater(() -> mainController.showToast("Failed to load card data: " + e.getMessage(), MessageType.ERROR));
           }
        });
    }
}

