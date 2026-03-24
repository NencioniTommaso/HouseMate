package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PopupShoppingListsController {

    private AppServices services;
    private final MainController mainController;

    private final Runnable onOpenDetailsCallback;
    private final Runnable onOpenCreateListCallback;

    @FXML private StackPane popupShoppingLists;
    @FXML private VBox listsContainer;

    public PopupShoppingListsController(AppServices services, MainController mainController, Runnable onOpenDetailsCallback, Runnable onOpenCreateListCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onOpenDetailsCallback = onOpenDetailsCallback;
        this.onOpenCreateListCallback = onOpenCreateListCallback;
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupShoppingLists);
    }

    @FXML
    public void handleOpenDetails() {
        onOpenDetailsCallback.run();
    }

    @FXML
    public void handleOpenCreateList(){
        onOpenCreateListCallback.run();
    }
}