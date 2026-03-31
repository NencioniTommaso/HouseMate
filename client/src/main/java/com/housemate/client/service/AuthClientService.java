package com.housemate.client.service;

import com.housemate.shared.dto.auth.request.LoginRequestDTO;
import com.housemate.shared.dto.auth.request.RegisterRequestDTO;
import com.housemate.shared.dto.auth.response.LoginResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;

import lombok.RequiredArgsConstructor;

import static com.housemate.client.config.ApiConfig.BASE_URL;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.security.auth.login.LoginException;

@RequiredArgsConstructor
public class AuthClientService {

    private final HttpRestClient httpRestClient;

    public UserResponseDTO login(LoginRequestDTO requestDto) throws LoginException {
        String jsonRequestBody = httpRestClient.serializeDTO(requestDto);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 200) {
            throw new LoginException(
                "Failed to login user. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body()
            );
        }

        LoginResponseDTO responseDto = httpRestClient.deserializeDTO(response.body(), LoginResponseDTO.class);
        httpRestClient.context.getAuthState().setJwt(responseDto.token());
        return responseDto.user();
    }

    public UserResponseDTO register(RegisterRequestDTO requestDto) throws LoginException {
        String jsonRequestBody = httpRestClient.serializeDTO(requestDto);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 201) {
            throw new LoginException(
                "Failed to register user. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body()
            );
        }
        LoginResponseDTO responseDto = httpRestClient.deserializeDTO(response.body(), LoginResponseDTO.class);
        httpRestClient.context.getAuthState().setJwt(responseDto.token());
        return responseDto.user();
    }
}