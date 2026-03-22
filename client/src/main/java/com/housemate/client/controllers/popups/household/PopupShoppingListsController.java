package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.tabs.household.TabHouseholdController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class PopupShoppingListsController {


    private AppServices services;
    private final MainController mainController;

    private final Runnable onOpenDetailsCallback;

    @FXML private StackPane popupShoppingLists;
    @FXML private Button btnCloseSettings;

    public PopupShoppingListsController(AppServices services, MainController mainController, Runnable onOpenDetailsCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onOpenDetailsCallback = onOpenDetailsCallback;
    }

    @FXML
    public void initialize() {
        // TODO: Implementare la logica di inizializzazione
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupShoppingLists);
    }

    @FXML
    public void handleOpenDetails() {
        onOpenDetailsCallback.run();
    }
}