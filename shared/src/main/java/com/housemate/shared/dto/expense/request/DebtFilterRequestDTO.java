package com.housemate.shared.dto.expense.request;

import java.util.UUID;

import com.housemate.shared.enums.UserTransactionRole;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for filtering debts.
 * HouseholdId is fetched server-side from the user's current household.
 */
public record DebtFilterRequestDTO(
    @NotNull(message = "Transaction role cannot be null")
    UserTransactionRole userTransactionRole,

    UUID involvedId
) {}