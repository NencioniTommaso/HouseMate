package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseFilterRequestDTO;
import com.housemate.shared.utils.types.DateRange;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class QuerySpecification {

    public static Specification<Debt> buildDebtFilter(DebtFilterRequestDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // If householdId is present, add: WHERE household_id = ?
            if (filter.householdId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("household").get("id"), filter.householdId()));
            }
            
            // If debtorId is present, add: AND debtor_id = ?
            if (filter.debtorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("debtor").get("id"), filter.debtorId()));
            }

            // If creditorId is present, add: AND creditor_id = ?
            if (filter.creditorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("creditor").get("id"), filter.creditorId()));
            }

            // Combine all predicates with an AND operator
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Expense> buildExpenseFilter(ExpenseFilterRequestDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by Household
            if (filter.householdId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("household").get("id"), filter.householdId()));
            }
            
            // 2. Filter strictly by Payer
            if (filter.payerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("payer").get("id"), filter.payerId()));
            }

            // 3. Filter by Involvement
            if (filter.involvedId() != null) {
                Join<Expense, ExpenseShare> sharesJoin = root.join("shares", JoinType.LEFT);
                Predicate isSharedUser = criteriaBuilder.equal(sharesJoin.get("user").get("id"), filter.involvedId());

                if (filter.payerId() != null) {
                    // If payerId is already provided, 'involvedId' strictly means they must be in the shares.
                    // We drop the "OR isPayer" condition.
                    predicates.add(isSharedUser);
                } else {
                    // If no payerId is provided, 'involvedId' means they can be EITHER the payer OR in the shares.
                    Predicate isPayer = criteriaBuilder.equal(root.get("payer").get("id"), filter.involvedId());
                    predicates.add(criteriaBuilder.or(isPayer, isSharedUser));
                }

                // Enforce distinct results to avoid duplicates from the SQL LEFT JOIN
                query.distinct(true);
            }

            // 4. Filter by Date Range
            if (filter.dateRange() != null) {
                DateRange dateRange = filter.dateRange();
                if (dateRange.startDate() != null && dateRange.endDate() != null) {
                    predicates.add(criteriaBuilder.between(
                        root.get("date"),
                        dateRange.startDate(),
                        dateRange.endDate()
                    ));
                } else if (dateRange.startDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateRange.startDate()));
                } else if (dateRange.endDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateRange.endDate()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
} 