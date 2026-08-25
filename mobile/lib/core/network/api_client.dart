import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class ApiClient {

  static const String baseUrl = "https://api.housemateapp.stream/api";

  final Dio dio;
  final FlutterSecureStorage secureStorage;

  ApiClient() : dio = Dio(BaseOptions(
    baseUrl: baseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
    headers: {'Content-Type': 'application/json'},
  )),
        secureStorage = const FlutterSecureStorage() {

    // Add the Interceptor
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          // Do not attach token for Auth endpoints
          if (options.path.startsWith('/auth/')) {
            return handler.next(options);
          }

          final token = await secureStorage.read(key: 'jwt_token');

          if (token != null) {
            options.headers['Authorization'] = 'Bearer $token';
          }

          return handler.next(options);
        },
        onError: (e, handler) {
          debugPrint("NETWORK ERROR [${e.response?.statusCode}]: ${e.message}");
          if (e.response?.data != null) {
            debugPrint("RESPONSE DATA: ${e.response?.data}");
          }
          if (e.error is TypeError) {
            debugPrint("CRITICAL PARSING ERROR: ${e.error}");
          }
          return handler.next(e);
        },
      ),
    );
  }
}