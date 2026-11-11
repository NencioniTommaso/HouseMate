package com.housemate.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;
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

@DisplayName("SettlementClientService Unit Tests")
class SettlementClientServiceTest {

    // ============ Injected Dependencies ============
    private SettlementClientService settlementClientService;
    private HttpRestClient mockHttpRestClient;
    private ObjectMapper objectMapper;

    // ============ Test Data Constants ============
    private static final UUID TEST_DEBT_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID TEST_SETTLEMENT_ID = UUID.fromString("30000000-0000-0000-0000-000000000002");
    private static final UUID TEST_CREDITOR_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID TEST_INVOLVED_ID = UUID.fromString("30000000-0000-0000-0000-000000000004");
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("30000000-0000-0000-0000-000000000005");

    // ============ Test Objects ============
    private SettlementResponseDTO testSettlementResponseDTO;
    private SettlementCreateRequestDTO testSettlementCreateRequestDTO;
    private TransactionFilterRequestDTO testFilterRequestDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockHttpRestClient = mock(HttpRestClient.class);
        settlementClientService = new SettlementClientService(mockHttpRestClient);

        testSettlementResponseDTO = createTestSettlementResponseDTO();
        testSettlementCreateRequestDTO = createTestSettlementCreateRequestDTO();
        testFilterRequestDTO = createTestFilterRequestDTO();
    }

    // ============ Helper Methods for Test Data ============

    private SettlementResponseDTO createTestSettlementResponseDTO() {
        return new SettlementResponseDTO(
                TEST_SETTLEMENT_ID,
                UserTransactionRole.CREDITOR,
                TEST_INVOLVED_ID,
                "Alex Smith",
                BigDecimal.valueOf(30.00),
                LocalDateTime.now(),
                "Partial settlement",
                TEST_HOUSEHOLD_ID
        );
    }

    private SettlementCreateRequestDTO createTestSettlementCreateRequestDTO() {
        return new SettlementCreateRequestDTO(
                TEST_DEBT_ID,
                TEST_CREDITOR_ID,
                BigDecimal.valueOf(30.00),
                "Partial settlement"
        );
    }

    private TransactionFilterRequestDTO createTestFilterRequestDTO() {
        DateRange dateRange = new DateRange(LocalDateTime.now().minusDays(30), LocalDateTime.now().plusDays(1));
        return new TransactionFilterRequestDTO(
                TEST_HOUSEHOLD_ID,
                UserTransactionRole.CREDITOR,
                dateRange,
                "settlement"
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

    // ============ Tests for settleDebt ============

    @Test
    @DisplayName("settleDebt - should successfully settle debt and return SettlementResponseDTO")
    void testSettleDebt_Success() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(testSettlementResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(201, jsonResponse);
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"amount\":30.00}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, SettlementResponseDTO.class)).thenReturn(testSettlementResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        SettlementResponseDTO result = settlementClientService.settleDebt(TEST_DEBT_ID, testSettlementCreateRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_SETTLEMENT_ID, result.settlementId());
        assertEquals(UserTransactionRole.CREDITOR, result.userTransactionRole());

        verify(mockHttpRestClient).serializeDTO(any());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, SettlementResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("POST", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/settlements/" + TEST_DEBT_ID));
        assertEquals("application/json", capturedRequest.headers().firstValue("Content-Type").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("settleDebt - should throw RuntimeException when server returns error status code")
    void testSettleDebt_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(400, "Invalid settlement payload");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"amount\":30.00}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> settlementClientService.settleDebt(TEST_DEBT_ID, testSettlementCreateRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to settle debt"));
        assertTrue(exception.getMessage().contains("Status code: 400"));
    }

    // ============ Tests for getFilteredSettlements ============

    @Test
    @DisplayName("getFilteredSettlements - should successfully retrieve filtered settlements and return list of SettlementResponseDTO")
    void testGetFilteredSettlements_Success() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(List.of(testSettlementResponseDTO));

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, SettlementResponseDTO.class)).thenReturn(List.of(testSettlementResponseDTO));
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");
        when(mockHttpRestClient.encodeString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SettlementResponseDTO> result = settlementClientService.getFilteredSettlements(testFilterRequestDTO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(TEST_SETTLEMENT_ID, result.get(0).settlementId());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, SettlementResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("GET", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/settlements"));
        assertNotNull(capturedRequest.uri().getQuery());
        assertTrue(capturedRequest.uri().getQuery().contains("householdId=" + TEST_HOUSEHOLD_ID));
        assertTrue(capturedRequest.uri().getQuery().contains("userTransactionRole=CREDITOR"));
        assertTrue(capturedRequest.uri().getQuery().contains("description=settlement"));
        assertTrue(capturedRequest.uri().getQuery().contains("dateRange.startDate="));
        assertTrue(capturedRequest.uri().getQuery().contains("dateRange.endDate="));
        assertEquals("application/json", capturedRequest.headers().firstValue("Accept").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("getFilteredSettlements - should return empty list when no settlements match filter")
    void testGetFilteredSettlements_EmptyResult() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(List.of());

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, SettlementResponseDTO.class)).thenReturn(List.of());
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");
        when(mockHttpRestClient.encodeString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SettlementResponseDTO> result = settlementClientService.getFilteredSettlements(testFilterRequestDTO);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, SettlementResponseDTO.class);
    }

    @Test
    @DisplayName("getFilteredSettlements - should throw RuntimeException when server returns error status code")
    void testGetFilteredSettlements_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(500, "Unexpected server error");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");
        when(mockHttpRestClient.encodeString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> settlementClientService.getFilteredSettlements(testFilterRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to retrieve filtered settlements"));
        assertTrue(exception.getMessage().contains("Status code: 500"));
    }
}
