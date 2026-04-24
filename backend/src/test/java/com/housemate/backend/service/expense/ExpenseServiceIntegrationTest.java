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
import com.housemate.shared.dto.expense.response.UserSettlementOverviewResponseDTO;
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
    @DisplayName("getCurrentMonthUserExpenseOverview sums settlements and payer-own-shares for current month only")
    void getCurrentMonthUserExpenseOverview_sumsSettlementsAndOwnPayerShares() {
        LocalDateTime startOfCurrentMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfCurrentMonth.plusMonths(1);

        Household household = persistHousehold("Main Household");
        User userA = persistUser("Alice", "Payer", "alice.payer@test.com");
        User userB = persistUser("Bob", "Peer", "bob.peer@test.com");
        persistMembership(household, userA, true);
        persistMembership(household, userB, false);

        // Included settlement amount for user A (debtor)
        Debt currentMonthDebt = entityManager.persistAndFlush(
                new Debt(userA, userB, household, new BigDecimal("30.00"))
        );
        Settlement includedSettlement = new Settlement(currentMonthDebt, userA, userB, new BigDecimal("30.00"), "Transfer");
        includedSettlement.setSettlementDate(startOfCurrentMonth.plusDays(2));
        entityManager.persistAndFlush(includedSettlement);

        // Excluded settlement (outside month)
        Debt oldDebt = entityManager.persistAndFlush(
                new Debt(userA, userB, household, new BigDecimal("99.00"))
        );
        Settlement excludedSettlement = new Settlement(oldDebt, userA, userB, new BigDecimal("99.00"), "Old transfer");
        excludedSettlement.setSettlementDate(startOfCurrentMonth.minusDays(1));
        entityManager.persistAndFlush(excludedSettlement);

        // Included own share: user A is payer and also has a share on this expense
        Expense includedExpense = new Expense(
                "Groceries",
                new BigDecimal("30.00"),
                userA,
                household,
                ExpenseSplitType.EQUAL_SPLIT
        );
        includedExpense.setDate(startOfCurrentMonth.plusDays(1));
        includedExpense = entityManager.persistAndFlush(includedExpense);
        entityManager.persistAndFlush(new ExpenseShare(includedExpense, userA, new BigDecimal("10.00")));
        entityManager.persistAndFlush(new ExpenseShare(includedExpense, userB, new BigDecimal("20.00")));

        // Excluded share: user A is payer but this expense is outside month (end boundary is exclusive)
        Expense boundaryExpense = new Expense(
                "Next month expense",
                new BigDecimal("20.00"),
                userA,
                household,
                ExpenseSplitType.EQUAL_SPLIT
        );
        boundaryExpense.setDate(startOfNextMonth);
        boundaryExpense = entityManager.persistAndFlush(boundaryExpense);
        entityManager.persistAndFlush(new ExpenseShare(boundaryExpense, userA, new BigDecimal("20.00")));

        // Excluded share: user A has a share but is not payer
        Expense notPayerExpense = new Expense(
                "Someone else paid",
                new BigDecimal("15.00"),
                userB,
                household,
                ExpenseSplitType.EXACT_AMOUNT
        );
        notPayerExpense.setDate(startOfCurrentMonth.plusDays(3));
        notPayerExpense = entityManager.persistAndFlush(notPayerExpense);
        entityManager.persistAndFlush(new ExpenseShare(notPayerExpense, userA, new BigDecimal("15.00")));

        entityManager.flush();
        entityManager.clear();

        UserSettlementOverviewResponseDTO result = expenseService.getCurrentMonthUserExpenseOverview(userA.getId());

        // 30.00 (included settlement) + 10.00 (included own share as payer) = 40.00
        assertThat(result.totalSettlementsMade()).isEqualByComparingTo("40.00");
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
