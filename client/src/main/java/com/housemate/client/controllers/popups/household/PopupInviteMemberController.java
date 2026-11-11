package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

import java.util.concurrent.CompletableFuture;

public class PopupInviteMemberController {

    private final AppServices services;
    private final MainController mainController;

    @FXML private StackPane popupInviteMember;
    @FXML private TextArea lblInvitationCode;
    @FXML private Button btnRefreshCode;

    public PopupInviteMemberController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        fetchInvitationCode();
    }

    @FXML
    public void handleRefreshCode() {
        CompletableFuture.runAsync(() -> {
            try {
                var newCode = services.getHouseholdClientService().refreshInvitationCode();
                Platform.runLater(() -> {
                    lblInvitationCode.setText(newCode.invitationCode());
                    mainController.showToast("Invitation code refreshed successfully!", MessageType.SUCCESS);
                });
            }catch (RuntimeException e){
                Platform.runLater(() -> mainController.showToast("Failed to refresh invitation code: " + e.getMessage(), MessageType.ERROR));
            }
        });
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupInviteMember);
    }

    public void fetchInvitationCode() {
        CompletableFuture.runAsync(() -> {
            try {
                var code = services.getHouseholdClientService().getInvitationCode();
                Platform.runLater(() -> lblInvitationCode.setText(code.invitationCode()));
            }catch (RuntimeException e){
                Platform.runLater(() -> mainController.showToast("Failed to retrieve invitation code: " + e.getMessage(), MessageType.ERROR));
            }
        });
    }

    public void setAdminMode(boolean isAdmin) {
        btnRefreshCode.setDisable(!isAdmin);
    }
}

