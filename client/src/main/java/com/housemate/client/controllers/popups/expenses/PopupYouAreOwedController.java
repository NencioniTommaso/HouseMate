package com.housemate.client.controllers.popups.expenses;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.enums.MessageType;
import com.housemate.shared.enums.UserTransactionRole;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PopupYouAreOwedController {

    private final AppServices services;
    private final MainController mainController;

    @FXML private StackPane popupDebtsYouAreOwed;
    @FXML private VBox debtsListContainer;

    public PopupYouAreOwedController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupDebtsYouAreOwed);
    }

    public void fetchDebtsData() {

        debtsListContainer.getChildren().clear();

        CompletableFuture.runAsync(() -> {
            try {
                List<DebtResponseDTO> debts = services.getDebtClientService().getFilteredDebts(
                        new DebtFilterRequestDTO(UserTransactionRole.CREDITOR, null)
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
                    mainController.closePopup(popupDebtsYouAreOwed);
                });
            }
        });

    }

    private HBox createDebtItemElement(DebtResponseDTO debt) {
        HBox debtItem = new HBox();
        debtItem.setAlignment(Pos.CENTER_LEFT);
        debtItem.setSpacing(10.0);
        debtItem.setStyle("-fx-padding: 10;");
        debtItem.getStyleClass().add("debt-item-owed");

        VBox leftVBox = new VBox();
        HBox.setHgrow(leftVBox, Priority.ALWAYS);

        Label debtTitle = new Label(debt.involvedName() + " owes you");
        debtTitle.getStyleClass().add("debt-title-owed");
        leftVBox.getChildren().add(debtTitle);

        VBox rightVBox = new VBox();
        rightVBox.setAlignment(Pos.CENTER_RIGHT);
        rightVBox.setSpacing(5.0);

        Label debtAmount = new Label(String.format("€ %.2f", debt.amount()));
        debtAmount.getStyleClass().add("debt-amount-owed");
        debtAmount.setFont(new Font("System Bold", 14.0));

        rightVBox.getChildren().add(debtAmount);

        debtItem.getChildren().addAll(leftVBox, rightVBox);
        return debtItem;
    }

}

