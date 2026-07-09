import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../shared/dto/household/request/add_member_request_dto.dart';
import '../shared/dto/household/request/household_create_request_dto.dart';
import '../shared/dto/household/response/household_invitation_code_response_dto.dart';
import '../shared/dto/household/response/household_member_response_dto.dart';
import '../shared/dto/household/response/household_response_dto.dart';

class HouseholdService {
  final ApiClient apiClient;

  HouseholdService(this.apiClient);

  Future<HouseholdResponseDTO> createHousehold(HouseholdCreateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/households',
        data: requestDTO.toJson(),
      );

      return HouseholdResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<HouseholdResponseDTO> getCurrentUserHousehold() async {
    try {
      final response = await apiClient.dio.get('/households/me');

      return HouseholdResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<HouseholdResponseDTO> addMember(AddMemberRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/households/members',
        data: requestDTO.toJson(),
      );

      return HouseholdResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<List<HouseholdMemberResponseDTO>> getHouseholdMembers() async {
    try {
      final response = await apiClient.dio.get('/households/members');

      final List<dynamic> data = response.data;
      return data.map((json) => HouseholdMemberResponseDTO.fromJson(json)).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<HouseholdResponseDTO> removeMember(String memberId) async {
    try {
      final response = await apiClient.dio.delete('/households/members/$memberId');

      return HouseholdResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> leaveHousehold() async {
    try {
      await apiClient.dio.delete('/households/me');
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<HouseholdInvitationCodeResponseDTO> getInvitationCode() async {
    try {
      final response = await apiClient.dio.get('/households/invitation-code');

      return HouseholdInvitationCodeResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<HouseholdInvitationCodeResponseDTO> refreshInvitationCode() async {
    try {
      final response = await apiClient.dio.post('/households/invitation-code/refresh');

      return HouseholdInvitationCodeResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
