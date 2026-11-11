// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_create_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserCreateRequestDTO _$UserCreateRequestDTOFromJson(
  Map<String, dynamic> json,
) => UserCreateRequestDTO(
  name: json['name'] as String,
  surname: json['surname'] as String,
  email: json['email'] as String,
  password: json['password'] as String,
  iban: json['iban'] as String,
  paymentLink: json['paymentLink'] as String?,
);

Map<String, dynamic> _$UserCreateRequestDTOToJson(
  UserCreateRequestDTO instance,
) => <String, dynamic>{
  'name': instance.name,
  'surname': instance.surname,
  'email': instance.email,
  'password': instance.password,
  'iban': instance.iban,
  'paymentLink': instance.paymentLink,
};
