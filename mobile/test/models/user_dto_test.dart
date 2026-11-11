import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/shared/dto/user/request/user_create_request_dto.dart';
import 'package:mobile/shared/dto/user/request/user_update_request_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';

void main() {
  group('UserResponseDTO', () {
    test('should instantiate and serialize to JSON with all fields present', () {
      final dto = UserResponseDTO(
        id: 'u-1',
        name: 'Jane',
        surname: 'Doe',
        email: 'jane@example.com',
        iban: 'NL91ABNA0417164300',
        paymentLink: 'https://paypal.me/janedoe',
      );

      expect(dto.id, 'u-1');
      expect(dto.name, 'Jane');
      expect(dto.surname, 'Doe');
      expect(dto.email, 'jane@example.com');
      expect(dto.iban, 'NL91ABNA0417164300');
      expect(dto.paymentLink, 'https://paypal.me/janedoe');

      final json = dto.toJson();
      expect(json, {
        'id': 'u-1',
        'name': 'Jane',
        'surname': 'Doe',
        'email': 'jane@example.com',
        'iban': 'NL91ABNA0417164300',
        'paymentLink': 'https://paypal.me/janedoe',
      });
    });

    test('should deserialize from JSON with null optional fields', () {
      final json = {
        'id': 'u-2',
        'name': 'John',
        'surname': 'Smith',
        'email': 'john@smith.org',
        'iban': null,
        'paymentLink': null,
      };

      final dto = UserResponseDTO.fromJson(json);
      expect(dto.id, 'u-2');
      expect(dto.name, 'John');
      expect(dto.surname, 'Smith');
      expect(dto.email, 'john@smith.org');
      expect(dto.iban, isNull);
      expect(dto.paymentLink, isNull);

      final serialized = dto.toJson();
      expect(serialized, json);
    });

    test('should deserialize from JSON when optional fields are omitted', () {
      final json = {
        'id': 'u-3',
        'name': 'Alice',
        'surname': 'Wonder',
        'email': 'alice@wonder.org',
      };

      final dto = UserResponseDTO.fromJson(json);
      expect(dto.id, 'u-3');
      expect(dto.iban, isNull);
      expect(dto.paymentLink, isNull);
    });
  });

  group('UserCreateRequestDTO', () {
    test('should instantiate and serialize to JSON with paymentLink present', () {
      final dto = UserCreateRequestDTO(
        name: 'Alice',
        surname: 'Wonderland',
        email: 'alice@wonderland.com',
        password: 'passwordSecret',
        iban: 'NL99INGB0001234567',
        paymentLink: 'https://tikkie.me/pay/alice',
      );

      expect(dto.name, 'Alice');
      expect(dto.surname, 'Wonderland');
      expect(dto.email, 'alice@wonderland.com');
      expect(dto.password, 'passwordSecret');
      expect(dto.iban, 'NL99INGB0001234567');
      expect(dto.paymentLink, 'https://tikkie.me/pay/alice');

      final json = dto.toJson();
      expect(json, {
        'name': 'Alice',
        'surname': 'Wonderland',
        'email': 'alice@wonderland.com',
        'password': 'passwordSecret',
        'iban': 'NL99INGB0001234567',
        'paymentLink': 'https://tikkie.me/pay/alice',
      });

      final deserialized = UserCreateRequestDTO.fromJson(json);
      expect(deserialized.name, dto.name);
      expect(deserialized.iban, dto.iban);
      expect(deserialized.paymentLink, dto.paymentLink);
    });

    test('should instantiate and serialize to JSON with paymentLink null', () {
      final dto = UserCreateRequestDTO(
        name: 'Bob',
        surname: 'Builder',
        email: 'bob@builder.com',
        password: 'secretPassword',
        iban: 'NL88RABO0123456789',
        paymentLink: null,
      );

      final json = dto.toJson();
      expect(json['paymentLink'], isNull);

      final deserialized = UserCreateRequestDTO.fromJson(json);
      expect(deserialized.paymentLink, isNull);
      expect(deserialized.toJson(), json);
    });
  });

  group('UserUpdateRequestDTO', () {
    test('should instantiate and serialize with all fields populated', () {
      final dto = UserUpdateRequestDTO(
        name: 'UpdatedName',
        surname: 'UpdatedSurname',
        email: 'updated@example.com',
        iban: 'NL77SNSB0123456789',
        paymentLink: 'https://paypal.me/updated',
      );

      final json = dto.toJson();
      expect(json, {
        'name': 'UpdatedName',
        'surname': 'UpdatedSurname',
        'email': 'updated@example.com',
        'iban': 'NL77SNSB0123456789',
        'paymentLink': 'https://paypal.me/updated',
      });

      final deserialized = UserUpdateRequestDTO.fromJson(json);
      expect(deserialized.name, 'UpdatedName');
      expect(deserialized.surname, 'UpdatedSurname');
      expect(deserialized.email, 'updated@example.com');
      expect(deserialized.iban, 'NL77SNSB0123456789');
      expect(deserialized.paymentLink, 'https://paypal.me/updated');
    });

    test('should instantiate and serialize with all fields null', () {
      final dto = UserUpdateRequestDTO();

      expect(dto.name, isNull);
      expect(dto.surname, isNull);
      expect(dto.email, isNull);
      expect(dto.iban, isNull);
      expect(dto.paymentLink, isNull);

      final json = dto.toJson();
      expect(json, {
        'name': null,
        'surname': null,
        'email': null,
        'iban': null,
        'paymentLink': null,
      });

      final deserialized = UserUpdateRequestDTO.fromJson({});
      expect(deserialized.name, isNull);
      expect(deserialized.surname, isNull);
      expect(deserialized.email, isNull);
      expect(deserialized.iban, isNull);
      expect(deserialized.paymentLink, isNull);
    });

    test('should deserialize partial fields correctly', () {
      final json = {
        'name': 'PartialName',
        'iban': 'NL11TEST0000000000',
      };

      final dto = UserUpdateRequestDTO.fromJson(json);
      expect(dto.name, 'PartialName');
      expect(dto.surname, isNull);
      expect(dto.email, isNull);
      expect(dto.iban, 'NL11TEST0000000000');
      expect(dto.paymentLink, isNull);
    });
  });
}
