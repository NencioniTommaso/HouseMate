package com.housemate.client.service;

import com.housemate.shared.dto.user.request.UserUpdateRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.housemate.client.config.ApiConfig.BASE_URL;

@RequiredArgsConstructor
public class UserClientService {

    private final HttpRestClient httpRestClient;

    public UserResponseDTO getCurrentUser() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/me"))
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), UserResponseDTO.class);
        }

        throw new RuntimeException(
                "Failed to retrieve current user. Server responded with status code: " + response.statusCode() +
                        " and message: " + response.body()
        );
    }

    public UserResponseDTO updateCurrentUser(UserUpdateRequestDTO requestDTO) {
        String requestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/me"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
            .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), UserResponseDTO.class);
        }

        throw new RuntimeException(
                "Failed to update current user. Server responded with status code: " + response.statusCode() +
                        " and message: " + response.body()
        );
    }
}
