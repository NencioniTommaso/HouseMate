package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PopupShoppingListsController {

    private final AppServices services;
    private final MainController mainController;

    private final Consumer<ShoppingListResponseDTO> onOpenDetailsCallback;
    private final Runnable onOpenCreateListCallback;

    @FXML private StackPane popupShoppingLists;
    @FXML private VBox listsContainer;

    public PopupShoppingListsController(AppServices services,
                                        MainController mainController,
                                        Consumer<ShoppingListResponseDTO> onOpenDetailsCallback,
                                        Runnable onOpenCreateListCallback) {
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
    public void handleOpenCreateList(){
        onOpenCreateListCallback.run();
    }

    public void fetchListsData() {
        listsContainer.getChildren().clear();

        CompletableFuture.runAsync(() -> {
            try {
                var lists = services.getShoppingListClientService().getShoppingItemsByHousehold(services.getCurrentHousehold().id());

                Platform.runLater(() -> {
                    for (var list : lists) {

                        HBox listBox = new HBox();
                        listBox.getStyleClass().add("shopping-item");


                        VBox leftContainer = new VBox();
                        leftContainer.setAlignment(Pos.CENTER_LEFT);
                        Label lblListName = new Label(list.name());
                        lblListName.getStyleClass().add("shopping-title");
                        Label lblListDate = new Label("Created on: " + list.creationDate());
                        lblListDate.getStyleClass().add("shopping-date");

                        Region spacer = new  Region();
                        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                        VBox rightContainer = new VBox();
                        rightContainer.setAlignment(Pos.CENTER_RIGHT);
                        Label lblListStatus = new Label("Status: " + String.valueOf(list.status()).replace("_", " "));
                        lblListStatus.getStyleClass().add("popup-label");
                        Button btnDetails = new Button("Details");
                        btnDetails.getStyleClass().add("shopping-button");

                        btnDetails.setOnAction(e -> onOpenDetailsCallback.accept(list));

                        leftContainer.getChildren().addAll(lblListName, lblListDate);
                        rightContainer.getChildren().addAll(lblListStatus, btnDetails);
                        listBox.getChildren().addAll(leftContainer, spacer, rightContainer);

                        listsContainer.getChildren().add(listBox);

                        mainController.showToast("Shopping lists loaded successfully!", MessageType.SUCCESS);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> mainController.showToast(e.getMessage(), MessageType.ERROR));
            }
        });
    }
}