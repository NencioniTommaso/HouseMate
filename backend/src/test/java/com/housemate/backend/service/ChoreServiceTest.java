package com.housemate.backend.service;

import com.housemate.backend.model.chore.Chore;
import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.chore.ChoreAssignmentRepository;
import com.housemate.backend.repository.chore.ChoreRepository;
import com.housemate.backend.repository.household.HouseholdMembershipRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.chore.request.ChoreAssignmentCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreAssignmentFilterRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.ChoreStatus;
import com.housemate.shared.utils.types.DateRange;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChoreService Unit Tests")
class ChoreServiceTest {

    // ============ Mock Dependencies ============
    @Mock
    private ChoreRepository choreRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private HouseholdMembershipRepository householdMembershipRepository;

    @Mock
    private ChoreAssignmentRepository choreAssignmentRepository;

    @Mock
    private UserRepository userRepository;

    // ============ Service Under Test ============
    @InjectMocks
    private ChoreService choreService;

    // ============ Test Data Constants ============
    private static final UUID TEST_CHORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TEST_ASSIGNMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID TEST_SECOND_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

    private static final String TEST_CHORE_DESCRIPTION = "Vacuum living room";
    private static final String TEST_USER_NAME = "John Doe";
    private static final String TEST_USER_EMAIL = "john@example.com";
    private static final Integer TEST_FREQUENCY_DAYS = 7;

    // ============ Test Objects ============
    private Chore testChore;
    private ChoreAssignment testAssignment;
    private User testUser;
    private User testSecondUser;
    private Household testHousehold;
    private HouseholdMembership testMembership;

    @BeforeEach
    void setUp() {
        testHousehold = createTestHousehold();
        testUser = createTestUser();
        testSecondUser = createTestSecondUser();
        testChore = createTestChore();
        testMembership = createTestMembership();
        testAssignment = createTestAssignment();
    }

    // ============ Helper Methods ============

    private Household createTestHousehold() {
        Household household = new Household();
        ReflectionTestUtils.setField(household, "id", TEST_HOUSEHOLD_ID);
        household.setName("Test Household");
        return household;
    }

