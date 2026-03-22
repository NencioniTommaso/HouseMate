package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.tabs.household.TabHouseholdController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class PopupRulesAndChoresController {

    private AppServices services;
    private final MainController mainController;

    private final Runnable onCreateChoreCallback;

    @FXML private StackPane popupRulesAndChores;
    @FXML private Button btnCloseRulesAndChores;
    @FXML private Button btnCreateChore;

    public PopupRulesAndChoresController(AppServices services, MainController mainController, Runnable onCreateChoreCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onCreateChoreCallback = onCreateChoreCallback;
    }

    @FXML
    public void initialize() {
        // TODO: Implementare la logica di inizializzazione
    }

    @FXML
    public void handleOpenCreateChore(){
        onCreateChoreCallback.run();
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupRulesAndChores);
    }
}