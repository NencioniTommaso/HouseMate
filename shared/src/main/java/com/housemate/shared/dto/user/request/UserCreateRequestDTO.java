package com.housemate.shared.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.housemate.shared.utils.validation.ValidationPatterns;

/**
 * DTO representing the payload required to create a new User.
 */
public record UserCreateRequestDTO(
    @NotBlank(message = "First name cannot be blank")
    String name,

    @NotBlank(message = "Last name cannot be blank")
    String surname,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password cannot be blank")
    String password,

    @NotBlank(message = "IBAN cannot be blank")
    @Pattern(regexp = ValidationPatterns.IBAN, message = "IBAN must be a valid IBAN format")
    String iban,

    @Pattern(regexp = ValidationPatterns.PAYMENT_LINK, message = "Payment link must be a valid URL starting with http:// or https://")
    String paymentLink
) {}
