// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'shopping_list_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShoppingListResponseDTO _$ShoppingListResponseDTOFromJson(
  Map<String, dynamic> json,
) => ShoppingListResponseDTO(
  id: json['id'] as String,
  name: json['name'] as String,
  items: (json['items'] as List<dynamic>)
      .map((e) => ListItem.fromJson(e as Map<String, dynamic>))
      .toList(),
  status: $enumDecode(_$ShoppingListStatusEnumMap, json['status']),
  householdId: json['householdId'] as String,
  creationDate: DateTime.parse(json['creationDate'] as String),
);

Map<String, dynamic> _$ShoppingListResponseDTOToJson(
  ShoppingListResponseDTO instance,
) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'items': instance.items.map((e) => e.toJson()).toList(),
  'status': _$ShoppingListStatusEnumMap[instance.status]!,
  'householdId': instance.householdId,
  'creationDate': instance.creationDate.toIso8601String(),
};

const _$ShoppingListStatusEnumMap = {
  ShoppingListStatus.notStarted: 'NOT_STARTED',
  ShoppingListStatus.inProgress: 'IN_PROGRESS',
  ShoppingListStatus.completed: 'COMPLETED',
};
