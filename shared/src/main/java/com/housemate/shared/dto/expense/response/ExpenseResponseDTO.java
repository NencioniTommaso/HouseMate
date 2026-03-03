package com.housemate.shared.dto.expense.response;

import com.housemate.shared.enums.ExpenseSplitType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a fully persisted Expense to be displayed on the client.
 */
public record ExpenseResponseDTO(
    UUID id,
    String description,
    LocalDateTime date,
    BigDecimal amount,
    UUID payerId,
    String payerFullName, // Flattened from User entity to avoid exposing passwords/emails
    ExpenseSplitType splitType,
    List<ExpenseShareResponseDTO> shares
) {}