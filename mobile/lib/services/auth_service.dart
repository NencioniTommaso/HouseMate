import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../shared/dto/auth/request/login_request_dto.dart';
import '../shared/dto/auth/request/register_request_dto.dart';
import '../shared/dto/auth/response/login_response_dto.dart';
import '../shared/dto/user/response/user_response_dto.dart';

class AuthService {
  final ApiClient apiClient;

  AuthService(this.apiClient);

  Future<UserResponseDTO> login(LoginRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/auth/login',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 200) {
        final loginResponse = LoginResponseDTO.fromJson(response.data);
        await apiClient.secureStorage.write(
          key: 'jwt_token',
          value: loginResponse.token,
        );
        return loginResponse.user;
      } else {
        throw Exception(
            'Failed to login user. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      if (e.response?.statusCode == 401 || e.response?.statusCode == 403) {
        throw Exception('Invalid email or password');
      }
      throw Exception(
          'Failed to login user. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<UserResponseDTO> register(RegisterRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/auth/register',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 201) {
        final loginResponse = LoginResponseDTO.fromJson(response.data);
        await apiClient.secureStorage.write(
          key: 'jwt_token',
          value: loginResponse.token,
        );
        return loginResponse.user;
      } else {
        throw Exception(
            'Failed to register user. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to register user. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<void> logout() async {
    await apiClient.secureStorage.delete(key: 'jwt_token');
  }
}
