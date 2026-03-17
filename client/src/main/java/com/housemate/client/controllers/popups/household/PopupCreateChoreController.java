package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.tabs.TabHouseholdController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class PopupCreateChoreController {

    private AppServices services;
    private final MainController mainController;
    private final TabHouseholdController parentTab;

    @FXML private StackPane popupCreateChore;
    @FXML private Button btnReturn;
    @FXML private Button btnCloseCreateChore;
    @FXML private TextField txtChoreDesc;
    @FXML private Spinner<?> spnFrequencyDays;
    @FXML private Button btnCreateAssignment;
    @FXML private Button btnCancelAssignment;

    public PopupCreateChoreController(AppServices services, MainController mainController, TabHouseholdController parentTab) {
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
        mainController.closePopup(popupCreateChore);
    }

}

