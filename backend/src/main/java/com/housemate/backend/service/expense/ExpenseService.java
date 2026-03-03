package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.shared.enums.ExpenseSplitType;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.expense.ExpenseRepository;
import com.housemate.backend.repository.expense.ExpenseShareRepository;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final DebtService debtService;
    private final ExpenseSplitStrategyFactory strategyFactory;

    @Transactional
    public Expense createExpense(String description, BigDecimal amount, User payer, 
                                 ExpenseSplitType splitType, List<User> involvedUsers, 
                                 List<BigDecimal> splitParameters) {
                                 
        // 1. Create and persist the root Expense entity
        Expense expense = new Expense(description, amount, payer, splitType);
        expense = expenseRepository.save(expense);

        // 2. Select the correct strategy dynamically using the factory
        var strategy = strategyFactory.getStrategy(splitType);
        
        // 3. Calculate shares
        List<ExpenseShare> shares = strategy.calculateShares(expense, involvedUsers, splitParameters);
        expenseShareRepository.saveAll(shares);

        // 4. Update the graph of Debts
        for (ExpenseShare share : shares) {
            // The payer doesn't owe themselves
            if (!share.getUser().equals(payer)) {
                debtService.addDebt(share.getUser(), payer, share.getAmount());
            }
        }

        return expense;
    }
}