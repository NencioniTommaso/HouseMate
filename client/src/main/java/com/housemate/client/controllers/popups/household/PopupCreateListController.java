package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class PopupCreateListController {

    private final AppServices services;
    private final MainController mainController;

    private final Runnable onReturnCallback;

    @FXML private StackPane popupCreateList;
    @FXML private Button btnReturn;
    @FXML private Button btnCloseCreateChore;
    @FXML private TextField txtChoreDesc;
    @FXML private Spinner<Integer> spnFrequencyDays;
    @FXML private Button btnCreateAssignment;
    @FXML private Button btnCancelAssignment;

    public PopupCreateListController(AppServices services, MainController mainController, Runnable onReturnCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onReturnCallback = onReturnCallback;
    }

    @FXML
    public void initialize() {

    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupCreateList);
    }


    @FXML
    public void handleReturnToLists() {
        onReturnCallback.run();
    }

    @FXML
    public void handleListCreation() {

    }

    @FXML
    public void handleAddItem(){

    }

}

