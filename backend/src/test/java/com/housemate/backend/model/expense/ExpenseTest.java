package com.housemate.backend.model.expense;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import com.housemate.shared.enums.ExpenseSplitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpenseTest {

    private User payer;
    private Household household;

    @BeforeEach
    void setUp() {
        payer = mock(User.class);
        when(payer.getName()).thenReturn("John");

        household = mock(Household.class);
    }

    @Nested
    class ConstructorTests {

        @Test
        void constructor_withValidArguments_createsExpense() {
            Expense expense = new Expense(
                    "Groceries",
                    new BigDecimal("50.00"),
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            );

            assertThat(expense).isNotNull();
            assertThat(expense.getDescription()).isEqualTo("Groceries");
            assertThat(expense.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(expense.getPayer()).isEqualTo(payer);
            assertThat(expense.getHousehold()).isEqualTo(household);
            assertThat(expense.getSplitType()).isEqualTo(ExpenseSplitType.EQUAL_SPLIT);
            assertThat(expense.getDate()).isNotNull();
        }

        @Test
        void constructor_setsDateToCurrentTime_withinAcceptableRange() {
            LocalDateTime beforeCreation = LocalDateTime.now();

            Expense expense = new Expense(
                    "Test",
                    new BigDecimal("100.00"),
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            );

            LocalDateTime afterCreation = LocalDateTime.now();

            assertThat(expense.getDate()).isAfterOrEqualTo(beforeCreation);
            assertThat(expense.getDate()).isBeforeOrEqualTo(afterCreation.plusSeconds(1));
        }

        @Test
        void constructor_supportsAllSplitTypes() {
            Expense equalExpense = new Expense("Equal", new BigDecimal("100.00"), payer, household, ExpenseSplitType.EQUAL_SPLIT);
            Expense sharesExpense = new Expense("Shares", new BigDecimal("100.00"), payer, household, ExpenseSplitType.SHARES);
            Expense exactExpense = new Expense("Exact", new BigDecimal("100.00"), payer, household, ExpenseSplitType.EXACT_AMOUNT);
            Expense adjustmentExpense = new Expense("Adjustment", new BigDecimal("100.00"), payer, household, ExpenseSplitType.ADJUSTMENT);

            assertThat(equalExpense.getSplitType()).isEqualTo(ExpenseSplitType.EQUAL_SPLIT);
            assertThat(sharesExpense.getSplitType()).isEqualTo(ExpenseSplitType.SHARES);
            assertThat(exactExpense.getSplitType()).isEqualTo(ExpenseSplitType.EXACT_AMOUNT);
            assertThat(adjustmentExpense.getSplitType()).isEqualTo(ExpenseSplitType.ADJUSTMENT);
        }

        @Test
        void constructor_withSmallAmount_createsExpense() {
            Expense expense = new Expense(
                    "Small Expense",
                    new BigDecimal("0.01"),
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            );

            assertThat(expense.getAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
        }

        @Test
        void constructor_withLargeAmount_createsExpense() {
            Expense expense = new Expense(
                    "Large Expense",
                    new BigDecimal("9999999.99"),
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            );

            assertThat(expense.getAmount()).isEqualByComparingTo(new BigDecimal("9999999.99"));
        }

        @Test
        void constructor_nullDescription_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new Expense(
                    null,
                    new BigDecimal("50.00"),
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Description cannot be null");
        }

        @Test
        void constructor_blankDescription_keepsProvidedValue() {
            Expense expense = new Expense(
                    "",
                    new BigDecimal("50.00"),
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            );

            assertThat(expense.getDescription()).isEmpty();
        }

        @Test
        void constructor_nullAmount_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new Expense(
                    "Groceries",
                    null,
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense amount cannot be null");
        }

        @Test
        void constructor_nullPayer_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new Expense(
                    "Groceries",
                    new BigDecimal("50.00"),
                    null,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payer cannot be null");
        }

        @Test
        void constructor_nullHousehold_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new Expense(
                    "Groceries",
                    new BigDecimal("50.00"),
                    payer,
                    null,
                    ExpenseSplitType.EQUAL_SPLIT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Household cannot be null");
        }

        @Test
        void constructor_nullSplitType_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new Expense(
                    "Groceries",
                    new BigDecimal("50.00"),
                    payer,
                    household,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Split type cannot be null");
        }

        @Test
        void constructor_zeroAmount_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new Expense(
                    "Groceries",
                    BigDecimal.ZERO,
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense amount must be strictly greater than zero");
        }

        @Test
        void constructor_negativeAmount_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new Expense(
                    "Groceries",
                    new BigDecimal("-50.00"),
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense amount must be strictly greater than zero");
        }
    }

    @Nested
    class InitializationTests {

        @Test
        void constructor_initializesSharesListEmpty() {
            Expense expense = new Expense(
                    "Test",
                    new BigDecimal("10.00"),
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            );

            assertThat(expense.getShares()).isNotNull().isEmpty();
        }

        @Test
        void constructor_allowsAddingShares() {
            Expense expense = new Expense(
                    "Test",
                    new BigDecimal("10.00"),
                    payer,
                    household,
                    ExpenseSplitType.EQUAL_SPLIT
            );

            ExpenseShare share = new ExpenseShare(expense, payer, new BigDecimal("5.00"));
            expense.getShares().add(share);

            assertThat(expense.getShares()).hasSize(1);
            assertThat(expense.getShares().get(0)).isEqualTo(share);
        }
    }
}
