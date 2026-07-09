import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
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

      if (response.statusCode == 201) {
        return HouseholdResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to create household. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to create household. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<HouseholdResponseDTO> getCurrentUserHousehold() async {
    try {
      final response = await apiClient.dio.get('/households/me');

      if (response.statusCode == 200) {
        return HouseholdResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to retrieve current household. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve current household. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<HouseholdResponseDTO> addMember(AddMemberRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/households/members',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 200) {
        return HouseholdResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to join household using invitation code. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to join household using invitation code. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<List<HouseholdMemberResponseDTO>> getHouseholdMembers() async {
    try {
      final response = await apiClient.dio.get('/households/members');

      if (response.statusCode == 200) {
        final List<dynamic> data = response.data;
        return data.map((json) => HouseholdMemberResponseDTO.fromJson(json)).toList();
      } else {
        throw Exception(
            'Failed to retrieve household members. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve household members. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<HouseholdResponseDTO> removeMember(String memberId) async {
    try {
      final response = await apiClient.dio.delete('/households/members/$memberId');

      if (response.statusCode == 200) {
        return HouseholdResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to remove household member. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to remove household member. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<void> leaveHousehold() async {
    try {
      final response = await apiClient.dio.delete('/households/me');

      if (response.statusCode != 204) {
        throw Exception(
            'Failed to leave household. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to leave household. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<HouseholdInvitationCodeResponseDTO> getInvitationCode() async {
    try {
      final response = await apiClient.dio.get('/households/invitation-code');

      if (response.statusCode == 200) {
        return HouseholdInvitationCodeResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to retrieve household invitation code. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve household invitation code. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<HouseholdInvitationCodeResponseDTO> refreshInvitationCode() async {
    try {
      final response = await apiClient.dio.post('/households/invitation-code/refresh');

      if (response.statusCode == 200) {
        return HouseholdInvitationCodeResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to refresh household invitation code. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to refresh household invitation code. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }
}
