package com.housemate.client.service;

import com.housemate.shared.dto.items.request.ShoppingItemCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingItemQuantityUpdateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingItemStatusUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingItemResponseDTO;

import lombok.RequiredArgsConstructor;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static com.housemate.client.config.ApiConfig.BASE_URL;

@RequiredArgsConstructor
public class ShoppingItemClientService {

    private final HttpRestClient httpRestClient;

    public ShoppingItemResponseDTO createShoppingItem(ShoppingItemCreateRequestDTO requestDTO) {
        String requestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/shopping-items"))
                .header("Content-Type", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 201) {
            return httpRestClient.deserializeDTO(response.body(), ShoppingItemResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to create shopping item. Status code: " + response.statusCode());
        }
    }

    public void deleteShoppingItem(UUID itemId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/shopping-items/" + itemId))
                .header("Authorization", httpRestClient.buildAuthHeader())
                .DELETE()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() != 204) {
            throw new RuntimeException("Failed to delete shopping item. Status code: " + response.statusCode());
        }
    }

    public ShoppingItemResponseDTO updateItemQuantity(UUID itemId, ShoppingItemQuantityUpdateRequestDTO requestDTO) {
        String requestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/shopping-items/" + itemId + "/quantity"))
                .header("Content-Type", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), ShoppingItemResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to update shopping item quantity. Status code: " + response.statusCode());
        }
    }

    public ShoppingItemResponseDTO updateItemStatus(UUID itemId, ShoppingItemStatusUpdateRequestDTO requestDTO) {
        String requestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/shopping-items/" + itemId + "/status"))
                .header("Content-Type", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), ShoppingItemResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to update shopping item status. Status code: " + response.statusCode());
        }
    }


    public ShoppingItemResponseDTO getShoppingItemById(UUID itemId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/shopping-items/" + itemId))
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), ShoppingItemResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve shopping item. Status code: " + response.statusCode());
        }
    }

    public List<ShoppingItemResponseDTO> getShoppingItemsByHousehold(UUID householdId, Boolean isBought) {
        String uri = BASE_URL + "/shopping-items/household/" + householdId;

        if (isBought != null) {
            uri += "?isBought=" + isBought;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(uri))
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTOList(response.body(), ShoppingItemResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve shopping items for household. Status code: " + response.statusCode());
        }
    }

}
