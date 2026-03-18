package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;

public class TabUserController {

    private AppServices services;
    private MainController mainController;
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
        logoutHandler.run();
    }
}

