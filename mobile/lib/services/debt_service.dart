import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../shared/dto/expense/request/debt_filter_request_dto.dart';
import '../shared/dto/expense/response/debt_overview_response_dto.dart';
import '../shared/dto/expense/response/debt_response_dto.dart';

class DebtService {
  final ApiClient apiClient;

  DebtService(this.apiClient);

  Future<List<DebtResponseDTO>> getFilteredDebts(DebtFilterRequestDTO filterDTO) async {
    try {
      final queryParams = <String, dynamic>{};
      queryParams['userTransactionRole'] = filterDTO.userTransactionRole.name;
      if (filterDTO.involvedId != null) {
        queryParams['involvedId'] = filterDTO.involvedId;
      }

      final response = await apiClient.dio.get(
        '/debts',
        queryParameters: queryParams,
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = response.data;
        return data.map((json) => DebtResponseDTO.fromJson(json)).toList();
      } else {
        throw Exception(
            'Failed to retrieve filtered debts. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve filtered debts. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<DebtOverviewResponseDTO> getCurrentUserDebtOverview() async {
    try {
      final response = await apiClient.dio.get('/debts/me');

      if (response.statusCode == 200) {
        return DebtOverviewResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to retrieve debt overview. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve debt overview. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<void> deleteDebt(String debtId) async {
    try {
      final response = await apiClient.dio.delete('/debts/$debtId');

      if (response.statusCode != 204) {
        throw Exception(
            'Failed to delete debt. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to delete debt. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }
}
