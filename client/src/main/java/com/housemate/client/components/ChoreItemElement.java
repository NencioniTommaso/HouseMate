package com.housemate.client.components;

import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;



public class ChoreItemElement extends HBox {

    public ChoreItemElement(ChoreResponseDTO chore, Runnable onChoreDeletion, boolean isAdminMode){
        this.getStyleClass().add("standard-element");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(10);

        VBox labelsBox = new VBox();
        HBox.setHgrow(labelsBox, Priority.ALWAYS);
        labelsBox.setMinWidth(0);

        Label lblChoreDesc = new Label();
        lblChoreDesc.getStyleClass().add("standard-label");
        lblChoreDesc.setText(chore.description());
        lblChoreDesc.setMinWidth(0);
        lblChoreDesc.prefWidthProperty().bind(labelsBox.widthProperty());
        Label lblChoreFreq = new Label();
        lblChoreFreq.getStyleClass().add("element-detail");
        if(chore.frequencyDays() == 0){
            lblChoreFreq.setText("Frequency: not periodical");
        }else if (chore.frequencyDays() == 1){
            lblChoreFreq.setText("Frequency: every day");
        }else{
            lblChoreFreq.setText("Frequency: every " + chore.frequencyDays() + " days");
        }

        VBox buttonBox = new VBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setSpacing(5);
        buttonBox.setMinWidth(Region.USE_PREF_SIZE);

        Button btnDeleteChore = new Button();
        btnDeleteChore.getStyleClass().add("danger-button");
        btnDeleteChore.setText("Delete");
        btnDeleteChore.setOnAction(e -> onChoreDeletion.run());
        btnDeleteChore.setDisable(!isAdminMode);

        labelsBox.getChildren().addAll(lblChoreDesc, lblChoreFreq);
        buttonBox.getChildren().addAll(btnDeleteChore);
        this.getChildren().addAll(labelsBox, buttonBox);
    }
}
