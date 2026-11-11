package com.housemate.backend.repository;

import com.housemate.backend.model.chore.Chore;
import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.chore.ChoreAssignmentRepository;
import com.housemate.backend.repository.chore.ChoreAssignmentSpecification;
import com.housemate.shared.dto.chore.request.ChoreAssignmentFilterRequestDTO;
import com.housemate.shared.enums.ChoreStatus;
import com.housemate.shared.utils.types.DateRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@DisplayName("ChoreAssignment Repository & Specification Tests")
class ChoreAssignmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChoreAssignmentRepository choreAssignmentRepository;

    @Test
    @DisplayName("findAll(Specification) - should filter assignments correctly with complex criteria")
    void testFindAll_WithComplexSpecification() {

        User user = new User();
        user.setName("John");
        user.setSurname("Test");
        user.setEmail("john@test.com");
        user.setPassword("password");
        user = entityManager.persistAndFlush(user);

        Household household = new Household();
        household.setName("Test Household");
        household = entityManager.persistAndFlush(household);

        Chore chore = new Chore();
        chore.setDescription("Vacuum the living room");
        chore.setFrequency(7);
        chore.setHousehold(household);
        chore = entityManager.persistAndFlush(chore);

        ChoreAssignment targetAssignment = new ChoreAssignment();
        targetAssignment.setAssignedChore(chore);
        targetAssignment.setAssignedUser(user);
        targetAssignment.setChoreStatus(ChoreStatus.PENDING);
        targetAssignment.setDueDate(LocalDateTime.now().plusDays(2));
        entityManager.persistAndFlush(targetAssignment);

        ChoreAssignment wrongAssignment = new ChoreAssignment();
        wrongAssignment.setAssignedChore(chore);
        wrongAssignment.setAssignedUser(user);
        wrongAssignment.setChoreStatus(ChoreStatus.COMPLETED); // Status diverso!
        wrongAssignment.setDueDate(LocalDateTime.now().plusDays(2));
        entityManager.persistAndFlush(wrongAssignment);

        DateRange dateRange = new DateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5));

        ChoreAssignmentFilterRequestDTO filterDTO = new ChoreAssignmentFilterRequestDTO(
                List.of(ChoreStatus.PENDING),
                user.getId(),
                "room",
                dateRange
        );

        Specification<ChoreAssignment> spec = ChoreAssignmentSpecification.buildAssignmentFilter(household.getId(), filterDTO);

        List<ChoreAssignment> results = choreAssignmentRepository.findAll(spec);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size(), "Only one assignment should be returned");

        Assertions.assertEquals(targetAssignment.getId(), results.get(0).getId());
        Assertions.assertEquals(ChoreStatus.PENDING, results.get(0).getChoreStatus());
    }
}