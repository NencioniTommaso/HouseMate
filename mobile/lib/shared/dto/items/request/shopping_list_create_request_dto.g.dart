// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'shopping_list_create_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShoppingListCreateRequestDTO _$ShoppingListCreateRequestDTOFromJson(
  Map<String, dynamic> json,
) => ShoppingListCreateRequestDTO(
  name: json['name'] as String,
  items: (json['items'] as List<dynamic>)
      .map((e) => ListItem.fromJson(e as Map<String, dynamic>))
      .toList(),
  householdId: json['householdId'] as String,
  creationDate: DateTime.parse(json['creationDate'] as String),
);

Map<String, dynamic> _$ShoppingListCreateRequestDTOToJson(
  ShoppingListCreateRequestDTO instance,
) => <String, dynamic>{
  'name': instance.name,
  'items': instance.items.map((e) => e.toJson()).toList(),
  'householdId': instance.householdId,
  'creationDate': instance.creationDate.toIso8601String(),
};
