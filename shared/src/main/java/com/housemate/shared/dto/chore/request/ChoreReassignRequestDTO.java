package com.housemate.shared.dto.chore.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChoreReassignRequestDTO(
        @NotNull(message = "New assignee id cannot be null")
        UUID newAssigneeId
) {}
