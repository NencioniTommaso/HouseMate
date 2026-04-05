package com.housemate.client.utils;

import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseShareResponseDTO;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ExpenseItemCard extends HBox {

    public ExpenseItemCard(ExpenseResponseDTO dto) {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(10.0);
        this.getStyleClass().add("expense-item");

        // Icon Label
        Label iconLabel = new Label("🛒");
        iconLabel.getStyleClass().add("expense-icon");

        // Title and Details VBox
        VBox detailsBox = new VBox();
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        Label titleLabel = new Label(dto.description());
        titleLabel.getStyleClass().add("expense-title");

        String dateString = dto.date().format(DateTimeFormatter.ofPattern("dd MMM"));
        Label detailsLabel = new Label("Paid by " + dto.payerFullName() + " • " + dateString);
        detailsLabel.getStyleClass().add("expense-details");

        detailsBox.getChildren().addAll(titleLabel, detailsLabel);

        // Amount VBox
        VBox amountBox = new VBox();
        amountBox.setAlignment(Pos.CENTER_RIGHT);

        Label amountLabel = new Label("€ " + String.format("%.2f", dto.amount()));
        amountLabel.getStyleClass().add("expense-amount");

        // Find the share for the current user
        BigDecimal userShare = dto.shares().stream()
                .map(ExpenseShareResponseDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Label shareLabel = new Label("Your share: € " + String.format("%.2f", userShare));
        shareLabel.getStyleClass().add("expense-owed");

        amountBox.getChildren().addAll(amountLabel, shareLabel);

        // Add all to HBox
        this.getChildren().addAll(iconLabel, detailsBox, amountBox);
    }

}
