package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Debt Repository Integration Tests")
class DebtRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DebtRepository debtRepository;

    @Test
    @DisplayName("findByDebtorAndCreditorAndHousehold should retrieve persisted debt")
    void findByDebtorAndCreditorAndHousehold_shouldReturnDebt_whenEntityExists() {
        // Arrange
        Household household = new Household();
        household.setName("Debt House");
        household = entityManager.persist(household);

        User debtor = new User("Derek", "Debtor", "derek.debtor@test.com", "password");
        debtor = entityManager.persist(debtor);

        User creditor = new User("Cora", "Creditor", "cora.creditor@test.com", "password");
        creditor = entityManager.persist(creditor);

        Debt debt = new Debt(
            Objects.requireNonNull(debtor),
            Objects.requireNonNull(creditor),
            Objects.requireNonNull(household),
            new BigDecimal("45.50")
        );
        debt = entityManager.persist(debt);

        entityManager.flush();
        entityManager.clear();

        User persistedDebtor = entityManager.find(User.class, debtor.getId());
        User persistedCreditor = entityManager.find(User.class, creditor.getId());
        Household persistedHousehold = entityManager.find(Household.class, household.getId());

        // Act
        Optional<Debt> result = debtRepository.findByDebtorAndCreditorAndHousehold(
                persistedDebtor,
                persistedCreditor,
                persistedHousehold
        );

        // Assert
        assertThat(result)
                .isPresent()
                .get()
                .extracting(Debt::getId)
                .isEqualTo(debt.getId());
    }

            @Test
            @DisplayName("sum queries should aggregate only matching household/user debts")
            void sumQueries_shouldAggregateOnlyMatchingDebts() {
            Household targetHousehold = new Household();
            targetHousehold.setName("Target Household");
            targetHousehold = entityManager.persistAndFlush(targetHousehold);

            Household otherHousehold = new Household();
            otherHousehold.setName("Other Household");
            otherHousehold = entityManager.persistAndFlush(otherHousehold);

            User targetUser = new User("Tina", "Target", "tina.target@test.com", "password");
            targetUser = entityManager.persistAndFlush(targetUser);

            User userA = new User("Alice", "A", "alice.a@test.com", "password");
            userA = entityManager.persistAndFlush(userA);

            User userB = new User("Bob", "B", "bob.b@test.com", "password");
            userB = entityManager.persistAndFlush(userB);

            User userC = new User("Cora", "C", "cora.c@test.com", "password");
            userC = entityManager.persistAndFlush(userC);

            // Included in total owed by targetUser (debtor in target household)
            entityManager.persistAndFlush(new Debt(
                Objects.requireNonNull(targetUser),
                Objects.requireNonNull(userA),
                Objects.requireNonNull(targetHousehold),
                new BigDecimal("10.00")
            ));
            entityManager.persistAndFlush(new Debt(
                Objects.requireNonNull(targetUser),
                Objects.requireNonNull(userB),
                Objects.requireNonNull(targetHousehold),
                new BigDecimal("20.50")
            ));

            // Included in total owed to targetUser (creditor in target household)
            entityManager.persistAndFlush(new Debt(
                Objects.requireNonNull(userA),
                Objects.requireNonNull(targetUser),
                Objects.requireNonNull(targetHousehold),
                new BigDecimal("7.25")
            ));
            entityManager.persistAndFlush(new Debt(
                Objects.requireNonNull(userB),
                Objects.requireNonNull(targetUser),
                Objects.requireNonNull(targetHousehold),
                new BigDecimal("2.75")
            ));

            // Excluded: same user but different household
            entityManager.persistAndFlush(new Debt(
                Objects.requireNonNull(targetUser),
                Objects.requireNonNull(userC),
                Objects.requireNonNull(otherHousehold),
                new BigDecimal("999.99")
            ));
            entityManager.persistAndFlush(new Debt(
                Objects.requireNonNull(userC),
                Objects.requireNonNull(targetUser),
                Objects.requireNonNull(otherHousehold),
                new BigDecimal("888.88")
            ));

            // Excluded: target household but target user not involved
            entityManager.persistAndFlush(new Debt(
                Objects.requireNonNull(userA),
                Objects.requireNonNull(userB),
                Objects.requireNonNull(targetHousehold),
                new BigDecimal("777.77")
            ));

            entityManager.flush();

            BigDecimal totalOwedByTargetUser = debtRepository.sumAmountByDebtorIdAndHouseholdId(
                targetUser.getId(),
                targetHousehold.getId()
            );
            BigDecimal totalOwedToTargetUser = debtRepository.sumAmountByCreditorIdAndHouseholdId(
                targetUser.getId(),
                targetHousehold.getId()
            );

            assertThat(totalOwedByTargetUser).isEqualByComparingTo("30.50");
            assertThat(totalOwedToTargetUser).isEqualByComparingTo("10.00");
            }

            @Test
            @DisplayName("sum queries should return zero when no matching debts exist")
            void sumQueries_shouldReturnZeroWhenNoMatchingDebts() {
            Household household = new Household();
            household.setName("Empty Household");
            household = entityManager.persistAndFlush(household);

            User targetUser = new User("Nina", "None", "nina.none@test.com", "password");
            targetUser = entityManager.persistAndFlush(targetUser);

            User otherUserA = new User("Olly", "Other", "olly.other@test.com", "password");
            otherUserA = entityManager.persistAndFlush(otherUserA);

            User otherUserB = new User("Pia", "Peer", "pia.peer@test.com", "password");
            otherUserB = entityManager.persistAndFlush(otherUserB);

            entityManager.persistAndFlush(new Debt(
                Objects.requireNonNull(otherUserA),
                Objects.requireNonNull(otherUserB),
                Objects.requireNonNull(household),
                new BigDecimal("15.00")
            ));

            entityManager.flush();

            BigDecimal totalOwedByTargetUser = debtRepository.sumAmountByDebtorIdAndHouseholdId(
                targetUser.getId(),
                household.getId()
            );
            BigDecimal totalOwedToTargetUser = debtRepository.sumAmountByCreditorIdAndHouseholdId(
                targetUser.getId(),
                household.getId()
            );

            assertThat(totalOwedByTargetUser).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(totalOwedToTargetUser).isEqualByComparingTo(BigDecimal.ZERO);
            }
}
