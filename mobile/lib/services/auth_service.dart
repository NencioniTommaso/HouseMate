import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
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

      final loginResponse = LoginResponseDTO.fromJson(response.data);
      await apiClient.secureStorage.write(
        key: 'jwt_token',
        value: loginResponse.token,
      );
      return loginResponse.user;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<UserResponseDTO> register(RegisterRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/auth/register',
        data: requestDTO.toJson(),
      );

      final loginResponse = LoginResponseDTO.fromJson(response.data);
      await apiClient.secureStorage.write(
        key: 'jwt_token',
        value: loginResponse.token,
      );
      return loginResponse.user;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> logout() async {
    await apiClient.secureStorage.delete(key: 'jwt_token');
  }
}
