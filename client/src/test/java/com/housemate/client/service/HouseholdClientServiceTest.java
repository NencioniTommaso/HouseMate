package com.housemate.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.housemate.shared.dto.household.request.AddMemberRequestDTO;
import com.housemate.shared.dto.household.request.HouseholdCreateRequestDTO;
import com.housemate.shared.dto.household.response.HouseholdInvitationCodeResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("HouseholdClientService Unit Tests")
class HouseholdClientServiceTest {

    // ============ Injected Dependencies ============
    private HouseholdClientService householdClientService;
    private HttpRestClient mockHttpRestClient;
    private ObjectMapper objectMapper;

    // ============ Test Data Constants ============
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("10000000-0000-0000-0000-000000000020");
    private static final UUID TEST_ADMIN_ID = UUID.fromString("10000000-0000-0000-0000-000000000021");
    private static final UUID TEST_MEMBER_ID = UUID.fromString("10000000-0000-0000-0000-000000000022");

    private static final String TEST_HOUSEHOLD_NAME = "Casa Bella";
    private static final String TEST_ADMIN_NAME = "Mario";
    private static final String TEST_ADMIN_SURNAME = "Rossi";
    private static final String TEST_ADMIN_EMAIL = "mario.rossi@example.com";
    private static final String TEST_ADMIN_IBAN = "IT60X0542811101000000123456";
    private static final String TEST_MEMBER_NAME = "Luigi";
    private static final String TEST_MEMBER_SURNAME = "Verdi";
    private static final String TEST_MEMBER_EMAIL = "luigi.verdi@example.com";
    private static final String TEST_INVITATION_CODE = "invitation-code-123";
    private static final LocalDateTime TEST_INVITATION_REFRESHED_AT = LocalDateTime.of(2026, 4, 3, 12, 30);

    // ============ Test Objects ============
    private HouseholdCreateRequestDTO testCreateRequestDTO;
    private AddMemberRequestDTO testAddMemberRequestDTO;
    private HouseholdResponseDTO testHouseholdResponseDTO;
    private HouseholdInvitationCodeResponseDTO testInvitationCodeResponseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockHttpRestClient = mock(HttpRestClient.class);
        householdClientService = new HouseholdClientService(mockHttpRestClient);

