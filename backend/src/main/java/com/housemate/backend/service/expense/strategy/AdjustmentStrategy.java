package com.housemate.backend.service.expense.strategy;

import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Strategy for adjusting expense shares.
 * Applies relative adjustments (positive or negative) to an equal baseline split.
 * Utilizes a round-robin penny distribution algorithm on the baseline to guarantee exact sums.
 * * Example: Expense amount = $100, users = 3, adjustments = [+10, -5, 0]
 * - Sum of adjustments = 5
 * - Amount to split equally = 100 - 5 = 95
 * - Baseline share = 95 / 3 = 31.66
 * - Remainder pennies = 95 - (31.66 * 3) = 0.02
 * - User A: 31.66 + 10 + 0.01 (penny) = $41.67
 * - User B: 31.66 - 5  + 0.01 (penny) = $26.67
 * - User C: 31.66 + 0  + 0.00         = $31.66
 */
@Component
@Slf4j
public class AdjustmentStrategy implements ExpenseSplitStrategy {

    @Override
    public Map<UUID, BigDecimal> calculateShares(
            @NonNull BigDecimal totalAmount,
            @NonNull List<ExpenseShareRequestDTO> shareRequests) {
        // 1. Fail-Fast Validation
        Assert.notNull(totalAmount, "Total amount must not be null");
        Assert.notNull(shareRequests, "Share requests must not be null");
        log.info("Starting adjusted expense split calculation");
        Assert.isTrue(!shareRequests.isEmpty(), "Share requests cannot be empty for adjustment split.");
        Assert.isTrue(totalAmount.compareTo(BigDecimal.ZERO) > 0, "Total amount must be strictly positive.");

        // 2. Calculate the sum of all adjustments (adjustments can be null, negative, zero, or positive)
        BigDecimal sumOfAdjustments = BigDecimal.ZERO;
        for (ExpenseShareRequestDTO request : shareRequests) {
            BigDecimal adjustment = request.share();
            // Null adjustments are treated as zero (no relative change)
            if (adjustment != null) {
                // Adjustments can be any value (negative, zero, or positive)
                // They represent relative offsets to the baseline equal split
                sumOfAdjustments = sumOfAdjustments.add(adjustment);
            }
        }

        // 3. Calculate the new baseline target to split equally
        BigDecimal amountToSplitEqually = totalAmount.subtract(sumOfAdjustments);
        
        // 3a. Validate that the baseline is still positive (adjustments shouldn't eliminate the entire amount)
        Assert.isTrue(
                amountToSplitEqually.compareTo(BigDecimal.ZERO) > 0,
                "Sum of adjustments (" + sumOfAdjustments + ") cannot exceed or equal the total amount (" + totalAmount + ")."
        );

        // 4. Perform the Equal Split math on the baseline
        int numberOfUsers = shareRequests.size();
        BigDecimal divisor = BigDecimal.valueOf(numberOfUsers);
        
        // Truncate to 2 decimal places
        BigDecimal baseShare = amountToSplitEqually.divide(divisor, 2, RoundingMode.DOWN);

        // Calculate missing pennies
        BigDecimal totalBaseDistributed = baseShare.multiply(divisor);
        BigDecimal remainder = amountToSplitEqually.subtract(totalBaseDistributed);
        BigDecimal penny = new BigDecimal("0.01");

        Map<UUID, BigDecimal> calculatedShares = new HashMap<>();

        // 5. Distribute Base + Adjustment + Penny
        for (ExpenseShareRequestDTO request : shareRequests) {
            UUID userId = request.userId();
            BigDecimal adjustment = request.share() != null ? request.share() : BigDecimal.ZERO;

            // Start with base + adjustment
            BigDecimal userShare = baseShare.add(adjustment);

            // Add a remainder penny if available
            if (remainder.compareTo(BigDecimal.ZERO) > 0) {
                userShare = userShare.add(penny);
                remainder = remainder.subtract(penny);
            }

            // Financial Integrity Check: An adjustment cannot result in a negative debt share
            if (userShare.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                    "Invalid adjustments: User " + userId + " would have a negative share (" + userShare + ")."
                );
            }

            // Map the result, preventing duplicate user IDs
            if (calculatedShares.put(userId, userShare) != null) {
                throw new IllegalArgumentException("Duplicate user ID found in share requests: " + userId);
            }
        }

        // 6. Optional: Filter out perfectly zero shares (if someone's adjustment perfectly negated their base)
        // We only retain shares strictly greater than zero for database cleanliness
        calculatedShares.entrySet().removeIf(entry -> entry.getValue().compareTo(BigDecimal.ZERO) == 0);

        log.info("Completed adjusted expense split calculation with share count: {}", calculatedShares.size());
        return calculatedShares;
    }
}