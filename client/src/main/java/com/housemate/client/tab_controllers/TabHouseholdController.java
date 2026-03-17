package com.housemate.client.tab_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;

public class TabHouseholdController {

    @FXML private Button btnManageMembers, btnRulesAndChores, btnSettings, btnInviteMember, btnPlanAssignments;

    private StackPane popupManageMembers;
    private StackPane popupRulesAndChores;
    private StackPane popupSettings;
    private StackPane popupInviteMember;
    private StackPane popupPlanAssignments;
    private StackPane popupCreateChore;

    private StackPane mainContentContainer;
    private StackPane popupLayer;
    private Button btnNavH, btnNavC, btnNavE, btnNavU;

    public void setContainers(StackPane mainContentContainer,
                              StackPane popupLayer,
                              Button btnNavH,
                              Button btnNavC,
                              Button btnNavE,
                              Button btnNavU) {
        this.mainContentContainer = mainContentContainer;
        this.popupLayer = popupLayer;
        this.btnNavH = btnNavH;
        this.btnNavC = btnNavC;
        this.btnNavE = btnNavE;
        this.btnNavU = btnNavU;
    }

    public void setHouseholdPopups(StackPane popupManageMembers,
                                   StackPane popupRulesAndChores,
                                   StackPane popupSettings,
                                   StackPane popupInviteMember,
                                   StackPane popupPlanAssignments,
                                   StackPane popupCreateChore) {
        this.popupManageMembers = popupManageMembers;
        this.popupRulesAndChores = popupRulesAndChores;
        this.popupSettings = popupSettings;
        this.popupInviteMember = popupInviteMember;
        this.popupPlanAssignments = popupPlanAssignments;
        this.popupCreateChore = popupCreateChore;
        
        setupPopupListeners();
    }

    private void setupPopupListeners() {
        if (btnManageMembers != null) {
            btnManageMembers.setOnAction(e -> openPopup(popupManageMembers));
        }
        if (btnRulesAndChores != null) {
            btnRulesAndChores.setOnAction(e -> openPopup(popupRulesAndChores));
        }
        if (btnSettings != null) {
            btnSettings.setOnAction(e -> openPopup(popupSettings));
        }
        if (btnInviteMember != null) {
            btnInviteMember.setOnAction(e -> openPopup(popupInviteMember));
        }
        if (btnPlanAssignments != null) {
            btnPlanAssignments.setOnAction(e -> openPopup(popupPlanAssignments));
        }

        Button btnCreateChore = (Button) popupRulesAndChores.lookup("#btnCreateChore");
        if(btnCreateChore != null) {
            btnCreateChore.setOnAction(e -> {
                closePopup(popupRulesAndChores);
                openPopup(popupCreateChore);
            });
        }

        Button btnReturnToChores =  (Button) popupCreateChore.lookup("#btnReturn");
        if (btnReturnToChores != null) {
            btnReturnToChores.setOnAction(e -> {
                closePopup(popupCreateChore);
                openPopup(popupRulesAndChores);
            });
        }

    }

    public void openPopup(StackPane popup) {
        if (popup != null && mainContentContainer != null && popupLayer != null) {
            popupLayer.setMouseTransparent(false);
            mainContentContainer.setEffect(new GaussianBlur(15));
            popup.setVisible(true);
            popup.setManaged(true);
            disableNavigationButtons(true);
        }
    }

    public void closePopup(StackPane popup) {
        if (popup != null && mainContentContainer != null && popupLayer != null) {
            popupLayer.setMouseTransparent(true);
            mainContentContainer.setEffect(null);
            popup.setVisible(false);
            popup.setManaged(false);
            disableNavigationButtons(false);
        }
    }

    // Metodo per disabilitare/abilitare i bottoni di navigazione
    private void disableNavigationButtons(boolean disable) {
        if (btnNavH != null) btnNavH.setDisable(disable);
        if (btnNavC != null) btnNavC.setDisable(disable);
        if (btnNavE != null) btnNavE.setDisable(disable);
        if (btnNavU != null) btnNavU.setDisable(disable);
    }
}
