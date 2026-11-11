import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/shared/dto/items/request/shopping_list_create_request_dto.dart';
import 'package:mobile/shared/dto/items/request/shopping_list_update_request_dto.dart';
import 'package:mobile/shared/dto/items/response/shopping_list_response_dto.dart';
import 'package:mobile/shared/enums/shopping_list_status.dart';
import 'package:mobile/shared/utils/types/list_item.dart';

void main() {
  group('ShoppingListResponseDTO', () {
    test('should serialize and deserialize correctly with items and status', () {
      final creationDate = DateTime.parse('2026-11-10T08:00:00.000Z');
      final item1 = ListItem(itemName: 'Milk', isBought: true);
      final item2 = ListItem(itemName: 'Bread', isBought: false);

      final dto = ShoppingListResponseDTO(
        id: 'shop-1',
        name: 'Weekly Groceries',
        items: [item1, item2],
        status: ShoppingListStatus.inProgress,
        householdId: 'house-100',
        creationDate: creationDate,
      );

      expect(dto.id, 'shop-1');
      expect(dto.name, 'Weekly Groceries');
      expect(dto.status, ShoppingListStatus.inProgress);
      expect(dto.householdId, 'house-100');
      expect(dto.items.length, 2);

      final json = dto.toJson();
      expect(json, {
        'id': 'shop-1',
        'name': 'Weekly Groceries',
        'items': [
          {'itemName': 'Milk', 'bought': true},
          {'itemName': 'Bread', 'bought': false},
        ],
        'status': 'IN_PROGRESS',
        'householdId': 'house-100',
        'creationDate': creationDate.toIso8601String(),
      });

      final deserialized = ShoppingListResponseDTO.fromJson(json);
      expect(deserialized.id, dto.id);
      expect(deserialized.name, dto.name);
      expect(deserialized.status, ShoppingListStatus.inProgress);
      expect(deserialized.items.length, 2);
      expect(deserialized.items[0].itemName, 'Milk');
      expect(deserialized.items[0].isBought, isTrue);
      expect(deserialized.items[1].itemName, 'Bread');
      expect(deserialized.items[1].isBought, isFalse);
      expect(deserialized.creationDate, creationDate);
    });

    test('should serialize and deserialize with empty items list', () {
      final creationDate = DateTime.parse('2026-11-01T00:00:00.000Z');
      final dto = ShoppingListResponseDTO(
        id: 'shop-2',
        name: 'Empty List',
        items: [],
        status: ShoppingListStatus.notStarted,
        householdId: 'house-101',
        creationDate: creationDate,
      );

      final json = dto.toJson();
      expect(json['items'], isEmpty);
      expect(json['status'], 'NOT_STARTED');

      final deserialized = ShoppingListResponseDTO.fromJson(json);
      expect(deserialized.items, isEmpty);
      expect(deserialized.status, ShoppingListStatus.notStarted);
    });
  });

  group('ShoppingListCreateRequestDTO', () {
    test('should serialize and deserialize correctly', () {
      final date = DateTime.parse('2026-11-10T11:00:00.000Z');
      final item = ListItem(itemName: 'Apples', isBought: false);

      final dto = ShoppingListCreateRequestDTO(
        name: 'Fruit run',
        items: [item],
        householdId: 'house-200',
        creationDate: date,
      );

      final json = dto.toJson();
      expect(json, {
        'name': 'Fruit run',
        'items': [
          {'itemName': 'Apples', 'bought': false},
        ],
        'householdId': 'house-200',
        'creationDate': date.toIso8601String(),
      });

      final deserialized = ShoppingListCreateRequestDTO.fromJson(json);
      expect(deserialized.name, 'Fruit run');
      expect(deserialized.householdId, 'house-200');
      expect(deserialized.creationDate, date);
      expect(deserialized.items.length, 1);
      expect(deserialized.items.first.itemName, 'Apples');
    });
  });

  group('ShoppingListUpdateRequestDTO', () {
    test('should serialize and deserialize boolean bought list', () {
      final dto = ShoppingListUpdateRequestDTO(boughtItems: [true, false, true]);

      final json = dto.toJson();
      expect(json, {
        'boughtItems': [true, false, true],
      });

      final deserialized = ShoppingListUpdateRequestDTO.fromJson(json);
      expect(deserialized.boughtItems, [true, false, true]);
    });

    test('should handle empty bought list', () {
      final dto = ShoppingListUpdateRequestDTO(boughtItems: []);
      final json = dto.toJson();
      expect(json['boughtItems'], isEmpty);

      final deserialized = ShoppingListUpdateRequestDTO.fromJson(json);
      expect(deserialized.boughtItems, isEmpty);
    });
  });
}
