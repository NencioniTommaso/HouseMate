package com.housemate.client;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HouseMateApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        AppServices services = new AppServices();

        //load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));

        //use controller factory to inject the services dependency
        loader.setControllerFactory(clazz -> new MainController(services));

        Scene scene = new Scene(loader.load(), 420, 680);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        primaryStage.setTitle("HouseMate");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
