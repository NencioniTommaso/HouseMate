package com.housemate.backend.service.expense.strategy;

import com.housemate.shared.enums.ExpenseSplitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpenseSplitStrategyFactoryTest {

    private EqualSplitStrategy equalSplitStrategy;
    private SharesSplitStrategy sharesSplitStrategy;
    private ExactAmountStrategy exactAmountStrategy;
    private AdjustmentStrategy adjustmentStrategy;
    private ExpenseSplitStrategyFactory factory;

    @BeforeEach
    void setUp() {
        equalSplitStrategy = new EqualSplitStrategy();
        sharesSplitStrategy = new SharesSplitStrategy();
        exactAmountStrategy = new ExactAmountStrategy();
        adjustmentStrategy = new AdjustmentStrategy();

        factory = new ExpenseSplitStrategyFactory(
                equalSplitStrategy,
                sharesSplitStrategy,
                exactAmountStrategy,
                adjustmentStrategy
        );
    }

    @Nested
    class ConstructorTests {

        @Test
        void constructor_withNullEqualStrategy_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new ExpenseSplitStrategyFactory(
                    null,
                    sharesSplitStrategy,
                    exactAmountStrategy,
                    adjustmentStrategy
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Equal split strategy must not be null");
        }

        @Test
        void constructor_withNullSharesStrategy_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new ExpenseSplitStrategyFactory(
                    equalSplitStrategy,
                    null,
                    exactAmountStrategy,
                    adjustmentStrategy
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Shares split strategy must not be null");
        }

        @Test
        void constructor_withNullExactAmountStrategy_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new ExpenseSplitStrategyFactory(
                    equalSplitStrategy,
                    sharesSplitStrategy,
                    null,
                    adjustmentStrategy
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Exact amount strategy must not be null");
        }

        @Test
        void constructor_withNullAdjustmentStrategy_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> new ExpenseSplitStrategyFactory(
                    equalSplitStrategy,
                    sharesSplitStrategy,
                    exactAmountStrategy,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Adjustment strategy must not be null");
        }
    }

    @Nested
    class GetStrategyTests {

        @Test
        void getStrategy_equalSplit_returnsEqualSplitStrategyInstance() {
            ExpenseSplitStrategy strategy = factory.getStrategy(ExpenseSplitType.EQUAL_SPLIT);

            assertThat(strategy).isSameAs(equalSplitStrategy);
        }

        @Test
        void getStrategy_shares_returnsSharesSplitStrategyInstance() {
            ExpenseSplitStrategy strategy = factory.getStrategy(ExpenseSplitType.SHARES);

            assertThat(strategy).isSameAs(sharesSplitStrategy);
        }

        @Test
        void getStrategy_exactAmount_returnsExactAmountStrategyInstance() {
            ExpenseSplitStrategy strategy = factory.getStrategy(ExpenseSplitType.EXACT_AMOUNT);

            assertThat(strategy).isSameAs(exactAmountStrategy);
        }

        @Test
        void getStrategy_adjustment_returnsAdjustmentStrategyInstance() {
            ExpenseSplitStrategy strategy = factory.getStrategy(ExpenseSplitType.ADJUSTMENT);

            assertThat(strategy).isSameAs(adjustmentStrategy);
        }

        @Test
        void getStrategy_nullType_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> factory.getStrategy(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Split type must not be null");
        }

        @Test
        void getStrategy_returnsNonNullForAllEnumValues() {
            for (ExpenseSplitType type : ExpenseSplitType.values()) {
                assertThat(factory.getStrategy(type)).isNotNull();
            }
        }
    }
}
