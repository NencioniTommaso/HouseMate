package com.housemate.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.shared.dto.user.request.UserUpdateRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UserClientService Unit Tests")
class UserClientServiceTest {

    // ============ Injected Dependencies ============
    private UserClientService userClientService;
    private HttpRestClient mockHttpRestClient;
    private ObjectMapper objectMapper;

    // ============ Test Data Constants ============
    private static final UUID TEST_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000031");
    private static final String TEST_NAME = "Mario";
    private static final String TEST_SURNAME = "Rossi";
    private static final String TEST_EMAIL = "mario.rossi@example.com";
    private static final String TEST_IBAN = "IT60X0542811101000000123456";
    private static final String TEST_PAYMENT_LINK = "https://payments.example.com/mario";

    // ============ Test Objects ============
    private UserResponseDTO testUserResponseDTO;
    private UserUpdateRequestDTO testUpdateRequestDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockHttpRestClient = mock(HttpRestClient.class);
        userClientService = new UserClientService(mockHttpRestClient);

        testUserResponseDTO = new UserResponseDTO(
            TEST_USER_ID,
            TEST_NAME,
            TEST_SURNAME,
            TEST_EMAIL,
            TEST_IBAN,
            TEST_PAYMENT_LINK
        );

        testUpdateRequestDTO = new UserUpdateRequestDTO(
            "Giulia",
            "Bianchi",
            "giulia.bianchi@example.com",
            "IT02A0306909606100000123456",
            "https://payments.example.com/giulia"
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

    // ============ Tests for getCurrentUser ============

    @Test
    @DisplayName("getCurrentUser - should successfully retrieve current user")
    void testGetCurrentUser_Success() throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(testUserResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, UserResponseDTO.class)).thenReturn(testUserResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        UserResponseDTO result = userClientService.getCurrentUser();

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.id());
        assertEquals(TEST_EMAIL, result.email());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, UserResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("GET", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/users/me"));
        assertEquals("application/json", capturedRequest.headers().firstValue("Accept").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("getCurrentUser - should throw RuntimeException when server returns error status code")
    void testGetCurrentUser_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(401, "Unauthorized");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userClientService.getCurrentUser());

        assertTrue(exception.getMessage().contains("Failed to retrieve current user"));
        assertTrue(exception.getMessage().contains("status code: 401"));
    }

    // ============ Tests for updateCurrentUser ============

    @Test
    @DisplayName("updateCurrentUser - should successfully update current user")
    void testUpdateCurrentUser_Success() throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(testUserResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"name\":\"Giulia\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, UserResponseDTO.class)).thenReturn(testUserResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        UserResponseDTO result = userClientService.updateCurrentUser(testUpdateRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.id());

        verify(mockHttpRestClient).serializeDTO(any());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, UserResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("PATCH", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/users/me"));
        assertEquals("application/json", capturedRequest.headers().firstValue("Content-Type").orElse(null));
        assertEquals("application/json", capturedRequest.headers().firstValue("Accept").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("updateCurrentUser - should throw RuntimeException when server returns error status code")
    void testUpdateCurrentUser_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(400, "Validation failed");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"name\":\"Giulia\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userClientService.updateCurrentUser(testUpdateRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to update current user"));
        assertTrue(exception.getMessage().contains("status code: 400"));
    }
}