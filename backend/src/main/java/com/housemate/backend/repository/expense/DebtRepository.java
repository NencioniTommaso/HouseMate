package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DebtRepository extends JpaRepository<Debt, UUID>, JpaSpecificationExecutor<Debt> {

    // REQUIRED for the DebtService netting algorithm
    Optional<Debt> findByDebtorAndCreditorAndHousehold(User debtor, User creditor, Household household);

    @Query("""
        SELECT COALESCE(SUM(d.amount), 0)
        FROM Debt d
        WHERE d.debtor.id = :userId
        AND d.household.id = :householdId
        """)
    BigDecimal sumAmountByDebtorIdAndHouseholdId(
        @Param("userId") UUID userId,
        @Param("householdId") UUID householdId
    );

    @Query("""
        SELECT COALESCE(SUM(d.amount), 0)
        FROM Debt d
        WHERE d.creditor.id = :userId
        AND d.household.id = :householdId
        """)
    BigDecimal sumAmountByCreditorIdAndHouseholdId(
        @Param("userId") UUID userId,
        @Param("householdId") UUID householdId
    );

}
