package com.housemate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.backend.service.HouseholdService;
import com.housemate.shared.dto.household.request.AddMemberRequestDTO;
import com.housemate.shared.dto.household.request.HouseholdCreateRequestDTO;
import com.housemate.shared.dto.household.response.HouseholdInvitationCodeResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HouseholdController.class)
@DisplayName("HouseholdController Integration Tests")
@WithMockUser(username = "10000000-0000-0000-0000-000000000001")
@SuppressWarnings("null")
class HouseholdControllerTest {

    private static final String BASE_URL = "/api/households";
    private static final UUID TEST_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID TEST_MEMBER_ID = UUID.fromString("10000000-0000-0000-0000-000000000003");

    private static final String TEST_HOUSEHOLD_NAME = "Casa Bella";
    private static final String TEST_USER_NAME = "Mario";
    private static final String TEST_USER_SURNAME = "Rossi";
    private static final String TEST_USER_EMAIL = "mario.rossi@example.com";
    private static final String TEST_USER_IBAN = "IT60X0542811101000000123456";
    private static final String TEST_INVITATION_CODE = "invitation-code-123";
    private static final LocalDateTime TEST_INVITATION_REFRESHED_AT = LocalDateTime.of(2026, 4, 3, 12, 30);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HouseholdService householdService;

    private HouseholdCreateRequestDTO testCreateRequestDTO;
    private AddMemberRequestDTO testAddMemberRequestDTO;
    private HouseholdResponseDTO testHouseholdResponseDTO;
    private HouseholdInvitationCodeResponseDTO testInvitationCodeResponseDTO;

    @BeforeEach
    void setUp() {
        testCreateRequestDTO = new HouseholdCreateRequestDTO(TEST_HOUSEHOLD_NAME);
        testAddMemberRequestDTO = new AddMemberRequestDTO(TEST_INVITATION_CODE);
        testHouseholdResponseDTO = createTestHouseholdResponseDTO();
        testInvitationCodeResponseDTO = createTestInvitationCodeResponseDTO();
    }

    private HouseholdResponseDTO createTestHouseholdResponseDTO() {
        UserResponseDTO member = new UserResponseDTO(
            TEST_USER_ID,
            TEST_USER_NAME,
            TEST_USER_SURNAME,
            TEST_USER_EMAIL,
            TEST_USER_IBAN,
            null
        );

        return new HouseholdResponseDTO(
            TEST_HOUSEHOLD_ID,
            TEST_HOUSEHOLD_NAME,
            LocalDate.of(2026, 4, 3),
            List.of(member)
        );
    }

    private HouseholdInvitationCodeResponseDTO createTestInvitationCodeResponseDTO() {
        return new HouseholdInvitationCodeResponseDTO(TEST_INVITATION_CODE, TEST_INVITATION_REFRESHED_AT);
    }

    // ============ Tests for POST /api/households ============

