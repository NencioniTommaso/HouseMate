package com.housemate.backend.service;
import com.housemate.backend.model.chore.Chore;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.repository.chore.ChoreRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.shared.dto.chore.request.ChoreRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.spi.Limit;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final HouseholdRepository householdRepository;

    @Transactional //executes each transactional method atomically
    public ChoreResponseDTO createChore(ChoreRequestDTO dto) {

        log.info("Requested creation of new chore {} for household {}", dto.description(), dto.householdId());

        //find the actual household based on the UUID
        Household household = householdRepository.findById(dto.householdId())
                              .orElseThrow(()  -> new IllegalArgumentException("Household with ID: "
                                                                               + dto.householdId() +
                                                                               " not found."));

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
}
