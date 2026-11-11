import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/user_service.dart';
import 'package:mobile/shared/dto/user/request/user_update_request_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';

import 'test_helpers.dart';

void main() {
  late TestContext context;
  late UserService userService;

  setUp(() {
    context = createTestContext();
    userService = UserService(context.apiClient);
  });

  group('UserService.getCurrentUser', () {
    test('sends GET /users/me and parses UserResponseDTO on 200', () async {
      context.adapter.reply(200, {
        'id': 'user-123',
        'name': 'John',
        'surname': 'Doe',
        'email': 'john.doe@example.com',
        'iban': 'NL01BANK0123456789',
        'paymentLink': 'https://pay.me/johndoe',
      });

      final result = await userService.getCurrentUser();

      expect(context.adapter.lastRequest?.method, 'GET');
      expect(context.adapter.lastRequest?.path, '/users/me');
      expect(result, isA<UserResponseDTO>());
      expect(result.id, 'user-123');
      expect(result.name, 'John');
      expect(result.surname, 'Doe');
      expect(result.email, 'john.doe@example.com');
      expect(result.iban, 'NL01BANK0123456789');
      expect(result.paymentLink, 'https://pay.me/johndoe');
    });

    test('throws ApiException on 401 unauthorized', () async {
      context.adapter.replyError(
        DioExceptionType.badResponse,
        statusCode: 401,
        data: {'error': 'Unauthorized'},
      );

      expect(
        () => userService.getCurrentUser(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 401)),
      );
    });

    test('throws ApiException on 404 not found', () async {
      context.adapter.replyError(
        DioExceptionType.badResponse,
        statusCode: 404,
        data: {'error': 'User not found'},
      );

      expect(
        () => userService.getCurrentUser(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 404)),
      );
    });

    test('throws ApiException on 500 server error', () async {
      context.adapter.replyError(
        DioExceptionType.badResponse,
        statusCode: 500,
        data: {'error': 'Internal server error'},
      );

      expect(
        () => userService.getCurrentUser(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 500)),
      );
    });

    test('throws ApiException on connection timeout', () async {
      context.adapter.replyError(DioExceptionType.connectionTimeout);

      expect(
        () => userService.getCurrentUser(),
        throwsA(isA<ApiException>().having(
          (e) => e.message,
          'message',
          contains('Connection timed out'),
        )),
      );
    });
  });

  group('UserService.updateCurrentUser', () {
    test('sends PATCH /users/me with payload and parses updated UserResponseDTO on 200', () async {
      final requestDTO = UserUpdateRequestDTO(
        name: 'Jane',
        surname: 'Doe',
        iban: 'DE89370400440532013000',
        paymentLink: 'https://paypal.me/janedoe',
      );

      context.adapter.reply(200, {
        'id': 'user-123',
        'name': 'Jane',
        'surname': 'Doe',
        'email': 'john.doe@example.com',
        'iban': 'DE89370400440532013000',
        'paymentLink': 'https://paypal.me/janedoe',
      });

      final result = await userService.updateCurrentUser(requestDTO);

      expect(context.adapter.lastRequest?.method, 'PATCH');
      expect(context.adapter.lastRequest?.path, '/users/me');
      expect(context.adapter.lastRequest?.data, {
        'name': 'Jane',
        'surname': 'Doe',
        'email': null,
        'iban': 'DE89370400440532013000',
        'paymentLink': 'https://paypal.me/janedoe',
      });
      expect(result.name, 'Jane');
      expect(result.iban, 'DE89370400440532013000');
    });

    test('throws ApiException on 400 bad request validation error', () async {
      final requestDTO = UserUpdateRequestDTO(name: '');

      context.adapter.replyError(
        DioExceptionType.badResponse,
        statusCode: 400,
        data: {'error': 'Invalid user data'},
      );

      expect(
        () => userService.updateCurrentUser(requestDTO),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });

    test('throws ApiException on 500 server error', () async {
      final requestDTO = UserUpdateRequestDTO(name: 'Jane');

      context.adapter.replyError(
        DioExceptionType.badResponse,
        statusCode: 500,
        data: {'error': 'Internal server error'},
      );

      expect(
        () => userService.updateCurrentUser(requestDTO),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 500)),
      );
    });

    test('throws ApiException on receive timeout', () async {
      final requestDTO = UserUpdateRequestDTO(name: 'Jane');

      context.adapter.replyError(DioExceptionType.receiveTimeout);

      expect(
        () => userService.updateCurrentUser(requestDTO),
        throwsA(isA<ApiException>().having(
          (e) => e.message,
          'message',
          contains('Connection timed out'),
        )),
      );
    });
  });
}
