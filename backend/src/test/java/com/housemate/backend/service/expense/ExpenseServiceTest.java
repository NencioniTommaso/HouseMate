package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.repository.expense.ExpenseRepository;
import com.housemate.backend.repository.expense.QuerySpecification;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategy;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategyFactory;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.enums.UserTransactionRole;
import com.housemate.shared.enums.ExpenseSplitType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked"})
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DebtService debtService;

    @Mock
    private ExpenseSplitStrategyFactory strategyFactory;

    private ExpenseService expenseService;

    private UUID payerId;
    private UUID userId1;
    private UUID userId2;
    private UUID householdId;
    private User payer;
    private User user1;
    private User user2;
    private Household household;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(expenseRepository, userRepository, debtService, strategyFactory);

        payerId = UUID.randomUUID();
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        householdId = UUID.randomUUID();

        household = new Household();
        household.setId(householdId); 
        household.setName("Test Household");

        payer = createUser(payerId, "John", "Payer");
        user1 = createUser(userId1, "Alice", "User");
        user2 = createUser(userId2, "Bob", "User");

        HouseholdMembership payerMembership = new HouseholdMembership();
        payerMembership.setHousehold(household);
        payerMembership.setUser(payer);
        payer.setHouseholdMembership(payerMembership);
    }

    @Nested
    class CreateExpenseTests {

        @Test
        void createExpense_whenPayerIsInShares_doesNotCreateSelfDebt() {
            // Arrange
            // Payer pays 30.00 for the whole house (10.00 each)
            ExpenseCreateRequestDTO requestDTO = new ExpenseCreateRequestDTO(
                    "Groceries for everyone",
                    new BigDecimal("30.00"),
                    ExpenseSplitType.EQUAL_SPLIT,
                    Arrays.asList(
                            new ExpenseShareRequestDTO(payerId, null), // Payer explicitly in shares
                            new ExpenseShareRequestDTO(userId1, null),
                            new ExpenseShareRequestDTO(userId2, null)
                    )
            );

            when(userRepository.findById(payerId)).thenReturn(Optional.of(payer));
            when(userRepository.findAllById(any())).thenReturn(List.of(payer, user1, user2));

            ExpenseSplitStrategy mockStrategy = mock(ExpenseSplitStrategy.class);
            Map<UUID, BigDecimal> calculatedShares = Map.of(
                    payerId, new BigDecimal("10.00"),
                    userId1, new BigDecimal("10.00"),
                    userId2, new BigDecimal("10.00")
            );
            
            when(mockStrategy.calculateShares(eq(new BigDecimal("30.00")), anyList())).thenReturn(calculatedShares);
            when(strategyFactory.getStrategy(ExpenseSplitType.EQUAL_SPLIT)).thenReturn(mockStrategy);
            when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ExpenseResponseDTO result = expenseService.createExpense(payerId, requestDTO);

            // Assert
            assertThat(result.shares()).hasSize(3);

            // Verify debts are created for the OTHER users
            verify(debtService).addDebt(eq(userId1), eq(payerId), eq(householdId), eq(new BigDecimal("10.00")));
            verify(debtService).addDebt(eq(userId2), eq(payerId), eq(householdId), eq(new BigDecimal("10.00")));
            
            // CRITICAL: Verify the payer does NOT owe themselves 10.00
            verify(debtService, never()).addDebt(eq(payerId), any(), any(), any());
            verifyNoMoreInteractions(debtService);
        }

        @Test
        void createExpense_whenPayerIsNotInShares_createsDebtsForOnlyInvolvedUsers() {
            // Arrange
            // Payer pays 100.00 entirely on behalf of User 1 and User 2 (50.00 each)
            ExpenseCreateRequestDTO requestDTO = new ExpenseCreateRequestDTO(
                    "Concert tickets for housemates",
                    new BigDecimal("100.00"),
                    ExpenseSplitType.EXACT_AMOUNT,
                    Arrays.asList(
                            new ExpenseShareRequestDTO(userId1, new BigDecimal("50.00")),
                            new ExpenseShareRequestDTO(userId2, new BigDecimal("50.00"))
                    )
            );

            when(userRepository.findById(payerId)).thenReturn(Optional.of(payer));
            when(userRepository.findAllById(any())).thenReturn(List.of(payer, user1, user2));

            ExpenseSplitStrategy mockStrategy = mock(ExpenseSplitStrategy.class);
            Map<UUID, BigDecimal> calculatedShares = Map.of(
                    userId1, new BigDecimal("50.00"),
                    userId2, new BigDecimal("50.00")
            );
            
            when(mockStrategy.calculateShares(eq(new BigDecimal("100.00")), anyList())).thenReturn(calculatedShares);
            when(strategyFactory.getStrategy(ExpenseSplitType.EXACT_AMOUNT)).thenReturn(mockStrategy);
            when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ExpenseResponseDTO result = expenseService.createExpense(payerId, requestDTO);

            // Assert
            assertThat(result.shares()).hasSize(2);
            
            // Verify the payer is NOT in the resulting shares list
            assertThat(result.shares())
                    .extracting(share -> share.userId())
                    .containsExactlyInAnyOrder(userId1, userId2)
                    .doesNotContain(payerId);

            // Verify debts are created properly for the two involved users
            verify(debtService).addDebt(eq(userId1), eq(payerId), eq(householdId), eq(new BigDecimal("50.00")));
            verify(debtService).addDebt(eq(userId2), eq(payerId), eq(householdId), eq(new BigDecimal("50.00")));
            
            // Verify no stray debts were created
            verify(debtService, never()).addDebt(eq(payerId), any(), any(), any());
            verifyNoMoreInteractions(debtService);
        }

        @Test
        void createExpense_withNullPayerId_throwsIllegalArgumentException() {
            // Arrange
            ExpenseCreateRequestDTO requestDTO = new ExpenseCreateRequestDTO(
                    "Groceries",
                    new BigDecimal("30.00"),
                    ExpenseSplitType.EQUAL_SPLIT,
                    Collections.emptyList()
            );

            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(null, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payer ID must not be null");

            verifyNoInteractions(expenseRepository, userRepository, debtService, strategyFactory);
        }

        @Test
        void createExpense_withNullRequestDTO_throwsIllegalArgumentException() {
            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(payerId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense request DTO must not be null");

            verifyNoInteractions(expenseRepository, userRepository, debtService, strategyFactory);
        }

        @Test
        void createExpense_payerNotFound_throwsIllegalArgumentException() {
            // Arrange
            ExpenseCreateRequestDTO requestDTO = new ExpenseCreateRequestDTO(
                    "Groceries",
                    new BigDecimal("30.00"),
                    ExpenseSplitType.EQUAL_SPLIT,
                    Collections.emptyList()
            );

            when(userRepository.findById(payerId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(payerId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payer not found with ID");

            verify(expenseRepository, never()).save(any());
            verifyNoInteractions(debtService, strategyFactory);
        }

        @Test
        void createExpense_payerNotMemberOfHousehold_throwsIllegalStateException() {
            // Arrange
            ExpenseCreateRequestDTO requestDTO = new ExpenseCreateRequestDTO(
                    "Groceries",
                    new BigDecimal("30.00"),
                    ExpenseSplitType.EQUAL_SPLIT,
                    Collections.emptyList()
            );

            User payerWithoutHousehold = createUser(payerId, "No", "Household");
            payerWithoutHousehold.setHouseholdMembership(null);

            when(userRepository.findById(payerId)).thenReturn(Optional.of(payerWithoutHousehold));

            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(payerId, requestDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Payer is not currently a member of any household");

            verify(expenseRepository, never()).save(any());
            verifyNoInteractions(debtService, strategyFactory);
        }

        @Test
        void createExpense_withZeroAmount_throwsIllegalArgumentException() {
            // Arrange
            ExpenseCreateRequestDTO requestDTO = new ExpenseCreateRequestDTO(
                    "Invalid Groceries",
                    BigDecimal.ZERO, 
                    ExpenseSplitType.EQUAL_SPLIT,
                    Collections.emptyList()
            );

            when(userRepository.findById(payerId)).thenReturn(Optional.of(payer));

            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(payerId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense amount must be strictly greater than zero");

            verifyNoInteractions(strategyFactory, expenseRepository, debtService);
        }

        @Test
        void createExpense_withNegativeAmount_throwsIllegalArgumentException() {
            // Arrange
            ExpenseCreateRequestDTO requestDTO = new ExpenseCreateRequestDTO(
                    "Invalid Groceries",
                    new BigDecimal("-1.00"),
                    ExpenseSplitType.EQUAL_SPLIT,
                    Collections.emptyList()
            );

            when(userRepository.findById(payerId)).thenReturn(Optional.of(payer));

            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(payerId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense amount must be strictly greater than zero");

            verifyNoInteractions(strategyFactory, expenseRepository, debtService);
        }

        @Test
        void createExpense_repositoryThrowsException_preventsDebtCreation() {
            // Arrange
            ExpenseCreateRequestDTO requestDTO = new ExpenseCreateRequestDTO(
                    "Groceries",
                    new BigDecimal("30.00"),
                    ExpenseSplitType.EQUAL_SPLIT,
                    Arrays.asList(new ExpenseShareRequestDTO(userId1, null))
            );

            when(userRepository.findById(payerId)).thenReturn(Optional.of(payer));
            when(userRepository.findAllById(any())).thenReturn(List.of(payer, user1));

            ExpenseSplitStrategy mockStrategy = mock(ExpenseSplitStrategy.class);
            Map<UUID, BigDecimal> calculatedShares = Map.of(
                    payerId, new BigDecimal("15.00"),
                    userId1, new BigDecimal("15.00")
            );
            
            when(mockStrategy.calculateShares(any(), any())).thenReturn(calculatedShares);
            when(strategyFactory.getStrategy(any())).thenReturn(mockStrategy);
            
            // Simulate Database Failure
            when(expenseRepository.save(any(Expense.class))).thenThrow(new RuntimeException("Database down"));

            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(payerId, requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database down");

            // Verify transactional integrity: debts should NOT be created
            verifyNoInteractions(debtService);
        }

        @Test
        void createExpense_unknownUserInCalculatedShares_throwsAndDoesNotPersist() {
            // Arrange
            UUID unknownUserId = UUID.randomUUID();
            ExpenseCreateRequestDTO requestDTO = new ExpenseCreateRequestDTO(
                    "Groceries",
                    new BigDecimal("30.00"),
                    ExpenseSplitType.EQUAL_SPLIT,
                    Arrays.asList(
                            new ExpenseShareRequestDTO(userId1, null),
                            new ExpenseShareRequestDTO(userId2, null)
                    )
            );

            when(userRepository.findById(payerId)).thenReturn(Optional.of(payer));
            when(userRepository.findAllById(any())).thenReturn(Arrays.asList(payer, user1));

            ExpenseSplitStrategy mockStrategy = mock(ExpenseSplitStrategy.class);
            Map<UUID, BigDecimal> calculatedShares = new HashMap<>();
            calculatedShares.put(payerId, new BigDecimal("10.00"));
            calculatedShares.put(unknownUserId, new BigDecimal("10.00"));
            
            when(mockStrategy.calculateShares(eq(new BigDecimal("30.00")), anyList())).thenReturn(calculatedShares);
            when(strategyFactory.getStrategy(ExpenseSplitType.EQUAL_SPLIT)).thenReturn(mockStrategy);

            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(payerId, requestDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Calculated share for unknown user ID");

            verify(expenseRepository, never()).save(any());
            verifyNoInteractions(debtService);
        }

        @Test
        void createExpense_withExactAmountSplit_usesRequestedStrategy() {
            // Arrange
            ExpenseCreateRequestDTO requestDTO = new ExpenseCreateRequestDTO(
                    "Bill Split",
                    new BigDecimal("150.00"),
                    ExpenseSplitType.EXACT_AMOUNT,
                    Arrays.asList(
                            new ExpenseShareRequestDTO(userId1, new BigDecimal("50.00")),
                            new ExpenseShareRequestDTO(userId2, new BigDecimal("100.00"))
                    )
            );

            when(userRepository.findById(payerId)).thenReturn(Optional.of(payer));
            when(userRepository.findAllById(any())).thenReturn(List.of(payer, user1, user2));

            ExpenseSplitStrategy mockStrategy = mock(ExpenseSplitStrategy.class);
            Map<UUID, BigDecimal> calculatedShares = new HashMap<>();
            calculatedShares.put(userId1, new BigDecimal("50.00"));
            calculatedShares.put(userId2, new BigDecimal("100.00"));
            
            when(mockStrategy.calculateShares(eq(new BigDecimal("150.00")), anyList())).thenReturn(calculatedShares);
            when(strategyFactory.getStrategy(ExpenseSplitType.EXACT_AMOUNT)).thenReturn(mockStrategy);
            when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ExpenseResponseDTO result = expenseService.createExpense(payerId, requestDTO);

            // Assert
            assertThat(result).isNotNull();
            verify(strategyFactory).getStrategy(ExpenseSplitType.EXACT_AMOUNT);
            verify(debtService).addDebt(eq(userId1), eq(payerId), eq(householdId), eq(new BigDecimal("50.00")));
            verify(debtService).addDebt(eq(userId2), eq(payerId), eq(householdId), eq(new BigDecimal("100.00")));
            verify(debtService, never()).addDebt(eq(payerId), any(), any(), any());
        }
    }

    @Nested
    class GetFilteredExpensesTests {

        @Test
        void getFilteredExpenses_withNullUserId_throwsIllegalArgumentException() {
            // Arrange
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                    householdId,
                    null, null, null
            );

            // Act & Assert
            assertThatThrownBy(() -> expenseService.getFilteredExpenses(null, filter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID must not be null");

            verifyNoInteractions(userRepository, expenseRepository);
        }

        @Test
        void getFilteredExpenses_withNullFilter_throwsIllegalArgumentException() {
            // Act & Assert
            assertThatThrownBy(() -> expenseService.getFilteredExpenses(userId1, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Filter DTO must not be null");

            verifyNoInteractions(userRepository, expenseRepository);
        }

        @Test
        void getFilteredExpenses_userNotFound_throwsIllegalArgumentException() {
            // Arrange
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                    householdId,
                    null, null, null
            );

            when(userRepository.findById(userId1)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> expenseService.getFilteredExpenses(userId1, filter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found with ID");

            verify(expenseRepository, never()).findAll(any(Specification.class));
        }

        @Test
        void getFilteredExpenses_withNoExpensesFound_returnsEmptyList() {
            // Arrange
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                    householdId,
                    UserTransactionRole.ALL,
                    null,
                    null
            );

            when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));
            Specification<Expense> spec = mock(Specification.class);

            // Act & Assert
            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildExpenseFilter(eq(userId1), eq(householdId), same(filter)))
                        .thenReturn(spec);
                
                when(expenseRepository.findAll(spec)).thenReturn(Collections.emptyList());

                List<ExpenseResponseDTO> result = expenseService.getFilteredExpenses(userId1, filter);

                assertThat(result).isNotNull();
                assertThat(result).isEmpty();
            }
        }

        @Test
        void getFilteredExpenses_usesUserHouseholdWhenFilterHouseholdMissing() {
            // Arrange
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                    null,
                    UserTransactionRole.ALL,
                    null,
                    "rent"
            );

            HouseholdMembership membership = new HouseholdMembership();
            membership.setHousehold(household);
            membership.setUser(user1);
            user1.setHouseholdMembership(membership);

            when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));
            Specification<Expense> spec = mock(Specification.class);
            when(expenseRepository.findAll(spec)).thenReturn(Collections.emptyList());

            // Act & Assert
            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildExpenseFilter(eq(userId1), eq(householdId), same(filter)))
                        .thenReturn(spec);

                List<ExpenseResponseDTO> result = expenseService.getFilteredExpenses(userId1, filter);

                assertThat(result).isEmpty();
                querySpec.verify(() -> QuerySpecification.buildExpenseFilter(eq(userId1), eq(householdId), same(filter)));
                verify(expenseRepository).findAll(spec);
            }
        }

        @Test
        void getFilteredExpenses_usesFilterHouseholdWhenProvided() {
            // Arrange
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                    householdId,
                    UserTransactionRole.CREDITOR,
                    null,
                    null
            );

            User userWithoutMembership = createUser(userId1, "Only", "Filter");
            userWithoutMembership.setHouseholdMembership(null);

            when(userRepository.findById(userId1)).thenReturn(Optional.of(userWithoutMembership));
            Specification<Expense> spec = mock(Specification.class);
            when(expenseRepository.findAll(spec)).thenReturn(Collections.emptyList());

            // Act & Assert
            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildExpenseFilter(eq(userId1), eq(householdId), same(filter)))
                        .thenReturn(spec);

                List<ExpenseResponseDTO> result = expenseService.getFilteredExpenses(userId1, filter);

                assertThat(result).isEmpty();
                querySpec.verify(() -> QuerySpecification.buildExpenseFilter(eq(userId1), eq(householdId), same(filter)));
                verify(expenseRepository).findAll(spec);
            }
        }

        @Test
        void getFilteredExpenses_mapsEntityToResponseDto() {
            // Arrange
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                    householdId,
                    UserTransactionRole.ALL,
                    null,
                    null
            );

            when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));

            Expense expense = new Expense(
                    "Internet Bill",
                    new BigDecimal("60.00"),
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            );
            UUID expenseId = UUID.randomUUID();
            expense.setId(expenseId);

            ExpenseShare payerShare = new ExpenseShare(expense, payer, new BigDecimal("30.00"));
            ExpenseShare debtorShare = new ExpenseShare(expense, user1, new BigDecimal("30.00"));
            payerShare.setId(UUID.randomUUID());
            debtorShare.setId(UUID.randomUUID());
            expense.setShares(new ArrayList<>(List.of(payerShare, debtorShare)));

            Specification<Expense> spec = mock(Specification.class);

            // Act & Assert
            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildExpenseFilter(eq(userId1), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(expenseRepository.findAll(spec)).thenReturn(List.of(expense));

                List<ExpenseResponseDTO> result = expenseService.getFilteredExpenses(userId1, filter);

                assertThat(result).hasSize(1);
                ExpenseResponseDTO response = result.get(0);
                assertThat(response.id()).isEqualTo(expenseId);
                assertThat(response.description()).isEqualTo("Internet Bill");
                assertThat(response.amount()).isEqualByComparingTo("60.00");
                assertThat(response.payerId()).isEqualTo(payerId);
                assertThat(response.payerFullName()).isEqualTo("John Payer");
                assertThat(response.householdId()).isEqualTo(householdId);
                assertThat(response.shares()).hasSize(2);
                
                assertThat(response.shares())
                        .extracting(share -> share.userId())
                        .containsExactlyInAnyOrder(payerId, userId1);
            }
        }
    }

    /**
     * Helper method to instantiate User entities cleanly within the test file.
     */
    private User createUser(UUID id, String name, String surname) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(name.toLowerCase() + "@test.com");
        user.setPassword("password");
        user.setId(id);
        return user;
    }
}