package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.household.request.AddMemberRequestDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PopupJoinHouseholdController {

    @FXML private TextField txtInvitationCode;

    @FXML private StackPane popupJoinHousehold;

    private final AppServices services;
    private final MainController mainController;

    public PopupJoinHouseholdController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void handleJoinHousehold(){

        CompletableFuture.runAsync(() -> {
            try{
                HouseholdResponseDTO newHousehold = services.getHouseholdClientService().addMember(new AddMemberRequestDTO(
                        txtInvitationCode.getText()
                ));

                services.setCurrentHousehold(newHousehold);

                Platform.runLater(() -> {
                    mainController.showToast(
                            "Successfully joined household " + services.getCurrentHousehold().name(),
                            MessageType.SUCCESS
                    );
                    mainController.closePopup(popupJoinHousehold);
                    mainController.refreshDataAndReload();
                });
            } catch (RuntimeException e) {
                Platform.runLater(() -> mainController.showToast("Invalid invitation code", MessageType.ERROR));
            }
        });

    }

    @FXML
    public void handlePopupClosing(){
        txtInvitationCode.clear();
        mainController.closePopup(popupJoinHousehold);
    }

}