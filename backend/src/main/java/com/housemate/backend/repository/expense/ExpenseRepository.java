package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

	long countByHousehold_IdAndDateGreaterThanEqualAndDateLessThan(
		UUID householdId,
		LocalDateTime startDate,
		LocalDateTime endDateExclusive
	);

	@Query("""
		SELECT COALESCE(SUM(e.amount), 0)
		FROM Expense e
		WHERE e.household.id = :householdId
		AND e.date >= :startDate
		AND e.date < :endDateExclusive
		""")
	BigDecimal sumAmountByHouseholdIdForDateRange(
		@Param("householdId") UUID householdId,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDateExclusive") LocalDateTime endDateExclusive
	);

	@Query("""
		SELECT SUM(e.amount)
		FROM Expense e
		WHERE e.payer.id = :userId
		AND e.household.id = :householdId
		AND e.date >= :startDate
		AND e.date < :endDateExclusive
		""")
	BigDecimal sumTotalPaidByPayer(
		@Param("userId") UUID userId,
		@Param("householdId") UUID householdId,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDateExclusive") LocalDateTime endDateExclusive
	);

}
