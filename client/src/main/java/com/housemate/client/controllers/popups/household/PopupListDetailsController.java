package com.housemate.client.controllers.popups.household;


import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    //this popup fetches data on the initialize method because it is reloaded dynamically
    @FXML
    public void initialize() {

        if(selectedList == null || selectedList.items() == null) {
            return;
        }

        checkBoxList.clear();

        for (var item : selectedList.items()) {
            CheckBox checkBox = new CheckBox(item.getItemName());
            checkBox.getStyleClass().add("element-title");
            checkBox.setSelected(item.isBought());
            checkBox.setDisable(item.isBought());

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

    @FXML
    public void handleSaveChanges() {

        List<Boolean> updatedStatuses = checkBoxList.stream()
                .map(CheckBox::isSelected)
                .toList();

        ShoppingListUpdateRequestDTO requestDTO = new ShoppingListUpdateRequestDTO(updatedStatuses);

        CompletableFuture.runAsync(() -> {

            try {
                services.getShoppingListClientService()
                        .updateListInformation(selectedList.id(), requestDTO);

                Platform.runLater(() -> {
                    mainController.showToast("List updated successfully!", MessageType.SUCCESS);
                    handleReturnToLists();
                });

            }catch (RuntimeException e) {
                Platform.runLater(() -> mainController.showToast(e.getMessage(), MessageType.ERROR));
            }
        });
    }
}