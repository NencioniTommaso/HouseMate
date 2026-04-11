package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.enums.UserTransactionRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Debt Specification Integration Tests")
@SuppressWarnings("null")
class DebtSpecificationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DebtRepository debtRepository;

    @Test
    @DisplayName("buildDebtFilter should return debts where user is the DEBTOR")
    void buildDebtFilter_shouldReturnDebt_whenUserIsDebtor() {
        // Arrange
        DebtFixture fixture = persistDebtFixture();
        DebtFilterRequestDTO filter = new DebtFilterRequestDTO(
                UserTransactionRole.DEBTOR, 
                null // No specific creditor
        );
        
        Specification<Debt> specification = QuerySpecification.buildDebtFilter(
                Objects.requireNonNull(fixture.debtorId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Debt> results = debtRepository.findAll(specification);

        // Assert
        assertThat(results)
                .extracting(Debt::getId)
                .containsExactly(fixture.debtId);
    }

    @Test
    @DisplayName("buildDebtFilter should return debts where user is the CREDITOR")
    void buildDebtFilter_shouldReturnDebt_whenUserIsCreditor() {
        // Arrange
        DebtFixture fixture = persistDebtFixture();
        DebtFilterRequestDTO filter = new DebtFilterRequestDTO(
                UserTransactionRole.CREDITOR,
                null // No specific debtor
        );

        Specification<Debt> specification = QuerySpecification.buildDebtFilter(
                Objects.requireNonNull(fixture.creditorId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Debt> results = debtRepository.findAll(specification);

        // Assert
        assertThat(results)
                .extracting(Debt::getId)
                .containsExactly(fixture.debtId);
    }

    @Test
    @DisplayName("buildDebtFilter should filter by specific involved user (creditor)")
    void buildDebtFilter_shouldReturnDebt_whenUserIsDebtorAndInvolvedIdMatches() {
        // Arrange
        DebtFixture fixture = persistDebtFixture();
        
        // We want debts where Derek (Debtor) owes specifically Cora (Creditor)
        DebtFilterRequestDTO filter = new DebtFilterRequestDTO(
                UserTransactionRole.DEBTOR,
                fixture.creditorId // involvedId is Cora
        );

        Specification<Debt> specification = QuerySpecification.buildDebtFilter(
                Objects.requireNonNull(fixture.debtorId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Debt> results = debtRepository.findAll(specification);

        // Assert
        assertThat(results)
                .extracting(Debt::getId)
                .containsExactly(fixture.debtId);
    }

    @Test
    @DisplayName("buildDebtFilter should return empty when involved user does not match")
    void buildDebtFilter_shouldReturnEmpty_whenInvolvedIdDoesNotMatch() {
        // Arrange
        DebtFixture fixture = persistDebtFixture();
        UUID randomThirdPartyId = UUID.randomUUID(); // Someone Derek doesn't owe
        
        DebtFilterRequestDTO filter = new DebtFilterRequestDTO(
                UserTransactionRole.DEBTOR,
                randomThirdPartyId 
        );

        Specification<Debt> specification = QuerySpecification.buildDebtFilter(
                Objects.requireNonNull(fixture.debtorId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Debt> results = debtRepository.findAll(specification);

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("buildDebtFilter should return debt when user is CREDITOR and involvedId (debtor) matches")
    void buildDebtFilter_shouldReturnDebt_whenUserIsCreditorAndInvolvedIdMatches() {
        // Arrange
        DebtFixture fixture = persistDebtFixture();
        
        // Cora (Creditor) wants to see debts where specifically Derek (Debtor) owes her
        DebtFilterRequestDTO filter = new DebtFilterRequestDTO(
                UserTransactionRole.CREDITOR,
                fixture.debtorId // involvedId is Derek
        );

        Specification<Debt> specification = QuerySpecification.buildDebtFilter(
                Objects.requireNonNull(fixture.creditorId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Debt> results = debtRepository.findAll(specification);

        // Assert
        assertThat(results)
                .extracting(Debt::getId)
                .containsExactly(fixture.debtId);
    }

    @Test
    @DisplayName("buildDebtFilter should return empty when user is CREDITOR and involvedId does not match")
    void buildDebtFilter_shouldReturnEmpty_whenUserIsCreditorAndInvolvedIdDoesNotMatch() {
        // Arrange
        DebtFixture fixture = persistDebtFixture();
        UUID randomThirdPartyId = UUID.randomUUID(); // Someone who doesn't owe Cora
        
        DebtFilterRequestDTO filter = new DebtFilterRequestDTO(
                UserTransactionRole.CREDITOR,
                randomThirdPartyId 
        );

        Specification<Debt> specification = QuerySpecification.buildDebtFilter(
                Objects.requireNonNull(fixture.creditorId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Debt> results = debtRepository.findAll(specification);

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("buildDebtFilter MUST exclude debts that belong to a different household")
    void buildDebtFilter_shouldExcludeDebtsFromOtherHouseholds() {
        // Arrange
        DebtFixture fixture = persistDebtFixture();
        
        // Create a SECOND household
        Household secondHousehold = new Household();
        secondHousehold.setName("Vacation House");
        secondHousehold = entityManager.persist(secondHousehold);

        // Derek and Cora are also in the second household, and Derek owes Cora there too
        User debtor = entityManager.find(User.class, fixture.debtorId);
        User creditor = entityManager.find(User.class, fixture.creditorId);
        
        Debt secondDebt = new Debt(debtor, creditor, secondHousehold, new BigDecimal("100.00"));
        entityManager.persist(secondDebt);
        entityManager.flush();
        entityManager.clear();

        // Filter request targeting the FIRST household only
        DebtFilterRequestDTO filter = new DebtFilterRequestDTO(
                UserTransactionRole.DEBTOR,
                null
        );

        Specification<Debt> specification = QuerySpecification.buildDebtFilter(
                Objects.requireNonNull(fixture.debtorId),
                fixture.householdId, // Specifically querying the FIRST household
                filter
        );

        // Act
        List<Debt> results = debtRepository.findAll(specification);

        // Assert
        // It should ONLY return the $45.50 debt from the first household, NOT the $100.00 one.
        assertThat(results)
                .hasSize(1)
                .extracting(Debt::getId)
                .containsExactly(fixture.debtId);
    }

    // --- Helper Fixture Builder ---

    private DebtFixture persistDebtFixture() {
        Household household = new Household();
        household.setName("Debt House");
        household = entityManager.persist(household);

        User debtor = new User("Derek", "Debtor", "derek.debtor@test.com", "password");
        debtor = entityManager.persist(debtor);

        User creditor = new User("Cora", "Creditor", "cora.creditor@test.com", "password");
        creditor = entityManager.persist(creditor);

        // Derek owes Cora $45.50
        Debt debt = new Debt(
                Objects.requireNonNull(debtor),
                Objects.requireNonNull(creditor),
                Objects.requireNonNull(household),
                new BigDecimal("45.50")
        );
        debt = entityManager.persist(debt);

        entityManager.flush();

        return new DebtFixture(household.getId(), debtor.getId(), creditor.getId(), debt.getId());
    }

    private record DebtFixture(UUID householdId, UUID debtorId, UUID creditorId, UUID debtId) {
    }
}