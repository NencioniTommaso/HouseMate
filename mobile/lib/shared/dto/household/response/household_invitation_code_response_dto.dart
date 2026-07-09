import 'package:json_annotation/json_annotation.dart';

part 'household_invitation_code_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class HouseholdInvitationCodeResponseDTO {
  final String invitationCode;
  final DateTime? refreshedAt;

  HouseholdInvitationCodeResponseDTO({
    required this.invitationCode,
    this.refreshedAt,
  });

  factory HouseholdInvitationCodeResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$HouseholdInvitationCodeResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$HouseholdInvitationCodeResponseDTOToJson(this);
}
