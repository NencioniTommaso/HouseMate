// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'shopping_list_update_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShoppingListUpdateRequestDTO _$ShoppingListUpdateRequestDTOFromJson(
  Map<String, dynamic> json,
) => ShoppingListUpdateRequestDTO(
  boughtItems: (json['boughtItems'] as List<dynamic>)
      .map((e) => e as bool)
      .toList(),
);

Map<String, dynamic> _$ShoppingListUpdateRequestDTOToJson(
  ShoppingListUpdateRequestDTO instance,
) => <String, dynamic>{'boughtItems': instance.boughtItems};
