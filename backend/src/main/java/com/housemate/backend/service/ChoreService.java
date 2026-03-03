package com.housemate.backend.service;
import com.housemate.backend.model.chore.Chore;
import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.repository.chore.ChoreAssignmentRepository;
import com.housemate.backend.repository.chore.ChoreRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.shared.dto.chore.request.ChoreCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
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
    public void updateChoreAssignmentStatus(ChoreStatusUpdateRequestDTO dto){

        log.info("Requested status update for chore assignment {}", dto.assignmentId());

        //find the chore assignment
        ChoreAssignment assignment = choreAssignmentRepository.findById(dto.assignmentId())
                                        .orElseThrow(() -> new IllegalArgumentException("Chore assignment with ID: " + dto.assignmentId() + " not found."));

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
}
