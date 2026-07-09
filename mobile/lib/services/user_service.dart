import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../shared/dto/user/request/user_update_request_dto.dart';
import '../shared/dto/user/response/user_response_dto.dart';

class UserService {
  final ApiClient apiClient;

  UserService(this.apiClient);

  Future<UserResponseDTO> getCurrentUser() async {
    try {
      final response = await apiClient.dio.get('/users/me');

      if (response.statusCode == 200) {
        return UserResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to retrieve current user. Server responded with status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve current user. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<UserResponseDTO> updateCurrentUser(UserUpdateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.patch(
        '/users/me',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 200) {
        return UserResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to update current user. Server responded with status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to update current user. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }
}
