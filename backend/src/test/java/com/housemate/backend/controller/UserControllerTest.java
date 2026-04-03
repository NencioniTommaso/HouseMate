package com.housemate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.backend.service.UserService;
import com.housemate.shared.dto.user.request.UserUpdateRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DisplayName("UserController Integration Tests")
@WithMockUser(username = "10000000-0000-0000-0000-000000000031")
@SuppressWarnings("null")
class UserControllerTest {

    private static final String BASE_URL = "/api/users";
    private static final UUID TEST_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000031");
    private static final String TEST_NAME = "Mario";
    private static final String TEST_SURNAME = "Rossi";
    private static final String TEST_EMAIL = "mario.rossi@example.com";
    private static final String TEST_IBAN = "IT60X0542811101000000123456";
    private static final String TEST_PAYMENT_LINK = "https://payments.example.com/mario";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponseDTO testUserResponseDTO;
    private UserUpdateRequestDTO testUpdateRequestDTO;

    @BeforeEach
    void setUp() {
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

    // ============ Tests for GET /api/users/me ============

    @Test
    @DisplayName("GET /api/users/me - should return 200 OK with UserResponseDTO")
    void testGetCurrentUser_Success() throws Exception {
        when(userService.getCurrentUser(TEST_USER_ID)).thenReturn(testUserResponseDTO);

        mockMvc.perform(get(BASE_URL + "/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_USER_ID.toString()))
            .andExpect(jsonPath("$.name").value(TEST_NAME))
            .andExpect(jsonPath("$.email").value(TEST_EMAIL))
            .andExpect(jsonPath("$.paymentLink").value(TEST_PAYMENT_LINK));

        verify(userService).getCurrentUser(TEST_USER_ID);
    }

    @Test
    @DisplayName("GET /api/users/me - should return 400 Bad Request when service throws IllegalArgumentException")
    void testGetCurrentUser_IllegalArgument() throws Exception {
        when(userService.getCurrentUser(TEST_USER_ID)).thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get(BASE_URL + "/me"))
            .andExpect(status().isBadRequest());

        verify(userService).getCurrentUser(TEST_USER_ID);
    }

    @Test
    @DisplayName("GET /api/users/me - should return 500 Internal Server Error when service throws IllegalStateException")
    void testGetCurrentUser_IllegalState() throws Exception {
        when(userService.getCurrentUser(TEST_USER_ID)).thenThrow(new IllegalStateException("Unexpected state"));

        mockMvc.perform(get(BASE_URL + "/me"))
            .andExpect(status().isInternalServerError());

        verify(userService).getCurrentUser(TEST_USER_ID);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/users/me - should return 401 Unauthorized for unauthenticated user")
    void testGetCurrentUser_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/me"))
            .andExpect(status().isUnauthorized());

        verify(userService, never()).getCurrentUser(any(UUID.class));
    }

    // ============ Tests for PATCH /api/users/me ============

    @Test
    @DisplayName("PATCH /api/users/me - should return 200 OK with updated UserResponseDTO")
    void testUpdateCurrentUser_Success() throws Exception {
        UserResponseDTO updatedResponse = new UserResponseDTO(
            TEST_USER_ID,
            "Giulia",
            "Bianchi",
            "giulia.bianchi@example.com",
            "IT02A0306909606100000123456",
            "https://payments.example.com/giulia"
        );

        when(userService.updateCurrentUser(eq(TEST_USER_ID), any(UserUpdateRequestDTO.class)))
            .thenReturn(updatedResponse);

        mockMvc.perform(patch(BASE_URL + "/me")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testUpdateRequestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_USER_ID.toString()))
            .andExpect(jsonPath("$.name").value("Giulia"))
            .andExpect(jsonPath("$.email").value("giulia.bianchi@example.com"));

        verify(userService).updateCurrentUser(eq(TEST_USER_ID), any(UserUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/users/me - should return 400 Bad Request on invalid request body")
    void testUpdateCurrentUser_InvalidInput() throws Exception {
        UserUpdateRequestDTO invalidRequestDTO = new UserUpdateRequestDTO(
            "Giulia",
            "Bianchi",
            "invalid-email",
            "IT02A0306909606100000123456",
            "https://payments.example.com/giulia"
        );

        mockMvc.perform(patch(BASE_URL + "/me")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).updateCurrentUser(any(UUID.class), any(UserUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/users/me - should return 400 Bad Request when service throws IllegalArgumentException")
    void testUpdateCurrentUser_IllegalArgument() throws Exception {
        when(userService.updateCurrentUser(eq(TEST_USER_ID), any(UserUpdateRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Email already registered"));

        mockMvc.perform(patch(BASE_URL + "/me")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testUpdateRequestDTO)))
            .andExpect(status().isBadRequest());

        verify(userService).updateCurrentUser(eq(TEST_USER_ID), any(UserUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/users/me - should return 500 Internal Server Error when service throws IllegalStateException")
    void testUpdateCurrentUser_IllegalState() throws Exception {
        when(userService.updateCurrentUser(eq(TEST_USER_ID), any(UserUpdateRequestDTO.class)))
            .thenThrow(new IllegalStateException("Unexpected state"));

        mockMvc.perform(patch(BASE_URL + "/me")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testUpdateRequestDTO)))
            .andExpect(status().isInternalServerError());

        verify(userService).updateCurrentUser(eq(TEST_USER_ID), any(UserUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/users/me - should return 403 Forbidden when service throws AccessDeniedException")
    void testUpdateCurrentUser_Forbidden() throws Exception {
        when(userService.updateCurrentUser(eq(TEST_USER_ID), any(UserUpdateRequestDTO.class)))
            .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(patch(BASE_URL + "/me")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testUpdateRequestDTO)))
            .andExpect(status().isForbidden());

        verify(userService).updateCurrentUser(eq(TEST_USER_ID), any(UserUpdateRequestDTO.class));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("PATCH /api/users/me - should return 401 Unauthorized for unauthenticated user")
    void testUpdateCurrentUser_Unauthenticated() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/me")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testUpdateRequestDTO)))
            .andExpect(status().isUnauthorized());

        verify(userService, never()).updateCurrentUser(any(UUID.class), any(UserUpdateRequestDTO.class));
    }
}