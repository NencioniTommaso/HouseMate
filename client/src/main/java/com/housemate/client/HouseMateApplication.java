package com.housemate.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.housemate.client.controllers.AuthScreenController;
import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.client.service.context.AuthState;
import com.housemate.client.service.context.ClientContext;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.UUID;

public class HouseMateApplication extends Application {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private ClientContext clientContext = new ClientContext(new AuthState());


    private AppServices services;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        this.services = new AppServices(httpClient, objectMapper, clientContext);

        doDevAutoLogin(services);

        //the login screen always appears first, if a user chose "remember me"
        //the fields will automatically be filled (pw won't be real) and the user can just click "login"
        //the login will be executed with the jwt token, no password verification will be done
        showLoginScreen();

    }

    public void logout(){
        clientContext.getAuthState().clear();
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

    private void doDevAutoLogin(AppServices services) {
        try {
            System.out.println("⏳ Richiesta dati freschi al Backend (Dev Backdoor)...");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/dev/mock-login"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.body());

                // 1. Estraiamo i dati freschi
                String token = json.get("token").asText();
                UUID userId = UUID.fromString(json.get("userId").asText());
                UUID householdId = UUID.fromString(json.get("householdId").asText());

                // 2. Salviamo il token nel contesto globale (così HttpRestClient lo userà sempre)
                services.getClientContext().getAuthState().setJwt(token);


                // 3. Creiamo dei DTO "finti" solo con gli ID per far funzionare i tuoi Controller
                // NOTA: verifica che i costruttori combacino con i tuoi record/classi in `shared`
                UserResponseDTO fakeUser = new UserResponseDTO(userId, "Mario", "Rossi", "test@email.com", null);
                HouseholdResponseDTO fakeHousehold = householdId != null ?
                        new HouseholdResponseDTO(householdId, "Casa di Test", null, null) : null;

                services.setCurrentUser(fakeUser);
                services.setCurrentHousehold(fakeHousehold);

                System.out.println("✅ DEV Auto-Login perfetto! Sincronizzato con l'utente: " + userId);
            } else {
                System.err.println("❌ Errore Backend: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Impossibile contattare il server. È acceso? Errore: " + e.getMessage());
        }
    }
}
