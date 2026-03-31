package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import java.util.UUID;
import java.util.function.Consumer;

public class PopupCreateHouseholdController {

    @FXML private StackPane popupCreateHousehold;

    private final AppServices services;
    private final MainController mainController;

    private final Consumer<HouseholdResponseDTO> onHouseholdChangeCallback;

    public PopupCreateHouseholdController(AppServices services, MainController mainController,  Consumer<HouseholdResponseDTO> onHouseholdChangeCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onHouseholdChangeCallback = onHouseholdChangeCallback;
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupCreateHousehold);
    }

    @FXML
    public void handleHouseholdCreation(){

        //call backend
        //HouseholdResponseDTO householdCreationResponse = services.householdService.createHpusejh45yeiru

        //test only
        HouseholdResponseDTO householdCreationResponse = new HouseholdResponseDTO(UUID.randomUUID(), "Test Household", null, null);
        services.setCurrentHousehold(householdCreationResponse);
        mainController.closePopup(popupCreateHousehold);

        mainController.reloadApplicationState(householdCreationResponse);
    }

}
