package com.housemate.backend.repository.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.utils.types.DateRange;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuerySpecification {

    public static Specification<Debt> buildDebtFilter(UUID userId, UUID householdId, DebtFilterRequestDTO filter) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId cannot be null");
        }
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by household
            predicates.add(criteriaBuilder.equal(root.get("household").get("id"), householdId));
            
            // 2. Filter by user role (DEBTOR or CREDITOR)
            if (filter.userTransactionRole() != null) {
                switch (filter.userTransactionRole()) {
                    case DEBTOR:
                        // User is the debtor (owes money)
                        predicates.add(criteriaBuilder.equal(root.get("debtor").get("id"), userId));
                        
                        // If involvedId is specified, filter by specific creditor
                        if (filter.involvedId() != null) {
                            predicates.add(criteriaBuilder.equal(root.get("creditor").get("id"), filter.involvedId()));
                        }
                        break;

                    case CREDITOR:
                        // User is the creditor (is owed money)
                        predicates.add(criteriaBuilder.equal(root.get("creditor").get("id"), userId));
                        
                        // If involvedId is specified, filter by specific debtor
                        if (filter.involvedId() != null) {
                            predicates.add(criteriaBuilder.equal(root.get("debtor").get("id"), filter.involvedId()));
                        }
                        break;

                    default:
                        break;
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Expense> buildExpenseFilter(UUID userId, UUID householdId, TransactionFilterRequestDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by Household (or use provided one)
            if (householdId != null) {
                predicates.add(criteriaBuilder.equal(root.get("household").get("id"), householdId));
            }
            
            if (filter.userTransactionRole() != null) {
                switch (filter.userTransactionRole()) {
                    case CREDITOR:
                        // The user paid for the expense (CREDITOR = payer in expense context)
                        predicates.add(criteriaBuilder.equal(root.get("payer").get("id"), userId));
                        break;

                    case DEBTOR:
                        // The user owes money for this expense (but is NOT the payer)
                        Join<Expense, ExpenseShare> shareJoin = root.join("shares");
                        predicates.add(criteriaBuilder.equal(shareJoin.get("user").get("id"), userId));
                        
                        // CRITICAL: Exclude expenses where the user is the payer
                        Predicate debtorIsNotPayer = criteriaBuilder.notEqual(root.get("payer").get("id"), userId);
                        predicates.add(debtorIsNotPayer);
                        
                        query.distinct(true);
                        break;

                    case ALL:
                        // The user is EITHER the payer OR they owe money (and are NOT the payer)
                        Join<Expense, ExpenseShare> leftShareJoin = root.join("shares", JoinType.LEFT);
                        
                        Predicate isPayer = criteriaBuilder.equal(root.get("payer").get("id"), userId);
                        Predicate isDebtor = criteriaBuilder.and(
                            criteriaBuilder.equal(leftShareJoin.get("user").get("id"), userId),
                            criteriaBuilder.notEqual(root.get("payer").get("id"), userId)
                        );
                        
                        predicates.add(criteriaBuilder.or(isPayer, isDebtor));
                        
                        query.distinct(true);
                        break;

                    default:
                        break;
                }
            }

            // 2. Filter by Description (case-insensitive partial match)
            if (filter.description() != null && !filter.description().isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + filter.description().toLowerCase() + "%"
                ));
            }

            // 3. Filter by Date Range
            applyDateRange(root.get("date"), filter.dateRange(), criteriaBuilder, predicates);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Settlement> buildSettlementFilter(UUID userId, UUID householdId, TransactionFilterRequestDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by Household (direct field)
            if (householdId != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("household").get("id"), 
                    householdId
                ));
            }

            // 2. Filter by User Involvement
            if (filter.userTransactionRole() != null) {
                switch (filter.userTransactionRole()) {
                    case DEBTOR:
                        // User is paying the debt (debtor)
                        predicates.add(criteriaBuilder.equal(root.get("debtor").get("id"), userId));
                        break;

                    case CREDITOR:
                        // User is receiving the payment (creditor)
                        predicates.add(criteriaBuilder.equal(root.get("creditor").get("id"), userId));
                        break;

                    case ALL:
                        // User is EITHER debtor OR creditor in the settlement
                        Predicate isDebtor = criteriaBuilder.equal(root.get("debtor").get("id"), userId);
                        Predicate isCreditor = criteriaBuilder.equal(root.get("creditor").get("id"), userId);
                        predicates.add(criteriaBuilder.or(isDebtor, isCreditor));
                        break;

                    default:
                        break;
                }
            }

            // 3. Filter by Description (case-insensitive partial match)
            if (filter.description() != null && !filter.description().isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + filter.description().toLowerCase() + "%"
                ));
            }

            // 4. Filter by Date Range (Using our DRY helper method!)
            applyDateRange(root.get("settlementDate"), filter.dateRange(), criteriaBuilder, predicates);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Reusable helper method to apply date range filtering across different entities.
     */
    private static void applyDateRange(
            jakarta.persistence.criteria.Path<java.time.LocalDateTime> datePath, 
            DateRange dateRange, 
            jakarta.persistence.criteria.CriteriaBuilder cb, 
            List<Predicate> predicates) {
        
        if (dateRange == null) return;

        if (dateRange.startDate() != null && dateRange.endDate() != null) {
            predicates.add(cb.between(datePath, dateRange.startDate(), dateRange.endDate()));
        } else if (dateRange.startDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(datePath, dateRange.startDate()));
        } else if (dateRange.endDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(datePath, dateRange.endDate()));
        }
    }
} 