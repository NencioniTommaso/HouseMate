package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.household.request.HouseholdCreateRequestDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.util.concurrent.CompletableFuture;

public class PopupCreateHouseholdController {

    @FXML private TextField txtHouseholdName;

    @FXML private StackPane popupCreateHousehold;

    private final AppServices services;
    private final MainController mainController;

    public PopupCreateHouseholdController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void handlePopupClosing() {
        txtHouseholdName.clear();
        mainController.closePopup(popupCreateHousehold);
    }

    @FXML
    public void handleHouseholdCreation(){

        CompletableFuture.runAsync(() -> {
            try{

                HouseholdResponseDTO newHousehold = services.getHouseholdClientService().createHousehold(
                        new HouseholdCreateRequestDTO(txtHouseholdName.getText())
                );

                services.getSessionManager().setCurrentHousehold(newHousehold);

                Platform.runLater(() -> {
                   mainController.showToast("Household " + services.getSessionManager().getCurrentHousehold().name() + " created successfully!", MessageType.SUCCESS);
                   mainController.closePopup(popupCreateHousehold);
                   mainController.refreshDataAndReload();
                });

            }catch (RuntimeException e){
                Platform.runLater(() -> mainController.showToast("Household creation failed: " + e.getMessage(), MessageType.ERROR));
            }
        });
    }

}
