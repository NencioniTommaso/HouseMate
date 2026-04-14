package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.enums.ExpenseSplitType;
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
@DisplayName("Expense Specification Integration Tests")
class ExpenseSpecificationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    @DisplayName("buildExpenseFilter should return expense for creditor (payer) Alice")
    void buildExpenseFilter_shouldReturnExpense_whenRoleIsCreditorForPayer() {
        // Arrange
        ExpenseFixture fixture = persistExpenseFixture();
        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.CREDITOR,
                null,
                null
        );
        Specification<Expense> specification = QuerySpecification.buildExpenseFilter(
                Objects.requireNonNull(fixture.aliceId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Expense> results = expenseRepository.findAll(specification);

        // Assert
        assertThat(results)
                .extracting(Expense::getId)
                .containsExactly(fixture.expenseId);
    }

    @Test
    @DisplayName("buildExpenseFilter should return expense for debtor Bob")
    void buildExpenseFilter_shouldReturnExpense_whenRoleIsDebtorForShareUser() {
        // Arrange
        ExpenseFixture fixture = persistExpenseFixture();
        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.DEBTOR,
                null,
                null
        );
        Specification<Expense> specification = QuerySpecification.buildExpenseFilter(
                Objects.requireNonNull(fixture.bobId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Expense> results = expenseRepository.findAll(specification);

        // Assert
        assertThat(results)
                .extracting(Expense::getId)
                .containsExactly(fixture.expenseId);
    }

    @Test
    @DisplayName("buildExpenseFilter should exclude payer from debtor role even if payer has an expense share")
    void buildExpenseFilter_shouldExcludePayer_whenRoleIsDebtor() {
        // Arrange
        ExpenseFixture fixture = persistExpenseFixture();
        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.DEBTOR,
                null,
                null
        );
        Specification<Expense> specification = QuerySpecification.buildExpenseFilter(
                Objects.requireNonNull(fixture.aliceId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Expense> results = expenseRepository.findAll(specification);

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("buildExpenseFilter should return expense once for ALL role when user is payer and share participant")
    void buildExpenseFilter_shouldReturnDistinctExpense_whenRoleIsAll() {
        // Arrange
        ExpenseFixture fixture = persistExpenseFixture();
        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.ALL,
                null,
                null
        );
        Specification<Expense> specification = QuerySpecification.buildExpenseFilter(
                Objects.requireNonNull(fixture.aliceId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Expense> results = expenseRepository.findAll(specification);

        // Assert
        assertThat(results)
                .extracting(Expense::getId)
                .containsExactly(fixture.expenseId);
    }

    @Test
    @DisplayName("buildExpenseFilter should return expense when description matches partially (case-insensitive)")
    void buildExpenseFilter_shouldReturnExpense_whenDescriptionMatches() {
        // Arrange
        ExpenseFixture fixture = persistExpenseFixture();
        // The original description is "Groceries"
        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.ALL,
                null,
                "grocer" // Partial, lower-case search
        );
        Specification<Expense> specification = QuerySpecification.buildExpenseFilter(
                Objects.requireNonNull(fixture.aliceId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Expense> results = expenseRepository.findAll(specification);

        // Assert
        assertThat(results)
                .extracting(Expense::getId)
                .containsExactly(fixture.expenseId);
    }

    @Test
    @DisplayName("buildExpenseFilter should return empty when description does not match")
    void buildExpenseFilter_shouldReturnEmpty_whenDescriptionDoesNotMatch() {
        // Arrange
        ExpenseFixture fixture = persistExpenseFixture();
        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.ALL,
                null,
                "vacation"  // Does not match "Groceries"
        );
        Specification<Expense> specification = QuerySpecification.buildExpenseFilter(
                Objects.requireNonNull(fixture.aliceId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Expense> results = expenseRepository.findAll(specification);

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("buildExpenseFilter should exclude expenses from other households")
    void buildExpenseFilter_shouldExcludeExpensesFromOtherHouseholds() {
        // Arrange
        ExpenseFixture fixture = persistExpenseFixture();
        
        // Create a completely separate household and expense
        Household otherHousehold = new Household();
        otherHousehold.setName("Other House");
        otherHousehold = entityManager.persist(otherHousehold);

        User alice = entityManager.find(User.class, fixture.aliceId);

        Expense otherExpense = new Expense(
                "Other Groceries",
                new BigDecimal("50.00"),
                Objects.requireNonNull(alice),
                Objects.requireNonNull(otherHousehold),
                ExpenseSplitType.EXACT_AMOUNT
        );
        entityManager.persist(otherExpense);
        entityManager.flush();
        entityManager.clear();

        // We specifically request the FIRST household
        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId, 
                UserTransactionRole.CREDITOR,
                null,
                null
        );
        
        Specification<Expense> specification = QuerySpecification.buildExpenseFilter(
                Objects.requireNonNull(fixture.aliceId),
                fixture.householdId, // Testing isolation here
                filter
        );

        // Act
        List<Expense> results = expenseRepository.findAll(specification);

        // Assert - Should only return the expense from the first household, not 'otherExpense'
        assertThat(results)
                .hasSize(1)
                .extracting(Expense::getId)
                .containsExactly(fixture.expenseId);
    }

    @Test
    @DisplayName("buildExpenseFilter should filter correctly by date range")
    void buildExpenseFilter_shouldFilterByDateRange() {
        // Arrange
        ExpenseFixture fixture = persistExpenseFixture();
        
        // Because the Expense constructor sets the date to LocalDateTime.now(), 
        // a range spanning from yesterday to tomorrow will safely capture it.
        java.time.LocalDateTime yesterday = java.time.LocalDateTime.now().minusDays(1);
        java.time.LocalDateTime tomorrow = java.time.LocalDateTime.now().plusDays(1);
        
        com.housemate.shared.utils.types.DateRange range = 
            new com.housemate.shared.utils.types.DateRange(yesterday, tomorrow);

        TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(
                fixture.householdId,
                UserTransactionRole.ALL,
                range,
                null
        );
        Specification<Expense> specification = QuerySpecification.buildExpenseFilter(
                Objects.requireNonNull(fixture.aliceId),
                fixture.householdId,
                filter
        );

        entityManager.clear();

        // Act
        List<Expense> results = expenseRepository.findAll(specification);

        // Assert
        assertThat(results)
                .extracting(Expense::getId)
                .containsExactly(fixture.expenseId);
    }

    private ExpenseFixture persistExpenseFixture() {
        Household household = new Household();
        household.setName("Scholar House");
        household = entityManager.persist(household);

        User alice = new User("Alice", "Payer", "alice.payer@test.com", "password");
        alice = entityManager.persist(alice);

        User bob = new User("Bob", "Debtor", "bob.debtor@test.com", "password");
        bob = entityManager.persist(bob);

        Expense expense = new Expense(
                "Groceries",
                new BigDecimal("100.00"),
                Objects.requireNonNull(alice),
                Objects.requireNonNull(household),
                ExpenseSplitType.EXACT_AMOUNT
        );
        expense = entityManager.persist(expense);

        // Bob owes money for Alice's expense.
        ExpenseShare bobShare = new ExpenseShare(
                Objects.requireNonNull(expense),
                Objects.requireNonNull(bob),
                new BigDecimal("30.00")
        );
        entityManager.persist(bobShare);

        // Alice is intentionally persisted as a share too, to validate payer exclusion in DEBTOR filter.
        ExpenseShare aliceShare = new ExpenseShare(
                Objects.requireNonNull(expense),
                Objects.requireNonNull(alice),
                new BigDecimal("70.00")
        );
        entityManager.persist(aliceShare);

        entityManager.flush();

        return new ExpenseFixture(household.getId(), alice.getId(), bob.getId(), expense.getId());
    }

    private record ExpenseFixture(UUID householdId, UUID aliceId, UUID bobId, UUID expenseId) {
    }
}
