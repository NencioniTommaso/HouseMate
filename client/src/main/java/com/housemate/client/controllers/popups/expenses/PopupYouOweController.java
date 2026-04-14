package com.housemate.client.controllers.popups.expenses;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.client.components.DebtItemElement;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.enums.MessageType;
import com.housemate.shared.enums.UserTransactionRole;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PopupYouOweController {

    private final AppServices services;
    private final MainController mainController;

    private final Consumer<DebtResponseDTO> onOpenSettleDebtCallback;

    @FXML private StackPane popupDebts;
    @FXML private VBox debtsListContainer;

    public PopupYouOweController(AppServices services,
                                 MainController mainController,
                                 Consumer<DebtResponseDTO> onOpenSettleDebtCallback) {
        this.services = services;
        this.mainController = mainController;
        this.onOpenSettleDebtCallback = onOpenSettleDebtCallback;
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
                        HBox debtItem = new DebtItemElement(debt, onOpenSettleDebtCallback);
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
}

