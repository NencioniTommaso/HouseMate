package com.housemate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.backend.ServerApp;
import com.housemate.backend.service.JwtService;
import com.housemate.backend.service.UserService;
import com.housemate.backend.service.expense.SettlementService;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettlementController.class)
@ContextConfiguration(classes = ServerApp.class)
@DisplayName("SettlementController Tests")
@WithMockUser(username = "11111111-1111-1111-1111-111111111111")
class SettlementControllerTest {

    private static final String BASE_URL = "/api/settlements";
    private static final String TEST_USER_ID = "11111111-1111-1111-1111-111111111111";
    private static final UUID TEST_USER_UUID = UUID.fromString(TEST_USER_ID);
    private static final String DEBT_ID = "22222222-2222-2222-2222-222222222222";
    private static final UUID DEBT_UUID = UUID.fromString(DEBT_ID);
    private static final String SETTLEMENT_ID = "33333333-3333-3333-3333-333333333333";
    private static final UUID SETTLEMENT_UUID = UUID.fromString(SETTLEMENT_ID);
    private static final String CREDITOR_ID = "44444444-4444-4444-4444-444444444444";
    private static final UUID CREDITOR_UUID = UUID.fromString(CREDITOR_ID);
    private static final String HOUSEHOLD_ID = "55555555-5555-5555-5555-555555555555";
    private static final UUID HOUSEHOLD_UUID = UUID.fromString(HOUSEHOLD_ID);
    private static final String INVOLVED_USER_NAME = "Chris White";
    private static final String SETTLEMENT_AMOUNT = "45.75";
    private static final String SETTLEMENT_DESCRIPTION = "Partial repayment";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SettlementService settlementService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private SettlementCreateRequestDTO validCreateRequest;
    private SettlementCreateRequestDTO invalidCreateRequest;
    private SettlementResponseDTO settlementResponse;
    private TransactionFilterRequestDTO validFilterRequest;

    @BeforeEach
    void setUp() {
        validCreateRequest = createValidSettlementCreateRequest();
        invalidCreateRequest = createInvalidSettlementCreateRequest();
        settlementResponse = createSettlementResponse();
        validFilterRequest = createValidTransactionFilterRequest();
    }

    // ============ Tests for POST /api/settlements/{debtId} ============

