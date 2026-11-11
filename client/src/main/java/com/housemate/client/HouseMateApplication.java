package com.housemate.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.housemate.client.controllers.AuthScreenController;
import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.client.service.context.AuthState;
import com.housemate.client.service.context.SessionManager;
import com.housemate.client.service.context.JwtPersistanceHandler;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Objects;

public class HouseMateApplication extends Application {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final SessionManager clientContext = new SessionManager(new AuthState());

    private AppServices services;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        this.services = new AppServices(httpClient, objectMapper, clientContext);

        String token = JwtPersistanceHandler.getToken();

        if (token == null) {
            showLoginScreen();
            return;
        }

        //these two http calls retrieve the logged user's data and its household's data
        //the correct data is retrieved because of the JWT token being retrieved from the session manager
        try {
            //set the token first, to include it in the next requests
            services.getSessionManager().getAuthState().setJwt(token);
            UserResponseDTO currentUser = services.getUserClientService().getCurrentUser();
            services.getSessionManager().setCurrentUser(currentUser);
        } catch (RuntimeException e) {
            JwtPersistanceHandler.clearSession();
            showLoginScreen();
        }

        try{
            services.getSessionManager().setCurrentHousehold(services.getHouseholdClientService().getCurrentUserHousehold());
        }catch (RuntimeException e){
            services.getSessionManager().setCurrentHousehold(null);
        }
        showMainScreen();
    }

    public void logout(){
        JwtPersistanceHandler.clearSession();
        services.getSessionManager().getAuthState().clear();
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
