package com.housemate.client;

import com.housemate.client.controllers.LoginController;
import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HouseMateApplication extends Application {

    private AppServices services;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        this.services = new AppServices();

        if (services.isUserLoggedIn()) {
            showMainScreen();
        } else {
            showLoginScreen();
        }

    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            loader.setControllerFactory(clazz -> new LoginController(this.services, this::showMainScreen));
            Scene scene = new Scene(loader.load(), 400, 600);
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
            loader.setControllerFactory(clazz -> new MainController(services));

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
