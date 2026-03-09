package com.housemate.shared.dto.expense.request;

import java.util.UUID;

/**
 * DTO representing the query parameters used to filter the debts list.
 * All fields are optional (nullable) because a client might filter by just one, 
 * multiple, or none of these criteria.
 */
public record DebtFilterRequestDTO(
    UUID householdId,
    UUID debtorId,
    UUID creditorId,
    UUID userId
) {}