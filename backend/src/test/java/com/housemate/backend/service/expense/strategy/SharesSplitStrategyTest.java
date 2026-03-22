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

class SharesSplitStrategyTest {

    private SharesSplitStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SharesSplitStrategy();
    }

    @Nested
    class CalculateSharesTests {

        @Test
        void calculateShares_withWeightedSplit_distributesProportionally() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("3")),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("1"))
                    )
            );

            assertThat(result).hasSize(2);
            assertThat(result.get(u1)).isEqualByComparingTo("75.00");
            assertThat(result.get(u2)).isEqualByComparingTo("25.00");
            assertThat(sum(result)).isEqualByComparingTo("100.00");
        }

        @Test
        void calculateShares_withFractionalWeights_handlesPrecisionAndRemainder() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("10.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("1.5")),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("2.5"))
                    )
            );

            assertThat(result).hasSize(2);
            assertThat(sum(result)).isEqualByComparingTo("10.00");
            assertThat(result.get(u2)).isGreaterThan(result.get(u1));
            assertThat(result.values())
                    .containsExactlyInAnyOrder(new BigDecimal("3.75"), new BigDecimal("6.25"));
        }

        @Test
        void calculateShares_withThreeUsersAndUnevenResult_distributesPennies() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();
            UUID u3 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("1")),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("2")),
                            new ExpenseShareRequestDTO(u3, new BigDecimal("3"))
                    )
            );

            assertThat(result).hasSize(3);
            assertThat(sum(result)).isEqualByComparingTo("100.00");
        }

        @Test
        void calculateShares_withSingleUser_assignsFullAmount() {
            UUID user = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("88.88"),
                    List.of(new ExpenseShareRequestDTO(user, new BigDecimal("1")))
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(user)).isEqualByComparingTo("88.88");
        }

        @Test
        void calculateShares_withZeroWeight_throwsIllegalArgumentException() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("1")),
                            new ExpenseShareRequestDTO(u2, BigDecimal.ZERO)
                    )
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Share weights must be strictly greater than zero");
        }

        @Test
        void calculateShares_withNegativeWeight_throwsIllegalArgumentException() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("1")),
                            new ExpenseShareRequestDTO(u2, new BigDecimal("-1"))
                    )
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Share weights must be strictly greater than zero");
        }

        @Test
        void calculateShares_withNullWeight_throwsIllegalArgumentException() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, new BigDecimal("1")),
                            new ExpenseShareRequestDTO(u2, null)
                    )
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Share weights must be strictly greater than zero");
        }

        @Test
        void calculateShares_withDuplicateUsers_throwsIllegalArgumentException() {
            UUID user = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(user, new BigDecimal("1")),
                            new ExpenseShareRequestDTO(user, new BigDecimal("2"))
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
                    List.of(new ExpenseShareRequestDTO(user, new BigDecimal("1")))
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
                    List.of(new ExpenseShareRequestDTO(user, new BigDecimal("1")))
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Total amount must be strictly positive");
        }
    }

    private BigDecimal sum(Map<UUID, BigDecimal> shares) {
        return shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
