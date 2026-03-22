package com.housemate.backend.service.expense.strategy;

import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExactAmountStrategyTest {

    private ExactAmountStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ExactAmountStrategy();
    }

    @Nested
    class CalculateSharesTests {

        @Test
        void calculateShares_withValidExactAmounts_returnsMatchingMap() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("60.00")),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("40.00"))
                    )
            );

            assertThat(result).hasSize(2);
            assertThat(result.get(u1)).isEqualByComparingTo("60.00");
            assertThat(result.get(u2)).isEqualByComparingTo("40.00");
            assertThat(sum(result)).isEqualByComparingTo("100.00");
        }

        @Test
        void calculateShares_filtersOutZeroShares() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();
            UUID u3 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("100.00")),
                            new ExpenseShareRequestDTO(u2, BigDecimal.ZERO),
                            new ExpenseShareRequestDTO(u3, BigDecimal.ZERO)
                    )
            );

            assertThat(result).hasSize(1);
            assertThat(result).containsOnlyKeys(u1);
            assertThat(result.get(u1)).isEqualByComparingTo("100.00");
            assertThat(sum(result)).isEqualByComparingTo("100.00");
        }

        @Test
        void calculateShares_withSingleUser_assignsFullAmount() {
            UUID user = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("99.99"),
                    List.of(new ExpenseShareRequestDTO(user, new BigDecimal("99.99")))
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(user)).isEqualByComparingTo("99.99");
        }

        @Test
        void calculateShares_whenSumLessThanTotal_throwsIllegalArgumentException() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("40.00")),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("50.00"))
                    )
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Sum of exact amounts")
                    .hasMessageContaining("does not match total expense amount");
        }

        @Test
        void calculateShares_whenSumGreaterThanTotal_throwsIllegalArgumentException() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("60.00")),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("41.00"))
                    )
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Sum of exact amounts")
                    .hasMessageContaining("does not match total expense amount");
        }

        @Test
        void calculateShares_withNullExactAmount_throwsIllegalArgumentException() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("60.00")),
                            new ExpenseShareRequestDTO(u2, null)
                    )
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Exact amount cannot be null");
        }

        @Test
        void calculateShares_withNegativeExactAmount_throwsIllegalArgumentException() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("101.00")),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("-1.00"))
                    )
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Exact amounts must be non-negative");
        }

        @Test
        void calculateShares_withDuplicateUsers_throwsIllegalArgumentException() {
            UUID user = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(user, new BigDecimal("50.00")),
                            new ExpenseShareRequestDTO(user, new BigDecimal("50.00"))
                    )
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Duplicate user ID found in share requests");
        }

        @Test
        void calculateShares_withNullAmount_throwsIllegalArgumentException() {
            UUID user = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    null,
                    List.of(new ExpenseShareRequestDTO(user, new BigDecimal("1.00")))
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Total amount must not be null");
        }

        @Test
        void calculateShares_withNullShareRequests_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> strategy.calculateShares(new BigDecimal("100.00"), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Share requests must not be null");
        }

        @Test
        void calculateShares_withEmptyShareRequests_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> strategy.calculateShares(new BigDecimal("100.00"), List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Share requests cannot be empty");
        }
    }

    private BigDecimal sum(Map<UUID, BigDecimal> shares) {
        return shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
