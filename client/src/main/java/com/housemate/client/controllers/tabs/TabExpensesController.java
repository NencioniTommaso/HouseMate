package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.expenses.PopupAddExpenseController;
import com.housemate.client.controllers.popups.expenses.PopupSettleDebtsController;
import com.housemate.client.controllers.popups.expenses.PopupYouAreOwedController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class TabExpensesController {

    @FXML private Button btnAddExpense;
    @FXML private VBox cardYouOwe, cardYouAreOwed;
    
    @FXML private CheckBox chkMyExpenses, chkSettlements;
    @FXML private ComboBox<?> cmbUserFilterExp;
    @FXML private TextField txtSearchExp;
    @FXML private Button btnClearFiltersExp;

    @FXML private RadioButton radioEquale, radioPercentuale, radioQuote;
    @FXML private RadioButton radioExp, radioStl;
    @FXML private DatePicker txtSearchDateStart, txtSearchDateEnd;
    @FXML private ComboBox<?> householdSel;

    private StackPane popupAddExpense;
    private StackPane popupDebtsYouOwe;
    private StackPane popupCreditsYouAreOwed;

    private final AppServices services;
    private final MainController mainController;

    private PopupAddExpenseController popupAddExpenseController;
    private PopupSettleDebtsController popupSettleDebtsController;
    private PopupYouAreOwedController popupYouAreOwedController;

    public TabExpensesController(AppServices services, MainController mainController) {

        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadPopups();
    }

    @FXML
    public void handleAddExpense() {
        popupAddExpenseController.loadMembers();
        mainController.openPopup(popupAddExpense);
    }

    @FXML
    public void handleOpenSettleDebts() {
        popupSettleDebtsController.fetchDebtsData();
        mainController.openPopup(popupDebtsYouOwe);
    }

    @FXML
    public void handleOpenYouAreOwed() {
        popupYouAreOwedController.fetchDebtsData();
        mainController.openPopup(popupCreditsYouAreOwed);
    }

    private void loadPopups() {

        try {
            // Load Add Expense Popup
            FXMLLoader loaderAddExpense = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/expenses/popup_create_expense.fxml"));
            loaderAddExpense.setControllerFactory(
                    clazz -> new PopupAddExpenseController(this.services, this.mainController));
            popupAddExpense = loaderAddExpense.load();
            popupAddExpenseController = loaderAddExpense.getController();
            mainController.addPopupToLayer(popupAddExpense);
            popupAddExpense.setVisible(false);
            popupAddExpense.setManaged(false);

            // Load Settle Debts Popup (You Owe)
            FXMLLoader loaderSettleDebts = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/expenses/popup_settle_debts.fxml"));
            loaderSettleDebts.setControllerFactory(
                    clazz -> new PopupSettleDebtsController(this.services, this.mainController));
            popupDebtsYouOwe = loaderSettleDebts.load();
            popupSettleDebtsController = loaderSettleDebts.getController();
            mainController.addPopupToLayer(popupDebtsYouOwe);
            popupDebtsYouOwe.setVisible(false);
            popupDebtsYouOwe.setManaged(false);

            // Load You Are Owed Popup (Credits)
            FXMLLoader loaderYouAreOwed = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/expenses/popup_you_are_owed.fxml"));
            loaderYouAreOwed.setControllerFactory(
                    clazz -> new PopupYouAreOwedController(this.services, this.mainController));
            popupCreditsYouAreOwed = loaderYouAreOwed.load();
            popupYouAreOwedController = loaderYouAreOwed.getController();
            mainController.addPopupToLayer(popupCreditsYouAreOwed);
            popupCreditsYouAreOwed.setVisible(false);
            popupCreditsYouAreOwed.setManaged(false);

        } catch (IOException e) {
            throw new RuntimeException("Error loading popup: " + e.getMessage(), e);
        }

    }
}








