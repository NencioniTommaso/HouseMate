package com.housemate.shared.dto.expense.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseShareResponseDTO(
    UUID id,
    UUID userId,
    String userFullName,
    BigDecimal amount
) {}