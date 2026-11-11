package com.housemate.backend.service;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.household.HouseholdMembershipRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.household.request.AddMemberRequestDTO;
import com.housemate.shared.dto.household.request.HouseholdCreateRequestDTO;
import com.housemate.shared.dto.household.response.HouseholdInvitationCodeResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdMemberResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("HouseholdService Unit Tests")
class HouseholdServiceTest {

    // ============ Mock Dependencies ============
    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private HouseholdMembershipRepository householdMembershipRepository;

    @Mock
    private UserRepository userRepository;

    // ============ Service Under Test ============
    @InjectMocks
    private HouseholdService householdService;

    // ============ Test Data Constants ============
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("10000000-0000-0000-0000-000000000010");
    private static final UUID TEST_REQUESTER_ID = UUID.fromString("10000000-0000-0000-0000-000000000011");
    private static final UUID TEST_MEMBER_ID = UUID.fromString("10000000-0000-0000-0000-000000000012");

    private static final String TEST_HOUSEHOLD_NAME = "HouseMate Home";
    private static final String TEST_REQUESTER_EMAIL = "mario.rossi@example.com";
    private static final String TEST_MEMBER_EMAIL = "luigi.verdi@example.com";
    private static final String TEST_INVITATION_CODE = "inv-code-initial-123";
    private static final LocalDateTime TEST_INVITATION_REFRESHED_AT = LocalDateTime.of(2026, 4, 3, 12, 30);
    private static final LocalDate TEST_REQUESTER_MEMBERSHIP_DATE = LocalDate.of(2026, 4, 4);
    private static final LocalDate TEST_MEMBER_MEMBERSHIP_DATE = LocalDate.of(2026, 4, 5);

    // ============ Test Objects ============
    private Household testHousehold;
    private User testRequester;
    private User testMember;
    private HouseholdMembership requesterMembership;
    private HouseholdMembership memberMembership;

    @BeforeEach
    void setUp() {
        testHousehold = createTestHousehold(TEST_HOUSEHOLD_ID, TEST_HOUSEHOLD_NAME);
        testRequester = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        testMember = createTestUser(TEST_MEMBER_ID, "Luigi", "Verdi", TEST_MEMBER_EMAIL);

        requesterMembership = createMembership(testHousehold, testRequester, true, TEST_REQUESTER_MEMBERSHIP_DATE);
        memberMembership = createMembership(testHousehold, testMember, false, TEST_MEMBER_MEMBERSHIP_DATE);

        testHousehold.setMemberships(new ArrayList<>(List.of(requesterMembership, memberMembership)));
        testRequester.setHouseholdMembership(requesterMembership);
        testMember.setHouseholdMembership(memberMembership);
    }

    // ============ Helper Methods ============

    private Household createTestHousehold(UUID id, String name) {
        Household household = new Household();
        ReflectionTestUtils.setField(household, "id", id);
        ReflectionTestUtils.setField(household, "date", LocalDate.of(2026, 4, 3));
        household.setName(name);
        household.setInvitationCode(TEST_INVITATION_CODE);
        household.setInvitationCodeRefreshedAt(TEST_INVITATION_REFRESHED_AT);
        household.setMemberships(new ArrayList<>());
        return household;
    }

