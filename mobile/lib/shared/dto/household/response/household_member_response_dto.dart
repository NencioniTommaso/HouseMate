import 'package:json_annotation/json_annotation.dart';
import '../../user/response/user_response_dto.dart';
import 'household_membership_response_dto.dart';

part 'household_member_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class HouseholdMemberResponseDTO {
  final UserResponseDTO user;
  final HouseholdMembershipResponseDTO membership;

  HouseholdMemberResponseDTO({
    required this.user,
    required this.membership,
  });

  factory HouseholdMemberResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$HouseholdMemberResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$HouseholdMemberResponseDTOToJson(this);
}
