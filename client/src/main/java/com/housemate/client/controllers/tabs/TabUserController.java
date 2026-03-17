package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;

public class TabUserController {

    private AppServices services;
    private MainController mainController;

    public TabUserController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // TODO: Implementare la logica di inizializzazione
    }
}

