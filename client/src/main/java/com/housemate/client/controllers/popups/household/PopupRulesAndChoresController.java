package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.tabs.TabHouseholdController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class PopupRulesAndChoresController {

    private AppServices services;
    private final MainController mainController;
    private final TabHouseholdController parentTab;

    @FXML private StackPane popupRulesAndChores;
    @FXML private Button btnCloseRulesAndChores;
    @FXML private Button btnCreateChore;

    public PopupRulesAndChoresController(AppServices services, MainController mainController, TabHouseholdController parentTab) {
        this.services = services;
        this.mainController = mainController;
        this.parentTab = parentTab;
    }

    @FXML
    public void initialize() {
        // TODO: Implementare la logica di inizializzazione
    }

    @FXML
    public void handleOpenCreateChore(){
        mainController.closePopup(popupRulesAndChores);
        mainController.openPopup(parentTab.getPopupCreateChore());
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupRulesAndChores);
    }
}