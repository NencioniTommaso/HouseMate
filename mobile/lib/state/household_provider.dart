import 'package:flutter/material.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../services/household_service.dart';
import '../services/shopping_list_service.dart';
import '../services/chore_service.dart';
import '../shared/dto/household/request/add_member_request_dto.dart';
import '../shared/dto/household/request/household_create_request_dto.dart';
import '../shared/dto/items/request/shopping_list_create_request_dto.dart';
import '../shared/utils/types/list_item.dart';
import '../shared/dto/items/request/shopping_list_update_request_dto.dart';
import '../shared/dto/household/response/household_invitation_code_response_dto.dart';
import '../shared/dto/household/response/household_response_dto.dart';
import '../shared/dto/items/response/shopping_list_response_dto.dart';
import '../shared/dto/chore/response/chore_response_dto.dart';
import '../core/utils/ui_service.dart';
import 'package:collection/collection.dart';

class HouseholdProvider extends ChangeNotifier {
  final HouseholdService _householdService;
  final ShoppingListService _shoppingListService;
  final ChoreService _choreService;
  final UiService _uiService;

  HouseholdResponseDTO? _currentHousehold;
  HouseholdInvitationCodeResponseDTO? _invitationCode;
  List<ShoppingListResponseDTO> _shoppingLists = [];
  List<ChoreResponseDTO> _householdChores = [];
  bool _isLoading = false;
  String? _errorMessage;

  HouseholdProvider({required ApiClient apiClient, required UiService uiService})
      : _householdService = HouseholdService(apiClient),
        _shoppingListService = ShoppingListService(apiClient),
        _choreService = ChoreService(apiClient),
        _uiService = uiService;

  HouseholdResponseDTO? get currentHousehold => _currentHousehold;
  HouseholdInvitationCodeResponseDTO? get invitationCode => _invitationCode;
  List<ShoppingListResponseDTO> get shoppingLists => _shoppingLists;
  List<ChoreResponseDTO> get householdChores => _householdChores;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  bool get hasHousehold => _currentHousehold != null;

  bool isAdmin(String userId) {
    if (_currentHousehold == null) return false;
    final membership = _currentHousehold!.memberships.firstWhereOrNull(
      (m) => m.user.id == userId,
    );
    return membership?.membership.isAdmin ?? false;
  }

  Future<void> loadHouseholdData() async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      _currentHousehold = await _householdService.getCurrentUserHousehold();
    } on ApiException catch (e) {
      if (e.statusCode == 404) {
        _currentHousehold = null;
      } else {
        _errorMessage = e.message;
        _uiService.showError(e.message);
      }
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching household data.";
      _uiService.showError(_errorMessage!);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadShoppingLists() async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      _shoppingLists = await _shoppingListService.getShoppingItemsByHousehold();
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching shopping lists.";
      _uiService.showError(_errorMessage!);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadHouseholdChores() async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      _householdChores = await _choreService.getAllHouseholdChores();
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching chores.";
      _uiService.showError(_errorMessage!);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> refreshAll() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      await Future.wait([
        loadHouseholdData(),
        loadShoppingLists(),
        loadHouseholdChores(),
      ]);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> getInviteCode() async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      _invitationCode = await _householdService.getInvitationCode();
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching invitation code.";
      _uiService.showError(_errorMessage!);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> refreshInvitationCode() async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      _invitationCode = await _householdService.refreshInvitationCode();
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
    } catch (e) {
      _errorMessage = "An unexpected error occurred while refreshing invitation code.";
      _uiService.showError(_errorMessage!);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> createHousehold(String name) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      final request = HouseholdCreateRequestDTO(name: name);
      _currentHousehold = await _householdService.createHousehold(request);
      _uiService.showSuccess("Household created successfully");
      await refreshAll(); // Fetch all new data for the fresh household
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while creating household.";
      _uiService.showError(_errorMessage!);
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> joinHousehold(String code) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      final request = AddMemberRequestDTO(invitationCode: code);
      _currentHousehold = await _householdService.addMember(request);
      _uiService.showSuccess("Joined household successfully");
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while joining household.";
      _uiService.showError(_errorMessage!);
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> leaveHousehold() async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      await _householdService.leaveHousehold();
      _currentHousehold = null;
      _shoppingLists = [];
      _uiService.showSuccess("Successfully left the household");
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while leaving household.";
      _uiService.showError(_errorMessage!);
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> createShoppingList(String name, List<String> items) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      final request = ShoppingListCreateRequestDTO(
        name: name,
        items: items.map((itemName) => ListItem(itemName: itemName)).toList(),
        householdId: _currentHousehold?.id ?? "",
        creationDate: DateTime.now(),
      );
      await _shoppingListService.createShoppingList(request);
      _uiService.showSuccess("Shopping list created successfully");
      await loadShoppingLists();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while creating shopping list.";
      _uiService.showError(_errorMessage!);
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> updateShoppingList(String listId, List<bool> boughtItems) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      final request = ShoppingListUpdateRequestDTO(boughtItems: boughtItems);
      await _shoppingListService.updateListInformation(listId, request);
      _uiService.showSuccess("Shopping list updated successfully");
      await loadShoppingLists();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while updating shopping list.";
      _uiService.showError(_errorMessage!);
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> removeMember(String memberId) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      await _householdService.removeMember(memberId);
      _uiService.showSuccess("Member removed successfully");
      await loadHouseholdData();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while removing member.";
      _uiService.showError(_errorMessage!);
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void clear() {
    _currentHousehold = null;
    _invitationCode = null;
    _shoppingLists = [];
    _householdChores = [];
    _isLoading = false;
    _errorMessage = null;
    notifyListeners();
  }
}
