package com.housemate.client.components;

import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseShareResponseDTO;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class ExpenseItemElement extends HBox {

    public ExpenseItemElement(ExpenseResponseDTO dto, UUID currentUserID) {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(10.0);
        this.getStyleClass().add("expense-item");

        Region iconRegion = new Region();
        iconRegion.getStyleClass().addAll("base-icon", "icon-expense");

        VBox detailsBox = new VBox();
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        Label titleLabel = new Label();
        titleLabel.setText(dto.description());
        titleLabel.getStyleClass().add("expense-title");

        String dateString = dto.date().format(DateTimeFormatter.ofPattern("dd MMM"));
        Label detailsLabel = new Label();
        detailsLabel.setText("Paid by " + dto.payerFullName() + " • " + dateString);
        detailsLabel.getStyleClass().add("expense-details");

        detailsBox.getChildren().addAll(titleLabel, detailsLabel);

        VBox amountBox = new VBox();
        amountBox.setAlignment(Pos.CENTER_RIGHT);

        Label amountLabel = new Label();
        amountLabel.setText("€ " + String.format("%.2f", dto.amount()));
        amountLabel.getStyleClass().add("expense-amount");

        // Find the share for the current user
        BigDecimal userShare = dto.shares().stream()
            .filter(share -> share.userId().equals(currentUserID))
            .map(ExpenseShareResponseDTO::amount)
            .findFirst()
            .orElse(BigDecimal.ZERO);

        Label shareLabel = new Label();
        if(!Objects.equals(dto.payerId(), currentUserID)) {
            shareLabel.setText("Your share: € " + String.format("%.2f", userShare));
            shareLabel.getStyleClass().add("expense-owed");
        } else if (dto.amount().subtract(userShare).compareTo(BigDecimal.ZERO) > 0) {
            shareLabel.setText("You are owed: € " + String.format("%.2f", dto.amount().subtract(userShare)));
            shareLabel.getStyleClass().add("expense-owed-positive");
        } else {
            shareLabel.setText("Personal expense");
            shareLabel.getStyleClass().add("expense-details");
        }

        amountBox.getChildren().addAll(amountLabel, shareLabel);

        this.getChildren().addAll(iconRegion, detailsBox, amountBox);
    }
}
