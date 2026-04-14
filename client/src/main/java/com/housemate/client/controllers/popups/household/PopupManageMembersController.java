package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PopupManageMembersController {

    private final AppServices services;
    private final MainController mainController;

    @FXML private StackPane popupManageMembers;
    @FXML private VBox membersListContainer;

    private List<UserResponseDTO> currentMembers;

    @Setter
    private boolean isAdminMode;

    public PopupManageMembersController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
        this.currentMembers = new ArrayList<>();

    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupManageMembers);
    }

    public void fetchMembersData() {

        membersListContainer.getChildren().clear();
        currentMembers.clear();

        try{
            CompletableFuture.runAsync(() -> {

                HouseholdResponseDTO currentHousehold = services.getHouseholdClientService().getCurrentUserHousehold();
                services.setCurrentHousehold(currentHousehold);
                currentMembers = currentHousehold.members();

                Platform.runLater(() -> {

                    for (var member : currentMembers) {

                         HBox memberContainer = new HBox();
                         memberContainer.setAlignment(Pos.CENTER_LEFT);

                         VBox leftContainer = new VBox();
                         HBox.setHgrow(leftContainer, Priority.ALWAYS);
                         Label lblName = new Label(member.name() + " " + member.surname());
                         lblName.getStyleClass().add("member-name");
                         Label lblEmail = new Label(member.email());
                         lblEmail.getStyleClass().add("member-date");
                         Label lblIban = new Label(member.iban());
                         lblIban.getStyleClass().add("member-date");
                         Label lblPaymentLink = new Label(member.paymentLink());
                         lblPaymentLink.getStyleClass().add("member-date");

                         leftContainer.getChildren().addAll(lblName, lblEmail, lblIban, lblPaymentLink);

                         VBox rightContainer = new VBox();
                         rightContainer.setAlignment(Pos.CENTER_RIGHT);

                         if (Objects.equals(member.id(), services.getCurrentUser().id())) {
                             Label youLabel = new Label("You");
                             youLabel.getStyleClass().add("btn-complete");
                             youLabel.setPrefSize(80, 30);
                             youLabel.setAlignment(Pos.CENTER);
                             youLabel.setMouseTransparent(true);
                             rightContainer.getChildren().add(youLabel);
                         }else {
                             Button btnRemoveMember = new Button("Remove");
                             btnRemoveMember.getStyleClass().add("btn-delete");
                             btnRemoveMember.setOnAction(e -> {
                                 mainController.requestConfirmForAction("Are you sure you want to remove " + member.name() + " " + member.surname() + " from the household?", () -> {
                                     handleRemoveMember(member.id());
                                 });
                             });
                             btnRemoveMember.setDisable(!isAdminMode);
                             rightContainer.getChildren().add(btnRemoveMember);
                         }

                         memberContainer.getChildren().addAll(leftContainer, rightContainer);
                         membersListContainer.getChildren().add(memberContainer);
                    }
                });
            });
        }catch (RuntimeException e){
            e.printStackTrace();
            Platform.runLater(() -> {
                mainController.showToast(e.getMessage(), MessageType.ERROR);
            });
        }
    }

    private void handleRemoveMember(UUID userID) {
        CompletableFuture.runAsync(() -> {
            try {
                services.getHouseholdClientService().removeMember(userID);

                Platform.runLater(() -> {
                    mainController.showToast("Member removed succesfully!", MessageType.SUCCESS);
                    fetchMembersData();
                });
            }catch (RuntimeException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                   mainController.showToast(e.getMessage(), MessageType.ERROR);
                });
            }
        });
    }

}

