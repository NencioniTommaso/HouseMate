package com.housemate.shared.dto.expense;

import java.util.UUID;

public record SettlementCreateDTO(UUID debtorId, UUID creditorId, double amount) {}
