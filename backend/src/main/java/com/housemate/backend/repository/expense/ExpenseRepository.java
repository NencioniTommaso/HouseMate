package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Find expenses for a household within a date range, ordered by date
    @Query("SELECT e FROM Expense e WHERE e.household = :household AND e.date BETWEEN :startDate AND :endDate ORDER BY e.date DESC")
    List<Expense> findByHouseholdAndDateRange(@Param("household") Household household, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find all expenses for a household, ordered by date
    @Query("SELECT e FROM Expense e WHERE e.household = :household ORDER BY e.date DESC")
    List<Expense> findByHousehold(@Param("household") Household household);

    // Find all expenses paid by a specific user in a household, ordered by date
    @Query("SELECT e FROM Expense e WHERE e.payer = :payer AND e.household = :household ORDER BY e.date DESC")
    List<Expense> findByPayerAndHousehold(@Param("payer") User payer, @Param("household") Household household);

    // Find all expenses paid by a specific user in a household within a date range, ordered by date
    @Query("SELECT e FROM Expense e WHERE e.payer = :payer AND e.household = :household AND e.date BETWEEN :startDate AND :endDate ORDER BY e.date DESC")
    List<Expense> findByPayerAndHouseholdAndDateRange(@Param("payer") User payer, @Param("household") Household household, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
