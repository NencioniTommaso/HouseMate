// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserResponseDTO _$UserResponseDTOFromJson(Map<String, dynamic> json) =>
    UserResponseDTO(
      id: json['id'] as String,
      name: json['name'] as String,
      surname: json['surname'] as String,
      email: json['email'] as String,
      iban: json['iban'] as String?,
      paymentLink: json['paymentLink'] as String?,
    );

Map<String, dynamic> _$UserResponseDTOToJson(UserResponseDTO instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'surname': instance.surname,
      'email': instance.email,
      'iban': instance.iban,
      'paymentLink': instance.paymentLink,
    };
