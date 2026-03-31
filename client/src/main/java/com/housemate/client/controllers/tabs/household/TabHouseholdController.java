package com.housemate.client.controllers.tabs.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.household.*;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class TabHouseholdController {

    @FXML private Button btnManageMembers, btnRulesAndChores, btnInviteMember;

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

    private AppServices services;
    private final MainController mainController;

    public TabHouseholdController(AppServices services, MainController mainController) {

        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

        mainController.enableNavigationButtons(true);

        loadPopups();


        btnInviteMember.setOnAction(e -> mainController.openPopup(popupInviteMember));
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

        popupInviteMember = loadPopup("/com/housemate/client/popups/household/popup_invite_member.fxml",
                new PopupInviteMemberController(services, mainController));

        popupRulesAndChoresController = new PopupRulesAndChoresController(services, mainController, this::openCreateChore);
        popupRulesAndChores = loadPopup("/com/housemate/client/popups/household/popup_rules_and_chores.fxml",
                popupRulesAndChoresController);

        popupCreateChore = loadPopup("/com/housemate/client/popups/household/popup_create_chore.fxml",
                new PopupCreateChoreController(services, mainController, this::handleOpenRulesAndChores));

        this.popupShoppingListsController = new PopupShoppingListsController(
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
