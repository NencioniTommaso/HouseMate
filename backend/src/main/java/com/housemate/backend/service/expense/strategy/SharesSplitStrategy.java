package com.housemate.backend.service.expense.strategy;

import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Strategy for splitting an expense proportionally based on abstract "shares" or weights.
 * Example: Total = $100. User A has 3 shares, User B has 1 share. Total shares = 4.
 * - User A pays (3/4) * 100 = $75.00
 * - User B pays (1/4) * 100 = $25.00
 * * Utilizes a round-robin penny distribution algorithm to handle infinite repeating decimals,
 * mathematically guaranteeing the exact total is reached without creating or destroying pennies.
 */
@Component
public class SharesSplitStrategy implements ExpenseSplitStrategy {

    @Override
    public Map<UUID, BigDecimal> calculateShares(BigDecimal totalAmount, List<ExpenseShareRequestDTO> shareRequests) {
        // 1. Fail-Fast Validation
        if (shareRequests == null || shareRequests.isEmpty()) {
            throw new IllegalArgumentException("Share requests cannot be empty for proportional shares split.");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be strictly positive.");
        }

        // 2. Calculate the total sum of weights (shares)
        BigDecimal totalWeights = BigDecimal.ZERO;
        for (ExpenseShareRequestDTO request : shareRequests) {
            BigDecimal weight = request.share();
            if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Share weights must be strictly greater than zero for user: " + request.userId());
            }
            totalWeights = totalWeights.add(weight);
        }

        Map<UUID, BigDecimal> calculatedShares = new HashMap<>();
        BigDecimal totalDistributed = BigDecimal.ZERO;

        // 3. First Pass: Calculate exact base proportions truncated to 2 decimal places
        for (ExpenseShareRequestDTO request : shareRequests) {
            UUID userId = request.userId();
            BigDecimal weight = request.share();

            // Formula: (Weight * Total Amount) / Total Weights
            // Using RoundingMode.DOWN to prevent the sum from ever exceeding the total amount
            BigDecimal userBaseShare = weight.multiply(totalAmount)
                    .divide(totalWeights, 2, RoundingMode.DOWN);

            // Prevent duplicates
            if (calculatedShares.put(userId, userBaseShare) != null) {
                throw new IllegalArgumentException("Duplicate user ID found in share requests: " + userId);
            }
            
            totalDistributed = totalDistributed.add(userBaseShare);
        }

        // 4. Second Pass: Distribute the remaining pennies
        BigDecimal remainder = totalAmount.subtract(totalDistributed);
        BigDecimal penny = new BigDecimal("0.01");

        // We iterate through the requests again, handing out 1 penny at a time until the remainder is exhausted
        for (ExpenseShareRequestDTO request : shareRequests) {
            if (remainder.compareTo(BigDecimal.ZERO) <= 0) {
                break; // Stop distributing if no pennies are left
            }

            UUID userId = request.userId();
            BigDecimal currentShare = calculatedShares.get(userId);
            
            // Add a penny to this user and deduct from the remainder
            calculatedShares.put(userId, currentShare.add(penny));
            remainder = remainder.subtract(penny);
        }

        return calculatedShares;
    }
}