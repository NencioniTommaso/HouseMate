package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.expense.DebtRepository;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategyFactory;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategy;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import com.housemate.shared.dto.expense.response.UserNetOverviewResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.enums.ExpenseSplitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({ExpenseService.class, DebtService.class})
@DisplayName("ExpenseService Integration Tests")
@SuppressWarnings("null")
class ExpenseServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseService expenseService;

        @Autowired
        private DebtRepository debtRepository;

    @MockitoBean
    private ExpenseSplitStrategyFactory strategyFactory;

        @Test
        @DisplayName("createExpense persists expense graph and creates debt rows for non-payer shares")
        void createExpense_persistsExpenseAndCreatesDebtsForNonPayerShares() {
        Household household = persistHousehold("Main Household");
        User payer = persistUser("Alice", "Payer", "alice.int@test.com");
        User debtor = persistUser("Bob", "Debtor", "bob.int@test.com");
        persistMembership(household, payer, true);
        persistMembership(household, debtor, false);

        ExpenseSplitStrategy mockStrategy = mock(ExpenseSplitStrategy.class);
        when(strategyFactory.getStrategy(ExpenseSplitType.EXACT_AMOUNT)).thenReturn(mockStrategy);
        when(mockStrategy.calculateShares(eq(new BigDecimal("30.00")), anyList()))
            .thenReturn(Map.of(
                payer.getId(), new BigDecimal("10.00"),
                debtor.getId(), new BigDecimal("20.00")
            ));

        ExpenseCreateRequestDTO request = new ExpenseCreateRequestDTO(
            "Utilities",
            new BigDecimal("30.00"),
            ExpenseSplitType.EXACT_AMOUNT,
            List.of(
                new ExpenseShareRequestDTO(payer.getId(), new BigDecimal("10.00")),
                new ExpenseShareRequestDTO(debtor.getId(), new BigDecimal("20.00"))
            )
        );

        ExpenseResponseDTO result = expenseService.createExpense(payer.getId(), request);

        assertThat(result.id()).isNotNull();
        assertThat(result.shares()).hasSize(2);
        assertThat(debtRepository.findAll())
            .hasSize(1)
            .first()
            .satisfies(savedDebt -> {
                assertThat(savedDebt.getDebtor().getId()).isEqualTo(debtor.getId());
                assertThat(savedDebt.getCreditor().getId()).isEqualTo(payer.getId());
                assertThat(savedDebt.getHousehold().getId()).isEqualTo(household.getId());
                assertThat(savedDebt.getAmount()).isEqualByComparingTo("20.00");
            });
        }

    @Test
        @DisplayName("getCurrentMonthUserNetOverview returns true cash-flow and ignores expenses paid by others")
        void getCurrentMonthUserNetOverview_returnsTrueCashFlow() {
        LocalDateTime startOfCurrentMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        Household household = persistHousehold("Main Household");
        User userA = persistUser("Alice", "Payer", "alice.payer@test.com");
        User userB = persistUser("Bob", "Peer", "bob.peer@test.com");
        persistMembership(household, userA, true);
        persistMembership(household, userB, false);

        // User A pays an expense of 40.00 (included)
        Expense expensePaidByA = new Expense(
            "User A paid",
            new BigDecimal("40.00"),
            userA,
            household,
            ExpenseSplitType.EQUAL_SPLIT
        );
        expensePaidByA.setDate(startOfCurrentMonth.plusDays(1));
        entityManager.persistAndFlush(expensePaidByA);

        // User B pays an expense of 100.00 (must be ignored for User A cash flow)
        Expense expensePaidByB = new Expense(
            "User B paid",
            new BigDecimal("100.00"),
            userB,
            household,
            ExpenseSplitType.EQUAL_SPLIT
        );
        expensePaidByB.setDate(startOfCurrentMonth.plusDays(2));
        entityManager.persistAndFlush(expensePaidByB);

        // User A pays a settlement of 10.00 (included as cash out)
        Debt currentMonthDebt = entityManager.persistAndFlush(
            new Debt(userA, userB, household, new BigDecimal("10.00"))
        );
        Settlement includedSettlement = new Settlement(currentMonthDebt, userA, userB, new BigDecimal("10.00"), "Transfer");
        includedSettlement.setSettlementDate(startOfCurrentMonth.plusDays(2));
        entityManager.persistAndFlush(includedSettlement);

        entityManager.flush();
        entityManager.clear();

        UserNetOverviewResponseDTO result = expenseService.getCurrentMonthUserNetOverview(userA.getId());

        // Net Cash Flow = 40.00 (receipts paid) + 10.00 (settlements paid) - 0.00 (settlements received)
        assertThat(result.actualCashFlowAmount()).isEqualByComparingTo("50.00");
    }

    private Household persistHousehold(String name) {
        Household household = new Household();
        household.setName(name);
        return entityManager.persistAndFlush(household);
    }

    private User persistUser(String name, String surname, String email) {
        User user = new User(name, surname, email, "password");
        return entityManager.persistAndFlush(user);
    }

    private void persistMembership(Household household, User user, boolean isAdmin) {
        HouseholdMembership membership = new HouseholdMembership(household, user, isAdmin);
        user.setHouseholdMembership(membership);
        entityManager.persistAndFlush(membership);
    }
}
