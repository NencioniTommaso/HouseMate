import 'package:json_annotation/json_annotation.dart';

part 'household_membership_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class HouseholdMembershipResponseDTO {
  final bool isAdmin;
  final DateTime date;

  HouseholdMembershipResponseDTO({
    required this.isAdmin,
    required this.date,
  });

  factory HouseholdMembershipResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$HouseholdMembershipResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$HouseholdMembershipResponseDTOToJson(this);
}
