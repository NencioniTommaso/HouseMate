package com.housemate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.tomcat.util.http.parser.TE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ChoreController.class)
@DisplayName("ChoreController Integration Tests")
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
        return new ChoreAssignmentResponseDTO(
            TEST_ASSIGNMENT_ID,
            TEST_CHORE_ID,
            TEST_CHORE_DESCRIPTION,
            TEST_USER_NAME,
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
        // TODO: Implement happy path test - expects 201 status and ChoreResponseDTO in response body
    }

    @Test
    @DisplayName("POST /api/chores - should return 400 Bad Request on invalid request body")
    void testCreateChore_InvalidInput() throws Exception {
        // TODO: Implement validation error test - expects 400 status on validation failure
    }

    @Test
    @DisplayName("POST /api/chores - should return 400 Bad Request when service throws IllegalArgumentException")
    void testCreateChore_ServiceError() throws Exception {
        // TODO: Implement service error handling test - expects 400 status when service throws exception
    }

    // ============ Tests for DELETE /api/chores/{choreId} ============

    @Test
    @DisplayName("DELETE /api/chores/{choreId} - should return 204 No Content on successful deletion")
    void testDeleteChore_Success() throws Exception {
        // TODO: Implement happy path deletion test - expects 204 status
    }

    @Test
    @DisplayName("DELETE /api/chores/{choreId} - should return 400 Bad Request when chore not found")
    void testDeleteChore_NotFound() throws Exception {
        // TODO: Implement not found error test - expects 400 status when service throws IllegalArgumentException
    }

    // ============ Tests for POST /api/chores/assignments ============

    @Test
    @DisplayName("POST /api/chores/assignments - should return 201 Created with ChoreAssignmentResponseDTO on valid input")
    void testCreateAssignment_Success() throws Exception {
        // TODO: Implement happy path assignment creation test - expects 201 status and ChoreAssignmentResponseDTO in response body
    }

    @Test
    @DisplayName("POST /api/chores/assignments - should return 400 Bad Request on invalid request body")
    void testCreateAssignment_InvalidInput() throws Exception {
        // TODO: Implement validation error test - expects 400 status on validation failure
    }

    @Test
    @DisplayName("POST /api/chores/assignments - should return 400 Bad Request when service throws IllegalArgumentException")
    void testCreateAssignment_ServiceError() throws Exception {
        // TODO: Implement service error handling test - expects 400 status when service throws exception (chore or user not found)
    }

    // ============ Tests for DELETE /api/chores/assigments/{assignmentId} ============

    @Test
    @DisplayName("DELETE /api/chores/assigments/{assignmentId} - should return 204 No Content on successful deletion")
    void testDeleteChoreAssignment_Success() throws Exception {
        // TODO: Implement happy path deletion test - expects 204 status
    }

    @Test
    @DisplayName("DELETE /api/chores/assigments/{assignmentId} - should return 400 Bad Request when assignment not found")
    void testDeleteChoreAssignment_NotFound() throws Exception {
        // TODO: Implement not found error test - expects 400 status when service throws IllegalArgumentException
    }

    // ============ Tests for PATCH /api/chores/assignments/{assignmentId}/status ============

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/status - should return 204 No Content on successful status update")
    void testUpdateChoreStatus_Success() throws Exception {
        // TODO: Implement happy path status update test - expects 204 status
        // NOTE: Current implementation has bug - uses 'id' instead of 'assignmentId' path variable
    }

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/status - should return 400 Bad Request on invalid request body")
    void testUpdateChoreStatus_InvalidInput() throws Exception {
        // TODO: Implement validation error test - expects 400 status on validation failure
    }

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/status - should return 400 Bad Request when assignment not found")
    void testUpdateChoreStatus_NotFound() throws Exception {
        // TODO: Implement not found error test - expects 400 status when service throws IllegalArgumentException
    }

    // ============ Tests for PATCH /api/chores/assignments/{assignmentId}/reassign ============

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/reassign - should return 200 OK with updated ChoreAssignmentResponseDTO")
    void testReassignChore_Success() throws Exception {
        // TODO: Implement happy path reassignment test - expects 200 status and updated ChoreAssignmentResponseDTO in response body
    }

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/reassign - should return 400 Bad Request on invalid request body")
    void testReassignChore_InvalidInput() throws Exception {
        // TODO: Implement validation error test - expects 400 status on validation failure
    }

    @Test
    @DisplayName("PATCH /api/chores/assignments/{assignmentId}/reassign - should return 400 Bad Request when user not found")
    void testReassignChore_UserNotFound() throws Exception {
        // TODO: Implement user not found error test - expects 400 status when service throws IllegalArgumentException
    }

    // ============ Tests for GET /api/chores/{householdId} ============

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003") // Mock authenticated user
    @DisplayName("GET /api/chores/{householdId} - should return 200 OK with list of ChoreResponseDTOs")
    void testGetAllHouseholdChores_Success() throws Exception {
        // TODO: Implement happy path retrieval test - expects 200 status and list of ChoreResponseDTOs
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/{householdId} - should return 403 Forbidden when user not member of household")
    void testGetAllHouseholdChores_AccessDenied() throws Exception {
        // TODO: Implement access denied test - expects 403 status when service throws AccessDeniedException
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/{householdId} - should return 200 OK with empty list when no chores found")
    void testGetAllHouseholdChores_EmptyResult() throws Exception {
        // TODO: Implement empty result test - expects 200 status with empty list in response body
    }

    @Test
    @DisplayName("GET /api/chores/{householdId} - should return 401 Unauthorized when user not authenticated")
    void testGetAllHouseholdChores_Unauthenticated() throws Exception {
        // TODO: Implement unauthenticated request test - expects 401 status when no authentication provided
        // NOTE: Test may need to be adjusted if security filter is re-enabled
    }

    // ============ Tests for GET /api/chores/assignments/{householdId}/overview ============

    @Test
    @DisplayName("GET /api/chores/assignments/{householdId}/overview - should return 200 OK with AssignmentOverviewDTO")
    void testGetAssignmentOverview_Success() throws Exception {
        // TODO: Implement happy path overview retrieval test - expects 200 status and AssignmentOverviewDTO in response body
    }

    @Test
    @DisplayName("GET /api/chores/assignments/{householdId}/overview - should return 400 Bad Request when household not found")
    void testGetAssignmentOverview_NotFound() throws Exception {
        // TODO: Implement not found error test - expects 400 status when service throws IllegalArgumentException
    }

    // ============ Tests for GET /api/chores/assignments ============

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/assignments - should return 200 OK with filtered list of ChoreAssignmentResponseDTOs")
    void testGetFilteredChoreAssignments_Success() throws Exception {
        // TODO: Implement happy path filtered retrieval test - expects 200 status and list of ChoreAssignmentResponseDTOs
        // NOTE: This endpoint uses @ModelAttribute for query parameters, not request body
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/assignments - should return 200 OK with empty list when no assignments match filters")
    void testGetFilteredChoreAssignments_EmptyResult() throws Exception {
        // TODO: Implement empty result test - expects 200 status with empty list in response body
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003")
    @DisplayName("GET /api/chores/assignments - should return 400 Bad Request on invalid filter parameters")
    void testGetFilteredChoreAssignments_InvalidInput() throws Exception {
        // TODO: Implement validation error test - expects 400 status on invalid filter parameters
    }

    @Test
    @DisplayName("GET /api/chores/assignments - should return 401 Unauthorized when user not authenticated")
    void testGetFilteredChoreAssignments_Unauthenticated() throws Exception {
        // TODO: Implement unauthenticated request test - expects 401 status when no authentication provided
        // NOTE: Test may need to be adjusted if security filter is re-enabled
    }

}

