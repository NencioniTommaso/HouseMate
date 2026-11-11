import 'package:json_annotation/json_annotation.dart';

part 'user_create_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class UserCreateRequestDTO {
  final String name;
  final String surname;
  final String email;
  final String password;
  final String iban;
  final String? paymentLink;

  UserCreateRequestDTO({
    required this.name,
    required this.surname,
    required this.email,
    required this.password,
    required this.iban,
    this.paymentLink,
  });

  factory UserCreateRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$UserCreateRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$UserCreateRequestDTOToJson(this);
}
