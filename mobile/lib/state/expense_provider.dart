import 'package:flutter/material.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../services/expense_service.dart';
import '../services/debt_service.dart';
import '../services/settlement_service.dart';
import '../shared/dto/expense/response/expense_overview_response_dto.dart';
import '../shared/dto/expense/response/debt_response_dto.dart';
import '../shared/dto/expense/response/expense_response_dto.dart';
import '../shared/dto/expense/response/settlement_response_dto.dart';
import '../shared/dto/expense/response/user_net_overview_response_dto.dart';
import '../shared/dto/expense/request/transaction_filter_request_dto.dart';
import '../shared/dto/expense/request/debt_filter_request_dto.dart';
import '../shared/enums/user_transaction_role.dart';

class ExpenseProvider extends ChangeNotifier {
  final ExpenseService _expenseService = ExpenseService(ApiClient());
  final DebtService _debtService = DebtService(ApiClient());
  final SettlementService _settlementService = SettlementService(ApiClient());

  ExpenseOverviewResponseDTO? _overview;
  UserNetOverviewResponseDTO? _userNetOverview;
  List<DebtResponseDTO> _debts = [];
  List<ExpenseResponseDTO> _recentExpenses = [];
  List<SettlementResponseDTO> _recentSettlements = [];
  bool _isLoading = false;
  String? _errorMessage;

  ExpenseOverviewResponseDTO? get overview => _overview;
  UserNetOverviewResponseDTO? get userNetOverview => _userNetOverview;
  List<DebtResponseDTO> get debts => _debts;
  List<ExpenseResponseDTO> get recentExpenses => _recentExpenses;
  List<SettlementResponseDTO> get recentSettlements => _recentSettlements;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  Future<void> loadExpenseDashboard() async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      // Fetch overview, debts, and recent expenses in parallel
      final results = await Future.wait([
        _expenseService.getCurrentMonthExpenseOverview(),
        _debtService.getFilteredDebts(DebtFilterRequestDTO(userTransactionRole: UserTransactionRole.all)),
        _expenseService.getFilteredExpenses(TransactionFilterRequestDTO(userTransactionRole: UserTransactionRole.all)),
        _expenseService.getCurrentMonthUserNetOverview(),
        _settlementService.getFilteredSettlements(TransactionFilterRequestDTO(userTransactionRole: UserTransactionRole.all)),
      ]);

      _overview = results[0] as ExpenseOverviewResponseDTO;
      _debts = results[1] as List<DebtResponseDTO>;
      _recentExpenses = results[2] as List<ExpenseResponseDTO>;
      _userNetOverview = results[3] as UserNetOverviewResponseDTO;
      _recentSettlements = results[4] as List<SettlementResponseDTO>;

    } on ApiException catch (e) {
      _errorMessage = e.message;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching expense data.";
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
