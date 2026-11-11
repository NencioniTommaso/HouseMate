package com.housemate.backend.repository.chore;

import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.shared.enums.ChoreStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ChoreAssignmentRepository extends JpaRepository<ChoreAssignment, UUID>, JpaSpecificationExecutor<ChoreAssignment> {

    int countByAssignedChore_Household_IdAndChoreStatus(UUID householdId, ChoreStatus status);

    int countByAssignedUserIdAndChoreStatus(UUID userId, ChoreStatus status);

    //doing this with a direct SQL query is more efficient than loading all the objects into memory
    @Modifying
    @Query("UPDATE ChoreAssignment c SET c.choreStatus = 'OVERDUE' WHERE c.choreStatus = 'PENDING' AND c.dueDate < :now")
    int markOverdueAssignments(@Param("now") LocalDateTime now);

}
