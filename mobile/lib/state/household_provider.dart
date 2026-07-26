import 'package:flutter/material.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../services/household_service.dart';
import '../services/shopping_list_service.dart';
import '../shared/dto/household/response/household_response_dto.dart';
import '../shared/dto/items/response/shopping_list_response_dto.dart';

class HouseholdProvider extends ChangeNotifier {
  final HouseholdService _householdService = HouseholdService(ApiClient());
  final ShoppingListService _shoppingListService = ShoppingListService(ApiClient());

  HouseholdResponseDTO? _currentHousehold;
  List<ShoppingListResponseDTO> _shoppingLists = [];
  bool _isLoading = false;
  String? _errorMessage;

  HouseholdResponseDTO? get currentHousehold => _currentHousehold;
  List<ShoppingListResponseDTO> get shoppingLists => _shoppingLists;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  bool get hasHousehold => _currentHousehold != null;

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
      }
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching household data.";
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
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching shopping lists.";
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
      ]);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
