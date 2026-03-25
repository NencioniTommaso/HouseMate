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

@SuppressWarnings("null")
class AdjustmentStrategyTest {

    private AdjustmentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AdjustmentStrategy();
    }

    @Nested
    class CalculateSharesTests {

        @Test
        void calculateShares_withBalancedPositiveAndNegativeAdjustments_splitsAsExpected() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("10.00")),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("-10.00"))
                    )
            );

            assertThat(result).hasSize(2);
            assertThat(result.get(u1)).isEqualByComparingTo("60.00");
            assertThat(result.get(u2)).isEqualByComparingTo("40.00");
            assertThat(sum(result)).isEqualByComparingTo("100.00");
        }

        @Test
        void calculateShares_withNullAdjustments_treatsNullAsZero() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, null),
                            new ExpenseShareRequestDTO(u2, null)
                    )
            );

            assertThat(result.get(u1)).isEqualByComparingTo("50.00");
            assertThat(result.get(u2)).isEqualByComparingTo("50.00");
            assertThat(sum(result)).isEqualByComparingTo("100.00");
        }

        @Test
        void calculateShares_distributesBaselineRemainderPennies() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();
            UUID u3 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, BigDecimal.ZERO),
                            new ExpenseShareRequestDTO(u2, BigDecimal.ZERO),
                            new ExpenseShareRequestDTO(u3, BigDecimal.ZERO)
                    )
            );

            assertThat(result).hasSize(3);
            assertThat(sum(result)).isEqualByComparingTo("100.00");
            assertThat(result.values())
                    .containsExactlyInAnyOrder(new BigDecimal("33.34"), new BigDecimal("33.33"), new BigDecimal("33.33"));
        }

        @Test
        void calculateShares_whenOneShareBecomesExactlyZero_filtersThatShareOut() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, BigDecimal.ZERO),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("-100.00"))
                    )
            );

            assertThat(result).hasSize(1);
            assertThat(result).containsKey(u1);
            assertThat(result).doesNotContainKey(u2);
            assertThat(result.get(u1)).isEqualByComparingTo("100.00");
            assertThat(sum(result)).isEqualByComparingTo("100.00");
        }

        @Test
        void calculateShares_whenAdjustmentCausesNegativeShare_throwsIllegalArgumentException() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, BigDecimal.ZERO),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("-101.00"))
                    )
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid adjustments")
                    .hasMessageContaining("negative share");
        }

        @Test
        void calculateShares_whenPositiveAdjustmentsSumEqualsTotal_throwsIllegalArgumentException() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("60.00")),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("40.00"))
                    )
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Sum of adjustments")
                    .hasMessageContaining("cannot exceed or equal the total amount");
        }

        @Test
        void calculateShares_whenPositiveAdjustmentsSumExceedsTotal_throwsIllegalArgumentException() {
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
                    .hasMessageContaining("Sum of adjustments")
                    .hasMessageContaining("cannot exceed or equal the total amount");
        }

        @Test
        void calculateShares_withDuplicateUsers_throwsIllegalArgumentException() {
            UUID user = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(user, BigDecimal.ZERO),
                            new ExpenseShareRequestDTO(user, new BigDecimal("5.00"))
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
                    List.of(new ExpenseShareRequestDTO(user, BigDecimal.ZERO))
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

        @Test
        void calculateShares_withZeroAmount_throwsIllegalArgumentException() {
            UUID user = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    BigDecimal.ZERO,
                    List.of(new ExpenseShareRequestDTO(user, BigDecimal.ZERO))
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Total amount must be strictly positive");
        }
    }

    private BigDecimal sum(Map<UUID, BigDecimal> shares) {
        return shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
