package com.housemate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.backend.service.AuthService;
import com.housemate.shared.dto.auth.request.LoginRequestDTO;
import com.housemate.shared.dto.auth.request.RegisterRequestDTO;
import com.housemate.shared.dto.auth.response.LoginResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Integration Tests")
@SuppressWarnings("null")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String TEST_NAME = "Mario";
    private static final String TEST_SURNAME = "Rossi";
    private static final String TEST_EMAIL = "mario@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_IBAN = "IT60X0542811101000000123456";
    private static final String TEST_TOKEN = "jwt-token";

    private LoginRequestDTO testLoginRequestDTO;
    private RegisterRequestDTO testRegisterRequestDTO;
    private LoginResponseDTO testLoginResponseDTO;

    @BeforeEach
    void setUp() {
        testLoginRequestDTO = new LoginRequestDTO(TEST_EMAIL, TEST_PASSWORD);
        testRegisterRequestDTO = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, TEST_EMAIL, TEST_PASSWORD, TEST_IBAN);

        UserResponseDTO userResponseDTO = new UserResponseDTO(
            TEST_USER_ID,
            TEST_NAME,
            TEST_SURNAME,
            TEST_EMAIL,
            TEST_IBAN,
            null
        );
        testLoginResponseDTO = new LoginResponseDTO(userResponseDTO, TEST_TOKEN);
    }


    // ============ Login Tests ============

    @Test
    @DisplayName("POST /api/auth/login - should return 200 OK with LoginResponseDTO on valid input")
    void login_validInput_returnsOkWithLoginResponseDTO() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(testLoginResponseDTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testLoginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TEST_TOKEN))
                .andExpect(jsonPath("$.user.id").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.user.email").value(TEST_EMAIL));

        verify(authService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 400 Bad Request on invalid request body")
    void login_invalidInput_returnsBadRequest() throws Exception {
        LoginRequestDTO invalidRequestDTO = new LoginRequestDTO("", TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 401 Unauthorized on bad credentials")
    void login_badCredentials_returnsUnauthorized() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testLoginRequestDTO)))
                .andExpect(status().isUnauthorized());

        verify(authService).login(any(LoginRequestDTO.class));
    }


    // ============ Register Tests ============

    @Test
    @DisplayName("POST /api/auth/register - should return 201 Created with LoginResponseDTO on valid input")
    void register_validInput_returnsCreatedWithLoginResponseDTO() throws Exception {
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(testLoginResponseDTO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testRegisterRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(TEST_TOKEN))
                .andExpect(jsonPath("$.user.id").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.user.email").value(TEST_EMAIL));

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 Bad Request on invalid request body")
    void register_invalidInput_returnsBadRequest() throws Exception {
        RegisterRequestDTO invalidRequestDTO = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, "", TEST_PASSWORD, TEST_IBAN);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 Bad Request when email is already registered")
    void register_emailAlreadyRegistered_returnsBadRequest() throws Exception {
        when(authService.register(any(RegisterRequestDTO.class))).thenThrow(new IllegalArgumentException("Email already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testRegisterRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(authService).register(any(RegisterRequestDTO.class));
    }


    // ============ Invalid HTTP Method Tests ============

    @Test
    @DisplayName("GET /api/auth/login - should return 405 Method Not Allowed")
    void login_invalidHttpMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }
}
