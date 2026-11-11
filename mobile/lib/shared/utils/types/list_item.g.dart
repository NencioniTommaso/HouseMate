// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'list_item.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ListItem _$ListItemFromJson(Map<String, dynamic> json) => ListItem(
  itemName: json['itemName'] as String,
  isBought: json['bought'] as bool? ?? false,
);

Map<String, dynamic> _$ListItemToJson(ListItem instance) => <String, dynamic>{
  'itemName': instance.itemName,
  'bought': instance.isBought,
};
