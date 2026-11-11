package com.housemate.backend.service.expense.strategy;

import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Strategy for splitting an expense using exact amounts.
 * This pure calculator validates that the sum of the requested exact amounts 
 * strictly matches the total expense amount.
 */
@Component
@Slf4j
public class ExactAmountStrategy implements ExpenseSplitStrategy {

    @Override
    public Map<UUID, BigDecimal> calculateShares(
            @NonNull BigDecimal totalAmount,
            @NonNull List<ExpenseShareRequestDTO> shareRequests) {
        // 1. Fail-Fast Validation
        Assert.notNull(totalAmount, "Total amount must not be null");
        Assert.notNull(shareRequests, "Share requests must not be null");
        log.info("Starting exact amount expense split calculation");
        Assert.isTrue(!shareRequests.isEmpty(), "Share requests cannot be empty for exact amount split.");

        // 2. Validate individual amounts and calculate the sum
        BigDecimal sumOfExactAmounts = BigDecimal.ZERO;

        for (ExpenseShareRequestDTO request : shareRequests) {
            BigDecimal exactAmount = request.share();
            
            if (exactAmount == null) {
                throw new IllegalArgumentException("Exact amount cannot be null for user: " + request.userId());
            }
            if (exactAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Exact amounts must be non-negative. Invalid amount for user: " + request.userId());
            }
            
            sumOfExactAmounts = sumOfExactAmounts.add(exactAmount);
        }

        // 3. Verify the sum matches the total expense strictly
        if (sumOfExactAmounts.compareTo(totalAmount) != 0) {
            throw new IllegalArgumentException(
                    "Sum of exact amounts (" + sumOfExactAmounts + ") does not match total expense amount (" + totalAmount + ")."
            );
        }

        // 4. Map the results, filtering out any $0.00 shares to keep the database clean
        Map<UUID, BigDecimal> calculatedShares = shareRequests.stream()
                .filter(request -> request.share().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toMap(
                        ExpenseShareRequestDTO::userId,
                        ExpenseShareRequestDTO::share,
                        // If a malicious client sends the same user ID twice, throw an error instead of silently overwriting
                        (existing, replacement) -> {
                            throw new IllegalArgumentException("Duplicate user ID found in share requests.");
                        }
                ));
            log.info("Completed exact amount expense split calculation with share count: {}", calculatedShares.size());
            return calculatedShares;
    }
}