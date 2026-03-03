package com.housemate.shared.dto.chore;

import java.time.LocalDate;
import java.util.UUID;

public record ChoreAssignmentDTO(UUID assignmentId, String choreDescription, String assignedUserName, LocalDate dueDate, String status) {}
