package com.housemate.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.ExpenseOverviewResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseShareResponseDTO;
import com.housemate.shared.dto.expense.response.UserSettlementOverviewResponseDTO;
import com.housemate.shared.enums.ExpenseSplitType;
import com.housemate.shared.enums.UserTransactionRole;
import com.housemate.shared.utils.types.DateRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ExpenseClientService Unit Tests")
class ExpenseClientServiceTest {

    // ============ Injected Dependencies ============
    private ExpenseClientService expenseClientService;
    private HttpRestClient mockHttpRestClient;
    private ObjectMapper objectMapper;

    // ============ Test Data Constants ============
    private static final UUID TEST_EXPENSE_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID TEST_PAYER_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("20000000-0000-0000-0000-000000000003");
    private static final UUID TEST_USER_ID = UUID.fromString("20000000-0000-0000-0000-000000000004");
    private static final String TEST_DESCRIPTION = "Groceries";

    // ============ Test Objects ============
    private ExpenseResponseDTO testExpenseResponseDTO;
    private ExpenseOverviewResponseDTO testExpenseOverviewResponseDTO;
    private UserSettlementOverviewResponseDTO testUserSettlementOverviewResponseDTO;
    private ExpenseCreateRequestDTO testExpenseCreateRequestDTO;
    private TransactionFilterRequestDTO testFilterRequestDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockHttpRestClient = mock(HttpRestClient.class);
        expenseClientService = new ExpenseClientService(mockHttpRestClient);

