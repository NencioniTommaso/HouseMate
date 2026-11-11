import 'package:json_annotation/json_annotation.dart';

part 'add_member_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class AddMemberRequestDTO {
  final String invitationCode;

  AddMemberRequestDTO({
    required this.invitationCode,
  });

  factory AddMemberRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$AddMemberRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$AddMemberRequestDTOToJson(this);
}
