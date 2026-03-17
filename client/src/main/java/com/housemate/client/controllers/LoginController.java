package com.housemate.client.controllers;

import com.housemate.client.service.AppServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.concurrent.CompletableFuture;

public class LoginController {

    @FXML private Label loggingInLabel;

    private AppServices services;
    private final Runnable onLoginSuccess;

    public LoginController(AppServices services, Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        this.services = services;

    }

    @FXML
    public void initialize() {


    }

    @FXML
    public void handleLogin() {

        //get username and password from text fields

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
    public void handleRegister() {
        //navigate to register screen

     }

}
