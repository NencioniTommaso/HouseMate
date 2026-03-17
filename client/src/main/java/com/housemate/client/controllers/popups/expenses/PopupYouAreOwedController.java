package com.housemate.client.controllers.popups.expenses;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.tabs.TabExpensesController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class PopupYouAreOwedController {

    private AppServices services;
    private final MainController mainController;
    private final TabExpensesController parentTab;

    @FXML private StackPane popupOverlayOwed;
    @FXML private Button btnClosePopupOwed;

    public PopupYouAreOwedController(AppServices services, MainController mainController, TabExpensesController parentTab) {
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
        mainController.closePopup(popupOverlayOwed);
    }
}

