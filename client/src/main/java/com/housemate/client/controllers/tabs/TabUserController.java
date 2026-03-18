package com.housemate.client.controllers.tabs;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class TabUserController {

    @FXML private HBox hboxEditActions;
    @FXML private Button btnEditProfile;
    @FXML private Label lblFirstName;
    @FXML private TextField txtFirstName;
    @FXML private Label lblLastName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private Label lblEmail;
    @FXML private Label lblPaymentLink;
    @FXML private TextField txtPaymentLink;


    private AppServices services;
    private MainController mainController;
    private final Runnable logoutHandler;

    public TabUserController(AppServices services, MainController mainController,  Runnable logoutHandler) {
        this.services = services;
        this.mainController = mainController;
        this.logoutHandler = logoutHandler;
    }

    @FXML
    public void initialize() {
    }

    @FXML
    public void handleLogout() {
        logoutHandler.run();
    }

    @FXML
    public void handleLeaveCurrentHousehold() {
    }

    @FXML
    public void handleSaveProfile() {
        //call client service
        setEditMode(false);
    }

    @FXML
    public void handleEditProfile() {
        setEditMode(true);
    }

    @FXML
    public void handleCancelEdit( ) {
    }

    private void setEditMode(boolean editing) {

        btnEditProfile.setVisible(!editing);
        btnEditProfile.setManaged(!editing);

        hboxEditActions.setVisible(editing);
        hboxEditActions.setManaged(editing);

        lblFirstName.setVisible(!editing);
        txtFirstName.setVisible(editing);

        lblLastName.setVisible(!editing);
        txtLastName.setVisible(editing);

        lblEmail.setVisible(!editing);
        txtEmail.setVisible(editing);

        lblPaymentLink.setVisible(!editing);
        txtPaymentLink.setVisible(editing);
    }
}

