package com.housemate.backend.service;
import com.housemate.backend.model.chore.Chore;
import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.chore.ChoreAssignmentRepository;
import com.housemate.backend.repository.chore.ChoreRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.chore.request.ChoreAssignmentCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.ChoreStatus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final HouseholdRepository householdRepository;
    private final ChoreAssignmentRepository choreAssignmentRepository;
    private final UserRepository userRepository;

    @Transactional //executes each transactional method atomically
    public ChoreResponseDTO createChore(ChoreCreateRequestDTO dto) {

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
    public void deleteChore(ChoreRequestDTO dto) {

        log.info("Requested deletion of chore {} for household {}", dto.description(), dto.householdId());

        //find the chore to delete
        Chore choreToDelete = choreRepository.findByDescriptionAndHouseholdId(dto.description(), dto.householdId());
        if(choreToDelete == null){
            throw new IllegalArgumentException("Unable to perform deletion. Chore with description: " + dto.description() + " not found in household with ID: " + dto.householdId());
        }

        //delete the chore
        choreRepository.delete(choreToDelete);
        log.info("Chore deleted successfully! Id: {}", choreToDelete.getId());

    }

    @Transactional
    public void updateChoreAssignmentStatus(UUID assignmentId, ChoreStatusUpdateRequestDTO dto){

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
    public List<ChoreResponseDTO> getAllHouseholdChores(UUID householdId) {

        log.info("Requested retrieval of all chores for household {}", householdId);

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
    public ChoreAssignmentResponseDTO createChoreAssignment(ChoreAssignmentCreateRequestDTO dto) {

        log.info("Requested creation of new chore assignment for chore {} and user {}", dto.choreId(), dto.assignedUserId());

        Chore choreToAssign = choreRepository.findById(dto.choreId())
                                .orElseThrow(() -> new IllegalArgumentException("Chore with ID: " + dto.choreId() + " not found."));

        User userToAssign = userRepository.findById(dto.assignedUserId())
                                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + dto.assignedUserId() + " not found."));

        ChoreAssignment newAssignment = new ChoreAssignment(dto.dueDate(), choreToAssign, userToAssign);

        ChoreAssignment savedAssignment = choreAssignmentRepository.save(newAssignment);
        log.info("Chore assignment saved successfully! Id: {}", savedAssignment.getId());

        return new ChoreAssignmentResponseDTO(savedAssignment.getId(),
                                              savedAssignment.getAssignedChore().getDescription(),
                                              savedAssignment.getAssignedUser().getName(),
                                              savedAssignment.getDueDate(),
                                              savedAssignment.getChoreStatus());
    }

    @Transactional
    public List<ChoreAssignmentResponseDTO> getAllUserChoreAssignments(UUID userId, ChoreStatus status) {

        log.info("Requested retrieval of all chore assignments for user {}", userId);

        List<ChoreAssignment> assignments;

        if(status == null) {
            assignments = choreAssignmentRepository.findAllByAssignedUserId(userId);
        }else{
            assignments = choreAssignmentRepository.findAllByAssignedUserIdAndChoreStatus(userId, status);
        }


        if (assignments.isEmpty()) {
            log.warn("No chore assignments in status {} found for user with ID: {}", userId, status);
            return java.util.Collections.emptyList();
        }

        log.info("Retrieved {} chore assignments for user with ID: {}", assignments.size(), userId);

        List<ChoreAssignmentResponseDTO> responseDTOs = assignments.stream()
            .map(assignment -> new ChoreAssignmentResponseDTO(assignment.getId(),
                                                              assignment.getAssignedChore().getDescription(),
                                                              assignment.getAssignedUser().getName(),
                                                              assignment.getDueDate(),
                                                              assignment.getChoreStatus()))
            .toList();

        return responseDTOs;
    }

    @Transactional
    public List<ChoreAssignmentResponseDTO> getAllHouseholdChoreAssignments(UUID householdId, ChoreStatus status) {

        log.info("Requested retrieval of all chore assignments for household {}", householdId);

        List<ChoreAssignment> assignments;

        if(status == null) {
            assignments = choreAssignmentRepository.findByAssignedChore_Household_Id(householdId);
        }else{
            assignments = choreAssignmentRepository.findByChoreStatusAndAssignedChore_Household_Id(status, householdId);
        }

        if (assignments.isEmpty()) {
            log.warn("No chore assignments in status {} found for household with ID: {}", householdId, status);
            return java.util.Collections.emptyList();
        }

        log.info("Retrieved {} chore assignments for household with ID: {}", assignments.size(), householdId);

        List<ChoreAssignmentResponseDTO> responseDTOs = assignments.stream()
            .map(assignment -> new ChoreAssignmentResponseDTO(assignment.getId(),
                                                              assignment.getAssignedChore().getDescription(),
                                                              assignment.getAssignedUser().getName(),
                                                              assignment.getDueDate(),
                                                              assignment.getChoreStatus()))
            .toList();

        return responseDTOs;
    }
}
