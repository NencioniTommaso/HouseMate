package com.housemate.backend.service.expense.strategy;

import com.housemate.shared.enums.ExpenseSplitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for obtaining the appropriate ExpenseSplitStrategy based on ExpenseSplitType.
 * This class manages the mapping between expense split types and their corresponding strategy implementations.
 */
@Component
public class ExpenseSplitStrategyFactory {

    private final EqualSplitStrategy equalSplitStrategy;
    private final SharesSplitStrategy sharesSplitStrategy;
    private final ExactAmountStrategy exactAmountStrategy;
    private final AdjustmentStrategy adjustmentStrategy;

    public ExpenseSplitStrategyFactory(
            EqualSplitStrategy equalSplitStrategy,
            SharesSplitStrategy sharesSplitStrategy,
            ExactAmountStrategy exactAmountStrategy,
            AdjustmentStrategy adjustmentStrategy) {
        this.equalSplitStrategy = equalSplitStrategy;
        this.sharesSplitStrategy = sharesSplitStrategy;
        this.exactAmountStrategy = exactAmountStrategy;
        this.adjustmentStrategy = adjustmentStrategy;
    }

    /**
     * Get the appropriate strategy for the given expense split type.
     *
     * @param splitType the type of expense split
     * @return the corresponding ExpenseSplitStrategy implementation
     * @throws IllegalArgumentException if the split type is not recognized
     */
    public ExpenseSplitStrategy getStrategy(ExpenseSplitType splitType) {
        if (splitType == null) {
            throw new IllegalArgumentException("Split type cannot be null.");
        }

        return switch (splitType) {
            case EQUAL_SPLIT -> equalSplitStrategy;
            case SHARES -> sharesSplitStrategy;
            case EXACT_AMOUNT -> exactAmountStrategy;
            case ADJUSTMENT -> adjustmentStrategy;
        };
    }
}
