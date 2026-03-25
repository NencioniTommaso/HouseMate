package com.housemate.client.controllers.popups.household;


import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class PopupListDetailsController {

    @FXML private StackPane popupListDetails;
    @FXML private VBox listDetailsContainer;

    private final AppServices services;
    private final MainController mainController;

    private final Runnable onReturnCallback;

    private final ShoppingListResponseDTO selectedList;
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

        checkBoxList.clear();

        for (var item : selectedList.items()) {
            CheckBox checkBox = new CheckBox(item.getItemName());
            checkBox.setSelected(item.isBought());

            listDetailsContainer.getChildren().add(checkBox);

            checkBoxList.add(checkBox);


        }

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