package com.housemate.client;

import com.housemate.client.controllers.AuthScreenController;
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

        //the login screen always appears first, if a user chose "remember me"
        //the fields will automatically be filled (pw won't be real) and the user can just click "login"
        //the login will be executed with the jwt token, no password verification will be done
        showLoginScreen();

    }

    public void logout(){
        this.services = new AppServices();
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
