package com.housemate.client.controllers;

import com.housemate.client.controllers.tabs.TabAssignmentsController;
import com.housemate.client.controllers.tabs.TabExpensesController;
import com.housemate.client.controllers.tabs.TabHouseholdController;
import com.housemate.client.controllers.tabs.TabUserController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class MainController {

    @FXML private Button btnNavH, btnNavC, btnNavE, btnNavU;
    @FXML private StackPane mainContentContainer;
    @FXML private StackPane tabsContainer;
    @FXML private StackPane popupLayer;

    private VBox tabHousehold, tabAssignments, tabExpenses, tabUser;

    private AppServices services;

    public MainController(AppServices services) {
        this.services = services;
    }

    @FXML
    public void initialize() {

        loadTabs();

        btnNavH.setOnAction(e -> switchTab(tabHousehold, btnNavH));
        btnNavC.setOnAction(e -> switchTab(tabAssignments, btnNavC));
        btnNavE.setOnAction(e -> switchTab(tabExpenses, btnNavE));
        btnNavU.setOnAction(e -> switchTab(tabUser, btnNavU));

    }

    private void loadTabs() {
        try {
            // Load Household Tab
            FXMLLoader loaderHousehold = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/tab_household.fxml"));
            loaderHousehold.setControllerFactory(clazz -> new TabHouseholdController(this.services, this));
            tabHousehold = loaderHousehold.load();
            tabsContainer.getChildren().add(tabHousehold);

            // Load Assignments Tab
            FXMLLoader loaderAssignments = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/tab_assignments.fxml"));
            loaderAssignments.setControllerFactory(clazz -> new TabAssignmentsController(this.services, this));
            tabAssignments = loaderAssignments.load();
            tabAssignments.setVisible(false);
            tabAssignments.setManaged(false);
            tabsContainer.getChildren().add(tabAssignments);

            // Load Expenses Tab
            FXMLLoader loaderExpenses = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/tab_expenses.fxml"));
            loaderExpenses.setControllerFactory(clazz -> new TabExpensesController(this.services, this));
            tabExpenses = loaderExpenses.load();
            tabExpenses.setVisible(false);
            tabExpenses.setManaged(false);
            tabsContainer.getChildren().add(tabExpenses);

            // Load User Tab
            FXMLLoader loaderUser = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/tab_user.fxml"));
            loaderUser.setControllerFactory(clazz -> new TabUserController(this.services, this));
            tabUser = loaderUser.load();
            tabUser.setVisible(false);
            tabUser.setManaged(false);
            tabsContainer.getChildren().add(tabUser);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openPopup(StackPane popup) {
        if (popup != null && mainContentContainer != null && popupLayer != null) {
            popupLayer.setMouseTransparent(false);
            mainContentContainer.setEffect(new GaussianBlur(15));
            popup.setVisible(true);
            popup.setManaged(true);
            enableNavigationButtons(false);
        }
    }

    public void closePopup(StackPane popup) {
        if (popup != null && mainContentContainer != null && popupLayer != null) {
            popupLayer.setMouseTransparent(true);
            mainContentContainer.setEffect(null);
            popup.setVisible(false);
            popup.setManaged(false);
            enableNavigationButtons(true);
        }
    }

    public void addPopupToLayer(StackPane popup) {
        if (popupLayer != null && popup != null) {
            popupLayer.getChildren().add(popup);
        }
    }

    public void enableNavigationButtons(boolean enable) {
        btnNavH.setDisable(!enable);
        btnNavC.setDisable(!enable);
        btnNavE.setDisable(!enable);
        btnNavU.setDisable(!enable);
    }

    private void switchTab(VBox activeTab, Button activeButton) {

        tabHousehold.setVisible(false); tabHousehold.setManaged(false);
        tabAssignments.setVisible(false); tabAssignments.setManaged(false);
        tabExpenses.setVisible(false); tabExpenses.setManaged(false);
        tabUser.setVisible(false); tabUser.setManaged(false);

        activeTab.setVisible(true); activeTab.setManaged(true);

        btnNavH.getStyleClass().remove("nav-button-active");
        btnNavH.getStyleClass().add("nav-button-inactive");
        btnNavC.getStyleClass().remove("nav-button-active");
        btnNavC.getStyleClass().add("nav-button-inactive");
        btnNavE.getStyleClass().remove("nav-button-active");
        btnNavE.getStyleClass().add("nav-button-inactive");
        btnNavU.getStyleClass().remove("nav-button-active");
        btnNavU.getStyleClass().add("nav-button-inactive");

        activeButton.getStyleClass().remove("nav-button-inactive");
        activeButton.getStyleClass().add("nav-button-active");
    }
}