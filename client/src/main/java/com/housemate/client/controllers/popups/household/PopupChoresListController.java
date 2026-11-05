package com.housemate.client.controllers.popups.household;

import com.housemate.client.components.ChoreItemElement;
import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PopupChoresListController {

    private final AppServices services;
    private final MainController mainController;

    private final Runnable onCreateChoreCallback;

    private List<ChoreResponseDTO> retrievedChores = new ArrayList<>();

    @Setter
    private boolean isAdminMode;

    @FXML private StackPane popupRulesAndChores;
    @FXML private VBox choresListContainer;

    public PopupChoresListController(AppServices services, MainController mainController, Runnable onCreateChoreCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onCreateChoreCallback = onCreateChoreCallback;
    }

    @FXML
    public void handleOpenCreateChore(){
        onCreateChoreCallback.run();
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupRulesAndChores);
    }

    private void deleteSelectedChore(ChoreResponseDTO chore) {

        CompletableFuture.runAsync(() -> {

            try {
                services.getChoreClientService().deleteChore(chore.id());

                Platform.runLater(() -> {
                    fetchChoresData();
                    mainController.showToast("Chore deleted successfully", MessageType.SUCCESS);
                });
            }catch (RuntimeException e){
                Platform.runLater(()-> mainController.showToast(e.getMessage(), MessageType.ERROR));
            }

        });

    }

    public void fetchChoresData() {
        CompletableFuture.runAsync(() -> {
            try {
                retrievedChores = services.getChoreClientService().getAllHouseholdChores();

                Platform.runLater(() -> {

                    choresListContainer.getChildren().clear();

                    for (var chore : retrievedChores){

                        ChoreItemElement choreBox = new ChoreItemElement(chore,
                            () -> mainController.requestConfirmForAction(
                                "Are you sure you want to delete chore " + chore.description() + "?",
                                () -> deleteSelectedChore(chore)
                            ),
                            isAdminMode
                        );

                        choresListContainer.getChildren().add(choreBox);
                    }
                });
            } catch (RuntimeException e) {
                Platform.runLater(() -> mainController.showToast(e.getMessage(), MessageType.ERROR));
            }
        });
    }

}