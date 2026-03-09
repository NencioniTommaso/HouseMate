package com.housemate.shared.dto.expense.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a settlement record to be displayed on the client.
 */
public record SettlementResponseDTO(
    UUID id,
    UUID debtId,
    UUID debtorId,
    String debtorName,
    UUID creditorId,
    String creditorName,
    BigDecimal amount,
    LocalDateTime date
) {}
