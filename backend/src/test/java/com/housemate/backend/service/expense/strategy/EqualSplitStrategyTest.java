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
class EqualSplitStrategyTest {

    private EqualSplitStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new EqualSplitStrategy();
    }

    @Nested
    class CalculateSharesTests {

        @Test
        void calculateShares_withTwoUsersAndEvenAmount_splitsExactly() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, null),
                            new ExpenseShareRequestDTO(u2, null)
                    )
            );

            assertThat(result).hasSize(2);
            assertThat(result.get(u1)).isEqualByComparingTo("50.00");
            assertThat(result.get(u2)).isEqualByComparingTo("50.00");
            assertThat(sum(result)).isEqualByComparingTo("100.00");
        }

        @Test
        void calculateShares_withThreeUsersAndRemainder_distributesSinglePenny() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();
            UUID u3 = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("10.00"),
                    List.of(
                            new ExpenseShareRequestDTO(u1, null),
                            new ExpenseShareRequestDTO(u2, null),
                            new ExpenseShareRequestDTO(u3, null)
                    )
            );

            assertThat(result).hasSize(3);
            assertThat(sum(result)).isEqualByComparingTo("10.00");
            assertThat(result.values())
                    .containsExactlyInAnyOrder(new BigDecimal("3.34"), new BigDecimal("3.33"), new BigDecimal("3.33"));
        }

        @Test
        void calculateShares_withSingleUser_assignsFullAmount() {
            UUID user = UUID.randomUUID();

            Map<UUID, BigDecimal> result = strategy.calculateShares(
                    new BigDecimal("99.99"),
                    List.of(new ExpenseShareRequestDTO(user, null))
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(user)).isEqualByComparingTo("99.99");
            assertThat(sum(result)).isEqualByComparingTo("99.99");
        }

        @Test
        void calculateShares_withDuplicateUsers_throwsIllegalArgumentException() {
            UUID user = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("100.00"),
                    List.of(
                            new ExpenseShareRequestDTO(user, null),
                            new ExpenseShareRequestDTO(user, null)
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
                    List.of(new ExpenseShareRequestDTO(user, null))
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
                    List.of(new ExpenseShareRequestDTO(user, null))
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Total amount must be strictly positive");
        }

        @Test
        void calculateShares_withNegativeAmount_throwsIllegalArgumentException() {
            UUID user = UUID.randomUUID();

            assertThatThrownBy(() -> strategy.calculateShares(
                    new BigDecimal("-1.00"),
                    List.of(new ExpenseShareRequestDTO(user, null))
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Total amount must be strictly positive");
        }
    }

    private BigDecimal sum(Map<UUID, BigDecimal> shares) {
        return shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
