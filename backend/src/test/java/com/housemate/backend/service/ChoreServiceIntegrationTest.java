package com.housemate.backend.service;

import com.housemate.backend.model.chore.Chore;
import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.chore.ChoreAssignmentRepository;
import com.housemate.backend.repository.chore.ChoreRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.chore.request.*;
import com.housemate.shared.dto.chore.response.AssignmentOverviewDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.dto.household.request.AddMemberRequestDTO;
import com.housemate.shared.dto.household.request.HouseholdCreateRequestDTO;
import com.housemate.shared.dto.household.response.HouseholdInvitationCodeResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.enums.ChoreStatus;
import com.housemate.shared.utils.types.DateRange;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ChoreService Integration Tests")
@SuppressWarnings("null")
class ChoreServiceIntegrationTest {

    @Autowired
    private ChoreService choreService;

    @Autowired
    private HouseholdService householdService;

    @Autowired
    private ChoreRepository choreRepository;

    @Autowired
    private ChoreAssignmentRepository choreAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User user1;
    private User user2;
    private User user3;
    private UUID primaryHouseholdId;
    private UUID isolatedHouseholdId;

    @BeforeEach
    void setUp() {
        // Seed Primary Household with User 1 (Admin) and User 2 (Member)
        user1 = persistUser("Alice", "Admin", "alice.chore@test.com");
        user2 = persistUser("Bob", "Member", "bob.chore@test.com");

        HouseholdResponseDTO primaryHousehold = householdService.createHousehold(
            user1.getId(),
            new HouseholdCreateRequestDTO("Primary Household")
        );
        primaryHouseholdId = primaryHousehold.id();

        HouseholdInvitationCodeResponseDTO codeResponse = householdService.getInvitationCode(user1.getId());
        householdService.addMember(user2.getId(), new AddMemberRequestDTO(codeResponse.invitationCode()));

        // Seed Isolated Household with User 3 (Admin) for cross-household isolation tests
        user3 = persistUser("Charlie", "Outsider", "charlie.chore@test.com");
        HouseholdResponseDTO isolatedHousehold = householdService.createHousehold(
            user3.getId(),
            new HouseholdCreateRequestDTO("Isolated Household")
        );
        isolatedHouseholdId = isolatedHousehold.id();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Scenario 1: Create a Chore inside the household and assign it to User 1 -> verify ChoreResponseDTO and persistence")
    void shouldCreateChoreAndAssignToUser() {
        // 1. Create Chore in Primary Household
        ChoreCreateRequestDTO choreRequest = new ChoreCreateRequestDTO(
            "Wash Dishes",
            3,
            primaryHouseholdId
        );
        ChoreResponseDTO choreResponse = choreService.createChore(user1.getId(), choreRequest);

        assertThat(choreResponse).isNotNull();
        assertThat(choreResponse.id()).isNotNull();
        assertThat(choreResponse.description()).isEqualTo("Wash Dishes");
        assertThat(choreResponse.frequencyDays()).isEqualTo(3);

        // Verify Chore persistence in DB
        entityManager.flush();
        entityManager.clear();

        Chore persistedChore = choreRepository.findById(choreResponse.id()).orElseThrow();
        assertThat(persistedChore.getDescription()).isEqualTo("Wash Dishes");
        assertThat(persistedChore.getFrequency()).isEqualTo(3);
        assertThat(persistedChore.getHousehold().getId()).isEqualTo(primaryHouseholdId);

        // 2. Assign Chore to User 1
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
        ChoreAssignmentCreateRequestDTO assignmentRequest = new ChoreAssignmentCreateRequestDTO(
            choreResponse.id(),
            user1.getId(),
            dueDate
        );
        ChoreAssignmentResponseDTO assignmentResponse = choreService.createChoreAssignment(user1.getId(), assignmentRequest);

        assertThat(assignmentResponse).isNotNull();
        assertThat(assignmentResponse.assignmentId()).isNotNull();
        assertThat(assignmentResponse.choreId()).isEqualTo(choreResponse.id());
        assertThat(assignmentResponse.choreDescription()).isEqualTo("Wash Dishes");
        assertThat(assignmentResponse.assignedUser().id()).isEqualTo(user1.getId());
        assertThat(assignmentResponse.status()).isEqualTo(ChoreStatus.PENDING);
        assertThat(assignmentResponse.dueDate()).isEqualTo(dueDate);

        // Verify ChoreAssignment persistence in DB
        entityManager.flush();
        entityManager.clear();

        ChoreAssignment persistedAssignment = choreAssignmentRepository.findById(assignmentResponse.assignmentId()).orElseThrow();
        assertThat(persistedAssignment.getAssignedChore().getId()).isEqualTo(choreResponse.id());
        assertThat(persistedAssignment.getAssignedUser().getId()).isEqualTo(user1.getId());
        assertThat(persistedAssignment.getChoreStatus()).isEqualTo(ChoreStatus.PENDING);
        assertThat(persistedAssignment.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    @DisplayName("Scenario 2: Update assignment status (PENDING to COMPLETED) -> verify state update and persistence")
    void shouldUpdateAssignmentStatus() {
        ChoreResponseDTO choreResponse = choreService.createChore(
            user1.getId(),
            new ChoreCreateRequestDTO("Take Out Trash", 2, primaryHouseholdId)
        );

        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        ChoreAssignmentResponseDTO assignmentResponse = choreService.createChoreAssignment(
            user1.getId(),
            new ChoreAssignmentCreateRequestDTO(choreResponse.id(), user1.getId(), dueDate)
        );

        // Assigned user updates status to COMPLETED
        ChoreStatusUpdateRequestDTO updateRequest = new ChoreStatusUpdateRequestDTO(ChoreStatus.COMPLETED);
        choreService.updateChoreAssignmentStatus(assignmentResponse.assignmentId(), user1.getId(), updateRequest);

        // Verify updated status in DB
        entityManager.flush();
        entityManager.clear();

        ChoreAssignment reloadedAssignment = choreAssignmentRepository.findById(assignmentResponse.assignmentId()).orElseThrow();
        assertThat(reloadedAssignment.getChoreStatus()).isEqualTo(ChoreStatus.COMPLETED);
    }

    @Test
    @DisplayName("Scenario 3: Reassign a chore from User 1 to User 2 -> verify assignment points to User 2")
    void shouldReassignChoreFromUser1ToUser2() {
        ChoreResponseDTO choreResponse = choreService.createChore(
            user1.getId(),
            new ChoreCreateRequestDTO("Mop Floors", 7, primaryHouseholdId)
        );

        ChoreAssignmentResponseDTO initialAssignment = choreService.createChoreAssignment(
            user1.getId(),
            new ChoreAssignmentCreateRequestDTO(choreResponse.id(), user1.getId(), LocalDateTime.now().plusDays(3))
        );
        assertThat(initialAssignment.assignedUser().id()).isEqualTo(user1.getId());

        // Reassign to User 2
        ChoreReassignRequestDTO reassignRequest = new ChoreReassignRequestDTO(user2.getId());
        ChoreAssignmentResponseDTO reassignedResponse = choreService.reassignChore(
            initialAssignment.assignmentId(),
            user1.getId(),
            reassignRequest
        );

        assertThat(reassignedResponse).isNotNull();
        assertThat(reassignedResponse.assignmentId()).isEqualTo(initialAssignment.assignmentId());
        assertThat(reassignedResponse.assignedUser().id()).isEqualTo(user2.getId());

        // Verify reassignment persistence in DB
        entityManager.flush();
        entityManager.clear();

        ChoreAssignment reloadedAssignment = choreAssignmentRepository.findById(initialAssignment.assignmentId()).orElseThrow();
        assertThat(reloadedAssignment.getAssignedUser().getId()).isEqualTo(user2.getId());
    }

    @Test
    @DisplayName("Scenario 4: Query assignments using ChoreAssignmentFilterRequestDTO -> verify filtered results match expected items")
    void shouldFilterChoreAssignmentsByAssigneeStatusAndDateRange() {
        LocalDateTime baseTime = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);

        // Create 3 chores
        ChoreResponseDTO chore1 = choreService.createChore(
            user1.getId(),
            new ChoreCreateRequestDTO("Vacuum Living Room", 7, primaryHouseholdId)
        );
        ChoreResponseDTO chore2 = choreService.createChore(
            user1.getId(),
            new ChoreCreateRequestDTO("Clean Kitchen", 3, primaryHouseholdId)
        );
        ChoreResponseDTO chore3 = choreService.createChore(
            user1.getId(),
            new ChoreCreateRequestDTO("Water Balcony Plants", 14, primaryHouseholdId)
        );

        // Assignment 1: User 1, due in baseTime + 1 day, PENDING
        ChoreAssignmentResponseDTO assign1 = choreService.createChoreAssignment(
            user1.getId(),
            new ChoreAssignmentCreateRequestDTO(chore1.id(), user1.getId(), baseTime.plusDays(1))
        );

        // Assignment 2: User 2, due in baseTime + 4 days, COMPLETED
        ChoreAssignmentResponseDTO assign2 = choreService.createChoreAssignment(
            user1.getId(),
            new ChoreAssignmentCreateRequestDTO(chore2.id(), user2.getId(), baseTime.plusDays(4))
        );
        choreService.updateChoreAssignmentStatus(assign2.assignmentId(), user2.getId(), new ChoreStatusUpdateRequestDTO(ChoreStatus.COMPLETED));

        // Assignment 3: User 1, due in baseTime + 9 days, PENDING
        ChoreAssignmentResponseDTO assign3 = choreService.createChoreAssignment(
            user1.getId(),
            new ChoreAssignmentCreateRequestDTO(chore3.id(), user1.getId(), baseTime.plusDays(9))
        );

        entityManager.flush();
        entityManager.clear();

        // 4a. Filter by Assignee (User 1) -> expects assign1 and assign3
        ChoreAssignmentFilterRequestDTO assigneeFilter = new ChoreAssignmentFilterRequestDTO(
            null,
            user1.getId(),
            null,
            new DateRange(null, null)
        );
        List<ChoreAssignmentResponseDTO> assigneeResults = choreService.getFilteredChoreAssignments(user1.getId(), assigneeFilter);
        assertThat(assigneeResults)
            .extracting(ChoreAssignmentResponseDTO::assignmentId)
            .containsExactlyInAnyOrder(assign1.assignmentId(), assign3.assignmentId());

        // 4b. Filter by Status (COMPLETED) -> expects assign2 only
        ChoreAssignmentFilterRequestDTO statusFilter = new ChoreAssignmentFilterRequestDTO(
            List.of(ChoreStatus.COMPLETED),
            null,
            null,
            new DateRange(null, null)
        );
        List<ChoreAssignmentResponseDTO> statusResults = choreService.getFilteredChoreAssignments(user1.getId(), statusFilter);
        assertThat(statusResults)
            .extracting(ChoreAssignmentResponseDTO::assignmentId)
            .containsExactly(assign2.assignmentId());

        // 4c. Filter by Date Range (between baseTime and baseTime + 2 days) -> expects assign1 only
        ChoreAssignmentFilterRequestDTO dateFilter = new ChoreAssignmentFilterRequestDTO(
            null,
            null,
            null,
            new DateRange(baseTime, baseTime.plusDays(2))
        );
        List<ChoreAssignmentResponseDTO> dateResults = choreService.getFilteredChoreAssignments(user1.getId(), dateFilter);
        assertThat(dateResults)
            .extracting(ChoreAssignmentResponseDTO::assignmentId)
            .containsExactly(assign1.assignmentId());

        // 4d. Filter by Description Contains ("kitchen") -> expects assign2 only
        ChoreAssignmentFilterRequestDTO descriptionFilter = new ChoreAssignmentFilterRequestDTO(
            null,
            null,
            "kitchen",
            new DateRange(null, null)
        );
        List<ChoreAssignmentResponseDTO> descriptionResults = choreService.getFilteredChoreAssignments(user1.getId(), descriptionFilter);
        assertThat(descriptionResults)
            .extracting(ChoreAssignmentResponseDTO::assignmentId)
            .containsExactly(assign2.assignmentId());
    }

    @Test
    @DisplayName("Scenario 5: Verify cross-household isolation and unauthorized access restrictions")
    void shouldEnforceCrossHouseholdIsolation() {
        // User 1 creates chore and assignment in Primary Household
        ChoreResponseDTO primaryChore = choreService.createChore(
            user1.getId(),
            new ChoreCreateRequestDTO("Deep Clean Oven", 30, primaryHouseholdId)
        );
        ChoreAssignmentResponseDTO primaryAssignment = choreService.createChoreAssignment(
            user1.getId(),
            new ChoreAssignmentCreateRequestDTO(primaryChore.id(), user1.getId(), LocalDateTime.now().plusDays(2))
        );

        entityManager.flush();
        entityManager.clear();

        // 5a. Outsider (User 3) cannot create a chore in Primary Household
        ChoreCreateRequestDTO unauthorizedChoreRequest = new ChoreCreateRequestDTO(
            "Invader Chore",
            5,
            primaryHouseholdId
        );
        assertThatThrownBy(() -> choreService.createChore(user3.getId(), unauthorizedChoreRequest))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("The logged user is not a member of household");

        // 5b. Outsider (User 3) cannot create an assignment for a chore in Primary Household
        ChoreAssignmentCreateRequestDTO unauthorizedAssignRequest = new ChoreAssignmentCreateRequestDTO(
            primaryChore.id(),
            user3.getId(),
            LocalDateTime.now().plusDays(2)
        );
        assertThatThrownBy(() -> choreService.createChoreAssignment(user3.getId(), unauthorizedAssignRequest))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("The logged user is not a member of household");

        // 5c. Outsider (User 3) cannot delete a chore belonging to Primary Household
        assertThatThrownBy(() -> choreService.deleteChore(primaryChore.id(), user3.getId()))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("The logged user is not a member of household");

        // 5d. Non-assigned user (User 3) cannot update assignment status of User 1's assignment
        ChoreStatusUpdateRequestDTO unauthorizedStatusUpdate = new ChoreStatusUpdateRequestDTO(ChoreStatus.COMPLETED);
        assertThatThrownBy(() -> choreService.updateChoreAssignmentStatus(primaryAssignment.assignmentId(), user3.getId(), unauthorizedStatusUpdate))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Only the assigned user can update an assignment's status");

        // 5e. Outsider (User 3) calling getAllHouseholdChores does not see Primary Household's chores
        List<ChoreResponseDTO> isolatedChores = choreService.getAllHouseholdChores(user3.getId());
        assertThat(isolatedChores).isEmpty();

        // 5f. Outsider (User 3) filtering assignments does not see Primary Household's assignments
        ChoreAssignmentFilterRequestDTO emptyFilter = new ChoreAssignmentFilterRequestDTO(
            null,
            null,
            null,
            new DateRange(null, null)
        );
        List<ChoreAssignmentResponseDTO> isolatedAssignments = choreService.getFilteredChoreAssignments(user3.getId(), emptyFilter);
        assertThat(isolatedAssignments).isEmpty();

        // 5g. Non-admin member (User 2) cannot delete a chore in Primary Household
        assertThatThrownBy(() -> choreService.deleteChore(primaryChore.id(), user2.getId()))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("The logged user is not an admin of household");
    }

    @Test
    @DisplayName("shouldRetrieveAssignmentOverviewAndOverdueCounts correctly aggregates household and user assignments")
    void shouldRetrieveAssignmentOverviewAndOverdueCounts() {
        ChoreResponseDTO chore = choreService.createChore(
            user1.getId(),
            new ChoreCreateRequestDTO("Dust Shelves", 5, primaryHouseholdId)
        );

        choreService.createChoreAssignment(
            user1.getId(),
            new ChoreAssignmentCreateRequestDTO(chore.id(), user1.getId(), LocalDateTime.now().plusDays(2))
        );
        choreService.createChoreAssignment(
            user1.getId(),
            new ChoreAssignmentCreateRequestDTO(chore.id(), user2.getId(), LocalDateTime.now().plusDays(3))
        );

        entityManager.flush();
        entityManager.clear();

        // Household overview: 2 pending, 0 overdue
        AssignmentOverviewDTO householdOverview = choreService.getAssignmentOverview(user1.getId());
        assertThat(householdOverview.pendingAssignments()).isEqualTo(2);
        assertThat(householdOverview.overdueAssignments()).isZero();

        // User overview for User 1: 1 pending, 0 overdue
        AssignmentOverviewDTO user1Overview = choreService.getUserAssignmentOverview(user1.getId());
        assertThat(user1Overview.pendingAssignments()).isEqualTo(1);
        assertThat(user1Overview.overdueAssignments()).isZero();

        // User overview for User 2: 1 pending, 0 overdue
        AssignmentOverviewDTO user2Overview = choreService.getUserAssignmentOverview(user2.getId());
        assertThat(user2Overview.pendingAssignments()).isEqualTo(1);
        assertThat(user2Overview.overdueAssignments()).isZero();
    }

    private User persistUser(String name, String surname, String email) {
        User user = new User(name, surname, email, "encodedPassword123!");
        return userRepository.saveAndFlush(user);
    }
}
