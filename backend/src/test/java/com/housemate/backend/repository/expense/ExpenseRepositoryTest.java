package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import com.housemate.shared.enums.ExpenseSplitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ExpenseRepository Monthly Aggregation Tests")
@SuppressWarnings("null")
class ExpenseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    @DisplayName("monthly sum/count query includes current month and excludes outside range")
    void monthlyAggregation_includesCurrentMonthOnly() {
        User payer = persistUser("payer@test.com");

        Household household = new Household();
        household.setName("Main Household");
        household = entityManager.persistAndFlush(household);

        Household otherHousehold = new Household();
        otherHousehold.setName("Other Household");
        otherHousehold = entityManager.persistAndFlush(otherHousehold);

        LocalDateTime startOfCurrentMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfCurrentMonth.plusMonths(1);

        // Included: exactly at start boundary
        persistExpense(payer, household, new BigDecimal("10.00"), startOfCurrentMonth);
        // Included: inside month
        persistExpense(payer, household, new BigDecimal("20.50"), startOfCurrentMonth.plusDays(3));
        // Excluded: exactly at end boundary (end is exclusive)
        persistExpense(payer, household, new BigDecimal("999.99"), startOfNextMonth);
        // Excluded: previous month
        persistExpense(payer, household, new BigDecimal("777.77"), startOfCurrentMonth.minusDays(1));
        // Excluded: different household
        persistExpense(payer, otherHousehold, new BigDecimal("555.55"), startOfCurrentMonth.plusDays(2));

        entityManager.flush();

        BigDecimal sum = expenseRepository.sumAmountByHouseholdIdForDateRange(
                household.getId(),
                startOfCurrentMonth,
                startOfNextMonth
        );
        long count = expenseRepository.countByHousehold_IdAndDateGreaterThanEqualAndDateLessThan(
                household.getId(),
                startOfCurrentMonth,
                startOfNextMonth
        );

        assertThat(sum).isEqualByComparingTo("30.50");
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("monthly sum/count query returns zero when no expenses in range")
    void monthlyAggregation_returnsZeroWhenNoMatchingExpenses() {
        User payer = persistUser("payer2@test.com");

        Household household = new Household();
        household.setName("Empty Month Household");
        household = entityManager.persistAndFlush(household);

        LocalDateTime startOfCurrentMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfCurrentMonth.plusMonths(1);

        persistExpense(payer, household, new BigDecimal("12.34"), startOfCurrentMonth.minusDays(2));
        persistExpense(payer, household, new BigDecimal("56.78"), startOfNextMonth.plusDays(1));

        entityManager.flush();

        BigDecimal sum = expenseRepository.sumAmountByHouseholdIdForDateRange(
                household.getId(),
                startOfCurrentMonth,
                startOfNextMonth
        );
        long count = expenseRepository.countByHousehold_IdAndDateGreaterThanEqualAndDateLessThan(
                household.getId(),
                startOfCurrentMonth,
                startOfNextMonth
        );

        assertThat(sum).isEqualByComparingTo("0");
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("sumTotalPaidByPayer aggregates only expenses where user is payer in date range")
    void sumTotalPaidByPayer_aggregatesOnlyPayerExpensesInRange() {
        User targetPayer = persistUser("target.payer@test.com");
        User otherPayer = persistUser("other.payer@test.com");

        Household household = new Household();
        household.setName("Cash Flow Household");
        household = entityManager.persistAndFlush(household);

        LocalDateTime startOfCurrentMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfCurrentMonth.plusMonths(1);

        // Included (target payer, in range)
        persistExpense(targetPayer, household, new BigDecimal("40.00"), startOfCurrentMonth.plusDays(1));
        // Excluded (different payer)
        persistExpense(otherPayer, household, new BigDecimal("100.00"), startOfCurrentMonth.plusDays(2));
        // Excluded (out of range)
        persistExpense(targetPayer, household, new BigDecimal("999.00"), startOfNextMonth);

        entityManager.flush();

        BigDecimal sum = expenseRepository.sumTotalPaidByPayer(
                targetPayer.getId(),
                household.getId(),
                startOfCurrentMonth,
                startOfNextMonth
        );

        assertThat(sum).isEqualByComparingTo("40.00");
    }

    @Test
    @DisplayName("sumTotalPaidByPayer returns null when no matching expenses")
    void sumTotalPaidByPayer_returnsNullWhenNoMatches() {
        User targetPayer = persistUser("none.payer@test.com");
        User otherPayer = persistUser("other2.payer@test.com");

        Household household = new Household();
        household.setName("No Match Household");
        household = entityManager.persistAndFlush(household);

        LocalDateTime startOfCurrentMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfCurrentMonth.plusMonths(1);

        persistExpense(otherPayer, household, new BigDecimal("10.00"), startOfCurrentMonth.plusDays(1));
        entityManager.flush();

        BigDecimal sum = expenseRepository.sumTotalPaidByPayer(
                targetPayer.getId(),
                household.getId(),
                startOfCurrentMonth,
                startOfNextMonth
        );

        assertThat(sum).isNull();
    }

    private User persistUser(String email) {
        User user = new User();
        user.setName("Test");
        user.setSurname("User");
        user.setEmail(email);
        user.setPassword("password");
        return entityManager.persistAndFlush(user);
    }

    private Expense persistExpense(User payer, Household household, BigDecimal amount, LocalDateTime date) {
        Expense expense = new Expense("Test Expense", amount, payer, household, ExpenseSplitType.EQUAL_SPLIT);
        expense.setDate(date);
        return entityManager.persist(expense);
    }
}
