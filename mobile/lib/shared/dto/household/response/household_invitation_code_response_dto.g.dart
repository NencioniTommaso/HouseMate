// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'household_invitation_code_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HouseholdInvitationCodeResponseDTO _$HouseholdInvitationCodeResponseDTOFromJson(
  Map<String, dynamic> json,
) => HouseholdInvitationCodeResponseDTO(
  invitationCode: json['invitationCode'] as String,
  refreshedAt: json['refreshedAt'] == null
      ? null
      : DateTime.parse(json['refreshedAt'] as String),
);

Map<String, dynamic> _$HouseholdInvitationCodeResponseDTOToJson(
  HouseholdInvitationCodeResponseDTO instance,
) => <String, dynamic>{
  'invitationCode': instance.invitationCode,
  'refreshedAt': instance.refreshedAt?.toIso8601String(),
};
