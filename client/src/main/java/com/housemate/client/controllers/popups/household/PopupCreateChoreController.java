package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.chore.request.ChoreCreateRequestDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.concurrent.CompletableFuture;

public class PopupCreateChoreController {

    private AppServices services;
    private final MainController mainController;

    private final Runnable onReturnCallback;

    @FXML private StackPane popupCreateChore;
    @FXML private TextField txtChoreDesc;
    @FXML private Spinner<Integer> spnFrequencyDays;
    @FXML private Label lblError;
    @FXML private Label lblSuccess;


    public PopupCreateChoreController(AppServices services, MainController mainController, Runnable onReturnCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onReturnCallback = onReturnCallback;
    }

    @FXML
    public void initialize() {

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 7);
        spnFrequencyDays.setValueFactory(valueFactory);
    }

    @FXML
    public void handlePopupClosing() {
        clearFields();
        mainController.closePopup(popupCreateChore);
    }

    @FXML
    public void handleChoreCreation() {

        ChoreCreateRequestDTO requestDTO = new ChoreCreateRequestDTO(
            txtChoreDesc.getText(),
            spnFrequencyDays.getValue(),
            services.getCurrentHousehold().id()
        );

        CompletableFuture.runAsync(() -> {
            try {
                services.getChoreClientService().createChore(requestDTO);
                Platform.runLater(() -> {
                    clearFields();
                    mainController.showToast("Chore created successfully!", MessageType.SUCCESS);
                });
            } catch (RuntimeException e) {

                Platform.runLater(() -> {
                    mainController.showToast(e.getMessage(), MessageType.ERROR);
                });
            }
        });



    }

    @FXML
    public void handleReturnToChores() {
        clearFields();
        onReturnCallback.run();
    }

    private void clearFields(){

        lblError.setVisible(false);
        lblError.setManaged(false);
        lblSuccess.setVisible(false);
        lblSuccess.setManaged(false);


        txtChoreDesc.clear();
        spnFrequencyDays.getEditor().clear();
    }

}

