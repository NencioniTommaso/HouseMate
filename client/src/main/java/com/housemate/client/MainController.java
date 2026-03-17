package com.housemate.client;

import com.housemate.client.controllers.tabs.TabAssignmentsController;
import com.housemate.client.controllers.tabs.TabExpensesController;
import com.housemate.client.controllers.tabs.TabHouseholdController;
import com.housemate.client.controllers.tabs.TabUserController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class MainController {

    @FXML private Button btnNavH, btnNavC, btnNavE, btnNavU;
    @FXML private StackPane mainContentContainer;
    @FXML private StackPane tabsContainer;
    @FXML private StackPane popupLayer;

    private TabHouseholdController tabHouseholdController;
    private TabAssignmentsController tabAssignmentsController;
    private TabExpensesController tabExpensesController;
    private TabUserController tabUserController;

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

        loadGlobalPopups();

        loadTabPopups();

        loadHouseholdPopups();

        tabHouseholdController.setContainers(mainContentContainer, popupLayer, btnNavH, btnNavC, btnNavE, btnNavU);
        tabAssignmentsController.setContainers(mainContentContainer, popupLayer, btnNavH, btnNavC, btnNavE, btnNavU);
        tabExpensesController.setContainers(mainContentContainer, popupLayer, btnNavH, btnNavC, btnNavE, btnNavU);
    }

    private void loadGlobalPopups() {
        try {
            FXMLLoader loaderSettleDebts = new FXMLLoader(getClass().getResource("popups/expenses/popup_settle_debts.fxml"));
            StackPane popupOverlay = loaderSettleDebts.load();
            popupLayer.getChildren().add(popupOverlay);
            Button btnClosePopup = (Button) popupOverlay.lookup("#btnClosePopup");
            if (btnClosePopup != null) {
                btnClosePopup.setOnAction(e -> {
                    mainContentContainer.setEffect(null);
                    popupOverlay.setVisible(false);
                    popupOverlay.setManaged(false);
                    popupLayer.setMouseTransparent(true);
                    enableNavigationButtons(true);
                });
            }

            FXMLLoader loaderYouAreOwed = new FXMLLoader(getClass().getResource("popups/expenses/popup_you_are_owed.fxml"));
            StackPane popupOverlayOwed = loaderYouAreOwed.load();
            popupLayer.getChildren().add(popupOverlayOwed);
            Button btnClosePopupOwed = (Button) popupOverlayOwed.lookup("#btnClosePopupOwed");
            if (btnClosePopupOwed != null) {
                btnClosePopupOwed.setOnAction(e -> {
                    mainContentContainer.setEffect(null);
                    popupOverlayOwed.setVisible(false);
                    popupOverlayOwed.setManaged(false);
                    popupLayer.setMouseTransparent(true);
                    enableNavigationButtons(true);
                });
            }

            tabExpensesController.setGlobalPopups(popupOverlay, popupOverlayOwed);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTabPopups() {
        try {
            FXMLLoader loaderAddAssignment = new FXMLLoader(getClass().getResource("popups/assignments/popup_assignment.fxml"));
            StackPane popupAddAssignment = loaderAddAssignment.load();
            popupLayer.getChildren().add(popupAddAssignment);

            Button btnCloseAddAssignment = (Button) popupAddAssignment.lookup("#btnCloseAddAssignment");
            Button btnCancelAssignment = (Button) popupAddAssignment.lookup("#btnCancelAssignment");
            Button btnCreateAssignment = (Button) popupAddAssignment.lookup("#btnCreateAssignment");
            ComboBox<?> cmbAssignUser = (ComboBox<?>) popupAddAssignment.lookup("#cmbAssignUser");
            DatePicker dateDueDate = (DatePicker) popupAddAssignment.lookup("#dateDueDate");

            if (btnCloseAddAssignment != null) {
                btnCloseAddAssignment.setOnAction(e -> closeTabPopup(popupAddAssignment, cmbAssignUser, dateDueDate));
            }
            if (btnCancelAssignment != null) {
                btnCancelAssignment.setOnAction(e -> closeTabPopup(popupAddAssignment, cmbAssignUser, dateDueDate));
            }
            if (btnCreateAssignment != null) {
                btnCreateAssignment.setOnAction(e -> closeTabPopup(popupAddAssignment, cmbAssignUser, dateDueDate));
            }

            tabAssignmentsController.setPopupAddAssignment(popupAddAssignment, btnCloseAddAssignment, btnCancelAssignment, btnCreateAssignment);

            FXMLLoader loaderAddExpense = new FXMLLoader(getClass().getResource("popups/expenses/popup_expense.fxml"));
            StackPane popupAddExpense = loaderAddExpense.load();
            popupLayer.getChildren().add(popupAddExpense);

            Button btnCloseAddExpense = (Button) popupAddExpense.lookup("#btnCloseAddExpense");
            Button btnCancelExpense = (Button) popupAddExpense.lookup("#btnCancelExpense");
            Button btnCreateExpense = (Button) popupAddExpense.lookup("#btnCreateExpense");
            TextField txtExpenseDescription = (TextField) popupAddExpense.lookup("#txtExpenseDescription");
            TextField txtExpenseAmount = (TextField) popupAddExpense.lookup("#txtExpenseAmount");
            ComboBox<?> cmbExpenseSplit = (ComboBox<?>) popupAddExpense.lookup("#cmbExpenseSplit");

            if (btnCloseAddExpense != null) {
                btnCloseAddExpense.setOnAction(e -> closeTabPopup(popupAddExpense, txtExpenseDescription, txtExpenseAmount, cmbExpenseSplit));
            }
            if (btnCancelExpense != null) {
                btnCancelExpense.setOnAction(e -> closeTabPopup(popupAddExpense, txtExpenseDescription, txtExpenseAmount, cmbExpenseSplit));
            }
            if (btnCreateExpense != null) {
                btnCreateExpense.setOnAction(e -> closeTabPopup(popupAddExpense, txtExpenseDescription, txtExpenseAmount, cmbExpenseSplit));
            }

            tabExpensesController.setPopupAddExpense(popupAddExpense, btnCloseAddExpense, btnCancelExpense, btnCreateExpense);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHouseholdPopups() {
        try {
            FXMLLoader loaderManageMembers = new FXMLLoader(getClass().getResource("popups/household/popup_manage_members.fxml"));
            StackPane popupManageMembers = loaderManageMembers.load();
            popupLayer.getChildren().add(popupManageMembers);
            Button btnCloseManageMembers = (Button) popupManageMembers.lookup("#btnCloseManageMembers");
            if (btnCloseManageMembers != null) {
                btnCloseManageMembers.setOnAction(e -> closeHouseholdPopup(popupManageMembers));
            }

            FXMLLoader loaderRulesAndChores = new FXMLLoader(getClass().getResource("popups/household/popup_rules_and_chores.fxml"));
            StackPane popupRulesAndChores = loaderRulesAndChores.load();
            popupLayer.getChildren().add(popupRulesAndChores);
            Button btnCloseRulesAndChores = (Button) popupRulesAndChores.lookup("#btnCloseRulesAndChores");
            if (btnCloseRulesAndChores != null) {
                btnCloseRulesAndChores.setOnAction(e -> closeHouseholdPopup(popupRulesAndChores));
            }

            FXMLLoader loaderSettings = new FXMLLoader(getClass().getResource("popups/household/popup_shopping_lists.fxml"));
            StackPane popupSettings = loaderSettings.load();
            popupLayer.getChildren().add(popupSettings);
            Button btnCloseSettings = (Button) popupSettings.lookup("#btnCloseSettings");
            if (btnCloseSettings != null) {
                btnCloseSettings.setOnAction(e -> closeHouseholdPopup(popupSettings));
            }

            FXMLLoader loaderInviteMember = new FXMLLoader(getClass().getResource("popups/household/popup_invite_member.fxml"));
            StackPane popupInviteMember = loaderInviteMember.load();
            popupLayer.getChildren().add(popupInviteMember);
            Button btnCloseInviteMember = (Button) popupInviteMember.lookup("#btnCloseInviteMember");
            if (btnCloseInviteMember != null) {
                btnCloseInviteMember.setOnAction(e -> closeHouseholdPopup(popupInviteMember));
            }

            FXMLLoader loaderPlanAssignments = new FXMLLoader(getClass().getResource("popups/household/popup_plan_assignments.fxml"));
            StackPane popupPlanAssignments = loaderPlanAssignments.load();
            popupLayer.getChildren().add(popupPlanAssignments);
            Button btnClosePlanAssignments = (Button) popupPlanAssignments.lookup("#btnClosePlanAssignments");
            if (btnClosePlanAssignments != null) {
                btnClosePlanAssignments.setOnAction(e -> closeHouseholdPopup(popupPlanAssignments));
            }

            FXMLLoader loaderCreateChore = new FXMLLoader(getClass().getResource("popups/household/popup_create_chore.fxml"));
            StackPane popupCreateChore = loaderCreateChore.load();
            popupLayer.getChildren().add(popupCreateChore);
            Button btnCloseCreateChore = (Button) popupCreateChore.lookup("#btnCloseCreateChore");
            if (btnCloseCreateChore != null) {
                btnCloseCreateChore.setOnAction(e -> closeHouseholdPopup(popupCreateChore));
            }

            tabHouseholdController.setHouseholdPopups(popupManageMembers, popupRulesAndChores, popupSettings, popupInviteMember, popupPlanAssignments, popupCreateChore);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeHouseholdPopup(StackPane popup) {
        if (popup != null) {
            popupLayer.setMouseTransparent(true);
            mainContentContainer.setEffect(null);
            popup.setVisible(false);
            popup.setManaged(false);
            enableNavigationButtons(true);
        }
    }

    private void closeTabPopup(StackPane popup, Object... fieldsToClean) {
        if (popup != null) {
            popupLayer.setMouseTransparent(true);
            mainContentContainer.setEffect(null);
            popup.setVisible(false);
            popup.setManaged(false);
            enableNavigationButtons(true);

            for (Object field : fieldsToClean) {
                if (field instanceof ComboBox) {
                    ((ComboBox<?>) field).getSelectionModel().clearSelection();
                } else if (field instanceof TextField) {
                    ((TextField) field).clear();
                } else if (field instanceof DatePicker) {
                    ((DatePicker) field).setValue(null);
                }
            }
        }
    }

    private void loadTabs() {
        try {
            // Carica la tab Household
            FXMLLoader loaderHousehold = new FXMLLoader(getClass().getResource("tabs/tab_household.fxml"));
            tabHousehold = loaderHousehold.load();
            tabHouseholdController = loaderHousehold.getController();
            tabsContainer.getChildren().add(tabHousehold);

            // Carica la tab Assignments
            FXMLLoader loaderAssignments = new FXMLLoader(getClass().getResource("tabs/tab_assignments.fxml"));
            tabAssignments = loaderAssignments.load();
            tabAssignmentsController = loaderAssignments.getController();
            tabAssignments.setVisible(false);
            tabAssignments.setManaged(false);
            tabsContainer.getChildren().add(tabAssignments);

            // Carica la tab Expenses
            FXMLLoader loaderExpenses = new FXMLLoader(getClass().getResource("tabs/tab_expenses.fxml"));
            tabExpenses = loaderExpenses.load();
            tabExpensesController = loaderExpenses.getController();
            tabExpenses.setVisible(false);
            tabExpenses.setManaged(false);
            tabsContainer.getChildren().add(tabExpenses);

            // Carica la tab User
            FXMLLoader loaderUser = new FXMLLoader(getClass().getResource("tabs/tab_user.fxml"));
            tabUser = loaderUser.load();
            tabUserController = loaderUser.getController();
            tabUser.setVisible(false);
            tabUser.setManaged(false);
            tabsContainer.getChildren().add(tabUser);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enableNavigationButtons(boolean enable) {
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