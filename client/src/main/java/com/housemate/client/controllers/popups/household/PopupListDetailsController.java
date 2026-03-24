package com.housemate.client.controllers.popups.household;


import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class PopupListDetailsController {

    @FXML private StackPane popupListDetails;

    private final AppServices services;
    private final MainController mainController;

    private final Runnable onReturnCallback;

    public PopupListDetailsController(AppServices services, MainController mainController, Runnable onReturnCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onReturnCallback = onReturnCallback;
    }

    @FXML
    public void initialize() {

    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupListDetails);
    }

    @FXML
    public void handleReturnToLists() {
        mainController.closePopup(popupListDetails);
        onReturnCallback.run();
    }
}