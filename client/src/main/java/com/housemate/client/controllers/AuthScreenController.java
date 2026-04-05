package com.housemate.client.controllers;

import com.housemate.client.service.AppServices;
import com.housemate.client.service.context.SessionManager;
import com.housemate.shared.dto.auth.request.LoginRequestDTO;
import com.housemate.shared.dto.auth.request.RegisterRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import javax.security.auth.login.LoginException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AuthScreenController {

    @FXML private CheckBox ckbRememberMe;
    @FXML private Button btnRegister;
    @FXML private TextField txtCreateEmail, txtCreateName, txtCreateSurname, txtCreatePassword, txtConfirmPassword;
    @FXML private TextField txtEmail, txtPassword;
    @FXML private Label lblSigningUp, lblSignedUp, lblRegisterError;
    @FXML private Label lblLoggingIn, lblLoginError;
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

        hideInformationLabels();
        ckbRememberMe.setDisable(true);
        btnRegister.setDisable(true);
        lblLoggingIn.setVisible(true);
        lblLoggingIn.setManaged(true);

        CompletableFuture.runAsync(() -> {

            String errorMessage = null;

            try {
                //the login api call also saves the returned token in the client context
                UserResponseDTO currentUser = services.getAuthClientService()
                        .login(new LoginRequestDTO(txtEmail.getText(), txtPassword.getText()));

                services.setCurrentUser(currentUser);

                //this block is required since an IllegalStateException is thrown if the user is not in a household
                //and not being in a household when logging in can be a perfectly correct scenario
                try{
                    services.setCurrentHousehold(services.getHouseholdClientService().getCurrentUserHousehold());
                }catch(RuntimeException e){
                    services.setCurrentHousehold(null);
                }

                if (ckbRememberMe.isSelected()) {
                    SessionManager.saveSession(
                            services.getClientContext().getAuthState().getJwt()
                    );
                }

                Platform.runLater(onLoginSuccess);

            } catch (LoginException e) {
                errorMessage = "Login failed: invalid credentials.";
                e.printStackTrace();
            }catch (RuntimeException e){
                errorMessage = "An unexpected error happened while connecting to the server. Please try again later.";
                e.printStackTrace();
            }

            if (errorMessage != null) {
            //reassignment required because the variable is used in a lambda expression
            final String finalErrorMessage = errorMessage;

            Platform.runLater(() -> {
                hideInformationLabels();
                lblLoginError.setText(finalErrorMessage);
                lblLoginError.setVisible(true);
                lblLoginError.setManaged(true);
                ckbRememberMe.setDisable(false);
                btnRegister.setDisable(false);
            });
        }
        });
    }

    @FXML
    public void handleRegisterPageSwitch() {
        hideInformationLabels();
        txtCreateEmail.setText(txtEmail.getText());
        txtEmail.clear();
        swapPage(registerPanel);
    }

    @FXML
    public void handleLoginPageSwitch() {
        hideInformationLabels();
        swapPage(loginPanel);
    }

    @FXML
    public void handleRegister() {

        hideInformationLabels();

        if(!Objects.equals(txtCreatePassword.getText(), txtConfirmPassword.getText())) {
            lblRegisterError.setText("Passwords do not match.");
            lblRegisterError.setVisible(true);
            lblRegisterError.setManaged(true);
            return;
        }

        lblSigningUp.setVisible(true);
        lblSigningUp.setManaged(true);

        CompletableFuture.runAsync(() -> {

            String errorMessage = null;

            try {
                //this does not save anything, since the user is redirected to the login page
                services.getAuthClientService().register(new RegisterRequestDTO(
                        txtCreateName.getText(),
                        txtCreateSurname.getText(),
                        txtCreateEmail.getText(),
                        txtCreatePassword.getText(),
                        null
                ));

                Platform.runLater(() -> {
                    lblSignedUp.setVisible(true);
                    lblSignedUp.setManaged(true);
                    txtEmail.setText(txtCreateEmail.getText());
                    swapPage(loginPanel);
                    lblSigningUp.setVisible(false);
                    lblSigningUp.setManaged(false);
                    lblRegisterError.setVisible(false);
                    lblRegisterError.setManaged(false);
                });

            } catch (LoginException e) {
                errorMessage = "Registration failed: invalid inputs.";
                e.printStackTrace();
            }catch (RuntimeException e){
                errorMessage = "An unexpected error happened while connecting to the server. Please try again later.";
                e.printStackTrace();
            }

            if (errorMessage != null) {
                //reassignment required because the variable is used in a lambda expression
                final String finalErrorMessage = errorMessage;

                Platform.runLater(() -> {
                    hideInformationLabels();
                    lblRegisterError.setText(finalErrorMessage);
                    lblRegisterError.setVisible(true);
                    lblRegisterError.setManaged(true);
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

    private void hideInformationLabels() {
        lblLoginError.setVisible(false);
        lblLoginError.setManaged(false);
        lblSigningUp.setVisible(false);
        lblSigningUp.setManaged(false);
        lblRegisterError.setVisible(false);
        lblRegisterError.setManaged(false);
        lblLoggingIn.setVisible(false);
        lblLoggingIn.setManaged(false);
        lblSignedUp.setVisible(false);
        lblSignedUp.setManaged(false);
    }
}
