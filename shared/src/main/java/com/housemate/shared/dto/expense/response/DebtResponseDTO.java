package com.housemate.shared.dto.expense.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.housemate.shared.enums.UserTransactionRole;

public record DebtResponseDTO(
    UUID debtId,
    UserTransactionRole userTransactionRole,       //CREDITOR means logged user is credited (receives money), DEBITOR means logged user owes money
    UUID involvedId,
    String involvedName,
    BigDecimal amount
) {}