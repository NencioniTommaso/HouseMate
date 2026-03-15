package com.housemate.shared.dto.expense.response;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExpenseShareResponseDTO(
    @NotNull(message = "Expense Share ID cannot be null")
    UUID id,

    @NotNull(message = "User ID cannot be null")
    UUID userId,

    @NotBlank(message = "User full name cannot be blank")
    String userFullName,

    @NotNull(message = "Amount cannot be null")
    BigDecimal amount
) {}