    private User createTestUser() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", TEST_USER_ID);
        user.setName(TEST_USER_NAME);
        user.setEmail(TEST_USER_EMAIL);
        user.setPassword("hashedPassword");
        return user;
    }

    private User createTestSecondUser() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", TEST_SECOND_USER_ID);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPassword("hashedPassword");
        return user;
    }

    private Chore createTestChore() {
        Chore chore = new Chore();
        ReflectionTestUtils.setField(chore, "id", TEST_CHORE_ID);
        chore.setDescription(TEST_CHORE_DESCRIPTION);
        chore.setFrequency(TEST_FREQUENCY_DAYS);
        chore.setHousehold(testHousehold);
        return chore;
    }

    private HouseholdMembership createTestMembership() {
        HouseholdMembership membership = new HouseholdMembership();
        membership.setHousehold(testHousehold);
        membership.setUser(testUser);
        return membership;
    }

    private ChoreAssignment createTestAssignment() {
        ChoreAssignment assignment = new ChoreAssignment();
        ReflectionTestUtils.setField(assignment, "id", TEST_ASSIGNMENT_ID);
        assignment.setAssignedChore(testChore);
        assignment.setAssignedUser(testUser);
        assignment.setDueDate(LocalDateTime.now().plusDays(7));
        assignment.setChoreStatus(ChoreStatus.PENDING);
        return assignment;
    }

    // ============ Tests for createChore ============

    @Test
    @DisplayName("createChore - should create and return ChoreResponseDTO on valid input")
    void testCreateChore_Success() {

        ChoreCreateRequestDTO requestDTO = new ChoreCreateRequestDTO(TEST_CHORE_DESCRIPTION, TEST_FREQUENCY_DAYS, TEST_HOUSEHOLD_ID);

        when(householdRepository.findById(testHousehold.getId())).thenReturn(Optional.of(testHousehold));
        when(choreRepository.save(any(Chore.class))).thenAnswer(invocation -> {
            Chore savedChore = invocation.getArgument(0);

            ReflectionTestUtils.setField(savedChore, "id", TEST_CHORE_ID);

            return savedChore;
        });

        ChoreResponseDTO responseDTO = choreService.createChore(requestDTO);

        Assertions.assertNotNull(responseDTO);
        Assertions.assertNotNull(responseDTO.id());
        Assertions.assertEquals(TEST_CHORE_DESCRIPTION, responseDTO.description());
        Assertions.assertEquals(TEST_FREQUENCY_DAYS, responseDTO.frequencyDays());

        verify(householdRepository).findById(testHousehold.getId());
        verify(choreRepository).save(any(Chore.class));
    }

    @Test
    @DisplayName("createChore - should throw IllegalArgumentException when DTO is null")
    void testCreateChore_DtoNull() {

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChore(null);
        });

        Assertions.assertEquals("No request body was sent", exception.getMessage());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(choreRepository);
    }

    @Test
    @DisplayName("createChore - should throw IllegalArgumentException when description is null")
    void testCreateChore_DescriptionNull() {

        ChoreCreateRequestDTO requestDTO = new ChoreCreateRequestDTO(null, TEST_FREQUENCY_DAYS, TEST_HOUSEHOLD_ID);


        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChore(requestDTO);
        });

        Assertions.assertEquals("Chore description cannot be null", exception.getMessage());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(choreRepository);
    }

    @Test
    @DisplayName("createChore - should throw IllegalArgumentException when frequencyDays is null")
    void testCreateChore_FrequencyDaysNull() {
        ChoreCreateRequestDTO requestDTO = new ChoreCreateRequestDTO(TEST_CHORE_DESCRIPTION, null, TEST_HOUSEHOLD_ID);


        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChore(requestDTO);
        });

        Assertions.assertEquals("Frequency days cannot be null", exception.getMessage());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(choreRepository);
    }

    @Test
    @DisplayName("createChore - should throw IllegalArgumentException when householdId is null")
    void testCreateChore_HouseholdIdNull() {
        ChoreCreateRequestDTO requestDTO = new ChoreCreateRequestDTO(TEST_CHORE_DESCRIPTION, TEST_FREQUENCY_DAYS, null);


        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChore(requestDTO);
        });

        Assertions.assertEquals("Household ID cannot be null", exception.getMessage());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(choreRepository);
    }

    @Test
    @DisplayName("createChore - should throw IllegalArgumentException when household not found")
    void testCreateChore_HouseholdNotFound() {

        UUID nonExistingId = UUID.randomUUID();

        ChoreCreateRequestDTO requestDTO = new ChoreCreateRequestDTO(TEST_CHORE_DESCRIPTION, TEST_FREQUENCY_DAYS, nonExistingId);

        when(householdRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChore(requestDTO);
        });

        Assertions.assertEquals("Household with ID: " + requestDTO.householdId()  + " not found.", exception.getMessage());

        verify(householdRepository).findById(nonExistingId);
        verifyNoInteractions(choreRepository);
    }

    @Test
    @DisplayName("createChore - should throw IllegalArgumentException when chore with same description already exists")
    void testCreateChore_DuplicateDescription() {
        // TODO: Implement duplicate chore description validation test
    }

    // ============ Tests for deleteChore ============

    @Test
    @DisplayName("deleteChore - should delete chore on valid ID")
    void testDeleteChore_Success() {

        when(choreRepository.findById(TEST_CHORE_ID)).thenReturn(Optional.of(testChore));

        choreService.deleteChore(TEST_CHORE_ID);

        verify(choreRepository).findById(TEST_CHORE_ID);
        verify(choreRepository).delete(testChore);
    }

    @Test
    @DisplayName("deleteChore - should throw IllegalArgumentException when choreId is null")
    void testDeleteChore_IdNull() {
        // TODO: Implement null choreId validation test
    }

    @Test
    @DisplayName("deleteChore - should throw IllegalArgumentException when chore not found")
    void testDeleteChore_NotFound() {
        // TODO: Implement chore not found error test
    }

    // ============ Tests for createChoreAssignment ============

    @Test
    @DisplayName("createChoreAssignment - should create and return ChoreAssignmentResponseDTO on valid input")
    void testCreateChoreAssignment_Success() {

        LocalDateTime dueDate =  LocalDateTime.now().plusDays(2);

        ChoreAssignmentCreateRequestDTO requestDTO = new ChoreAssignmentCreateRequestDTO(TEST_CHORE_ID,
                                                                                         TEST_USER_ID,
                                                                                         dueDate);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(choreRepository.findById(TEST_CHORE_ID)).thenReturn(Optional.of(testChore));
        when(choreAssignmentRepository.save(any(ChoreAssignment.class))).thenAnswer(invocation -> {
            ChoreAssignment savedAssignment = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedAssignment, "id", TEST_ASSIGNMENT_ID);
            return savedAssignment;
        });

        ChoreAssignmentResponseDTO responseDTO = choreService.createChoreAssignment(requestDTO);

        Assertions.assertNotNull(responseDTO);
        Assertions.assertEquals(TEST_ASSIGNMENT_ID, responseDTO.assignmentId());
        Assertions.assertEquals(TEST_CHORE_ID, responseDTO.choreId());
        Assertions.assertEquals(TEST_CHORE_DESCRIPTION, responseDTO.choreDescription());
        Assertions.assertEquals(dueDate,  responseDTO.dueDate());

        verify(choreRepository).findById(TEST_CHORE_ID);
        verify(userRepository).findById(TEST_USER_ID);
        verify(choreAssignmentRepository).save(any(ChoreAssignment.class));
    }

    @Test
    @DisplayName("createChoreAssignment - should throw IllegalArgumentException when DTO is null")
    void testCreateChoreAssignment_DtoNull() {
        // TODO: Implement null DTO validation test
    }

    @Test
    @DisplayName("createChoreAssignment - should throw IllegalArgumentException when choreId is null")
    void testCreateChoreAssignment_ChoreIdNull() {
        // TODO: Implement null choreId validation test
    }

    @Test
    @DisplayName("createChoreAssignment - should throw IllegalArgumentException when assignedUserId is null")
    void testCreateChoreAssignment_UserIdNull() {
        // TODO: Implement null assignedUserId validation test
    }

    @Test
    @DisplayName("createChoreAssignment - should throw IllegalArgumentException when chore not found")
    void testCreateChoreAssignment_ChoreNotFound() {
        // TODO: Implement chore not found error test
    }

    @Test
    @DisplayName("createChoreAssignment - should throw IllegalArgumentException when user not found")
    void testCreateChoreAssignment_UserNotFound() {
        // TODO: Implement user not found error test
    }

    // ============ Tests for deleteChoreAssignment ============

    @Test
    @DisplayName("deleteChoreAssignment - should delete assignment on valid ID")
    void testDeleteChoreAssignment_Success() {
        // TODO: Implement happy path deletion test
    }

    @Test
    @DisplayName("deleteChoreAssignment - should throw AssertionError when assignmentId is null")
    void testDeleteChoreAssignment_IdNull() {
        // TODO: Implement null assignmentId validation test
    }

    @Test
    @DisplayName("deleteChoreAssignment - should throw IllegalArgumentException when assignment not found")
    void testDeleteChoreAssignment_NotFound() {
        // TODO: Implement assignment not found error test
    }

    // ============ Tests for updateChoreAssignmentStatus ============

    @Test
    @DisplayName("updateChoreAssignmentStatus - should update status on valid input")
    void testUpdateChoreAssignmentStatus_Success() {

        ChoreStatusUpdateRequestDTO requestDTO = new ChoreStatusUpdateRequestDTO(ChoreStatus.COMPLETED);

        when(choreAssignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(choreAssignmentRepository.save(any(ChoreAssignment.class))).thenAnswer(invocation -> {
            ChoreAssignment savedAssignment = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedAssignment, "choreStatus", requestDTO.newStatus());
            return savedAssignment;
        });

        choreService.updateChoreAssignmentStatus(TEST_ASSIGNMENT_ID, requestDTO);

        Assertions.assertEquals(requestDTO.newStatus(), testAssignment.getChoreStatus());

        verify(choreAssignmentRepository).findById(TEST_ASSIGNMENT_ID);
        verify(choreAssignmentRepository).save(any(ChoreAssignment.class));
    }

    @Test
    @DisplayName("updateChoreAssignmentStatus - should throw IllegalArgumentException when DTO is null")
    void testUpdateChoreAssignmentStatus_DtoNull() {
        // TODO: Implement null DTO validation test
    }

    @Test
    @DisplayName("updateChoreAssignmentStatus - should throw IllegalArgumentException when assignmentId is null")
    void testUpdateChoreAssignmentStatus_AssignmentIdNull() {
        // TODO: Implement null assignmentId validation test
    }

    @Test
    @DisplayName("updateChoreAssignmentStatus - should throw IllegalArgumentException when newStatus is null")
    void testUpdateChoreAssignmentStatus_NewStatusNull() {
        // TODO: Implement null newStatus validation test
    }

    @Test
    @DisplayName("updateChoreAssignmentStatus - should throw IllegalArgumentException when assignment not found")
    void testUpdateChoreAssignmentStatus_AssignmentNotFound() {
        // TODO: Implement assignment not found error test
    }

    // ============ Tests for reassignChore ============

    @Test
    @DisplayName("reassignChore - should reassign and return updated ChoreAssignmentResponseDTO on valid input")
    void testReassignChore_Success() {
        // TODO: Implement happy path reassignment test
    }

    @Test
    @DisplayName("reassignChore - should throw AssertionError when assignmentId is null")
    void testReassignChore_AssignmentIdNull() {
        // TODO: Implement null assignmentId validation test
    }

    @Test
    @DisplayName("reassignChore - should throw AssertionError when newAssigneeId is null")
    void testReassignChore_NewAssigneeIdNull() {
        // TODO: Implement null newAssigneeId validation test
    }

    @Test
    @DisplayName("reassignChore - should throw IllegalArgumentException when assignment not found")
    void testReassignChore_AssignmentNotFound() {
        // TODO: Implement assignment not found error test
    }

    @Test
    @DisplayName("reassignChore - should throw IllegalArgumentException when new assignee user not found")
    void testReassignChore_NewAssigneeNotFound() {
        // TODO: Implement new assignee not found error test
    }

    // ============ Tests for getAllHouseholdChores ============

    @Test
    @DisplayName("getAllHouseholdChores - should return list of ChoreResponseDTOs on valid input")
    void testGetAllHouseholdChores_Success() {
        // TODO: Implement happy path retrieval test
    }

    @Test
    @DisplayName("getAllHouseholdChores - should throw IllegalArgumentException when userId is null")
    void testGetAllHouseholdChores_UserIdNull() {
        // TODO: Implement null userId validation test
    }

    @Test
    @DisplayName("getAllHouseholdChores - should throw IllegalArgumentException when householdId is null")
    void testGetAllHouseholdChores_HouseholdIdNull() {
        // TODO: Implement null householdId validation test
    }

    @Test
    @DisplayName("getAllHouseholdChores - should throw AccessDeniedException when user is not household member")
    void testGetAllHouseholdChores_AccessDenied() {
        // TODO: Implement access denied test when user not member of household
    }

    @Test
    @DisplayName("getAllHouseholdChores - should return empty list when no chores found")
    void testGetAllHouseholdChores_EmptyResult() {
        // TODO: Implement empty result test
    }

    // ============ Tests for deleteAllChoresForHousehold ============

    @Test
    @DisplayName("deleteAllChoresForHousehold - should delete all household chores on valid input")
    void testDeleteAllChoresForHousehold_Success() {
        // TODO: Implement happy path deletion test
    }

    @Test
    @DisplayName("deleteAllChoresForHousehold - should throw IllegalArgumentException when householdId is null")
    void testDeleteAllChoresForHousehold_HouseholdIdNull() {
        // TODO: Implement null householdId validation test
    }

    @Test
    @DisplayName("deleteAllChoresForHousehold - should return early when no chores found")
    void testDeleteAllChoresForHousehold_EmptyResult() {
        // TODO: Implement empty result test (no deletion needed)
    }

    // ============ Tests for getAssignmentOverview ============

    @Test
    @DisplayName("getAssignmentOverview - should return AssignmentOverviewDTO with correct counts")
    void testGetAssignmentOverview_Success() {
        // TODO: Implement happy path overview retrieval test
    }

    @Test
    @DisplayName("getAssignmentOverview - should throw AssertionError when householdId is null")
    void testGetAssignmentOverview_HouseholdIdNull() {
        // TODO: Implement null householdId validation test
    }

    @Test
    @DisplayName("getAssignmentOverview - should throw IllegalArgumentException when household not found")
    void testGetAssignmentOverview_HouseholdNotFound() {
        // TODO: Implement household not found error test
    }

    // ============ Tests for getFilteredChoreAssignments ============

    @Test
    @DisplayName("getFilteredChoreAssignments - should return filtered ChoreAssignmentResponseDTOs on valid input")
    void testGetFilteredChoreAssignments_Success() {

        List<ChoreStatus> statusesFilter = new ArrayList<>();
        statusesFilter.add(ChoreStatus.PENDING);
        statusesFilter.add(ChoreStatus.COMPLETED);

        ChoreAssignmentFilterRequestDTO requestDTO = new ChoreAssignmentFilterRequestDTO(
                statusesFilter,
                TEST_USER_ID,
                TEST_CHORE_DESCRIPTION,
                new DateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7))
        );

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(choreAssignmentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(testAssignment));


        List<ChoreAssignmentResponseDTO> responseDTOs = choreService.getFilteredChoreAssignments(TEST_USER_ID, TEST_HOUSEHOLD_ID, requestDTO);

        Assertions.assertNotNull(responseDTOs);
        Assertions.assertFalse(responseDTOs.isEmpty());
        Assertions.assertEquals(1, responseDTOs.size());

        Assertions.assertEquals(testAssignment.getId(), responseDTOs.get(0).assignmentId());

        verify(userRepository).findById(testUser.getId());
        verify(choreAssignmentRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should throw IllegalArgumentException when userId is null")
    void testGetFilteredChoreAssignments_UserIdNull() {
        // TODO: Implement null userId validation test
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should throw IllegalArgumentException when DTO is null")
    void testGetFilteredChoreAssignments_DtoNull() {
        // TODO: Implement null DTO validation test
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should throw IllegalArgumentException when user not found")
    void testGetFilteredChoreAssignments_UserNotFound() {
        // TODO: Implement user not found error test
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should return empty list when no assignments match filters")
    void testGetFilteredChoreAssignments_EmptyResult() {
        // TODO: Implement empty result test
    }

}

