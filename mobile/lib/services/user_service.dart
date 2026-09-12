import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../shared/dto/user/request/user_update_request_dto.dart';
import '../shared/dto/user/response/user_response_dto.dart';

class UserService {
  final ApiClient apiClient;

  UserService(this.apiClient);

  Future<UserResponseDTO> getCurrentUser() async {
    try {
      final response = await apiClient.dio.get('/users/me');

      return UserResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<UserResponseDTO> updateCurrentUser(UserUpdateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.patch(
        '/users/me',
        data: requestDTO.toJson(),
      );

      return UserResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
