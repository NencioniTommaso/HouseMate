import 'package:json_annotation/json_annotation.dart';

part 'user_update_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class UserUpdateRequestDTO {
  final String? name;
  final String? surname;
  final String? email;
  final String? iban;
  final String? paymentLink;

  UserUpdateRequestDTO({
    this.name,
    this.surname,
    this.email,
    this.iban,
    this.paymentLink,
  });

  factory UserUpdateRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$UserUpdateRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$UserUpdateRequestDTOToJson(this);
}
