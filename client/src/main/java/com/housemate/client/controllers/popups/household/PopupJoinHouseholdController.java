package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.enums.InvitationMode;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

public class PopupJoinHouseholdController {

    @FXML private Label titleLabel;
    @FXML private ScrollPane invitationsListView;
    @FXML private StackPane popupManageInvitations;

    private final AppServices services;
    private final MainController mainController;

    public PopupJoinHouseholdController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }


    @FXML
    public void handleJoinHousehold(){
        mainController.closePopup(popupManageInvitations);

    }

    @FXML
    public void handlePopupClosing(){
        mainController.closePopup(popupManageInvitations);
    }

}