package com.housemate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.backend.ServerApp;
import com.housemate.backend.service.ChoreService;
import com.housemate.shared.dto.chore.request.ChoreAssignmentCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreAssignmentFilterRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreReassignRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.AssignmentOverviewDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.ChoreStatus;
import com.housemate.shared.utils.types.DateRange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(ChoreController.class)
@DisplayName("ChoreController Integration Tests")
@WithMockUser(username = "00000000-0000-0000-0000-000000000003") //this has to be a literal, is the same as TEST_USER_ID
class ChoreControllerTest {

    // ============ Injected Dependencies ============
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChoreService choreService;

    // ============ Test Data Constants ============
    private static final UUID TEST_CHORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TEST_ASSIGNMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID TEST_SECOND_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

    private static final String TEST_CHORE_DESCRIPTION = "Vacuum living room";
    private static final String TEST_USER_NAME = "John Doe";
    private static final Integer TEST_FREQUENCY_DAYS = 7;

    // ============ Test Objects ============
    private ChoreResponseDTO testChoreResponseDTO;
    private ChoreAssignmentResponseDTO testAssignmentResponseDTO;
    private AssignmentOverviewDTO testOverviewDTO;
    private ChoreCreateRequestDTO testChoreCreateRequestDTO;
    private ChoreAssignmentCreateRequestDTO testAssignmentCreateRequestDTO;
    private ChoreStatusUpdateRequestDTO testStatusUpdateRequestDTO;
    private ChoreReassignRequestDTO testReassignRequestDTO;
    private ChoreAssignmentFilterRequestDTO testFilterRequestDTO;

    @BeforeEach
    void setUp() {
        testChoreResponseDTO = createTestChoreResponseDTO();
        testAssignmentResponseDTO = createTestAssignmentResponseDTO();
        testOverviewDTO = createTestOverviewDTO();
        testChoreCreateRequestDTO = createTestChoreCreateRequestDTO();
        testAssignmentCreateRequestDTO = createTestAssignmentCreateRequestDTO();
        testStatusUpdateRequestDTO = createTestStatusUpdateRequestDTO();
        testReassignRequestDTO = createTestReassignRequestDTO();
        testFilterRequestDTO = createTestFilterRequestDTO();
    }

    // ============ Helper Methods ============

    private ChoreResponseDTO createTestChoreResponseDTO() {
        return new ChoreResponseDTO(TEST_CHORE_ID, TEST_CHORE_DESCRIPTION, TEST_FREQUENCY_DAYS);
    }

    private ChoreAssignmentResponseDTO createTestAssignmentResponseDTO() {
        com.housemate.shared.dto.user.response.UserResponseDTO userDTO = new com.housemate.shared.dto.user.response.UserResponseDTO(
            TEST_USER_ID,
            TEST_USER_NAME,
            null,
            "john@example.com",
            null,
            null
        );
        return new ChoreAssignmentResponseDTO(
            TEST_ASSIGNMENT_ID,
            TEST_CHORE_ID,
            TEST_CHORE_DESCRIPTION,
            userDTO,
            LocalDateTime.now().plusDays(7),
            ChoreStatus.PENDING
        );
    }

    private AssignmentOverviewDTO createTestOverviewDTO() {
        return new AssignmentOverviewDTO(5, 2);
    }

    private ChoreCreateRequestDTO createTestChoreCreateRequestDTO() {
        return new ChoreCreateRequestDTO(TEST_CHORE_DESCRIPTION, TEST_FREQUENCY_DAYS, TEST_HOUSEHOLD_ID);
    }

    private ChoreAssignmentCreateRequestDTO createTestAssignmentCreateRequestDTO() {
        return new ChoreAssignmentCreateRequestDTO(
            TEST_CHORE_ID,
            TEST_USER_ID,
            LocalDateTime.now().plusDays(7)
        );
    }

    private ChoreStatusUpdateRequestDTO createTestStatusUpdateRequestDTO() {
        return new ChoreStatusUpdateRequestDTO(ChoreStatus.COMPLETED);
    }

    private ChoreReassignRequestDTO createTestReassignRequestDTO() {
        return new ChoreReassignRequestDTO(TEST_SECOND_USER_ID);
    }

