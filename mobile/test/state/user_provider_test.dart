import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/user_service.dart';
import 'package:mobile/shared/dto/user/request/user_update_request_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';
import 'package:mobile/state/user_provider.dart';

import '../services/test_helpers.dart';
import 'state_test_helpers.dart';

class FakeUserService extends UserService {
  FakeUserService(super.apiClient);

  UserResponseDTO mockUser = UserResponseDTO(
    id: 'user-001',
    name: 'Alice',
    surname: 'Wonderland',
    email: 'alice@wonderland.com',
    iban: 'NL91ABNA0417164300',
    paymentLink: 'https://pay.me/alice',
  );

  Exception? errorToThrow;
  UserUpdateRequestDTO? lastUpdateRequest;

  @override
  Future<UserResponseDTO> getCurrentUser() async {
    if (errorToThrow != null) throw errorToThrow!;
    return mockUser;
  }

  @override
  Future<UserResponseDTO> updateCurrentUser(UserUpdateRequestDTO request) async {
    lastUpdateRequest = request;
    if (errorToThrow != null) throw errorToThrow!;
    return UserResponseDTO(
      id: mockUser.id,
      name: request.name ?? mockUser.name,
      surname: request.surname ?? mockUser.surname,
      email: request.email ?? mockUser.email,
      iban: request.iban ?? mockUser.iban,
      paymentLink: request.paymentLink ?? mockUser.paymentLink,
    );
  }
}

void main() {
  late TestContext testContext;
  late FakeUserService fakeUserService;
  late FakeUiService fakeUiService;
  late UserProvider provider;

  setUp(() {
    testContext = createTestContext();
    fakeUserService = FakeUserService(testContext.apiClient);
    fakeUiService = FakeUiService();

    provider = UserProvider(
      apiClient: testContext.apiClient,
      uiService: fakeUiService,
      userService: fakeUserService,
    );
  });

  group('UserProvider - Initial State', () {
    test('initial values are null/empty and loading is false', () {
      expect(provider.currentUser, isNull);
      expect(provider.isLoading, isFalse);
      expect(provider.errorMessage, isNull);
    });
  });

  group('UserProvider - loadUserProfile', () {
    test('successfully loads profile, sets currentUser, and notifies listeners', () async {
      final notifications = <bool>[];
      provider.addListener(() {
        notifications.add(provider.isLoading);
      });

      await provider.loadUserProfile();

      expect(provider.currentUser?.id, 'user-001');
      expect(provider.currentUser?.name, 'Alice');
      expect(provider.isLoading, isFalse);
      expect(provider.errorMessage, isNull);
      expect(notifications, contains(true)); // loading state flipped
    });

    test('handles ApiException by setting error and showing in UI', () async {
      fakeUserService.errorToThrow = ApiException(message: 'Unauthorized', statusCode: 401);

      await provider.loadUserProfile();

      expect(provider.currentUser, isNull);
      expect(provider.errorMessage, 'Unauthorized');
      expect(fakeUiService.errorMessages, contains('Unauthorized'));
      expect(provider.isLoading, isFalse);
    });

    test('handles generic unexpected Exception by showing generic message', () async {
      fakeUserService.errorToThrow = Exception('Network crash');

      await provider.loadUserProfile();

      expect(provider.currentUser, isNull);
      expect(provider.errorMessage, contains('unexpected error occurred'));
      expect(fakeUiService.errorMessages, isNotEmpty);
      expect(provider.isLoading, isFalse);
    });
  });

  group('UserProvider - updateUserProfile', () {
    test('successfully updates profile, shows success, and returns true', () async {
      final request = UserUpdateRequestDTO(
        name: 'Alicia',
        surname: 'Keys',
        iban: 'NL99INGB0001234567',
        paymentLink: 'https://pay.me/aliciakeys',
      );

      final success = await provider.updateUserProfile(request);

      expect(success, isTrue);
      expect(fakeUserService.lastUpdateRequest?.name, 'Alicia');
      expect(provider.currentUser?.name, 'Alicia');
      expect(provider.currentUser?.surname, 'Keys');
      expect(fakeUiService.successMessages, contains('Profile updated successfully'));
      expect(provider.errorMessage, isNull);
    });

    test('handles ApiException on update and returns false', () async {
      fakeUserService.errorToThrow = ApiException(message: 'Invalid IBAN', statusCode: 400);

      final request = UserUpdateRequestDTO(iban: 'INVALID');
      final success = await provider.updateUserProfile(request);

      expect(success, isFalse);
      expect(provider.errorMessage, 'Invalid IBAN');
      expect(fakeUiService.errorMessages, contains('Invalid IBAN'));
      expect(provider.isLoading, isFalse);
    });

    test('handles generic exception on update and returns false', () async {
      fakeUserService.errorToThrow = Exception('Server timeout');

      final request = UserUpdateRequestDTO(name: 'Alicia');
      final success = await provider.updateUserProfile(request);

      expect(success, isFalse);
      expect(provider.errorMessage, contains('unexpected error occurred while updating profile'));
      expect(fakeUiService.errorMessages, isNotEmpty);
      expect(provider.isLoading, isFalse);
    });
  });

  group('UserProvider - clear', () {
    test('resets all state variables and notifies listeners', () async {
      await provider.loadUserProfile();
      expect(provider.currentUser, isNotNull);

      provider.clear();

      expect(provider.currentUser, isNull);
      expect(provider.isLoading, isFalse);
      expect(provider.errorMessage, isNull);
    });
  });
}
