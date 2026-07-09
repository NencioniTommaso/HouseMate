import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../shared/dto/chore/request/chore_assignment_create_request_dto.dart';
import '../shared/dto/chore/request/chore_assignment_filter_request_dto.dart';
import '../shared/dto/chore/request/chore_create_request_dto.dart';
import '../shared/dto/chore/request/chore_reassign_request_dto.dart';
import '../shared/dto/chore/request/chore_status_update_request_dto.dart';
import '../shared/dto/chore/response/assignment_overview_dto.dart';
import '../shared/dto/chore/response/chore_assignment_response_dto.dart';
import '../shared/dto/chore/response/chore_response_dto.dart';

class ChoreService {
  final ApiClient apiClient;

  ChoreService(this.apiClient);

  Future<ChoreResponseDTO> createChore(ChoreCreateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/chores',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 201) {
        return ChoreResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to create chore. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to create chore. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<void> deleteChore(String choreId) async {
    try {
      final response = await apiClient.dio.delete('/chores/$choreId');

      if (response.statusCode != 204) {
        throw Exception(
            'Failed to delete chore. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to delete chore. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<ChoreAssignmentResponseDTO> createAssignment(
      ChoreAssignmentCreateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/chores/assignments',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 201) {
        return ChoreAssignmentResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to create chore assignment. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to create chore assignment. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<void> deleteChoreAssignment(String assignmentId) async {
    try {
      final response = await apiClient.dio.delete('/chores/assignments/$assignmentId');

      if (response.statusCode != 204) {
        throw Exception(
            'Failed to delete chore assignment. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to delete chore assignment. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<void> updateChoreAssignmentStatus(
      String assignmentId, ChoreStatusUpdateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.patch(
        '/chores/assignments/$assignmentId/status',
        data: requestDTO.toJson(),
      );

      if (response.statusCode != 204) {
        throw Exception(
            'Failed to update chore assignment status. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to update chore assignment status. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<ChoreAssignmentResponseDTO> reassignChore(
      String assignmentId, ChoreReassignRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.patch(
        '/chores/assignments/$assignmentId/reassign',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 200) {
        return ChoreAssignmentResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to reassign chore. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to reassign chore. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<List<ChoreAssignmentResponseDTO>> getFilteredChoreAssignments(
      ChoreAssignmentFilterRequestDTO requestDTO) async {
    try {
      final queryParams = <String, dynamic>{};

      if (requestDTO.statuses != null && requestDTO.statuses!.isNotEmpty) {
        queryParams['statuses'] = requestDTO.statuses!.map((s) => s.name).toList();
      }

      if (requestDTO.assigneeId != null) {
        queryParams['assigneeId'] = requestDTO.assigneeId;
      }

      if (requestDTO.descriptionContains != null &&
          requestDTO.descriptionContains!.isNotEmpty) {
        queryParams['descriptionContains'] = requestDTO.descriptionContains;
      }

      if (requestDTO.dateRange.startDate != null) {
        queryParams['dateRange.startDate'] =
            requestDTO.dateRange.startDate!.toIso8601String();
      }

      if (requestDTO.dateRange.endDate != null) {
        queryParams['dateRange.endDate'] =
            requestDTO.dateRange.endDate!.toIso8601String();
      }

      final response = await apiClient.dio.get(
        '/chores/assignments',
        queryParameters: queryParams,
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = response.data;
        return data.map((json) => ChoreAssignmentResponseDTO.fromJson(json)).toList();
      } else {
        throw Exception(
            'Failed to get filtered chore assignments. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to get filtered chore assignments. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<List<ChoreResponseDTO>> getAllHouseholdChores() async {
    try {
      final response = await apiClient.dio.get('/chores');

      if (response.statusCode == 200) {
        final List<dynamic> data = response.data;
        return data.map((json) => ChoreResponseDTO.fromJson(json)).toList();
      } else {
        throw Exception(
            'Failed to get household chores. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to get household chores. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<AssignmentOverviewDTO> getHouseholdAssignmentOverview() async {
    try {
      final response = await apiClient.dio.get('/chores/assignments/overview');

      if (response.statusCode == 200) {
        return AssignmentOverviewDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to get household assignment overview. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to get household assignment overview. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<AssignmentOverviewDTO> getUserAssignmentOverview() async {
    try {
      final response = await apiClient.dio.get('/chores/assignments/me');

      if (response.statusCode == 200) {
        return AssignmentOverviewDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to get user assignment overview. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to get user assignment overview. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }
}
