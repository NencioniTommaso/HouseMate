import 'dart:async';
import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/utils/ui_service.dart';
import 'package:mobile/main.dart';
import 'package:mobile/state/auth_provider.dart';
import 'package:mobile/state/chore_provider.dart';
import 'package:mobile/state/expense_provider.dart';
import 'package:mobile/state/household_provider.dart';
import 'package:mobile/state/user_provider.dart';
import 'package:provider/provider.dart';

import '../services/test_helpers.dart';
import '../state/state_test_helpers.dart';

class MockState {
  Map<String, dynamic> user;
  Map<String, dynamic>? household;
  List<Map<String, dynamic>> chores;
  List<Map<String, dynamic>> assignments;
  Map<String, dynamic> choreOverview;
  List<Map<String, dynamic>> expenses;
  Map<String, dynamic> expenseOverview;
  Map<String, dynamic> userNetOverview;
  List<Map<String, dynamic>> debts;
  List<Map<String, dynamic>> settlements;
  List<Map<String, dynamic>> shoppingLists;

  MockState({
    Map<String, dynamic>? user,
    this.household,
    List<Map<String, dynamic>>? chores,
    List<Map<String, dynamic>>? assignments,
    Map<String, dynamic>? choreOverview,
    List<Map<String, dynamic>>? expenses,
    Map<String, dynamic>? expenseOverview,
    Map<String, dynamic>? userNetOverview,
    List<Map<String, dynamic>>? debts,
    List<Map<String, dynamic>>? settlements,
    List<Map<String, dynamic>>? shoppingLists,
  })  : user = user ?? defaultUser(),
        chores = chores ?? defaultChores(),
        assignments = assignments ?? defaultAssignments(),
        choreOverview = choreOverview ?? defaultChoreOverview(),
        expenses = expenses ?? defaultExpenses(),
        expenseOverview = expenseOverview ?? defaultExpenseOverview(),
        userNetOverview = userNetOverview ?? defaultUserNetOverview(),
        debts = debts ?? defaultDebts(),
        settlements = settlements ?? [],
        shoppingLists = shoppingLists ?? defaultShoppingLists();

  static Map<String, dynamic> defaultUser() => {
        'id': 'u-1',
        'name': 'Alice',
        'surname': 'Smith',
        'email': 'alice@example.com',
        'iban': 'DE89370400440532013000',
        'paymentLink': 'https://paypal.me/alice',
      };

  static Map<String, dynamic> defaultHousehold() => {
        'id': 'h-1',
        'name': 'The Cool Apartment',
        'creationDate': '2026-01-01T00:00:00.000Z',
        'memberships': [
          {
            'user': {
              'id': 'u-1',
              'name': 'Alice',
              'surname': 'Smith',
              'email': 'alice@example.com',
              'iban': 'DE89370400440532013000',
              'paymentLink': 'https://paypal.me/alice',
            },
            'membership': {
              'isAdmin': true,
              'date': '2026-01-01T00:00:00.000Z',
            },
          },
          {
            'user': {
              'id': 'u-2',
              'name': 'Bob',
              'surname': 'Jones',
              'email': 'bob@example.com',
              'iban': 'DE89370400440532013001',
              'paymentLink': 'https://paypal.me/bobjones',
            },
            'membership': {
              'isAdmin': false,
              'date': '2026-01-02T00:00:00.000Z',
            },
          },
        ],
      };

  static List<Map<String, dynamic>> defaultChores() => [
        {
          'id': 'chore-1',
          'description': 'Clean Kitchen',
          'frequencyDays': 7,
        },
      ];

  static List<Map<String, dynamic>> defaultAssignments() => [
        {
          'assignmentId': 'assign-1',
          'choreId': 'chore-1',
          'choreDescription': 'Clean Kitchen',
          'assignedUser': {
            'id': 'u-1',
            'name': 'Alice',
            'surname': 'Smith',
            'email': 'alice@example.com',
          },
          'status': 'PENDING',
          'dueDate': DateTime.now().toIso8601String(),
        },
      ];

  static Map<String, dynamic> defaultChoreOverview() => {
        'pendingAssignments': 2,
        'overdueAssignments': 1,
      };

  static List<Map<String, dynamic>> defaultExpenses() => [
        {
          'id': 'exp-1',
          'description': 'Groceries',
          'date': '2026-11-10T12:00:00.000Z',
          'amount': 80.0,
          'payerId': 'u-1',
          'payerFullName': 'Alice Smith',
          'splitType': 'EQUAL_SPLIT',
          'householdId': 'h-1',
          'shares': [
            {
              'id': 'share-1',
              'userId': 'u-1',
              'userFullName': 'Alice Smith',
              'amount': 40.0,
            },
            {
              'id': 'share-2',
              'userId': 'u-2',
              'userFullName': 'Bob Jones',
              'amount': 40.0,
            },
          ],
        },
      ];

  static Map<String, dynamic> defaultExpenseOverview() => {
        'totalAmount': 120.0,
        'expenseCount': 3,
      };

