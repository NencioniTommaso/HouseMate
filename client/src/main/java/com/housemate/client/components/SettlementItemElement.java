package com.housemate.client.components;

import com.housemate.shared.dto.expense.response.SettlementResponseDTO;
import com.housemate.shared.enums.UserTransactionRole;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import java.time.format.DateTimeFormatter;

public class SettlementItemElement extends HBox {

    public SettlementItemElement(SettlementResponseDTO dto) {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(10.0);
        this.getStyleClass().add("expense-item");

        Region iconRegion = new Region();
        iconRegion.getStyleClass().addAll( "base-icon", "icon-settlement");

        VBox detailsBox = new VBox();
        setHgrow(detailsBox, Priority.ALWAYS);

        Label titleLabel = new Label(dto.description());
        titleLabel.getStyleClass().add("expense-title");

        String dateString = dto.date().format(DateTimeFormatter.ofPattern("dd MMM"));
        String detailsText = dto.userTransactionRole() == UserTransactionRole.CREDITOR ?
                "From " + dto.involvedName() + " • " + dateString :
                "To " + dto.involvedName() + " • " + dateString;

        Label detailsLabel = new Label(detailsText);
        detailsLabel.getStyleClass().add("expense-details");

        detailsBox.getChildren().addAll(titleLabel, detailsLabel);

        VBox amountBox = new VBox();
        amountBox.setAlignment(Pos.CENTER_RIGHT);

        Label amountLabel = new Label("€ " + String.format("%.2f", dto.amount()));

        if (dto.userTransactionRole() == UserTransactionRole.CREDITOR) {
            amountLabel.getStyleClass().add("settlement-owed");
        } else {
            amountLabel.getStyleClass().add("settlement-owe");
        }

        amountBox.getChildren().add(amountLabel);

        this.getChildren().addAll(iconRegion, detailsBox, amountBox);
    }

}
