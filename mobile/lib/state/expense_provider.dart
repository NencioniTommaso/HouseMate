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
import '../shared/dto/expense/request/expense_create_request.dart';
import '../shared/dto/expense/request/settlement_create_request_dto.dart';
import '../shared/enums/user_transaction_role.dart';
import '../shared/utils/types/date_range.dart';

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

      // Providing a wide DateRange for "Recent Activity" and Settlements
      final now = DateTime.now();
      final defaultRange = DateRange(
        startDate: DateTime(now.year, now.month - 1, 1),
        endDate: now.add(const Duration(days: 1)),
      );

      // Fetch overview, debts, and recent expenses in parallel
      final results = await Future.wait([
        _expenseService.getCurrentMonthExpenseOverview(),
        _debtService.getFilteredDebts(DebtFilterRequestDTO(userTransactionRole: UserTransactionRole.all)),
        _expenseService.getFilteredExpenses(TransactionFilterRequestDTO(
          userTransactionRole: UserTransactionRole.all,
          dateRange: defaultRange,
        )),
        _expenseService.getCurrentMonthUserNetOverview(),
        _settlementService.getFilteredSettlements(TransactionFilterRequestDTO(
          userTransactionRole: UserTransactionRole.all,
          dateRange: defaultRange,
        )),
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

  Future<void> loadFilteredExpenses(TransactionFilterRequestDTO filter) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      _recentExpenses = await _expenseService.getFilteredExpenses(filter);
    } on ApiException catch (e) {
      _errorMessage = e.message;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while filtering expenses.";
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadFilteredSettlements(TransactionFilterRequestDTO filter) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      _recentSettlements = await _settlementService.getFilteredSettlements(filter);
    } on ApiException catch (e) {
      _errorMessage = e.message;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while filtering settlements.";
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> createExpense(ExpenseCreateRequestDTO request) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      await _expenseService.createExpense(request);
      await loadExpenseDashboard();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while creating expense.";
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> settleDebt(String debtId, String creditorId, double amount) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      final request = SettlementCreateRequestDTO(
        debtId: debtId,
        creditorId: creditorId,
        amount: amount,
      );
      await _settlementService.settleDebt(debtId, request);
      await loadExpenseDashboard();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while settling debt.";
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void clear() {
    _overview = null;
    _userNetOverview = null;
    _debts = [];
    _recentExpenses = [];
    _recentSettlements = [];
    _isLoading = false;
    _errorMessage = null;
    notifyListeners();
  }
}
