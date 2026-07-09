// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'household_membership_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HouseholdMembershipResponseDTO _$HouseholdMembershipResponseDTOFromJson(
  Map<String, dynamic> json,
) => HouseholdMembershipResponseDTO(
  isAdmin: json['isAdmin'] as bool,
  date: DateTime.parse(json['date'] as String),
);

Map<String, dynamic> _$HouseholdMembershipResponseDTOToJson(
  HouseholdMembershipResponseDTO instance,
) => <String, dynamic>{
  'isAdmin': instance.isAdmin,
  'date': instance.date.toIso8601String(),
};
