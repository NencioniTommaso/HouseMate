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
 * Strategy for splitting an expense equally among all involved users.
 * Utilizes a round-robin penny distribution algorithm to handle division remainders,
 * ensuring the sum of all shares perfectly matches the total expense amount.
 */
@Component
@Slf4j
public class EqualSplitStrategy implements ExpenseSplitStrategy {

    @Override
    public Map<UUID, BigDecimal> calculateShares(
            @NonNull BigDecimal totalAmount,
            @NonNull List<ExpenseShareRequestDTO> shareRequests) {
        // 1. Fail-Fast Validation
        Assert.notNull(totalAmount, "Total amount must not be null");
        Assert.notNull(shareRequests, "Share requests must not be null");
        log.info("Starting equal expense split calculation");
        Assert.isTrue(!shareRequests.isEmpty(), "Share requests cannot be empty for equal split.");
        Assert.isTrue(totalAmount.compareTo(BigDecimal.ZERO) > 0, "Total amount must be strictly positive.");

        int numberOfUsers = shareRequests.size();
        BigDecimal divisor = BigDecimal.valueOf(numberOfUsers);

        // 2. Calculate the base share truncated to 2 decimal places (RoundingMode.DOWN)
        // E.g., 10.00 / 3 = 3.33
        BigDecimal baseShare = totalAmount.divide(divisor, 2, RoundingMode.DOWN);

        // 3. Calculate the exact remainder in cents
        // E.g., 10.00 - (3.33 * 3) = 0.01
        BigDecimal totalDistributed = baseShare.multiply(divisor);
        BigDecimal remainder = totalAmount.subtract(totalDistributed);

        // We will distribute the remainder as 1-cent increments (0.01)
        BigDecimal penny = new BigDecimal("0.01");

        Map<UUID, BigDecimal> calculatedShares = new HashMap<>();

        // 4. Distribute the base share and the remainder cents in a round-robin fashion
        for (ExpenseShareRequestDTO request : shareRequests) {
            UUID userId = request.userId();
            BigDecimal userShare = baseShare;

            // If we still have pennies left in the remainder, give one to this user
            if (remainder.compareTo(BigDecimal.ZERO) > 0) {
                userShare = userShare.add(penny);
                remainder = remainder.subtract(penny);
            }

            // Put the calculated share into the map, preventing duplicate user IDs
            if (calculatedShares.put(userId, userShare) != null) {
                throw new IllegalArgumentException("Duplicate user ID found in share requests: " + userId);
            }
        }

        log.info("Completed equal expense split calculation with share count: {}", calculatedShares.size());
        return calculatedShares;
    }
}