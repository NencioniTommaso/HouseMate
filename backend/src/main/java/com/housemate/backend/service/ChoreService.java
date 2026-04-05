package com.housemate.backend.service;

import com.housemate.backend.model.chore.Chore;
import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.chore.ChoreAssignmentRepository;
import com.housemate.backend.repository.chore.ChoreAssignmentSpecification;
import com.housemate.backend.repository.chore.ChoreRepository;
import com.housemate.backend.repository.household.HouseholdMembershipRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.chore.request.*;
import com.housemate.shared.dto.chore.response.AssignmentOverviewDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.ChoreStatus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMembershipRepository householdMembershipRepository;
    private final ChoreAssignmentRepository choreAssignmentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional //executes each transactional method atomically
    public ChoreResponseDTO createChore(@NonNull UUID userId, @NonNull ChoreCreateRequestDTO dto) {

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

        // Check if logged user is a member of the household
        checkIfHouseholdMember(userId, household);

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
    public void deleteChore(@NonNull UUID choreId, @NonNull UUID userId) {

        Assert.notNull(userId, "User ID cannot be null");
        Assert.notNull(choreId, "Chore ID cannot be null");

        log.info("Requested deletion of chore {}", choreId);

        //find the chore to delete
        Chore choreToDelete = choreRepository.findById(choreId)
                                .orElseThrow(() -> new IllegalArgumentException("Chore with ID: " + choreId + " not found."));

        // Check if logged user is a member and admin of the household that the chore belongs to
        Household choreHousehold = choreToDelete.getHousehold();
        checkIfHouseholdMember(userId, choreHousehold);
        checkIfAdminOfHousehold(userId, choreHousehold);

        //delete the chore
        choreRepository.delete(choreToDelete);
        log.info("Chore deleted successfully! Id: {}", choreToDelete.getId());

    }

    @Transactional
    public ChoreAssignmentResponseDTO createChoreAssignment(@NonNull UUID userId, @NonNull ChoreAssignmentCreateRequestDTO dto) {

        Assert.notNull(dto, "No request body was sent");

        Assert.notNull(dto.choreId(), "Chore ID cannot be null");
        Assert.notNull(dto.assignedUserId(), "Assigned user ID cannot be null");

        log.info("Requested creation of new chore assignment for chore {} and user {}", dto.choreId(), dto.assignedUserId());

        Chore choreToAssign = choreRepository.findById(dto.choreId())
                .orElseThrow(() -> new IllegalArgumentException("Chore with ID: " + dto.choreId() + " not found."));

        // Check if logged user is a member of the household the chore belongs to
        Household choreHousehold = choreToAssign.getHousehold();
        checkIfHouseholdMember(userId, choreHousehold);

        User userToAssign = userRepository.findById(dto.assignedUserId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + dto.assignedUserId() + " not found."));

        ChoreAssignment newAssignment = new ChoreAssignment(dto.dueDate(), choreToAssign, userToAssign);

        ChoreAssignment savedAssignment = choreAssignmentRepository.save(newAssignment);
        log.info("Chore assignment saved successfully! Id: {}", savedAssignment.getId());

        UserResponseDTO userDTO = userService.getCurrentUser(savedAssignment.getAssignedUser().getId());

        return new ChoreAssignmentResponseDTO(savedAssignment.getId(),
                savedAssignment.getAssignedChore().getId(),
                savedAssignment.getAssignedChore().getDescription(),
                userDTO,
                savedAssignment.getDueDate(),
                savedAssignment.getChoreStatus());
    }

    @Transactional
    public void deleteChoreAssignment(@NonNull UUID assignmentId, @NonNull UUID userId) {

        Assert.notNull(assignmentId, "Chore assignment ID cannot be null");
        Assert.notNull(userId, "Unexpectedly logged in as a non-existing user");

        log.info("Requested deletion of chore assignment {}", assignmentId);

        ChoreAssignment assignmentToDelete = choreAssignmentRepository.findById(assignmentId)
                                        .orElseThrow(() -> new IllegalArgumentException("Chore assignment with ID: " + assignmentId + " not found."));

        // Check if logged user is a member and admin of the household that the assignment's chore belongs to
        Household assignmentHousehold = assignmentToDelete.getAssignedChore().getHousehold();
        checkIfHouseholdMember(userId, assignmentHousehold);
        checkIfAdminOfHousehold(userId, assignmentHousehold);

        choreAssignmentRepository.delete(assignmentToDelete);

        log.info("Chore assignment deleted successfully! Id: {}", assignmentToDelete.getId());
    }

    @Transactional
    public void updateChoreAssignmentStatus(@NonNull UUID assignmentId,
                                            @NonNull UUID userId,
                                            @NonNull ChoreStatusUpdateRequestDTO dto){

        Assert.notNull(userId, "Unexpectedly logged in as a non-existing user");
        Assert.notNull(dto, "No request body was sent");
        Assert.notNull(assignmentId, "Assignment ID cannot be null");
        Assert.notNull(dto.newStatus(), "New status cannot be null");

        log.info("Requested status update for chore assignment {}", assignmentId);

        //find the chore assignment
        ChoreAssignment assignment = choreAssignmentRepository.findById(assignmentId)
                                        .orElseThrow(() -> new IllegalArgumentException("Chore assignment with ID: " + assignmentId + " not found."));

        if(!Objects.equals(assignment.getAssignedUser().getId(), userId)) {
            throw new AccessDeniedException("Only the assigned user can update an assignment's status");
        }

        //update the status
        assignment.setChoreStatus(dto.newStatus());

        //save the updated assignment
        choreAssignmentRepository.save(assignment);
        log.info("Chore assignment status updated successfully! Assignment ID: {}, New Status: {}", assignment.getId(), assignment.getChoreStatus());
    }

    @Transactional
    public ChoreAssignmentResponseDTO reassignChore(@NonNull UUID assignmentId, @NonNull UUID userId, @NonNull ChoreReassignRequestDTO dto) {

        Assert.notNull(assignmentId, "Assignment ID cannot be null");
        Assert.notNull(dto, "New assignee ID cannot be null");

        log.info("Requested reassignment of chore assignment {} to new user {}", assignmentId, dto.newAssigneeId());

        ChoreAssignment assignment = choreAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Chore assignment with ID: " + assignmentId + " not found."));

        User newAssignee = userRepository.findById(dto.newAssigneeId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + dto.newAssigneeId() + " not found."));

        assignment.setAssignedUser(newAssignee);

        ChoreAssignment updatedAssignment = choreAssignmentRepository.save(assignment);
        log.info("Chore assignment reassigned successfully! Assignment ID: {}, New Assignee: {}", updatedAssignment.getId(), updatedAssignment.getAssignedUser().getName());

        UserResponseDTO userDTO = userService.getCurrentUser(updatedAssignment.getAssignedUser().getId());

        return new ChoreAssignmentResponseDTO(updatedAssignment.getId(),
                updatedAssignment.getAssignedChore().getId(),
                updatedAssignment.getAssignedChore().getDescription(),
                userDTO,
                updatedAssignment.getDueDate(),
                updatedAssignment.getChoreStatus());
    }

    @Transactional(readOnly = true)
    public List<ChoreResponseDTO> getAllHouseholdChores(@NonNull UUID userId) {

        Assert.notNull(userId, "User ID cannot be null");

        log.info("Requested retrieval of all chores from user {}", userId);

        // Fetch current household of the logged user
        Household currentHousehold = getCurrentHousehold(userId);

        //retrieve all chores for the household
        List<Chore> chores = choreRepository.findAllByHouseholdId(currentHousehold.getId());

        if (chores.isEmpty()) {
            log.warn("No chores found for household with ID: {}", currentHousehold.getId());
            return java.util.Collections.emptyList();
        }

        log.info("Retrieved {} chores for household with ID: {}", chores.size(), currentHousehold.getId());

        return chores.stream()
                .map(chore -> new ChoreResponseDTO(chore.getId(), chore.getDescription(), chore.getFrequency()))
                .toList();
    }

    @Transactional
    public void deleteAllChoresForHousehold(@NonNull UUID userId) {

        Assert.notNull(userId, "User ID cannot be null");

        log.info("Requested deletion of all chores for household of user {}", userId);

        // Fetch current household of the logged user
        Household currentHousehold = getCurrentHousehold(userId);

        // Check if user is admin of that household
        checkIfAdminOfHousehold(userId, currentHousehold);

        List<Chore> choresToDelete = choreRepository.findAllByHouseholdId(currentHousehold.getId());

        if (choresToDelete.isEmpty()) {
            log.warn("No chores found for household with ID: {}. No deletion performed.", currentHousehold.getId());
            return;
        }

        choreRepository.deleteAll(choresToDelete);
        log.info("Deleted {} chores for household with ID: {}", choresToDelete.size(), currentHousehold.getId());
    }

    @Transactional(readOnly = true)
    public AssignmentOverviewDTO getAssignmentOverview(@NonNull UUID userId) {

        Assert.notNull(userId, "User ID cannot be null");

        log.info("Requested retrieval of assignment overview for user {}", userId);

        // Fetch current household of the logged user
        Household currentHousehold = getCurrentHousehold(userId);

        Integer pendingAssignments = choreAssignmentRepository.countByAssignedChore_Household_IdAndChoreStatus(currentHousehold.getId(), ChoreStatus.PENDING);
        Integer overdueAssignments = choreAssignmentRepository.countByAssignedChore_Household_IdAndChoreStatus(currentHousehold.getId(), ChoreStatus.OVERDUE);

        log.info("Retrieved assignment overview for household with ID: {}. Pending Assignments: {}, Overdue Assignments: {}", currentHousehold.getId(), pendingAssignments, overdueAssignments);

        return new AssignmentOverviewDTO(pendingAssignments, overdueAssignments);
    }

    @Transactional(readOnly = true)
    public List<ChoreAssignmentResponseDTO> getFilteredChoreAssignments(@NonNull UUID userId,
                                                                        @NonNull ChoreAssignmentFilterRequestDTO dto){
        Assert.notNull(userId, "User ID cannot be null");
        Assert.notNull(dto, "No filter DTO provided");

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found."));
        
        
        UUID currentHouseholdId = getCurrentHousehold(userId).getId();


        Specification<ChoreAssignment> spec = ChoreAssignmentSpecification.buildAssignmentFilter(currentHouseholdId, dto);

        List<ChoreAssignment> filteredAssignments = choreAssignmentRepository.findAll(spec);

        if (filteredAssignments.isEmpty()) {
            log.warn("No chore assignments found for user with ID: {} matching filter criteria", userId);
            return java.util.Collections.emptyList();
        }

        log.info("Retrieved {} chore assignments for user with ID: {} matching filter criteria", filteredAssignments.size(), userId);
        return filteredAssignments.stream()
            .map(assignment -> {
                UserResponseDTO assignedUserDTO = userService.getCurrentUser(assignment.getAssignedUser().getId());
                return new ChoreAssignmentResponseDTO(assignment.getId(),
                                                      assignment.getAssignedChore().getId(),
                                                      assignment.getAssignedChore().getDescription(),
                                                      assignedUserDTO,
                                                      assignment.getDueDate(),
                                                      assignment.getChoreStatus());
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public AssignmentOverviewDTO getUserAssignmentOverview(@NonNull UUID userId) {

        Assert.notNull(userId, "User ID cannot be null");

        log.info("Requested retrieval of assignment overview for user {}", userId);

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found."));

        Integer pendingAssignments = choreAssignmentRepository.countByAssignedUserIdAndChoreStatus(userId, ChoreStatus.PENDING);
        Integer overdueAssignments = choreAssignmentRepository.countByAssignedUserIdAndChoreStatus(userId, ChoreStatus.OVERDUE);

        log.info("Retrieved assignment overview for user with ID: {}. Pending Assignments: {}, Overdue Assignments: {}", userId, pendingAssignments, overdueAssignments);

        return new AssignmentOverviewDTO(pendingAssignments, overdueAssignments);
    }

    private void checkIfHouseholdMember(UUID userId, Household household) {

        List<UUID> membersIds = household.getMemberships().stream()
                .map(membership -> membership.getUser().getId()).toList();

        if(!membersIds.contains(userId)){
            throw new AccessDeniedException("The logged user is not a member of household " + household.getName());
        }
    }

    private Household getCurrentHousehold(@NonNull UUID userId) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found."));

        if(user.getHouseholdMembership() == null || user.getHouseholdMembership().getHousehold() == null) {
            throw new IllegalStateException("User with ID: " + userId + " is not currently a member of any household.");
        }

        return user.getHouseholdMembership().getHousehold();
    }

    private void checkIfAdminOfHousehold(@NonNull UUID userId, @NonNull Household household) {
        HouseholdMembership membership = householdMembershipRepository.findByHouseholdAndUser(household, userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found.")))
                .orElseThrow(() -> new AccessDeniedException("The logged user is not a member of household " + household.getName()));

        if(!membership.isAdmin()) {
            throw new AccessDeniedException("The logged user is not an admin of household " + household.getName());
        }
    }
}
