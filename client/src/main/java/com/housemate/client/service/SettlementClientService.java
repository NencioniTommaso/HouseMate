package com.housemate.client.service;

import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.housemate.client.config.ApiConfig.BASE_URL;

public class SettlementClientService extends ClientService {

    /**
     * Submits a settlement to the backend. Uses JSON Serialization for the request body.
     */
    public SettlementResponseDTO settleDebt(UUID debtId, SettlementCreateRequestDTO requestDTO) {
        String requestBody = serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/settlements/" + debtId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = sendRequest(request);

        // Controller returns 201 Created upon successful settlement
        if (response.statusCode() == 201) {
            return deserializeDTO(response.body(), SettlementResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to settle debt. Status code: " + response.statusCode());
        }
    }

    /**
     * Retrieves a list of settlements based on the provided filter criteria.
     */
    public List<SettlementResponseDTO> getFilteredSettlements(TransactionFilterRequestDTO filterDTO) {
        String queryString = buildTransactionQueryString(filterDTO);
        String uriStr = BASE_URL + "/settlements" + (queryString.isEmpty() ? "" : "?" + queryString);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(request);

        if (response.statusCode() == 200) {
            return deserializeDTOList(response.body(), SettlementResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve filtered settlements. Status code: " + response.statusCode());
        }
    }

    /**
     * Helper method to convert TransactionFilterRequestDTO into URL query string.
     */
    private String buildTransactionQueryString(TransactionFilterRequestDTO filter) {
        if (filter == null) {
            return "";
        }

        List<String> queryParams = new ArrayList<>();

        // 1. Map standard UUID fields
        if (filter.householdId() != null) {
            queryParams.add("householdId=" + encodeString(filter.householdId().toString()));
        }

        if (filter.userTransactionRole() != null) {
            queryParams.add("userTransactionRole=" + encodeString(filter.userTransactionRole().toString()));
        }

        // 2. Map description field
        if (filter.description() != null && !filter.description().isBlank()) {
            queryParams.add("description=" + encodeString(filter.description()));
        }

        // 3. Map the nested DateRange object
        if (filter.dateRange() != null) {
            // Using dot-notation mapping which is standard for Spring's @ModelAttribute
            if (filter.dateRange().startDate() != null) {
                queryParams.add("dateRange.startDate=" + encodeString(filter.dateRange().startDate().toString()));
            }
            
            if (filter.dateRange().endDate() != null) {
                queryParams.add("dateRange.endDate=" + encodeString(filter.dateRange().endDate().toString()));
            }
        }

        return String.join("&", queryParams);
    }
}