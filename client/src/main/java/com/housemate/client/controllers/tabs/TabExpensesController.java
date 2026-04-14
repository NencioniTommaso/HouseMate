package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.expenses.PopupAddExpenseController;
import com.housemate.client.controllers.popups.expenses.PopupSettleDebtController;
import com.housemate.client.controllers.popups.expenses.PopupYouOweController;
import com.housemate.client.controllers.popups.expenses.PopupYouAreOwedController;
import com.housemate.client.service.AppServices;
import com.housemate.client.components.ExpenseItemElement;
import com.housemate.client.components.SettlementItemElement;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;
import com.housemate.shared.enums.MessageType;
import com.housemate.shared.enums.UserTransactionRole;
import com.housemate.shared.utils.types.DateRange;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TabExpensesController {

    private StackPane popupAddExpense;
    private StackPane popupDebtsYouOwe;
    private StackPane popupCreditsYouAreOwed;
    private StackPane popupSettleDebt;

    @FXML private Label lblAmountYouOwe, lblAmountYouAreOwed, lblOverview;
    @FXML private RadioButton rdbDebtor,  rdbCreditor;
    @FXML private ToggleGroup expenseSelectionGroup, searchType;
    @FXML private TextField txtSearchExp;
    @FXML private DatePicker dtpSearchDateStart, dtpSearchDateEnd;
    @FXML private RadioButton rdbExpenses;

    @FXML private VBox dataContainer;

    private final AppServices services;
    private final MainController mainController;

    private PopupAddExpenseController popupAddExpenseController;
    private PopupYouOweController popupYouOweController;
    private PopupYouAreOwedController popupYouAreOwedController;

    private final Label lblNoData = new Label();

    //this is useful to prevent multiple simultaneous backend calls
    private final PauseTransition searchTimer = new PauseTransition(Duration.millis(300));

    public TabExpensesController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

        lblNoData.getStyleClass().add("message-error");

        loadPopups();

        searchTimer.setOnFinished(event -> fetchAndDisplayTransactionsData());

        expenseSelectionGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                triggerSearch();
            }
        });

        searchType.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                triggerSearch();
            }
        });

        dtpSearchDateStart.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (dtpSearchDateEnd.getValue() == null || dtpSearchDateEnd.getValue().isBefore(newDate)) {
                dtpSearchDateEnd.setValue(dtpSearchDateStart.getValue());
            }
            triggerSearch();
        });

        dtpSearchDateEnd.valueProperty().addListener((obs, oldDate, newDate) -> {
            triggerSearch();
        });

        dtpSearchDateEnd.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate fromDate = dtpSearchDateStart.getValue();

                if (empty || (fromDate != null && date.isBefore(fromDate))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });

        txtSearchExp.textProperty().addListener((obs, oldText, newText) -> {
            triggerSearch();
        });
    }

    @FXML
    public void handleAddExpense() {
        popupAddExpenseController.loadMembers();
        mainController.openPopup(popupAddExpense);
    }

    @FXML
    public void handleOpenSettleDebts() {
        popupYouOweController.fetchDebtsData();
        mainController.openPopup(popupDebtsYouOwe);
    }

    @FXML
    public void handleOpenYouAreOwed() {
        popupYouAreOwedController.fetchDebtsData();
        mainController.openPopup(popupCreditsYouAreOwed);
    }

    public void fetchAndDisplayTransactionsData() {
        dataContainer.getChildren().clear();

        DateRange dateRange = new DateRange(
                dtpSearchDateStart.getValue() != null ? dtpSearchDateStart.getValue().atStartOfDay()
                        : LocalDateTime.now().minusWeeks(1).truncatedTo(java.time.temporal.ChronoUnit.DAYS),

                dtpSearchDateEnd.getValue() != null ? dtpSearchDateEnd.getValue().atTime(23, 59, 59)
                    : LocalDateTime.now().withHour(23).withMinute(59).withSecond(59)
        );

        UserTransactionRole roleFilter;
        if (rdbDebtor.isSelected()) {
            roleFilter = UserTransactionRole.DEBTOR;
        } else if (rdbCreditor.isSelected()) {
            roleFilter = UserTransactionRole.CREDITOR;
        } else {
            roleFilter = UserTransactionRole.ALL;
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<?> transactions;
                boolean isSearchingExpenses = rdbExpenses.isSelected();
                if (isSearchingExpenses) {
                    transactions = services.getExpenseClientService().getFilteredExpenses(
                            new TransactionFilterRequestDTO(
                                    services.getCurrentHousehold().id(),
                                    roleFilter,
                                    dateRange,
                                    txtSearchExp.getText()
                            )
                    );
                } else {
                    transactions = services.getSettlementClientService().getFilteredSettlements(
                            new TransactionFilterRequestDTO(
                                    services.getCurrentHousehold().id(),
                                    roleFilter,
                                    dateRange,
                                    txtSearchExp.getText()
                            )
                    );
                }

                Platform.runLater(() -> {
                    if(transactions.isEmpty()) {
                        lblNoData.setText(isSearchingExpenses ?
                                "No expenses found with the current filters." :
                                "No settlements found with the current filters."
                        );
                        dataContainer.getChildren().add(lblNoData); //fine, dataContainer gets cleared at every fetch
                        lblNoData.setVisible(true);
                        lblNoData.setManaged(true);
                        return;
                    }

                    if(isSearchingExpenses) {
                        for(var transaction : transactions) {
                            ExpenseItemElement item = new ExpenseItemElement(
                                    (ExpenseResponseDTO) transaction,
                                    services.getCurrentUser().id()
                            );
                            dataContainer.getChildren().add(item);
                        }
                    } else {
                        for (var transaction : transactions) {
                            SettlementItemElement item = new SettlementItemElement((SettlementResponseDTO) transaction);
                            dataContainer.getChildren().add(item);
                        }
                    }
                });
            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    mainController.showToast("Error fetching data: " + e.getMessage(), MessageType.ERROR);
                });
            }
        });
    }

    public void fetchAndDisplayOverview() {
        CompletableFuture.runAsync(() -> {
            try {
                var expenseOverview = services.getExpenseClientService().getCurrentMonthExpenseOverview();

                var debtOverview = services.getDebtClientService().getCurrentUserDebtOverview();

                Platform.runLater(() -> {
                    lblOverview.setText(
                            "This month: " + String.format("€ %.2f", expenseOverview.totalAmount()) +
                            " (" + expenseOverview.expenseCount() + " expenses)"
                    );
                    lblAmountYouOwe.setText(String.format("€ %.2f", debtOverview.totalOwedByMe()));
                    lblAmountYouAreOwed.setText(String.format("€ %.2f", debtOverview.totalOwedToMe()));
                });
            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    mainController.showToast("Failed to fetch expenses overview: " + e.getMessage(), MessageType.ERROR);
                });
            }
        });
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
            FXMLLoader loaderYouOwe = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/expenses/popup_you_owe.fxml"));
            loaderYouOwe.setControllerFactory(
                    clazz -> new PopupYouOweController(this.services, this.mainController, this::openSettleDebtPopup));
            popupDebtsYouOwe = loaderYouOwe.load();
            popupYouOweController = loaderYouOwe.getController();
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

    private void openSettleDebtPopup(DebtResponseDTO debtToSettle) {
        mainController.closePopup(popupDebtsYouOwe);

        PopupSettleDebtController settleDebtController =
                new PopupSettleDebtController(services, mainController, () -> {
                    mainController.closePopup(popupSettleDebt);
                    mainController.removePopupFromLayer(popupSettleDebt);
                    mainController.openPopup(popupDebtsYouOwe);
                    popupYouOweController.fetchDebtsData();
                }, debtToSettle);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/expenses/popup_settle_debt.fxml"));
            loader.setControllerFactory(clazz -> settleDebtController);

            popupSettleDebt = loader.load();
            mainController.addPopupToLayer(popupSettleDebt);

        } catch (IOException e) {
            throw new RuntimeException("Critical error loading settle debt popup: " + e);
        }

        mainController.openPopup(popupSettleDebt);
    }

    private void triggerSearch() {
        searchTimer.playFromStart();
    }
}