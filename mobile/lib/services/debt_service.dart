import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../shared/dto/expense/request/debt_filter_request_dto.dart';
import '../shared/dto/expense/response/debt_overview_response_dto.dart';
import '../shared/dto/expense/response/debt_response_dto.dart';

class DebtService {
  final ApiClient apiClient;

  DebtService(this.apiClient);

  Future<List<DebtResponseDTO>> getFilteredDebts(DebtFilterRequestDTO filterDTO) async {
    try {
      final queryParams = <String, dynamic>{};
      queryParams['userTransactionRole'] = filterDTO.userTransactionRole.name.toUpperCase();
      if (filterDTO.involvedId != null) {
        queryParams['involvedId'] = filterDTO.involvedId;
      }

      final response = await apiClient.dio.get(
        '/debts',
        queryParameters: queryParams,
      );

      final List<dynamic> data = response.data;
      return data.map((json) => DebtResponseDTO.fromJson(json)).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<DebtOverviewResponseDTO> getCurrentUserDebtOverview() async {
    try {
      final response = await apiClient.dio.get('/debts/me');

      return DebtOverviewResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> deleteDebt(String debtId) async {
    try {
      await apiClient.dio.delete('/debts/$debtId');
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
