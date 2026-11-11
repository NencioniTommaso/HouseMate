import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/chore_service.dart';
import 'package:mobile/services/household_service.dart';
import 'package:mobile/services/shopping_list_service.dart';
import 'package:mobile/shared/dto/chore/response/chore_response_dto.dart';
import 'package:mobile/shared/dto/household/request/add_member_request_dto.dart';
import 'package:mobile/shared/dto/household/request/household_create_request_dto.dart';
import 'package:mobile/shared/dto/household/response/household_invitation_code_response_dto.dart';
import 'package:mobile/shared/dto/household/response/household_member_response_dto.dart';
import 'package:mobile/shared/dto/household/response/household_membership_response_dto.dart';
import 'package:mobile/shared/dto/household/response/household_response_dto.dart';
import 'package:mobile/shared/dto/items/request/shopping_list_create_request_dto.dart';
import 'package:mobile/shared/dto/items/request/shopping_list_update_request_dto.dart';
import 'package:mobile/shared/dto/items/response/shopping_list_response_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';
import 'package:mobile/shared/enums/shopping_list_status.dart';
import 'package:mobile/state/household_provider.dart';

import '../services/test_helpers.dart';
import 'state_test_helpers.dart';

class FakeHouseholdService extends HouseholdService {
  FakeHouseholdService(super.apiClient);

  HouseholdResponseDTO? mockHousehold;
  HouseholdInvitationCodeResponseDTO mockInviteCode = HouseholdInvitationCodeResponseDTO(
    invitationCode: 'INV-123',
    refreshedAt: DateTime.now(),
  );
  Exception? errorToThrow;
  int? errorStatusCode;

  HouseholdCreateRequestDTO? lastCreateRequest;
  AddMemberRequestDTO? lastAddMemberRequest;
  String? lastRemovedMemberId;

  @override
  Future<HouseholdResponseDTO> getCurrentUserHousehold() async {
    if (errorToThrow != null) throw errorToThrow!;
    if (errorStatusCode != null) {
      throw ApiException(message: 'Household error', statusCode: errorStatusCode);
    }
    if (mockHousehold == null) {
      throw ApiException(message: 'Household not found', statusCode: 404);
    }
    return mockHousehold!;
  }

  @override
  Future<HouseholdResponseDTO> createHousehold(HouseholdCreateRequestDTO request) async {
    lastCreateRequest = request;
    if (errorToThrow != null) throw errorToThrow!;
    if (errorStatusCode != null) {
      throw ApiException(message: 'Create error', statusCode: errorStatusCode);
    }
    return mockHousehold ??
        HouseholdResponseDTO(
          id: 'h-created',
          name: request.name,
          creationDate: DateTime.now(),
          memberships: [],
        );
  }

  @override
  Future<HouseholdResponseDTO> addMember(AddMemberRequestDTO request) async {
    lastAddMemberRequest = request;
    if (errorToThrow != null) throw errorToThrow!;
    if (errorStatusCode != null) {
      throw ApiException(message: 'Join error', statusCode: errorStatusCode);
    }
    return mockHousehold ??
        HouseholdResponseDTO(
          id: 'h-joined',
          name: 'Joined Household',
          creationDate: DateTime.now(),
          memberships: [],
        );
  }

  @override
  Future<HouseholdInvitationCodeResponseDTO> getInvitationCode() async {
    if (errorToThrow != null) throw errorToThrow!;
    if (errorStatusCode != null) {
      throw ApiException(message: 'Invite error', statusCode: errorStatusCode);
    }
    return mockInviteCode;
  }

  @override
  Future<HouseholdInvitationCodeResponseDTO> refreshInvitationCode() async {
    if (errorToThrow != null) throw errorToThrow!;
    if (errorStatusCode != null) {
      throw ApiException(message: 'Refresh error', statusCode: errorStatusCode);
    }
    return mockInviteCode;
  }

  @override
  Future<void> leaveHousehold() async {
    if (errorToThrow != null) throw errorToThrow!;
    if (errorStatusCode != null) {
      throw ApiException(message: 'Leave error', statusCode: errorStatusCode);
    }
  }

