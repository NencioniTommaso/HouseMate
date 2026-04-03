package com.housemate.shared.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.housemate.shared.utils.validation.ValidationPatterns;

/**
 * DTO representing a partial request to update user information.
 */
public record UserUpdateRequestDTO(
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    String name,

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    String surname,

    @Email(message = "Email must be a valid email address")
    String email,

    @Pattern(regexp = ValidationPatterns.IBAN, message = "IBAN must be a valid IBAN format")
    String iban,

    @Pattern(regexp = ValidationPatterns.PAYMENT_LINK, message = "Payment link must be a valid URL starting with http:// or https://")
    String paymentLink
) {}
