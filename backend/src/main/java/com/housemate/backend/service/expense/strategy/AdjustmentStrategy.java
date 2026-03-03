package com.housemate.backend.service.expense.strategy;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for adjusting expense shares.
 * Applies relative adjustments to an equal baseline split.
 * 
 * Example: Expense amount = $100, involvedUsers = 3, adjustments = [+10, -5, 0]
 * - Baseline: (100 - (10 - 5 + 0)) / 3 = 95 / 3 = $31.67 per person
 * - User A: 31.67 + 10 = $41.67
 * - User B: 31.67 - 5 = $26.67
 * - User C: 31.67 + 0 = $31.67
 * 
 * splitParameters: List of BigDecimal values representing adjustment deltas for each user
 *                  (must have the same length as involvedUsers and in the same order)
 *                  Positive values = pays more, negative values = pays less
 */
@Component
public class AdjustmentStrategy implements ExpenseSplitStrategy {

    @Override
    public List<ExpenseShare> calculateShares(Expense expense, List<User> involvedUsers, List<BigDecimal> splitParameters) {
        if (involvedUsers == null || involvedUsers.isEmpty()) {
            throw new IllegalArgumentException("Involved users cannot be empty for adjustment split.");
        }

        if (splitParameters == null || splitParameters.isEmpty()) {
            throw new IllegalArgumentException("Split parameters (adjustments) cannot be empty for adjustment split.");
        }

        if (involvedUsers.size() != splitParameters.size()) {
            throw new IllegalArgumentException("Number of adjustments must match number of involved users.");
        }

        BigDecimal totalAmount = expense.getAmount();
        
        // Calculate sum of all adjustments
        BigDecimal sumOfAdjustments = BigDecimal.ZERO;
        for (BigDecimal adjustment : splitParameters) {
            sumOfAdjustments = sumOfAdjustments.add(adjustment);
        }

        // Calculate baseline: (total - sum of adjustments) / number of users
        BigDecimal baselineNumerator = totalAmount.subtract(sumOfAdjustments);
        BigDecimal divisor = new BigDecimal(involvedUsers.size());
        BigDecimal baseline = baselineNumerator.divide(divisor, 2, java.math.RoundingMode.HALF_DOWN);

        List<ExpenseShare> shares = new ArrayList<>();

        for (int i = 0; i < involvedUsers.size(); i++) {
            User user = involvedUsers.get(i);
            BigDecimal adjustment = splitParameters.get(i);
            
            // User's share = baseline + adjustment
            BigDecimal userShare = baseline.add(adjustment);

            // Only create shares for non-zero amounts
            if (userShare.compareTo(BigDecimal.ZERO) > 0) {
                shares.add(new ExpenseShare(expense, user, userShare));
            }
        }

        return shares;
    }
}
