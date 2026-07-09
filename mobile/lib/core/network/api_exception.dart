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

        // 1. Set a default fallback message
        String serverMessage = "An unexpected error occurred.";

        // 2. Check what the server ACTUALLY sent back
        final responseData = dioError.response?.data;

        if (responseData is Map<String, dynamic>) {
          // It IS a JSON object, safe to extract the message!
          serverMessage = responseData['message'] ?? serverMessage;
        } else if (responseData is String) {
          // The server just sent a raw string (like an HTML error page)
          // Let's print it to the console so you can see what Spring Boot is complaining about
          print("RAW SERVER ERROR: $responseData");
          serverMessage = "Server returned an unexpected format (Status $statusCode)";
        }

        if (statusCode == 401) {
          return ApiException(message: "Invalid credentials.", statusCode: 401);
        } else if (statusCode == 403) {
          return ApiException(message: "You do not have permission to do this.", statusCode: 403);
        } else if (statusCode == 404) {
          return ApiException(message: "Endpoint not found. Check your API URL!", statusCode: 404);
        } else {
          return ApiException(message: serverMessage, statusCode: statusCode);
        }

      case DioExceptionType.cancel:
        return ApiException(message: "Request to the server was cancelled.");

      case DioExceptionType.connectionError:
        return ApiException(message: "No internet connection detected.");

      default:
        return ApiException(message: "Something went wrong.");
    }
  }

  @override
  String toString() => message;
}