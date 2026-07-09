import 'package:dio/dio.dart';

class ApiException implements Exception {
  final String message;
  final int? statusCode;

  ApiException({required this.message, this.statusCode});

// A factory that reads the Dio error and generates a clean ApiException
  factory ApiException.fromDioError(DioException dioError) {
    switch (dioError.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.sendTimeout:
        return ApiException(
            message: "Connection timed out. Please check your internet.");


      case DioExceptionType.badResponse:
        final statusCode = dioError.response?.statusCode;
        // If your Spring Boot GlobalExceptionHandler returns a specific JSON error message,
        // you can read it here (e.g., dioError.response?.data['message'])
        final serverMessage = dioError.response?.data['message'] ??
            "An unexpected error occurred.";

        if (statusCode == 401) {
          return ApiException(
              message: "Invalid credentials or session expired.",
              statusCode: 401);
        } else if (statusCode == 403) {
          return ApiException(message: "You do not have permission to do this.",
              statusCode: 403);
        } else if (statusCode == 404) {
          return ApiException(message: "Resource not found.", statusCode: 404);
        } else {
          return ApiException(message: serverMessage, statusCode: statusCode);
        }

      case DioExceptionType.connectionError:
        return ApiException(
            message: "Cannot connect to server. Are you on the right Wi-Fi?");

      case DioExceptionType.cancel:
        return ApiException(message: "Request to the server was cancelled.");

      case DioExceptionType.badCertificate:
        return ApiException(message: "Secure connection failed. Bad certificate.");

      default:
        return ApiException(message: "Something went wrong.");
    }
  }

  @override
  String toString() => message;
}