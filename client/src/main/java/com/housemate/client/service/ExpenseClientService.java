package com.housemate.client.service;

import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static com.housemate.client.config.ApiConfig.BASE_URL;

public class ExpenseClientService extends ClientService {

    /**
     * Handles POST requests. Uses JSON Serialization for @RequestBody.
     */
    public ExpenseResponseDTO createExpense(ExpenseCreateRequestDTO requestDTO) {
        String requestBody = serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/expenses"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = sendRequest(request);

        // Controller explicitly returns 201 CREATED
        if (response.statusCode() == 201) {
            return deserializeDTO(response.body(), ExpenseResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to create expense. Status code: " + response.statusCode());
        }
    }

    /**
     * Retrieves a list of expenses filtered by transaction criteria.
     */
    public List<ExpenseResponseDTO> getFilteredExpenses(TransactionFilterRequestDTO filterDTO) {
        // 1. Convert the DTO into URL query parameters
        String queryString = buildTransactionQueryString(filterDTO);
        String uriStr = BASE_URL + "/api/expenses" + (queryString.isEmpty() ? "" : "?" + queryString);

        // 2. Build the GET request (No body allowed)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(request);

        // Controller returns 200 OK
        if (response.statusCode() == 200) {
            return deserializeDTOList(response.body(), ExpenseResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve filtered expenses. Status code: " + response.statusCode());
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