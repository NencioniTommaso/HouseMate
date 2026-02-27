package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {

    // Find all shares for a specific expense
    List<ExpenseShare> findByExpense(Expense expense);

    // Find all shares for a specific user
    List<ExpenseShare> findByUser(User user);

    // Find all shares for both a specific expense and user
    List<ExpenseShare> findByExpenseAndUser(Expense expense, User user);

    // Find all shares for a user across all expenses
    List<ExpenseShare> findAllByUser(User user);

    // Delete all shares for a specific expense (useful when deleting an expense)
    void deleteByExpense(Expense expense);

}
