package com.housemate.shared.dto.expense.request;

import java.util.UUID;

import com.housemate.shared.enums.UserTransactionRole;
import com.housemate.shared.utils.types.DateRange;

public record TransactionFilterRequestDTO(
    UUID householdId,
    UserTransactionRole userTransactionRole,
    DateRange dateRange,
    String description
) {}
