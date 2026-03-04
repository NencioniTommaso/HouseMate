package com.housemate.shared.dto.chore.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO required to create a new chore assignment to be displayed on the client.
 */
public record ChoreAssignmentCreateRequestDTO(
        @NotNull(message = "Chore ID cannot be null")
        UUID choreId,

        @NotNull(message = "Assigned user ID cannot be null")
        UUID assignedUserId,

        LocalDateTime dueDate
) {}
