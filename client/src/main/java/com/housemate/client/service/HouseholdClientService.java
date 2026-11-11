package com.housemate.client.service;

import com.housemate.shared.dto.household.request.AddMemberRequestDTO;
import com.housemate.shared.dto.household.request.HouseholdCreateRequestDTO;
import com.housemate.shared.dto.household.response.HouseholdInvitationCodeResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdMemberResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static com.housemate.client.config.ApiConfig.BASE_URL;

@RequiredArgsConstructor
public class HouseholdClientService {

    private final HttpRestClient httpRestClient;

    public HouseholdResponseDTO createHousehold(HouseholdCreateRequestDTO requestDTO) {

        String requestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/households"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 201) {
            return httpRestClient.deserializeDTO(response.body(), HouseholdResponseDTO.class);
        } else {
            throw new RuntimeException(
                    "Failed to create household. Server responded with status code: " + response.statusCode() +
                            " and message: " + response.body()
            );
        }
    }

    public HouseholdResponseDTO getCurrentUserHousehold() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/households/me"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), HouseholdResponseDTO.class);
        } else {
            throw new RuntimeException(
                    "Failed to retrieve current household. Server responded with status code: " + response.statusCode() +
                            " and message: " + response.body()
            );
        }
    }

    public HouseholdResponseDTO addMember(AddMemberRequestDTO requestDTO) {
        String requestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/households/members"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), HouseholdResponseDTO.class);
        } else {
            throw new RuntimeException(
                    "Failed to join household using invitation code. Server responded with status code: " + response.statusCode() +
                            " and message: " + response.body()
            );
        }
    }

    public List<HouseholdMemberResponseDTO> getHouseholdMembers() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/households/members"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                "Failed to retrieve household members. Server responded with status code: " + response.statusCode() +
                " and message: " + response.body()
            );
        }
        return httpRestClient.deserializeDTOList(response.body(), HouseholdMemberResponseDTO.class);
    }

    public HouseholdResponseDTO removeMember(UUID memberId) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/households/members/" + memberId))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .DELETE()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), HouseholdResponseDTO.class);
        } else {
            throw new RuntimeException(
                    "Failed to remove household member. Server responded with status code: " + response.statusCode() +
                            " and message: " + response.body()
            );
        }
    }

    public void leaveHousehold() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/households/me"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .DELETE()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 204) {
            throw new RuntimeException(
                    "Failed to leave household. Server responded with status code: " + response.statusCode() +
                            " and message: " + response.body()
            );
        }
    }

    public HouseholdInvitationCodeResponseDTO getInvitationCode() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/households/invitation-code"))
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), HouseholdInvitationCodeResponseDTO.class);
        } else {
            throw new RuntimeException(
                    "Failed to retrieve household invitation code. Server responded with status code: " + response.statusCode() +
                            " and message: " + response.body()
            );
        }
    }

    public HouseholdInvitationCodeResponseDTO refreshInvitationCode() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/households/invitation-code/refresh"))
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), HouseholdInvitationCodeResponseDTO.class);
        } else {
            throw new RuntimeException(
                    "Failed to refresh household invitation code. Server responded with status code: " + response.statusCode() +
                            " and message: " + response.body()
            );
        }
    }
}
