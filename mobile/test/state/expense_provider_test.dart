import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/debt_service.dart';
import 'package:mobile/services/expense_service.dart';
import 'package:mobile/services/settlement_service.dart';
import 'package:mobile/state/expense_provider.dart';
import 'package:mobile/shared/dto/expense/request/debt_filter_request_dto.dart';
import 'package:mobile/shared/dto/expense/request/expense_create_request.dart';
import 'package:mobile/shared/dto/expense/request/settlement_create_request_dto.dart';
import 'package:mobile/shared/dto/expense/request/transaction_filter_request_dto.dart';
import 'package:mobile/shared/dto/expense/response/debt_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/expense_overview_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/expense_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/settlement_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/user_net_overview_response_dto.dart';
import 'package:mobile/shared/enums/expense_split_type.dart';
import 'package:mobile/shared/enums/user_transaction_role.dart';

import '../services/test_helpers.dart';
import 'state_test_helpers.dart';

class FakeExpenseService extends ExpenseService {
  FakeExpenseService(super.apiClient);

  ExpenseOverviewResponseDTO mockOverview = ExpenseOverviewResponseDTO(
    totalAmount: 150.0,
    expenseCount: 5,
  );
  UserNetOverviewResponseDTO mockUserNetOverview = UserNetOverviewResponseDTO(
    actualCashFlowAmount: 25.5,
  );
  List<ExpenseResponseDTO> mockExpenses = [];
  ExpenseCreateRequestDTO? lastCreateExpenseRequest;
  TransactionFilterRequestDTO? lastFilterRequest;
  Exception? errorToThrow;

  @override
  Future<ExpenseOverviewResponseDTO> getCurrentMonthExpenseOverview() async {
    if (errorToThrow != null) throw errorToThrow!;
    return mockOverview;
  }

  @override
  Future<UserNetOverviewResponseDTO> getCurrentMonthUserNetOverview() async {
    if (errorToThrow != null) throw errorToThrow!;
    return mockUserNetOverview;
  }

  @override
  Future<List<ExpenseResponseDTO>> getFilteredExpenses(
    TransactionFilterRequestDTO requestDTO,
  ) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastFilterRequest = requestDTO;
    return mockExpenses;
  }

  @override
  Future<ExpenseResponseDTO> createExpense(ExpenseCreateRequestDTO requestDTO) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastCreateExpenseRequest = requestDTO;
    return ExpenseResponseDTO(
      id: 'exp-created-1',
      description: requestDTO.description,
      amount: requestDTO.amount,
      payerId: 'u1',
      payerFullName: 'Alice Smith',
      splitType: requestDTO.splitType,
      householdId: 'h1',
      shares: [],
    );
  }
}

class FakeDebtService extends DebtService {
  FakeDebtService(super.apiClient);

  List<DebtResponseDTO> mockDebts = [];
  DebtFilterRequestDTO? lastFilterRequest;
  Exception? errorToThrow;

  @override
  Future<List<DebtResponseDTO>> getFilteredDebts(DebtFilterRequestDTO requestDTO) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastFilterRequest = requestDTO;
    return mockDebts;
  }
}

class FakeSettlementService extends SettlementService {
  FakeSettlementService(super.apiClient);

  List<SettlementResponseDTO> mockSettlements = [];
  TransactionFilterRequestDTO? lastFilterRequest;
  String? lastSettleDebtId;
  SettlementCreateRequestDTO? lastSettleRequest;
  Exception? errorToThrow;

