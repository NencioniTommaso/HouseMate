package com.housemate.client.components;

import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.enums.UserTransactionRole;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class DebtItemElement extends HBox {

    public DebtItemElement(DebtResponseDTO debt, Consumer<DebtResponseDTO> onOpenSettleDebt) {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(10.0);
        this.setStyle("-fx-padding: 10;");
        this.getStyleClass().add("standard-element");

        boolean isOwed = debt.userTransactionRole() == UserTransactionRole.CREDITOR;

        String debtTitleMessage = isOwed ? debt.involvedName() + " owes you" : "Debt to " + debt.involvedName();
        String amountStyleClass = isOwed ? "positive-amount" : "negative-amount";

        Label debtTitle = new Label(debtTitleMessage);
        debtTitle.getStyleClass().add("standard-label");
        debtTitle.setMinWidth(0);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label debtAmount = new Label(String.format("€ %.2f", debt.amount()));
        debtAmount.getStyleClass().add(amountStyleClass);
        debtAmount.setFont(new Font("System Bold", 14.0));
        debtAmount.setMinWidth(Region.USE_PREF_SIZE);

        if(isOwed){
            this.getChildren().addAll(debtTitle, spacer, debtAmount);
            return;
        }

        Button payButton = new Button("Pay");
        payButton.getStyleClass().add("standard-button");
        payButton.setOnAction(event -> onOpenSettleDebt.accept(debt));
        payButton.setMinWidth(Region.USE_PREF_SIZE);

        payButton.setDisable(debt.amount().compareTo(BigDecimal.ZERO) == 0);

        this.getChildren().addAll(debtTitle, spacer, debtAmount, payButton);
    }
}
