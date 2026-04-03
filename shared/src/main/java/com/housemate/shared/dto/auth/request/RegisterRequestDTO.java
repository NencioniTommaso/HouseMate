package com.housemate.shared.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.housemate.shared.utils.validation.ValidationPatterns;

/**
 * DTO representing a user registration request.
 */
public record RegisterRequestDTO(
    @NotBlank(message = "First name cannot be blank")
    String name,

    @NotBlank(message = "Last name cannot be blank")
    String surname,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password cannot be blank")
    String password,

    @Pattern(regexp = ValidationPatterns.IBAN, message = "IBAN must be a valid IBAN format")
    String iban
) {}
