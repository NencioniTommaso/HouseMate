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
class DebtRepositoryIntegrationTest {

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
}
