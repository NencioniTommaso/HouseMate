import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:mobile/core/network/api_client.dart';

/// In-memory implementation of FlutterSecureStorage for testing.
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
  }) async {
    return _storage[key];
  }

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

  @override
  Future<Map<String, String>> readAll({
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    return Map<String, String>.from(_storage);
  }

  @override
  Future<bool> containsKey({
    required String key,
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    return _storage.containsKey(key);
  }
}

/// Mock HttpClientAdapter for testing Dio requests.
class MockHttpClientAdapter implements HttpClientAdapter {
  final List<RequestOptions> requests = [];
  RequestOptions? get lastRequest => requests.isEmpty ? null : requests.last;

  FutureOr<ResponseBody> Function(RequestOptions options)? _handler;

  void setHandler(FutureOr<ResponseBody> Function(RequestOptions options) handler) {
    _handler = handler;
  }

  void reply(int statusCode, dynamic data, {Map<String, List<String>>? headers}) {
    setHandler((options) {
      final bodyString = data is String ? data : jsonEncode(data);
      return ResponseBody.fromString(
        bodyString,
        statusCode,
        headers: headers ?? {
          Headers.contentTypeHeader: [Headers.jsonContentType],
        },
      );
    });
  }

  void replyError(DioExceptionType type, {int? statusCode, dynamic data}) {
    setHandler((options) {
      if (type == DioExceptionType.badResponse) {
        final bodyString = data is String ? data : jsonEncode(data);
        return ResponseBody.fromString(
          bodyString,
          statusCode ?? 400,
          headers: {
            Headers.contentTypeHeader: [Headers.jsonContentType],
          },
        );
      }
      throw DioException(
        requestOptions: options,
        type: type,
        message: 'Mocked DioException of type $type',
      );
    });
  }

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    requests.add(options);
    if (_handler != null) {
      return await _handler!(options);
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

class TestContext {
  final ApiClient apiClient;
  final MockHttpClientAdapter adapter;
  final FakeSecureStorage secureStorage;

  TestContext({
    required this.apiClient,
    required this.adapter,
    required this.secureStorage,
  });
}

TestContext createTestContext({
  Map<String, String>? initialStorage,
  String? defaultToken = 'mock_jwt_token',
}) {
  final storageData = <String, String>{};
  if (defaultToken != null) {
    storageData['jwt_token'] = defaultToken;
  }
  if (initialStorage != null) {
    storageData.addAll(initialStorage);
  }

  final fakeStorage = FakeSecureStorage(storageData);
  final adapter = MockHttpClientAdapter();
  final dio = Dio(BaseOptions(
    baseUrl: ApiClient.baseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
    headers: {'Content-Type': 'application/json'},
  ));
  dio.httpClientAdapter = adapter;

  final apiClient = ApiClient(dio: dio, secureStorage: fakeStorage);

  return TestContext(
    apiClient: apiClient,
    adapter: adapter,
    secureStorage: fakeStorage,
  );
}
