import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../shared/dto/expense/request/settlement_create_request_dto.dart';
import '../shared/dto/expense/request/transaction_filter_request_dto.dart';
import '../shared/dto/expense/response/settlement_response_dto.dart';

class SettlementService {
  final ApiClient apiClient;

  SettlementService(this.apiClient);

  Future<SettlementResponseDTO> settleDebt(
      String debtId, SettlementCreateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/settlements/$debtId',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 201) {
        return SettlementResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to settle debt. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to settle debt. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<List<SettlementResponseDTO>> getFilteredSettlements(
      TransactionFilterRequestDTO filterDTO) async {
    try {
      final queryParams = <String, dynamic>{};

      if (filterDTO.householdId != null) {
        queryParams['householdId'] = filterDTO.householdId;
      }

      if (filterDTO.userTransactionRole != null) {
        queryParams['userTransactionRole'] = filterDTO.userTransactionRole!.name;
      }

      if (filterDTO.description != null && filterDTO.description!.isNotEmpty) {
        queryParams['description'] = filterDTO.description;
      }

      if (filterDTO.dateRange != null) {
        if (filterDTO.dateRange!.startDate != null) {
          queryParams['dateRange.startDate'] =
              filterDTO.dateRange!.startDate!.toIso8601String();
        }
        if (filterDTO.dateRange!.endDate != null) {
          queryParams['dateRange.endDate'] =
              filterDTO.dateRange!.endDate!.toIso8601String();
        }
      }

      final response = await apiClient.dio.get(
        '/settlements',
        queryParameters: queryParams,
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = response.data;
        return data.map((json) => SettlementResponseDTO.fromJson(json)).toList();
      } else {
        throw Exception(
            'Failed to retrieve filtered settlements. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve filtered settlements. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }
}
