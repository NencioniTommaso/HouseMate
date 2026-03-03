package com.housemate.shared.dto.expense;

import java.time.LocalDate;
import java.util.UUID;

public record ExpenseDTO(UUID id, String description, double amount, String payerName, LocalDate date) {}
