package com.housemate.shared.dto.household.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO representing a request to add a member to a household.
 */
public record AddMemberRequestDTO(
    @NotBlank(message = "User email cannot be blank")
    @Email(message = "Email must be a valid email address")
    String userEmail
) {}
