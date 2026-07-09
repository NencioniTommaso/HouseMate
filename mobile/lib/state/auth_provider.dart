import 'package:flutter/material.dart';
import 'package:mobile/core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../services/auth_service.dart';
import '../shared/dto/auth/request/login_request_dto.dart';
import '../shared/dto/auth/request/register_request_dto.dart';
import '../shared/dto/user/response/user_response_dto.dart';

// ChangeNotifier is Flutter's built-in way to say "I can notify the UI when I change"
class AuthProvider extends ChangeNotifier {
  final AuthService _authService = AuthService(ApiClient());

  // The State Variables
  UserResponseDTO? currentUser;
  bool isLoading = false;
  String? errorMessage;

  // A getter to easily check if someone is logged in
  bool get isAuthenticated => currentUser != null;

  Future<bool> register(RegisterRequestDTO request) async {
    try {
      isLoading = true;
      errorMessage = null;
      notifyListeners();

      final response = await _authService.register(request);
      currentUser = response;

      return true; // Success!

    } on ApiException catch (e) {
      errorMessage = e.message;
      return false;
    } catch (e) {
      errorMessage = "An unexpected error occurred.";
      return false;

    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> login(String email, String password) async {
    try {
      // 1. Tell the UI we are loading
      isLoading = true;
      errorMessage = null;
      notifyListeners(); // <-- THIS IS THE MAGIC! It forces the UI to redraw.

      final request = LoginRequestDTO(email: email, password: password);
      final response = await _authService.login(request);

      currentUser = response;

      return true;

    } on ApiException catch (e) {
      errorMessage = e.message;
      return false;

    } finally {
      // Tell the UI we are done loading, regardless of success/fail
      isLoading = false;
      notifyListeners();
    }
  }

  void logout() {
    _authService.logout();
    currentUser = null;
    notifyListeners();
  }
}