package com.housemate.client.controllers.popups.household;

import com.housemate.client.components.MemberItemElement;
import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.household.response.HouseholdMemberResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
                services.setCurrentHouseholdMembers(
                        currentHousehold.memberships().stream().map(HouseholdMemberResponseDTO::user)
                                .collect(Collectors.toCollection(ArrayList::new))
                );
                currentMembers = services.getCurrentHouseholdMembers();

                Collections.swap(
                        currentMembers,
                        currentMembers.indexOf(currentMembers.get(0)),
                        currentMembers.indexOf(
                                currentMembers.stream()
                                        .filter(member -> Objects.equals(member, services.getCurrentUser()))
                                        .findFirst().orElse(currentMembers.get(0))
                        )
                );

                Platform.runLater(() -> {
                    for (var member : currentMembers) {
                         MemberItemElement memberContainer = new MemberItemElement(
                                 member,
                                 () -> mainController.requestConfirmForAction(
                                         "Are you sure you want to remove " + member.name() + " " + member.surname() + " from the household?",
                                         () -> handleRemoveMember(member.id())
                                 ),
                                 isAdminMode,
                                 services.getCurrentUser().id()
                         );

                         membersListContainer.getChildren().add(memberContainer);
                    }
                });
            });
        }catch (RuntimeException e){
            Platform.runLater(() -> mainController.showToast(e.getMessage(), MessageType.ERROR));
        }
    }

    private void handleRemoveMember(UUID userID) {
        CompletableFuture.runAsync(() -> {
            try {
                services.getHouseholdClientService().removeMember(userID);

                Platform.runLater(() -> {
                    mainController.showToast("Member removed successfully!", MessageType.SUCCESS);
                    fetchMembersData();
                });
            }catch (RuntimeException e) {
                Platform.runLater(() -> mainController.showToast(e.getMessage(), MessageType.ERROR));
            }
        });
    }

}

