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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MainController {

    @FXML private Button btnNavH, btnNavC, btnNavE, btnNavU;
    @FXML private Button btnRefresh;
    @FXML private StackPane outerContainer;
    @FXML private StackPane mainContentContainer;
    @FXML private StackPane popupLayer;
    @FXML private StackPane confirmPopupLayer;

    private Node tabHousehold, tabAssignments, tabExpenses, tabUser;

    private VBox toastContainer;
    private StackPane requestConfirmPopup;
    private boolean isAnotherPopupOpen;

    private final AppServices services;
    private final Runnable logoutHandler;

    private HouseholdTabWrapperController tabWrapperController;
    private TabUserController tabUserController;
    private TabAssignmentsController tabAssignmentsController;
    private TabExpensesController tabExpensesController;

    public MainController(AppServices services, Runnable logoutHandler) {
        this.services = services;
        this.logoutHandler = logoutHandler;
        this.isAnotherPopupOpen = false;
    }

    @FXML
    public void initialize() {

        setupToastContainer();

        loadTabs();

        //explicitly expanded to perform the backend calls when switching tabs
        btnNavH.setOnAction(e -> {
            switchTab(tabHousehold, btnNavH);
        });

        btnNavC.setOnAction(e -> {
            tabAssignmentsController.reloadMemberSelection();
            tabAssignmentsController.fetchAndDisplayAssignmentsData();
            switchTab(tabAssignments, btnNavC);
        });

        btnNavE.setOnAction(e -> {
            tabExpensesController.fetchAndDisplayOverview();
            tabExpensesController.fetchAndDisplayTransactionsData();
            switchTab(tabExpenses, btnNavE);
        });

        btnNavU.setOnAction(e -> {
            if(services.getCurrentHousehold() != null) {
                tabUserController.fetchAndDisplayCardsData();
            }
            tabUserController.fetchAndDisplayUserData();
            switchTab(tabUser, btnNavU);
        });

        refreshDataAndReload();
    }

    @FXML
    public void refreshDataAndReload() {

        disableRefreshButton(true);

        CompletableFuture.supplyAsync(() -> {
            services.setCurrentUser(services.getUserClientService().getCurrentUser());
            try {
                return services.getHouseholdClientService().getCurrentUserHousehold();
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("403")) return null;
                throw e;
            }
        }).thenAccept(household -> {
            Platform.runLater(() -> {
                tabUserController.fetchAndDisplayUserData();
                services.setCurrentHousehold(household);
                boolean hasHousehold = (household != null);

                btnNavC.setDisable(!hasHousehold);
                btnNavE.setDisable(!hasHousehold);
                tabUserController.updateHouseholdState(hasHousehold);

                if (!hasHousehold) {
                    tabWrapperController.initializeWithUserState(false);
                    switchTab(tabHousehold, btnNavH);
                    showToast("You are not in a household: you have been removed or you had left", MessageType.INFO);
                } else {
                    tabUserController.fetchAndDisplayCardsData();
                    tabAssignmentsController.fetchAndDisplayAssignmentsOverview();
                    tabAssignmentsController.fetchAndDisplayAssignmentsData();
                    tabExpensesController.fetchAndDisplayOverview();
                    tabExpensesController.fetchAndDisplayTransactionsData();

                    tabAssignmentsController.setAdminMode(true);
                    tabWrapperController.setAdminMode(true);

                    tabWrapperController.initializeWithUserState(true);
                    showToast("Refresh completed", MessageType.SUCCESS);
                }
                disableRefreshButton(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                ex.printStackTrace();
                showToast("Refresh failed: " + ex.getMessage(), MessageType.ERROR);
                disableRefreshButton(false);
            });
            return null;
        });
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

    public void disableRefreshButton(boolean disable){
        btnRefresh.setDisable(disable);
    }

    public void showToast(String message, MessageType messageType) {
        if (toastContainer == null) {
            setupToastContainer();
        }

        Label toast = new Label(message);
        toast.setAlignment(Pos.CENTER);
        toast.getStyleClass().clear();
        toast.setMouseTransparent(true);

        String styleClass = "toast-" + String.valueOf(messageType).toLowerCase();
        toast.getStyleClass().add(styleClass);
        toast.setWrapText(true);
        toast.setMaxWidth(400);

        toastContainer.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> toastContainer.getChildren().remove(toast));

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

    private void setupToastContainer() {
        toastContainer = new VBox(10);
        toastContainer.setAlignment(Pos.TOP_CENTER);
        toastContainer.setPadding(new Insets(20, 0, 0, 0));
        toastContainer.setMouseTransparent(true);
        toastContainer.setPickOnBounds(false);

        outerContainer.getChildren().add(toastContainer);
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
            tabAssignmentsController = loaderAssignments.getController();
            mainContentContainer.getChildren().add(tabAssignments);
            tabAssignments.setVisible(false);
            tabAssignments.setManaged(false);

            // Load Expenses Tab
            FXMLLoader loaderExpenses = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/tab_expenses.fxml"));
            loaderExpenses.setControllerFactory(clazz -> new TabExpensesController(this.services, this));
            tabExpenses = loaderExpenses.load();
            tabExpensesController = loaderExpenses.getController();
            mainContentContainer.getChildren().add(tabExpenses);
            tabExpenses.setVisible(false);
            tabExpenses.setManaged(false);

            // Load User Tab
            FXMLLoader loaderUser = new FXMLLoader(getClass().getResource("/com/housemate/client/tabs/tab_user.fxml"));
            loaderUser.setControllerFactory(clazz -> new TabUserController(this.services, this, logoutHandler));
            tabUser = loaderUser.load();
            tabUserController = loaderUser.getController();
            mainContentContainer.getChildren().add(tabUser);
            tabUser.setVisible(false);
            tabUser.setManaged(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
}