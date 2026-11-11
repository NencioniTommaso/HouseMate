// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'household_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HouseholdResponseDTO _$HouseholdResponseDTOFromJson(
  Map<String, dynamic> json,
) => HouseholdResponseDTO(
  id: json['id'] as String,
  name: json['name'] as String,
  creationDate: HouseholdResponseDTO._dateFromJson(json['creationDate']),
  memberships: (json['memberships'] as List<dynamic>)
      .map(
        (e) => HouseholdMemberResponseDTO.fromJson(e as Map<String, dynamic>),
      )
      .toList(),
);

Map<String, dynamic> _$HouseholdResponseDTOToJson(
  HouseholdResponseDTO instance,
) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'creationDate': instance.creationDate.toIso8601String(),
  'memberships': instance.memberships.map((e) => e.toJson()).toList(),
};
