package com.housemate.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.housemate.shared.dto.chore.request.ChoreAssignmentCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreAssignmentFilterRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreReassignRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.dto.chore.response.AssignmentOverviewDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.ChoreStatus;
import com.housemate.shared.utils.types.DateRange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ChoreClientService Unit Tests")
class ChoreClientServiceTest {

    // ============ Injected Dependencies ============
    private ChoreClientService choreClientService;
    private HttpRestClient mockHttpRestClient;
    private ObjectMapper objectMapper;

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
    private ChoreCreateRequestDTO testChoreCreateRequestDTO;
    private ChoreAssignmentCreateRequestDTO testAssignmentCreateRequestDTO;
    private ChoreStatusUpdateRequestDTO testStatusUpdateRequestDTO;
    private ChoreReassignRequestDTO testReassignRequestDTO;
    private ChoreAssignmentFilterRequestDTO testFilterRequestDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockHttpRestClient = mock(HttpRestClient.class);
        choreClientService = new ChoreClientService(mockHttpRestClient);
        
        testChoreResponseDTO = createTestChoreResponseDTO();
        testAssignmentResponseDTO = createTestAssignmentResponseDTO();
        testChoreCreateRequestDTO = createTestChoreCreateRequestDTO();
        testAssignmentCreateRequestDTO = createTestAssignmentCreateRequestDTO();
        testStatusUpdateRequestDTO = createTestStatusUpdateRequestDTO();
        testReassignRequestDTO = createTestReassignRequestDTO();
        testFilterRequestDTO = createTestFilterRequestDTO();
    }

    // ============ Helper Methods for Test Data ============

    private ChoreResponseDTO createTestChoreResponseDTO() {
        return new ChoreResponseDTO(TEST_CHORE_ID, TEST_CHORE_DESCRIPTION, TEST_FREQUENCY_DAYS);
    }

    private ChoreAssignmentResponseDTO createTestAssignmentResponseDTO() {
        UserResponseDTO userDTO = new UserResponseDTO(
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
            TEST_CHORE_DESCRIPTION,
            dateRange
        );
    }

    // ============ Helper Methods for HTTP Mocking ============

    @SuppressWarnings("unchecked")
    private <T> HttpResponse<T> createMockResponse(int statusCode, T body) {
        HttpResponse<T> response = (HttpResponse<T>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    // ============ Tests for createChore ============

    @Test
    @DisplayName("createChore - should successfully create a chore and return ChoreResponseDTO")
    void testCreateChore_Success() throws IOException {

        String jsonResponse = objectMapper.writeValueAsString(testChoreResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(201, jsonResponse);

        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"description\":\"test\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, ChoreResponseDTO.class)).thenReturn(testChoreResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        ChoreResponseDTO result = choreClientService.createChore(testChoreCreateRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_CHORE_ID, result.id());
        assertEquals(TEST_CHORE_DESCRIPTION, result.description());

        verify(mockHttpRestClient).serializeDTO(any());
        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, ChoreResponseDTO.class);

    }

    @Test
    @DisplayName("createChore - should throw RuntimeException when server returns error status code")
    void testCreateChore_ServerError()  {

        HttpResponse<String> mockResponse = createMockResponse(400, "Household not found");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"description\":\"test\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> choreClientService.createChore(testChoreCreateRequestDTO));

        assertTrue(exception.getMessage().contains("status code: 400"));
        assertTrue(exception.getMessage().contains("Household not found"));
    }

    // ============ Tests for deleteChore ============

    @Test
    @DisplayName("deleteChore - should successfully delete a chore with status 204")
    void testDeleteChore_Success()  {

        HttpResponse<String> mockResponse = createMockResponse(204, "");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        assertDoesNotThrow(() -> choreClientService.deleteChore(TEST_CHORE_ID));

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
    }

    @Test
    @DisplayName("deleteChore - should throw RuntimeException when server returns error status code")
    void testDeleteChore_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(404, "Chore not found");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> choreClientService.deleteChore(TEST_CHORE_ID));

        assertTrue(exception.getMessage().contains("Failed to delete chore"));
        assertTrue(exception.getMessage().contains("status code: 404"));
    }

    // ============ Tests for createAssignment ============

    @Test
    @DisplayName("createAssignment - should successfully create a chore assignment and return ChoreAssignmentResponseDTO")
    void testCreateAssignment_Success() throws IOException {

        String jsonResponse = objectMapper.writeValueAsString(testAssignmentResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(201, jsonResponse);
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"choreId\":\"test\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, ChoreAssignmentResponseDTO.class)).thenReturn(testAssignmentResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        ChoreAssignmentResponseDTO result = choreClientService.createAssignment(testAssignmentCreateRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_ASSIGNMENT_ID, result.assignmentId());
        assertEquals(TEST_CHORE_ID, result.choreId());
        assertEquals(ChoreStatus.PENDING, result.status());

        verify(mockHttpRestClient).serializeDTO(any());
        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, ChoreAssignmentResponseDTO.class);
    }

    @Test
    @DisplayName("createAssignment - should throw RuntimeException when server returns error status code")
    void testCreateAssignment_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(400, "Invalid chore or user ID");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"choreId\":\"test\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> choreClientService.createAssignment(testAssignmentCreateRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to create chore assignment"));
        assertTrue(exception.getMessage().contains("status code: 400"));
    }

    // ============ Tests for deleteChoreAssignment ============

    @Test
    @DisplayName("deleteChoreAssignment - should successfully delete a chore assignment with status 204")
    void testDeleteChoreAssignment_Success() {

        HttpResponse<String> mockResponse = createMockResponse(204, "");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        assertDoesNotThrow(() -> choreClientService.deleteChoreAssignment(TEST_ASSIGNMENT_ID));

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
    }

    @Test
    @DisplayName("deleteChoreAssignment - should throw RuntimeException when server returns error status code")
    void testDeleteChoreAssignment_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(404, "Assignment not found");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> choreClientService.deleteChoreAssignment(TEST_ASSIGNMENT_ID));

        assertTrue(exception.getMessage().contains("Failed to delete chore assignment"));
        assertTrue(exception.getMessage().contains("status code: 404"));
    }

    // ============ Tests for updateChoreAssignmentStatus ============

    @Test
    @DisplayName("updateChoreAssignmentStatus - should successfully update assignment status with 204 response")
    void testUpdateChoreAssignmentStatus_Success() {

        HttpResponse<String> mockResponse = createMockResponse(204, "");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"newStatus\":\"COMPLETED\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        assertDoesNotThrow(() -> choreClientService.updateChoreAssignmentStatus(TEST_ASSIGNMENT_ID, testStatusUpdateRequestDTO));

        verify(mockHttpRestClient).serializeDTO(any());
        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
    }

    @Test
    @DisplayName("updateChoreAssignmentStatus - should throw RuntimeException when server returns error status code")
    void testUpdateChoreAssignmentStatus_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(400, "Invalid status provided");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"newStatus\":\"COMPLETED\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> choreClientService.updateChoreAssignmentStatus(TEST_ASSIGNMENT_ID, testStatusUpdateRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to update chore assignment status"));
        assertTrue(exception.getMessage().contains("status code: 400"));
    }

    // ============ Tests for reassignChore ============

    @Test
    @DisplayName("reassignChore - should successfully reassign a chore and return updated ChoreAssignmentResponseDTO")
    void testReassignChore_Success() throws IOException {

        String jsonResponse = objectMapper.writeValueAsString(testAssignmentResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"newAssigneeId\":\"test\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, ChoreAssignmentResponseDTO.class)).thenReturn(testAssignmentResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        ChoreAssignmentResponseDTO result = choreClientService.reassignChore(TEST_ASSIGNMENT_ID, testReassignRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_ASSIGNMENT_ID, result.assignmentId());
        assertEquals(TEST_CHORE_ID, result.choreId());

        verify(mockHttpRestClient).serializeDTO(any());
        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, ChoreAssignmentResponseDTO.class);
    }

    @Test
    @DisplayName("reassignChore - should throw RuntimeException when server returns error status code")
    void testReassignChore_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(404, "Assignment or user not found");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"newAssigneeId\":\"test\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> choreClientService.reassignChore(TEST_ASSIGNMENT_ID, testReassignRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to reassign chore"));
        assertTrue(exception.getMessage().contains("status code: 404"));
    }

    // ============ Tests for getFilteredChoreAssignments ============

    @Test
    @DisplayName("getFilteredChoreAssignments - should successfully retrieve filtered assignments and return list of ChoreAssignmentResponseDTO")
    void testGetFilteredChoreAssignments_Success() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(List.of(testAssignmentResponseDTO));

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, ChoreAssignmentResponseDTO.class)).thenReturn(List.of(testAssignmentResponseDTO));
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        List<ChoreAssignmentResponseDTO> result = choreClientService.getFilteredChoreAssignments(testFilterRequestDTO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(TEST_ASSIGNMENT_ID, result.get(0).assignmentId());

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, ChoreAssignmentResponseDTO.class);
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should return empty list when no assignments match filter")
    void testGetFilteredChoreAssignments_EmptyResult() throws Exception {

        String jsonResponse = objectMapper.writeValueAsString(List.of());

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, ChoreAssignmentResponseDTO.class)).thenReturn(List.of());
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        List<ChoreAssignmentResponseDTO> result = choreClientService.getFilteredChoreAssignments(testFilterRequestDTO);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, ChoreAssignmentResponseDTO.class);
    }

    @Test
    @DisplayName("getFilteredChoreAssignments - should throw RuntimeException when server returns error status code")
    void testGetFilteredChoreAssignments_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(400, "Invalid filter parameters");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> choreClientService.getFilteredChoreAssignments(testFilterRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to get filtered chore assignments"));
        assertTrue(exception.getMessage().contains("status code: 400"));
    }

    // ============ Tests for getAllHouseholdChores ============

    @Test
    @DisplayName("getAllHouseholdChores - should successfully retrieve all household chores and return list of ChoreResponseDTO")
    void testGetAllHouseholdChores_Success() throws IOException {

        String jsonResponse = objectMapper.writeValueAsString(List.of(testChoreResponseDTO));

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, ChoreResponseDTO.class)).thenReturn(List.of(testChoreResponseDTO));
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        List<ChoreResponseDTO> result = choreClientService.getAllHouseholdChores();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(TEST_CHORE_ID, result.get(0).id());
        assertEquals(TEST_CHORE_DESCRIPTION, result.get(0).description());

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, ChoreResponseDTO.class);
    }

    @Test
    @DisplayName("getAllHouseholdChores - should return empty list when household has no chores")
    void testGetAllHouseholdChores_EmptyResult() throws IOException {

        String jsonResponse = objectMapper.writeValueAsString(List.of());

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, ChoreResponseDTO.class)).thenReturn(List.of());
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        List<ChoreResponseDTO> result = choreClientService.getAllHouseholdChores();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, ChoreResponseDTO.class);
    }

    @Test
    @DisplayName("getAllHouseholdChores - should throw RuntimeException when server returns error status code")
    void testGetAllHouseholdChores_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(404, "Household not found");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> choreClientService.getAllHouseholdChores());

        assertTrue(exception.getMessage().contains("Failed to get household chores"));
        assertTrue(exception.getMessage().contains("status code: 404"));
    }

    // ============ Tests for getUserAssignmentOverview ============

    @Test
    @DisplayName("getUserAssignmentOverview - should return AssignmentOverviewDTO on successful response")
    void testGetUserAssignmentOverview_Success() throws JsonProcessingException {
        AssignmentOverviewDTO expectedDTO = new com.housemate.shared.dto.chore.response.AssignmentOverviewDTO(3, 1);

        HttpResponse<String> mockResponse = createMockResponse(200, objectMapper.writeValueAsString(expectedDTO));
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");
        when(mockHttpRestClient.deserializeDTO(anyString(), eq(com.housemate.shared.dto.chore.response.AssignmentOverviewDTO.class)))
                .thenReturn(expectedDTO);

        AssignmentOverviewDTO result = choreClientService.getUserAssignmentOverview();

        assertNotNull(result);
        assertEquals(3, result.pendingAssignments());
        assertEquals(1, result.overdueAssignments());

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).buildAuthHeader();
        verify(mockHttpRestClient).deserializeDTO(anyString(), eq(com.housemate.shared.dto.chore.response.AssignmentOverviewDTO.class));
    }

    @Test
    @DisplayName("getUserAssignmentOverview - should throw RuntimeException on server error response")
    void testGetUserAssignmentOverview_BadRequest() {
        HttpResponse<String> mockResponse = createMockResponse(400, "Bad Request");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> choreClientService.getUserAssignmentOverview());

        assertTrue(exception.getMessage().contains("Failed to get user assignment overview"));
        assertTrue(exception.getMessage().contains("status code: 400"));
    }

    @Test
    @DisplayName("getUserAssignmentOverview - should throw RuntimeException on authentication error")
    void testGetUserAssignmentOverview_Unauthorized() {
        HttpResponse<String> mockResponse = createMockResponse(401, "Unauthorized");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer invalid-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> choreClientService.getUserAssignmentOverview());

        assertTrue(exception.getMessage().contains("Failed to get user assignment overview"));
        assertTrue(exception.getMessage().contains("status code: 401"));
    }
}
