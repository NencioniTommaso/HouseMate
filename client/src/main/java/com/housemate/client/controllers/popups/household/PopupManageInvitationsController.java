package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.enums.InvitationMode;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

public class PopupManageInvitationsController {

    @FXML private Label titleLabel;
    @FXML private ScrollPane invitationsListView;
    @FXML private StackPane popupManageInvitations;

    private InvitationMode currentMode;
    private Runnable onDataChanged;

    private final AppServices services;
    private final MainController mainController;

    public PopupManageInvitationsController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    public void initData(InvitationMode mode, Runnable onDataChanged) {
        this.currentMode = mode;
        this.onDataChanged = onDataChanged;

        if (mode == InvitationMode.RECEIVED) {
            titleLabel.setText("Incoming Invitations");

        } else {
            titleLabel.setText("Outgoing Invitations");
        }
    }

    @FXML
    public void handlePopupClosing(){
        mainController.closePopup(popupManageInvitations);
        if (currentMode == InvitationMode.RECEIVED) {
            mainController.setNoHouseholdMode();

        }
    }

}