import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../core/network/api_client.dart';
import '../services/user_service.dart';
import '../core/network/api_exception.dart';
import '../services/auth_service.dart';
import '../shared/dto/auth/request/login_request_dto.dart';
import '../shared/dto/auth/request/register_request_dto.dart';
import '../shared/dto/user/response/user_response_dto.dart';

import '../core/utils/ui_service.dart';

// ChangeNotifier is Flutter's built-in way to say "I can notify the UI when I change"
class AuthProvider extends ChangeNotifier {
  final AuthService _authService;
  final UserService _userService;
  final UiService _uiService;
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  // The State Variables
  UserResponseDTO? currentUser;
  bool isLoading = false;
  String? errorMessage;
  bool isCheckingSession = true;

  // A getter to easily check if someone is logged in
  bool get isAuthenticated => currentUser != null;

  AuthProvider({required ApiClient apiClient, required UiService uiService})
      : _authService = AuthService(apiClient),
        _userService = UserService(apiClient),
        _uiService = uiService {
    _checkExistingSession();
  }

  // NEW: The Bootstrapper Logic
  Future<void> _checkExistingSession() async {
    try {
      final token = await _storage.read(key: 'jwt_token');

      if (token != null) {
        currentUser = await _userService.getCurrentUser();
      }
    } catch (e) {
      await _storage.delete(key: 'jwt_token');
    } finally {
      // Whether we found a token or not, we are done checking.
      isCheckingSession = false;
      notifyListeners();
    }
  }

  Future<bool> register(RegisterRequestDTO request) async {
    errorMessage = null;
    try {
      isLoading = true;
      notifyListeners();

      final response = await _authService.register(request);
      currentUser = response;

      return true; // Success!

    } on ApiException catch (e) {
      errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      errorMessage = "An unexpected error occurred.";
      _uiService.showError(errorMessage!);
      return false;

    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> login(String email, String password) async {
    debugPrint("Attempting login for: $email");
    errorMessage = null;
    try {
      isLoading = true;
      notifyListeners();

      final request = LoginRequestDTO(email: email.trim(), password: password);
      final response = await _authService.login(request);

      currentUser = response;
      debugPrint("Login successful: ${response.email}");
      return true;
    } on ApiException catch (e) {
      debugPrint("Login API Error: ${e.message}");
      errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      debugPrint("Login Unexpected Error: $e");
      errorMessage = "An unexpected error occurred.";
      _uiService.showError(errorMessage!);
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    await _authService.logout();
    await _storage.delete(key: 'jwt_token');
    currentUser = null;
    _uiService.showSuccess("Successfully logged out");
    notifyListeners();
  }
}