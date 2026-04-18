package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.enums.MessageType;
import com.housemate.shared.utils.types.ListItem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PopupCreateListController {

    private final AppServices services;
    private final MainController mainController;

    private final Runnable onReturnCallback;

    final List<String> currentItems = new ArrayList<>();

    @FXML private VBox itemListContainer;
    @FXML private StackPane popupCreateList;
    @FXML private TextField txtAddItem;
    @FXML private TextField txtListName;

    public PopupCreateListController(AppServices services, MainController mainController, Runnable onReturnCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onReturnCallback = onReturnCallback;
    }

    @FXML
    public void handlePopupClosing() {

        clearFields();
        mainController.closePopup(popupCreateList);
    }


    @FXML
    public void handleReturnToLists() {

        clearFields();
        onReturnCallback.run();
    }

    @FXML
    public void handleListCreation() {

        List<ListItem> listItems = new ArrayList<>();

        for (String itemName : currentItems) {
            listItems.add(new ListItem(itemName));
        }

        if (listItems.isEmpty()) {
            mainController.showToast("Please add at least one item to the list!", MessageType.INFO);
            return;
        }

        ShoppingListCreateRequestDTO requestDTO = new ShoppingListCreateRequestDTO(
                txtListName.getText(), listItems, services.getCurrentHousehold().id(), LocalDate.now()
        );

        CompletableFuture.runAsync(() -> {
            try {
                services.getShoppingListClientService().createShoppingList(requestDTO);

                Platform.runLater(() -> {
                   mainController.showToast("Shopping list created successfully!", MessageType.SUCCESS);
                   onReturnCallback.run();
                });
            } catch (Exception e) {
                Platform.runLater(() -> mainController.showToast(e.getMessage(), MessageType.ERROR));
            }
        });

        clearFields();
        onReturnCallback.run();
    }

    @FXML
    public void handleAddItem(){

        if(txtAddItem.getText().isEmpty()){
            return;
        }

        if(currentItems.stream().anyMatch(item -> item.equalsIgnoreCase(txtAddItem.getText()))){
            mainController.showToast("Item already present!", MessageType.INFO);
            return;
        }

        HBox newItem =  new HBox();
        newItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label newItemLabel = new Label(txtAddItem.getText());
        newItemLabel.getStyleClass().add("standard-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().add("danger-button");
        removeButton.setOnAction(e -> {
            itemListContainer.getChildren().remove(newItem);
            currentItems.remove(newItemLabel.getText());
        });

        newItem.getChildren().addAll(newItemLabel, spacer, removeButton);
        itemListContainer.getChildren().add(newItem);
        currentItems.add(newItemLabel.getText());
        txtAddItem.clear();
    }

    private void clearFields(){

        currentItems.clear();
        itemListContainer.getChildren().clear();
        txtAddItem.clear();
        txtListName.clear();
    }
}