  static Map<String, dynamic> defaultUserNetOverview() => {
        'actualCashFlowAmount': 50.0,
      };

  static List<Map<String, dynamic>> defaultDebts() => [
        {
          'debtId': 'debt-1',
          'userTransactionRole': 'DEBTOR',
          'involvedId': 'u-2',
          'involvedName': 'Bob Jones',
          'amount': 40.0,
        },
      ];

  static List<Map<String, dynamic>> defaultShoppingLists() => [
        {
          'id': 'list-1',
          'name': 'Weekly Groceries',
          'items': [
            {'itemName': 'Milk', 'isBought': false},
            {'itemName': 'Bread', 'isBought': true},
          ],
          'status': 'IN_PROGRESS',
          'householdId': 'h-1',
          'creationDate': '2026-11-10T10:00:00.000Z',
        },
      ];
}

class IntegrationTestContext {
  final TestContext testContext;
  final MockState state;
  final FakeUiService uiService;
  final List<RequestOptions> recordedRequests = [];

  IntegrationTestContext({
    required this.testContext,
    required this.state,
    required this.uiService,
  });
}

IntegrationTestContext createIntegrationTestContext({
  bool authenticated = true,
  bool withHousehold = true,
  MockState? customState,
}) {
  final state = customState ??
      MockState(
        household: withHousehold ? MockState.defaultHousehold() : null,
      );

  final testCtx = createTestContext(
    defaultToken: authenticated ? 'jwt-abc-token' : null,
  );
  final uiService = FakeUiService();

  final integrationContext = IntegrationTestContext(
    testContext: testCtx,
    state: state,
    uiService: uiService,
  );

  testCtx.adapter.setHandler((options) {
    integrationContext.recordedRequests.add(options);
    final path = options.path;
    final method = options.method.toUpperCase();

    dynamic reqData = options.data;
    if (reqData is String && reqData.isNotEmpty) {
      try {
        reqData = jsonDecode(reqData);
      } catch (_) {}
    }

    // --- AUTH ENDPOINTS ---
    if (path.contains('/auth/login') || path.contains('/auth/register')) {
      return ResponseBody.fromString(
        jsonEncode({
          'token': 'jwt-abc-token',
          'user': state.user,
        }),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/auth/session')) {
      return ResponseBody.fromString(
        jsonEncode({'token': 'jwt-abc-token'}),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    // --- USER ENDPOINTS ---
    if (path.contains('/users/me')) {
      return ResponseBody.fromString(
        jsonEncode(state.user),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    // --- HOUSEHOLD ENDPOINTS ---
    if ((path.endsWith('/households/me') || path.contains('/households/current')) && method == 'GET') {
      if (state.household == null) {
        return ResponseBody.fromString(
          jsonEncode({'message': 'No household found'}),
          404,
          headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
        );
      }
      return ResponseBody.fromString(
        jsonEncode(state.household),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.endsWith('/households') && method == 'POST') {
      final name = reqData is Map ? reqData['name'] : 'The Cool Apartment';
      state.household = {
        'id': 'h-new-1',
        'name': name ?? 'The Cool Apartment',
        'creationDate': DateTime.now().toIso8601String(),
        'memberships': [
          {
            'user': state.user,
            'membership': {
              'isAdmin': true,
              'date': DateTime.now().toIso8601String(),
            },
          },
        ],
      };
      return ResponseBody.fromString(
        jsonEncode(state.household),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if ((path.contains('/households/join') || path.contains('/households/members')) && method == 'POST') {
      state.household = {
        'id': 'h-joined-1',
        'name': 'Joined Villa',
        'creationDate': DateTime.now().toIso8601String(),
        'memberships': [
          {
            'user': state.user,
            'membership': {
              'isAdmin': false,
              'date': DateTime.now().toIso8601String(),
            },
          },
        ],
      };
      return ResponseBody.fromString(
        jsonEncode(state.household),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/invite-code')) {
      return ResponseBody.fromString(
        jsonEncode({'invitationCode': 'INV-12345'}),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    // --- CHORES ENDPOINTS ---
    if (path.contains('/chores/assignments/me') ||
        path.contains('/chores/assignments/overview')) {
      return ResponseBody.fromString(
        jsonEncode(state.choreOverview),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/status') && method == 'PATCH') {
      // e.g. /chores/assignments/{id}/status
      for (var a in state.assignments) {
        if (path.contains(a['assignmentId'])) {
          a['status'] = 'COMPLETED';
        }
      }
      return ResponseBody.fromString(
        jsonEncode({'message': 'Status updated'}),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/chores/assignments/filter') ||
        (path.contains('/chores/assignments') && method == 'GET')) {
      return ResponseBody.fromString(
        jsonEncode(state.assignments),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/chores/assignments') && method == 'POST') {
      final newAssign = {
        'assignmentId': 'assign-${state.assignments.length + 1}',
        'choreId': 'chore-1',
        'choreDescription': 'Clean Kitchen',
        'assignedUser': state.user,
        'status': 'PENDING',
        'dueDate': DateTime.now().toIso8601String(),
      };
      state.assignments.add(newAssign);
      return ResponseBody.fromString(
        jsonEncode(newAssign),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.endsWith('/chores') && method == 'POST') {
      final desc = reqData is Map ? reqData['description'] : 'New Chore';
      final freq = reqData is Map ? reqData['frequencyDays'] ?? 0 : 0;
      final newChore = {
        'id': 'chore-${state.chores.length + 1}',
        'description': desc,
        'frequencyDays': freq,
      };
      state.chores.add(newChore);
      return ResponseBody.fromString(
        jsonEncode(newChore),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/chores') && method == 'GET') {
      return ResponseBody.fromString(
        jsonEncode(state.chores),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    // --- EXPENSES ENDPOINTS ---
    if (path.contains('/expenses/overview')) {
      return ResponseBody.fromString(
        jsonEncode(state.expenseOverview),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/expenses/me')) {
      return ResponseBody.fromString(
        jsonEncode(state.userNetOverview),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/debts')) {
      return ResponseBody.fromString(
        jsonEncode(state.debts),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.endsWith('/expenses') && method == 'POST') {
      final desc = reqData is Map ? reqData['description'] : 'Electricity Bill';
      final amount = reqData is Map ? (reqData['amount'] as num?)?.toDouble() ?? 60.0 : 60.0;
      final newExp = {
        'id': 'exp-${state.expenses.length + 1}',
        'description': desc,
        'date': DateTime.now().toIso8601String(),
        'amount': amount,
        'payerId': state.user['id'],
        'payerFullName': '${state.user['name']} ${state.user['surname']}',
        'splitType': 'EQUAL_SPLIT',
        'householdId': state.household?['id'] ?? 'h-1',
        'shares': [
          {
            'id': 'share-new',
            'userId': state.user['id'],
            'userFullName': '${state.user['name']} ${state.user['surname']}',
            'amount': amount,
          }
        ],
      };
      state.expenses.insert(0, newExp);
      return ResponseBody.fromString(
        jsonEncode(newExp),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/expenses') && method == 'GET') {
      return ResponseBody.fromString(
        jsonEncode(state.expenses),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    // --- SETTLEMENTS ENDPOINTS ---
    if (path.contains('/settlements') && method == 'POST') {
      final amount = reqData is Map ? (reqData['amount'] as num?)?.toDouble() ?? 40.0 : 40.0;
      final newSettlement = {
        'settlementId': 'settle-${state.settlements.length + 1}',
        'userTransactionRole': 'DEBTOR',
        'involvedId': 'u-2',
        'involvedName': 'Bob Jones',
        'amount': amount,
        'date': DateTime.now().toIso8601String(),
        'householdId': state.household?['id'] ?? 'h-1',
      };
      state.settlements.add(newSettlement);
      state.debts.clear(); // Debt has been settled
      return ResponseBody.fromString(
        jsonEncode(newSettlement),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/settlements') && method == 'GET') {
      return ResponseBody.fromString(
        jsonEncode(state.settlements),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    // --- SHOPPING LISTS ENDPOINTS ---
    if (path.endsWith('/shopping-lists') && method == 'POST') {
      final name = reqData is Map ? reqData['name'] : 'Party Supplies';
      final rawItems = reqData is Map ? (reqData['items'] as List<dynamic>?) : null;
      final items = rawItems?.map((it) {
            if (it is String) return {'itemName': it, 'isBought': false};
            return it;
          }).toList() ??
          [];
      final newList = {
        'id': 'list-${state.shoppingLists.length + 1}',
        'name': name,
        'items': items,
        'status': 'NOT_STARTED',
        'householdId': state.household?['id'] ?? 'h-1',
        'creationDate': DateTime.now().toIso8601String(),
      };
      state.shoppingLists.add(newList);
      return ResponseBody.fromString(
        jsonEncode(newList),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/shopping-lists') && method == 'PATCH') {
      // Find list and update
      for (var l in state.shoppingLists) {
        if (path.contains(l['id'])) {
          l['status'] = 'COMPLETED';
          final isBoughtList = (reqData is Map)
              ? (reqData['boughtItems'] ?? reqData['items']) as List<dynamic>?
              : null;
          if (isBoughtList != null) {
            final items = l['items'] as List<dynamic>;
            for (int i = 0; i < items.length && i < isBoughtList.length; i++) {
              items[i]['isBought'] = isBoughtList[i] == true;
            }
          }
          return ResponseBody.fromString(
            jsonEncode(l),
            200,
            headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
          );
        }
      }
      return ResponseBody.fromString(
        jsonEncode(state.shoppingLists.first),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    if (path.contains('/shopping-lists') && method == 'GET') {
      return ResponseBody.fromString(
        jsonEncode(state.shoppingLists),
        200,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
      );
    }

    // Fallback
    return ResponseBody.fromString(
      jsonEncode({}),
      200,
      headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
    );
  });

  return integrationContext;
}

Widget createIntegrationAppHarness({
  required IntegrationTestContext context,
}) {
  return HouseMateApp(
    apiClient: context.testContext.apiClient,
    uiService: context.uiService,
  );
}
