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
 * Strategy for splitting an expense equally among all involved users.
 * Handles rounding by distributing the remainder to the payer.
 */
@Component
public class EqualSplitStrategy implements ExpenseSplitStrategy {

    @Override
    public List<ExpenseShare> calculateShares(Expense expense, List<User> involvedUsers, List<BigDecimal> splitParameters) {
        if (involvedUsers == null || involvedUsers.isEmpty()) {
            throw new IllegalArgumentException("Involved users cannot be empty for equal split.");
        }

        BigDecimal totalAmount = expense.getAmount();
        BigDecimal divisor = new BigDecimal(involvedUsers.size());
        BigDecimal splitAmount = totalAmount.divide(divisor, 2, RoundingMode.HALF_DOWN);
        
        // Calculate remainder
        BigDecimal totalDistributed = splitAmount.multiply(divisor);
        BigDecimal remainder = totalAmount.subtract(totalDistributed);

        List<ExpenseShare> shares = new ArrayList<>();
        User payer = expense.getPayer();

        for (User user : involvedUsers) {
            BigDecimal userShare = splitAmount;
            
            // Add remainder to payer's share
            if (user.equals(payer) && remainder.compareTo(BigDecimal.ZERO) > 0) {
                userShare = userShare.add(remainder);
            }
            
            shares.add(new ExpenseShare(expense, user, userShare));
        }

        return shares;
    }
}