    private User createTestUser(UUID id, String name, String surname, String email) {
        User user = new User(name, surname, email, "hashed-password");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private HouseholdMembership createMembership(Household household, User user, boolean isAdmin) {
        return createMembership(household, user, isAdmin, LocalDate.of(2026, 4, 3));
    }

    private HouseholdMembership createMembership(Household household, User user, boolean isAdmin, LocalDate date) {
        HouseholdMembership membership = new HouseholdMembership(household, user, isAdmin);
        ReflectionTestUtils.setField(membership, "date", date);
        return membership;
    }

    // ============ Tests for createHousehold ============

    @Test
    @DisplayName("createHousehold - should create household and return HouseholdResponseDTO on valid input")
    void testCreateHousehold_Success() {
        HouseholdCreateRequestDTO requestDTO = new HouseholdCreateRequestDTO(TEST_HOUSEHOLD_NAME);
        User creator = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);

        when(householdRepository.existsByName(TEST_HOUSEHOLD_NAME)).thenReturn(false);
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(creator));
        when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> {
            Household saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", TEST_HOUSEHOLD_ID);
            ReflectionTestUtils.setField(saved, "date", LocalDate.of(2026, 4, 3));
            return saved;
        });

        HouseholdResponseDTO response = householdService.createHousehold(TEST_REQUESTER_ID, requestDTO);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(TEST_HOUSEHOLD_ID, response.id());
        Assertions.assertEquals(TEST_HOUSEHOLD_NAME, response.name());
        Assertions.assertEquals(1, response.memberships().size());
        Assertions.assertEquals(TEST_REQUESTER_ID, response.memberships().get(0).user().id());

        verify(householdRepository).existsByName(TEST_HOUSEHOLD_NAME);
        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    @DisplayName("createHousehold - should throw IllegalArgumentException when name is blank")
    void testCreateHousehold_BlankName() {
        HouseholdCreateRequestDTO requestDTO = new HouseholdCreateRequestDTO("   ");

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.createHousehold(TEST_REQUESTER_ID, requestDTO));

        Assertions.assertEquals("Household name cannot be blank", exception.getMessage());

        verifyNoInteractions(householdRepository, userRepository);
    }

    @Test
    @DisplayName("createHousehold - should throw IllegalArgumentException when household with same name already exists")
    void testCreateHousehold_DuplicateName() {
        HouseholdCreateRequestDTO requestDTO = new HouseholdCreateRequestDTO(TEST_HOUSEHOLD_NAME);

        when(householdRepository.existsByName(TEST_HOUSEHOLD_NAME)).thenReturn(true);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.createHousehold(TEST_REQUESTER_ID, requestDTO));

        Assertions.assertEquals("Household with name: " + TEST_HOUSEHOLD_NAME + " already exists.", exception.getMessage());

        verify(householdRepository).existsByName(TEST_HOUSEHOLD_NAME);
        verifyNoInteractions(userRepository);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("createHousehold - should throw IllegalArgumentException when creator user is not found")
    void testCreateHousehold_CreatorNotFound() {
        HouseholdCreateRequestDTO requestDTO = new HouseholdCreateRequestDTO(TEST_HOUSEHOLD_NAME);

        when(householdRepository.existsByName(TEST_HOUSEHOLD_NAME)).thenReturn(false);
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.createHousehold(TEST_REQUESTER_ID, requestDTO));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " not found.", exception.getMessage());

        verify(householdRepository).existsByName(TEST_HOUSEHOLD_NAME);
        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("createHousehold - should throw IllegalStateException when creator already belongs to a household")
    void testCreateHousehold_CreatorAlreadyInHousehold() {
        HouseholdCreateRequestDTO requestDTO = new HouseholdCreateRequestDTO(TEST_HOUSEHOLD_NAME);
        User creator = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        creator.setHouseholdMembership(createMembership(testHousehold, creator, false));

        when(householdRepository.existsByName(TEST_HOUSEHOLD_NAME)).thenReturn(false);
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(creator));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
            () -> householdService.createHousehold(TEST_REQUESTER_ID, requestDTO));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " already belongs to a household.", exception.getMessage());

        verify(householdRepository).existsByName(TEST_HOUSEHOLD_NAME);
        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository, never()).save(any(Household.class));
    }

    // ============ Tests for getCurrentUserHousehold ============

    @Test
    @DisplayName("getCurrentUserHousehold - should return household for a member user")
    void testGetCurrentUserHousehold_Success() {
        when(householdRepository.findByMemberships_User_Id(TEST_REQUESTER_ID)).thenReturn(Optional.of(testHousehold));

        HouseholdResponseDTO response = householdService.getCurrentUserHousehold(TEST_REQUESTER_ID);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(TEST_HOUSEHOLD_ID, response.id());
        Assertions.assertEquals(TEST_HOUSEHOLD_NAME, response.name());
        Assertions.assertEquals(2, response.memberships().size());

        HouseholdMemberResponseDTO requesterMembershipDto = response.memberships().stream()
            .filter(member -> member.user().id().equals(TEST_REQUESTER_ID))
            .findFirst()
            .orElseThrow();
        Assertions.assertTrue(requesterMembershipDto.membership().isAdmin());
        Assertions.assertEquals(TEST_REQUESTER_MEMBERSHIP_DATE, requesterMembershipDto.membership().date());

        verify(householdRepository).findByMemberships_User_Id(TEST_REQUESTER_ID);
    }

    @Test
    @DisplayName("getCurrentUserHousehold - should throw IllegalStateException when user has no household")
    void testGetCurrentUserHousehold_UserWithoutHousehold() {
        when(householdRepository.findByMemberships_User_Id(TEST_REQUESTER_ID)).thenReturn(Optional.empty());

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
            () -> householdService.getCurrentUserHousehold(TEST_REQUESTER_ID));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " does not belong to any household.", exception.getMessage());

        verify(householdRepository).findByMemberships_User_Id(TEST_REQUESTER_ID);
    }

    // ============ Tests for getHouseholdMembers ============

    @Test
    @DisplayName("getHouseholdMembers - should return all household members with membership metadata")
    void testGetHouseholdMembers_Success() {
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(testRequester));
        when(householdMembershipRepository.findByHousehold(testHousehold))
            .thenReturn(List.of(requesterMembership, memberMembership));

        List<HouseholdMemberResponseDTO> response = householdService.getHouseholdMembers(TEST_REQUESTER_ID);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.size());

        HouseholdMemberResponseDTO requesterDto = response.stream()
            .filter(member -> member.user().id().equals(TEST_REQUESTER_ID))
            .findFirst()
            .orElseThrow();
        Assertions.assertTrue(requesterDto.membership().isAdmin());
        Assertions.assertEquals(TEST_REQUESTER_MEMBERSHIP_DATE, requesterDto.membership().date());

        HouseholdMemberResponseDTO memberDto = response.stream()
            .filter(member -> member.user().id().equals(TEST_MEMBER_ID))
            .findFirst()
            .orElseThrow();
        Assertions.assertFalse(memberDto.membership().isAdmin());
        Assertions.assertEquals(TEST_MEMBER_MEMBERSHIP_DATE, memberDto.membership().date());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdMembershipRepository).findByHousehold(testHousehold);
    }

    @Test
    @DisplayName("getHouseholdMembers - should throw IllegalArgumentException when requester is not found")
    void testGetHouseholdMembers_RequesterNotFound() {
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.getHouseholdMembers(TEST_REQUESTER_ID));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " not found.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdMembershipRepository, never()).findByHousehold(any(Household.class));
    }

    @Test
    @DisplayName("getHouseholdMembers - should throw IllegalStateException when requester has no household")
    void testGetHouseholdMembers_RequesterWithoutHousehold() {
        User requesterWithoutHousehold = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        requesterWithoutHousehold.setHouseholdMembership(null);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(requesterWithoutHousehold));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
            () -> householdService.getHouseholdMembers(TEST_REQUESTER_ID));

        Assertions.assertEquals("Requester user with ID: " + TEST_REQUESTER_ID + " does not belong to any household.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdMembershipRepository, never()).findByHousehold(any(Household.class));
    }

    // ============ Tests for getInvitationCode ============

    @Test
    @DisplayName("getInvitationCode - should return invitation code for household member")
    void testGetInvitationCode_Success() {
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(testRequester));

        HouseholdInvitationCodeResponseDTO response = householdService.getInvitationCode(TEST_REQUESTER_ID);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(TEST_INVITATION_CODE, response.invitationCode());
        Assertions.assertEquals(TEST_INVITATION_REFRESHED_AT, response.refreshedAt());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("getInvitationCode - should initialize and persist code when household code is missing")
    void testGetInvitationCode_InitializesMissingCode() {
        testHousehold.setInvitationCode(null);
        testHousehold.setInvitationCodeRefreshedAt(null);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(testRequester));
        when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HouseholdInvitationCodeResponseDTO response = householdService.getInvitationCode(TEST_REQUESTER_ID);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.invitationCode());
        Assertions.assertFalse(response.invitationCode().isBlank());
        Assertions.assertNotNull(response.refreshedAt());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    @DisplayName("getInvitationCode - should throw IllegalArgumentException when user is not found")
    void testGetInvitationCode_UserNotFound() {
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.getInvitationCode(TEST_REQUESTER_ID));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " not found.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("getInvitationCode - should throw IllegalStateException when user has no household")
    void testGetInvitationCode_UserWithoutHousehold() {
        User userWithoutHousehold = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        userWithoutHousehold.setHouseholdMembership(null);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(userWithoutHousehold));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
            () -> householdService.getInvitationCode(TEST_REQUESTER_ID));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " does not belong to any household.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository, never()).save(any(Household.class));
    }

    // ============ Tests for refreshInvitationCode ============

    @Test
    @DisplayName("refreshInvitationCode - should refresh invitation code when requester is admin")
    void testRefreshInvitationCode_Success() {
        String previousInvitationCode = testHousehold.getInvitationCode();

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(testRequester));
        when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HouseholdInvitationCodeResponseDTO response = householdService.refreshInvitationCode(TEST_REQUESTER_ID);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.invitationCode());
        Assertions.assertFalse(response.invitationCode().isBlank());
        Assertions.assertNotEquals(previousInvitationCode, response.invitationCode());
        Assertions.assertNotNull(response.refreshedAt());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    @DisplayName("refreshInvitationCode - should throw IllegalArgumentException when requester is not found")
    void testRefreshInvitationCode_RequesterNotFound() {
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.refreshInvitationCode(TEST_REQUESTER_ID));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " not found.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("refreshInvitationCode - should throw IllegalStateException when requester has no household")
    void testRefreshInvitationCode_RequesterWithoutHousehold() {
        User requester = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        requester.setHouseholdMembership(null);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(requester));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
            () -> householdService.refreshInvitationCode(TEST_REQUESTER_ID));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " does not belong to any household.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("refreshInvitationCode - should throw AccessDeniedException when requester is not admin")
    void testRefreshInvitationCode_RequesterNotAdmin() {
        User nonAdminRequester = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        HouseholdMembership nonAdminMembership = createMembership(testHousehold, nonAdminRequester, false);
        nonAdminRequester.setHouseholdMembership(nonAdminMembership);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(nonAdminRequester));

        AccessDeniedException exception = Assertions.assertThrows(AccessDeniedException.class,
            () -> householdService.refreshInvitationCode(TEST_REQUESTER_ID));

        Assertions.assertEquals("Only household admins can refresh invitation code.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository, never()).save(any(Household.class));
    }

    // ============ Tests for addMember ============

    @Test
    @DisplayName("addMember - should join household and return updated HouseholdResponseDTO when invitation code is valid")
    void testAddMember_Success() {
        AddMemberRequestDTO requestDTO = new AddMemberRequestDTO(TEST_INVITATION_CODE);

        User joiningUser = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        joiningUser.setHouseholdMembership(null);

        testHousehold.setMemberships(new ArrayList<>(List.of(memberMembership)));
        testMember.setHouseholdMembership(memberMembership);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(joiningUser));
        when(householdRepository.findByInvitationCode(TEST_INVITATION_CODE)).thenReturn(Optional.of(testHousehold));
        when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HouseholdResponseDTO response = householdService.addMember(TEST_REQUESTER_ID, requestDTO);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(TEST_HOUSEHOLD_ID, response.id());
        Assertions.assertEquals(2, response.memberships().size());
        Assertions.assertNotNull(joiningUser.getHouseholdMembership());
        Assertions.assertEquals(TEST_HOUSEHOLD_ID, joiningUser.getHouseholdMembership().getHousehold().getId());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository).findByInvitationCode(TEST_INVITATION_CODE);
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    @DisplayName("addMember - should throw IllegalStateException when requester already belongs to a household")
    void testAddMember_RequesterAlreadyInHousehold() {
        AddMemberRequestDTO requestDTO = new AddMemberRequestDTO(TEST_INVITATION_CODE);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(testRequester));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
            () -> householdService.addMember(TEST_REQUESTER_ID, requestDTO));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " already belongs to a household.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository, never()).findByInvitationCode(TEST_INVITATION_CODE);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("addMember - should throw IllegalArgumentException when invitation code is invalid")
    void testAddMember_InvalidInvitationCode() {
        AddMemberRequestDTO requestDTO = new AddMemberRequestDTO(TEST_INVITATION_CODE);

        User joiningUser = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        joiningUser.setHouseholdMembership(null);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(joiningUser));
        when(householdRepository.findByInvitationCode(TEST_INVITATION_CODE)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.addMember(TEST_REQUESTER_ID, requestDTO));

        Assertions.assertEquals("Invalid household invitation code.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository).findByInvitationCode(TEST_INVITATION_CODE);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("addMember - should throw IllegalArgumentException when requester is not found")
    void testAddMember_RequesterNotFound() {
        AddMemberRequestDTO requestDTO = new AddMemberRequestDTO(TEST_INVITATION_CODE);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.addMember(TEST_REQUESTER_ID, requestDTO));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " not found.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdRepository, never()).findByInvitationCode(TEST_INVITATION_CODE);
        verify(householdRepository, never()).save(any(Household.class));
    }

    // ============ Tests for removeMember ============

    @Test
    @DisplayName("removeMember - should remove member and return updated HouseholdResponseDTO when requester is admin")
    void testRemoveMember_Success() {
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(testRequester));
        when(userRepository.findById(TEST_MEMBER_ID)).thenReturn(Optional.of(testMember));
        when(householdMembershipRepository.findByHouseholdAndUser(eq(testHousehold), eq(testMember)))
            .thenReturn(Optional.of(memberMembership));
        when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HouseholdResponseDTO response = householdService.removeMember(TEST_REQUESTER_ID, TEST_MEMBER_ID);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(TEST_HOUSEHOLD_ID, response.id());
        Assertions.assertEquals(1, response.memberships().size());
        Assertions.assertNull(testMember.getHouseholdMembership());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(userRepository).findById(TEST_MEMBER_ID);
        verify(householdMembershipRepository).findByHouseholdAndUser(eq(testHousehold), eq(testMember));
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    @DisplayName("removeMember - should throw AccessDeniedException when requester is not admin")
    void testRemoveMember_RequesterNotAdmin() {
        User nonAdminRequester = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        HouseholdMembership nonAdminMembership = createMembership(testHousehold, nonAdminRequester, false);
        nonAdminRequester.setHouseholdMembership(nonAdminMembership);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(nonAdminRequester));

        AccessDeniedException exception = Assertions.assertThrows(AccessDeniedException.class,
            () -> householdService.removeMember(TEST_REQUESTER_ID, TEST_MEMBER_ID));

        Assertions.assertEquals("Only household admins can remove members.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(userRepository, never()).findById(TEST_MEMBER_ID);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("removeMember - should throw IllegalArgumentException when requester tries to remove themselves")
    void testRemoveMember_RequesterRemovesSelf() {
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(testRequester));

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.removeMember(TEST_REQUESTER_ID, TEST_REQUESTER_ID));

        Assertions.assertEquals("Admin cannot remove themselves. Use leave household operation instead.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(userRepository, never()).findById(TEST_MEMBER_ID);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("removeMember - should throw IllegalArgumentException when target user is not in requester's household")
    void testRemoveMember_TargetNotInHousehold() {
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(testRequester));
        when(userRepository.findById(TEST_MEMBER_ID)).thenReturn(Optional.of(testMember));
        when(householdMembershipRepository.findByHouseholdAndUser(eq(testHousehold), eq(testMember)))
            .thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.removeMember(TEST_REQUESTER_ID, TEST_MEMBER_ID));

        Assertions.assertEquals(
            "User with ID: " + TEST_MEMBER_ID + " is not a member of household with ID: " + TEST_HOUSEHOLD_ID,
            exception.getMessage()
        );

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(userRepository).findById(TEST_MEMBER_ID);
        verify(householdMembershipRepository).findByHouseholdAndUser(eq(testHousehold), eq(testMember));
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("removeMember - should throw IllegalStateException when trying to remove the only admin")
    void testRemoveMember_OnlyAdminCannotBeRemoved() {
        User adminToRemove = createTestUser(TEST_MEMBER_ID, "Luigi", "Verdi", TEST_MEMBER_EMAIL);
        HouseholdMembership adminMembershipToRemove = createMembership(testHousehold, adminToRemove, true);
        adminToRemove.setHouseholdMembership(adminMembershipToRemove);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(testRequester));
        when(userRepository.findById(TEST_MEMBER_ID)).thenReturn(Optional.of(adminToRemove));
        when(householdMembershipRepository.findByHouseholdAndUser(eq(testHousehold), eq(adminToRemove)))
            .thenReturn(Optional.of(adminMembershipToRemove));
        when(householdMembershipRepository.findByHouseholdAndIsAdminTrue(testHousehold))
            .thenReturn(List.of(adminMembershipToRemove));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
            () -> householdService.removeMember(TEST_REQUESTER_ID, TEST_MEMBER_ID));

        Assertions.assertEquals("Cannot remove the only admin from the household.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(userRepository).findById(TEST_MEMBER_ID);
        verify(householdMembershipRepository).findByHouseholdAndUser(eq(testHousehold), eq(adminToRemove));
        verify(householdMembershipRepository).findByHouseholdAndIsAdminTrue(testHousehold);
        verify(householdRepository, never()).save(any(Household.class));
    }

    // ============ Tests for leaveHousehold ============

    @Test
    @DisplayName("leaveHousehold - should remove non-admin member and keep household")
    void testLeaveHousehold_NonAdminSuccess() {
        when(userRepository.findById(TEST_MEMBER_ID)).thenReturn(Optional.of(testMember));
        when(householdMembershipRepository.findByHousehold(testHousehold))
            .thenReturn(new ArrayList<>(List.of(requesterMembership, memberMembership)));
        when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> invocation.getArgument(0));

        householdService.leaveHousehold(TEST_MEMBER_ID);

        Assertions.assertNull(testMember.getHouseholdMembership());
        Assertions.assertEquals(1, testHousehold.getMemberships().size());
        Assertions.assertEquals(TEST_REQUESTER_ID, testHousehold.getMemberships().get(0).getUser().getId());

        verify(userRepository).findById(TEST_MEMBER_ID);
        verify(householdMembershipRepository).findByHousehold(testHousehold);
        verify(householdMembershipRepository, never()).findByHouseholdAndIsAdminTrue(testHousehold);
        verify(householdRepository).save(any(Household.class));
        verify(householdRepository, never()).delete(testHousehold);
    }

    @Test
    @DisplayName("leaveHousehold - should transfer admin role when last admin leaves and members remain")
    void testLeaveHousehold_AdminTransfersRole() {
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(testRequester));
        when(householdMembershipRepository.findByHousehold(testHousehold))
            .thenReturn(new ArrayList<>(List.of(requesterMembership, memberMembership)));
        when(householdMembershipRepository.findByHouseholdAndIsAdminTrue(testHousehold))
            .thenReturn(List.of(requesterMembership));
        when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> invocation.getArgument(0));

        householdService.leaveHousehold(TEST_REQUESTER_ID);

        Assertions.assertNull(testRequester.getHouseholdMembership());
        Assertions.assertTrue(memberMembership.isAdmin());
        Assertions.assertEquals(1, testHousehold.getMemberships().size());
        Assertions.assertEquals(TEST_MEMBER_ID, testHousehold.getMemberships().get(0).getUser().getId());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdMembershipRepository).findByHousehold(testHousehold);
        verify(householdMembershipRepository).findByHouseholdAndIsAdminTrue(testHousehold);
        verify(householdRepository).save(any(Household.class));
        verify(householdRepository, never()).delete(testHousehold);
    }

    @Test
    @DisplayName("leaveHousehold - should delete household when last member leaves")
    void testLeaveHousehold_LastMemberDeletesHousehold() {
        User onlyMember = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        Household singleMemberHousehold = createTestHousehold(TEST_HOUSEHOLD_ID, TEST_HOUSEHOLD_NAME);
        HouseholdMembership onlyMembership = createMembership(singleMemberHousehold, onlyMember, true);

        singleMemberHousehold.setMemberships(new ArrayList<>(List.of(onlyMembership)));
        onlyMember.setHouseholdMembership(onlyMembership);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(onlyMember));
        when(householdMembershipRepository.findByHousehold(singleMemberHousehold))
            .thenReturn(new ArrayList<>(List.of(onlyMembership)));
        when(householdMembershipRepository.findByHouseholdAndIsAdminTrue(singleMemberHousehold))
            .thenReturn(List.of(onlyMembership));

        householdService.leaveHousehold(TEST_REQUESTER_ID);

        Assertions.assertNull(onlyMember.getHouseholdMembership());
        Assertions.assertTrue(singleMemberHousehold.getMemberships().isEmpty());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verify(householdMembershipRepository).findByHousehold(singleMemberHousehold);
        verify(householdMembershipRepository).findByHouseholdAndIsAdminTrue(singleMemberHousehold);
        verify(householdRepository).delete(singleMemberHousehold);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    @DisplayName("leaveHousehold - should throw IllegalArgumentException when user is not found")
    void testLeaveHousehold_UserNotFound() {
        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> householdService.leaveHousehold(TEST_REQUESTER_ID));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " not found.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verifyNoInteractions(householdMembershipRepository, householdRepository);
    }

    @Test
    @DisplayName("leaveHousehold - should throw IllegalStateException when user does not belong to a household")
    void testLeaveHousehold_UserWithoutHousehold() {
        User userWithoutHousehold = createTestUser(TEST_REQUESTER_ID, "Mario", "Rossi", TEST_REQUESTER_EMAIL);
        userWithoutHousehold.setHouseholdMembership(null);

        when(userRepository.findById(TEST_REQUESTER_ID)).thenReturn(Optional.of(userWithoutHousehold));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
            () -> householdService.leaveHousehold(TEST_REQUESTER_ID));

        Assertions.assertEquals("User with ID: " + TEST_REQUESTER_ID + " does not belong to any household.", exception.getMessage());

        verify(userRepository).findById(TEST_REQUESTER_ID);
        verifyNoInteractions(householdMembershipRepository, householdRepository);
    }
}
