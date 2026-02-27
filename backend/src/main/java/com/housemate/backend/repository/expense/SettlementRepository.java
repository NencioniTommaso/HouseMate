package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.backend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    // Find all settlements for a specific debt
    List<Settlement> findByDebt(Debt debt);

    // Find all settlements made by a specific debtor
    List<Settlement> findByDebtor(User debtor);

    // Find all settlements received by a specific creditor
    List<Settlement> findByCreditor(User creditor);

    // Find all settlements between two users
    List<Settlement> findByDebtorAndCreditor(User debtor, User creditor);

    // Find settlements within a date range
    List<Settlement> findBySettlementDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find settlements for a specific debt within a date range
    List<Settlement> findByDebtAndSettlementDateBetween(Debt debt, LocalDateTime startDate, LocalDateTime endDate);

    // Find all settlements ordered by date (most recent first)
    List<Settlement> findAllByOrderBySettlementDateDesc();

}