        testExpenseResponseDTO = createTestExpenseResponseDTO();
        testExpenseOverviewResponseDTO = createTestExpenseOverviewResponseDTO();
        testUserSettlementOverviewResponseDTO = createTestUserSettlementOverviewResponseDTO();
        testExpenseCreateRequestDTO = createTestExpenseCreateRequestDTO();
        testFilterRequestDTO = createTestFilterRequestDTO();
    }

    // ============ Helper Methods for Test Data ============

    private ExpenseResponseDTO createTestExpenseResponseDTO() {
        return new ExpenseResponseDTO(
                TEST_EXPENSE_ID,
                TEST_DESCRIPTION,
                LocalDateTime.now(),
                BigDecimal.valueOf(100.00),
                TEST_PAYER_ID,
                "John Doe",
                ExpenseSplitType.EQUAL_SPLIT,
                TEST_HOUSEHOLD_ID,
                List.of(new ExpenseShareResponseDTO(UUID.randomUUID(), TEST_USER_ID, "Jane Doe", BigDecimal.valueOf(50.00)))
        );
    }

    private ExpenseCreateRequestDTO createTestExpenseCreateRequestDTO() {
        return new ExpenseCreateRequestDTO(
                TEST_DESCRIPTION,
                BigDecimal.valueOf(100.00),
                ExpenseSplitType.EQUAL_SPLIT,
                List.of(new ExpenseShareRequestDTO(TEST_USER_ID, BigDecimal.valueOf(50.00)))
        );
    }

    private ExpenseOverviewResponseDTO createTestExpenseOverviewResponseDTO() {
        return new ExpenseOverviewResponseDTO(
                BigDecimal.valueOf(321.45),
                5L
        );
    }

    private UserSettlementOverviewResponseDTO createTestUserSettlementOverviewResponseDTO() {
        return new UserSettlementOverviewResponseDTO(
                BigDecimal.valueOf(150.75)
        );
    }

    private TransactionFilterRequestDTO createTestFilterRequestDTO() {
        DateRange dateRange = new DateRange(LocalDateTime.now().minusDays(7), LocalDateTime.now().plusDays(1));
        return new TransactionFilterRequestDTO(
                TEST_HOUSEHOLD_ID,
            UserTransactionRole.DEBTOR,
                dateRange,
                TEST_DESCRIPTION
        );
    }

    // ============ Helper Methods for HTTP Mocking ============

    @SuppressWarnings("unchecked")
    private <T> HttpResponse<T> createMockResponse(int statusCode, T body) {
        HttpResponse<T> response = (HttpResponse<T>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    // ============ Tests for createExpense ============

    @Test
    @DisplayName("createExpense - should successfully create an expense and return ExpenseResponseDTO")
    void testCreateExpense_Success() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(testExpenseResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(201, jsonResponse);
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"description\":\"Groceries\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, ExpenseResponseDTO.class)).thenReturn(testExpenseResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        ExpenseResponseDTO result = expenseClientService.createExpense(testExpenseCreateRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_EXPENSE_ID, result.id());
        assertEquals(TEST_DESCRIPTION, result.description());

        verify(mockHttpRestClient).serializeDTO(any());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, ExpenseResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("POST", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/expenses"));
        assertEquals("application/json", capturedRequest.headers().firstValue("Content-Type").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("createExpense - should throw RuntimeException when server returns error status code")
    void testCreateExpense_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(400, "Invalid expense payload");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"description\":\"Groceries\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> expenseClientService.createExpense(testExpenseCreateRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to create expense"));
        assertTrue(exception.getMessage().contains("Status code: 400"));
    }

    // ============ Tests for getFilteredExpenses ============

    @Test
    @DisplayName("getFilteredExpenses - should successfully retrieve filtered expenses and return list of ExpenseResponseDTO")
    void testGetFilteredExpenses_Success() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(List.of(testExpenseResponseDTO));

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, ExpenseResponseDTO.class)).thenReturn(List.of(testExpenseResponseDTO));
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");
        when(mockHttpRestClient.encodeString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ExpenseResponseDTO> result = expenseClientService.getFilteredExpenses(testFilterRequestDTO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(TEST_EXPENSE_ID, result.get(0).id());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, ExpenseResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("GET", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/expenses"));
        assertNotNull(capturedRequest.uri().getQuery());
        assertTrue(capturedRequest.uri().getQuery().contains("householdId=" + TEST_HOUSEHOLD_ID));
        assertTrue(capturedRequest.uri().getQuery().contains("userTransactionRole=DEBTOR"));
        assertTrue(capturedRequest.uri().getQuery().contains("description=" + TEST_DESCRIPTION));
        assertTrue(capturedRequest.uri().getQuery().contains("dateRange.startDate="));
        assertTrue(capturedRequest.uri().getQuery().contains("dateRange.endDate="));
        assertEquals("application/json", capturedRequest.headers().firstValue("Accept").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("getFilteredExpenses - should return empty list when no expenses match filter")
    void testGetFilteredExpenses_EmptyResult() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(List.of());

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, ExpenseResponseDTO.class)).thenReturn(List.of());
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");
        when(mockHttpRestClient.encodeString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ExpenseResponseDTO> result = expenseClientService.getFilteredExpenses(testFilterRequestDTO);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, ExpenseResponseDTO.class);
    }

    @Test
    @DisplayName("getFilteredExpenses - should throw RuntimeException when server returns error status code")
    void testGetFilteredExpenses_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(400, "Invalid filter parameters");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");
        when(mockHttpRestClient.encodeString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> expenseClientService.getFilteredExpenses(testFilterRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to retrieve filtered expenses"));
        assertTrue(exception.getMessage().contains("Status code: 400"));
    }

    // ============ Tests for getCurrentMonthExpenseOverview ============

    @Test
    @DisplayName("getCurrentMonthExpenseOverview - should successfully retrieve overview")
    void testGetCurrentMonthExpenseOverview_Success() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(testExpenseOverviewResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, ExpenseOverviewResponseDTO.class))
                .thenReturn(testExpenseOverviewResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        ExpenseOverviewResponseDTO result = expenseClientService.getCurrentMonthExpenseOverview();

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(321.45), result.totalAmount());
        assertEquals(5L, result.expenseCount());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, ExpenseOverviewResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("GET", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/expenses/overview"));
        assertEquals("application/json", capturedRequest.headers().firstValue("Accept").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("getCurrentMonthExpenseOverview - should throw RuntimeException on server error")
    void testGetCurrentMonthExpenseOverview_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(500, "Server error");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> expenseClientService.getCurrentMonthExpenseOverview());

        assertTrue(exception.getMessage().contains("Failed to retrieve current month expense overview"));
        assertTrue(exception.getMessage().contains("Status code: 500"));
    }

    // ============ Tests for getCurrentMonthUserSettlementOverview ============

    @Test
    @DisplayName("getCurrentMonthUserSettlementOverview - should successfully retrieve user settlement overview")
    void testGetCurrentMonthUserSettlementOverview_Success() throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(testUserSettlementOverviewResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, UserSettlementOverviewResponseDTO.class))
                .thenReturn(testUserSettlementOverviewResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        UserSettlementOverviewResponseDTO result = expenseClientService.getCurrentMonthUserSettlementOverview();

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150.75), result.totalSettlementsMade());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, UserSettlementOverviewResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("GET", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/expenses/me"));
        assertEquals("application/json", capturedRequest.headers().firstValue("Accept").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("getCurrentMonthUserSettlementOverview - should throw RuntimeException on server error")
    void testGetCurrentMonthUserSettlementOverview_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(500, "Server error");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> expenseClientService.getCurrentMonthUserSettlementOverview());

        assertTrue(exception.getMessage().contains("Failed to retrieve current month user settlement overview"));
        assertTrue(exception.getMessage().contains("Status code: 500"));
    }
}
