package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.enums.UserTransactionRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Settlement Specification Integration Tests")
@SuppressWarnings("null")
class SettlementSpecificationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SettlementRepository settlementRepository;

    @Test
    @DisplayName("buildSettlementFilter should return settlements where requester is debtor")
    void buildSettlementFilter_returnsDebtorSettlements() {
        SettlementFixture fixture = persistSettlementFixture();
        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.DEBTOR,
                null,
                null
        );

        Specification<Settlement> specification = QuerySpecification.buildSettlementFilter(
                Objects.requireNonNull(fixture.debtorId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        List<Settlement> results = settlementRepository.findAll(specification);

        assertThat(results)
                .extracting(Settlement::getId)
                .containsExactly(fixture.settlementId);
    }

    @Test
    @DisplayName("buildSettlementFilter should return settlements where requester is creditor")
    void buildSettlementFilter_returnsCreditorSettlements() {
        SettlementFixture fixture = persistSettlementFixture();
        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.CREDITOR,
                null,
                null
        );

        Specification<Settlement> specification = QuerySpecification.buildSettlementFilter(
                Objects.requireNonNull(fixture.creditorId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        List<Settlement> results = settlementRepository.findAll(specification);

        assertThat(results)
                .extracting(Settlement::getId)
                .containsExactly(fixture.settlementId);
    }

    @Test
    @DisplayName("buildSettlementFilter should apply case-insensitive description filter")
    void buildSettlementFilter_filtersByDescription() {
        SettlementFixture fixture = persistSettlementFixture();
        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.ALL,
                null,
                "transfer"
        );

        Specification<Settlement> specification = QuerySpecification.buildSettlementFilter(
                Objects.requireNonNull(fixture.debtorId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        List<Settlement> results = settlementRepository.findAll(specification);

        assertThat(results)
                .extracting(Settlement::getId)
                .containsExactly(fixture.settlementId);
    }

    @Test
    @DisplayName("buildSettlementFilter should exclude settlements from other households")
    void buildSettlementFilter_excludesOtherHouseholds() {
        SettlementFixture fixture = persistSettlementFixture();

        Household otherHousehold = new Household();
        otherHousehold.setName("Other House");
        otherHousehold = entityManager.persist(otherHousehold);

        User debtor = entityManager.find(User.class, fixture.debtorId);
        User creditor = entityManager.find(User.class, fixture.creditorId);
        Debt otherDebt = entityManager.persist(new Debt(debtor, creditor, otherHousehold, new BigDecimal("40.00")));
        Settlement otherSettlement = new Settlement(otherDebt, debtor, creditor, new BigDecimal("40.00"), "Other transfer");
        otherSettlement.setSettlementDate(LocalDateTime.now());
        entityManager.persist(otherSettlement);
        entityManager.flush();
        entityManager.clear();

        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.ALL,
                null,
                null
        );
        Specification<Settlement> specification = QuerySpecification.buildSettlementFilter(
                Objects.requireNonNull(fixture.debtorId),
                fixture.householdId,
                filter
        );

        List<Settlement> results = settlementRepository.findAll(specification);

        assertThat(results)
                .hasSize(1)
                .extracting(Settlement::getId)
                .containsExactly(fixture.settlementId);
    }

    private SettlementFixture persistSettlementFixture() {
        Household household = new Household();
        household.setName("Main House");
        household = entityManager.persist(household);

        User debtor = new User("Alice", "Debtor", "alice.spec.settle@test.com", "password");
        debtor = entityManager.persist(debtor);

        User creditor = new User("Bob", "Creditor", "bob.spec.settle@test.com", "password");
        creditor = entityManager.persist(creditor);

        Debt debt = new Debt(
                Objects.requireNonNull(debtor),
                Objects.requireNonNull(creditor),
                Objects.requireNonNull(household),
                new BigDecimal("25.00")
        );
        debt = entityManager.persist(debt);

        Settlement settlement = new Settlement(
                Objects.requireNonNull(debt),
                Objects.requireNonNull(debtor),
                Objects.requireNonNull(creditor),
                new BigDecimal("25.00"),
                "Bank transfer"
        );
        settlement = entityManager.persist(settlement);

        entityManager.flush();

        return new SettlementFixture(household.getId(), debtor.getId(), creditor.getId(), settlement.getId());
    }

    private record SettlementFixture(UUID householdId, UUID debtorId, UUID creditorId, UUID settlementId) {
    }
}
