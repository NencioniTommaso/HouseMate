package com.housemate.shared.dto.chore.response;

public record AssignmentOverviewDTO (
    Integer pendingAssignments,
    Integer overdueAssignments
){}
