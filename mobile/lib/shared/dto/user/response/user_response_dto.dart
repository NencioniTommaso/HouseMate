import 'package:json_annotation/json_annotation.dart';

part 'user_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class UserResponseDTO {
  final String id;
  final String name;
  final String surname;
  final String email;
  final String? iban;
  final String? paymentLink;

  UserResponseDTO({
    required this.id,
    required this.name,
    required this.surname,
    required this.email,
    this.iban,
    this.paymentLink,
  });

  factory UserResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$UserResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$UserResponseDTOToJson(this);
}
