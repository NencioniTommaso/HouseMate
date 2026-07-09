// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chore_create_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChoreCreateRequestDTO _$ChoreCreateRequestDTOFromJson(
  Map<String, dynamic> json,
) => ChoreCreateRequestDTO(
  description: json['description'] as String,
  frequencyDays: (json['frequencyDays'] as num).toInt(),
  householdId: json['householdId'] as String,
);

Map<String, dynamic> _$ChoreCreateRequestDTOToJson(
  ChoreCreateRequestDTO instance,
) => <String, dynamic>{
  'description': instance.description,
  'frequencyDays': instance.frequencyDays,
  'householdId': instance.householdId,
};
