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
import com.housemate.shared.dto.chore.request.*;
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
        ChoreCreateRequestDTO requestDTO = new ChoreCreateRequestDTO(TEST_CHORE_DESCRIPTION, TEST_FREQUENCY_DAYS, TEST_HOUSEHOLD_ID);

        when(householdRepository.findById(TEST_HOUSEHOLD_ID)).thenReturn(Optional.of(testHousehold));
        when(choreRepository.findByDescriptionAndHouseholdId(TEST_CHORE_DESCRIPTION, TEST_HOUSEHOLD_ID)).thenReturn(testChore);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChore(requestDTO);
        });

        Assertions.assertTrue(exception.getMessage().contains("already exists"));

        verify(householdRepository).findById(TEST_HOUSEHOLD_ID);
        verify(choreRepository).findByDescriptionAndHouseholdId(TEST_CHORE_DESCRIPTION, TEST_HOUSEHOLD_ID);
        verifyNoMoreInteractions(choreRepository);
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
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.deleteChore(null);
        });

        Assertions.assertEquals("Chore ID cannot be null", exception.getMessage());

        verifyNoInteractions(choreRepository);
    }

    @Test
    @DisplayName("deleteChore - should throw IllegalArgumentException when chore not found")
    void testDeleteChore_NotFound() {
        when(choreRepository.findById(TEST_CHORE_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.deleteChore(TEST_CHORE_ID);
        });

        Assertions.assertEquals("Chore with ID: " + TEST_CHORE_ID + " not found.", exception.getMessage());

        verify(choreRepository).findById(TEST_CHORE_ID);
        verifyNoMoreInteractions(choreRepository);
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
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChoreAssignment(null);
        });

        Assertions.assertEquals("No request body was sent", exception.getMessage());

        verifyNoInteractions(choreRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("createChoreAssignment - should throw IllegalArgumentException when choreId is null")
    void testCreateChoreAssignment_ChoreIdNull() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);
        ChoreAssignmentCreateRequestDTO requestDTO = new ChoreAssignmentCreateRequestDTO(null, TEST_USER_ID, dueDate);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChoreAssignment(requestDTO);
        });

        Assertions.assertEquals("Chore ID cannot be null", exception.getMessage());

        verifyNoInteractions(choreRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("createChoreAssignment - should throw IllegalArgumentException when assignedUserId is null")
    void testCreateChoreAssignment_UserIdNull() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);
        ChoreAssignmentCreateRequestDTO requestDTO = new ChoreAssignmentCreateRequestDTO(TEST_CHORE_ID, null, dueDate);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChoreAssignment(requestDTO);
        });

        Assertions.assertEquals("Assigned user ID cannot be null", exception.getMessage());

        verifyNoInteractions(choreRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("createChoreAssignment - should throw IllegalArgumentException when chore not found")
    void testCreateChoreAssignment_ChoreNotFound() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);
        ChoreAssignmentCreateRequestDTO requestDTO = new ChoreAssignmentCreateRequestDTO(TEST_CHORE_ID, TEST_USER_ID, dueDate);

        when(choreRepository.findById(TEST_CHORE_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChoreAssignment(requestDTO);
        });

        Assertions.assertEquals("Chore with ID: " + TEST_CHORE_ID + " not found.", exception.getMessage());

        verify(choreRepository).findById(TEST_CHORE_ID);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("createChoreAssignment - should throw IllegalArgumentException when user not found")
    void testCreateChoreAssignment_UserNotFound() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);
        ChoreAssignmentCreateRequestDTO requestDTO = new ChoreAssignmentCreateRequestDTO(TEST_CHORE_ID, TEST_USER_ID, dueDate);

        when(choreRepository.findById(TEST_CHORE_ID)).thenReturn(Optional.of(testChore));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.createChoreAssignment(requestDTO);
        });

        Assertions.assertEquals("User with ID: " + TEST_USER_ID + " not found.", exception.getMessage());

        verify(choreRepository).findById(TEST_CHORE_ID);
        verify(userRepository).findById(TEST_USER_ID);
        verifyNoInteractions(choreAssignmentRepository);
    }

    // ============ Tests for deleteChoreAssignment ============

    @Test
    @DisplayName("deleteChoreAssignment - should delete assignment on valid ID")
    void testDeleteChoreAssignment_Success() {
        when(choreAssignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        choreService.deleteChoreAssignment(TEST_ASSIGNMENT_ID);

        verify(choreAssignmentRepository).findById(TEST_ASSIGNMENT_ID);
        verify(choreAssignmentRepository).delete(testAssignment);
    }

    @Test
    @DisplayName("deleteChoreAssignment - should throw AssertionError when assignmentId is null")
    void testDeleteChoreAssignment_IdNull() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.deleteChoreAssignment(null);
        });

        Assertions.assertEquals("Chore assignment ID cannot be null", exception.getMessage());

        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("deleteChoreAssignment - should throw IllegalArgumentException when assignment not found")
    void testDeleteChoreAssignment_NotFound() {
        when(choreAssignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.deleteChoreAssignment(TEST_ASSIGNMENT_ID);
        });

        Assertions.assertEquals("Chore assignment with ID: " + TEST_ASSIGNMENT_ID + " not found.", exception.getMessage());

        verify(choreAssignmentRepository).findById(TEST_ASSIGNMENT_ID);
        verifyNoMoreInteractions(choreAssignmentRepository);
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
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.updateChoreAssignmentStatus(TEST_ASSIGNMENT_ID, null);
        });

        Assertions.assertEquals("No request body was sent", exception.getMessage());

        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("updateChoreAssignmentStatus - should throw IllegalArgumentException when assignmentId is null")
    void testUpdateChoreAssignmentStatus_AssignmentIdNull() {
        ChoreStatusUpdateRequestDTO requestDTO = new ChoreStatusUpdateRequestDTO(ChoreStatus.COMPLETED);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.updateChoreAssignmentStatus(null, requestDTO);
        });

        Assertions.assertEquals("Assignment ID cannot be null", exception.getMessage());

        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("updateChoreAssignmentStatus - should throw IllegalArgumentException when newStatus is null")
    void testUpdateChoreAssignmentStatus_NewStatusNull() {
        ChoreStatusUpdateRequestDTO requestDTO = new ChoreStatusUpdateRequestDTO(null);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.updateChoreAssignmentStatus(TEST_ASSIGNMENT_ID, requestDTO);
        });

        Assertions.assertEquals("New status cannot be null", exception.getMessage());

        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("updateChoreAssignmentStatus - should throw IllegalArgumentException when assignment not found")
    void testUpdateChoreAssignmentStatus_AssignmentNotFound() {
        ChoreStatusUpdateRequestDTO requestDTO = new ChoreStatusUpdateRequestDTO(ChoreStatus.COMPLETED);

        when(choreAssignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.updateChoreAssignmentStatus(TEST_ASSIGNMENT_ID, requestDTO);
        });

        Assertions.assertEquals("Chore assignment with ID: " + TEST_ASSIGNMENT_ID + " not found.", exception.getMessage());

        verify(choreAssignmentRepository).findById(TEST_ASSIGNMENT_ID);
        verifyNoMoreInteractions(choreAssignmentRepository);
    }

    // ============ Tests for reassignChore ============

    @Test
    @DisplayName("reassignChore - should reassign and return updated ChoreAssignmentResponseDTO on valid input")
    void testReassignChore_Success() {
        when(choreAssignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_SECOND_USER_ID)).thenReturn(Optional.of(testSecondUser));
        when(choreAssignmentRepository.save(any(ChoreAssignment.class))).thenAnswer(invocation -> {
            ChoreAssignment savedAssignment = invocation.getArgument(0);
            return savedAssignment;
        });

        ChoreAssignmentResponseDTO responseDTO = choreService.reassignChore(TEST_ASSIGNMENT_ID, new ChoreReassignRequestDTO(TEST_SECOND_USER_ID));

        Assertions.assertNotNull(responseDTO);
        Assertions.assertEquals(TEST_ASSIGNMENT_ID, responseDTO.assignmentId());
        Assertions.assertEquals(testSecondUser.getName(), responseDTO.assignedUserName());

        verify(choreAssignmentRepository).findById(TEST_ASSIGNMENT_ID);
        verify(userRepository).findById(TEST_SECOND_USER_ID);
        verify(choreAssignmentRepository).save(any(ChoreAssignment.class));
    }

    @Test
    @DisplayName("reassignChore - should throw AssertionError when assignmentId is null")
    void testReassignChore_AssignmentIdNull() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.reassignChore(null, new ChoreReassignRequestDTO(TEST_SECOND_USER_ID));
        });

        Assertions.assertEquals("Assignment ID cannot be null", exception.getMessage());

        verifyNoInteractions(choreAssignmentRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("reassignChore - should throw AssertionError when newAssigneeId is null")
    void testReassignChore_NewAssigneeIdNull() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.reassignChore(TEST_ASSIGNMENT_ID, null);
        });

        Assertions.assertEquals("New assignee ID cannot be null", exception.getMessage());

        verifyNoInteractions(choreAssignmentRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("reassignChore - should throw IllegalArgumentException when assignment not found")
    void testReassignChore_AssignmentNotFound() {
        when(choreAssignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.reassignChore(TEST_ASSIGNMENT_ID, new ChoreReassignRequestDTO(TEST_SECOND_USER_ID));
        });

        Assertions.assertEquals("Chore assignment with ID: " + TEST_ASSIGNMENT_ID + " not found.", exception.getMessage());

        verify(choreAssignmentRepository).findById(TEST_ASSIGNMENT_ID);
        verifyNoInteractions(userRepository);
        verifyNoMoreInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("reassignChore - should throw IllegalArgumentException when new assignee user not found")
    void testReassignChore_NewAssigneeNotFound() {
        when(choreAssignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_SECOND_USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.reassignChore(TEST_ASSIGNMENT_ID, new ChoreReassignRequestDTO(TEST_SECOND_USER_ID));
        });

        Assertions.assertEquals("User with ID: " + TEST_SECOND_USER_ID + " not found.", exception.getMessage());

        verify(choreAssignmentRepository).findById(TEST_ASSIGNMENT_ID);
        verify(userRepository).findById(TEST_SECOND_USER_ID);
        verifyNoMoreInteractions(choreAssignmentRepository);
    }

    // ============ Tests for getAllHouseholdChores ============

    @Test
    @DisplayName("getAllHouseholdChores - should return list of ChoreResponseDTOs on valid input")
    void testGetAllHouseholdChores_Success() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(householdMembershipRepository.existsByHouseholdIdAndUserId(TEST_HOUSEHOLD_ID, TEST_USER_ID)).thenReturn(true);
        when(choreRepository.findAllByHouseholdId(TEST_HOUSEHOLD_ID)).thenReturn(List.of(testChore));

        List<ChoreResponseDTO> responseDTOs = choreService.getAllHouseholdChores(TEST_USER_ID, TEST_HOUSEHOLD_ID);

        Assertions.assertNotNull(responseDTOs);
        Assertions.assertFalse(responseDTOs.isEmpty());
        Assertions.assertEquals(1, responseDTOs.size());
        Assertions.assertEquals(TEST_CHORE_ID, responseDTOs.get(0).id());

        verify(userRepository).findById(TEST_USER_ID);
        verify(householdMembershipRepository).existsByHouseholdIdAndUserId(TEST_HOUSEHOLD_ID, TEST_USER_ID);
        verify(choreRepository).findAllByHouseholdId(TEST_HOUSEHOLD_ID);
    }

    @Test
    @DisplayName("getAllHouseholdChores - should throw IllegalArgumentException when userId is null")
    void testGetAllHouseholdChores_UserIdNull() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.getAllHouseholdChores(null, TEST_HOUSEHOLD_ID);
        });

        Assertions.assertEquals("User ID cannot be null", exception.getMessage());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(householdMembershipRepository);
        verifyNoInteractions(choreRepository);
    }

    @Test
    @DisplayName("getAllHouseholdChores - should throw IllegalArgumentException when householdId is null")
    void testGetAllHouseholdChores_HouseholdIdNull() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.getAllHouseholdChores(TEST_USER_ID, null);
        });

        Assertions.assertEquals("Household ID cannot be null", exception.getMessage());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(householdMembershipRepository);
        verifyNoInteractions(choreRepository);
    }

    @Test
    @DisplayName("getAllHouseholdChores - should throw AccessDeniedException when user is not household member")
    void testGetAllHouseholdChores_AccessDenied() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(householdMembershipRepository.existsByHouseholdIdAndUserId(TEST_HOUSEHOLD_ID, TEST_USER_ID)).thenReturn(false);

        org.springframework.security.access.AccessDeniedException exception = Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            choreService.getAllHouseholdChores(TEST_USER_ID, TEST_HOUSEHOLD_ID);
        });

        Assertions.assertTrue(exception.getMessage().contains("not a member"));

        verify(userRepository).findById(TEST_USER_ID);
        verify(householdMembershipRepository).existsByHouseholdIdAndUserId(TEST_HOUSEHOLD_ID, TEST_USER_ID);
        verifyNoInteractions(choreRepository);
    }

    @Test
    @DisplayName("getAllHouseholdChores - should return empty list when no chores found")
    void testGetAllHouseholdChores_EmptyResult() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(householdMembershipRepository.existsByHouseholdIdAndUserId(TEST_HOUSEHOLD_ID, TEST_USER_ID)).thenReturn(true);
        when(choreRepository.findAllByHouseholdId(TEST_HOUSEHOLD_ID)).thenReturn(java.util.Collections.emptyList());

        List<ChoreResponseDTO> responseDTOs = choreService.getAllHouseholdChores(TEST_USER_ID, TEST_HOUSEHOLD_ID);

        Assertions.assertNotNull(responseDTOs);
        Assertions.assertTrue(responseDTOs.isEmpty());

        verify(userRepository).findById(TEST_USER_ID);
        verify(householdMembershipRepository).existsByHouseholdIdAndUserId(TEST_HOUSEHOLD_ID, TEST_USER_ID);
        verify(choreRepository).findAllByHouseholdId(TEST_HOUSEHOLD_ID);
    }

    // ============ Tests for deleteAllChoresForHousehold ============

    @Test
    @DisplayName("deleteAllChoresForHousehold - should delete all household chores on valid input")
    void testDeleteAllChoresForHousehold_Success() {
        when(choreRepository.findAllByHouseholdId(TEST_HOUSEHOLD_ID)).thenReturn(List.of(testChore));

        choreService.deleteAllChoresForHousehold(TEST_HOUSEHOLD_ID);

        verify(choreRepository).findAllByHouseholdId(TEST_HOUSEHOLD_ID);
        verify(choreRepository).deleteAll(List.of(testChore));
    }

    @Test
    @DisplayName("deleteAllChoresForHousehold - should throw IllegalArgumentException when householdId is null")
    void testDeleteAllChoresForHousehold_HouseholdIdNull() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.deleteAllChoresForHousehold(null);
        });

        Assertions.assertEquals("Household ID cannot be null", exception.getMessage());

        verifyNoInteractions(choreRepository);
    }

    @Test
    @DisplayName("deleteAllChoresForHousehold - should return early when no chores found")
    void testDeleteAllChoresForHousehold_EmptyResult() {
        when(choreRepository.findAllByHouseholdId(TEST_HOUSEHOLD_ID)).thenReturn(java.util.Collections.emptyList());

        choreService.deleteAllChoresForHousehold(TEST_HOUSEHOLD_ID);

        verify(choreRepository).findAllByHouseholdId(TEST_HOUSEHOLD_ID);
        verifyNoMoreInteractions(choreRepository);
    }

    // ============ Tests for getAssignmentOverview ============

    @Test
    @DisplayName("getAssignmentOverview - should return AssignmentOverviewDTO with correct counts")
    void testGetAssignmentOverview_Success() {
        when(householdRepository.findById(TEST_HOUSEHOLD_ID)).thenReturn(Optional.of(testHousehold));
        when(choreAssignmentRepository.countByAssignedChore_Household_IdAndChoreStatus(TEST_HOUSEHOLD_ID, ChoreStatus.PENDING)).thenReturn(5);
        when(choreAssignmentRepository.countByAssignedChore_Household_IdAndChoreStatus(TEST_HOUSEHOLD_ID, ChoreStatus.OVERDUE)).thenReturn(2);

        com.housemate.shared.dto.chore.response.AssignmentOverviewDTO responseDTO = choreService.getAssignmentOverview(TEST_HOUSEHOLD_ID);

        Assertions.assertNotNull(responseDTO);
        Assertions.assertEquals(5, responseDTO.pendingAssignments());
        Assertions.assertEquals(2, responseDTO.overdueAssignments());

        verify(householdRepository).findById(TEST_HOUSEHOLD_ID);
        verify(choreAssignmentRepository).countByAssignedChore_Household_IdAndChoreStatus(TEST_HOUSEHOLD_ID, ChoreStatus.PENDING);
        verify(choreAssignmentRepository).countByAssignedChore_Household_IdAndChoreStatus(TEST_HOUSEHOLD_ID, ChoreStatus.OVERDUE);
    }

    @Test
    @DisplayName("getAssignmentOverview - should throw AssertionError when householdId is null")
    void testGetAssignmentOverview_HouseholdIdNull() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.getAssignmentOverview(null);
        });

        Assertions.assertEquals("Household ID cannot be null", exception.getMessage());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("getAssignmentOverview - should throw IllegalArgumentException when household not found")
    void testGetAssignmentOverview_HouseholdNotFound() {
        when(householdRepository.findById(TEST_HOUSEHOLD_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.getAssignmentOverview(TEST_HOUSEHOLD_ID);
        });

        Assertions.assertEquals("Household with ID: " + TEST_HOUSEHOLD_ID + " not found.", exception.getMessage());

        verify(householdRepository).findById(TEST_HOUSEHOLD_ID);
        verifyNoInteractions(choreAssignmentRepository);
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

        testUser.setHouseholdMembership(testMembership);
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(choreAssignmentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(testAssignment));

        List<ChoreAssignmentResponseDTO> responseDTOs = choreService.getFilteredChoreAssignments(TEST_USER_ID, requestDTO);

        Assertions.assertNotNull(responseDTOs);
        Assertions.assertFalse(responseDTOs.isEmpty());
        Assertions.assertEquals(1, responseDTOs.size());

        Assertions.assertEquals(testAssignment.getId(), responseDTOs.get(0).assignmentId());

        verify(userRepository).findById(TEST_USER_ID);
        verify(choreAssignmentRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should throw IllegalArgumentException when userId is null")
    void testGetFilteredChoreAssignments_UserIdNull() {
        List<ChoreStatus> statusesFilter = new ArrayList<>();
        statusesFilter.add(ChoreStatus.PENDING);

        ChoreAssignmentFilterRequestDTO requestDTO = new ChoreAssignmentFilterRequestDTO(
                statusesFilter,
                TEST_USER_ID,
                TEST_CHORE_DESCRIPTION,
                new DateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7))
        );

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.getFilteredChoreAssignments(null, requestDTO);
        });

        Assertions.assertEquals("User ID cannot be null", exception.getMessage());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should throw IllegalArgumentException when DTO is null")
    void testGetFilteredChoreAssignments_DtoNull() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.getFilteredChoreAssignments(TEST_USER_ID, null);
        });

        Assertions.assertEquals("No filter DTO provided", exception.getMessage());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should throw IllegalArgumentException when user not found")
    void testGetFilteredChoreAssignments_UserNotFound() {
        List<ChoreStatus> statusesFilter = new ArrayList<>();
        statusesFilter.add(ChoreStatus.PENDING);

        ChoreAssignmentFilterRequestDTO requestDTO = new ChoreAssignmentFilterRequestDTO(
                statusesFilter,
                TEST_USER_ID,
                TEST_CHORE_DESCRIPTION,
                new DateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7))
        );

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.getFilteredChoreAssignments(TEST_USER_ID, requestDTO);
        });

        Assertions.assertEquals("User with ID: " + TEST_USER_ID + " not found.", exception.getMessage());

        verify(userRepository).findById(TEST_USER_ID);
        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should throw IllegalStateException when user has no household membership")
    void testGetFilteredChoreAssignments_NoHouseholdMembership() {
        List<ChoreStatus> statusesFilter = new ArrayList<>();
        statusesFilter.add(ChoreStatus.PENDING);

        ChoreAssignmentFilterRequestDTO requestDTO = new ChoreAssignmentFilterRequestDTO(
                statusesFilter,
                TEST_USER_ID,
                TEST_CHORE_DESCRIPTION,
                new DateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7))
        );

        User userWithoutMembership = new User();
        ReflectionTestUtils.setField(userWithoutMembership, "id", TEST_USER_ID);
        userWithoutMembership.setHouseholdMembership(null);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithoutMembership));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            choreService.getFilteredChoreAssignments(TEST_USER_ID, requestDTO);
        });

        Assertions.assertTrue(exception.getMessage().contains("not currently a member of any household"));

        verify(userRepository).findById(TEST_USER_ID);
        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should return empty list when no assignments match filters")
    void testGetFilteredChoreAssignments_EmptyResult() {
        List<ChoreStatus> statusesFilter = new ArrayList<>();
        statusesFilter.add(ChoreStatus.PENDING);

        ChoreAssignmentFilterRequestDTO requestDTO = new ChoreAssignmentFilterRequestDTO(
                statusesFilter,
                TEST_USER_ID,
                TEST_CHORE_DESCRIPTION,
                new DateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7))
        );

        testUser.setHouseholdMembership(testMembership);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(choreAssignmentRepository.findAll(any(Specification.class)))
                .thenReturn(java.util.Collections.emptyList());

        List<ChoreAssignmentResponseDTO> responseDTOs = choreService.getFilteredChoreAssignments(TEST_USER_ID, requestDTO);

        Assertions.assertNotNull(responseDTOs);
        Assertions.assertTrue(responseDTOs.isEmpty());

        verify(userRepository).findById(TEST_USER_ID);
        verify(choreAssignmentRepository).findAll(any(Specification.class));
    }

    // ============ Tests for getUserAssignmentOverview ============

    @Test
    @DisplayName("getUserAssignmentOverview - should return user assignment overview with completed and overdue counts")
    void testGetUserAssignmentOverview_Success() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(choreAssignmentRepository.countByAssignedUserIdAndChoreStatus(TEST_USER_ID, ChoreStatus.COMPLETED)).thenReturn(3);
        when(choreAssignmentRepository.countByAssignedUserIdAndChoreStatus(TEST_USER_ID, ChoreStatus.OVERDUE)).thenReturn(1);

        com.housemate.shared.dto.chore.response.AssignmentOverviewDTO responseDTO = choreService.getUserAssignmentOverview(TEST_USER_ID);

        Assertions.assertNotNull(responseDTO);
        Assertions.assertEquals(3, responseDTO.pendingAssignments());
        Assertions.assertEquals(1, responseDTO.overdueAssignments());

        verify(userRepository).findById(TEST_USER_ID);
        verify(choreAssignmentRepository).countByAssignedUserIdAndChoreStatus(TEST_USER_ID, ChoreStatus.COMPLETED);
        verify(choreAssignmentRepository).countByAssignedUserIdAndChoreStatus(TEST_USER_ID, ChoreStatus.OVERDUE);
    }

    @Test
    @DisplayName("getUserAssignmentOverview - should throw IllegalArgumentException when userId is null")
    void testGetUserAssignmentOverview_UserIdNull() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.getUserAssignmentOverview(null);
        });

        Assertions.assertEquals("User ID cannot be null", exception.getMessage());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(choreAssignmentRepository);
    }

    @Test
    @DisplayName("getUserAssignmentOverview - should throw IllegalArgumentException when user not found")
    void testGetUserAssignmentOverview_UserNotFound() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            choreService.getUserAssignmentOverview(TEST_USER_ID);
        });

        Assertions.assertEquals("User with ID: " + TEST_USER_ID + " not found.", exception.getMessage());

        verify(userRepository).findById(TEST_USER_ID);
        verifyNoInteractions(choreAssignmentRepository);
    }

}
