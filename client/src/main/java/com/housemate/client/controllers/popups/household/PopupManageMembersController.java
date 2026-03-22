package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class PopupManageMembersController {

    private AppServices services;
    private final MainController mainController;

    @FXML private StackPane popupManageMembers;

    public PopupManageMembersController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;

    }

    @FXML
    public void initialize() {

    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupManageMembers);
    }

}

