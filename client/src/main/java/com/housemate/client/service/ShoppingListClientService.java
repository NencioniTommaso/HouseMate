package com.housemate.client.service;

import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;

import lombok.RequiredArgsConstructor;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static com.housemate.client.config.ApiConfig.BASE_URL;

@RequiredArgsConstructor
public class ShoppingListClientService {

    private final HttpRestClient httpRestClient;

    public ShoppingListResponseDTO createShoppingList(ShoppingListCreateRequestDTO requestDTO) {

        String requestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/shopping-lists"))
                .header("Content-Type", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 201) {
            return httpRestClient.deserializeDTO(response.body(), ShoppingListResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to create shopping list. Status code: " + response.statusCode());
        }
    }

    public void deleteShoppingList(UUID listId) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/shopping-items/" + listId))
                .header("Content-Type", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .DELETE()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 204) {
            throw new RuntimeException("Failed to delete shopping list. Status code: " + response.statusCode());
        }
    }

    public ShoppingListResponseDTO updateListInformation(UUID listID, ShoppingListUpdateRequestDTO requestDTO) {

        String requestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/shopping-items/" +  listID))
                .header("Content-Type", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), ShoppingListResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to update shopping list status. Status code: " + response.statusCode());
        }
    }

    public List<ShoppingListResponseDTO> getShoppingItemsByHousehold(UUID householdId) {


        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/shopping-lists/" + householdId))
                .header("Content-Type", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTOList(response.body(), ShoppingListResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve shopping lists for household. Status code: " + response.statusCode());
        }
    }

}
