package com.housemate.backend.model.expense;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("null")
class SettlementTest {

    private Debt debt;
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

        debt = mock(Debt.class);
        when(debt.getId()).thenReturn(UUID.randomUUID());
        when(debt.getHousehold()).thenReturn(household);
    }

    @Nested
    class ConstructorTests {

        // --- Happy Path ---

        @Test
        void constructor_withValidArguments_createsSettlement() {
            // Execute
            Settlement settlement = new Settlement(
                    debt, debtor, creditor, new BigDecimal("50.00"), "Bank transfer"
            );

            // Assert
            assertThat(settlement).isNotNull();
            assertThat(settlement.getDebt()).isEqualTo(debt);
            assertThat(settlement.getDebtor()).isEqualTo(debtor);
            assertThat(settlement.getCreditor()).isEqualTo(creditor);
            assertThat(settlement.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(settlement.getDescription()).isEqualTo("Bank transfer");
            assertThat(settlement.getHousehold()).isEqualTo(household);
            assertThat(settlement.getSettlementDate()).isNotNull();
        }

        @Test
        void constructor_withNullDescription_createsSettlementWithoutDescription() {
            // Execute
            Settlement settlement = new Settlement(
                    debt, debtor, creditor, new BigDecimal("50.00"), null
            );

            // Assert
            assertThat(settlement).isNotNull();
            assertThat(settlement.getDescription()).isNull();
        }

        @Test
        void constructor_setsSettlementDateToCurrentTime_withinAcceptableRange() {
            // Setup
            LocalDateTime beforeCreation = LocalDateTime.now();

            // Execute
            Settlement settlement = new Settlement(
                    debt, debtor, creditor, new BigDecimal("50.00"), null
            );

            LocalDateTime afterCreation = LocalDateTime.now();

            // Assert
            assertThat(beforeCreation.minusSeconds(1)).isBefore(settlement.getSettlementDate());
            assertThat(afterCreation.plusSeconds(1)).isAfter(settlement.getSettlementDate());
        }

        @Test
        void constructor_withSmallAmount_createsSettlement() {
            // Execute
            Settlement settlement = new Settlement(
                    debt, debtor, creditor, new BigDecimal("0.01"), "Payment"
            );

            // Assert
            assertThat(settlement.getAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
        }

        @Test
        void constructor_withLargeAmount_createsSettlement() {
            // Execute
            Settlement settlement = new Settlement(
                    debt, debtor, creditor, new BigDecimal("9999999.99"), "Payment"
            );

            // Assert
            assertThat(settlement.getAmount()).isEqualByComparingTo(new BigDecimal("9999999.99"));
        }

        @Test
        void constructor_withLongDescription_createsSettlement() {
            // Setup: Max length 500 characters
            String longDescription = "A".repeat(500);

            // Execute
            Settlement settlement = new Settlement(
                    debt, debtor, creditor, new BigDecimal("50.00"), longDescription
            );

            // Assert
            assertThat(settlement.getDescription()).isEqualTo(longDescription);
        }

        // --- Null Validation ---

        @Test
        void constructor_nullDebt_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Settlement(
                    null, debtor, creditor, new BigDecimal("50.00"), "Payment"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debt cannot be null");
        }

        @Test
        void constructor_nullDebtor_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Settlement(
                    debt, null, creditor, new BigDecimal("50.00"), "Payment"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debtor cannot be null");
        }

        @Test
        void constructor_nullCreditor_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Settlement(
                    debt, debtor, null, new BigDecimal("50.00"), "Payment"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Creditor cannot be null");
        }

        @Test
        void constructor_nullAmount_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Settlement(
                    debt, debtor, creditor, null, "Payment"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Settlement amount cannot be null");
        }

        // --- Amount Validation ---

        @Test
        void constructor_zeroAmount_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Settlement(
                    debt, debtor, creditor, BigDecimal.ZERO, "Payment"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Settlement amount must be strictly greater than zero");
        }

        @Test
        void constructor_negativeAmount_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Settlement(
                    debt, debtor, creditor, new BigDecimal("-50.00"), "Payment"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Settlement amount must be strictly greater than zero");
        }

        // --- Debtor/Creditor Validation ---

        @Test
        void constructor_debtorAndCreditorSameUser_throwsIllegalArgumentException() {
            // Execute & Assert
            assertThatThrownBy(() -> new Settlement(
                    debt, debtor, debtor, new BigDecimal("50.00"), "Payment"
            ))
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
            String description = "Partial payment";

            // Execute
            Settlement settlement = new Settlement(debt, debtor, creditor, amount, description);

            // Assert
            assertThat(settlement.getDebt()).isEqualTo(debt);
            assertThat(settlement.getDebtor()).isEqualTo(debtor);
            assertThat(settlement.getCreditor()).isEqualTo(creditor);
            assertThat(settlement.getAmount()).isEqualByComparingTo(amount);
            assertThat(settlement.getDescription()).isEqualTo(description);
            assertThat(settlement.getHousehold()).isEqualTo(household);
        }

        @Test
        void householdIsDenormalizedFromDebt() {
            // Execute
            Settlement settlement = new Settlement(
                    debt, debtor, creditor, new BigDecimal("50.00"), null
            );

            // Assert: Household should come from debt
            assertThat(settlement.getHousehold()).isEqualTo(household);
        }

        @Test
        void settersAllowModifyingAmount() {
            // Setup
            Settlement settlement = new Settlement(
                    debt, debtor, creditor, new BigDecimal("100.00"), "Payment"
            );

            // Execute
            settlement.setAmount(new BigDecimal("75.00"));

            // Assert
            assertThat(settlement.getAmount()).isEqualByComparingTo(new BigDecimal("75.00"));
        }

        @Test
        void settlementIsInitiallyWithoutId() {
            // Execute
            Settlement settlement = new Settlement(
                    debt, debtor, creditor, new BigDecimal("50.00"), null
            );

            // Assert
            assertThat(settlement.getId()).isNull();  // ID is only set by JPA
        }
    }
}
