package com.housemate.client.service;

import com.housemate.shared.dto.chore.request.ChoreAssignmentCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreReassignRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.ChoreStatus;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static com.housemate.client.config.ApiConfig.BASE_URL;

public class  ChoreClientService extends ClientService {

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