    @Test
    @DisplayName("POST /api/households - should return 201 Created with HouseholdResponseDTO on valid input")
    void testCreateHousehold_Success() throws Exception {
        when(householdService.createHousehold(eq(TEST_USER_ID), any(HouseholdCreateRequestDTO.class)))
            .thenReturn(testHouseholdResponseDTO);

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testCreateRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_HOUSEHOLD_ID.toString()))
                .andExpect(jsonPath("$.name").value(TEST_HOUSEHOLD_NAME))
                .andExpect(jsonPath("$.creationDate").value("2026-04-03"))
                .andExpect(jsonPath("$.members[0].id").value(TEST_USER_ID.toString()));

        verify(householdService).createHousehold(eq(TEST_USER_ID), any(HouseholdCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/households - should return 400 Bad Request on invalid request body")
    void testCreateHousehold_InvalidInput() throws Exception {
        HouseholdCreateRequestDTO invalidRequestDTO = new HouseholdCreateRequestDTO("   ");

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(householdService, never()).createHousehold(any(UUID.class), any(HouseholdCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/households - should return 400 Bad Request when service throws IllegalArgumentException")
    void testCreateHousehold_IllegalArgument() throws Exception {
        when(householdService.createHousehold(eq(TEST_USER_ID), any(HouseholdCreateRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Household with same name already exists"));

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testCreateRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(householdService).createHousehold(eq(TEST_USER_ID), any(HouseholdCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/households - should return 403 Forbidden when service throws IllegalStateException")
    void testCreateHousehold_IllegalState() throws Exception {
        when(householdService.createHousehold(eq(TEST_USER_ID), any(HouseholdCreateRequestDTO.class)))
            .thenThrow(new IllegalStateException("User already belongs to a household"));

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testCreateRequestDTO)))
                .andExpect(status().isForbidden());

        verify(householdService).createHousehold(eq(TEST_USER_ID), any(HouseholdCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/households - should return 403 Forbidden when service throws AccessDeniedException")
    void testCreateHousehold_Forbidden() throws Exception {
        when(householdService.createHousehold(eq(TEST_USER_ID), any(HouseholdCreateRequestDTO.class)))
            .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testCreateRequestDTO)))
                .andExpect(status().isForbidden());

        verify(householdService).createHousehold(eq(TEST_USER_ID), any(HouseholdCreateRequestDTO.class));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/households - should return 401 Unauthorized for unauthenticated user")
    void testCreateHousehold_Unauthenticated() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testCreateRequestDTO)))
                .andExpect(status().isUnauthorized());

        verify(householdService, never()).createHousehold(any(UUID.class), any(HouseholdCreateRequestDTO.class));
    }

    // ============ Tests for GET /api/households/me ============

    @Test
    @DisplayName("GET /api/households/me - should return 200 OK with HouseholdResponseDTO")
    void testGetCurrentUserHousehold_Success() throws Exception {
        when(householdService.getCurrentUserHousehold(TEST_USER_ID)).thenReturn(testHouseholdResponseDTO);

        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_HOUSEHOLD_ID.toString()))
                .andExpect(jsonPath("$.name").value(TEST_HOUSEHOLD_NAME));

        verify(householdService).getCurrentUserHousehold(TEST_USER_ID);
    }

    @Test
    @DisplayName("GET /api/households/me - should return 403 Forbidden when service throws IllegalStateException")
    void testGetCurrentUserHousehold_IllegalState() throws Exception {
        when(householdService.getCurrentUserHousehold(TEST_USER_ID))
            .thenThrow(new IllegalStateException("User does not belong to any household"));

        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isForbidden());

        verify(householdService).getCurrentUserHousehold(TEST_USER_ID);
    }

    @Test
    @DisplayName("GET /api/households/me - should return 403 Forbidden when service throws AccessDeniedException")
    void testGetCurrentUserHousehold_Forbidden() throws Exception {
        when(householdService.getCurrentUserHousehold(TEST_USER_ID))
            .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isForbidden());

        verify(householdService).getCurrentUserHousehold(TEST_USER_ID);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/households/me - should return 401 Unauthorized for unauthenticated user")
    void testGetCurrentUserHousehold_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isUnauthorized());

        verify(householdService, never()).getCurrentUserHousehold(any(UUID.class));
    }

    // ============ Tests for GET /api/households/invitation-code ============

    @Test
    @DisplayName("GET /api/households/invitation-code - should return 200 OK with invitation code for household member")
    void testGetInvitationCode_Success() throws Exception {
        when(householdService.getInvitationCode(TEST_USER_ID)).thenReturn(testInvitationCodeResponseDTO);

        mockMvc.perform(get(BASE_URL + "/invitation-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitationCode").value(TEST_INVITATION_CODE))
                .andExpect(jsonPath("$.refreshedAt").value("2026-04-03T12:30:00"));

        verify(householdService).getInvitationCode(TEST_USER_ID);
    }

    @Test
    @DisplayName("GET /api/households/invitation-code - should return 403 Forbidden when service throws IllegalStateException")
    void testGetInvitationCode_IllegalState() throws Exception {
        when(householdService.getInvitationCode(TEST_USER_ID))
            .thenThrow(new IllegalStateException("User does not belong to any household"));

        mockMvc.perform(get(BASE_URL + "/invitation-code"))
                .andExpect(status().isForbidden());

        verify(householdService).getInvitationCode(TEST_USER_ID);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/households/invitation-code - should return 401 Unauthorized for unauthenticated user")
    void testGetInvitationCode_Unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/invitation-code"))
                .andExpect(status().isUnauthorized());

        verify(householdService, never()).getInvitationCode(any(UUID.class));
    }

    // ============ Tests for POST /api/households/invitation-code/refresh ============

    @Test
    @DisplayName("POST /api/households/invitation-code/refresh - should return 200 OK with refreshed invitation code when requester is admin")
    void testRefreshInvitationCode_Success() throws Exception {
        when(householdService.refreshInvitationCode(TEST_USER_ID)).thenReturn(testInvitationCodeResponseDTO);

        mockMvc.perform(post(BASE_URL + "/invitation-code/refresh")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitationCode").value(TEST_INVITATION_CODE))
                .andExpect(jsonPath("$.refreshedAt").value("2026-04-03T12:30:00"));

        verify(householdService).refreshInvitationCode(TEST_USER_ID);
    }

    @Test
    @DisplayName("POST /api/households/invitation-code/refresh - should return 400 Bad Request when service throws IllegalArgumentException")
    void testRefreshInvitationCode_IllegalArgument() throws Exception {
        when(householdService.refreshInvitationCode(TEST_USER_ID))
            .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post(BASE_URL + "/invitation-code/refresh")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(householdService).refreshInvitationCode(TEST_USER_ID);
    }

    @Test
    @DisplayName("POST /api/households/invitation-code/refresh - should return 403 Forbidden when service throws AccessDeniedException")
    void testRefreshInvitationCode_Forbidden() throws Exception {
        when(householdService.refreshInvitationCode(TEST_USER_ID))
            .thenThrow(new AccessDeniedException("Only household admins can refresh invitation code."));

        mockMvc.perform(post(BASE_URL + "/invitation-code/refresh")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(householdService).refreshInvitationCode(TEST_USER_ID);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/households/invitation-code/refresh - should return 401 Unauthorized for unauthenticated user")
    void testRefreshInvitationCode_Unauthenticated() throws Exception {
        mockMvc.perform(post(BASE_URL + "/invitation-code/refresh")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(householdService, never()).refreshInvitationCode(any(UUID.class));
    }

    // ============ Tests for POST /api/households/members ============

    @Test
    @DisplayName("POST /api/households/members - should return 200 OK with updated HouseholdResponseDTO")
    void testAddMember_Success() throws Exception {
        when(householdService.addMember(eq(TEST_USER_ID), any(AddMemberRequestDTO.class)))
            .thenReturn(testHouseholdResponseDTO);

        mockMvc.perform(post(BASE_URL + "/members")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testAddMemberRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_HOUSEHOLD_ID.toString()));

        verify(householdService).addMember(eq(TEST_USER_ID), any(AddMemberRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/households/members - should return 400 Bad Request on invalid request body")
    void testAddMember_InvalidInput() throws Exception {
        AddMemberRequestDTO invalidRequestDTO = new AddMemberRequestDTO("   ");

        mockMvc.perform(post(BASE_URL + "/members")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(householdService, never()).addMember(any(UUID.class), any(AddMemberRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/households/members - should return 400 Bad Request when service throws IllegalArgumentException")
    void testAddMember_IllegalArgument() throws Exception {
        when(householdService.addMember(eq(TEST_USER_ID), any(AddMemberRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post(BASE_URL + "/members")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testAddMemberRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(householdService).addMember(eq(TEST_USER_ID), any(AddMemberRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/households/members - should return 403 Forbidden when service throws AccessDeniedException")
    void testAddMember_Forbidden() throws Exception {
        when(householdService.addMember(eq(TEST_USER_ID), any(AddMemberRequestDTO.class)))
            .thenThrow(new AccessDeniedException("Only admins can add members"));

        mockMvc.perform(post(BASE_URL + "/members")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testAddMemberRequestDTO)))
                .andExpect(status().isForbidden());

        verify(householdService).addMember(eq(TEST_USER_ID), any(AddMemberRequestDTO.class));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/households/members - should return 401 Unauthorized for unauthenticated user")
    void testAddMember_Unauthenticated() throws Exception {
        mockMvc.perform(post(BASE_URL + "/members")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testAddMemberRequestDTO)))
                .andExpect(status().isUnauthorized());

        verify(householdService, never()).addMember(any(UUID.class), any(AddMemberRequestDTO.class));
    }

    // ============ Tests for DELETE /api/households/members/{memberId} ============

    @Test
    @DisplayName("DELETE /api/households/members/{memberId} - should return 200 OK with updated HouseholdResponseDTO")
    void testRemoveMember_Success() throws Exception {
        when(householdService.removeMember(TEST_USER_ID, TEST_MEMBER_ID)).thenReturn(testHouseholdResponseDTO);

        mockMvc.perform(delete(BASE_URL + "/members/{memberId}", TEST_MEMBER_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_HOUSEHOLD_ID.toString()));

        verify(householdService).removeMember(TEST_USER_ID, TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("DELETE /api/households/members/{memberId} - should return 400 Bad Request for malformed UUID")
    void testRemoveMember_InvalidInput() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/members/{memberId}", "not-a-uuid")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(householdService, never()).removeMember(any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/households/members/{memberId} - should return 400 Bad Request when service throws IllegalArgumentException")
    void testRemoveMember_IllegalArgument() throws Exception {
        when(householdService.removeMember(TEST_USER_ID, TEST_MEMBER_ID))
            .thenThrow(new IllegalArgumentException("User is not a member of this household"));

        mockMvc.perform(delete(BASE_URL + "/members/{memberId}", TEST_MEMBER_ID)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(householdService).removeMember(TEST_USER_ID, TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("DELETE /api/households/members/{memberId} - should return 403 Forbidden when service throws AccessDeniedException")
    void testRemoveMember_Forbidden() throws Exception {
        when(householdService.removeMember(TEST_USER_ID, TEST_MEMBER_ID))
            .thenThrow(new AccessDeniedException("Only admins can remove members"));

        mockMvc.perform(delete(BASE_URL + "/members/{memberId}", TEST_MEMBER_ID)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(householdService).removeMember(TEST_USER_ID, TEST_MEMBER_ID);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("DELETE /api/households/members/{memberId} - should return 401 Unauthorized for unauthenticated user")
    void testRemoveMember_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/members/{memberId}", TEST_MEMBER_ID)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(householdService, never()).removeMember(any(UUID.class), any(UUID.class));
    }

    // ============ Tests for DELETE /api/households/me ============

    @Test
    @DisplayName("DELETE /api/households/me - should return 204 No Content on successful leave")
    void testLeaveHousehold_Success() throws Exception {
        doNothing().when(householdService).leaveHousehold(TEST_USER_ID);

        mockMvc.perform(delete(BASE_URL + "/me")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(householdService).leaveHousehold(TEST_USER_ID);
    }

    @Test
    @DisplayName("DELETE /api/households/me - should return 403 Forbidden when service throws IllegalStateException")
    void testLeaveHousehold_IllegalState() throws Exception {
        doThrow(new IllegalStateException("User does not belong to any household"))
            .when(householdService)
            .leaveHousehold(TEST_USER_ID);

        mockMvc.perform(delete(BASE_URL + "/me")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(householdService).leaveHousehold(TEST_USER_ID);
    }

    @Test
    @DisplayName("DELETE /api/households/me - should return 403 Forbidden when service throws AccessDeniedException")
    void testLeaveHousehold_Forbidden() throws Exception {
        doThrow(new AccessDeniedException("Forbidden"))
            .when(householdService)
            .leaveHousehold(TEST_USER_ID);

        mockMvc.perform(delete(BASE_URL + "/me")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(householdService).leaveHousehold(TEST_USER_ID);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("DELETE /api/households/me - should return 401 Unauthorized for unauthenticated user")
    void testLeaveHousehold_Unauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/me")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(householdService, never()).leaveHousehold(any(UUID.class));
    }
}
