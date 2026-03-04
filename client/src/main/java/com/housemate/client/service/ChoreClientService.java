package com.housemate.client.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.housemate.shared.dto.chore.request.ChoreAssignmentCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.ChoreStatus;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static com.housemate.client.config.ApiConfig.BASE_URL;

public class ChoreClientService {

    private String serializeDTO(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize DTO to JSON", e);
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
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

    private final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChoreResponseDTO createChore(ChoreRequestDTO requestDTO) {

        String jsonRequestBody = serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/chores"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();


        HttpResponse<String> response = sendRequest(request);

        if(response.statusCode() != 201) {
            throw new RuntimeException("Failed to create chore. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return deserializeDTO(response.body(), ChoreResponseDTO.class);

    }

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

    public void deleteChore(ChoreRequestDTO requestDTO) {

        String jsonRequestBody = serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/chores"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonRequestBody))
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
                .PUT(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
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
}
