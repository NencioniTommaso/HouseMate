package com.housemate.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.housemate.shared.dto.chore.request.ChoreAssignmentCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreReassignRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.ChoreStatus;

import static com.housemate.client.config.ApiConfig.BASE_URL;

/*
* A unified ClientService which exposes methods for all client operations, performing the corresponding HTTP requests.
*/

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

public class ClientService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static volatile ClientService instance = new ClientService();

    //Private constructor to prevent instantiation
    private ClientService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public static ClientService getInstance() {
        //Double-checked locking for thread-safe singleton instantiation
        synchronized (ClientService.class) {
            if (instance == null) {
                instance = new ClientService();
            }
        }
        return instance;
    }

    // Helper methods for HTTP communication and JSON processing

    private String serializeDTO(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize DTO to JSON", e);
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request Interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("An unexpected error happened while connecting to the server", e);
        }
    }

    private <T> T deserializeDTO(String json, Class<T> clazz) {

        T responseDTO;
        try {
            responseDTO = objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to DTO", e);
        }

        return responseDTO;
    }

    private <T> List<T> deserializeDTOList(String json, Class<T> clazz) {

        List<T> responseDTOList;
        try {
            responseDTOList = objectMapper.readValue(json,
                    TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to DTO List", e);
        }

        return responseDTOList;
    }

    //Methods to perform specific HTTP requests
    //Chores

    public ChoreAssignmentResponseDTO createAssignment(ChoreAssignmentCreateRequestDTO requestDTO){

        String jsonRequestBody = serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/chores/assignments"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        HttpResponse<String> response = sendRequest(request);

        if(response.statusCode() != 201) {
            throw new RuntimeException("Failed to create chore assignment. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return deserializeDTO(response.body(), ChoreAssignmentResponseDTO.class);
    }

    public void deleteChore(UUID choreId) {


        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/chores/" + choreId))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = sendRequest(request);

        if(response.statusCode() != 204) {
            throw new RuntimeException("Failed to delete chore. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }
    }

    public List<ChoreResponseDTO> getAllHouseholdChores(UUID householdId) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/chores/" + householdId))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(request);

        if(response.statusCode() != 200) {
            throw new RuntimeException("Failed to retrieve household chores. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return deserializeDTOList(response.body(), ChoreResponseDTO.class);
    }

    public void updateChoreAssignmentStatus(UUID assignmentId, ChoreStatusUpdateRequestDTO requestDTO) {

        String jsonRequestBody = serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/chores/assignments/" + assignmentId + "/status"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        HttpResponse<String> response = sendRequest(request);

        if(response.statusCode() != 204) {
            throw new RuntimeException("Failed to update chore assignment status. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }
    }

    public List<ChoreAssignmentResponseDTO> getUserAssignments(UUID userId, ChoreStatus status) {

        String uri = BASE_URL + "/api/chores/assignments/user/" + userId;
        if (status != null) {
            uri += "?status=" + status.name();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(uri))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(request);

        if(response.statusCode() != 200) {
            throw new RuntimeException("Failed to retrieve user chore assignments. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return deserializeDTOList(response.body(), ChoreAssignmentResponseDTO.class);
    }

    public List<ChoreAssignmentResponseDTO> getHouseholdAssignments(UUID householdId, ChoreStatus status) {

        String uri = BASE_URL + "/api/chores/assignments/household/" + householdId;
        if (status != null) {
            uri += "?status=" + status.name();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(uri))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(request);

        if(response.statusCode() != 200) {
            throw new RuntimeException("Failed to retrieve household chore assignments. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return deserializeDTOList(response.body(), ChoreAssignmentResponseDTO.class);
    }

    public ChoreAssignmentResponseDTO reassignChore(UUID assignmentId, ChoreReassignRequestDTO requestDTO) {

        String jsonRequestBody = serializeDTO(requestDTO);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/chores/assignments/" + assignmentId + "/reassign"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        HttpResponse<String> response = sendRequest(request);

        if(response.statusCode() != 200) {
            throw new RuntimeException("Failed to reassign chore. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return deserializeDTO(response.body(), ChoreAssignmentResponseDTO.class);
    }
}






