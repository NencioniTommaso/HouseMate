package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID>, JpaSpecificationExecutor<Settlement> {

	@Query("""
		SELECT COALESCE(SUM(s.amount), 0)
		FROM Settlement s
		WHERE s.debtor.id = :userId
		AND s.household.id = :householdId
		AND s.settlementDate >= :startDate
		AND s.settlementDate < :endDateExclusive
		""")
	BigDecimal sumAmountByDebtorIdAndHouseholdIdForDateRange(
			@Param("userId") UUID userId,
			@Param("householdId") UUID householdId,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDateExclusive") LocalDateTime endDateExclusive
	);

	@Query("""
		SELECT SUM(s.amount)
		FROM Settlement s
		WHERE s.debtor.id = :userId
		AND s.household.id = :householdId
		AND s.settlementDate >= :startDate
		AND s.settlementDate < :endDateExclusive
		""")
	BigDecimal sumSettlementsPaid(
			@Param("userId") UUID userId,
			@Param("householdId") UUID householdId,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDateExclusive") LocalDateTime endDateExclusive
	);

	@Query("""
		SELECT SUM(s.amount)
		FROM Settlement s
		WHERE s.creditor.id = :userId
		AND s.household.id = :householdId
		AND s.settlementDate >= :startDate
		AND s.settlementDate < :endDateExclusive
		""")
	BigDecimal sumSettlementsReceived(
			@Param("userId") UUID userId,
			@Param("householdId") UUID householdId,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDateExclusive") LocalDateTime endDateExclusive
	);

}
