package com.housemate.client.controllers.popups;

import com.housemate.client.controllers.MainController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PopupConfirmActionController {

    @FXML private Label lblActionDesc;

    private final MainController mainController;
    private final Runnable onConfirmAction;

    private final String confirmDescription;

    public PopupConfirmActionController(MainController mainController,
                                        Runnable onConfirmAction,
                                        String message) {
        this.mainController = mainController;
        this.onConfirmAction = onConfirmAction;
        this.confirmDescription = message;
    }

    @FXML
    public void initialize() {
        lblActionDesc.setText(confirmDescription);
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closeRequestConfirmPopup();
    }

    @FXML
    public void handleConfirmAction() {
        mainController.closeRequestConfirmPopup();
        onConfirmAction.run();
    }
}
