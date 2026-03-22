package com.housemate.backend.model.expense;

import com.housemate.backend.model.household.Household;
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

class DebtTest {

    private User debtor;
    private User creditor;
    private Household household;

    @BeforeEach
    void setUp() {
        debtor = mock(User.class);
        when(debtor.getId()).thenReturn(UUID.randomUUID());
        when(debtor.getName()).thenReturn("Alice");

        creditor = mock(User.class);
        when(creditor.getId()).thenReturn(UUID.randomUUID());
        when(creditor.getName()).thenReturn("Bob");

        household = mock(Household.class);
        when(household.getId()).thenReturn(UUID.randomUUID());
    }

    @Nested
    class ConstructorTests {

        // --- Happy Path ---

        @Test
        void constructor_withValidArguments_createsDebt() {
            // Execute
            Debt debt = new Debt(debtor, creditor, household, new BigDecimal("100.00"));

            // Assert
            assertThat(debt).isNotNull();
            assertThat(debt.getDebtor()).isEqualTo(debtor);
            assertThat(debt.getCreditor()).isEqualTo(creditor);
            assertThat(debt.getHousehold()).isEqualTo(household);
            assertThat(debt.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        void constructor_withSmallAmount_createsDebt() {
            // Execute
            Debt debt = new Debt(debtor, creditor, household, new BigDecimal("0.01"));

            // Assert
            assertThat(debt.getAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
        }

        @Test
        void constructor_withLargeAmount_createsDebt() {
            // Execute
            Debt debt = new Debt(debtor, creditor, household, new BigDecimal("9999999.99"));

            // Assert
            assertThat(debt.getAmount()).isEqualByComparingTo(new BigDecimal("9999999.99"));
        }

        // --- Null Validation ---

        @Test
        void constructor_nullDebtor_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Debt(null, creditor, household, new BigDecimal("100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debtor cannot be null");
        }

        @Test
        void constructor_nullCreditor_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Debt(debtor, null, household, new BigDecimal("100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Creditor cannot be null");
        }

        @Test
        void constructor_nullHousehold_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Debt(debtor, creditor, null, new BigDecimal("100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Household cannot be null");
        }

        @Test
        void constructor_nullAmount_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Debt(debtor, creditor, household, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debt amount cannot be null");
        }

        // --- Amount Validation ---

        @Test
        void constructor_zeroAmount_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Debt(debtor, creditor, household, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debt amount must be strictly greater than zero");
        }

        @Test
        void constructor_negativeAmount_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Debt(debtor, creditor, household, new BigDecimal("-100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debt amount must be strictly greater than zero");
        }

        // --- Debtor/Creditor Validation ---

        @Test
        void constructor_debtorAndCreditorSameUser_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Debt(debtor, debtor, household, new BigDecimal("100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debtor and Creditor cannot be the same user");
        }
    }

    @Nested
    class PropertyTests {

        @Test
        void gettersReturnConstructorValues() {
            // Setup
            BigDecimal amount = new BigDecimal("75.50");

            // Execute
            Debt debt = new Debt(debtor, creditor, household, amount);

            // Assert
            assertThat(debt.getDebtor()).isEqualTo(debtor);
            assertThat(debt.getCreditor()).isEqualTo(creditor);
            assertThat(debt.getHousehold()).isEqualTo(household);
            assertThat(debt.getAmount()).isEqualByComparingTo(amount);
        }

        @Test
        void settersAllowModifyingAmount() {
            // Setup
            Debt debt = new Debt(debtor, creditor, household, new BigDecimal("100.00"));

            // Execute
            debt.setAmount(new BigDecimal("50.00"));

            // Assert
            assertThat(debt.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        void debtIsInitiallyNotNull() {
            // Execute
            Debt debt = new Debt(debtor, creditor, household, new BigDecimal("100.00"));

            // Assert
            assertThat(debt.getId()).isNull();  // ID is only set by JPA
        }
    }
}
