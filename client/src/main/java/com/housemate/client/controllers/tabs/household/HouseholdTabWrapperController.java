package com.housemate.client.controllers.tabs.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class HouseholdTabWrapperController {

    private final AppServices services;
    private final MainController mainController;

    public HouseholdTabWrapperController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
    }


    public void initializeWithUserState(HouseholdResponseDTO activeHousehold) {
        contentArea.getChildren().clear();

        try {
            if (activeHousehold == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/household/tab_no_household.fxml"));
                    loader.setControllerFactory(clazz -> new TabNoHouseholdController(services, mainController, this::initializeWithUserState));
                Node emptyStateView = loader.load();

                contentArea.getChildren().add(emptyStateView);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/household/tab_household.fxml"));
                loader.setControllerFactory(clazz -> new TabHouseholdController(services, mainController));
                Node dashboardView = loader.load();
                contentArea.getChildren().add(dashboardView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}