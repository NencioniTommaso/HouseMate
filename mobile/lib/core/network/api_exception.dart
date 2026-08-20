import 'package:dio/dio.dart';

class ApiException implements Exception {
  final String message;
  final int? statusCode;

  ApiException({required this.message, this.statusCode});

  // A factory that reads the Dio error and generates a clean, sanitized ApiException
  factory ApiException.fromDioError(DioException dioError) {
    switch (dioError.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.sendTimeout:
        return ApiException(
          message: "Connection timed out. Please check your internet connection.",
        );

      case DioExceptionType.badResponse:
        final statusCode = dioError.response?.statusCode;
        final responseData = dioError.response?.data;

        String extractedMessage = _extractMessage(responseData, statusCode);
        String sanitizedMessage = _sanitizeMessage(extractedMessage);

        return ApiException(
          message: sanitizedMessage,
          statusCode: statusCode,
        );

      case DioExceptionType.cancel:
        return ApiException(message: "Request to the server was cancelled.");

      case DioExceptionType.connectionError:
        return ApiException(message: "No internet connection detected.");

      default:
        return ApiException(message: "An unexpected network error occurred.");
    }
  }

  /// Extracts a human-readable message from the backend response.
  static String _extractMessage(dynamic data, int? statusCode) {
    if (data is Map<String, dynamic>) {
      // 1. Check for Spring Boot validation errors (BindException)
      if (data['error'] == "Validation Failed") {
        final fieldErrors = <String>[];
        data.forEach((key, value) {
          if (key != 'error') {
            fieldErrors.add("$key: $value");
          }
        });
        return "Validation failed: ${fieldErrors.join(', ')}";
      }

      // 2. Check for standard 'message' or 'error' keys
      if (data.containsKey('message') && data['message'] != null) {
        return data['message'].toString();
      }
      if (data.containsKey('error') && data['error'] != null) {
        return data['error'].toString();
      }
    }

    if (data is String && data.isNotEmpty) {
      // Handle raw string responses (e.g. from handleIllegalArgument)
      if (data.startsWith('<!DOCTYPE html>')) {
        return "Server error (HTML response received).";
      }
      return data;
    }

    // Fallback based on status code
    switch (statusCode) {
      case 400:
        return "Bad request. Please check your input.";
      case 401:
        return "Unauthorized. Please log in again.";
      case 403:
        return "Access denied. You don't have permission for this action.";
      case 404:
        return "Resource not found.";
      case 500:
        return "Internal server error. Please try again later.";
      default:
        return "Unexpected error occurred.";
    }
  }

  /// Redacts sensitive information like UUIDs from the error message.
  static String _sanitizeMessage(String msg) {
    final uuidRegex = RegExp(
      r'[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}',
    );
    return msg.replaceAll(uuidRegex, '[ID]');
  }

  @override
  String toString() {
    if (statusCode != null) {
      return "$message (Status $statusCode)";
    }
    return message;
  }
}
