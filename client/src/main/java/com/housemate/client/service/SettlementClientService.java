package com.housemate.client.service;

import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.request.SettlementFilterRequestDTO;
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
    public List<SettlementResponseDTO> getFilteredSettlements(SettlementFilterRequestDTO filterDTO) {
        String queryString = buildQueryString(filterDTO);
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
     * Helper method to safely convert the SettlementFilterRequestDTO record into a URL Query String.
     */
    private String buildQueryString(SettlementFilterRequestDTO filter) {
        if (filter == null) {
            return "";
        }

        List<String> queryParams = new ArrayList<>();

        if (filter.debtId() != null) {
            queryParams.add("debtId=" + encodeString(filter.debtId().toString()));
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

        // Handle the nested DateRange record using standard Spring notation
        if (filter.dateRange() != null) {
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