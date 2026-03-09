package com.housemate.client.service;

import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.housemate.client.config.ApiConfig.BASE_URL;

public class DebtClientService extends ClientService {

    /**
     * Retrieves a list of debts based on the provided filter criteria.
     * Maps the DTO to URL query parameters to satisfy the backend's @ModelAttribute.
     */
    public List<DebtResponseDTO> getFilteredDebts(DebtFilterRequestDTO filterDTO) {
        String queryString = buildQueryString(filterDTO);
        String uriStr = BASE_URL + "/debts" + (queryString.isEmpty() ? "" : "?" + queryString);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(request);

        if (response.statusCode() == 200) {
            return deserializeDTOList(response.body(), DebtResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve filtered debts. Status code: " + response.statusCode());
        }
    }

    /**
     * Deletes a specific debt by its ID.
     */
    public void deleteDebt(UUID debtId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/debts/" + debtId))
                .DELETE()
                .build();

        HttpResponse<String> response = sendRequest(request);

        // Controller returns 204 No Content upon successful deletion
        if (response.statusCode() != 204) {
            throw new RuntimeException("Failed to delete debt. Status code: " + response.statusCode());
        }
    }

    /**
     * Helper method to safely convert the DebtFilterRequestDTO record into a URL Query String.
     */
    private String buildQueryString(DebtFilterRequestDTO filter) {
        if (filter == null) {
            return "";
        }

        List<String> queryParams = new ArrayList<>();

        if (filter.householdId() != null) {
            queryParams.add("householdId=" + encodeString(filter.householdId().toString()));
        }
        if (filter.debtorId() != null) {
            queryParams.add("debtorId=" + encodeString(filter.debtorId().toString()));
        }
        if (filter.creditorId() != null) {
            queryParams.add("creditorId=" + encodeString(filter.creditorId().toString()));
        }
        if (filter.involvedId() != null) {
            queryParams.add("involvedId=" + encodeString(filter.involvedId().toString()));
        }

        return String.join("&", queryParams);
    }
}