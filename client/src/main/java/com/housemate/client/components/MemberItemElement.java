package com.housemate.client.components;

import com.housemate.shared.dto.user.response.UserResponseDTO;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.UUID;

public class MemberItemElement extends HBox {

    public MemberItemElement(UserResponseDTO member, Runnable onUserRemoval, boolean isAdminMode, UUID currentUserId){
        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().add("standard-element");

        VBox leftContainer = new VBox();
        HBox.setHgrow(leftContainer, Priority.ALWAYS);
        leftContainer.setMinWidth(0);
        Label lblName = new Label(member.name() + " " + member.surname());
        lblName.getStyleClass().add("standard-label");
        lblName.setMinWidth(0);
        lblName.prefWidthProperty().bind(leftContainer.widthProperty());
        Label lblEmail = new Label(member.email());
        lblEmail.getStyleClass().add("element-detail");
        Label lblIban = new Label(member.iban());
        lblIban.getStyleClass().add("element-detail");
        Label lblPaymentLink = new Label(member.paymentLink());
        lblPaymentLink.getStyleClass().add("element-detail");

        leftContainer.getChildren().addAll(lblName, lblEmail, lblIban, lblPaymentLink);

        VBox rightContainer = new VBox();
        rightContainer.setAlignment(Pos.CENTER_RIGHT);
        rightContainer.setMinWidth(Region.USE_PREF_SIZE);

        if (Objects.equals(member.id(), currentUserId)) {
            Button youLabel = new Button("You");
            youLabel.getStyleClass().add("success-button");
            youLabel.setAlignment(Pos.CENTER);
            youLabel.setMouseTransparent(true);
            rightContainer.getChildren().add(youLabel);
        }else {
            Button btnRemoveMember = new Button("Remove");
            btnRemoveMember.getStyleClass().add("danger-button");
            btnRemoveMember.setOnAction(e -> onUserRemoval.run());
            btnRemoveMember.setDisable(!isAdminMode);
            rightContainer.getChildren().add(btnRemoveMember);
        }

        this.getChildren().addAll(leftContainer, rightContainer);
    }
}
