package com.housemate.backend.repository.chore;

import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.shared.dto.chore.request.ChoreAssignmentFilterRequestDTO;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChoreAssignmentSpecification {

    public static Specification<ChoreAssignment> buildAssignmentFilter(UUID householdId,
                                                                       ChoreAssignmentFilterRequestDTO filter){
        if (householdId == null) {
            throw new IllegalArgumentException("householdId cannot be null");
        }

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by householdId
            predicates.add(criteriaBuilder.equal(root.get("assignedChore").get("household").get("id"), householdId));

            // Filter by statuses
            if (filter.statuses() != null && !filter.statuses().isEmpty()) {
                predicates.add(root.get("choreStatus").in(filter.statuses()));
            }

            // Filter by assigneeId
            if (filter.assigneeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedUser").get("id"), filter.assigneeId()));
            }

            // Filter by descriptionContains
            if (filter.descriptionContains() != null && !filter.descriptionContains().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("assignedChore").get("description")),
                        "%" + filter.descriptionContains().toLowerCase() + "%"));
            }

            // Filter by date range
            if (filter.dateRange() != null) {
                if (filter.dateRange().startDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"),
                                                                        filter.dateRange().startDate()));
                }
                if (filter.dateRange().endDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"),
                                                                     filter.dateRange().endDate()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
