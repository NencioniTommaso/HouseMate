package com.housemate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.backend.ServerApp;
import com.housemate.backend.service.JwtService;
import com.housemate.backend.service.UserService;
import com.housemate.backend.service.expense.ExpenseService;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.ExpenseOverviewResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseShareResponseDTO;
import com.housemate.shared.dto.expense.response.UserSettlementOverviewResponseDTO;
import com.housemate.shared.enums.ExpenseSplitType;
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

@WebMvcTest(ExpenseController.class)
@ContextConfiguration(classes = ServerApp.class)
@DisplayName("ExpenseController Tests")
@WithMockUser(username = "11111111-1111-1111-1111-111111111111")
@SuppressWarnings("null")
class ExpenseControllerTest {

    private static final String BASE_URL = "/api/expenses";
    private static final String TEST_USER_ID = "11111111-1111-1111-1111-111111111111";
    private static final UUID TEST_USER_UUID = UUID.fromString(TEST_USER_ID);
    private static final String EXPENSE_ID = "22222222-2222-2222-2222-222222222222";
    private static final UUID EXPENSE_UUID = UUID.fromString(EXPENSE_ID);
    private static final String HOUSEHOLD_ID = "33333333-3333-3333-3333-333333333333";
    private static final UUID HOUSEHOLD_UUID = UUID.fromString(HOUSEHOLD_ID);
    private static final String INVOLVED_USER_ID = "44444444-4444-4444-4444-444444444444";
    private static final UUID INVOLVED_USER_UUID = UUID.fromString(INVOLVED_USER_ID);
    private static final String SHARE_ID = "55555555-5555-5555-5555-555555555555";
    private static final UUID SHARE_UUID = UUID.fromString(SHARE_ID);
    private static final String EXPENSE_DESCRIPTION = "Groceries";
    private static final String EXPENSE_AMOUNT = "120.00";
    private static final String SHARE_AMOUNT = "60.00";
    private static final String PAYER_FULL_NAME = "Alex Smith";
    private static final String SHARE_USER_FULL_NAME = "Taylor Brown";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private ExpenseCreateRequestDTO validCreateRequest;
    private ExpenseCreateRequestDTO invalidCreateRequest;
    private ExpenseResponseDTO expenseResponse;
        private ExpenseOverviewResponseDTO expenseOverviewResponse;
        private UserSettlementOverviewResponseDTO userSettlementOverviewResponse;
    private TransactionFilterRequestDTO validFilterRequest;

    @BeforeEach
    void setUp() {
        validCreateRequest = createValidExpenseCreateRequest();
        invalidCreateRequest = createInvalidExpenseCreateRequest();
        expenseResponse = createExpenseResponse();
        expenseOverviewResponse = createExpenseOverviewResponse();
        userSettlementOverviewResponse = createUserSettlementOverviewResponse();
        validFilterRequest = createValidTransactionFilterRequest();
    }

    // ============ Tests for POST /api/expenses ============

    @Test
    @DisplayName("POST /api/expenses - returns 201 with created expense")
    void testCreateExpense_Success() throws Exception {
        when(expenseService.createExpense(eq(TEST_USER_UUID), any(ExpenseCreateRequestDTO.class)))
                .thenReturn(expenseResponse);

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(EXPENSE_ID))
                .andExpect(jsonPath("$.description").value(EXPENSE_DESCRIPTION))
                .andExpect(jsonPath("$.amount").value(comparesEqualTo(new BigDecimal(EXPENSE_AMOUNT)), BigDecimal.class))
                .andExpect(jsonPath("$.payerId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.payerFullName").value(PAYER_FULL_NAME))
                .andExpect(jsonPath("$.splitType").value(ExpenseSplitType.EQUAL_SPLIT.name()))
                .andExpect(jsonPath("$.householdId").value(HOUSEHOLD_ID))
                .andExpect(jsonPath("$.shares[0].id").value(SHARE_ID))
                .andExpect(jsonPath("$.shares[0].userId").value(INVOLVED_USER_ID))
                .andExpect(jsonPath("$.shares[0].userFullName").value(SHARE_USER_FULL_NAME))
                .andExpect(jsonPath("$.shares[0].amount").value(comparesEqualTo(new BigDecimal(SHARE_AMOUNT)), BigDecimal.class));

