package com.housemate.shared.dto.chore.request;

import com.housemate.shared.enums.ChoreStatus;
import jakarta.validation.constraints.NotNull;


/**
 * DTO representing a request to update a chore's status.
 */
public record ChoreStatusUpdateRequestDTO(
    @NotNull(message = "Chore status cannot be null")
    ChoreStatus newStatus
) {}
