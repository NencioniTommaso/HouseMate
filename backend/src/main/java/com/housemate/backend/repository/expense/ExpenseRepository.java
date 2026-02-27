package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // Find all expenses for a specific payer
    List<Expense> findByPayer(User payer);

    // Find all expenses within a date range
    List<Expense> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find all expenses by payer within a date range
    List<Expense> findByPayerAndDateBetween(User payer, LocalDateTime startDate, LocalDateTime endDate);

    // Find all expenses ordered by date (most recent first)
    List<Expense> findAllByOrderByDateDesc();

}
