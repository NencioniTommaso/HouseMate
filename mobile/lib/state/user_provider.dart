import 'package:flutter/material.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../services/user_service.dart';
import '../shared/dto/user/response/user_response_dto.dart';

class UserProvider extends ChangeNotifier {
  final UserService _userService = UserService(ApiClient());

  UserResponseDTO? _currentUser;
  bool _isLoading = false;
  String? _errorMessage;

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
    } catch (e) {
      _errorMessage = "An unexpected error occurred.";
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
