package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DebtRepository extends JpaRepository<Debt, UUID>, JpaSpecificationExecutor<Debt> {

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

    // Find all debts in a household
    List<Debt> findByHousehold(Household household);

    // Find all debts owed by a user in a specific household
    List<Debt> findByDebtorAndHousehold(User debtor, Household household);

    // Find all debts owed to a user in a specific household
    List<Debt> findByCreditorAndHousehold(User creditor, Household household);

    // Find all debts for a user (as debtor or creditor) in a specific household
    @Query("SELECT d FROM Debt d WHERE (d.debtor = :user OR d.creditor = :user) AND d.household = :household")
    List<Debt> findByUserAndHousehold(@Param("user") User user, @Param("household") Household household);
}
