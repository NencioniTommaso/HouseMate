package com.housemate.client.controllers.popups.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PopupManageMembersController {

    private AppServices services;
    private final MainController mainController;

    @FXML private StackPane popupManageMembers;
    @FXML private VBox membersListContainer;

    private List<UserResponseDTO> currentMembers;

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

                //currentMembers = services.getHouseholdClientService().getAllHouseholdMembers(services.getCurrentHousehold().id());

                //fake data to test the ui
                currentMembers.add(new UserResponseDTO(UUID.randomUUID(), "John", "Doe", "realemail.format@gmail.com", "IT57K0479832748324873287326832"));
                currentMembers.add(new UserResponseDTO(UUID.randomUUID(), "Jane", "Smith", "c", "d"));
                currentMembers.add(new UserResponseDTO(UUID.randomUUID(), "Alice", "Johnson", "e", "f"));

                Platform.runLater(() -> {

                    for (var member : currentMembers) {

                         HBox memberContainer = new HBox();
                         memberContainer.setAlignment(Pos.CENTER_LEFT);

                         VBox leftContainer = new VBox();
                         HBox.setHgrow(leftContainer, Priority.ALWAYS);
                         Label lblName = new Label(member.name() + " " + member.surname());
                         lblName.getStyleClass().add("member-name");
                         Label lblDate = new Label("Member since: ");
                         lblDate.getStyleClass().add("member-date");
                         Label lblEmail = new Label(member.email());
                         lblEmail.getStyleClass().add("member-date");
                         Label lblIban = new Label(member.iban());
                         lblIban.getStyleClass().add("member-date");
                         Label lblPaymentLink = new Label("Payment link: ");
                         lblPaymentLink.getStyleClass().add("member-date");

                         leftContainer.getChildren().addAll(lblName, lblDate, lblEmail, lblIban, lblPaymentLink);

                         VBox rightContainer = new VBox();
                         rightContainer.setAlignment(Pos.CENTER_RIGHT);
                         Button btnRemoveMember = new Button("Remove");
                         btnRemoveMember.getStyleClass().add("btn-delete");
                         btnRemoveMember.setOnAction(e -> handleRemoveMember(member.id()));
                         rightContainer.getChildren().add(btnRemoveMember);

                         memberContainer.getChildren().addAll(leftContainer, rightContainer);
                         membersListContainer.getChildren().add(memberContainer);
                    }

                    mainController.showToast("Members data loaded successfully!", MessageType.SUCCESS);
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

        try {
            CompletableFuture.runAsync(() -> {

                //services.getHouseholdClientService().removeMemberFromHousehold(services.getCurrentHousehold().id(), userID);
                fetchMembersData();

                Platform.runLater(() -> {
                    mainController.showToast("Member removed successfully!", MessageType.SUCCESS);
                    fetchMembersData();
                });
            });
        }catch (RuntimeException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
               mainController.showToast(e.getMessage(), MessageType.ERROR);
            });
        }

    }

}

