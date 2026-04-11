package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Settlement Repository Integration Tests")
class SettlementRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SettlementRepository settlementRepository;

    @Test
    @DisplayName("sumAmountByDebtorIdAndHouseholdIdForDateRange should aggregate only matching settlements in range")
    void sumAmountByDebtorIdAndHouseholdIdForDateRange_shouldAggregateMatchingSettlements() {
        Household targetHousehold = persistHousehold("Target Household");
        Household otherHousehold = persistHousehold("Other Household");

        User targetDebtor = persistUser("Tina", "Target", "tina.target@test.com");
        User creditorA = persistUser("Alice", "Creditor", "alice.creditor@test.com");
        User creditorB = persistUser("Bob", "Creditor", "bob.creditor@test.com");
        User outsider = persistUser("Oscar", "Out", "oscar.out@test.com");

        LocalDateTime startDate = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime endDateExclusive = LocalDateTime.of(2026, 5, 1, 0, 0);

        // Included: exactly at start boundary
        persistSettlement(targetDebtor, creditorA, targetHousehold, new BigDecimal("10.00"), startDate);
        // Included: inside interval
        persistSettlement(targetDebtor, creditorB, targetHousehold, new BigDecimal("20.25"), startDate.plusDays(10));

        // Excluded: exactly at end boundary (exclusive)
        persistSettlement(targetDebtor, creditorA, targetHousehold, new BigDecimal("999.99"), endDateExclusive);
        // Excluded: before start
        persistSettlement(targetDebtor, creditorA, targetHousehold, new BigDecimal("777.77"), startDate.minusSeconds(1));
        // Excluded: different household
        persistSettlement(targetDebtor, outsider, otherHousehold, new BigDecimal("555.55"), startDate.plusDays(2));
        // Excluded: different debtor in target household
        persistSettlement(outsider, targetDebtor, targetHousehold, new BigDecimal("444.44"), startDate.plusDays(3));

        entityManager.flush();

        BigDecimal sum = settlementRepository.sumAmountByDebtorIdAndHouseholdIdForDateRange(
                targetDebtor.getId(),
                targetHousehold.getId(),
                startDate,
                endDateExclusive
        );

        assertThat(sum).isEqualByComparingTo("30.25");
    }

    @Test
    @DisplayName("sumAmountByDebtorIdAndHouseholdIdForDateRange should return zero when no matching rows")
    void sumAmountByDebtorIdAndHouseholdIdForDateRange_shouldReturnZeroWhenNoMatches() {
        Household household = persistHousehold("Empty Household");

        User targetDebtor = persistUser("Nina", "None", "nina.none@test.com");
        User userA = persistUser("Adam", "A", "adam.a@test.com");
        User userB = persistUser("Bella", "B", "bella.b@test.com");

        LocalDateTime startDate = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime endDateExclusive = LocalDateTime.of(2026, 5, 1, 0, 0);

        // Only unrelated settlement
        persistSettlement(userA, userB, household, new BigDecimal("15.00"), startDate.plusDays(5));

        entityManager.flush();

        BigDecimal sum = settlementRepository.sumAmountByDebtorIdAndHouseholdIdForDateRange(
                targetDebtor.getId(),
                household.getId(),
                startDate,
                endDateExclusive
        );

        assertThat(sum).isEqualByComparingTo(BigDecimal.ZERO);
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

    private Settlement persistSettlement(
            User debtor,
            User creditor,
            Household household,
            BigDecimal amount,
            LocalDateTime settlementDate) {
        Debt debt = new Debt(
                Objects.requireNonNull(debtor),
                Objects.requireNonNull(creditor),
                Objects.requireNonNull(household),
                Objects.requireNonNull(amount)
        );
        debt = entityManager.persistAndFlush(debt);

        Settlement settlement = new Settlement(
                Objects.requireNonNull(debt),
                Objects.requireNonNull(debtor),
                Objects.requireNonNull(creditor),
                Objects.requireNonNull(amount),
                "test settlement"
        );
        settlement.setSettlementDate(settlementDate);
        return entityManager.persistAndFlush(settlement);
    }
}
