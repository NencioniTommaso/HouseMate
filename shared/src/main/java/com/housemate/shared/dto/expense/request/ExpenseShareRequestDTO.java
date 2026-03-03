package com.housemate.shared.dto.expense.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO representing a user's share of an expense during creation.
 */
public record ExpenseShareRequestDTO(
    @NotNull(message = "User ID cannot be null")
    UUID userId,

    @NotNull(message = "Share amount cannot be null")
    @DecimalMin(value = "0.01", message = "Share amount must be strictly positive")
    BigDecimal amount
) {}