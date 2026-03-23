package com.housemate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.backend.ServerApp;
import com.housemate.backend.service.JwtService;
import com.housemate.backend.service.UserService;
import com.housemate.backend.service.expense.DebtService;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.enums.UserTransactionRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DebtController.class)
@ContextConfiguration(classes = ServerApp.class)
@DisplayName("DebtController Tests")
@WithMockUser(username = "11111111-1111-1111-1111-111111111111")
class DebtControllerTest {

    private static final String BASE_URL = "/api/debts";
    private static final String TEST_USER_ID = "11111111-1111-1111-1111-111111111111";
    private static final UUID TEST_USER_UUID = UUID.fromString(TEST_USER_ID);
    private static final String DEBT_ID = "22222222-2222-2222-2222-222222222222";
    private static final UUID DEBT_UUID = UUID.fromString(DEBT_ID);
    private static final String INVOLVED_USER_ID = "33333333-3333-3333-3333-333333333333";
    private static final UUID INVOLVED_USER_UUID = UUID.fromString(INVOLVED_USER_ID);
    private static final String INVOLVED_USER_NAME = "John Doe";
    private static final String DEBT_AMOUNT = "125.50";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DebtService debtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private DebtFilterRequestDTO validFilterRequest;
    private DebtResponseDTO debtResponse;

    @BeforeEach
    void setUp() {
        validFilterRequest = createValidFilterRequest();
        debtResponse = createDebtResponse();
    }

    // ============ Tests for GET /api/debts ============

    @Test
    @DisplayName("GET /api/debts - returns 200 with filtered debts")
    void testGetFilteredDebts_Success() throws Exception {
        when(debtService.getFilteredDebts(eq(TEST_USER_UUID), any(DebtFilterRequestDTO.class)))
                .thenReturn(List.of(debtResponse));

        mockMvc.perform(get(BASE_URL)
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("involvedId", validFilterRequest.involvedId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].debtId").value(DEBT_ID))
                .andExpect(jsonPath("$[0].userTransactionRole").value(UserTransactionRole.DEBTOR.name()))
                .andExpect(jsonPath("$[0].involvedId").value(INVOLVED_USER_ID))
                .andExpect(jsonPath("$[0].involvedName").value(INVOLVED_USER_NAME))
                .andExpect(jsonPath("$[0].amount").value(comparesEqualTo(new BigDecimal(DEBT_AMOUNT)), BigDecimal.class));

        verify(debtService).getFilteredDebts(eq(TEST_USER_UUID), any(DebtFilterRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/debts - returns 400 when required transaction role is missing")
    void testGetFilteredDebts_InvalidInput() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("involvedId", INVOLVED_USER_ID))
                .andExpect(status().isBadRequest());

        verify(debtService, never()).getFilteredDebts(any(UUID.class), any(DebtFilterRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/debts - returns 400 when service throws IllegalArgumentException")
    void testGetFilteredDebts_IllegalArgument() throws Exception {
        when(debtService.getFilteredDebts(eq(TEST_USER_UUID), any(DebtFilterRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("User not found with ID: " + TEST_USER_UUID));

        mockMvc.perform(get(BASE_URL)
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("involvedId", validFilterRequest.involvedId().toString()))
                .andExpect(status().isBadRequest());

        verify(debtService).getFilteredDebts(eq(TEST_USER_UUID), any(DebtFilterRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/debts - returns 500 when service throws IllegalStateException")
    void testGetFilteredDebts_IllegalState() throws Exception {
        when(debtService.getFilteredDebts(eq(TEST_USER_UUID), any(DebtFilterRequestDTO.class)))
                .thenThrow(new IllegalStateException("User must be in an active household to view debts."));

        mockMvc.perform(get(BASE_URL)
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("involvedId", validFilterRequest.involvedId().toString()))
                .andExpect(status().isInternalServerError());

        verify(debtService).getFilteredDebts(eq(TEST_USER_UUID), any(DebtFilterRequestDTO.class));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/debts - returns 401 for unauthenticated user")
    void testGetFilteredDebts_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("involvedId", validFilterRequest.involvedId().toString()))
                .andExpect(status().isUnauthorized());

        verify(debtService, never()).getFilteredDebts(any(UUID.class), any(DebtFilterRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/debts - returns 403 when access is denied")
    void testGetFilteredDebts_Forbidden() throws Exception {
        when(debtService.getFilteredDebts(eq(TEST_USER_UUID), any(DebtFilterRequestDTO.class)))
                .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(get(BASE_URL)
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("involvedId", validFilterRequest.involvedId().toString()))
                .andExpect(status().isForbidden());

        verify(debtService).getFilteredDebts(eq(TEST_USER_UUID), any(DebtFilterRequestDTO.class));
    }

    // ============ Tests for DELETE /api/debts/{debtId} ============

    @Test
    @DisplayName("DELETE /api/debts/{debtId} - returns 204 when debt is deleted")
    void testDeleteDebt_Success() throws Exception {
        doNothing().when(debtService).deleteDebt(DEBT_UUID);

        mockMvc.perform(delete(BASE_URL + "/{debtId}", DEBT_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(debtService).deleteDebt(DEBT_UUID);
    }

    @Test
    @DisplayName("DELETE /api/debts/{debtId} - returns 400 for malformed debtId")
    void testDeleteDebt_InvalidInput() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{debtId}", "not-a-uuid")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(debtService, never()).deleteDebt(any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/debts/{debtId} - returns 400 when service throws IllegalArgumentException")
    void testDeleteDebt_IllegalArgument() throws Exception {
        doThrow(new IllegalArgumentException("Debt not found with ID: " + DEBT_UUID))
                .when(debtService)
                .deleteDebt(DEBT_UUID);

        mockMvc.perform(delete(BASE_URL + "/{debtId}", DEBT_ID)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(debtService).deleteDebt(DEBT_UUID);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("DELETE /api/debts/{debtId} - returns 401 for unauthenticated user")
    void testDeleteDebt_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{debtId}", DEBT_ID)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(debtService, never()).deleteDebt(any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/debts/{debtId} - returns 403 when access is denied")
    void testDeleteDebt_Forbidden() throws Exception {
        doThrow(new AccessDeniedException("Forbidden"))
                .when(debtService)
                .deleteDebt(DEBT_UUID);

        mockMvc.perform(delete(BASE_URL + "/{debtId}", DEBT_ID)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(debtService).deleteDebt(DEBT_UUID);
    }

    private DebtFilterRequestDTO createValidFilterRequest() {
        return new DebtFilterRequestDTO(UserTransactionRole.DEBTOR, INVOLVED_USER_UUID);
    }

    private DebtResponseDTO createDebtResponse() {
        return new DebtResponseDTO(
                DEBT_UUID,
                UserTransactionRole.DEBTOR,
                INVOLVED_USER_UUID,
                INVOLVED_USER_NAME,
                new BigDecimal(DEBT_AMOUNT)
        );
    }
}
