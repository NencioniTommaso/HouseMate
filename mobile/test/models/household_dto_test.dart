import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/shared/dto/household/request/add_member_request_dto.dart';
import 'package:mobile/shared/dto/household/request/household_create_request_dto.dart';
import 'package:mobile/shared/dto/household/response/household_invitation_code_response_dto.dart';
import 'package:mobile/shared/dto/household/response/household_member_response_dto.dart';
import 'package:mobile/shared/dto/household/response/household_membership_response_dto.dart';
import 'package:mobile/shared/dto/household/response/household_response_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';

void main() {
  group('HouseholdInvitationCodeResponseDTO', () {
    test('should serialize and deserialize with ISO String refreshedAt', () {
      final date = DateTime.parse('2026-11-10T09:30:00.000Z');
      final dto = HouseholdInvitationCodeResponseDTO(
        invitationCode: 'INV-999',
        refreshedAt: date,
      );

      final json = dto.toJson();
      expect(json, {
        'invitationCode': 'INV-999',
        'refreshedAt': date.toIso8601String(),
      });

      final deserialized = HouseholdInvitationCodeResponseDTO.fromJson(json);
      expect(deserialized.invitationCode, 'INV-999');
      expect(deserialized.refreshedAt, date);
    });

    test('should deserialize with list-format refreshedAt date', () {
      final json = {
        'invitationCode': 'INV-888',
        'refreshedAt': [2026, 11, 10, 10, 15, 30],
      };

      final dto = HouseholdInvitationCodeResponseDTO.fromJson(json);
      expect(dto.invitationCode, 'INV-888');
      expect(dto.refreshedAt, DateTime(2026, 11, 10, 10, 15, 30));
    });

    test('should deserialize with null or unknown refreshedAt', () {
      final jsonNull = {
        'invitationCode': 'INV-777',
        'refreshedAt': null,
      };
      final dtoNull = HouseholdInvitationCodeResponseDTO.fromJson(jsonNull);
      expect(dtoNull.refreshedAt, isNull);

      final jsonInvalid = {
        'invitationCode': 'INV-666',
        'refreshedAt': 123456,
      };
      final dtoInvalid = HouseholdInvitationCodeResponseDTO.fromJson(jsonInvalid);
      expect(dtoInvalid.refreshedAt, isNull);
    });
  });

  group('HouseholdMembershipResponseDTO', () {
    test('should serialize and deserialize with ISO String date and isAdmin true', () {
      final date = DateTime.parse('2026-01-15T08:00:00.000Z');
      final dto = HouseholdMembershipResponseDTO(
        isAdmin: true,
        date: date,
      );

      final json = dto.toJson();
      expect(json, {
        'isAdmin': true,
        'date': date.toIso8601String(),
      });

      final deserialized = HouseholdMembershipResponseDTO.fromJson(json);
      expect(deserialized.isAdmin, isTrue);
      expect(deserialized.date, date);
    });

    test('should deserialize with list-format date and default isAdmin', () {
      final json = {
        'date': [2026, 3, 20],
      };

      final dto = HouseholdMembershipResponseDTO.fromJson(json);
      expect(dto.isAdmin, isFalse);
      expect(dto.date, DateTime(2026, 3, 20));
    });

    test('should deserialize fallback date when date is invalid or missing', () {
      final json = {'isAdmin': false};
      final before = DateTime.now().subtract(const Duration(seconds: 1));
      final dto = HouseholdMembershipResponseDTO.fromJson(json);
      final after = DateTime.now().add(const Duration(seconds: 1));

      expect(dto.date.isAfter(before), isTrue);
      expect(dto.date.isBefore(after), isTrue);
    });
  });

  group('HouseholdMemberResponseDTO', () {
    test('should serialize and deserialize nested user and membership', () {
      final user = UserResponseDTO(
        id: 'u-10',
        name: 'MemberName',
        surname: 'MemberSurname',
        email: 'member@house.org',
      );
      final membership = HouseholdMembershipResponseDTO(
        isAdmin: true,
        date: DateTime.parse('2026-05-01T00:00:00.000Z'),
      );

      final dto = HouseholdMemberResponseDTO(
        user: user,
        membership: membership,
      );

      final json = dto.toJson();
      expect(json['user'], user.toJson());
      expect(json['membership'], membership.toJson());

      final deserialized = HouseholdMemberResponseDTO.fromJson(json);
      expect(deserialized.user.id, 'u-10');
      expect(deserialized.membership.isAdmin, isTrue);
      expect(deserialized.membership.date, membership.date);
    });
  });

  group('HouseholdResponseDTO', () {
    test('should serialize and deserialize with full membership list', () {
      final user = UserResponseDTO(
        id: 'u-1',
        name: 'Alice',
        surname: 'Wonder',
        email: 'alice@house.org',
      );
      final membership = HouseholdMembershipResponseDTO(
        isAdmin: true,
        date: DateTime.parse('2026-01-01T00:00:00.000Z'),
      );
      final member = HouseholdMemberResponseDTO(
        user: user,
        membership: membership,
      );

      final creationDate = DateTime.parse('2026-01-01T00:00:00.000Z');
      final dto = HouseholdResponseDTO(
        id: 'house-1',
        name: 'Sweet Home',
        creationDate: creationDate,
        memberships: [member],
      );

      final json = dto.toJson();
      expect(json['id'], 'house-1');
      expect(json['name'], 'Sweet Home');
      expect(json['creationDate'], creationDate.toIso8601String());
      expect((json['memberships'] as List).length, 1);

      final deserialized = HouseholdResponseDTO.fromJson(json);
      expect(deserialized.id, 'house-1');
      expect(deserialized.name, 'Sweet Home');
      expect(deserialized.creationDate, creationDate);
      expect(deserialized.memberships.first.user.name, 'Alice');
    });

    test('should deserialize with list-format creationDate', () {
      final json = {
        'id': 'house-2',
        'name': 'Beach House',
        'creationDate': [2026, 6, 1, 12, 0, 0],
        'memberships': <Map<String, dynamic>>[],
      };

      final dto = HouseholdResponseDTO.fromJson(json);
      expect(dto.id, 'house-2');
      expect(dto.creationDate, DateTime(2026, 6, 1, 12, 0, 0));
      expect(dto.memberships, isEmpty);
    });
  });

  group('HouseholdCreateRequestDTO', () {
    test('should serialize and deserialize correctly', () {
      final dto = HouseholdCreateRequestDTO(name: 'New Household');
      expect(dto.name, 'New Household');

      final json = dto.toJson();
      expect(json, {'name': 'New Household'});

      final deserialized = HouseholdCreateRequestDTO.fromJson(json);
      expect(deserialized.name, 'New Household');
    });
  });

  group('AddMemberRequestDTO', () {
    test('should serialize and deserialize correctly', () {
      final dto = AddMemberRequestDTO(invitationCode: 'JOIN123');
      expect(dto.invitationCode, 'JOIN123');

      final json = dto.toJson();
      expect(json, {'invitationCode': 'JOIN123'});

      final deserialized = AddMemberRequestDTO.fromJson(json);
      expect(deserialized.invitationCode, 'JOIN123');
    });
  });
}
