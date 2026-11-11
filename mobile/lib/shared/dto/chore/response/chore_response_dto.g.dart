// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chore_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChoreResponseDTO _$ChoreResponseDTOFromJson(Map<String, dynamic> json) =>
    ChoreResponseDTO(
      id: json['id'] as String,
      description: json['description'] as String,
      frequencyDays: (json['frequencyDays'] as num).toInt(),
    );

Map<String, dynamic> _$ChoreResponseDTOToJson(ChoreResponseDTO instance) =>
    <String, dynamic>{
      'id': instance.id,
      'description': instance.description,
      'frequencyDays': instance.frequencyDays,
    };
