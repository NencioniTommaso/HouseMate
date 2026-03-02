package com.housemate.backend.repository.chore;

import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.backend.model.chore.ChoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChoreAssignmentRepository extends JpaRepository<ChoreAssignment, UUID> {

    List<ChoreAssignment> findByAssignedUserId(UUID userId);

    List<ChoreAssignment> findByAssignedUserIdAndChoreStatus(UUID userId, ChoreStatus status);

    List<ChoreAssignment> findByAssignedUserIdAndChoreStatusIn(UUID userId, List<ChoreStatus> statuses);

    List<ChoreAssignment> findByAssignedChoreIdOrderByDueDateDesc(UUID choreId);
}
