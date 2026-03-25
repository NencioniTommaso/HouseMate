package com.housemate.shared.dto.expense.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO representing a user's share of an expense during creation.
 */
public record ExpenseShareRequestDTO(
    @NotNull(message = "User ID cannot be null")
    UUID userId,

    BigDecimal share
) {}