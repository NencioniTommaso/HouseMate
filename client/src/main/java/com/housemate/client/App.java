package com.housemate.client;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("Premi il bottone per contattare il server...");
        Button button = new Button("Chiama Server");

        button.setOnAction(e -> callServer(label));

        VBox root = new VBox(20, label, button);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("HouseMate Client Test");
        stage.setScene(scene);
        stage.show();
    }

    private void callServer(Label label) {
        label.setText("Connessione in corso...");
    }

    public static void main(String[] args) {
        launch();
    }
}