import 'package:flutter/material.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../services/user_service.dart';
import '../shared/dto/user/request/user_update_request_dto.dart';
import '../shared/dto/user/response/user_response_dto.dart';

import '../core/utils/ui_service.dart';

class UserProvider extends ChangeNotifier {
  final UserService _userService;
  final UiService _uiService;

  UserResponseDTO? _currentUser;
  bool _isLoading = false;
  String? _errorMessage;

  UserProvider({required ApiClient apiClient, required UiService uiService}) 
      : _userService = UserService(apiClient),
        _uiService = uiService;

  UserResponseDTO? get currentUser => _currentUser;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  Future<void> loadUserProfile() async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      _currentUser = await _userService.getCurrentUser();
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
    } catch (e) {
      _errorMessage = "An unexpected error occurred.";
      _uiService.showError(_errorMessage!);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> updateUserProfile(UserUpdateRequestDTO request) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      _currentUser = await _userService.updateCurrentUser(request);
      _uiService.showSuccess("Profile updated successfully");
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while updating profile.";
      _uiService.showError(_errorMessage!);
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void clear() {
    _currentUser = null;
    _isLoading = false;
    _errorMessage = null;
    notifyListeners();
  }
}