  @override
  Future<HouseholdResponseDTO> removeMember(String memberId) async {
    lastRemovedMemberId = memberId;
    if (errorToThrow != null) throw errorToThrow!;
    if (errorStatusCode != null) {
      throw ApiException(message: 'Remove error', statusCode: errorStatusCode);
    }
    return mockHousehold ??
        HouseholdResponseDTO(
          id: 'h-100',
          name: 'Dream House',
          creationDate: DateTime.now(),
          memberships: [],
        );
  }
}

class FakeShoppingListService extends ShoppingListService {
  FakeShoppingListService(super.apiClient);

  List<ShoppingListResponseDTO> mockLists = [];
  Exception? errorToThrow;
  ShoppingListCreateRequestDTO? lastCreateRequest;
  ShoppingListUpdateRequestDTO? lastUpdateRequest;

  @override
  Future<List<ShoppingListResponseDTO>> getShoppingItemsByHousehold() async {
    if (errorToThrow != null) throw errorToThrow!;
    return mockLists;
  }

  @override
  Future<ShoppingListResponseDTO> createShoppingList(ShoppingListCreateRequestDTO request) async {
    lastCreateRequest = request;
    if (errorToThrow != null) throw errorToThrow!;
    return ShoppingListResponseDTO(
      id: 'sl-1',
      name: request.name,
      status: ShoppingListStatus.notStarted,
      creationDate: request.creationDate,
      items: request.items,
      householdId: request.householdId,
    );
  }

  @override
  Future<ShoppingListResponseDTO> updateListInformation(
      String listId, ShoppingListUpdateRequestDTO request) async {
    lastUpdateRequest = request;
    if (errorToThrow != null) throw errorToThrow!;
    return ShoppingListResponseDTO(
      id: listId,
      name: 'Updated List',
      status: ShoppingListStatus.inProgress,
      creationDate: DateTime.now(),
      items: [],
      householdId: 'h-1',
    );
  }
}

class FakeChoreService extends ChoreService {
  FakeChoreService(super.apiClient);

  List<ChoreResponseDTO> mockChores = [];
  Exception? errorToThrow;

  @override
  Future<List<ChoreResponseDTO>> getAllHouseholdChores() async {
    if (errorToThrow != null) throw errorToThrow!;
    return mockChores;
  }
}

