import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class ApiClient {
  // Replace with your PC's actual IPv4 address!
  static const String baseUrl = 'http://192.168.192.118:8080/api';

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
          // 1. Before every request goes out, read the JWT from the phone's vault
          final token = await secureStorage.read(key: 'jwt_token');

          // 2. If we have a token, attach it!
          if (token != null) {
            options.headers['Authorization'] = 'Bearer $token';
          }

          // 3. Send the request on its way
          return handler.next(options);
        },
        onError: (DioException e, handler) async {
          // You can handle global 401 Unauthorized errors here (e.g., force logout)
          return handler.next(e);
        },
      ),
    );
  }
}