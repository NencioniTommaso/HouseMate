package com.housemate.client.controllers.popups.expenses;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.enums.MessageType;
import com.housemate.shared.enums.UserTransactionRole;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PopupSettleDebtsController {

    private final AppServices services;
    private final MainController mainController;

    @FXML private StackPane popupDebts;
    @FXML private VBox debtsListContainer;

    public PopupSettleDebtsController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupDebts);
    }

    public void fetchDebtsData() {

        debtsListContainer.getChildren().clear();

        CompletableFuture.runAsync(() -> {
            try {
                List<DebtResponseDTO> debts = services.getDebtClientService().getFilteredDebts(
                        new DebtFilterRequestDTO(UserTransactionRole.DEBTOR, null)
                );

                Platform.runLater(() -> {

                    for (DebtResponseDTO debt : debts) {
                        HBox debtItem = createDebtItemElement(debt);
                        debtsListContainer.getChildren().add(debtItem);
                    }
                });

            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    mainController.showToast("Failed to fetch debts data: " + e.getMessage(), MessageType.ERROR);
                    mainController.closePopup(popupDebts);
                });
            }
        });

    }

    private HBox createDebtItemElement(DebtResponseDTO debt) {
        HBox debtItem = new HBox();
        debtItem.setAlignment(Pos.CENTER_LEFT);
        debtItem.setSpacing(10.0);
        debtItem.setStyle("-fx-padding: 10;");
        debtItem.getStyleClass().add("debt-item");

        VBox leftVBox = new VBox();
        HBox.setHgrow(leftVBox, Priority.ALWAYS);

        Label debtTitle = new Label("Debt to " + debt.involvedName());
        debtTitle.getStyleClass().add("debt-title");
        leftVBox.getChildren().add(debtTitle);

        VBox rightVBox = new VBox();
        rightVBox.setAlignment(Pos.CENTER_RIGHT);
        rightVBox.setSpacing(5.0);

        Label debtAmount = new Label(String.format("€ %.2f", debt.amount()));
        debtAmount.getStyleClass().add("debt-amount");
        debtAmount.setFont(new Font("System Bold", 14.0));

        Button payButton = new Button("Pay");
        payButton.getStyleClass().add("settle-button");
        payButton.setOnAction(event -> handleSettleDebt(debt));

        rightVBox.getChildren().addAll(debtAmount, payButton);

        debtItem.getChildren().addAll(leftVBox, rightVBox);
        return debtItem;
    }

    //this is only able to settle the entirety of a debt at once,
    //settling part of it would require another popup
    private void handleSettleDebt(DebtResponseDTO debt) {
        CompletableFuture.runAsync(() -> {
            try{
                services.getSettlementClientService().settleDebt(debt.debtId(), new SettlementCreateRequestDTO(
                        debt.debtId(), debt.involvedId(), debt.amount(), "Settling debt to " + debt.involvedName()
                ));

                Platform.runLater(this::fetchDebtsData);

            }catch (RuntimeException e){
                Platform.runLater(() -> {
                   mainController.showToast("Failed to fetch debts data: " + e.getMessage(), MessageType.ERROR);
                });
            }
        });

    }

}

