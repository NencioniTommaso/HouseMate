package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.shared.enums.ExpenseSplitType;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.repository.expense.ExpenseRepository;
import com.housemate.backend.repository.expense.ExpenseShareRepository;
import com.housemate.backend.repository.household.HouseholdMembershipRepository;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final HouseholdMembershipRepository householdMembershipRepository;
    private final DebtService debtService;
    private final ExpenseSplitStrategyFactory strategyFactory;

    /**
     * Create a new expense and calculate shares using the appropriate strategy.
     */
    @Transactional
    public Expense createExpense(String description, BigDecimal amount, User payer, Household household,
                                 ExpenseSplitType splitType, List<User> involvedUsers, 
                                 List<BigDecimal> splitParameters) {
                                 
        // 1. Create and persist the root Expense entity
        Expense expense = new Expense(description, amount, payer, household, splitType);
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
                debtService.addDebt(share.getUser(), payer, household, share.getAmount());
            }
        }

        return expense;
    }

    /**
     * Get all expenses for a household within a date range.
     *
     * @param household the household
     * @param startDate the start of the date range (inclusive)
     * @param endDate   the end of the date range (inclusive)
     * @return list of expenses ordered by date (most recent first)
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByHouseholdAndDateRange(Household household, LocalDateTime startDate, LocalDateTime endDate) {
        return expenseRepository.findByHouseholdAndDateRange(household, startDate, endDate);
    }

    /**
     * Get all expenses for a household.
     *
     * @param household the household
     * @return list of expenses ordered by date (most recent first)
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesForHousehold(Household household) {
        return expenseRepository.findByHousehold(household);
    }

    /**
     * Get all expenses paid by a user in a household.
     * If household is null, defaults to the user's current household.
     *
     * @param payer     the user who paid the expenses
     * @param household the household (if null, uses user's current household)
     * @return list of expenses ordered by date (most recent first), or empty list if no household available
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByPayerInHousehold(User payer, Household household) {
        if (household == null) {
            Optional<HouseholdMembership> membership = householdMembershipRepository.findByUser(payer)
                    .stream()
                    .findFirst();
            
            if (membership.isEmpty()) {
                return List.of();
            }
            household = membership.get().getHousehold();
        }
        
        return expenseRepository.findByPayerAndHousehold(payer, household);
    }

    /**
     * Get all expenses paid by a user in a household within a date range.
     * If household is null, defaults to the user's current household.
     *
     * @param payer     the user who paid the expenses
     * @param household the household (if null, uses user's current household)
     * @param startDate the start of the date range (inclusive)
     * @param endDate   the end of the date range (inclusive)
     * @return list of expenses ordered by date (most recent first), or empty list if no household available
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByPayerInHouseholdAndDateRange(User payer, Household household, LocalDateTime startDate, LocalDateTime endDate) {
        if (household == null) {
            Optional<HouseholdMembership> membership = householdMembershipRepository.findByUser(payer)
                    .stream()
                    .findFirst();
            
            if (membership.isEmpty()) {
                return List.of();
            }
            household = membership.get().getHousehold();
        }
        
        return expenseRepository.findByPayerAndHouseholdAndDateRange(payer, household, startDate, endDate);
    }
}