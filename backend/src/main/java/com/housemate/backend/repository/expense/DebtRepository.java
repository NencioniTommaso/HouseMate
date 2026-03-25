package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DebtRepository extends JpaRepository<Debt, UUID>, JpaSpecificationExecutor<Debt> {

    // REQUIRED for the DebtService netting algorithm
    Optional<Debt> findByDebtorAndCreditorAndHousehold(User debtor, User creditor, Household household);

}
