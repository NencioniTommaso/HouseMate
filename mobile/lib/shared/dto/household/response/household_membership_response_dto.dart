import 'package:json_annotation/json_annotation.dart';

part 'household_membership_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class HouseholdMembershipResponseDTO {
  @JsonKey(name: 'isAdmin', defaultValue: false)
  final bool isAdmin;
  @JsonKey(fromJson: _dateFromJson)
  final DateTime date;

  HouseholdMembershipResponseDTO({
    required this.isAdmin,
    required this.date,
  });

  factory HouseholdMembershipResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$HouseholdMembershipResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$HouseholdMembershipResponseDTOToJson(this);

  static DateTime _dateFromJson(dynamic json) {
    if (json is String) return DateTime.parse(json);
    if (json is List) {
      return DateTime(
        json[0] as int,
        json[1] as int,
        json[2] as int,
      );
    }
    return DateTime.now();
  }
}
