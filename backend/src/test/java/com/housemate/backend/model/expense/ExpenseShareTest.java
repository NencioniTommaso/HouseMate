package com.housemate.backend.model.expense;

import com.housemate.backend.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpenseShareTest {

    private Expense expense;
    private User user;

    @BeforeEach
    void setUp() {
        expense = mock(Expense.class);
        when(expense.getId()).thenReturn(UUID.randomUUID());

        user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getName()).thenReturn("John");
    }

    @Nested
    class ConstructorTests {

        // --- Happy Path ---

        @Test
        void constructor_withValidArguments_createsExpenseShare() {
            // Execute
            ExpenseShare share = new ExpenseShare(expense, user, new BigDecimal("50.00"));

            // Assert
            assertThat(share).isNotNull();
            assertThat(share.getExpense()).isEqualTo(expense);
            assertThat(share.getUser()).isEqualTo(user);
            assertThat(share.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        void constructor_withSmallAmount_createsExpenseShare() {
            // Execute
            ExpenseShare share = new ExpenseShare(expense, user, new BigDecimal("0.01"));

            // Assert
            assertThat(share.getAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
        }

        @Test
        void constructor_withLargeAmount_createsExpenseShare() {
            // Execute
            ExpenseShare share = new ExpenseShare(expense, user, new BigDecimal("9999999.99"));

            // Assert
            assertThat(share.getAmount()).isEqualByComparingTo(new BigDecimal("9999999.99"));
        }

        @Test
        void constructor_withPreciseDecimalAmount_createsExpenseShare() {
            // Execute
            ExpenseShare share = new ExpenseShare(expense, user, new BigDecimal("33.33"));

            // Assert
            assertThat(share.getAmount()).isEqualByComparingTo(new BigDecimal("33.33"));
        }

        // --- Null Validation ---

        @Test
        void constructor_nullExpense_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new ExpenseShare(null, user, new BigDecimal("50.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense cannot be null");
        }

        @Test
        void constructor_nullUser_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new ExpenseShare(expense, null, new BigDecimal("50.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User cannot be null");
        }

        @Test
        void constructor_nullAmount_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new ExpenseShare(expense, user, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense share amount cannot be null");
        }

        // --- Amount Validation ---

        @Test
        void constructor_zeroAmount_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new ExpenseShare(expense, user, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense share amount must be strictly greater than zero");
        }

        @Test
        void constructor_negativeAmount_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new ExpenseShare(expense, user, new BigDecimal("-50.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense share amount must be strictly greater than zero");
        }
    }

    @Nested
    class PropertyTests {

        @Test
        void gettersReturnConstructorValues() {
            // Setup
            BigDecimal amount = new BigDecimal("37.25");

            // Execute
            ExpenseShare share = new ExpenseShare(expense, user, amount);

            // Assert
            assertThat(share.getExpense()).isEqualTo(expense);
            assertThat(share.getUser()).isEqualTo(user);
            assertThat(share.getAmount()).isEqualByComparingTo(amount);
        }

        @Test
        void settersAllowModifyingAmount() {
            // Setup
            ExpenseShare share = new ExpenseShare(expense, user, new BigDecimal("100.00"));

            // Execute
            share.setAmount(new BigDecimal("75.00"));

            // Assert
            assertThat(share.getAmount()).isEqualByComparingTo(new BigDecimal("75.00"));
        }

        @Test
        void shareIsInitiallyWithoutId() {
            // Execute
            ExpenseShare share = new ExpenseShare(expense, user, new BigDecimal("50.00"));

            // Assert
            assertThat(share.getId()).isNull();  // ID is only set by JPA
        }
    }
}
