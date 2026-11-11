import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/shopping_list_service.dart';
import 'package:mobile/shared/dto/items/request/shopping_list_create_request_dto.dart';
import 'package:mobile/shared/dto/items/request/shopping_list_update_request_dto.dart';
import 'package:mobile/shared/enums/shopping_list_status.dart';
import 'package:mobile/shared/utils/types/list_item.dart';

import 'test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late TestContext context;
  late ShoppingListService shoppingListService;

  setUp(() {
    context = createTestContext(defaultToken: 'jwt-shopping-token');
    shoppingListService = ShoppingListService(context.apiClient);
  });

  final mockListJson = {
    'id': 'list-1',
    'name': 'Weekly Groceries',
    'items': [
      {'itemName': 'Apples', 'bought': false},
      {'itemName': 'Milk', 'bought': true},
    ],
    'status': 'IN_PROGRESS',
    'householdId': 'h-1',
    'creationDate': '2026-11-10T10:00:00.000Z',
  };

  group('ShoppingListService - createShoppingList', () {
    final createReq = ShoppingListCreateRequestDTO(
      name: 'Weekly Groceries',
      items: [
        ListItem(itemName: 'Apples', isBought: false),
        ListItem(itemName: 'Milk', isBought: true),
      ],
      householdId: 'h-1',
      creationDate: DateTime.parse('2026-11-10T10:00:00.000Z'),
    );

    test('creates shopping list via POST /shopping-lists and returns ShoppingListResponseDTO', () async {
      context.adapter.reply(200, mockListJson);

      final result = await shoppingListService.createShoppingList(createReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'POST');
      expect(req.path, '/shopping-lists');
      expect(req.data, createReq.toJson());

      expect(result.id, 'list-1');
      expect(result.name, 'Weekly Groceries');
      expect(result.items.length, 2);
      expect(result.status, ShoppingListStatus.inProgress);
    });

    test('throws ApiException on 400 Bad Request', () async {
      context.adapter.reply(400, {'message': 'Shopping list name cannot be blank'});

      expect(
        () => shoppingListService.createShoppingList(createReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });
  });

  group('ShoppingListService - deleteShoppingList', () {
    test('deletes list via DELETE /shopping-lists/:id', () async {
      context.adapter.reply(200, null);

      await shoppingListService.deleteShoppingList('list-1');

      final req = context.adapter.lastRequest!;
      expect(req.method, 'DELETE');
      expect(req.path, '/shopping-lists/list-1');
    });

    test('throws ApiException on 404 Not Found', () async {
      context.adapter.reply(404, {'message': 'List not found'});

      expect(
        () => shoppingListService.deleteShoppingList('list-999'),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 404)),
      );
    });

    test('throws ApiException on timeout', () async {
      context.adapter.replyError(DioExceptionType.connectionTimeout);

      expect(
        () => shoppingListService.deleteShoppingList('list-1'),
        throwsA(isA<ApiException>()),
      );
    });
  });

  group('ShoppingListService - updateListInformation', () {
    final updateReq = ShoppingListUpdateRequestDTO(boughtItems: [true, true]);

    final updatedJson = {
      'id': 'list-1',
      'name': 'Weekly Groceries',
      'items': [
        {'itemName': 'Apples', 'bought': true},
        {'itemName': 'Milk', 'bought': true},
      ],
      'status': 'COMPLETED',
      'householdId': 'h-1',
      'creationDate': '2026-11-10T10:00:00.000Z',
    };

    test('updates list via PATCH /shopping-lists/:id and returns updated ShoppingListResponseDTO', () async {
      context.adapter.reply(200, updatedJson);

      final result = await shoppingListService.updateListInformation('list-1', updateReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'PATCH');
      expect(req.path, '/shopping-lists/list-1');
      expect(req.data, updateReq.toJson());

      expect(result.id, 'list-1');
      expect(result.status, ShoppingListStatus.completed);
      expect(result.items.every((i) => i.isBought), isTrue);
    });

    test('throws ApiException on 400 error', () async {
      context.adapter.reply(400, {'message': 'Mismatched items length'});

      expect(
        () => shoppingListService.updateListInformation('list-1', updateReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });
  });

  group('ShoppingListService - getShoppingItemsByHousehold', () {
    test('fetches lists via GET /shopping-lists', () async {
      context.adapter.reply(200, [mockListJson]);

      final result = await shoppingListService.getShoppingItemsByHousehold();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/shopping-lists');

      expect(result.length, 1);
      expect(result.first.id, 'list-1');
    });

    test('throws ApiException on 401 Unauthorized', () async {
      context.adapter.reply(401, {'error': 'Unauthorized'});

      expect(
        () => shoppingListService.getShoppingItemsByHousehold(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 401)),
      );
    });
  });
}
