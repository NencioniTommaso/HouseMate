package com.housemate.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import static com.housemate.client.config.ApiConfig.BASE_URL;

/*
* A unified ClientService which exposes methods for all client operations, performing the corresponding HTTP requests.
*/

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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
}






