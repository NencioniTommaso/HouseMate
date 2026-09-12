// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'household_member_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HouseholdMemberResponseDTO _$HouseholdMemberResponseDTOFromJson(
  Map<String, dynamic> json,
) => HouseholdMemberResponseDTO(
  user: UserResponseDTO.fromJson(json['user'] as Map<String, dynamic>),
  membership: HouseholdMembershipResponseDTO.fromJson(
    json['membership'] as Map<String, dynamic>,
  ),
);

Map<String, dynamic> _$HouseholdMemberResponseDTOToJson(
  HouseholdMemberResponseDTO instance,
) => <String, dynamic>{
  'user': instance.user.toJson(),
  'membership': instance.membership.toJson(),
};
