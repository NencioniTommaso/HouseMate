package com.housemate.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.client.service.context.AuthState;
import com.housemate.client.service.context.ClientContext;
import com.housemate.shared.dto.auth.request.LoginRequestDTO;
import com.housemate.shared.dto.auth.request.RegisterRequestDTO;
import com.housemate.shared.dto.auth.response.LoginResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.security.auth.login.LoginException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AuthClientService Unit Tests")
class AuthClientServiceTest {

    // ============ Injected Dependencies ============
    private AuthClientService authClientService;
    private HttpRestClient mockHttpRestClient;
    private ClientContext mockClientContext;
    private AuthState mockAuthState;
    private ObjectMapper objectMapper;

    // ============ Test Data Constants ============
    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String TEST_NAME = "Mario";
    private static final String TEST_SURNAME = "Rossi";
    private static final String TEST_EMAIL = "mario@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_IBAN = "IT60X0542811101000000123456";
    private static final String TEST_TOKEN = "jwt-token";

    // ============ Test Objects ============
    private LoginRequestDTO testLoginRequestDTO;
    private RegisterRequestDTO testRegisterRequestDTO;
    private UserResponseDTO testUserResponseDTO;
    private LoginResponseDTO testLoginResponseDTO;

    @BeforeEach
    void setUp() {
        mockClientContext = mock(ClientContext.class);
        mockAuthState = mock(AuthState.class);

        HttpRestClient realHttpRestClient = new HttpRestClient(
            mock(HttpClient.class),
            mockClientContext,
            new ObjectMapper()
        );
        mockHttpRestClient = spy(realHttpRestClient);

        when(mockClientContext.getAuthState()).thenReturn(mockAuthState);

        authClientService = new AuthClientService(mockHttpRestClient);
        objectMapper = new ObjectMapper();

        testLoginRequestDTO = new LoginRequestDTO(TEST_EMAIL, TEST_PASSWORD);
        testRegisterRequestDTO = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, TEST_EMAIL, TEST_PASSWORD, TEST_IBAN);
        testUserResponseDTO = new UserResponseDTO(
            TEST_USER_ID,
            TEST_NAME,
            TEST_SURNAME,
            TEST_EMAIL,
            TEST_IBAN,
            null
        );
        testLoginResponseDTO = new LoginResponseDTO(testUserResponseDTO, TEST_TOKEN);
    }

    // ============ Helper Methods for HTTP Mocking ============

    @SuppressWarnings("unchecked")
    private <T> HttpResponse<T> createMockResponse(int statusCode, T body) {
        HttpResponse<T> response = (HttpResponse<T>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    // ============ Tests for login ============

    @Test
    @DisplayName("login - should successfully login and return UserResponseDTO")
    void testLogin_Success() throws IOException, LoginException {

        String jsonResponse = objectMapper.writeValueAsString(testLoginResponseDTO);
        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);

        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"email\":\"mario@example.com\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, LoginResponseDTO.class)).thenReturn(testLoginResponseDTO);

        UserResponseDTO result = authClientService.login(testLoginRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.id());
        assertEquals(TEST_EMAIL, result.email());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).serializeDTO(any());
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, LoginResponseDTO.class);
        verify(mockClientContext).getAuthState();
        verify(mockAuthState).setJwt(TEST_TOKEN);

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("POST", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/login"));
        assertEquals("application/json", capturedRequest.headers().firstValue("Content-Type").orElse(null));
        assertEquals("application/json", capturedRequest.headers().firstValue("Accept").orElse(null));
    }

    @Test
    @DisplayName("login - should throw LoginException when server returns error status code")
    void testLogin_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(401, "Invalid credentials");

        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"email\":\"mario@example.com\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);

        LoginException exception = assertThrows(LoginException.class,
            () -> authClientService.login(testLoginRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to login user"));
        assertTrue(exception.getMessage().contains("status code: 401"));
        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    // ============ Tests for register ============

    @Test
    @DisplayName("register - should successfully register and return UserResponseDTO")
    void testRegister_Success() throws LoginException, IOException {

        String jsonResponse = objectMapper.writeValueAsString(testLoginResponseDTO);
        HttpResponse<String> mockResponse = createMockResponse(201, jsonResponse);

        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"email\":\"mario@example.com\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, LoginResponseDTO.class)).thenReturn(testLoginResponseDTO);

        UserResponseDTO result = authClientService.register(testRegisterRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.id());
        assertEquals(TEST_EMAIL, result.email());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).serializeDTO(any());
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, LoginResponseDTO.class);
        verify(mockClientContext).getAuthState();
        verify(mockAuthState).setJwt(TEST_TOKEN);

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("POST", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/register"));
        assertEquals("application/json", capturedRequest.headers().firstValue("Content-Type").orElse(null));
        assertEquals("application/json", capturedRequest.headers().firstValue("Accept").orElse(null));
    }

    @Test
    @DisplayName("register - should throw LoginException when server returns error status code")
    void testRegister_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(400, "Email already registered");

        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"email\":\"mario@example.com\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);

        LoginException exception = assertThrows(LoginException.class,
            () -> authClientService.register(testRegisterRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to register user"));
        assertTrue(exception.getMessage().contains("status code: 400"));
        assertTrue(exception.getMessage().contains("Email already registered"));
    }
}