    @Test
    @DisplayName("POST /api/settlements/{debtId} - returns 201 with created settlement")
    void testSettleDebt_Success() throws Exception {
        when(settlementService.settleDebt(eq(TEST_USER_UUID), any(SettlementCreateRequestDTO.class)))
                .thenReturn(settlementResponse);

        mockMvc.perform(post(BASE_URL + "/{debtId}", DEBT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.settlementId").value(SETTLEMENT_ID))
                .andExpect(jsonPath("$.userTransactionRole").value(UserTransactionRole.DEBTOR.name()))
                .andExpect(jsonPath("$.involvedId").value(CREDITOR_ID))
                .andExpect(jsonPath("$.involvedName").value(INVOLVED_USER_NAME))
                .andExpect(jsonPath("$.amount").value(comparesEqualTo(new BigDecimal(SETTLEMENT_AMOUNT)), BigDecimal.class))
                .andExpect(jsonPath("$.description").value(SETTLEMENT_DESCRIPTION))
                .andExpect(jsonPath("$.householdId").value(HOUSEHOLD_ID));

        verify(settlementService).settleDebt(eq(TEST_USER_UUID), any(SettlementCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/settlements/{debtId} - returns 400 for invalid payload")
    void testSettleDebt_InvalidInput() throws Exception {
        mockMvc.perform(post(BASE_URL + "/{debtId}", DEBT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidCreateRequest)))
                .andExpect(status().isBadRequest());

        verify(settlementService, never()).settleDebt(any(UUID.class), any(SettlementCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/settlements/{debtId} - returns 400 when service throws IllegalArgumentException")
    void testSettleDebt_IllegalArgument() throws Exception {
        when(settlementService.settleDebt(eq(TEST_USER_UUID), any(SettlementCreateRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Debt not found with ID: " + DEBT_UUID));

        mockMvc.perform(post(BASE_URL + "/{debtId}", DEBT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest());

        verify(settlementService).settleDebt(eq(TEST_USER_UUID), any(SettlementCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/settlements/{debtId} - forwards body debtId even when path debtId differs")
    void testSettleDebt_PathDebtIdDiffersFromBodyDebtId_UsesBody() throws Exception {
        String differentPathDebtId = "99999999-9999-9999-9999-999999999999";
        when(settlementService.settleDebt(eq(TEST_USER_UUID), any(SettlementCreateRequestDTO.class)))
                .thenReturn(settlementResponse);

        mockMvc.perform(post(BASE_URL + "/{debtId}", differentPathDebtId)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.settlementId").value(SETTLEMENT_ID));

        verify(settlementService).settleDebt(eq(TEST_USER_UUID), any(SettlementCreateRequestDTO.class));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/settlements/{debtId} - returns 401 for unauthenticated user")
    void testSettleDebt_Unauthenticated() throws Exception {
        mockMvc.perform(post(BASE_URL + "/{debtId}", DEBT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isUnauthorized());

        verify(settlementService, never()).settleDebt(any(UUID.class), any(SettlementCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/settlements/{debtId} - returns 403 when access is denied")
    void testSettleDebt_Forbidden() throws Exception {
        when(settlementService.settleDebt(eq(TEST_USER_UUID), any(SettlementCreateRequestDTO.class)))
                .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(post(BASE_URL + "/{debtId}", DEBT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isForbidden());

        verify(settlementService).settleDebt(eq(TEST_USER_UUID), any(SettlementCreateRequestDTO.class));
    }

    // ============ Tests for GET /api/settlements ============

    @Test
    @DisplayName("GET /api/settlements - returns 200 with filtered settlements")
    void testGetFilteredSettlements_Success() throws Exception {
        when(settlementService.getFilteredSettlements(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class)))
                .thenReturn(List.of(settlementResponse));

        mockMvc.perform(get(BASE_URL)
                        .param("householdId", validFilterRequest.householdId().toString())
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("description", validFilterRequest.description()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].settlementId").value(SETTLEMENT_ID))
                .andExpect(jsonPath("$[0].userTransactionRole").value(UserTransactionRole.DEBTOR.name()))
                .andExpect(jsonPath("$[0].involvedId").value(CREDITOR_ID))
                .andExpect(jsonPath("$[0].description").value(SETTLEMENT_DESCRIPTION))
                .andExpect(jsonPath("$[0].householdId").value(HOUSEHOLD_ID));

        verify(settlementService).getFilteredSettlements(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/settlements - returns 400 for malformed householdId")
    void testGetFilteredSettlements_InvalidInput() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("householdId", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(settlementService, never()).getFilteredSettlements(any(UUID.class), any(TransactionFilterRequestDTO.class));
    }

        @Test
        @DisplayName("GET /api/settlements - returns 400 when service throws IllegalArgumentException")
        void testGetFilteredSettlements_IllegalArgument() throws Exception {
                when(settlementService.getFilteredSettlements(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class)))
                                .thenThrow(new IllegalArgumentException("User not found with ID: " + TEST_USER_UUID));

        mockMvc.perform(get(BASE_URL)
                        .param("householdId", validFilterRequest.householdId().toString())
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("description", validFilterRequest.description()))
                .andExpect(status().isBadRequest());

        verify(settlementService).getFilteredSettlements(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/settlements - returns 401 for unauthenticated user")
    void testGetFilteredSettlements_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("householdId", validFilterRequest.householdId().toString())
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("description", validFilterRequest.description()))
                .andExpect(status().isUnauthorized());

        verify(settlementService, never()).getFilteredSettlements(any(UUID.class), any(TransactionFilterRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/settlements - returns 403 when access is denied")
    void testGetFilteredSettlements_Forbidden() throws Exception {
        when(settlementService.getFilteredSettlements(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class)))
                .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(get(BASE_URL)
                        .param("householdId", validFilterRequest.householdId().toString())
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("description", validFilterRequest.description()))
                .andExpect(status().isForbidden());

        verify(settlementService).getFilteredSettlements(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class));
    }

    private SettlementCreateRequestDTO createValidSettlementCreateRequest() {
        return new SettlementCreateRequestDTO(
                DEBT_UUID,
                CREDITOR_UUID,
                new BigDecimal(SETTLEMENT_AMOUNT),
                SETTLEMENT_DESCRIPTION
        );
    }

    private SettlementCreateRequestDTO createInvalidSettlementCreateRequest() {
        return new SettlementCreateRequestDTO(
                null,
                null,
                null,
                SETTLEMENT_DESCRIPTION
        );
    }

    private SettlementResponseDTO createSettlementResponse() {
        return new SettlementResponseDTO(
                SETTLEMENT_UUID,
                UserTransactionRole.DEBTOR,
                CREDITOR_UUID,
                INVOLVED_USER_NAME,
                new BigDecimal(SETTLEMENT_AMOUNT),
                LocalDateTime.of(2026, 2, 10, 14, 45),
                SETTLEMENT_DESCRIPTION,
                HOUSEHOLD_UUID
        );
    }

    private TransactionFilterRequestDTO createValidTransactionFilterRequest() {
        return new TransactionFilterRequestDTO(
                HOUSEHOLD_UUID,
                UserTransactionRole.ALL,
                null,
                SETTLEMENT_DESCRIPTION
        );
    }
}
