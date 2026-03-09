package com.housemate.client.service;

import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseFilterRequestDTO;
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
     * Handles GET requests. Maps DTO fields to URL query parameters for @ModelAttribute.
     */
    public List<ExpenseResponseDTO> getFilteredExpenses(ExpenseFilterRequestDTO filterDTO) {
        // 1. Convert the DTO into URL query parameters
        String queryString = buildQueryString(filterDTO);
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
     * Helper method to safely convert the ExpenseFilterRequestDTO record into a URL Query String.
     */
    private String buildQueryString(ExpenseFilterRequestDTO filter) {
        if (filter == null) {
            return "";
        }

        List<String> queryParams = new ArrayList<>();

        // 1. Map standard UUID fields
        if (filter.householdId() != null) {
            queryParams.add("householdId=" + encodeString(filter.householdId().toString()));
        }

        if (filter.payerId() != null) {
            queryParams.add("payerId=" + encodeString(filter.payerId().toString()));
        }

        if (filter.involvedId() != null) {
            queryParams.add("involvedId=" + encodeString(filter.involvedId().toString()));
        }

        // 2. Map the nested DateRange object
        // Note: Adjust the accessors (.startDate() vs .getStartDate()) depending on how 
        // DateRange is implemented (Record vs standard Class).
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