package com.housemate.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@RequiredArgsConstructor
public class HttpRestClient {

    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    // Helper methods for HTTP communication and JSON processing

    protected String serializeDTO(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize DTO to JSON", e);
        }
    }

    protected HttpResponse<String> sendRequest(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request Interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("An unexpected error happened while connecting to the server", e);
        }
    }

    protected <T> T deserializeDTO(String json, Class<T> clazz) {

        T responseDTO;
        try {
            responseDTO = objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to DTO", e);
        }

        return responseDTO;
    }

    protected <T> List<T> deserializeDTOList(String json, Class<T> clazz) {

        List<T> responseDTOList;
        try {
            responseDTOList = objectMapper.readValue(json,
                    TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to DTO List", e);
        }

        return responseDTOList;
    }

    protected String encodeString(String input) {
        try {
            return java.net.URLEncoder.encode(input, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode string: " + input, e);
        }
    }
}
