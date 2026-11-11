package com.housemate.backend.service.expense.strategy;

import com.housemate.shared.enums.ExpenseSplitType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for obtaining the appropriate ExpenseSplitStrategy based on ExpenseSplitType.
 * This class manages the mapping between expense split types and their corresponding strategy implementations.
 */
@Component
@Slf4j
public class ExpenseSplitStrategyFactory {

    private final EqualSplitStrategy equalSplitStrategy;
    private final SharesSplitStrategy sharesSplitStrategy;
    private final ExactAmountStrategy exactAmountStrategy;
    private final AdjustmentStrategy adjustmentStrategy;

    public ExpenseSplitStrategyFactory(
            @NonNull EqualSplitStrategy equalSplitStrategy,
            @NonNull SharesSplitStrategy sharesSplitStrategy,
            @NonNull ExactAmountStrategy exactAmountStrategy,
            @NonNull AdjustmentStrategy adjustmentStrategy) {
        // 1. Fail-Fast Validation
        Assert.notNull(equalSplitStrategy, "Equal split strategy must not be null");
        Assert.notNull(sharesSplitStrategy, "Shares split strategy must not be null");
        Assert.notNull(exactAmountStrategy, "Exact amount strategy must not be null");
        Assert.notNull(adjustmentStrategy, "Adjustment strategy must not be null");
        
        // 2. Assignment
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
    public ExpenseSplitStrategy getStrategy(@NonNull ExpenseSplitType splitType) {
        // 1. Fail-Fast Validation
        Assert.notNull(splitType, "Split type must not be null");
        log.info("Starting expense split strategy selection");

        // 2. Return the appropriate strategy
        ExpenseSplitStrategy strategy = switch (splitType) {
            case EQUAL_SPLIT -> equalSplitStrategy;
            case SHARES -> sharesSplitStrategy;
            case EXACT_AMOUNT -> exactAmountStrategy;
            case ADJUSTMENT -> adjustmentStrategy;
        };
        log.info("Completed expense split strategy selection successfully");
        return strategy;
    }
}