void main() {
  late TestContext testContext;
  late FakeHouseholdService fakeHouseholdService;
  late FakeShoppingListService fakeShoppingListService;
  late FakeChoreService fakeChoreService;
  late FakeUiService fakeUiService;
  late HouseholdProvider provider;

  final sampleMember = HouseholdMemberResponseDTO(
    user: UserResponseDTO(
      id: 'u-1',
      name: 'Admin',
      surname: 'User',
      email: 'admin@test.com',
    ),
    membership: HouseholdMembershipResponseDTO(
      isAdmin: true,
      date: DateTime(2026, 1, 1),
    ),
  );

  final sampleHousehold = HouseholdResponseDTO(
    id: 'h-100',
    name: 'Dream House',
    creationDate: DateTime(2026, 1, 1),
    memberships: [sampleMember],
  );

  setUp(() {
    testContext = createTestContext();
    fakeHouseholdService = FakeHouseholdService(testContext.apiClient);
    fakeShoppingListService = FakeShoppingListService(testContext.apiClient);
    fakeChoreService = FakeChoreService(testContext.apiClient);
    fakeUiService = FakeUiService();

    provider = HouseholdProvider(
      apiClient: testContext.apiClient,
      uiService: fakeUiService,
      householdService: fakeHouseholdService,
      shoppingListService: fakeShoppingListService,
      choreService: fakeChoreService,
    );
  });

  group('HouseholdProvider - Initial State & Getters', () {
    test('initial state values are null/empty and loading is false', () {
      expect(provider.currentHousehold, isNull);
      expect(provider.invitationCode, isNull);
      expect(provider.shoppingLists, isEmpty);
      expect(provider.householdChores, isEmpty);
      expect(provider.isLoading, isFalse);
      expect(provider.errorMessage, isNull);
      expect(provider.hasHousehold, isFalse);
    });

    test('isAdmin returns true for admin member and false for non-admin/null', () async {
      expect(provider.isAdmin('u-1'), isFalse);

      fakeHouseholdService.mockHousehold = sampleHousehold;
      await provider.loadHouseholdData();

      expect(provider.isAdmin('u-1'), isTrue);
      expect(provider.isAdmin('u-non-existent'), isFalse);
    });
  });

  group('HouseholdProvider - loadHouseholdData', () {
    test('successfully loads household and updates state', () async {
      fakeHouseholdService.mockHousehold = sampleHousehold;

      await provider.loadHouseholdData();

      expect(provider.currentHousehold, equals(sampleHousehold));
      expect(provider.hasHousehold, isTrue);
      expect(provider.errorMessage, isNull);
      expect(provider.isLoading, isFalse);
    });

    test('handles 404 by setting currentHousehold to null without setting errorMessage', () async {
      fakeHouseholdService.errorStatusCode = 404;

      await provider.loadHouseholdData();

      expect(provider.currentHousehold, isNull);
      expect(provider.hasHousehold, isFalse);
      expect(provider.errorMessage, isNull);
      expect(fakeUiService.errorMessages, isEmpty);
    });

    test('handles ApiException non-404 by showing error and updating errorMessage', () async {
      fakeHouseholdService.errorStatusCode = 500;

      await provider.loadHouseholdData();

      expect(provider.errorMessage, 'Household error');
      expect(fakeUiService.errorMessages, contains('Household error'));
    });

    test('handles generic unexpected exception', () async {
      fakeHouseholdService.errorToThrow = Exception('Network crash');

      await provider.loadHouseholdData();

      expect(provider.errorMessage, contains('unexpected error occurred'));
      expect(fakeUiService.errorMessages, isNotEmpty);
    });
  });

  group('HouseholdProvider - loadShoppingLists & loadHouseholdChores', () {
    test('loadShoppingLists successfully updates lists', () async {
      fakeShoppingListService.mockLists = [
        ShoppingListResponseDTO(
          id: 'sl-1',
          name: 'Groceries',
          status: ShoppingListStatus.notStarted,
          creationDate: DateTime.now(),
          items: [],
          householdId: 'h-100',
        ),
      ];

      await provider.loadShoppingLists();

      expect(provider.shoppingLists.length, 1);
      expect(provider.shoppingLists.first.name, 'Groceries');
    });

    test('loadShoppingLists handles ApiException', () async {
      fakeShoppingListService.errorToThrow = ApiException(message: 'Lists unavailable');

      await provider.loadShoppingLists();

      expect(provider.errorMessage, 'Lists unavailable');
      expect(fakeUiService.errorMessages, contains('Lists unavailable'));
    });

    test('loadHouseholdChores successfully updates chores', () async {
      fakeChoreService.mockChores = [
        ChoreResponseDTO(id: 'c-1', description: 'Clean kitchen', frequencyDays: 3),
      ];

      await provider.loadHouseholdChores();

      expect(provider.householdChores.length, 1);
      expect(provider.householdChores.first.description, 'Clean kitchen');
    });

    test('loadHouseholdChores handles generic exception', () async {
      fakeChoreService.errorToThrow = Exception('DB error');

      await provider.loadHouseholdChores();

      expect(provider.errorMessage, contains('unexpected error occurred while fetching chores'));
      expect(fakeUiService.errorMessages, isNotEmpty);
    });
  });

  group('HouseholdProvider - refreshAll', () {
    test('refreshes household, shopping lists, and chores concurrently', () async {
      fakeHouseholdService.mockHousehold = sampleHousehold;
      fakeShoppingListService.mockLists = [
        ShoppingListResponseDTO(
          id: 'sl-1',
          name: 'Weekly',
          status: ShoppingListStatus.inProgress,
          creationDate: DateTime.now(),
          items: [],
          householdId: 'h-100',
        ),
      ];
      fakeChoreService.mockChores = [
        ChoreResponseDTO(id: 'c-1', description: 'Vacuum', frequencyDays: 7),
      ];

      await provider.refreshAll();

      expect(provider.currentHousehold?.name, 'Dream House');
      expect(provider.shoppingLists.length, 1);
      expect(provider.householdChores.length, 1);
      expect(provider.isLoading, isFalse);
    });
  });

  group('HouseholdProvider - getInviteCode & refreshInvitationCode', () {
    test('getInviteCode populates invitationCode on success', () async {
      await provider.getInviteCode();

      expect(provider.invitationCode?.invitationCode, 'INV-123');
      expect(provider.errorMessage, isNull);
    });

    test('getInviteCode handles ApiException', () async {
      fakeHouseholdService.errorStatusCode = 403;

      await provider.getInviteCode();

      expect(provider.errorMessage, 'Invite error');
      expect(fakeUiService.errorMessages, contains('Invite error'));
    });

    test('refreshInvitationCode populates invitationCode on success', () async {
      await provider.refreshInvitationCode();

      expect(provider.invitationCode?.invitationCode, 'INV-123');
    });

    test('refreshInvitationCode handles generic error', () async {
      fakeHouseholdService.errorToThrow = Exception('Fail');

      await provider.refreshInvitationCode();

      expect(provider.errorMessage, contains('unexpected error occurred while refreshing invitation code'));
    });
  });

  group('HouseholdProvider - Mutation methods', () {
    test('createHousehold succeeds and refreshes data', () async {
      fakeHouseholdService.mockHousehold = sampleHousehold;

      final success = await provider.createHousehold('New Mansion');

      expect(success, isTrue);
      expect(fakeHouseholdService.lastCreateRequest?.name, 'New Mansion');
      expect(fakeUiService.successMessages, contains('Household created successfully'));
    });

    test('createHousehold handles error and returns false', () async {
      fakeHouseholdService.errorStatusCode = 400;

      final success = await provider.createHousehold('Invalid');

      expect(success, isFalse);
      expect(fakeUiService.errorMessages, isNotEmpty);
    });

    test('joinHousehold succeeds and returns true', () async {
      final success = await provider.joinHousehold('JOIN-CODE');

      expect(success, isTrue);
      expect(fakeHouseholdService.lastAddMemberRequest?.invitationCode, 'JOIN-CODE');
      expect(fakeUiService.successMessages, contains('Joined household successfully'));
    });

    test('joinHousehold handles ApiException and returns false', () async {
      fakeHouseholdService.errorStatusCode = 404;

      final success = await provider.joinHousehold('INVALID');

      expect(success, isFalse);
      expect(fakeUiService.errorMessages, contains('Join error'));
    });

    test('leaveHousehold clears household and shopping lists on success', () async {
      fakeHouseholdService.mockHousehold = sampleHousehold;
      await provider.loadHouseholdData();

      final success = await provider.leaveHousehold();

      expect(success, isTrue);
      expect(provider.currentHousehold, isNull);
      expect(provider.shoppingLists, isEmpty);
      expect(fakeUiService.successMessages, contains('Successfully left the household'));
    });

    test('leaveHousehold handles exception and returns false', () async {
      fakeHouseholdService.errorToThrow = Exception('Cannot leave');

      final success = await provider.leaveHousehold();

      expect(success, isFalse);
      expect(fakeUiService.errorMessages, isNotEmpty);
    });

    test('createShoppingList creates item, shows success, and returns true', () async {
      final success = await provider.createShoppingList('Party', ['Chips', 'Soda']);

      expect(success, isTrue);
      expect(fakeShoppingListService.lastCreateRequest?.name, 'Party');
      expect(fakeShoppingListService.lastCreateRequest?.items.length, 2);
      expect(fakeUiService.successMessages, contains('Shopping list created successfully'));
    });

    test('updateShoppingList updates item, shows success, and returns true', () async {
      final success = await provider.updateShoppingList('sl-1', [true, false]);

      expect(success, isTrue);
      expect(fakeShoppingListService.lastUpdateRequest?.boughtItems, [true, false]);
      expect(fakeUiService.successMessages, contains('Shopping list updated successfully'));
    });

    test('removeMember removes member, reloads household data, and returns true', () async {
      final success = await provider.removeMember('u-2');

      expect(success, isTrue);
      expect(fakeHouseholdService.lastRemovedMemberId, 'u-2');
      expect(fakeUiService.successMessages, contains('Member removed successfully'));
    });

    test('clear resets all state variables and notifies listeners', () async {
      fakeHouseholdService.mockHousehold = sampleHousehold;
      await provider.loadHouseholdData();

      provider.clear();

      expect(provider.currentHousehold, isNull);
      expect(provider.invitationCode, isNull);
      expect(provider.shoppingLists, isEmpty);
      expect(provider.householdChores, isEmpty);
      expect(provider.isLoading, isFalse);
      expect(provider.errorMessage, isNull);
    });
  });
}
