import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../shared/dto/expense/request/expense_create_request.dart';
import '../shared/dto/expense/request/transaction_filter_request_dto.dart';
import '../shared/dto/expense/response/expense_overview_response_dto.dart';
import '../shared/dto/expense/response/expense_response_dto.dart';
import '../shared/dto/expense/response/user_net_overview_response_dto.dart';

class ExpenseService {
  final ApiClient apiClient;

  ExpenseService(this.apiClient);

  Future<ExpenseResponseDTO> createExpense(ExpenseCreateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/expenses',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 201) {
        return ExpenseResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to create expense. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to create expense. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<List<ExpenseResponseDTO>> getFilteredExpenses(
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
        '/expenses',
        queryParameters: queryParams,
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = response.data;
        return data.map((json) => ExpenseResponseDTO.fromJson(json)).toList();
      } else {
        throw Exception(
            'Failed to retrieve filtered expenses. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve filtered expenses. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<ExpenseOverviewResponseDTO> getCurrentMonthExpenseOverview() async {
    try {
      final response = await apiClient.dio.get('/expenses/overview');

      if (response.statusCode == 200) {
        return ExpenseOverviewResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to retrieve current month expense overview. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve current month expense overview. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<UserNetOverviewResponseDTO> getCurrentMonthUserNetOverview() async {
    try {
      final response = await apiClient.dio.get('/expenses/me');

      if (response.statusCode == 200) {
        return UserNetOverviewResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to retrieve current month user net overview. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve current month user net overview. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }
}