    private ChoreAssignmentFilterRequestDTO createTestFilterRequestDTO() {
        LocalDateTime now = LocalDateTime.now();
        DateRange dateRange = new DateRange(now, now.plusDays(30));
        return new ChoreAssignmentFilterRequestDTO(
            List.of(ChoreStatus.PENDING),
            TEST_USER_ID,
            null,
            dateRange
        );
    }

    // ============ Tests for POST /api/chores ============

    @Test
    @DisplayName("POST /api/chores - should return 201 Created with ChoreResponseDTO on valid input")
    void testCreateChore_Success() throws Exception {

        when(choreService.createChore(any(ChoreCreateRequestDTO.class))).thenReturn(testChoreResponseDTO);

        mockMvc.perform(post( "/api/chores")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testChoreCreateRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_CHORE_ID.toString()))
                .andExpect(jsonPath("$.description").value(TEST_CHORE_DESCRIPTION));

        verify(choreService).createChore(any(ChoreCreateRequestDTO.class));

    }

    @Test
    @DisplayName("POST /api/chores - should return 400 Bad Request on invalid request body")
    void testCreateChore_InvalidInput() throws Exception {

        ChoreCreateRequestDTO invalidRequestDTO = new ChoreCreateRequestDTO("", TEST_FREQUENCY_DAYS, TEST_HOUSEHOLD_ID);

        mockMvc.perform(post( "/api/chores")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(choreService, never()).createChore(any(ChoreCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/chores - should return 400 Bad Request when service throws IllegalArgumentException")
    void testCreateChore_ServiceError() throws Exception {

        when(choreService.createChore(any(ChoreCreateRequestDTO.class))).thenThrow(new IllegalArgumentException("Household not found"));

        mockMvc.perform(post( "/api/chores")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testChoreCreateRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(choreService).createChore(any(ChoreCreateRequestDTO.class));
    }

    // ============ Tests for DELETE /api/chores/{choreId} ============

    @Test
    @DisplayName("DELETE /api/chores/{choreId} - should return 204 No Content on successful deletion")
    void testDeleteChore_Success() throws Exception {

        doNothing().when(choreService).deleteChore(TEST_CHORE_ID);


        mockMvc.perform(delete( "/api/chores/{choreId}", TEST_CHORE_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(choreService).deleteChore(TEST_CHORE_ID);
    }

    @Test
    @DisplayName("DELETE /api/chores/{choreId} - should return 400 Bad Request when chore not found")
    void testDeleteChore_NotFound() throws Exception {

        doThrow(new IllegalArgumentException("Chore not found")).when(choreService).deleteChore(TEST_CHORE_ID);

        mockMvc.perform(delete( "/api/chores/{choreId}", TEST_CHORE_ID)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(choreService).deleteChore(TEST_CHORE_ID);
    }

    // ============ Tests for POST /api/chores/assignments ============

    @Test
    @DisplayName("POST /api/chores/assignments - should return 201 Created with ChoreAssignmentResponseDTO on valid input")
    void testCreateAssignment_Success() throws Exception {

        when(choreService.createChoreAssignment(any(ChoreAssignmentCreateRequestDTO.class))).thenReturn(testAssignmentResponseDTO);

        mockMvc.perform(post( "/api/chores/assignments")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testAssignmentCreateRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentId").value(TEST_ASSIGNMENT_ID.toString()))
                .andExpect(jsonPath("$.choreId").value(TEST_CHORE_ID.toString()))
                .andExpect(jsonPath("$.choreDescription").value(TEST_CHORE_DESCRIPTION))
                .andExpect(jsonPath("$.assignedUser.id").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.assignedUser.name").value(TEST_USER_NAME))
                .andExpect(jsonPath("$.status").value(ChoreStatus.PENDING.toString()));

        verify(choreService).createChoreAssignment(any(ChoreAssignmentCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/chores/assignments - should return 400 Bad Request on invalid request body")
    void testCreateAssignment_InvalidInput() throws Exception {

        ChoreAssignmentCreateRequestDTO invalidRequestDTO = new ChoreAssignmentCreateRequestDTO(
            null, // Missing choreId
            TEST_USER_ID,
            LocalDateTime.now().plusDays(7)
        );

        mockMvc.perform(post( "/api/chores/assignments")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(choreService, never()).createChoreAssignment(any(ChoreAssignmentCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/chores/assignments - should return 400 Bad Request when service throws IllegalArgumentException")
    void testCreateAssignment_ServiceError() throws Exception {
        when(choreService.createChoreAssignment(any(ChoreAssignmentCreateRequestDTO.class))).thenThrow(new IllegalArgumentException("Chore not found"));

        mockMvc.perform(post( "/api/chores/assignments")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testAssignmentCreateRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(choreService).createChoreAssignment(any(ChoreAssignmentCreateRequestDTO.class));
    }

    // ============ Tests for DELETE /api/chores/assigments/{assignmentId} ============

    @Test
    @DisplayName("DELETE /api/chores/assigments/{assignmentId} - should return 204 No Content on successful deletion")
    void testDeleteChoreAssignment_Success() throws Exception {

        doNothing().when(choreService).deleteChoreAssignment(TEST_ASSIGNMENT_ID);

        mockMvc.perform(delete( "/api/chores/assignments/{assignmentId}", TEST_ASSIGNMENT_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(choreService).deleteChoreAssignment(TEST_ASSIGNMENT_ID);

    }

    @Test
    @DisplayName("DELETE /api/chores/assigments/{assignmentId} - should return 400 Bad Request when assignment not found")
    void testDeleteChoreAssignment_NotFound() throws Exception {

        doThrow(new IllegalArgumentException("Assignment not found")).when(choreService).deleteChoreAssignment(TEST_ASSIGNMENT_ID);

        mockMvc.perform(delete( "/api/chores/assignments/{assignmentId}", TEST_ASSIGNMENT_ID)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(choreService).deleteChoreAssignment(TEST_ASSIGNMENT_ID);
    }

    // ============ Tests for PATCH /api/chores/assignments/{assignmentId}/status ============

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/status - should return 204 No Content on successful status update")
    void testUpdateChoreStatus_Success() throws Exception {

        doNothing().when(choreService).updateChoreAssignmentStatus(eq(TEST_ASSIGNMENT_ID), any(ChoreStatusUpdateRequestDTO.class));

        mockMvc.perform(patch( "/api/chores/assignments/{assignmentId}/status", TEST_ASSIGNMENT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testStatusUpdateRequestDTO)))
                .andExpect(status().isNoContent());

        verify(choreService).updateChoreAssignmentStatus(eq(TEST_ASSIGNMENT_ID), any(ChoreStatusUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/status - should return 400 Bad Request on invalid request body")
    void testUpdateChoreStatus_InvalidInput() throws Exception {

        ChoreStatusUpdateRequestDTO invalidRequestDTO = new ChoreStatusUpdateRequestDTO(null); // Missing status

        mockMvc.perform(patch( "/api/chores/assignments/{assignmentId}/status", TEST_ASSIGNMENT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(choreService, never()).updateChoreAssignmentStatus(any(UUID.class), any(ChoreStatusUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/status - should return 400 Bad Request when assignment not found")
    void testUpdateChoreStatus_NotFound() throws Exception {
        doThrow(new IllegalArgumentException("Assignment not found")).when(choreService).updateChoreAssignmentStatus(eq(TEST_ASSIGNMENT_ID), any(ChoreStatusUpdateRequestDTO.class));

        mockMvc.perform(patch( "/api/chores/assignments/{assignmentId}/status", TEST_ASSIGNMENT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testStatusUpdateRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(choreService).updateChoreAssignmentStatus(eq(TEST_ASSIGNMENT_ID), any(ChoreStatusUpdateRequestDTO.class));
    }

    // ============ Tests for PATCH /api/chores/assignments/{assignmentId}/reassign ============

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/reassign - should return 200 OK with updated ChoreAssignmentResponseDTO")
    void testReassignChore_Success() throws Exception {

        when(choreService.reassignChore(any(UUID.class), any(ChoreReassignRequestDTO.class))).thenReturn(testAssignmentResponseDTO);

        mockMvc.perform(patch( "/api/chores/assignments/{assignmentId}/reassign", TEST_ASSIGNMENT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testReassignRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentId").value(TEST_ASSIGNMENT_ID.toString()))
                .andExpect(jsonPath("$.assignedUser.id").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.assignedUser.name").value(TEST_USER_NAME));

        verify(choreService).reassignChore(any(UUID.class), any(ChoreReassignRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/reassign - should return 400 Bad Request on invalid request body")
    void testReassignChore_InvalidInput() throws Exception {

        ChoreReassignRequestDTO invalidRequestDTO = new ChoreReassignRequestDTO(null); // Missing newUserId

        mockMvc.perform(patch( "/api/chores/assignments/{assignmentId}/reassign", TEST_ASSIGNMENT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(choreService, never()).reassignChore(any(UUID.class), any(ChoreReassignRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/reassign - should return 400 Bad Request when user not found")
    void testReassignChore_UserNotFound() throws Exception {

        when(choreService.reassignChore(any(UUID.class), any(ChoreReassignRequestDTO.class))).thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(patch( "/api/chores/assignments/{assignmentId}/reassign", TEST_ASSIGNMENT_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testReassignRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(choreService).reassignChore(any(UUID.class), any(ChoreReassignRequestDTO.class));
    }

    // ============ Tests for GET /api/chores/{householdId} ============

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003") // Mock authenticated user
    @DisplayName("GET /api/chores/{householdId} - should return 200 OK with list of ChoreResponseDTOs")
    void testGetAllHouseholdChores_Success() throws Exception {

        when(choreService.getAllHouseholdChores(TEST_USER_ID, TEST_HOUSEHOLD_ID)).thenReturn(List.of(testChoreResponseDTO));

        mockMvc.perform(get( "/api/chores/{householdId}", TEST_HOUSEHOLD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(TEST_CHORE_ID.toString()))
                .andExpect(jsonPath("$[0].description").value(TEST_CHORE_DESCRIPTION));

        verify(choreService).getAllHouseholdChores(TEST_USER_ID, TEST_HOUSEHOLD_ID);
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/{householdId} - should return 403 Forbidden when user not member of household")
    void testGetAllHouseholdChores_AccessDenied() throws Exception {

        when(choreService.getAllHouseholdChores(TEST_USER_ID, TEST_HOUSEHOLD_ID)).thenThrow(new AccessDeniedException("User not a member of the household"));

        mockMvc.perform(get( "/api/chores/{householdId}", TEST_HOUSEHOLD_ID))
                .andExpect(status().isForbidden());

        verify(choreService).getAllHouseholdChores(TEST_USER_ID, TEST_HOUSEHOLD_ID);
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/{householdId} - should return 200 OK with empty list when no chores found")
    void testGetAllHouseholdChores_EmptyResult() throws Exception {

        when(choreService.getAllHouseholdChores(TEST_USER_ID, TEST_HOUSEHOLD_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/chores/{householdId}", TEST_HOUSEHOLD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(choreService).getAllHouseholdChores(TEST_USER_ID, TEST_HOUSEHOLD_ID);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/chores/{householdId} - should return 401 Unauthorized when user not authenticated")
    void testGetAllHouseholdChores_Unauthenticated() throws Exception {

        mockMvc.perform(get("/api/chores/{householdId}", TEST_HOUSEHOLD_ID))
                .andExpect(status().isUnauthorized());

        verify(choreService, never()).getAllHouseholdChores(any(UUID.class), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/{householdId} - should return 405 Method Not Allowed when an incorrect HTTP method is used")
    void testGetAllHouseholdChores_InvalidHttpMethod() throws Exception {

        mockMvc.perform(put("/api/chores/{householdId}", TEST_HOUSEHOLD_ID)
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed());

        verify(choreService, never()).getAllHouseholdChores(any(UUID.class), any(UUID.class));
    }

    // ============ Tests for GET /api/chores/assignments/{householdId}/overview ============

    @Test
    @DisplayName("GET /api/chores/assignments/{householdId}/overview - should return 200 OK with AssignmentOverviewDTO")
    void testGetAssignmentOverview_Success() throws Exception {

        when(choreService.getAssignmentOverview(TEST_HOUSEHOLD_ID)).thenReturn(testOverviewDTO);

        mockMvc.perform(get("/api/chores/assignments/{householdId}/overview", TEST_HOUSEHOLD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingAssignments").value(testOverviewDTO.pendingAssignments()))
                .andExpect(jsonPath("$.overdueAssignments").value(testOverviewDTO.overdueAssignments()));

        verify(choreService).getAssignmentOverview(TEST_HOUSEHOLD_ID);
    }

    @Test
    @DisplayName("GET /api/chores/assignments/{householdId}/overview - should return 400 Bad Request when household not found")
    void testGetAssignmentOverview_NotFound() throws Exception {

        when(choreService.getAssignmentOverview(TEST_HOUSEHOLD_ID)).thenThrow(new IllegalArgumentException("Household not found"));

        mockMvc.perform(get("/api/chores/assignments/{householdId}/overview", TEST_HOUSEHOLD_ID))
                .andExpect(status().isBadRequest());

        verify(choreService).getAssignmentOverview(TEST_HOUSEHOLD_ID);
    }

    // ============ Tests for GET /api/chores/assignments ============

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/assignments - should return 200 OK with filtered list of ChoreAssignmentResponseDTOs")
    void testGetFilteredChoreAssignments_Success() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        String startDateStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endDateStr = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        when(choreService.getFilteredChoreAssignments(any(UUID.class), any(ChoreAssignmentFilterRequestDTO.class)))
                .thenReturn(List.of(testAssignmentResponseDTO));

        mockMvc.perform(get("/api/chores/assignments")
                        .param("statuses", ChoreStatus.PENDING.name())
                        .param("assigneeId", TEST_USER_ID.toString())
                        .param("descriptionContains", TEST_CHORE_DESCRIPTION)
                        .param("dateRange.startDate", startDateStr)
                        .param("dateRange.endDate", endDateStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].assignmentId").value(TEST_ASSIGNMENT_ID.toString()));

        verify(choreService).getFilteredChoreAssignments(
                eq(TEST_USER_ID),
                any(ChoreAssignmentFilterRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/assignments - should return 200 OK with empty list when no assignments match filters")
    void testGetFilteredChoreAssignments_EmptyResult() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        String startDateStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endDateStr = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        when(choreService.getFilteredChoreAssignments(any(UUID.class), any(ChoreAssignmentFilterRequestDTO.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/chores/assignments")
                        .param("statuses", ChoreStatus.PENDING.name())
                        .param("assigneeId", TEST_USER_ID.toString())
                        .param("descriptionContains", TEST_CHORE_DESCRIPTION)
                        .param("dateRange.startDate", startDateStr)
                        .param("dateRange.endDate", endDateStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(choreService).getFilteredChoreAssignments(
                eq(TEST_USER_ID),
                any(ChoreAssignmentFilterRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/assignments - should return 400 Bad Request on invalid filter parameters")
    void testGetFilteredChoreAssignments_InvalidInput() throws Exception {

        mockMvc.perform(get("/api/chores/assignments")
                        .param("statuses", ChoreStatus.PENDING.name())
                        .param("assigneeId", TEST_USER_ID.toString())
                        .param("descriptionContains", TEST_CHORE_DESCRIPTION)
                        .param("dateRange.startDate", "invalidDateStr")
                        .param("dateRange.endDate", "invalidDateStr"))
                .andExpect(status().isBadRequest());

        verify(choreService, never()).getFilteredChoreAssignments(
                any(UUID.class),
                any(ChoreAssignmentFilterRequestDTO.class)
        );
    }

    // ============ Tests for GET /api/chores/assignments/me ============

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/assignments/me - should return 200 OK with user assignment overview")
    void testGetUserAssignmentOverview_Success() throws Exception {

        when(choreService.getUserAssignmentOverview(TEST_USER_ID)).thenReturn(testOverviewDTO);

        mockMvc.perform(get("/api/chores/assignments/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingAssignments").value(testOverviewDTO.pendingAssignments()))
                .andExpect(jsonPath("$.overdueAssignments").value(testOverviewDTO.overdueAssignments()));

        verify(choreService).getUserAssignmentOverview(TEST_USER_ID);
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/assignments/me - should return 400 Bad Request when user not found")
    void testGetUserAssignmentOverview_NotFound() throws Exception {

        when(choreService.getUserAssignmentOverview(TEST_USER_ID)).thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/chores/assignments/me"))
                .andExpect(status().isBadRequest());

        verify(choreService).getUserAssignmentOverview(TEST_USER_ID);
    }
}
