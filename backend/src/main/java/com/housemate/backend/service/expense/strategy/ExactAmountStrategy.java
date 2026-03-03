package com.housemate.backend.service.expense.strategy;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for splitting an expense using exact amounts.
 * Each user is assigned a specific amount they owe.
 * 
 * splitParameters: List of BigDecimal values representing the exact amount for each user
 *                  (must have the same length as involvedUsers and in the same order)
 * 
 * Note: The sum of exact amounts should match the total expense amount.
 */
@Component
public class ExactAmountStrategy implements ExpenseSplitStrategy {

    @Override
    public List<ExpenseShare> calculateShares(Expense expense, List<User> involvedUsers, List<BigDecimal> splitParameters) {
        if (involvedUsers == null || involvedUsers.isEmpty()) {
            throw new IllegalArgumentException("Involved users cannot be empty for exact amount split.");
        }

        if (splitParameters == null || splitParameters.isEmpty()) {
            throw new IllegalArgumentException("Split parameters (exact amounts) cannot be empty for exact amount split.");
        }

        if (involvedUsers.size() != splitParameters.size()) {
            throw new IllegalArgumentException("Number of exact amounts must match number of involved users.");
        }

        // Validate amounts and calculate total
        BigDecimal totalAmount = expense.getAmount();
        BigDecimal sumOfExactAmounts = BigDecimal.ZERO;

        for (BigDecimal amount : splitParameters) {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Exact amounts must be non-negative.");
            }
            sumOfExactAmounts = sumOfExactAmounts.add(amount);
        }

        // Verify that the sum matches the total expense
        if (sumOfExactAmounts.compareTo(totalAmount) != 0) {
            throw new IllegalArgumentException(
                    "Sum of exact amounts (" + sumOfExactAmounts + ") does not match total expense amount (" + totalAmount + ")."
            );
        }

        List<ExpenseShare> shares = new ArrayList<>();

        for (int i = 0; i < involvedUsers.size(); i++) {
            User user = involvedUsers.get(i);
            BigDecimal exactAmount = splitParameters.get(i);

            if (exactAmount.compareTo(BigDecimal.ZERO) > 0) {
                shares.add(new ExpenseShare(expense, user, exactAmount));
            }
        }

        return shares;
    }
}
