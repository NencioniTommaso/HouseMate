package com.housemate.shared.dto.expense.response;

import java.math.BigDecimal;
import java.util.UUID;

public record DebtResponseDTO(
    UUID id,
    UUID debtorId,
    String debtorName,
    UUID creditorId,
    String creditorName,
    BigDecimal amount
) {}