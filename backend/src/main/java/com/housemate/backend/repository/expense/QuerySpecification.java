package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseFilterRequestDTO;
import com.housemate.shared.dto.expense.request.SettlementFilterRequestDTO;
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

            // Filter by household
            if (filter.householdId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("household").get("id"), filter.householdId()));
            }
            
            // Filter strictly by debtor
            if (filter.debtorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("debtor").get("id"), filter.debtorId()));
            }

            // Filter strictly by creditor
            if (filter.creditorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("creditor").get("id"), filter.creditorId()));
            }

            // Filter by user involvement (User is either the debtor OR the creditor)
            if (filter.involvedId() != null && filter.debtorId() == null && filter.creditorId() == null) {
                Predicate isDebtor = criteriaBuilder.equal(root.get("debtor").get("id"), filter.involvedId());
                Predicate isCreditor = criteriaBuilder.equal(root.get("creditor").get("id"), filter.involvedId());
                predicates.add(criteriaBuilder.or(isDebtor, isCreditor));
            }

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

    public static Specification<Settlement> buildSettlementFilter(SettlementFilterRequestDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by specific Debt
            if (filter.debtId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("debt").get("id"), filter.debtId()));
            }

            // 2. Filter strictly by Debtor (the one paying)
            if (filter.debtorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("debtor").get("id"), filter.debtorId()));
            }

            // 3. Filter strictly by Creditor (the one receiving)
            if (filter.creditorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("creditor").get("id"), filter.creditorId()));
            }

            // 4. Filter by Involvement (User is either the debtor OR the creditor)
            if (filter.involvedId() != null) {
                Predicate isDebtor = criteriaBuilder.equal(root.get("debtor").get("id"), filter.involvedId());
                Predicate isCreditor = criteriaBuilder.equal(root.get("creditor").get("id"), filter.involvedId());
                predicates.add(criteriaBuilder.or(isDebtor, isCreditor));
            }

            // 5. Filter by Date Range (Note: The field in Settlement entity is named 'settlementDate')
            if (filter.dateRange() != null) {
                DateRange dateRange = filter.dateRange();
                if (dateRange.startDate() != null && dateRange.endDate() != null) {
                    predicates.add(criteriaBuilder.between(
                        root.get("settlementDate"),
                        dateRange.startDate(),
                        dateRange.endDate()
                    ));
                } else if (dateRange.startDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("settlementDate"), dateRange.startDate()));
                } else if (dateRange.endDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("settlementDate"), dateRange.endDate()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
} 