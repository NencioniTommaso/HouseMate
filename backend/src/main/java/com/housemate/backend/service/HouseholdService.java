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
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final HouseholdMembershipRepository householdMembershipRepository;
    private final UserRepository userRepository;

    @Transactional
    public HouseholdResponseDTO createHousehold(@NonNull UUID creatorUserId, @NonNull HouseholdCreateRequestDTO dto) {
        Assert.notNull(creatorUserId, "Creator user ID cannot be null");
        Assert.notNull(dto, "No request body was sent");
        Assert.notNull(dto.name(), "Household name cannot be null");
        Assert.isTrue(!dto.name().isBlank(), "Household name cannot be blank");

        log.info("Requested creation of new household '{}' by user {}", dto.name(), creatorUserId);

        if (householdRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Household with name: " + dto.name() + " already exists.");
        }

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + creatorUserId + " not found."));

        if (creator.getHouseholdMembership() != null) {
            throw new IllegalStateException("User with ID: " + creatorUserId + " already belongs to a household.");
        }

        Household household = new Household();
        household.setName(dto.name());
        household.setMemberships(new ArrayList<>());
        household.refreshInvitationCode(householdRepository::existsByInvitationCode);

        HouseholdMembership membership = new HouseholdMembership(household, creator, true);
        household.getMemberships().add(membership);
        creator.setHouseholdMembership(membership);

        Household saved = householdRepository.save(household);
        log.info("Household created successfully! Id: {}", saved.getId());

        return toHouseholdResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public HouseholdResponseDTO getCurrentUserHousehold(@NonNull UUID userId) {
        Assert.notNull(userId, "User ID cannot be null");

        log.info("Requested retrieval of current household for user {}", userId);

        Household household = householdRepository.findByMemberships_User_Id(userId)
                .orElseThrow(() -> new IllegalStateException("User with ID: " + userId + " does not belong to any household."));

        log.info("Retrieved current household {} for user {}", household.getId(), userId);
        return toHouseholdResponseDTO(household);
    }

    @Transactional
    public HouseholdResponseDTO addMember(@NonNull UUID requesterUserId, @NonNull AddMemberRequestDTO dto) {
        Assert.notNull(requesterUserId, "Requester user ID cannot be null");
        Assert.notNull(dto, "No request body was sent");
        Assert.notNull(dto.invitationCode(), "Invitation code cannot be null");
        Assert.isTrue(!dto.invitationCode().isBlank(), "Invitation code cannot be blank");

        log.info("Requested household join by user {} via invitation code", requesterUserId);

        User joiningUser = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + requesterUserId + " not found."));

        if (joiningUser.getHouseholdMembership() != null) {
            throw new IllegalStateException("User with ID: " + requesterUserId + " already belongs to a household.");
        }

        Household household = householdRepository.findByInvitationCode(dto.invitationCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid household invitation code."));

        if (household.getMemberships() == null) {
            household.setMemberships(new ArrayList<>());
        }

        HouseholdMembership newMembership = new HouseholdMembership(household, joiningUser, false);
        household.getMemberships().add(newMembership);
        joiningUser.setHouseholdMembership(newMembership);

        Household savedHousehold = householdRepository.save(household);
        log.info("User {} joined household {} via invitation code", joiningUser.getId(), household.getId());

        return toHouseholdResponseDTO(savedHousehold);
    }

    @Transactional
    public HouseholdInvitationCodeResponseDTO getInvitationCode(@NonNull UUID userId) {
        Assert.notNull(userId, "User ID cannot be null");

        log.info("Requested invitation code retrieval by user {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found."));

        HouseholdMembership membership = user.getHouseholdMembership();
        if (membership == null || membership.getHousehold() == null) {
            throw new IllegalStateException("User with ID: " + userId + " does not belong to any household.");
        }

        Household household = membership.getHousehold();

        if (household.getInvitationCode() == null
                || household.getInvitationCode().isBlank()
                || household.getInvitationCodeRefreshedAt() == null) {
            household.refreshInvitationCode(householdRepository::existsByInvitationCode);
            household = householdRepository.save(household);
        }

        return toInvitationCodeResponseDTO(household);
    }

    @Transactional
    public HouseholdInvitationCodeResponseDTO refreshInvitationCode(@NonNull UUID requesterUserId) {
        Assert.notNull(requesterUserId, "Requester user ID cannot be null");

        log.info("Requested invitation code refresh by user {}", requesterUserId);

        User requester = userRepository.findById(requesterUserId)
            .orElseThrow(() -> new IllegalArgumentException("User with ID: " + requesterUserId + " not found."));

        HouseholdMembership requesterMembership = requester.getHouseholdMembership();
        if (requesterMembership == null || requesterMembership.getHousehold() == null) {
            throw new IllegalStateException("User with ID: " + requesterUserId + " does not belong to any household.");
        }

        if (!requesterMembership.isAdmin()) {
            throw new AccessDeniedException("Only household admins can refresh invitation code.");
        }

        Household household = requesterMembership.getHousehold();
        household.refreshInvitationCode(householdRepository::existsByInvitationCode);

        Household savedHousehold = householdRepository.save(household);

        log.info("Invitation code refreshed for household {} by admin {}", savedHousehold.getId(), requesterUserId);
        return toInvitationCodeResponseDTO(savedHousehold);
    }

    @Transactional
    public HouseholdResponseDTO removeMember(@NonNull UUID requesterUserId, @NonNull UUID memberId) {
        Assert.notNull(requesterUserId, "Requester user ID cannot be null");
        Assert.notNull(memberId, "Member user ID cannot be null");

        log.info("Requested removal of member {} by user {}", memberId, requesterUserId);

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + requesterUserId + " not found."));

        HouseholdMembership requesterMembership = requester.getHouseholdMembership();
        if (requesterMembership == null || requesterMembership.getHousehold() == null) {
            throw new IllegalStateException("User with ID: " + requesterUserId + " does not belong to any household.");
        }

        if (!requesterMembership.isAdmin()) {
            throw new AccessDeniedException("Only household admins can remove members.");
        }

        if (requesterUserId.equals(memberId)) {
            throw new IllegalArgumentException("Admin cannot remove themselves. Use leave household operation instead.");
        }

        Household household = requesterMembership.getHousehold();

        User memberToRemove = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + memberId + " not found."));

        HouseholdMembership membershipToRemove = householdMembershipRepository.findByHouseholdAndUser(household, memberToRemove)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + memberId + " is not a member of household with ID: " + household.getId()));

        if (membershipToRemove.isAdmin()) {
            List<HouseholdMembership> admins = householdMembershipRepository.findByHouseholdAndIsAdminTrue(household);
            if (admins.size() <= 1) {
                throw new IllegalStateException("Cannot remove the only admin from the household.");
            }
        }

        household.getMemberships().removeIf(membership -> Objects.equals(membership.getUser().getId(), memberId));
        memberToRemove.setHouseholdMembership(null);

        Household savedHousehold = householdRepository.save(household);
        log.info("Member {} removed from household {} by admin {}", memberId, household.getId(), requesterUserId);

        return toHouseholdResponseDTO(savedHousehold);
    }

    @Transactional
    public void leaveHousehold(@NonNull UUID userId) {
        Assert.notNull(userId, "User ID cannot be null");

        log.info("Requested household leave by user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found."));

        HouseholdMembership membership = user.getHouseholdMembership();
        if (membership == null || membership.getHousehold() == null) {
            throw new IllegalStateException("User with ID: " + userId + " does not belong to any household.");
        }

        Household household = membership.getHousehold();
        List<HouseholdMembership> householdMemberships = householdMembershipRepository.findByHousehold(household);

        if (membership.isAdmin()) {
            List<HouseholdMembership> admins = householdMembershipRepository.findByHouseholdAndIsAdminTrue(household);
            if (admins.size() == 1 && householdMemberships.size() > 1) {
                HouseholdMembership newAdmin = householdMemberships.stream()
                        .filter(householdMembership -> !householdMembership.getUser().getId().equals(userId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No eligible member found for admin role transfer."));

                newAdmin.setAdmin(true);
                log.info("Transferred admin role in household {} to user {}", household.getId(), newAdmin.getUser().getId());
            }
        }

        household.getMemberships().removeIf(householdMembership -> householdMembership.getUser().getId().equals(userId));
        user.setHouseholdMembership(null);

        if (household.getMemberships().isEmpty()) {
            householdRepository.delete(household);
            log.info("Household {} deleted because last member {} left", household.getId(), userId);
            return;
        }

        householdRepository.save(household);
        log.info("User {} left household {}", userId, household.getId());
    }

    private HouseholdResponseDTO toHouseholdResponseDTO(@NonNull Household household) {
        Assert.notNull(household, "Household cannot be null");

        List<HouseholdMembership> memberships =
                household.getMemberships() == null
                ? Collections.emptyList()
                : household.getMemberships();

        List<UserResponseDTO> memberDTOs = memberships.stream()
                .map(m -> {
                    User u = m.getUser();
                    return new UserResponseDTO(u.getId(), u.getName(), u.getSurname(), u.getEmail(), u.getIban());
                })
                .toList();

        return new HouseholdResponseDTO(household.getId(), household.getName(), household.getDate(), memberDTOs);
    }

    private HouseholdInvitationCodeResponseDTO toInvitationCodeResponseDTO(@NonNull Household household) {
        Assert.notNull(household, "Household cannot be null");
        Assert.hasText(household.getInvitationCode(), "Household invitation code cannot be blank");
        Assert.notNull(household.getInvitationCodeRefreshedAt(), "Invitation code refreshed timestamp cannot be null");

        return new HouseholdInvitationCodeResponseDTO(
            household.getInvitationCode(),
            household.getInvitationCodeRefreshedAt()
        );
    }
}
