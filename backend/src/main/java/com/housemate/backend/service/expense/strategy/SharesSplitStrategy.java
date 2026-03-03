package com.housemate.backend.service.expense.strategy;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for splitting an expense based on shares.
 * Each user has a corresponding share value (weight) in splitParameters.
 * The expense is divided proportionally based on these shares.
 * 
 * splitParameters: List of BigDecimal values representing the number of shares for each user
 *                  (must have the same length as involvedUsers and in the same order)
 */
@Component
public class SharesSplitStrategy implements ExpenseSplitStrategy {

    @Override
    public List<ExpenseShare> calculateShares(Expense expense, List<User> involvedUsers, List<BigDecimal> splitParameters) {
        if (involvedUsers == null || involvedUsers.isEmpty()) {
            throw new IllegalArgumentException("Involved users cannot be empty for shares split.");
        }

        if (splitParameters == null || splitParameters.isEmpty()) {
            throw new IllegalArgumentException("Split parameters (shares) cannot be empty for shares split.");
        }

        if (involvedUsers.size() != splitParameters.size()) {
            throw new IllegalArgumentException("Number of shares must match number of involved users.");
        }

        // Calculate total shares
        BigDecimal totalShares = BigDecimal.ZERO;
        for (BigDecimal share : splitParameters) {
            if (share.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Share values must be strictly greater than zero.");
            }
            totalShares = totalShares.add(share);
        }

        BigDecimal totalAmount = expense.getAmount();
        List<ExpenseShare> shares = new ArrayList<>();

        for (int i = 0; i < involvedUsers.size(); i++) {
            User user = involvedUsers.get(i);
            BigDecimal userShares = splitParameters.get(i);
            
            // Calculate this user's share: (userShares / totalShares) * totalAmount
            BigDecimal userShare = userShares
                    .multiply(totalAmount)
                    .divide(totalShares, 2, RoundingMode.HALF_DOWN);
            
            shares.add(new ExpenseShare(expense, user, userShare));
        }

        // Handle rounding remainder by adding to payer's share
        BigDecimal totalDistributed = shares.stream()
                .map(ExpenseShare::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal remainder = totalAmount.subtract(totalDistributed);
        
        if (remainder.compareTo(BigDecimal.ZERO) > 0) {
            for (ExpenseShare share : shares) {
                if (share.getUser().equals(expense.getPayer())) {
                    share.setAmount(share.getAmount().add(remainder));
                    break;
                }
            }
        }

        return shares;
    }
}
