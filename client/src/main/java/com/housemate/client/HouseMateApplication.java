package com.housemate.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.housemate.client.controllers.AuthScreenController;
import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.client.service.context.AuthState;
import com.housemate.client.service.context.ClientContext;
import com.housemate.client.service.context.SessionManager;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Objects;
import java.util.UUID;

public class HouseMateApplication extends Application {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final ClientContext clientContext = new ClientContext(new AuthState());

    private AppServices services;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        this.services = new AppServices(httpClient, objectMapper, clientContext);

        String token = SessionManager.getToken();

        if (token == null) {
            showLoginScreen();
            return;
        }

        String userId = SessionManager.getUserId();
        String householdId = SessionManager.getHouseholdId();

        //TODO un-comment this when the user and household clientservice-controller-service chain is implemented
        /*
        try {
            //these calls return 401 if the memorized token is no longer valid
            UserResponseDTO currentUser = services.getUserClientService().getUserById(UUID.fromString(userId));
            HouseholdResponseDTO currentHousehold = services.getHouseholdClientService().getHouseholdById(UUID.fromString(householdId));
            services.setCurrentUser(currentUser);
            services.setCurrentHousehold(currentHousehold);
            services.getClientContext().getAuthState().setToken(token);
            showMainScreen();
        } catch (UnauthorizedException e) {
            SessionManager.clearSession();
            showLoginScreen();
        }
        */

    }

    public void logout(){
        SessionManager.clearSession();
        services.getClientContext().getAuthState().clear();
        this.services = new AppServices(httpClient, objectMapper, clientContext);
        showLoginScreen();
    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("auth.fxml"));
            loader.setControllerFactory(clazz -> new AuthScreenController(this.services, this::showMainScreen));
            Scene scene = new Scene(loader.load(), 420, 680);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());

            primaryStage.setTitle("HouseMate - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMainScreen() {
        try {
            //load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));

            //use controller factory to inject the services dependency
            loader.setControllerFactory(clazz -> new MainController(services, this::logout));

            Scene scene = new Scene(loader.load(), 420, 680);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());

            primaryStage.setTitle("HouseMate");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
