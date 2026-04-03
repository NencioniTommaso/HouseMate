package com.housemate.shared.dto.household.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO representing a request for the current authenticated user to join a household.
 */
public record AddMemberRequestDTO(
    @NotBlank(message = "Invitation code cannot be blank")
    @Size(max = 64, message = "Invitation code cannot exceed 64 characters")
    String invitationCode
) {}
