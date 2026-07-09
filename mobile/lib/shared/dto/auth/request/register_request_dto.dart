import 'package:json_annotation/json_annotation.dart';

part 'register_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class RegisterRequestDTO {
  final String name;
  final String surname;
  final String email;
  final String password;
  final String? iban;

  RegisterRequestDTO({
    required this.name,
    required this.surname,
    required this.email,
    required this.password,
    this.iban,
  });

  factory RegisterRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$RegisterRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$RegisterRequestDTOToJson(this);
}
