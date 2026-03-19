package com.housemate.backend.service;

import com.housemate.backend.model.chore.Chore;
import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.chore.ChoreAssignmentRepository;
import com.housemate.backend.repository.chore.ChoreAssignmentSpecification;
import com.housemate.backend.repository.chore.ChoreRepository;
import com.housemate.backend.repository.household.HouseholdMembershipRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.chore.request.ChoreAssignmentCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreAssignmentFilterRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.AssignmentOverviewDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.ChoreStatus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMembershipRepository householdMembershipRepository;
    private final ChoreAssignmentRepository choreAssignmentRepository;
    private final UserRepository userRepository;

    @Transactional //executes each transactional method atomically
    public ChoreResponseDTO createChore(@NonNull ChoreCreateRequestDTO dto) {

        Assert.notNull(dto, "No request body was sent");

        Assert.notNull(dto.description(), "Chore description cannot be null");
        Assert.notNull(dto.frequencyDays(), "Frequency days cannot be null");
        Assert.notNull(dto.householdId(), "Household ID cannot be null");

        log.info("Requested creation of new chore {} for household {}", dto.description(), dto.householdId());

        //find the actual household based on the UUID
        Household household = householdRepository.findById(dto.householdId())
                              .orElseThrow(()  -> new IllegalArgumentException("Household with ID: "
                                                                               + dto.householdId() +
                                                                               " not found."));


        Chore existingChore = choreRepository.findByDescriptionAndHouseholdId(dto.description(), dto.householdId());
        if(existingChore != null){
            throw new IllegalArgumentException("Chore with description: " + dto.description() + " already exists in household with ID: " + dto.householdId());
        }

        //istantiate new Chore
        Chore newChore = new Chore();
        newChore.setHousehold(household);
        newChore.setFrequency(dto.frequencyDays());
        newChore.setDescription(dto.description());

        //save to database
        Chore savedChore = choreRepository.save(newChore);
        log.info("Chore saved successfully! Id: {}", savedChore.getId());

        return new ChoreResponseDTO(savedChore.getId(),
                                    savedChore.getDescription(),
                                    savedChore.getFrequency());

    }

    @Transactional
    public void deleteChore(@NonNull UUID choreID) {

        Assert.notNull(choreID, "Chore ID cannot be null");

        log.info("Requested deletion of chore {}", choreID);

        //find the chore to delete
        Chore choreToDelete = choreRepository.findById(choreID)
                                .orElseThrow(() -> new IllegalArgumentException("Chore with ID: " + choreID + " not found."));


        //delete the chore
        choreRepository.delete(choreToDelete);
        log.info("Chore deleted successfully! Id: {}", choreToDelete.getId());

    }

    @Transactional
    public ChoreAssignmentResponseDTO createChoreAssignment(@NonNull ChoreAssignmentCreateRequestDTO dto) {

        Assert.notNull(dto, "No request body was sent");

        Assert.notNull(dto.choreId(), "Chore ID cannot be null");
        Assert.notNull(dto.assignedUserId(), "Assigned user ID cannot be null");

        log.info("Requested creation of new chore assignment for chore {} and user {}", dto.choreId(), dto.assignedUserId());

        Chore choreToAssign = choreRepository.findById(dto.choreId())
                .orElseThrow(() -> new IllegalArgumentException("Chore with ID: " + dto.choreId() + " not found."));

        User userToAssign = userRepository.findById(dto.assignedUserId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + dto.assignedUserId() + " not found."));

        ChoreAssignment newAssignment = new ChoreAssignment(dto.dueDate(), choreToAssign, userToAssign);

        ChoreAssignment savedAssignment = choreAssignmentRepository.save(newAssignment);
        log.info("Chore assignment saved successfully! Id: {}", savedAssignment.getId());

        return new ChoreAssignmentResponseDTO(savedAssignment.getId(),
                savedAssignment.getAssignedChore().getId(),
                savedAssignment.getAssignedChore().getDescription(),
                savedAssignment.getAssignedUser().getName(),
                savedAssignment.getDueDate(),
                savedAssignment.getChoreStatus());
    }

    @Transactional
    public void deleteChoreAssignment(@NonNull UUID assignmentId) {

        Assert.notNull(assignmentId, "Chore assignment ID cannot be null");

        log.info("Requested deletion of chore assignment {}", assignmentId);

        ChoreAssignment assignmentToDelete = choreAssignmentRepository.findById(assignmentId)
                                        .orElseThrow(() -> new IllegalArgumentException("Chore assignment with ID: " + assignmentId + " not found."));

        choreAssignmentRepository.delete(assignmentToDelete);

        log.info("Chore assignment deleted successfully! Id: {}", assignmentToDelete.getId());
    }

    @Transactional
    public void updateChoreAssignmentStatus(@NonNull UUID assignmentId, @NonNull ChoreStatusUpdateRequestDTO dto){

        Assert.notNull(dto, "No request body was sent");
        Assert.notNull(assignmentId, "Assignment ID cannot be null");
        Assert.notNull(dto.newStatus(), "New status cannot be null");

        log.info("Requested status update for chore assignment {}", assignmentId);

        //find the chore assignment
        ChoreAssignment assignment = choreAssignmentRepository.findById(assignmentId)
                                        .orElseThrow(() -> new IllegalArgumentException("Chore assignment with ID: " + assignmentId + " not found."));

        //update the status
        assignment.setChoreStatus(dto.newStatus());

        //save the updated assignment
        choreAssignmentRepository.save(assignment);
        log.info("Chore assignment status updated successfully! Assignment ID: {}, New Status: {}", assignment.getId(), assignment.getChoreStatus());
    }

    @Transactional
    public ChoreAssignmentResponseDTO reassignChore(@NonNull UUID assignmentId, @NonNull UUID newAssigneeId) {

        Assert.notNull(assignmentId, "Assignment ID cannot be null");
        Assert.notNull(newAssigneeId, "New assignee ID cannot be null");

        log.info("Requested reassignment of chore assignment {} to new user {}", assignmentId, newAssigneeId);

        ChoreAssignment assignment = choreAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Chore assignment with ID: " + assignmentId + " not found."));

        User newAssignee = userRepository.findById(newAssigneeId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + newAssigneeId + " not found."));

        assignment.setAssignedUser(newAssignee);

        ChoreAssignment updatedAssignment = choreAssignmentRepository.save(assignment);
        log.info("Chore assignment reassigned successfully! Assignment ID: {}, New Assignee: {}", updatedAssignment.getId(), updatedAssignment.getAssignedUser().getName());

        return new ChoreAssignmentResponseDTO(updatedAssignment.getId(),
                updatedAssignment.getAssignedChore().getId(),
                updatedAssignment.getAssignedChore().getDescription(),
                updatedAssignment.getAssignedUser().getName(),
                updatedAssignment.getDueDate(),
                updatedAssignment.getChoreStatus());
    }

    @Transactional(readOnly = true)
    public List<ChoreResponseDTO> getAllHouseholdChores(@NonNull UUID userId, @NonNull UUID householdId) {

        Assert.notNull(householdId, "Household ID cannot be null");
        Assert.notNull(userId, "User ID cannot be null");

        log.info("Requested retrieval of all chores for household {}", householdId);

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found."));
        boolean isMember = householdMembershipRepository.existsByHouseholdIdAndUserId(householdId, user.getId());
        if(!isMember){
            throw new AccessDeniedException("User with ID: " + userId + " is not a member of household with ID: " + householdId);
        }
        //retrieve all chores for the household
        List<Chore> chores = choreRepository.findAllByHouseholdId(householdId);

        if (chores.isEmpty()) {
            log.warn("No chores found for household with ID: {}", householdId);
            return java.util.Collections.emptyList();
        }

        log.info("Retrieved {} chores for household with ID: {}", chores.size(), householdId);

        List<ChoreResponseDTO> choreResponseDTOs = chores.stream()
            .map(chore -> new ChoreResponseDTO(chore.getId(), chore.getDescription(), chore.getFrequency()))
            .toList();

        return choreResponseDTOs;
    }

    @Transactional
    public void deleteAllChoresForHousehold(@NonNull UUID householdId) {

        Assert.notNull(householdId, "Household ID cannot be null");

        log.info("Requested deletion of all chores for household {}", householdId);

        List<Chore> choresToDelete = choreRepository.findAllByHouseholdId(householdId);

        if (choresToDelete.isEmpty()) {
            log.warn("No chores found for household with ID: {}. No deletion performed.", householdId);
            return;
        }

        choreRepository.deleteAll(choresToDelete);
        log.info("Deleted {} chores for household with ID: {}", choresToDelete.size(), householdId);
    }

    @Transactional(readOnly = true)
    public AssignmentOverviewDTO getAssignmentOverview(@NonNull UUID householdId) {

        Assert.notNull(householdId, "Household ID cannot be null");

        log.info("Requested retrieval of assignment overview for household {}", householdId);

        Household household = householdRepository.findById(householdId)
                                .orElseThrow(() -> new IllegalArgumentException("Household with ID: " + householdId + " not found."));

        Integer pendingAssignments = choreAssignmentRepository.countByAssignedChore_Household_IdAndChoreStatus(householdId, ChoreStatus.PENDING);
        Integer overdueAssignments = choreAssignmentRepository.countByAssignedChore_Household_IdAndChoreStatus(householdId, ChoreStatus.OVERDUE);

        log.info("Retrieved assignment overview for household with ID: {}. Pending Assignments: {}, Overdue Assignments: {}", householdId, pendingAssignments, overdueAssignments);

        return new AssignmentOverviewDTO(pendingAssignments, overdueAssignments);
    }

    @Transactional(readOnly = true)
    public List<ChoreAssignmentResponseDTO> getFilteredChoreAssignments(@NonNull UUID userId,
                                                                        @NonNull UUID currentHouseholdId,
                                                                        @NonNull ChoreAssignmentFilterRequestDTO dto){
        Assert.notNull(userId, "User ID cannot be null");
        Assert.notNull(currentHouseholdId, "Household ID cannot be null");
        Assert.notNull(dto, "No filter DTO provided");

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found."));


        List<UUID> userHouseholdIds = householdMembershipRepository.findAllByUserId(userId)
                                        .stream()
                                        .map(membership -> membership.getHousehold().getId())
                                        .toList();

        if (!userHouseholdIds.contains(currentHouseholdId)) {
            throw new AccessDeniedException("User with ID: " + userId + " is not a member of household with ID: " + currentHouseholdId);
        }

        Specification<ChoreAssignment> spec = ChoreAssignmentSpecification.buildAssignmentFilter(currentHouseholdId, dto);

        List<ChoreAssignment> filteredAssignments = choreAssignmentRepository.findAll(spec);

        if (filteredAssignments.isEmpty()) {
            log.warn("No chore assignments found for user with ID: {} matching filter criteria", userId);
            return java.util.Collections.emptyList();
        }

        log.info("Retrieved {} chore assignments for user with ID: {} matching filter criteria", filteredAssignments.size(), userId);
        return filteredAssignments.stream()
            .map(assignment -> new ChoreAssignmentResponseDTO(assignment.getId(),
                                                              assignment.getAssignedChore().getId(),
                                                              assignment.getAssignedChore().getDescription(),
                                                              assignment.getAssignedUser().getName(),
                                                              assignment.getDueDate(),
                                                              assignment.getChoreStatus()))
            .toList();
    }
}
