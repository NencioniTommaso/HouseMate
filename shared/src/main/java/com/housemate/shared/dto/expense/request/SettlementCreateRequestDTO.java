package com.housemate.shared.dto.expense.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO representing the payload required to settle a debt.
 */
public record SettlementCreateRequestDTO(
    @NotNull(message = "Debt ID cannot be null")
    UUID debtId,

    @NotNull(message = "Debtor ID cannot be null")
    UUID debtorId,

    @NotNull(message = "Creditor ID cannot be null")
    UUID creditorId,

    @NotNull(message = "Settlement amount cannot be null")
    @DecimalMin(value = "0.01", message = "Settlement amount must be strictly positive")
    BigDecimal amount
) {}
