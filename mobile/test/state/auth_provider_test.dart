import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/auth_service.dart';
import 'package:mobile/services/user_service.dart';
import 'package:mobile/state/auth_provider.dart';
import 'package:mobile/shared/dto/auth/request/login_request_dto.dart';
import 'package:mobile/shared/dto/auth/request/register_request_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';

import '../services/test_helpers.dart';
import 'state_test_helpers.dart';

class FakeAuthService extends AuthService {
  FakeAuthService(super.apiClient);

  Future<UserResponseDTO> Function(LoginRequestDTO)? onLogin;
  Future<UserResponseDTO> Function(RegisterRequestDTO)? onRegister;

  @override
  Future<UserResponseDTO> login(LoginRequestDTO requestDTO) {
    if (onLogin != null) return onLogin!(requestDTO);
    return super.login(requestDTO);
  }

  @override
  Future<UserResponseDTO> register(RegisterRequestDTO requestDTO) {
    if (onRegister != null) return onRegister!(requestDTO);
    return super.register(requestDTO);
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late TestContext context;
  late FakeUiService uiService;

  final testUserJson = {
    'id': 'u1',
    'name': 'Test',
    'surname': 'User',
    'email': 'test@example.com',
  };

  final loginResponsePayload = {
    'token': 'mock-jwt-token-xyz',
    'user': testUserJson,
  };

  setUp(() {
    context = createTestContext(defaultToken: null);
    uiService = FakeUiService();
  });

  group('AuthProvider - Initialization & Session Check', () {
    test('initial state when no token exists in storage', () async {
      final authProvider = AuthProvider(
        apiClient: context.apiClient,
        uiService: uiService,
        storage: context.secureStorage,
      );

      // Awaiting microtasks to let _checkExistingSession resolve
      await pumpEventQueue();

      expect(authProvider.isCheckingSession, false);
      expect(authProvider.currentUser, isNull);
      expect(authProvider.isAuthenticated, false);
      expect(authProvider.isLoading, false);
      expect(authProvider.errorMessage, isNull);
    });

    test('successfully restores session when token exists in storage', () async {
      await context.secureStorage.write(key: 'jwt_token', value: 'saved_token');
      context.adapter.reply(200, testUserJson);

      final authProvider = AuthProvider(
        apiClient: context.apiClient,
        uiService: uiService,
        storage: context.secureStorage,
      );

      await pumpEventQueue();

      expect(authProvider.isCheckingSession, false);
      expect(authProvider.currentUser, isNotNull);
      expect(authProvider.currentUser!.email, 'test@example.com');
      expect(authProvider.isAuthenticated, true);
      expect(authProvider.errorMessage, isNull);
      expect(await context.secureStorage.read(key: 'jwt_token'), 'saved_token');
    });

    test('deletes token if session restoration fails with error', () async {
      await context.secureStorage.write(key: 'jwt_token', value: 'invalid_token');
      context.adapter.replyError(DioExceptionType.badResponse, statusCode: 401, data: {
        'message': 'Token expired',
      });

      final authProvider = AuthProvider(
        apiClient: context.apiClient,
        uiService: uiService,
        storage: context.secureStorage,
      );

      await pumpEventQueue();

      expect(authProvider.isCheckingSession, false);
      expect(authProvider.currentUser, isNull);
      expect(authProvider.isAuthenticated, false);
      expect(await context.secureStorage.read(key: 'jwt_token'), isNull);
    });
  });

  group('AuthProvider - login', () {
    test('successful login sets currentUser and updates isAuthenticated', () async {
      final authProvider = AuthProvider(
        apiClient: context.apiClient,
        uiService: uiService,
        storage: context.secureStorage,
      );
      await pumpEventQueue();

      context.adapter.reply(200, loginResponsePayload);

      final notifiedStates = <bool>[];
      authProvider.addListener(() {
        notifiedStates.add(authProvider.isLoading);
      });

      final result = await authProvider.login('test@example.com', 'password123');

      expect(result, true);
      expect(authProvider.currentUser, isNotNull);
      expect(authProvider.currentUser!.email, 'test@example.com');
      expect(authProvider.isAuthenticated, true);
      expect(authProvider.isLoading, false);
      expect(authProvider.errorMessage, isNull);
      expect(notifiedStates, contains(true)); // loading flipped to true then false
      expect(await context.secureStorage.read(key: 'jwt_token'), 'mock-jwt-token-xyz');
    });

    test('login failure with ApiException records error and notifies UI', () async {
      final authProvider = AuthProvider(
        apiClient: context.apiClient,
        uiService: uiService,
        storage: context.secureStorage,
      );
      await pumpEventQueue();

      context.adapter.replyError(
        DioExceptionType.badResponse,
        statusCode: 401,
        data: {'message': 'Invalid credentials'},
      );

      final result = await authProvider.login('test@example.com', 'wrongpassword');

      expect(result, false);
      expect(authProvider.currentUser, isNull);
      expect(authProvider.isAuthenticated, false);
      expect(authProvider.isLoading, false);
      expect(authProvider.errorMessage, 'Invalid credentials');
      expect(uiService.errorMessages, contains('Invalid credentials'));
    });

    test('login failure with generic error sets fallback error message', () async {
      final fakeAuth = FakeAuthService(context.apiClient);
      fakeAuth.onLogin = (_) => throw Exception('Socket closed unexpectedly');

      final authProvider = AuthProvider(
        apiClient: context.apiClient,
        uiService: uiService,
        storage: context.secureStorage,
        authService: fakeAuth,
      );
      await pumpEventQueue();

      final result = await authProvider.login('test@example.com', 'pass');

      expect(result, false);
      expect(authProvider.currentUser, isNull);
      expect(authProvider.isLoading, false);
      expect(authProvider.errorMessage, 'An unexpected error occurred.');
      expect(uiService.errorMessages, contains('An unexpected error occurred.'));
    });
  });

  group('AuthProvider - register', () {
    final registerDto = RegisterRequestDTO(
      name: 'Test',
      surname: 'User',
      email: 'test@example.com',
      password: 'password123',
      inviteCode: 'INVITE123',
    );

    test('successful register sets currentUser and returns true', () async {
      final authProvider = AuthProvider(
        apiClient: context.apiClient,
        uiService: uiService,
        storage: context.secureStorage,
      );
      await pumpEventQueue();

      context.adapter.reply(200, loginResponsePayload);

      final result = await authProvider.register(registerDto);

      expect(result, true);
      expect(authProvider.currentUser, isNotNull);
      expect(authProvider.currentUser!.name, 'Test');
      expect(authProvider.isAuthenticated, true);
      expect(authProvider.isLoading, false);
      expect(authProvider.errorMessage, isNull);
      expect(await context.secureStorage.read(key: 'jwt_token'), 'mock-jwt-token-xyz');
    });

    test('register failure with ApiException records error message', () async {
      final authProvider = AuthProvider(
        apiClient: context.apiClient,
        uiService: uiService,
        storage: context.secureStorage,
      );
      await pumpEventQueue();

      context.adapter.replyError(
        DioExceptionType.badResponse,
        statusCode: 409,
        data: {'message': 'Email already registered'},
      );

      final result = await authProvider.register(registerDto);

      expect(result, false);
      expect(authProvider.currentUser, isNull);
      expect(authProvider.isLoading, false);
      expect(authProvider.errorMessage, 'Email already registered');
      expect(uiService.errorMessages, contains('Email already registered'));
    });

    test('register failure with unexpected exception sets fallback error message', () async {
      final fakeAuth = FakeAuthService(context.apiClient);
      fakeAuth.onRegister = (_) => throw Exception('Malformed response');

      final authProvider = AuthProvider(
        apiClient: context.apiClient,
        uiService: uiService,
        storage: context.secureStorage,
        authService: fakeAuth,
      );
      await pumpEventQueue();

      final result = await authProvider.register(registerDto);

      expect(result, false);
      expect(authProvider.currentUser, isNull);
      expect(authProvider.isLoading, false);
      expect(authProvider.errorMessage, 'An unexpected error occurred.');
      expect(uiService.errorMessages, contains('An unexpected error occurred.'));
    });
  });

  group('AuthProvider - logout', () {
    test('clears session, removes token, and notifies UI', () async {
      await context.secureStorage.write(key: 'jwt_token', value: 'token-to-delete');
      final authProvider = AuthProvider(
        apiClient: context.apiClient,
        uiService: uiService,
        storage: context.secureStorage,
      );
      await pumpEventQueue();

      authProvider.currentUser = UserResponseDTO(
        id: 'u1',
        name: 'Test',
        surname: 'User',
        email: 'test@example.com',
      );
      expect(authProvider.isAuthenticated, true);

      var listenerNotified = false;
      authProvider.addListener(() {
        listenerNotified = true;
      });

      await authProvider.logout();

      expect(authProvider.currentUser, isNull);
      expect(authProvider.isAuthenticated, false);
      expect(await context.secureStorage.read(key: 'jwt_token'), isNull);
      expect(uiService.successMessages, contains('Successfully logged out'));
      expect(listenerNotified, true);
    });
  });
}
