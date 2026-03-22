package com.housemate.client.controllers.tabs.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.household.PopupCreateHouseholdController;
import com.housemate.client.controllers.popups.household.PopupManageInvitationsController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.enums.InvitationMode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.function.Consumer;

public class TabNoHouseholdController {

    private StackPane manageInvitationsPopup;
    private StackPane createHouseholdPopup;

    private final AppServices services;
    private final MainController mainController;

    private final Consumer<HouseholdResponseDTO> onHouseholdChangeCallback;

    public TabNoHouseholdController(AppServices services, MainController mainController, Consumer<HouseholdResponseDTO> onHouseholdChangeCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onHouseholdChangeCallback = onHouseholdChangeCallback;
    }

    @FXML
    public void initialize() {
        mainController.setNoHouseholdMode();

        loadPopups();
    }

    @FXML
    public void handleOpenInvitations() {
        mainController.openPopup(manageInvitationsPopup);
    }

    @FXML
    public void handleOpenCreateHousehold() {
        mainController.openPopup(createHouseholdPopup);
    }

    private void loadPopups() {

        try {

            //Open invitations popup
            FXMLLoader loaderManageInvitations = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/household/popup_manage_invitations.fxml"));
            loaderManageInvitations.setControllerFactory(clazz -> new PopupManageInvitationsController(this.services, this.mainController));
            manageInvitationsPopup = loaderManageInvitations.load();
            PopupManageInvitationsController manageInvitationsController = loaderManageInvitations.getController();
            manageInvitationsController.initData(InvitationMode.RECEIVED, () -> {

            });
            mainController.addPopupToLayer(manageInvitationsPopup);

            //Open create household popup
            FXMLLoader loaderCreateHousehold = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/household/popup_create_household.fxml"));
            loaderCreateHousehold.setControllerFactory(clazz -> new PopupCreateHouseholdController(this.services, this.mainController, onHouseholdChangeCallback));
            createHouseholdPopup = loaderCreateHousehold.load();
            mainController.addPopupToLayer(createHouseholdPopup);


        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
