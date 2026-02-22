package com.housemate.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.shared.dto.StatusDTO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class App extends Application {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper(); // Per leggere il JSON

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

        // Usiamo un thread separato per non bloccare la grafica (Regola d'oro!)
        new Thread(() -> {
            try {
                // 1. Prepara la richiesta
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/status"))
                        .GET()
                        .build();

                // 2. Invia la richiesta
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // 3. Trasforma il JSON (Testo) in Oggetto Java (Shared DTO)
                StatusDTO status = objectMapper.readValue(response.body(), StatusDTO.class);

                // 4. Aggiorna la grafica (dobbiamo tornare nel thread grafico)
                Platform.runLater(() -> {
                    label.setText("Risposta: " + status.message() + "\nOre: " + status.timestamp());
                    label.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    label.setText("Errore: Server non raggiungibile!");
                    label.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}