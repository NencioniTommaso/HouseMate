package com.housemate.backend.repository.chore;

import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.shared.enums.ChoreStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChoreAssignmentRepository extends JpaRepository<ChoreAssignment, UUID>, JpaSpecificationExecutor<ChoreAssignment> {

    List<ChoreAssignment> findAllByAssignedUserId(UUID userId);

    List<ChoreAssignment> findAllByAssignedUserIdAndChoreStatus(UUID userId, ChoreStatus status);

    List<ChoreAssignment> findByAssignedUserIdAndChoreStatusIn(UUID userId, List<ChoreStatus> statuses);

    List<ChoreAssignment> findByAssignedChoreIdOrderByDueDateDesc(UUID choreId);

    List<ChoreAssignment> findByAssignedChore_Household_Id(UUID householdId);

    List<ChoreAssignment> findByChoreStatusAndAssignedChore_Household_Id(ChoreStatus status, UUID householdId);

    int countByAssignedChore_Household_IdAndChoreStatus(UUID householdId, ChoreStatus status);

    int countByAssignedUserIdAndChoreStatus(UUID userId, ChoreStatus status);

}
