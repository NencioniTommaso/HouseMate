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

import '../core/utils/ui_service.dart';

class ExpenseProvider extends ChangeNotifier {
  final ExpenseService _expenseService;
  final DebtService _debtService;
  final SettlementService _settlementService;
  final UiService _uiService;

  ExpenseOverviewResponseDTO? _overview;
  UserNetOverviewResponseDTO? _userNetOverview;
  List<DebtResponseDTO> _debts = [];
  List<ExpenseResponseDTO> _recentExpenses = [];
  List<SettlementResponseDTO> _recentSettlements = [];
  bool _isLoading = false;
  String? _errorMessage;

  ExpenseProvider({required ApiClient apiClient, required UiService uiService})
      : _expenseService = ExpenseService(apiClient),
        _debtService = DebtService(apiClient),
        _settlementService = SettlementService(apiClient),
        _uiService = uiService;

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
      _uiService.showError(e.message);
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching expense data.";
      _uiService.showError(_errorMessage!);
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
      _uiService.showError(e.message);
    } catch (e) {
      _errorMessage = "An unexpected error occurred while filtering expenses.";
      _uiService.showError(_errorMessage!);
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
      _uiService.showError(e.message);
    } catch (e) {
      _errorMessage = "An unexpected error occurred while filtering settlements.";
      _uiService.showError(_errorMessage!);
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
      _uiService.showSuccess("Expense created successfully");
      await loadExpenseDashboard();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while creating expense.";
      _uiService.showError(_errorMessage!);
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
      _uiService.showSuccess("Debt settled successfully");
      await loadExpenseDashboard();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      _uiService.showError(e.message);
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while settling debt.";
      _uiService.showError(_errorMessage!);
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
