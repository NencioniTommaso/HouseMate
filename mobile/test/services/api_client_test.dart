import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/network/api_exception.dart';

class FakeSecureStorage extends FlutterSecureStorage {
  final Map<String, String> _storage = {};

  FakeSecureStorage([Map<String, String>? initial]) : super() {
    if (initial != null) {
      _storage.addAll(initial);
    }
  }

  @override
  Future<String?> read({
    required String key,
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async => _storage[key];

  @override
  Future<void> write({
    required String key,
    required String? value,
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    if (value == null) {
      _storage.remove(key);
    } else {
      _storage[key] = value;
    }
  }

  @override
  Future<void> delete({
    required String key,
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    _storage.remove(key);
  }

  @override
  Future<void> deleteAll({
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    _storage.clear();
  }
}

class MockHttpClientAdapter implements HttpClientAdapter {
  final List<RequestOptions> requests = [];
  FutureOr<ResponseBody> Function(RequestOptions options)? handler;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    requests.add(options);
    if (handler != null) {
      return handler!(options);
    }
    return ResponseBody.fromString(
      '{}',
      200,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('ApiClient Configuration & Defaults', () {
    test('Default constructor sets expected BaseOptions', () {
      final client = ApiClient();
      expect(client.dio.options.baseUrl, ApiClient.baseUrl);
      expect(client.dio.options.connectTimeout, const Duration(seconds: 10));
      expect(client.dio.options.receiveTimeout, const Duration(seconds: 10));
      expect(client.dio.options.headers['Content-Type'], 'application/json');
      expect(client.dio.interceptors.isNotEmpty, isTrue);
    });

    test('Custom constructor preserves injected Dio and FlutterSecureStorage', () {
      final customDio = Dio();
      final customStorage = FakeSecureStorage({'jwt_token': 'test_token'});
      final client = ApiClient(dio: customDio, secureStorage: customStorage);

      expect(identical(client.dio, customDio), isTrue);
      expect(identical(client.secureStorage, customStorage), isTrue);
    });
  });

  group('ApiClient Interceptor', () {
    late FakeSecureStorage fakeStorage;
    late MockHttpClientAdapter mockAdapter;
    late Dio dio;
    late ApiClient apiClient;

    setUp(() {
      fakeStorage = FakeSecureStorage();
      mockAdapter = MockHttpClientAdapter();
      dio = Dio(BaseOptions(baseUrl: ApiClient.baseUrl));
      dio.httpClientAdapter = mockAdapter;
      apiClient = ApiClient(dio: dio, secureStorage: fakeStorage);
    });

    test('Attaches Authorization Bearer token to authenticated endpoints', () async {
      await fakeStorage.write(key: 'jwt_token', value: 'secret-token-123');

      await apiClient.dio.get('/users/me');

      expect(mockAdapter.requests.length, 1);
      final req = mockAdapter.requests.first;
      expect(req.headers['Authorization'], 'Bearer secret-token-123');
    });

    test('Does not attach Authorization header when token is null', () async {
      await fakeStorage.delete(key: 'jwt_token');

      await apiClient.dio.get('/users/me');

      expect(mockAdapter.requests.length, 1);
      final req = mockAdapter.requests.first;
      expect(req.headers.containsKey('Authorization'), isFalse);
    });

    test('Does not attach Authorization header for auth endpoints starting with /auth/', () async {
      await fakeStorage.write(key: 'jwt_token', value: 'secret-token-123');

      await apiClient.dio.post('/auth/login', data: {'email': 'a@b.com'});

      expect(mockAdapter.requests.length, 1);
      final req = mockAdapter.requests.first;
      expect(req.headers.containsKey('Authorization'), isFalse);
    });

    test('Error interceptor passes through errors cleanly', () async {
      mockAdapter.handler = (options) {
        return ResponseBody.fromString(
          jsonEncode({'message': 'Internal Server Error'}),
          500,
          headers: {
            Headers.contentTypeHeader: [Headers.jsonContentType],
          },
        );
      };

      expect(
        () => apiClient.dio.get('/broken'),
        throwsA(isA<DioException>()),
      );
    });
  });

  group('ApiException Tests', () {
    final dummyOptions = RequestOptions(path: '/test');

    test('fromDioError maps timeout types correctly', () {
      final types = [
        DioExceptionType.connectionTimeout,
        DioExceptionType.receiveTimeout,
        DioExceptionType.sendTimeout,
      ];

      for (final type in types) {
        final dioEx = DioException(requestOptions: dummyOptions, type: type);
        final apiEx = ApiException.fromDioError(dioEx);
        expect(
          apiEx.message,
          'Connection timed out. Please check your internet connection.',
        );
      }
    });

    test('fromDioError maps cancel type correctly', () {
      final dioEx = DioException(
        requestOptions: dummyOptions,
        type: DioExceptionType.cancel,
      );
      final apiEx = ApiException.fromDioError(dioEx);
      expect(apiEx.message, 'Request to the server was cancelled.');
    });

    test('fromDioError maps connectionError correctly on non-web', () {
      final dioEx = DioException(
        requestOptions: dummyOptions,
        type: DioExceptionType.connectionError,
      );
      final apiEx = ApiException.fromDioError(dioEx);
      expect(apiEx.message, 'No internet connection detected.');
    });

    test('fromDioError maps default unknown type', () {
      final dioEx = DioException(
        requestOptions: dummyOptions,
        type: DioExceptionType.unknown,
      );
      final apiEx = ApiException.fromDioError(dioEx);
      expect(apiEx.message, 'An unexpected network error occurred.');
    });

    test('fromDioError extracts Spring Boot validation errors', () {
      final dioEx = DioException(
        requestOptions: dummyOptions,
        type: DioExceptionType.badResponse,
        response: Response(
          requestOptions: dummyOptions,
          statusCode: 400,
          data: {
            'error': 'Validation Failed',
            'email': 'must not be empty',
            'amount': 'must be positive',
          },
        ),
      );

      final apiEx = ApiException.fromDioError(dioEx);
      expect(apiEx.statusCode, 400);
      expect(
        apiEx.message,
        contains('Validation failed: email: must not be empty, amount: must be positive'),
      );
    });

    test('fromDioError extracts message key from response data', () {
      final dioEx = DioException(
        requestOptions: dummyOptions,
        type: DioExceptionType.badResponse,
        response: Response(
          requestOptions: dummyOptions,
          statusCode: 404,
          data: {'message': 'User not found in system'},
        ),
      );

      final apiEx = ApiException.fromDioError(dioEx);
      expect(apiEx.statusCode, 404);
      expect(apiEx.message, 'User not found in system');
    });

    test('fromDioError extracts error key when message is missing', () {
      final dioEx = DioException(
        requestOptions: dummyOptions,
        type: DioExceptionType.badResponse,
        response: Response(
          requestOptions: dummyOptions,
          statusCode: 401,
          data: {'error': 'Invalid credentials'},
        ),
      );

      final apiEx = ApiException.fromDioError(dioEx);
      expect(apiEx.statusCode, 401);
      expect(apiEx.message, 'Invalid credentials');
    });

    test('fromDioError extracts raw string error', () {
      final dioEx = DioException(
        requestOptions: dummyOptions,
        type: DioExceptionType.badResponse,
        response: Response(
          requestOptions: dummyOptions,
          statusCode: 400,
          data: 'Custom bad input message',
        ),
      );

      final apiEx = ApiException.fromDioError(dioEx);
      expect(apiEx.message, 'Custom bad input message');
    });

    test('fromDioError handles HTML error response gracefully', () {
      final dioEx = DioException(
        requestOptions: dummyOptions,
        type: DioExceptionType.badResponse,
        response: Response(
          requestOptions: dummyOptions,
          statusCode: 502,
          data: '<!DOCTYPE html><html><body>502 Bad Gateway</body></html>',
        ),
      );

      final apiEx = ApiException.fromDioError(dioEx);
      expect(apiEx.message, 'Server error (HTML response received).');
    });

    test('fromDioError uses status code fallbacks when data is empty', () {
      final cases = {
        400: 'Bad request. Please check your input.',
        401: 'Unauthorized. Please log in again.',
        403: "Access denied. You don't have permission for this action.",
        404: 'Resource not found.',
        500: 'Internal server error. Please try again later.',
        503: 'Unexpected error occurred.',
      };

      for (final entry in cases.entries) {
        final dioEx = DioException(
          requestOptions: dummyOptions,
          type: DioExceptionType.badResponse,
          response: Response(
            requestOptions: dummyOptions,
            statusCode: entry.key,
            data: null,
          ),
        );
        final apiEx = ApiException.fromDioError(dioEx);
        expect(apiEx.statusCode, entry.key);
        expect(apiEx.message, entry.value);
      }
    });

    test('fromDioError sanitizes UUIDs in error message', () {
      const uuid = '123e4567-e89b-12d3-a456-426614174000';
      final dioEx = DioException(
        requestOptions: dummyOptions,
        type: DioExceptionType.badResponse,
        response: Response(
          requestOptions: dummyOptions,
          statusCode: 400,
          data: {'message': 'Entity with ID $uuid was not found.'},
        ),
      );

      final apiEx = ApiException.fromDioError(dioEx);
      expect(apiEx.message, 'Entity with ID [ID] was not found.');
    });

    test('toString formats correctly with and without statusCode', () {
      final exWithStatus = ApiException(message: 'Forbidden', statusCode: 403);
      expect(exWithStatus.toString(), 'Forbidden (Status 403)');

      final exWithoutStatus = ApiException(message: 'Timeout');
      expect(exWithoutStatus.toString(), 'Timeout');
    });
  });
}
