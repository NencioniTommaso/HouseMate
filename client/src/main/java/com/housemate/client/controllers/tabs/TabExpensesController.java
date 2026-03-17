package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
    private TextField txtExpenseDescription, txtExpenseAmount;
    private ComboBox<?> cmbExpenseSplit;
    private Button btnCloseAddExpense, btnCancelExpense, btnCreateExpense;

    private StackPane popupDebtsYouOwe, popupDebtsYouAreOwed;

    private StackPane mainContentContainer;
    private StackPane popupLayer;
    private Button btnNavH, btnNavC, btnNavE, btnNavU;

    private AppServices services;
    private MainController mainController;

    public TabExpensesController(AppServices services, MainController mainController) {

        this.services = services;
        this.mainController = mainController;
    }

    public void setContainers(StackPane mainContentContainer, StackPane popupLayer, Button btnNavH, Button btnNavC, Button btnNavE, Button btnNavU) {
        this.mainContentContainer = mainContentContainer;
        this.popupLayer = popupLayer;
        this.btnNavH = btnNavH;
        this.btnNavC = btnNavC;
        this.btnNavE = btnNavE;
        this.btnNavU = btnNavU;
    }

    public void setPopupAddExpense(StackPane popupAddExpense, Button btnCloseAddExpense, Button btnCancelExpense, Button btnCreateExpense) {
        this.popupAddExpense = popupAddExpense;
        this.btnCloseAddExpense = btnCloseAddExpense;
        this.btnCancelExpense = btnCancelExpense;
        this.btnCreateExpense = btnCreateExpense;

        this.txtExpenseDescription = (TextField) popupAddExpense.lookup("#txtExpenseDescription");
        this.txtExpenseAmount = (TextField) popupAddExpense.lookup("#txtExpenseAmount");
        this.cmbExpenseSplit = (ComboBox<?>) popupAddExpense.lookup("#cmbExpenseSplit");
    }

    public void setGlobalPopups(StackPane popupOverlay, StackPane popupOverlayOwed) {
        this.popupDebtsYouOwe = popupOverlay;
        this.popupDebtsYouAreOwed = popupOverlayOwed;
    }

    @FXML
    public void initialize() {

        if (cardYouOwe != null) {
            cardYouOwe.setOnMouseClicked(e -> openPopup(popupDebtsYouOwe));
        }
        if (cardYouAreOwed != null) {
            cardYouAreOwed.setOnMouseClicked(e -> openPopup(popupDebtsYouAreOwed));
        }

        if (btnAddExpense != null) {
            btnAddExpense.setOnAction(e -> openPopup(popupAddExpense));
        }
        if (btnCloseAddExpense != null) {
            btnCloseAddExpense.setOnAction(e -> closeAddExpensePopup());
        }
        if (btnCancelExpense != null) {
            btnCancelExpense.setOnAction(e -> closeAddExpensePopup());
        }
        if (btnCreateExpense != null) {
            btnCreateExpense.setOnAction(e -> closeAddExpensePopup());
        }
    }

    private void openPopup(StackPane popup) {
        if (popup != null && mainContentContainer != null && popupLayer != null) {
            popupLayer.setMouseTransparent(false);
            mainContentContainer.setEffect(new GaussianBlur(15));
            popup.setVisible(true);
            popup.setManaged(true);
            disableNavigationButtons(true);
        }
    }

    private void closeDebtsPopup(boolean isYouOwe) {
        StackPane popup = isYouOwe ? popupDebtsYouOwe : popupDebtsYouAreOwed;
        if (popup != null && mainContentContainer != null && popupLayer != null) {
            popupLayer.setMouseTransparent(true);
            mainContentContainer.setEffect(null);
            popup.setVisible(false);
            popup.setManaged(false);
            disableNavigationButtons(false);
        }
    }

    private void closeAddExpensePopup() {
        if (popupAddExpense != null && mainContentContainer != null && popupLayer != null) {
            popupLayer.setMouseTransparent(true);
            mainContentContainer.setEffect(null);
            popupAddExpense.setVisible(false);
            popupAddExpense.setManaged(false);
            disableNavigationButtons(false);
            // Pulisci i campi del form
            if (txtExpenseDescription != null) {
                txtExpenseDescription.clear();
            }
            if (txtExpenseAmount != null) {
                txtExpenseAmount.clear();
            }
            if (cmbExpenseSplit != null) {
                cmbExpenseSplit.getSelectionModel().clearSelection();
            }
        }
    }

    private void disableNavigationButtons(boolean disable) {
        if (btnNavH != null) btnNavH.setDisable(disable);
        if (btnNavC != null) btnNavC.setDisable(disable);
        if (btnNavE != null) btnNavE.setDisable(disable);
        if (btnNavU != null) btnNavU.setDisable(disable);
    }
}








