package com.housemate.shared.dto.chore.request;

import com.housemate.shared.enums.ChoreStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * DTO representing a request to update a chore's status.
 */
public record ChoreStatusUpdateRequestDTO(
    @NotNull(message = "Chore status cannot be null")
    ChoreStatus newStatus,

    @NotNull(message = "Assignment cannot be null")
    UUID assignmentId
) {}
