package com.housemate.backend.service.expense.strategy;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.user.User;
import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy interface for calculating expense shares based on different splitting methods.
 * Each implementation handles a specific expense split type.
 */
public interface ExpenseSplitStrategy {
    /**
     * Calculate shares for an expense.
     *
     * @param expense the expense to split
     * @param involvedUsers the users involved in the expense
     * @param splitParameters parameters specific to the split strategy
     * @return a list of expense shares
     */
    List<ExpenseShare> calculateShares(Expense expense, List<User> involvedUsers, List<BigDecimal> splitParameters);
}