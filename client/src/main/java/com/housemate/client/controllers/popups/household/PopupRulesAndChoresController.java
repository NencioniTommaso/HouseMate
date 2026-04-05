package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PopupRulesAndChoresController {

    private final AppServices services;
    private final MainController mainController;

    private final Runnable onCreateChoreCallback;

    private List<ChoreResponseDTO> retrievedChores = new ArrayList<>();

    @FXML private StackPane popupRulesAndChores;
    @FXML private VBox choresListContainer;

    public PopupRulesAndChoresController(AppServices services, MainController mainController, Runnable onCreateChoreCallback) {
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
                Platform.runLater(()->{
                    mainController.showToast(e.getMessage(), MessageType.ERROR);
                });
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

                        HBox choreBox = new HBox();
                        choreBox.getStyleClass().add("chore-item");
                        choreBox.setAlignment(Pos.CENTER_LEFT);
                        choreBox.setSpacing(10);

                        VBox labelsBox = new VBox();
                        HBox.setHgrow(labelsBox, Priority.ALWAYS);

                        Label lblChoreDesc = new Label();
                        lblChoreDesc.getStyleClass().add("chore-title");
                        lblChoreDesc.setText(chore.description());
                        Label lblChoreFreq = new Label();
                        lblChoreFreq.getStyleClass().add("chore-detail");
                        if(chore.frequencyDays() == 0){
                            lblChoreFreq.setText("Frequency: not periodical");
                        }else if (chore.frequencyDays() == 1){
                            lblChoreFreq.setText("Frequency: every day");
                        }else{
                            lblChoreFreq.setText("Frequency: every " + chore.frequencyDays() + " days");
                        }

                        VBox buttonBox = new VBox();
                        buttonBox.setAlignment(Pos.CENTER_RIGHT);
                        buttonBox.setSpacing(5);

                        Button btnDeleteChore = new Button();
                        btnDeleteChore.getStyleClass().add("btn-delete");
                        btnDeleteChore.setText("Delete");
                        btnDeleteChore.setOnAction(e -> {
                            mainController.requestConfirmForAction(
                                    "Are you sure you want to delete chore " + chore.description() + "?",
                                    () -> deleteSelectedChore(chore)
                            );
                        });

                        labelsBox.getChildren().addAll(lblChoreDesc, lblChoreFreq);
                        buttonBox.getChildren().addAll(btnDeleteChore);
                        choreBox.getChildren().addAll(labelsBox, buttonBox);

                        choresListContainer.getChildren().add(choreBox);
                    }

                });
            } catch (RuntimeException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    mainController.showToast(e.getMessage(), MessageType.ERROR);
                });
            }
        });
    }

}