package com.housemate.client.controllers.tabs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TabAssignmentsController {

    @FXML private FlowPane searchFiltersPanel;
    @FXML private Button btnAddAssignment, btnPrevWeek, btnNextWeek;
    @FXML private Button btnClearFilters;
    @FXML private CheckBox chkMyTasks, chkPending;
    @FXML private ComboBox<?> cmbUserFilter;
    @FXML private TextField txtSearch;
    @FXML private GridPane scheduleGrid;
    @FXML private VBox vboxMonday, vboxTuesday, vboxWednesday, vboxThursday, vboxFriday, vboxSaturday, vboxSunday;
    @FXML private VBox detailsPane;
    @FXML private Label lblDetailTitle, lblDetailDesc, lblDetailUser, lblDetailDate, lblDetailStatus;
    @FXML private Button btnDelete, btnReschedule, btnComplete, btnCloseDetails;

    // Popup assignment (caricato dinamicamente dal MainController)
    private StackPane popupAddAssignment;
    private ComboBox<?> cmbAssignUser;
    private DatePicker dateDueDate;
    private Button btnCloseAddAssignment, btnCancelAssignment, btnCreateAssignment;

    private StackPane mainContentContainer;
    private StackPane popupLayer;
    private Button btnNavH, btnNavC, btnNavE, btnNavU;

    // Setter per i container
    public void setContainers(StackPane mainContentContainer, StackPane popupLayer, Button btnNavH, Button btnNavC, Button btnNavE, Button btnNavU) {
        this.mainContentContainer = mainContentContainer;
        this.popupLayer = popupLayer;
        this.btnNavH = btnNavH;
        this.btnNavC = btnNavC;
        this.btnNavE = btnNavE;
        this.btnNavU = btnNavU;
    }

    // Setter per il popup Add Assignment (caricato dal MainController)
    public void setPopupAddAssignment(StackPane popupAddAssignment, Button btnCloseAddAssignment, Button btnCancelAssignment, Button btnCreateAssignment) {
        this.popupAddAssignment = popupAddAssignment;
        this.btnCloseAddAssignment = btnCloseAddAssignment;
        this.btnCancelAssignment = btnCancelAssignment;
        this.btnCreateAssignment = btnCreateAssignment;
        // Recupera cmbAssignUser e dateDueDate dal popup
        this.cmbAssignUser = (ComboBox<?>) popupAddAssignment.lookup("#cmbAssignUser");
        this.dateDueDate = (DatePicker) popupAddAssignment.lookup("#dateDueDate");
    }

    @FXML
    public void initialize() {
        // Logica di chiusura del detailsPane
        btnCloseDetails.setOnAction(e -> {
            btnPrevWeek.setDisable(false);
            btnNextWeek.setDisable(false);
            searchFiltersPanel.setVisible(true);
            searchFiltersPanel.setManaged(true);
            disableNavigationButtons(false);
            detailsPane.setVisible(false);
            detailsPane.setManaged(false);
        });

        // Setup del listener per il bottone "Add Assignment"
        if (btnAddAssignment != null) {
            btnAddAssignment.setOnAction(e -> openAddAssignmentPopup());
        }

        // Logica per mostrare il detailsPane quando clicchi su un compito
        setupAssignmentListeners();
    }

    // Metodo per impostare i listener dei compiti
    private void setupAssignmentListeners() {
        // Aggiungi listener per mostrare il detailsPane quando clicchi su un compito
        vboxTuesday.setOnMouseClicked(e -> {
            btnPrevWeek.setDisable(true);
            btnNextWeek.setDisable(true);
            searchFiltersPanel.setVisible(false);
            searchFiltersPanel.setManaged(false);
            disableNavigationButtons(true);
            detailsPane.setVisible(true);
            detailsPane.setManaged(true);
        });
    }

    // Metodo per aprire il popup "Add Assignment"
    private void openAddAssignmentPopup() {
        if (popupAddAssignment != null && mainContentContainer != null && popupLayer != null) {
            popupLayer.setMouseTransparent(false);
            mainContentContainer.setEffect(new GaussianBlur(15));
            popupAddAssignment.setVisible(true);
            popupAddAssignment.setManaged(true);
            disableNavigationButtons(true);
        }
    }

    // Metodo per chiudere il popup "Add Assignment"
    private void closeAddAssignmentPopup() {
        if (popupAddAssignment != null && mainContentContainer != null && popupLayer != null) {
            popupLayer.setMouseTransparent(true);
            mainContentContainer.setEffect(null);
            popupAddAssignment.setVisible(false);
            popupAddAssignment.setManaged(false);
            disableNavigationButtons(false);
            // Pulisci i campi del form
            if (cmbAssignUser != null) {
                cmbAssignUser.getSelectionModel().clearSelection();
            }
            if (dateDueDate != null) {
                dateDueDate.setValue(null);
            }
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








