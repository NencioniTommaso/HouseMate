import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/auth_service.dart';
import 'package:mobile/shared/dto/auth/request/login_request_dto.dart';
import 'package:mobile/shared/dto/auth/request/register_request_dto.dart';

import 'test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late TestContext context;
  late AuthService authService;

  setUp(() {
    context = createTestContext(defaultToken: null);
    authService = AuthService(context.apiClient);
  });

  group('AuthService - login', () {
    final loginReq = LoginRequestDTO(
      email: 'user@example.com',
      password: 'password123',
    );

    final mockSuccessPayload = {
      'token': 'jwt-sample-token-abc',
      'user': {
        'id': 'u1',
        'email': 'user@example.com',
        'name': 'John',
        'surname': 'Doe',
      },
    };

    test('successfully logs in, saves JWT token, and returns UserResponseDTO', () async {
      context.adapter.reply(200, mockSuccessPayload);

      final result = await authService.login(loginReq);

      expect(context.adapter.requests.length, 1);
      final req = context.adapter.lastRequest!;
      expect(req.method, 'POST');
      expect(req.path, '/auth/login');
      expect(req.data, loginReq.toJson());

      // Token must be stored in secureStorage
      final savedToken = await context.secureStorage.read(key: 'jwt_token');
      expect(savedToken, 'jwt-sample-token-abc');

      // Returned user matches payload
      expect(result.id, 'u1');
      expect(result.email, 'user@example.com');
      expect(result.name, 'John');
      expect(result.surname, 'Doe');
    });

    test('throws ApiException on 401 Unauthorized', () async {
      context.adapter.reply(401, {'error': 'Invalid credentials'});

      expect(
        () => authService.login(loginReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 401)),
      );

      final token = await context.secureStorage.read(key: 'jwt_token');
      expect(token, isNull);
    });

    test('throws ApiException on 400 Bad Request validation error', () async {
      context.adapter.reply(400, {
        'error': 'Validation Failed',
        'email': 'Email is not valid',
      });

      expect(
        () => authService.login(loginReq),
        throwsA(isA<ApiException>().having(
          (e) => e.message,
          'message',
          contains('Email is not valid'),
        )),
      );
    });

    test('throws ApiException on timeout', () async {
      context.adapter.replyError(DioExceptionType.connectionTimeout);

      expect(
        () => authService.login(loginReq),
        throwsA(isA<ApiException>().having(
          (e) => e.message,
          'message',
          contains('Connection timed out'),
        )),
      );
    });
  });

  group('AuthService - register', () {
    final registerReq = RegisterRequestDTO(
      name: 'Jane',
      surname: 'Smith',
      email: 'new@example.com',
      password: 'password123',
      iban: 'DE1234567890',
      inviteCode: 'INVITE123',
    );

    final mockSuccessPayload = {
      'token': 'jwt-registered-token-xyz',
      'user': {
        'id': 'u2',
        'email': 'new@example.com',
        'name': 'Jane',
        'surname': 'Smith',
        'iban': 'DE1234567890',
      },
    };

    test('successfully registers, saves JWT token, and returns UserResponseDTO', () async {
      context.adapter.reply(200, mockSuccessPayload);

      final result = await authService.register(registerReq);

      expect(context.adapter.requests.length, 1);
      final req = context.adapter.lastRequest!;
      expect(req.method, 'POST');
      expect(req.path, '/auth/register');
      expect(req.data, registerReq.toJson());

      final savedToken = await context.secureStorage.read(key: 'jwt_token');
      expect(savedToken, 'jwt-registered-token-xyz');

      expect(result.id, 'u2');
      expect(result.email, 'new@example.com');
      expect(result.name, 'Jane');
      expect(result.surname, 'Smith');
      expect(result.iban, 'DE1234567890');
    });

    test('throws ApiException on 400 Conflict / duplicate email', () async {
      context.adapter.reply(400, {'message': 'Email already registered'});

      expect(
        () => authService.register(registerReq),
        throwsA(isA<ApiException>().having(
          (e) => e.message,
          'message',
          'Email already registered',
        )),
      );
    });

    test('throws ApiException on 500 server error', () async {
      context.adapter.reply(500, {'message': 'Internal Server Error'});

      expect(
        () => authService.register(registerReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 500)),
      );
    });
  });

  group('AuthService - logout', () {
    test('removes token from secureStorage', () async {
      await context.secureStorage.write(key: 'jwt_token', value: 'existing_token');
      expect(await context.secureStorage.read(key: 'jwt_token'), 'existing_token');

      await authService.logout();

      expect(await context.secureStorage.read(key: 'jwt_token'), isNull);
    });
  });
}
