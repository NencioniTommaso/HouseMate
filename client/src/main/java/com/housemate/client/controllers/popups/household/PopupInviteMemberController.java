package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.tabs.household.TabHouseholdController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Random;

public class PopupInviteMemberController {

    private AppServices services;
    private final MainController mainController;

    @FXML private StackPane popupInviteMember;
    @FXML private Button btnCloseInviteMember;
    @FXML private Label lblInvitationCode;

    public PopupInviteMemberController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Generate a random 6-digit invitation code
        Random rand = new Random();
        lblInvitationCode.setText(rand.nextInt(100000, 999999) + "");
    }

    @FXML
    public void handleRefreshCode() {
        Random rand = new Random();
        lblInvitationCode.setText(rand.nextInt(100000, 999999) + "");
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupInviteMember);
    }

}

