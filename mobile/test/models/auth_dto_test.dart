import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/shared/dto/auth/request/login_request_dto.dart';
import 'package:mobile/shared/dto/auth/request/register_request_dto.dart';
import 'package:mobile/shared/dto/auth/response/login_response_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';

void main() {
  group('LoginRequestDTO', () {
    test('should correctly instantiate and serialize to JSON', () {
      final dto = LoginRequestDTO(
        email: 'test@example.com',
        password: 'password123',
      );

      expect(dto.email, 'test@example.com');
      expect(dto.password, 'password123');

      final json = dto.toJson();
      expect(json, {
        'email': 'test@example.com',
        'password': 'password123',
      });
    });

    test('should correctly deserialize from JSON', () {
      final json = {
        'email': 'user@domain.org',
        'password': 'securePassword!',
      };

      final dto = LoginRequestDTO.fromJson(json);
      expect(dto.email, 'user@domain.org');
      expect(dto.password, 'securePassword!');
      expect(dto.toJson(), json);
    });
  });

  group('RegisterRequestDTO', () {
    test('should correctly instantiate and serialize to JSON with optional iban present', () {
      final dto = RegisterRequestDTO(
        name: 'John',
        surname: 'Doe',
        email: 'john.doe@example.com',
        password: 'secretPassword',
        iban: 'NL91ABNA0417164300',
        inviteCode: 'INVITE123',
      );

      expect(dto.name, 'John');
      expect(dto.surname, 'Doe');
      expect(dto.email, 'john.doe@example.com');
      expect(dto.password, 'secretPassword');
      expect(dto.iban, 'NL91ABNA0417164300');
      expect(dto.inviteCode, 'INVITE123');

      final json = dto.toJson();
      expect(json, {
        'name': 'John',
        'surname': 'Doe',
        'email': 'john.doe@example.com',
        'password': 'secretPassword',
        'iban': 'NL91ABNA0417164300',
        'inviteCode': 'INVITE123',
      });
    });

    test('should correctly instantiate and serialize to JSON with iban null', () {
      final dto = RegisterRequestDTO(
        name: 'Jane',
        surname: 'Smith',
        email: 'jane.smith@example.com',
        password: 'secretPassword2',
        iban: null,
        inviteCode: 'INVITE456',
      );

      expect(dto.iban, isNull);

      final json = dto.toJson();
      expect(json['iban'], isNull);

      final deserialized = RegisterRequestDTO.fromJson(json);
      expect(deserialized.name, 'Jane');
      expect(deserialized.surname, 'Smith');
      expect(deserialized.email, 'jane.smith@example.com');
      expect(deserialized.password, 'secretPassword2');
      expect(deserialized.iban, isNull);
      expect(deserialized.inviteCode, 'INVITE456');
    });

    test('should deserialize from JSON missing iban field as null', () {
      final json = {
        'name': 'Alice',
        'surname': 'Wonder',
        'email': 'alice@wonder.land',
        'password': 'pass',
        'inviteCode': 'WONDER99',
      };

      final dto = RegisterRequestDTO.fromJson(json);
      expect(dto.iban, isNull);
      expect(dto.name, 'Alice');
      expect(dto.inviteCode, 'WONDER99');
    });
  });

  group('LoginResponseDTO', () {
    test('should correctly instantiate and serialize with nested UserResponseDTO', () {
      final user = UserResponseDTO(
        id: 'user-123',
        name: 'Bob',
        surname: 'Builder',
        email: 'bob@builder.com',
        iban: 'NL00TEST1234567890',
        paymentLink: 'https://paypal.me/bob',
      );

      final response = LoginResponseDTO(
        user: user,
        token: 'jwt-auth-token-xyz',
      );

      expect(response.user.id, 'user-123');
      expect(response.token, 'jwt-auth-token-xyz');

      final json = response.toJson();
      expect(json, {
        'user': {
          'id': 'user-123',
          'name': 'Bob',
          'surname': 'Builder',
          'email': 'bob@builder.com',
          'iban': 'NL00TEST1234567890',
          'paymentLink': 'https://paypal.me/bob',
        },
        'token': 'jwt-auth-token-xyz',
      });
    });

    test('should deserialize nested UserResponseDTO from JSON with null optional fields', () {
      final json = {
        'user': {
          'id': 'user-456',
          'name': 'Charlie',
          'surname': 'Brown',
          'email': 'charlie@peanuts.com',
          'iban': null,
          'paymentLink': null,
        },
        'token': 'token-456',
      };

      final dto = LoginResponseDTO.fromJson(json);
      expect(dto.user.id, 'user-456');
      expect(dto.user.name, 'Charlie');
      expect(dto.user.iban, isNull);
      expect(dto.user.paymentLink, isNull);
      expect(dto.token, 'token-456');
      expect(dto.toJson(), json);
    });
  });
}
