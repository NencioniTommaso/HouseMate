package com.housemate.client.controllers.tabs.household;

import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.popups.household.*;
import com.housemate.client.service.AppServices;
import com.housemate.shared.enums.InvitationMode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import lombok.Getter;

import java.io.IOException;

public class TabHouseholdController {

    @FXML private Button btnManageMembers, btnRulesAndChores, btnSettings, btnInviteMember, btnPlanAssignments, btnViewInvitations;

    private StackPane popupManageMembers;
    private StackPane popupRulesAndChores;
    private StackPane popupSettings;
    private StackPane popupInviteMember;
    private StackPane popupCreateChore;

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
        btnRulesAndChores.setOnAction(e -> mainController.openPopup(popupRulesAndChores));
        btnSettings.setOnAction(e -> mainController.openPopup(popupSettings));
        btnInviteMember.setOnAction(e -> mainController.openPopup(popupInviteMember));
    }
    
    private void loadPopups() {

        try {
            // Load Manage Members Popup
            FXMLLoader loaderManageMembers = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/household/popup_manage_members.fxml"));
            loaderManageMembers.setControllerFactory(
                    clazz -> new PopupManageMembersController(this.services, this.mainController));
            popupManageMembers = loaderManageMembers.load();
            mainController.addPopupToLayer(popupManageMembers);
            popupManageMembers.setVisible(false);
            popupManageMembers.setManaged(false);

            // Load Rules and Chores Popup
            Runnable onChoreCreateCallback = () -> {
                mainController.closePopup(popupRulesAndChores);
                mainController.openPopup(popupCreateChore);
            };
            FXMLLoader loaderRulesAndChores = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/household/popup_rules_and_chores.fxml"));
            loaderRulesAndChores.setControllerFactory(
                    clazz -> new PopupRulesAndChoresController(this.services, this.mainController, onChoreCreateCallback));
            popupRulesAndChores = loaderRulesAndChores.load();
            mainController.addPopupToLayer(popupRulesAndChores);
            popupRulesAndChores.setVisible(false);
            popupRulesAndChores.setManaged(false);

            // Load Shopping Lists Popup
            FXMLLoader loaderShoppingLists = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/household/popup_shopping_lists.fxml"));
            loaderShoppingLists.setControllerFactory(
                    clazz -> new PopupShoppingListsController(this.services, this.mainController));
            popupSettings = loaderShoppingLists.load();
            mainController.addPopupToLayer(popupSettings);
            popupSettings.setVisible(false);
            popupSettings.setManaged(false);

            // Load Invite Member Popup
            FXMLLoader loaderInviteMember = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/household/popup_invite_member.fxml"));
            loaderInviteMember.setControllerFactory(
                    clazz -> new PopupInviteMemberController(this.services, this.mainController));
            popupInviteMember = loaderInviteMember.load();
            mainController.addPopupToLayer(popupInviteMember);
            popupInviteMember.setVisible(false);
            popupInviteMember.setManaged(false);

            // Load Create Chore Popup
            Runnable onReturnCallback = () -> {
                mainController.closePopup(popupCreateChore);
                mainController.openPopup(popupRulesAndChores);
            };
            FXMLLoader loaderCreateChore = new FXMLLoader(getClass().getResource("/com/housemate/client/popups/household/popup_create_chore.fxml"));
            loaderCreateChore.setControllerFactory(
                    clazz -> new PopupCreateChoreController(this.services, this.mainController, onReturnCallback));
            popupCreateChore = loaderCreateChore.load();
            mainController.addPopupToLayer(popupCreateChore);
            popupCreateChore.setVisible(false);
            popupCreateChore.setManaged(false);

        } catch (IOException e) {
            throw new RuntimeException("Error loading popup: " + e.getMessage(), e);
        }
    }
}
