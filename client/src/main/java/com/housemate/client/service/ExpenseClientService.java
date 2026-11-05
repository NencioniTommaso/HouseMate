package com.housemate.client.service;

import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.ExpenseOverviewResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.dto.expense.response.UserNetOverviewResponseDTO;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static com.housemate.client.config.ApiConfig.BASE_URL;

@RequiredArgsConstructor
public class ExpenseClientService {

    private final HttpRestClient httpRestClient;

    /**
     * Handles POST requests. Uses JSON Serialization for @RequestBody.
     */
    public ExpenseResponseDTO createExpense(ExpenseCreateRequestDTO requestDTO) {
        String requestBody = httpRestClient.serializeDTO(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/expenses"))
                .header("Content-Type", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        // Controller explicitly returns 201 CREATED
        if (response.statusCode() == 201) {
            return httpRestClient.deserializeDTO(response.body(), ExpenseResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to create expense. Status code: " + response.statusCode()
                + " and message: " + response.body());
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
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        // Controller returns 200 OK
        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTOList(response.body(), ExpenseResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve filtered expenses. Status code: " + response.statusCode()
                + " and message: " + response.body());
        }
    }

    /**
     * Retrieves current-month expense overview (sum of expenses and expense count)
     * for the authenticated user's current household.
     */
    public ExpenseOverviewResponseDTO getCurrentMonthExpenseOverview() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/expenses/overview"))
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), ExpenseOverviewResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve current month expense overview. Status code: "
                    + response.statusCode() + " and message: " + response.body());
        }
    }

    /**
     * Retrieves the current-month net cash flow for the authenticated user.
     */
    public UserNetOverviewResponseDTO getCurrentMonthUserNetOverview() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/expenses/me"))
                .header("Accept", "application/json")
                .header("Authorization", httpRestClient.buildAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpRestClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return httpRestClient.deserializeDTO(response.body(), UserNetOverviewResponseDTO.class);
        } else {
            throw new RuntimeException("Failed to retrieve current month user net overview. Status code: "
                    + response.statusCode() + " and message: " + response.body());
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
            queryParams.add("householdId=" + httpRestClient.encodeString(filter.householdId().toString()));
        }

        if (filter.userTransactionRole() != null) {
            queryParams.add("userTransactionRole=" + httpRestClient.encodeString(filter.userTransactionRole().toString()));
        }

        // 2. Map description field
        if (filter.description() != null && !filter.description().isBlank()) {
            queryParams.add("description=" + httpRestClient.encodeString(filter.description()));
        }

        // 3. Map the nested DateRange object
        if (filter.dateRange() != null) {
            // Using dot-notation mapping which is standard for Spring's @ModelAttribute
            if (filter.dateRange().startDate() != null) {
                queryParams.add("dateRange.startDate=" + httpRestClient.encodeString(filter.dateRange().startDate().toString()));
            }
            
            if (filter.dateRange().endDate() != null) {
                queryParams.add("dateRange.endDate=" + httpRestClient.encodeString(filter.dateRange().endDate().toString()));
            }
        }

        return String.join("&", queryParams);
    }
}