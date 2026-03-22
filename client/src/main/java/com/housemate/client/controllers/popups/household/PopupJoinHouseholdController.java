package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

import java.util.UUID;

public class PopupJoinHouseholdController {

    @FXML private Label titleLabel;
    @FXML private ScrollPane invitationsListView;
    @FXML private StackPane popupJoinHousehold;

    private final AppServices services;
    private final MainController mainController;

    public PopupJoinHouseholdController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }


    @FXML
    public void handleJoinHousehold(){

        HouseholdResponseDTO household = new HouseholdResponseDTO(UUID.randomUUID(), "Household Example", null, null);
        //this gets the real household from the service

        mainController.reloadApplicationState(household);
        mainController.closePopup(popupJoinHousehold);
    }

    @FXML
    public void handlePopupClosing(){
        mainController.closePopup(popupJoinHousehold);
    }

}