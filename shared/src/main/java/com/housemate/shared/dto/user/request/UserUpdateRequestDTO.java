package com.housemate.shared.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO representing a request to update user information.
 */
public record UserUpdateRequestDTO(
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "IBAN cannot be blank")
    @Pattern(regexp = "^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$", message = "IBAN must be a valid IBAN format")
    String iban
) {}
