import 'package:json_annotation/json_annotation.dart';

part 'household_invitation_code_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class HouseholdInvitationCodeResponseDTO {
  final String invitationCode;
  @JsonKey(fromJson: _dateFromJson)
  final DateTime? refreshedAt;

  HouseholdInvitationCodeResponseDTO({
    required this.invitationCode,
    this.refreshedAt,
  });

  factory HouseholdInvitationCodeResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$HouseholdInvitationCodeResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$HouseholdInvitationCodeResponseDTOToJson(this);

  static DateTime? _dateFromJson(dynamic json) {
    if (json == null) return null;
    if (json is String) return DateTime.parse(json);
    if (json is List) {
      return DateTime(
        json[0] as int,
        json[1] as int,
        json[2] as int,
        json.length > 3 ? json[3] as int : 0,
        json.length > 4 ? json[4] as int : 0,
        json.length > 5 ? json[5] as int : 0,
      );
    }
    return null;
  }
}
