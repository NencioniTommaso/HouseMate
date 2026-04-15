package com.housemate.client.controllers.tabs.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.household.PopupCreateHouseholdController;
import com.housemate.client.controllers.popups.household.PopupJoinHouseholdController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

import java.io.IOException;


public class TabNoHouseholdController {

    private StackPane joinHouseholdPopup;
    private StackPane createHouseholdPopup;

    private final AppServices services;
    private final MainController mainController;

    public TabNoHouseholdController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadPopups();
    }

    @FXML
    public void handleOpenInvitations() {
        mainController.openPopup(joinHouseholdPopup);
    }

    @FXML
    public void handleOpenCreateHousehold() {
        mainController.openPopup(createHouseholdPopup);
    }

    private void loadPopups() {

        try {
            //open join household popup
            FXMLLoader loaderJoinHousehold = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/household/popup_join_household.fxml"));
            loaderJoinHousehold.setControllerFactory(clazz -> new PopupJoinHouseholdController(this.services, this.mainController));
            joinHouseholdPopup = loaderJoinHousehold.load();
            mainController.addPopupToLayer(joinHouseholdPopup);

            //open create household popup
            FXMLLoader loaderCreateHousehold = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/household/popup_create_household.fxml"));
            loaderCreateHousehold.setControllerFactory(clazz -> new PopupCreateHouseholdController(this.services, this.mainController));
            createHouseholdPopup = loaderCreateHousehold.load();
            mainController.addPopupToLayer(createHouseholdPopup);

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
