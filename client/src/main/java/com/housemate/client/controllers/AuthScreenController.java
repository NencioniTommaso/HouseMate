package com.housemate.client.controllers;

import com.housemate.client.service.AppServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.concurrent.CompletableFuture;

public class AuthScreenController {

    @FXML private TextField txtCreateEmail;
    @FXML private TextField txtEmail;
    @FXML private Label signingInLabel;
    @FXML private Label signedUpLabel;
    @FXML private Label loggingInLabel;
    @FXML private VBox loginPanel;
    @FXML private VBox registerPanel;

    private AppServices services;
    private final Runnable onLoginSuccess;

    public AuthScreenController(AppServices services, Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        this.services = services;

    }

    @FXML
    public void initialize() {

        loginPanel.setVisible(true);
        loginPanel.setManaged(true);
        registerPanel.setVisible(false);
        registerPanel.setManaged(false);

    }

    @FXML
    public void handleLogin() {

        loggingInLabel.setVisible(true);
        loggingInLabel.setManaged(true);

        CompletableFuture.runAsync(() -> {
            try {
                //api call to authenticate user and store token in services
                Thread.sleep(1500); // Simulate network delay
                Platform.runLater(onLoginSuccess);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loggingInLabel.setText("Login failed. Please try again.");
                });
            }
        });
    }

    @FXML
    public void handleRegisterPageSwitch() {
        txtCreateEmail.setText(txtEmail.getText());
        txtEmail.clear();
        swapPage(registerPanel);

    }

    @FXML
    public void handleRegister() {
        //get username and password from text fields

        signingInLabel.setVisible(true);
        signingInLabel.setManaged(true);

        CompletableFuture.runAsync(() -> {
            try {
                //api call to register user
                Thread.sleep(1500); // Simulate network delay
                Platform.runLater(() -> {
                    signedUpLabel.setVisible(true);
                    txtEmail.setText(txtCreateEmail.getText());
                    swapPage(loginPanel);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    //this thing could show why registration failed
                    signedUpLabel.setText("Registration failed. Please try again.");
                });
            }
        });
    }

    private void swapPage(VBox activePage){
        VBox pageToHide = activePage == loginPanel ? registerPanel : loginPanel;
        activePage.setVisible(true);
        activePage.setManaged(true);
        pageToHide.setVisible(false);
        pageToHide.setManaged(false);
    }
}
