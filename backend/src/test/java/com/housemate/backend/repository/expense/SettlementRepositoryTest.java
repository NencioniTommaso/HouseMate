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
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Settlement Repository Integration Tests")
@SuppressWarnings("null")
class SettlementRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private DebtRepository debtRepository;

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

    @Test
    @DisplayName("DB Schema: Should throw DataIntegrityViolationException to prevent hard-delete of a linked Debt")
    void databaseSchema_preventsHardDeleteOfLinkedDebt() {
        // Arrange
        Household household = persistHousehold("Constraint Test House");
        User debtor = persistUser("Frank", "Debtor", "frank.debtor@test.com");
        User creditor = persistUser("Grace", "Creditor", "grace.creditor@test.com");

        Settlement settlement = persistSettlement(
                debtor, creditor, household, new BigDecimal("50.00"), LocalDateTime.now()
        );

        // Salviamo l'ID prima di pulire la cache
        UUID debtId = settlement.getDebt().getId();
        
        // FIX: Svuotiamo la cache di Hibernate (Level 1 Cache). 
        // Questo scollega il Settlement dalla memoria e simula una nuova richiesta HTTP pulita.
        entityManager.clear();

        // Act & Assert
        assertThatThrownBy(() -> {
            // Usiamo deleteById per ricaricare il debito pulito e provare a cancellarlo
            debtRepository.deleteById(debtId);
            
            // Forza la query DELETE verso il database H2
            debtRepository.flush(); 
        })
        .isInstanceOf(DataIntegrityViolationException.class);;
    }

    @Test
    @DisplayName("Soft Delete: Should allow updating a Debt amount to zero even when referenced by a Settlement")
    void softDelete_allowsUpdatingDebtToZero() {
        // Arrange
        Household household = persistHousehold("Update Test House");
        User debtor = persistUser("Henry", "Debtor", "henry.debtor@test.com");
        User creditor = persistUser("Ivy", "Creditor", "ivy.creditor@test.com");

        Settlement settlement = persistSettlement(
                debtor, creditor, household, new BigDecimal("50.00"), LocalDateTime.now()
        );
        Debt linkedDebt = settlement.getDebt();

        // Act: Simuliamo esattamente ciò che fa ora il nostro SettlementService
        linkedDebt.setAmount(BigDecimal.ZERO);
        debtRepository.saveAndFlush(linkedDebt); // Forza la query UPDATE

        // Assert: Il database deve accettare l'aggiornamento senza lanciare eccezioni
        Debt updatedDebt = debtRepository.findById(linkedDebt.getId()).orElseThrow();
        assertThat(updatedDebt.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        
        // Verifichiamo che il settlement esista ancora e punti al debito corretto
        Settlement savedSettlement = settlementRepository.findById(settlement.getId()).orElseThrow();
        assertThat(savedSettlement.getDebt().getId()).isEqualTo(linkedDebt.getId());
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
