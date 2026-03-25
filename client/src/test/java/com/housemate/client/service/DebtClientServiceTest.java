package com.housemate.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.enums.UserTransactionRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("DebtClientService Unit Tests")
class DebtClientServiceTest {

    // ============ Injected Dependencies ============
    private DebtClientService debtClientService;
    private HttpRestClient mockHttpRestClient;
    private ObjectMapper objectMapper;

    // ============ Test Data Constants ============
    private static final UUID TEST_DEBT_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID TEST_INVOLVED_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");

    // ============ Test Objects ============
    private DebtResponseDTO testDebtResponseDTO;
    private DebtFilterRequestDTO testFilterRequestDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockHttpRestClient = mock(HttpRestClient.class);
        debtClientService = new DebtClientService(mockHttpRestClient);

        testDebtResponseDTO = createTestDebtResponseDTO();
        testFilterRequestDTO = createTestFilterRequestDTO();
    }

    // ============ Helper Methods for Test Data ============

    private DebtResponseDTO createTestDebtResponseDTO() {
        return new DebtResponseDTO(
                TEST_DEBT_ID,
            UserTransactionRole.DEBTOR,
                TEST_INVOLVED_ID,
                "Jane Doe",
                java.math.BigDecimal.valueOf(42.50)
        );
    }

    private DebtFilterRequestDTO createTestFilterRequestDTO() {
        return new DebtFilterRequestDTO(UserTransactionRole.DEBTOR, TEST_INVOLVED_ID);
    }

    // ============ Helper Methods for HTTP Mocking ============

    @SuppressWarnings("unchecked")
    private <T> HttpResponse<T> createMockResponse(int statusCode, T body) {
        HttpResponse<T> response = (HttpResponse<T>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    // ============ Tests for getFilteredDebts ============

    @Test
    @DisplayName("getFilteredDebts - should successfully retrieve filtered debts and return list of DebtResponseDTO")
    void testGetFilteredDebts_Success() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(List.of(testDebtResponseDTO));

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, DebtResponseDTO.class)).thenReturn(List.of(testDebtResponseDTO));
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");
        when(mockHttpRestClient.encodeString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        List<DebtResponseDTO> result = debtClientService.getFilteredDebts(testFilterRequestDTO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(TEST_DEBT_ID, result.get(0).debtId());
        assertEquals(UserTransactionRole.DEBTOR, result.get(0).userTransactionRole());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, DebtResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("GET", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/debts"));
        assertNotNull(capturedRequest.uri().getQuery());
        assertTrue(capturedRequest.uri().getQuery().contains("userTransactionRole=DEBTOR"));
        assertTrue(capturedRequest.uri().getQuery().contains("involvedId=" + TEST_INVOLVED_ID));
        assertEquals("application/json", capturedRequest.headers().firstValue("Accept").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("getFilteredDebts - should return empty list when no debts match filter")
    void testGetFilteredDebts_EmptyResult() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(List.of());

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, DebtResponseDTO.class)).thenReturn(List.of());
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");
        when(mockHttpRestClient.encodeString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        List<DebtResponseDTO> result = debtClientService.getFilteredDebts(testFilterRequestDTO);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, DebtResponseDTO.class);
    }

    @Test
    @DisplayName("getFilteredDebts - should throw RuntimeException when server returns error status code")
    void testGetFilteredDebts_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(400, "Invalid filter parameters");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");
        when(mockHttpRestClient.encodeString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> debtClientService.getFilteredDebts(testFilterRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to retrieve filtered debts"));
        assertTrue(exception.getMessage().contains("Status code: 400"));
    }

    // ============ Tests for deleteDebt ============

    @Test
    @DisplayName("deleteDebt - should successfully delete a debt with status 204")
    void testDeleteDebt_Success() {

        HttpResponse<String> mockResponse = createMockResponse(204, "");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        assertDoesNotThrow(() -> debtClientService.deleteDebt(TEST_DEBT_ID));

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("DELETE", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/debts/" + TEST_DEBT_ID));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("deleteDebt - should throw RuntimeException when server returns error status code")
    void testDeleteDebt_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(404, "Debt not found");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> debtClientService.deleteDebt(TEST_DEBT_ID));

        assertTrue(exception.getMessage().contains("Failed to delete debt"));
        assertTrue(exception.getMessage().contains("Status code: 404"));
    }
}
