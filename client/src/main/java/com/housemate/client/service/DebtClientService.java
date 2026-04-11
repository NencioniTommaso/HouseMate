package com.housemate.client.service;

import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtOverviewResponseDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.housemate.client.config.ApiConfig.BASE_URL;

@RequiredArgsConstructor
public class DebtClientService {

    private final HttpRestClient httpRestClient;

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
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTOList(response.body(), DebtResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve filtered debts. Status code: " + response.statusCode());
        }
    }

    /**
     * Retrieves debt overview totals for the authenticated user.
     */
    public DebtOverviewResponseDTO getCurrentUserDebtOverview() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/debts/me"))
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), DebtOverviewResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve debt overview. Status code: " + response.statusCode());
        }
    }

    /**
     * Deletes a specific debt by its ID.
     */
    public void deleteDebt(UUID debtId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/debts/" + debtId))
                .header("Authorization", httpRestClient.buildAuthHeader())
                .DELETE()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

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

        if (filter.userTransactionRole() != null) {
            queryParams.add("userTransactionRole=" + httpRestClient.encodeString(filter.userTransactionRole().toString()));
        }
        if (filter.involvedId() != null) {
            queryParams.add("involvedId=" + httpRestClient.encodeString(filter.involvedId().toString()));
        }

        return String.join("&", queryParams);
    }
}