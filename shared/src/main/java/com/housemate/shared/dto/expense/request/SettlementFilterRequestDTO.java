package com.housemate.shared.dto.expense.request;

import java.util.UUID;
import com.housemate.shared.utils.types.DateRange;

public record SettlementFilterRequestDTO(
    UUID debtId,       // Filter by a specific debt
    UUID debtorId,     // Filter by the person who paid the settlement
    UUID creditorId,   // Filter by the person who received the settlement
    UUID involvedId,   // Filter if the user is either the debtor OR the creditor
    DateRange dateRange 
) {}