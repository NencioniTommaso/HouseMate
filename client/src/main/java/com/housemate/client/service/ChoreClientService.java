package com.housemate.client.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.shared.dto.chore.request.ChoreAssignmentCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.housemate.client.config.ApiConfig.BASE_URL;

public class ChoreClientService {

    private final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChoreResponseDTO createChore(ChoreRequestDTO requestDTO) {

        String jsonRequestBody;

        try{
            jsonRequestBody = objectMapper.writeValueAsString(requestDTO);
        }catch(JsonProcessingException e){
            throw new RuntimeException("Failed to serialize chore request DTO to JSON", e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/chores"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();


        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request Interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("An unexpected error happened while connecting to the server", e);
        }

        if(response.statusCode() != 200) {
            throw new RuntimeException("Failed to create chore. Server responded with status code: " + response.statusCode() +
                    " and message: " + response.body());
        }

        ChoreResponseDTO responseDTO;
        try{
            responseDTO =  objectMapper.readValue(response.body(), ChoreResponseDTO.class);
        }catch (JsonProcessingException e){
            throw new RuntimeException("Failed to deserialize response body to ChoreResponseDTO", e);
        }

        return responseDTO;

    }

    public ChoreAssignmentResponseDTO createAssignment(ChoreAssignmentCreateRequestDTO requestDTO){

        String jsonRequestBody;

            try{
                jsonRequestBody = objectMapper.writeValueAsString(requestDTO);
            }catch(JsonProcessingException e){
                throw new RuntimeException("Failed to serialize chore assignment request DTO to JSON", e);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(BASE_URL + "/api/chores/assignments"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                    .build();

            HttpResponse<String> response;

            try  {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Request Interrupted", e);
            } catch (IOException e) {
                throw new RuntimeException("An unexpected error happened while connecting to the server", e);
            }

            if(response.statusCode() != 200) {
                throw new RuntimeException("Failed to create chore assignment. Server responded with status code: " + response.statusCode() +
                        " and message: " + response.body());
            }

            ChoreAssignmentResponseDTO responseDTO;

            try {
                responseDTO = objectMapper.readValue(response.body(), ChoreAssignmentResponseDTO.class);
            }catch (JsonProcessingException e){
                throw new RuntimeException("Failed to deserialize response body to ChoreAssignmentResponseDTO", e);
            }

            return responseDTO;
    }

}
