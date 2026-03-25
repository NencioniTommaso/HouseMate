package com.housemate.client.controllers.popups.household;


import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

public class PopupListDetailsController {

    @FXML private StackPane popupListDetails;

    private final AppServices services;
    private final MainController mainController;

    private final Runnable onReturnCallback;

    private ShoppingListResponseDTO selectedList;
    private final List<CheckBox> checkBoxList = new ArrayList<>();

    public PopupListDetailsController(AppServices services,
                                      MainController mainController,
                                      Runnable onReturnCallback,
                                      ShoppingListResponseDTO selectedList) {
        this.services = services;
        this.mainController = mainController;
        this.onReturnCallback = onReturnCallback;
        this.selectedList = selectedList;
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