package com.housemate.shared.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO representing a user login request.
 */
public record LoginRequestDTO(
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password cannot be blank")
    String password
) {}
