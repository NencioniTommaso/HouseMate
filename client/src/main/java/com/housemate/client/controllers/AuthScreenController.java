package com.housemate.client.controllers;

import com.housemate.client.service.AppServices;
import com.housemate.client.service.context.SessionManager;
import com.housemate.shared.dto.auth.request.LoginRequestDTO;
import com.housemate.shared.dto.auth.request.RegisterRequestDTO;
import com.housemate.shared.dto.auth.response.LoginResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import javax.security.auth.login.LoginException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AuthScreenController {

    @FXML private CheckBox ckbRememberMe;
    @FXML private Button btnRegister;
    @FXML private TextField txtCreateEmail, txtCreateName, txtCreateSurname, txtCreatePassword, txtConfirmPassword;
    @FXML private TextField txtEmail, txtPassword;
    @FXML private Label signingInLabel;
    @FXML private Label signedUpLabel;
    @FXML private Label loggingInLabel;
    @FXML private VBox loginPanel;
    @FXML private VBox registerPanel;

    private final AppServices services;
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

        ckbRememberMe.setDisable(true);
        btnRegister.setDisable(true);
        loggingInLabel.setVisible(true);
        loggingInLabel.setManaged(true);

        CompletableFuture.runAsync(() -> {
            try {

                UserResponseDTO responseDTO = services.getAuthClientService()
                        .login(new LoginRequestDTO(txtEmail.getText(), txtPassword.getText()));

                services.setCurrentUser(responseDTO);

                //call to api/users/me to get current household id, for now it's a fake household
                //services.setCurrentHousehold(...);
                HouseholdResponseDTO fakeHousehold = new HouseholdResponseDTO(UUID.randomUUID(), "Fake Household", null, null);
                services.setCurrentHousehold(fakeHousehold);

                if (ckbRememberMe.isSelected()) {
                    SessionManager.saveSession(services.getClientContext().getAuthState().getJwt(),
                                               String.valueOf(services.getCurrentUser().id()), null);
                }

                Platform.runLater(onLoginSuccess);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loggingInLabel.setText("Login failed. Please try again.");
                    ckbRememberMe.setDisable(false);
                    btnRegister.setDisable(false);
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

        signingInLabel.setVisible(true);
        signingInLabel.setManaged(true);

        CompletableFuture.runAsync(() -> {
            try {

                if(!txtCreatePassword.getText().equals(txtConfirmPassword.getText())){
                    Platform.runLater(() -> {
                        signingInLabel.setText("Passwords do not match.");
                    });
                    return;
                }

                //this does not save anything, since the user is redirected to the login page
                services.getAuthClientService().register(new RegisterRequestDTO(
                        txtCreateName.getText(),
                        txtCreateSurname.getText(),
                        txtCreateEmail.getText(),
                        txtCreatePassword.getText(),
                        null
                ));

                Platform.runLater(() -> {
                    signedUpLabel.setVisible(true);
                    txtEmail.setText(txtCreateEmail.getText());
                    swapPage(loginPanel);
                    signingInLabel.setVisible(false);
                    signingInLabel.setManaged(false);
                });
            } catch (LoginException e) {
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
