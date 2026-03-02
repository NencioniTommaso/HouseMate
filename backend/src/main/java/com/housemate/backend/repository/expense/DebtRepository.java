package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DebtRepository extends JpaRepository<Debt, UUID> {

    // Find all debts owed by a specific debtor
    List<Debt> findByDebtor(User debtor);

    // Find all debts owed to a specific creditor
    List<Debt> findByCreditor(User creditor);

    // Find a specific debt between debtor and creditor
    List<Debt> findByDebtorAndCreditor(User debtor, User creditor);

    // Find all debts for a user (both as debtor and creditor)
    List<Debt> findByDebtorOrCreditor(User debtor, User creditor);

    // Check if there is an existing debt between two users
    boolean existsByDebtorAndCreditor(User debtor, User creditor);

}
