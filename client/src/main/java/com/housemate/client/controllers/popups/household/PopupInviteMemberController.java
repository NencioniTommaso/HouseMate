package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.tabs.TabHouseholdController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class PopupInviteMemberController {

    private AppServices services;
    private final MainController mainController;
    private final TabHouseholdController parentTab;

    @FXML private StackPane popupInviteMember;
    @FXML private Button btnCloseInviteMember;

    public PopupInviteMemberController(AppServices services, MainController mainController, TabHouseholdController parentTab) {
        this.services = services;
        this.mainController = mainController;
        this.parentTab = parentTab;
    }

    @FXML
    public void initialize() {
        // TODO: Implementare la logica di inizializzazione
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupInviteMember);
    }

}