        verify(expenseService).createExpense(eq(TEST_USER_UUID), any(ExpenseCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/expenses - returns 400 for invalid payload")
    void testCreateExpense_InvalidInput() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidCreateRequest)))
                .andExpect(status().isBadRequest());

        verify(expenseService, never()).createExpense(any(UUID.class), any(ExpenseCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/expenses - returns 400 when service throws IllegalArgumentException")
    void testCreateExpense_IllegalArgument() throws Exception {
        when(expenseService.createExpense(eq(TEST_USER_UUID), any(ExpenseCreateRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Payer not found with ID: " + TEST_USER_UUID));

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest());

        verify(expenseService).createExpense(eq(TEST_USER_UUID), any(ExpenseCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/expenses - returns 403 when service throws IllegalStateException")
    void testCreateExpense_IllegalState() throws Exception {
        when(expenseService.createExpense(eq(TEST_USER_UUID), any(ExpenseCreateRequestDTO.class)))
                .thenThrow(new IllegalStateException("Payer is not currently a member of any household"));

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isForbidden());

        verify(expenseService).createExpense(eq(TEST_USER_UUID), any(ExpenseCreateRequestDTO.class));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/expenses - returns 401 for unauthenticated user")
    void testCreateExpense_Unauthenticated() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isUnauthorized());

        verify(expenseService, never()).createExpense(any(UUID.class), any(ExpenseCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/expenses - returns 403 when access is denied")
    void testCreateExpense_Forbidden() throws Exception {
        when(expenseService.createExpense(eq(TEST_USER_UUID), any(ExpenseCreateRequestDTO.class)))
                .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isForbidden());

        verify(expenseService).createExpense(eq(TEST_USER_UUID), any(ExpenseCreateRequestDTO.class));
    }

    // ============ Tests for GET /api/expenses ============

    @Test
    @DisplayName("GET /api/expenses - returns 200 with filtered expenses")
    void testGetFilteredExpenses_Success() throws Exception {
        when(expenseService.getFilteredExpenses(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class)))
                .thenReturn(List.of(expenseResponse));

        mockMvc.perform(get(BASE_URL)
                        .param("householdId", validFilterRequest.householdId().toString())
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("description", validFilterRequest.description()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(EXPENSE_ID))
                .andExpect(jsonPath("$[0].description").value(EXPENSE_DESCRIPTION))
                .andExpect(jsonPath("$[0].splitType").value(ExpenseSplitType.EQUAL_SPLIT.name()))
                .andExpect(jsonPath("$[0].householdId").value(HOUSEHOLD_ID));

        verify(expenseService).getFilteredExpenses(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/expenses - returns 400 for malformed householdId")
    void testGetFilteredExpenses_InvalidInput() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("householdId", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(expenseService, never()).getFilteredExpenses(any(UUID.class), any(TransactionFilterRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/expenses - returns 400 when service throws IllegalArgumentException")
    void testGetFilteredExpenses_IllegalArgument() throws Exception {
        when(expenseService.getFilteredExpenses(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("User not found with ID: " + TEST_USER_UUID));

        mockMvc.perform(get(BASE_URL)
                        .param("householdId", validFilterRequest.householdId().toString())
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("description", validFilterRequest.description()))
                .andExpect(status().isBadRequest());

        verify(expenseService).getFilteredExpenses(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/expenses - returns 403 when service throws IllegalStateException")
    void testGetFilteredExpenses_IllegalState() throws Exception {
        when(expenseService.getFilteredExpenses(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class)))
                .thenThrow(new IllegalStateException("Unexpected business state"));

        mockMvc.perform(get(BASE_URL)
                        .param("householdId", validFilterRequest.householdId().toString())
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("description", validFilterRequest.description()))
                .andExpect(status().isForbidden());

        verify(expenseService).getFilteredExpenses(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/expenses - returns 401 for unauthenticated user")
    void testGetFilteredExpenses_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("householdId", validFilterRequest.householdId().toString())
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("description", validFilterRequest.description()))
                .andExpect(status().isUnauthorized());

        verify(expenseService, never()).getFilteredExpenses(any(UUID.class), any(TransactionFilterRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/expenses - returns 403 when access is denied")
    void testGetFilteredExpenses_Forbidden() throws Exception {
        when(expenseService.getFilteredExpenses(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class)))
                .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(get(BASE_URL)
                        .param("householdId", validFilterRequest.householdId().toString())
                        .param("userTransactionRole", validFilterRequest.userTransactionRole().name())
                        .param("description", validFilterRequest.description()))
                .andExpect(status().isForbidden());

        verify(expenseService).getFilteredExpenses(eq(TEST_USER_UUID), any(TransactionFilterRequestDTO.class));
    }

    // ============ Tests for GET /api/expenses/overview ============

    @Test
        @DisplayName("GET /api/expenses/overview - returns 200 with current-month household expense overview")
        void testGetCurrentMonthExpenseOverview_Success() throws Exception {
                when(expenseService.getCurrentMonthExpenseOverview(TEST_USER_UUID))
                .thenReturn(expenseOverviewResponse);

        mockMvc.perform(get(BASE_URL + "/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(comparesEqualTo(new BigDecimal("420.00")), BigDecimal.class))
                .andExpect(jsonPath("$.expenseCount").value(7));

        verify(expenseService).getCurrentMonthExpenseOverview(TEST_USER_UUID);
    }

    @Test
    @DisplayName("GET /api/expenses/overview - returns 400 when service throws IllegalArgumentException")
        void testGetCurrentMonthExpenseOverview_IllegalArgument() throws Exception {
                when(expenseService.getCurrentMonthExpenseOverview(TEST_USER_UUID))
                .thenThrow(new IllegalArgumentException("User not found with ID: " + TEST_USER_UUID));

        mockMvc.perform(get(BASE_URL + "/overview"))
                .andExpect(status().isBadRequest());

                verify(expenseService).getCurrentMonthExpenseOverview(TEST_USER_UUID);
    }

    @Test
    @DisplayName("GET /api/expenses/overview - returns 403 when service throws IllegalStateException")
        void testGetCurrentMonthExpenseOverview_IllegalState() throws Exception {
                when(expenseService.getCurrentMonthExpenseOverview(TEST_USER_UUID))
                .thenThrow(new IllegalStateException("User is not currently a member of any household"));

        mockMvc.perform(get(BASE_URL + "/overview"))
                .andExpect(status().isForbidden());

                verify(expenseService).getCurrentMonthExpenseOverview(TEST_USER_UUID);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/expenses/overview - returns 401 for unauthenticated user")
        void testGetCurrentMonthExpenseOverview_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/overview"))
                .andExpect(status().isUnauthorized());

                verify(expenseService, never()).getCurrentMonthExpenseOverview(any(UUID.class));
    }

    @Test
    @DisplayName("GET /api/expenses/overview - returns 403 when access is denied")
        void testGetCurrentMonthExpenseOverview_Forbidden() throws Exception {
                when(expenseService.getCurrentMonthExpenseOverview(TEST_USER_UUID))
                .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(get(BASE_URL + "/overview"))
                .andExpect(status().isForbidden());

                verify(expenseService).getCurrentMonthExpenseOverview(TEST_USER_UUID);
    }

    // ============ Tests for GET /api/expenses/me ============

    @Test
    @DisplayName("GET /api/expenses/me - returns 200 with current-month settlements made by user")
    void testGetCurrentMonthUserSettlementOverview_Success() throws Exception {
        when(expenseService.getCurrentMonthUserExpenseOverview(TEST_USER_UUID))
                .thenReturn(userSettlementOverviewResponse);

        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSettlementsMade")
                        .value(comparesEqualTo(new BigDecimal("150.75")), BigDecimal.class));

        verify(expenseService).getCurrentMonthUserExpenseOverview(TEST_USER_UUID);
    }

    @Test
    @DisplayName("GET /api/expenses/me - returns 400 when service throws IllegalArgumentException")
    void testGetCurrentMonthUserSettlementOverview_IllegalArgument() throws Exception {
        when(expenseService.getCurrentMonthUserExpenseOverview(TEST_USER_UUID))
                .thenThrow(new IllegalArgumentException("User not found with ID: " + TEST_USER_UUID));

        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isBadRequest());

        verify(expenseService).getCurrentMonthUserExpenseOverview(TEST_USER_UUID);
    }

    @Test
    @DisplayName("GET /api/expenses/me - returns 403 when service throws IllegalStateException")
    void testGetCurrentMonthUserSettlementOverview_IllegalState() throws Exception {
        when(expenseService.getCurrentMonthUserExpenseOverview(TEST_USER_UUID))
                .thenThrow(new IllegalStateException("User is not currently a member of any household"));

        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isForbidden());

        verify(expenseService).getCurrentMonthUserExpenseOverview(TEST_USER_UUID);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/expenses/me - returns 401 for unauthenticated user")
    void testGetCurrentMonthUserSettlementOverview_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isUnauthorized());

        verify(expenseService, never()).getCurrentMonthUserExpenseOverview(any(UUID.class));
    }

    @Test
    @DisplayName("GET /api/expenses/me - returns 403 when access is denied")
    void testGetCurrentMonthUserSettlementOverview_Forbidden() throws Exception {
        when(expenseService.getCurrentMonthUserExpenseOverview(TEST_USER_UUID))
                .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isForbidden());

        verify(expenseService).getCurrentMonthUserExpenseOverview(TEST_USER_UUID);
    }

    private ExpenseCreateRequestDTO createValidExpenseCreateRequest() {
        return new ExpenseCreateRequestDTO(
                EXPENSE_DESCRIPTION,
                new BigDecimal(EXPENSE_AMOUNT),
                ExpenseSplitType.EQUAL_SPLIT,
                List.of(new ExpenseShareRequestDTO(INVOLVED_USER_UUID, new BigDecimal(SHARE_AMOUNT)))
        );
    }

    private ExpenseCreateRequestDTO createInvalidExpenseCreateRequest() {
        return new ExpenseCreateRequestDTO(
                "",
                null,
                null,
                List.of()
        );
    }

    private TransactionFilterRequestDTO createValidTransactionFilterRequest() {
        return new TransactionFilterRequestDTO(
                HOUSEHOLD_UUID,
                UserTransactionRole.ALL,
                null,
                EXPENSE_DESCRIPTION
        );
    }

    private ExpenseResponseDTO createExpenseResponse() {
        return new ExpenseResponseDTO(
                EXPENSE_UUID,
                EXPENSE_DESCRIPTION,
                LocalDateTime.of(2026, 1, 15, 10, 30),
                new BigDecimal(EXPENSE_AMOUNT),
                TEST_USER_UUID,
                PAYER_FULL_NAME,
                ExpenseSplitType.EQUAL_SPLIT,
                HOUSEHOLD_UUID,
                List.of(createExpenseShareResponse())
        );
    }

        private ExpenseOverviewResponseDTO createExpenseOverviewResponse() {
                return new ExpenseOverviewResponseDTO(
                                new BigDecimal("420.00"),
                                7L
                );
        }

        private UserSettlementOverviewResponseDTO createUserSettlementOverviewResponse() {
                return new UserSettlementOverviewResponseDTO(new BigDecimal("150.75"));
        }

    private ExpenseShareResponseDTO createExpenseShareResponse() {
        return new ExpenseShareResponseDTO(
                SHARE_UUID,
                INVOLVED_USER_UUID,
                SHARE_USER_FULL_NAME,
                new BigDecimal(SHARE_AMOUNT)
        );
    }
}
