package com.housemate.client.controllers.tabs.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.household.*;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class TabHouseholdController {

    @FXML private Label lblHouseholdName;

    private StackPane popupManageMembers;
    private StackPane popupRulesAndChores;
    private StackPane popupInviteMember;
    private StackPane popupCreateChore;
    private StackPane popupShoppingLists;
    private StackPane popupListDetails;
    private StackPane popupCreateList;

    private PopupRulesAndChoresController popupRulesAndChoresController;
    private PopupShoppingListsController popupShoppingListsController;
    private PopupManageMembersController popupManageMembersController;
    private PopupInviteMemberController popupInviteMemberController;

    private final AppServices services;
    private final MainController mainController;

    public TabHouseholdController(AppServices services, MainController mainController) {

        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        mainController.enableNavigationButtons(true);
        loadPopups();

        //household name loaded once, since it's immutable and the tab is reloaded on household change
        CompletableFuture.runAsync(() -> {
            try {
                String householdName = services.getHouseholdClientService().getCurrentUserHousehold().name();
                Platform.runLater(() -> lblHouseholdName.setText(householdName));
            } catch (RuntimeException e) {
                Platform.runLater(() -> mainController.showToast("Error loading household name", MessageType.ERROR));
            }
        });
    }

    @FXML
    public void handleOpenShoppingLists() {
        mainController.closePopup(popupCreateList);
        mainController.openPopup(popupShoppingLists);
        popupShoppingListsController.fetchListsData();
    }

    @FXML
    public void handleOpenRulesAndChores() {
        mainController.closePopup(popupCreateChore);
        mainController.openPopup(popupRulesAndChores);
        popupRulesAndChoresController.fetchChoresData();
    }

    @FXML
    public void handleOpenManageMembers() {
        mainController.openPopup(popupManageMembers);
        popupManageMembersController.fetchMembersData();
    }

    @FXML
    public void handleOpenInviteMember() {
        popupInviteMemberController.fetchInvitationCode();
        mainController.openPopup(popupInviteMember);
    }

    public void setAdminMode(boolean isAdmin){
        popupInviteMemberController.setAdminMode(isAdmin);
        popupManageMembersController.setAdminMode(isAdmin);
        popupRulesAndChoresController.setAdminMode(isAdmin);
    }

    private void openShoppingListDetails(ShoppingListResponseDTO selectedList) {
        mainController.closePopup(popupShoppingLists);

        PopupListDetailsController detailsController =
                new PopupListDetailsController(services, mainController, () -> {
                    mainController.closePopup(popupListDetails);
                    mainController.removePopupFromLayer(popupListDetails);
                    mainController.openPopup(popupShoppingLists);
                    popupShoppingListsController.fetchListsData();
                }, selectedList);

        popupListDetails = loadPopup("/com/housemate/client/popups/household/popup_list_details.fxml", detailsController);

        popupListDetails.setVisible(true);
        popupListDetails.setManaged(true);
        mainController.openPopup(popupListDetails);
    }

    private void openCreateList() {
        mainController.closePopup(popupShoppingLists);
        mainController.openPopup(popupCreateList);
    }

    private void openCreateChore() {
        mainController.closePopup(popupRulesAndChores);
        mainController.openPopup(popupCreateChore);
    }

    private void loadPopups() {

        popupManageMembersController = new PopupManageMembersController(services, mainController);
        popupManageMembers = loadPopup("/com/housemate/client/popups/household/popup_manage_members.fxml",
                popupManageMembersController);

        popupInviteMemberController = new PopupInviteMemberController(services, mainController);
        popupInviteMember = loadPopup("/com/housemate/client/popups/household/popup_invite_member.fxml",
                popupInviteMemberController);

        popupRulesAndChoresController = new PopupRulesAndChoresController(services, mainController, this::openCreateChore);
        popupRulesAndChores = loadPopup("/com/housemate/client/popups/household/popup_rules_and_chores.fxml",
                popupRulesAndChoresController);

        popupCreateChore = loadPopup("/com/housemate/client/popups/household/popup_create_chore.fxml",
                new PopupCreateChoreController(services, mainController, this::handleOpenRulesAndChores));

        popupShoppingListsController = new PopupShoppingListsController(
                services, mainController, this::openShoppingListDetails, this::openCreateList
        );
        popupShoppingLists = loadPopup("/com/housemate/client/popups/household/popup_shopping_lists.fxml", popupShoppingListsController);

        popupCreateList = loadPopup("/com/housemate/client/popups/household/popup_create_list.fxml",
                new PopupCreateListController(services, mainController, this::handleOpenShoppingLists));
    }

    private StackPane loadPopup(String fxmlPath, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(clazz -> controller);

            StackPane popup = loader.load();

            mainController.addPopupToLayer(popup);
            popup.setVisible(false);
            popup.setManaged(false);

            return popup;
        } catch (IOException e) {
            throw new RuntimeException("Critical error loading popup: " + fxmlPath, e);
        }
    }
}
