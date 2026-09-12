import 'package:json_annotation/json_annotation.dart';
import 'household_member_response_dto.dart';

part 'household_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class HouseholdResponseDTO {
  final String id;
  final String name;
  @JsonKey(fromJson: _dateFromJson)
  final DateTime creationDate;
  final List<HouseholdMemberResponseDTO> memberships;

  HouseholdResponseDTO({
    required this.id,
    required this.name,
    required this.creationDate,
    required this.memberships,
  });

  factory HouseholdResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$HouseholdResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$HouseholdResponseDTOToJson(this);

  static DateTime _dateFromJson(dynamic json) {
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
    return DateTime.now(); // Fallback for required field
  }
}
