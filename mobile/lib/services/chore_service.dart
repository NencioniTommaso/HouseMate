import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
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

      return ChoreResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> deleteChore(String choreId) async {
    try {
      await apiClient.dio.delete('/chores/$choreId');
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<ChoreAssignmentResponseDTO> createAssignment(
      ChoreAssignmentCreateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/chores/assignments',
        data: requestDTO.toJson(),
      );

      return ChoreAssignmentResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> deleteChoreAssignment(String assignmentId) async {
    try {
      await apiClient.dio.delete('/chores/assignments/$assignmentId');
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> updateChoreAssignmentStatus(
      String assignmentId, ChoreStatusUpdateRequestDTO requestDTO) async {
    try {
      await apiClient.dio.patch(
        '/chores/assignments/$assignmentId/status',
        data: requestDTO.toJson(),
      );
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<ChoreAssignmentResponseDTO> reassignChore(
      String assignmentId, ChoreReassignRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.patch(
        '/chores/assignments/$assignmentId/reassign',
        data: requestDTO.toJson(),
      );

      return ChoreAssignmentResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<List<ChoreAssignmentResponseDTO>> getFilteredChoreAssignments(
      ChoreAssignmentFilterRequestDTO requestDTO) async {
    try {
      final queryParams = <String, dynamic>{};

      if (requestDTO.statuses != null && requestDTO.statuses!.isNotEmpty) {
        queryParams['statuses'] = requestDTO.statuses!.map((s) => s.name.toUpperCase()).toList();
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

      final List<dynamic> data = response.data;
      return data.map((json) => ChoreAssignmentResponseDTO.fromJson(json)).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<List<ChoreResponseDTO>> getAllHouseholdChores() async {
    try {
      final response = await apiClient.dio.get('/chores');

      final List<dynamic> data = response.data;
      return data.map((json) => ChoreResponseDTO.fromJson(json)).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<AssignmentOverviewDTO> getHouseholdAssignmentOverview() async {
    try {
      final response = await apiClient.dio.get('/chores/assignments/overview');

      return AssignmentOverviewDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<AssignmentOverviewDTO> getUserAssignmentOverview() async {
    try {
      final response = await apiClient.dio.get('/chores/assignments/me');

      return AssignmentOverviewDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
