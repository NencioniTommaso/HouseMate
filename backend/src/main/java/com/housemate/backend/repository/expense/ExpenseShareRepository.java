package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, UUID> {

	@Query("""
		SELECT COALESCE(SUM(es.amount), 0)
		FROM ExpenseShare es
		WHERE es.user.id = :userId
		AND es.expense.payer.id = :userId
		AND es.expense.household.id = :householdId
		AND es.expense.date >= :startDate
		AND es.expense.date < :endDateExclusive
		""")
	BigDecimal sumUserOwnSharesAsPayerForDateRange(
			@Param("userId") UUID userId,
			@Param("householdId") UUID householdId,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDateExclusive") LocalDateTime endDateExclusive
	);

}
