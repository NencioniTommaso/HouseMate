import 'package:json_annotation/json_annotation.dart';
import '../../user/response/user_response_dto.dart';

part 'login_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class LoginResponseDTO {
  final UserResponseDTO user;
  final String token;

  LoginResponseDTO({
    required this.user,
    required this.token,
  });

  factory LoginResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$LoginResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$LoginResponseDTOToJson(this);
}
