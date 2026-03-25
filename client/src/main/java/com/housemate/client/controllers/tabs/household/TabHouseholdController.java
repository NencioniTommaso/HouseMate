package com.housemate.client.controllers.tabs.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.household.*;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.function.Consumer;

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

        btnManageMembers.setOnAction(e -> mainController.openPopup(popupManageMembers));
        btnInviteMember.setOnAction(e -> mainController.openPopup(popupInviteMember));
        btnRulesAndChores.setOnAction(e -> openRulesAndChores());
    }

    @FXML
    public void handleOpenShoppingLists() {
        mainController.openPopup(popupShoppingLists);
    }

    private void openShoppingListDetails(ShoppingListResponseDTO selectedList) {
        mainController.closePopup(popupShoppingLists);

        PopupListDetailsController detailsController =
                new PopupListDetailsController(services, mainController, () -> {
                    mainController.closePopup(popupListDetails);
                    mainController.openPopup(popupShoppingLists);
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

    private void openRulesAndChores() {
        mainController.closePopup(popupCreateChore); // Se veniamo da "Create Chore"
        mainController.openPopup(popupRulesAndChores);
        popupRulesAndChoresController.fetchChoresData();
    }

    private void openCreateChore() {
        mainController.closePopup(popupRulesAndChores);
        mainController.openPopup(popupCreateChore);
    }


    private void loadPopups() {

        popupManageMembers = loadPopup("/com/housemate/client/popups/household/popup_manage_members.fxml",
                new PopupManageMembersController(services, mainController));
        popupInviteMember = loadPopup("/com/housemate/client/popups/household/popup_invite_member.fxml",
                new PopupInviteMemberController(services, mainController));

        popupRulesAndChoresController = new PopupRulesAndChoresController(services, mainController, this::openCreateChore);
        popupRulesAndChores = loadPopup("/com/housemate/client/popups/household/popup_rules_and_chores.fxml",
                popupRulesAndChoresController);

        popupCreateChore = loadPopup("/com/housemate/client/popups/household/popup_create_chore.fxml",
                new PopupCreateChoreController(services, mainController, () -> {
            mainController.closePopup(popupCreateChore);
            mainController.openPopup(popupRulesAndChores);
            popupRulesAndChoresController.fetchChoresData();
        }));

        this.popupShoppingListsController = new PopupShoppingListsController(
                services, mainController, this::openShoppingListDetails, this::openCreateList
        );
        popupShoppingLists = loadPopup("/com/housemate/client/popups/household/popup_shopping_lists.fxml", popupShoppingListsController);

        popupCreateList = loadPopup("/com/housemate/client/popups/household/popup_create_list.fxml",
                new PopupCreateListController(services, mainController, () -> {
            mainController.closePopup(popupCreateList);
            mainController.openPopup(popupShoppingLists);
            popupShoppingListsController.fetchListsData();
        }));
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
