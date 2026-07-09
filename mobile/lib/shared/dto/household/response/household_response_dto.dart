import 'package:json_annotation/json_annotation.dart';
import 'household_member_response_dto.dart';

part 'household_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class HouseholdResponseDTO {
  final String id;
  final String name;
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
}
