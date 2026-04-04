package com.housemate.client.controllers;

import com.housemate.client.controllers.popups.PopupConfirmActionController;
import com.housemate.client.controllers.tabs.TabAssignmentsController;
import com.housemate.client.controllers.tabs.TabExpensesController;
import com.housemate.client.controllers.tabs.household.HouseholdTabWrapperController;
import com.housemate.client.controllers.tabs.TabUserController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.enums.MessageType;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

public class MainController {

    @FXML private Button btnNavH, btnNavC, btnNavE, btnNavU;
    @FXML private StackPane outerContainer;
    @FXML private StackPane mainContentContainer;
    @FXML private StackPane popupLayer;
    @FXML private StackPane confirmPopupLayer;

    private Node tabHousehold, tabAssignments, tabExpenses, tabUser;

    private StackPane requestConfirmPopup;
    private boolean isAnotherPopupOpen;

    private final AppServices services;
    private final Runnable logoutHandler;

    private HouseholdTabWrapperController tabWrapperController;
    private TabUserController tabUserController;

    public MainController(AppServices services, Runnable logoutHandler) {
        this.services = services;
        this.logoutHandler = logoutHandler;
        this.isAnotherPopupOpen = false;
    }

    @FXML
    public void initialize() {

        loadTabs();

        btnNavH.setOnAction(e -> switchTab(tabHousehold, btnNavH));
        btnNavC.setOnAction(e -> switchTab(tabAssignments, btnNavC));
        btnNavE.setOnAction(e -> switchTab(tabExpenses, btnNavE));
        btnNavU.setOnAction(e -> switchTab(tabUser, btnNavU));

        reloadApplicationState();
    }

    private void loadTabs() {
        try {
            // Load Household Tab
            FXMLLoader loaderHousehold = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/household/household_tab_wrapper.fxml"));
            loaderHousehold.setControllerFactory(clazz -> new HouseholdTabWrapperController(this.services, this));
            tabHousehold = loaderHousehold.load();
            tabWrapperController = loaderHousehold.getController();
            mainContentContainer.getChildren().add(tabHousehold);

            // Load Assignments Tab
            FXMLLoader loaderAssignments = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/tab_assignments.fxml"));
            loaderAssignments.setControllerFactory(clazz -> new TabAssignmentsController(this.services, this));
            tabAssignments = loaderAssignments.load();
            tabAssignments.setVisible(false);
            tabAssignments.setManaged(false);
            mainContentContainer.getChildren().add(tabAssignments);

            // Load Expenses Tab
            FXMLLoader loaderExpenses = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/tab_expenses.fxml"));
            loaderExpenses.setControllerFactory(clazz -> new TabExpensesController(this.services, this));
            tabExpenses = loaderExpenses.load();
            tabExpenses.setVisible(false);
            tabExpenses.setManaged(false);
            mainContentContainer.getChildren().add(tabExpenses);

            // Load User Tab
            FXMLLoader loaderUser = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/tab_user.fxml"));
            loaderUser.setControllerFactory(clazz -> new TabUserController(this.services, this, logoutHandler));
            tabUser = loaderUser.load();
            tabUserController = loaderUser.getController();
            tabUser.setVisible(false);
            tabUser.setManaged(false);
            mainContentContainer.getChildren().add(tabUser);

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
            this.isAnotherPopupOpen = true;
        }
    }

    public void closePopup(StackPane popup) {
        if (popup != null && mainContentContainer != null && popupLayer != null) {
            popupLayer.setMouseTransparent(true);
            mainContentContainer.setEffect(null);
            popup.setVisible(false);
            popup.setManaged(false);
            enableNavigationButtons(true);
            this.isAnotherPopupOpen = false;
        }
    }

    public void addPopupToLayer(StackPane popup) {
        if (popupLayer != null && popup != null) {
            popupLayer.getChildren().add(popup);
        }
    }

    public void removePopupFromLayer(StackPane popup) {
        if (popupLayer != null && popup != null) {
            popupLayer.getChildren().remove(popup);
        }
    }

    public void enableNavigationButtons(boolean enable) {
        btnNavH.setDisable(!enable);
        btnNavC.setDisable(!enable);
        btnNavE.setDisable(!enable);
        btnNavU.setDisable(!enable);
    }

    private void switchTab(Node activeTab, Button activeButton) {

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

    public void reloadApplicationState() {

        boolean hasHousehold = (services.getCurrentHousehold() != null);

        btnNavC.setDisable(!hasHousehold);
        btnNavE.setDisable(!hasHousehold);

        if(tabWrapperController != null){
            tabWrapperController.initializeWithUserState(hasHousehold);
        }

        if(tabUserController != null){
            tabUserController.updateHouseholdState(hasHousehold);
        }

        if(!hasHousehold){
            switchTab(tabHousehold, btnNavH);
        }
    }

    public void showToast(String message, MessageType messageType) {

        Label toast = new Label(message);
        toast.getStyleClass().clear();

        toast.setMouseTransparent(true);

        String styleClass = "toast-" + String.valueOf(messageType).toLowerCase();
        toast.getStyleClass().add(styleClass);
        toast.setWrapText(true);

        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 0, 0));
        outerContainer.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> outerContainer.getChildren().remove(toast));

        SequentialTransition toastAnimation = new SequentialTransition(fadeIn, delay, fadeOut);
        toastAnimation.play();
    }

    public void requestConfirmForAction(String confirmMessage, Runnable onConfirmAction) {
        PopupConfirmActionController confirmController =
                new PopupConfirmActionController(this, onConfirmAction, confirmMessage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/popup_confirm.fxml"));
        loader.setControllerFactory(clazz -> confirmController);

        try {
            requestConfirmPopup = loader.load();
            openRequestConfirmPopup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openRequestConfirmPopup() {
        confirmPopupLayer.setVisible(true);
        confirmPopupLayer.setManaged(true);
        confirmPopupLayer.getChildren().add(requestConfirmPopup);
        mainContentContainer.setEffect(new GaussianBlur(15));
        popupLayer.setEffect(new GaussianBlur(15));
        confirmPopupLayer.setMouseTransparent(false);
        requestConfirmPopup.setVisible(true);
        requestConfirmPopup.setManaged(true);
        enableNavigationButtons(false);
    }

    public void closeRequestConfirmPopup() {
        if (requestConfirmPopup == null) {
            return;
        }

        confirmPopupLayer.setVisible(false);
        confirmPopupLayer.setManaged(false);
        requestConfirmPopup.setVisible(false);
        requestConfirmPopup.setManaged(false);
        confirmPopupLayer.getChildren().remove(requestConfirmPopup);
        confirmPopupLayer.setMouseTransparent(true);
        popupLayer.setEffect(null);

        if(!isAnotherPopupOpen){
            popupLayer.setMouseTransparent(true);
            mainContentContainer.setEffect(null);
            mainContentContainer.setMouseTransparent(false);
            enableNavigationButtons(true);
        }else {
            popupLayer.setMouseTransparent(false);
        }

        requestConfirmPopup = null;
    }
}