        testCreateRequestDTO = new HouseholdCreateRequestDTO(TEST_HOUSEHOLD_NAME);
        testAddMemberRequestDTO = new AddMemberRequestDTO(TEST_INVITATION_CODE);
        testHouseholdResponseDTO = createTestHouseholdResponseDTO();
        testInvitationCodeResponseDTO = createTestInvitationCodeResponseDTO();
    }

    private HouseholdResponseDTO createTestHouseholdResponseDTO() {
        UserResponseDTO admin = new UserResponseDTO(
            TEST_ADMIN_ID,
            TEST_ADMIN_NAME,
            TEST_ADMIN_SURNAME,
            TEST_ADMIN_EMAIL,
            TEST_ADMIN_IBAN,
            null
        );

        UserResponseDTO member = new UserResponseDTO(
            TEST_MEMBER_ID,
            TEST_MEMBER_NAME,
            TEST_MEMBER_SURNAME,
            TEST_MEMBER_EMAIL,
            null,
            null
        );

        return new HouseholdResponseDTO(
            TEST_HOUSEHOLD_ID,
            TEST_HOUSEHOLD_NAME,
            LocalDate.of(2026, 4, 3),
            List.of(admin, member)
        );
    }

    private HouseholdInvitationCodeResponseDTO createTestInvitationCodeResponseDTO() {
        return new HouseholdInvitationCodeResponseDTO(TEST_INVITATION_CODE, TEST_INVITATION_REFRESHED_AT);
    }

    // ============ Helper Methods for HTTP Mocking ============

    @SuppressWarnings("unchecked")
    private <T> HttpResponse<T> createMockResponse(int statusCode, T body) {
        HttpResponse<T> response = (HttpResponse<T>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    // ============ Tests for createHousehold ============

    @Test
    @DisplayName("createHousehold - should successfully create household and return HouseholdResponseDTO")
    void testCreateHousehold_Success() throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(testHouseholdResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(201, jsonResponse);
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"name\":\"Casa Bella\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, HouseholdResponseDTO.class)).thenReturn(testHouseholdResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        HouseholdResponseDTO result = householdClientService.createHousehold(testCreateRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_HOUSEHOLD_ID, result.id());
        assertEquals(TEST_HOUSEHOLD_NAME, result.name());
        assertEquals(2, result.members().size());

        verify(mockHttpRestClient).serializeDTO(any());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, HouseholdResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("POST", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/households"));
        assertEquals("application/json", capturedRequest.headers().firstValue("Content-Type").orElse(null));
        assertEquals("application/json", capturedRequest.headers().firstValue("Accept").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("createHousehold - should throw RuntimeException when server returns error status code")
    void testCreateHousehold_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(400, "Household already exists");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"name\":\"Casa Bella\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> householdClientService.createHousehold(testCreateRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to create household"));
        assertTrue(exception.getMessage().contains("status code: 400"));
    }

    // ============ Tests for getCurrentUserHousehold ============

    @Test
    @DisplayName("getCurrentUserHousehold - should successfully retrieve current household")
    void testGetCurrentUserHousehold_Success() throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(testHouseholdResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, HouseholdResponseDTO.class)).thenReturn(testHouseholdResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        HouseholdResponseDTO result = householdClientService.getCurrentUserHousehold();

        assertNotNull(result);
        assertEquals(TEST_HOUSEHOLD_ID, result.id());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, HouseholdResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("GET", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/households/me"));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("getCurrentUserHousehold - should throw RuntimeException when server returns error status code")
    void testGetCurrentUserHousehold_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(404, "No household found");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> householdClientService.getCurrentUserHousehold());

        assertTrue(exception.getMessage().contains("Failed to retrieve current household"));
        assertTrue(exception.getMessage().contains("status code: 404"));
    }

    // ============ Tests for addMember ============

    @Test
    @DisplayName("addMember - should successfully add member and return updated HouseholdResponseDTO")
    void testAddMember_Success() throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(testHouseholdResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"invitationCode\":\"invitation-code-123\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, HouseholdResponseDTO.class)).thenReturn(testHouseholdResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        HouseholdResponseDTO result = householdClientService.addMember(testAddMemberRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_HOUSEHOLD_ID, result.id());

        verify(mockHttpRestClient).serializeDTO(any());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, HouseholdResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("POST", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/households/members"));
        assertEquals("application/json", capturedRequest.headers().firstValue("Content-Type").orElse(null));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("addMember - should throw RuntimeException when server returns error status code")
    void testAddMember_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(403, "Invalid household invitation code");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"invitationCode\":\"invitation-code-123\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> householdClientService.addMember(testAddMemberRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to join household using invitation code"));
        assertTrue(exception.getMessage().contains("status code: 403"));
    }

    // ============ Tests for removeMember ============

    @Test
    @DisplayName("removeMember - should successfully remove member and return updated HouseholdResponseDTO")
    void testRemoveMember_Success() throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(testHouseholdResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, HouseholdResponseDTO.class)).thenReturn(testHouseholdResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        HouseholdResponseDTO result = householdClientService.removeMember(TEST_MEMBER_ID);

        assertNotNull(result);
        assertEquals(TEST_HOUSEHOLD_ID, result.id());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, HouseholdResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("DELETE", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/households/members/" + TEST_MEMBER_ID));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("removeMember - should throw RuntimeException when server returns error status code")
    void testRemoveMember_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(400, "User is not part of this household");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> householdClientService.removeMember(TEST_MEMBER_ID));

        assertTrue(exception.getMessage().contains("Failed to remove household member"));
        assertTrue(exception.getMessage().contains("status code: 400"));
    }

    // ============ Tests for leaveHousehold ============

    @Test
    @DisplayName("leaveHousehold - should succeed when server returns 204")
    void testLeaveHousehold_Success() {
        HttpResponse<String> mockResponse = createMockResponse(204, "");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        assertDoesNotThrow(() -> householdClientService.leaveHousehold());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("DELETE", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/households/me"));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("leaveHousehold - should throw RuntimeException when server returns error status code")
    void testLeaveHousehold_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(500, "Unexpected error");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> householdClientService.leaveHousehold());

        assertTrue(exception.getMessage().contains("Failed to leave household"));
        assertTrue(exception.getMessage().contains("status code: 500"));
    }

    // ============ Tests for getInvitationCode ============

    @Test
    @DisplayName("getInvitationCode - should successfully retrieve household invitation code")
    void testGetInvitationCode_Success() throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(testInvitationCodeResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, HouseholdInvitationCodeResponseDTO.class))
            .thenReturn(testInvitationCodeResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        HouseholdInvitationCodeResponseDTO result = householdClientService.getInvitationCode();

        assertNotNull(result);
        assertEquals(TEST_INVITATION_CODE, result.invitationCode());
        assertEquals(TEST_INVITATION_REFRESHED_AT, result.refreshedAt());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, HouseholdInvitationCodeResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("GET", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/households/invitation-code"));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("getInvitationCode - should throw RuntimeException when server returns error status code")
    void testGetInvitationCode_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(403, "User does not belong to a household");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> householdClientService.getInvitationCode());

        assertTrue(exception.getMessage().contains("Failed to retrieve household invitation code"));
        assertTrue(exception.getMessage().contains("status code: 403"));
    }

    // ============ Tests for refreshInvitationCode ============

    @Test
    @DisplayName("refreshInvitationCode - should successfully refresh household invitation code")
    void testRefreshInvitationCode_Success() throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(testInvitationCodeResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, HouseholdInvitationCodeResponseDTO.class))
            .thenReturn(testInvitationCodeResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        HouseholdInvitationCodeResponseDTO result = householdClientService.refreshInvitationCode();

        assertNotNull(result);
        assertEquals(TEST_INVITATION_CODE, result.invitationCode());
        assertEquals(TEST_INVITATION_REFRESHED_AT, result.refreshedAt());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpRestClient).sendRequest(requestCaptor.capture());
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, HouseholdInvitationCodeResponseDTO.class);
        verify(mockHttpRestClient).buildAuthHeader();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("POST", capturedRequest.method());
        assertTrue(capturedRequest.uri().getPath().endsWith("/api/households/invitation-code/refresh"));
        assertEquals("Bearer test-token", capturedRequest.headers().firstValue("Authorization").orElse(null));
    }

    @Test
    @DisplayName("refreshInvitationCode - should throw RuntimeException when server returns error status code")
    void testRefreshInvitationCode_ServerError() {
        HttpResponse<String> mockResponse = createMockResponse(403, "Only household admins can refresh invitation code");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> householdClientService.refreshInvitationCode());

        assertTrue(exception.getMessage().contains("Failed to refresh household invitation code"));
        assertTrue(exception.getMessage().contains("status code: 403"));
    }
}
