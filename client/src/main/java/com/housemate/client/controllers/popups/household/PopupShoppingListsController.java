package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PopupShoppingListsController {

    private AppServices services;
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
    public void handleOpenDetails() {
        onOpenDetailsCallback.accept(null);
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
                        Label lblListName = new Label(list.name());
                        Button btnDetails = new Button("Details");

                        btnDetails.setOnAction(e -> onOpenDetailsCallback.accept(list));

                        listBox.getChildren().addAll(lblListName, btnDetails);
                        listsContainer.getChildren().add(listBox);

                        mainController.showToast("Shopping lists loaded successfully!", MessageType.SUCCESS);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    mainController.showToast(e.getMessage(), MessageType.ERROR);
                });
            }
        }
        );
    }
}