  @override
  Future<List<SettlementResponseDTO>> getFilteredSettlements(
    TransactionFilterRequestDTO requestDTO,
  ) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastFilterRequest = requestDTO;
    return mockSettlements;
  }

  @override
  Future<SettlementResponseDTO> settleDebt(
    String debtId,
    SettlementCreateRequestDTO requestDTO,
  ) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastSettleDebtId = debtId;
    lastSettleRequest = requestDTO;
    return SettlementResponseDTO(
      settlementId: 'settle-1',
      userTransactionRole: UserTransactionRole.creditor,
      involvedId: requestDTO.creditorId,
      involvedName: 'Bob',
      amount: requestDTO.amount,
      householdId: 'h1',
    );
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late TestContext context;
  late FakeUiService uiService;
  late FakeExpenseService fakeExpenseService;
  late FakeDebtService fakeDebtService;
  late FakeSettlementService fakeSettlementService;
  late ExpenseProvider expenseProvider;

  final sampleExpense = ExpenseResponseDTO(
    id: 'e1',
    description: 'Groceries',
    amount: 50.0,
    payerId: 'u1',
    payerFullName: 'Alice',
    splitType: ExpenseSplitType.equalSplit,
    householdId: 'h1',
    shares: [],
  );

  final sampleDebt = DebtResponseDTO(
    debtId: 'd1',
    userTransactionRole: UserTransactionRole.debtor,
    involvedId: 'u2',
    involvedName: 'Bob',
    amount: 25.0,
  );

  final sampleSettlement = SettlementResponseDTO(
    settlementId: 's1',
    userTransactionRole: UserTransactionRole.creditor,
    involvedId: 'u2',
    involvedName: 'Bob',
    amount: 15.0,
    householdId: 'h1',
  );

  setUp(() {
    context = createTestContext();
    uiService = FakeUiService();
    fakeExpenseService = FakeExpenseService(context.apiClient);
    fakeDebtService = FakeDebtService(context.apiClient);
    fakeSettlementService = FakeSettlementService(context.apiClient);

    fakeExpenseService.mockExpenses = [sampleExpense];
    fakeDebtService.mockDebts = [sampleDebt];
    fakeSettlementService.mockSettlements = [sampleSettlement];

    expenseProvider = ExpenseProvider(
      apiClient: context.apiClient,
      uiService: uiService,
      expenseService: fakeExpenseService,
      debtService: fakeDebtService,
      settlementService: fakeSettlementService,
    );
  });

  group('ExpenseProvider - Initial State', () {
    test('initial getters return empty/null defaults', () {
      expect(expenseProvider.overview, isNull);
      expect(expenseProvider.userNetOverview, isNull);
      expect(expenseProvider.debts, isEmpty);
      expect(expenseProvider.recentExpenses, isEmpty);
      expect(expenseProvider.recentSettlements, isEmpty);
      expect(expenseProvider.isLoading, false);
      expect(expenseProvider.errorMessage, isNull);
    });
  });

  group('ExpenseProvider - loadExpenseDashboard', () {
    test('fetches all 5 dashboard resources in parallel and populates state', () async {
      final loadingStates = <bool>[];
      expenseProvider.addListener(() {
        loadingStates.add(expenseProvider.isLoading);
      });

      await expenseProvider.loadExpenseDashboard();

      expect(expenseProvider.overview, isNotNull);
      expect(expenseProvider.overview!.totalAmount, 150.0);
      expect(expenseProvider.userNetOverview, isNotNull);
      expect(expenseProvider.userNetOverview!.actualCashFlowAmount, 25.5);
      expect(expenseProvider.debts.length, 1);
      expect(expenseProvider.debts.first.debtId, 'd1');
      expect(expenseProvider.recentExpenses.length, 1);
      expect(expenseProvider.recentExpenses.first.description, 'Groceries');
      expect(expenseProvider.recentSettlements.length, 1);
      expect(expenseProvider.recentSettlements.first.settlementId, 's1');
      expect(expenseProvider.isLoading, false);
      expect(expenseProvider.errorMessage, isNull);
      expect(loadingStates, contains(true));
    });

    test('handles ApiException when dashboard loading fails', () async {
      fakeExpenseService.errorToThrow = ApiException(
        message: 'Unauthorized expense access',
        statusCode: 403,
      );

      await expenseProvider.loadExpenseDashboard();

      expect(expenseProvider.isLoading, false);
      expect(expenseProvider.errorMessage, 'Unauthorized expense access');
      expect(uiService.errorMessages, contains('Unauthorized expense access'));
    });

    test('handles generic unexpected error when dashboard loading fails', () async {
      fakeDebtService.errorToThrow = Exception('Connection reset by peer');

      await expenseProvider.loadExpenseDashboard();

      expect(expenseProvider.isLoading, false);
      expect(
        expenseProvider.errorMessage,
        'An unexpected error occurred while fetching expense data.',
      );
      expect(
        uiService.errorMessages,
        contains('An unexpected error occurred while fetching expense data.'),
      );
    });
  });

  group('ExpenseProvider - loadFilteredExpenses', () {
    final filter = TransactionFilterRequestDTO(
      userTransactionRole: UserTransactionRole.creditor,
    );

    test('populates recentExpenses with filtered results', () async {
      await expenseProvider.loadFilteredExpenses(filter);

      expect(expenseProvider.recentExpenses.length, 1);
      expect(expenseProvider.recentExpenses.first.id, 'e1');
      expect(fakeExpenseService.lastFilterRequest?.userTransactionRole, UserTransactionRole.creditor);
      expect(expenseProvider.isLoading, false);
      expect(expenseProvider.errorMessage, isNull);
    });

    test('handles ApiException in loadFilteredExpenses', () async {
      fakeExpenseService.errorToThrow = ApiException(message: 'Invalid filter range');

      await expenseProvider.loadFilteredExpenses(filter);

      expect(expenseProvider.isLoading, false);
      expect(expenseProvider.errorMessage, 'Invalid filter range');
      expect(uiService.errorMessages, contains('Invalid filter range'));
    });

    test('handles generic error in loadFilteredExpenses', () async {
      fakeExpenseService.errorToThrow = Exception('Database down');

      await expenseProvider.loadFilteredExpenses(filter);

      expect(expenseProvider.isLoading, false);
      expect(
        expenseProvider.errorMessage,
        'An unexpected error occurred while filtering expenses.',
      );
      expect(
        uiService.errorMessages,
        contains('An unexpected error occurred while filtering expenses.'),
      );
    });
  });

  group('ExpenseProvider - loadFilteredSettlements', () {
    final filter = TransactionFilterRequestDTO(
      userTransactionRole: UserTransactionRole.all,
    );

    test('populates recentSettlements with filtered results', () async {
      await expenseProvider.loadFilteredSettlements(filter);

      expect(expenseProvider.recentSettlements.length, 1);
      expect(expenseProvider.recentSettlements.first.settlementId, 's1');
      expect(fakeSettlementService.lastFilterRequest?.userTransactionRole, UserTransactionRole.all);
      expect(expenseProvider.isLoading, false);
      expect(expenseProvider.errorMessage, isNull);
    });

    test('handles ApiException in loadFilteredSettlements', () async {
      fakeSettlementService.errorToThrow = ApiException(message: 'Settlements inaccessible');

      await expenseProvider.loadFilteredSettlements(filter);

      expect(expenseProvider.isLoading, false);
      expect(expenseProvider.errorMessage, 'Settlements inaccessible');
      expect(uiService.errorMessages, contains('Settlements inaccessible'));
    });

    test('handles generic error in loadFilteredSettlements', () async {
      fakeSettlementService.errorToThrow = Exception('Settlement service down');

      await expenseProvider.loadFilteredSettlements(filter);

      expect(expenseProvider.isLoading, false);
      expect(
        expenseProvider.errorMessage,
        'An unexpected error occurred while filtering settlements.',
      );
      expect(
        uiService.errorMessages,
        contains('An unexpected error occurred while filtering settlements.'),
      );
    });
  });

  group('ExpenseProvider - createExpense', () {
    final createReq = ExpenseCreateRequestDTO(
      description: 'Dinner',
      amount: 40.0,
      splitType: ExpenseSplitType.equalSplit,
      shares: [],
    );

    test('successfully creates expense and reloads dashboard', () async {
      final success = await expenseProvider.createExpense(createReq);

      expect(success, true);
      expect(fakeExpenseService.lastCreateExpenseRequest?.description, 'Dinner');
      expect(fakeExpenseService.lastCreateExpenseRequest?.amount, 40.0);
      expect(uiService.successMessages, contains('Expense created successfully'));
      expect(expenseProvider.overview, isNotNull);
      expect(expenseProvider.isLoading, false);
    });

    test('handles ApiException on createExpense', () async {
      fakeExpenseService.errorToThrow = ApiException(message: 'Invalid split shares');

      final success = await expenseProvider.createExpense(createReq);

      expect(success, false);
      expect(expenseProvider.errorMessage, 'Invalid split shares');
      expect(uiService.errorMessages, contains('Invalid split shares'));
      expect(expenseProvider.isLoading, false);
    });

    test('handles generic error on createExpense', () async {
      fakeExpenseService.errorToThrow = Exception('Creation failed');

      final success = await expenseProvider.createExpense(createReq);

      expect(success, false);
      expect(
        expenseProvider.errorMessage,
        'An unexpected error occurred while creating expense.',
      );
      expect(
        uiService.errorMessages,
        contains('An unexpected error occurred while creating expense.'),
      );
    });
  });

  group('ExpenseProvider - settleDebt', () {
    test('successfully settles debt and reloads dashboard', () async {
      final success = await expenseProvider.settleDebt('d1', 'u2', 25.0);

      expect(success, true);
      expect(fakeSettlementService.lastSettleDebtId, 'd1');
      expect(fakeSettlementService.lastSettleRequest?.debtId, 'd1');
      expect(fakeSettlementService.lastSettleRequest?.creditorId, 'u2');
      expect(fakeSettlementService.lastSettleRequest?.amount, 25.0);
      expect(uiService.successMessages, contains('Debt settled successfully'));
      expect(expenseProvider.overview, isNotNull);
      expect(expenseProvider.isLoading, false);
    });

    test('handles ApiException on settleDebt', () async {
      fakeSettlementService.errorToThrow = ApiException(message: 'Debt already settled');

      final success = await expenseProvider.settleDebt('d1', 'u2', 25.0);

      expect(success, false);
      expect(expenseProvider.errorMessage, 'Debt already settled');
      expect(uiService.errorMessages, contains('Debt already settled'));
      expect(expenseProvider.isLoading, false);
    });

    test('handles generic error on settleDebt', () async {
      fakeSettlementService.errorToThrow = Exception('Bank gateway error');

      final success = await expenseProvider.settleDebt('d1', 'u2', 25.0);

      expect(success, false);
      expect(
        expenseProvider.errorMessage,
        'An unexpected error occurred while settling debt.',
      );
      expect(
        uiService.errorMessages,
        contains('An unexpected error occurred while settling debt.'),
      );
    });
  });

  group('ExpenseProvider - clear', () {
    test('resets all state variables and notifies listeners', () async {
      await expenseProvider.loadExpenseDashboard();
      expect(expenseProvider.overview, isNotNull);
      expect(expenseProvider.debts, isNotEmpty);

      var notified = false;
      expenseProvider.addListener(() {
        notified = true;
      });

      expenseProvider.clear();

      expect(expenseProvider.overview, isNull);
      expect(expenseProvider.userNetOverview, isNull);
      expect(expenseProvider.debts, isEmpty);
      expect(expenseProvider.recentExpenses, isEmpty);
      expect(expenseProvider.recentSettlements, isEmpty);
      expect(expenseProvider.isLoading, false);
      expect(expenseProvider.errorMessage, isNull);
      expect(notified, true);
    });
  });
}
