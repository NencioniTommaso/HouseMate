package com.housemate.shared.dto.expense.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.housemate.shared.enums.UserTransactionRole;

/**
 * DTO representing a settlement record to be displayed on the client.
 */
public record SettlementResponseDTO(
    UUID settlementId,
    UserTransactionRole userTransactionRole,       //CREDITOR means logged user is credited (receives money), DEBITOR means logged user owes money
    UUID involvedId,
    String involvedName,
    BigDecimal amount,
    LocalDateTime date,
    String description,
    UUID householdId
) {}
