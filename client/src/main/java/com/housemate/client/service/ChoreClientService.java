package com.housemate.client.service;

import com.housemate.shared.dto.chore.request.*;
import com.housemate.shared.dto.chore.response.AssignmentOverviewDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import lombok.Getter;
import lombok.NonNull;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static com.housemate.client.config.ApiConfig.BASE_URL;


public class ChoreClientService {

    @Getter
    private final HttpRestClient httpRestClient;

    public ChoreClientService(HttpRestClient httpRestClient) {
        this.httpRestClient = httpRestClient;
    }

    public ChoreResponseDTO createChore(ChoreCreateRequestDTO requestDTO) {

        String jsonRequestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/chores"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to create chore. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return httpRestClient.deserializeDTO(response.body(), ChoreResponseDTO.class);
    }

    public void deleteChore(UUID choreId) {


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/chores/" + choreId))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .method("DELETE", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 204) {
            throw new RuntimeException("Failed to delete chore. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }
    }

    public ChoreAssignmentResponseDTO createAssignment(ChoreAssignmentCreateRequestDTO requestDTO) {

        String jsonRequestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/chores/assignments"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to create chore assignment. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return httpRestClient.deserializeDTO(response.body(), ChoreAssignmentResponseDTO.class);
    }

    public void deleteChoreAssignment(UUID assignmentId) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/chores/assignments/" + assignmentId))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .method("DELETE", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 204) {
            throw new RuntimeException("Failed to delete chore assignment. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }
    }

    public void updateChoreAssignmentStatus(UUID assignmentId, ChoreStatusUpdateRequestDTO requestDTO) {

        String jsonRequestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/chores/assignments/" + assignmentId + "/status"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 204) {
            throw new RuntimeException("Failed to update chore assignment status. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }
    }

    public ChoreAssignmentResponseDTO reassignChore(UUID assignmentId, ChoreReassignRequestDTO requestDTO) {

        String jsonRequestBody = httpRestClient.serializeDTO(requestDTO);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/chores/assignments/" + assignmentId + "/reassign"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to reassign chore. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return httpRestClient.deserializeDTO(response.body(), ChoreAssignmentResponseDTO.class);
    }

    public List<ChoreAssignmentResponseDTO> getFilteredChoreAssignments(ChoreAssignmentFilterRequestDTO requestDTO) {
        StringBuilder queryString = new StringBuilder("?");

        if (requestDTO.statuses() != null && !requestDTO.statuses().isEmpty()) {
            for (int i = 0; i < requestDTO.statuses().size(); i++) {
                if (i > 0) queryString.append("&");
                queryString.append("statuses=").append(requestDTO.statuses().get(i).name());
            }
        }

        if (requestDTO.assigneeId() != null) {
            if (queryString.length() > 1) queryString.append("&");
            queryString.append("assigneeId=").append(requestDTO.assigneeId());
        }

        if (requestDTO.descriptionContains() != null && !requestDTO.descriptionContains().isEmpty()) {
            if (queryString.length() > 1) queryString.append("&");
            queryString.append("descriptionContains=").append(URLEncoder.encode(requestDTO.descriptionContains(), StandardCharsets.UTF_8));
        }

        if (requestDTO.dateRange() != null) {
            if (requestDTO.dateRange().startDate() != null) {
                if (queryString.length() > 1) queryString.append("&");
                queryString.append("dateRange.startDate=").append(requestDTO.dateRange().startDate());
            }
            if (requestDTO.dateRange().endDate() != null) {
                if (queryString.length() > 1) queryString.append("&");
                queryString.append("dateRange.endDate=").append(requestDTO.dateRange().endDate());
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/chores/assignments" + queryString))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get filtered chore assignments. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return httpRestClient.deserializeDTOList(response.body(), ChoreAssignmentResponseDTO.class);
    }

    public List<ChoreResponseDTO> getAllHouseholdChores(UUID householdId) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/chores/" + householdId))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get household chores. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return httpRestClient.deserializeDTOList(response.body(), ChoreResponseDTO.class);
    }

    public AssignmentOverviewDTO getAssignmentOverview(UUID householdId) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/chores/assignments/" + householdId + "/overview"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get chore assignment overview. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        return httpRestClient.deserializeDTO(response.body(), AssignmentOverviewDTO.class);
    }

}








