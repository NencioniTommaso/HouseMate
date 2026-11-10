import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/constants/app_version.dart';
import 'package:mobile/services/update_service.dart';

import 'test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late UpdateService updateService;
  late MockHttpClientAdapter mockAdapter;
  late Dio dio;

  setUp(() {
    updateService = UpdateService();
    updateService.resetForTesting();

    mockAdapter = MockHttpClientAdapter();
    dio = Dio();
    dio.httpClientAdapter = mockAdapter;
    updateService.dioOverride = dio;
  });

  tearDown(() {
    updateService.resetForTesting();
  });

  group('UpdateService - Singleton & Defaults', () {
    test('returns same singleton instance', () {
      final instance1 = UpdateService();
      final instance2 = UpdateService();
      expect(identical(instance1, instance2), isTrue);
    });

    test('updateAvailable defaults to false', () {
      expect(updateService.updateAvailable.value, isFalse);
    });
  });

  group('UpdateService - Non-web environment', () {
    test('does nothing when isWeb is false', () async {
      updateService.isWebOverride = false;

      await updateService.checkForUpdate();

      expect(mockAdapter.requests, isEmpty);
      expect(updateService.updateAvailable.value, isFalse);
    });

    test('startChecking returns without starting timer when isWeb is false', () {
      updateService.isWebOverride = false;

      updateService.startChecking(interval: const Duration(seconds: 1));

      expect(mockAdapter.requests, isEmpty);
      updateService.stop();
    });
  });

  group('UpdateService - Web environment with update detection', () {
    setUp(() {
      updateService.isWebOverride = true;
    });

    test('detects update when server build number differs from kAppBuildNumber', () async {
      final differentBuildNumber = (int.parse(kAppBuildNumber) + 1).toString();
      mockAdapter.reply(200, {'build_number': differentBuildNumber});

      await updateService.checkForUpdate();

      expect(mockAdapter.requests.length, 1);
      final req = mockAdapter.lastRequest!;
      expect(req.path, contains('/version.json?t='));
      expect(updateService.updateAvailable.value, isTrue);
    });

    test('does not trigger update when server build number matches kAppBuildNumber', () async {
      mockAdapter.reply(200, {'build_number': kAppBuildNumber});

      await updateService.checkForUpdate();

      expect(mockAdapter.requests.length, 1);
      expect(updateService.updateAvailable.value, isFalse);
    });

    test('handles JSON string data from response', () async {
      final differentBuildNumber = (int.parse(kAppBuildNumber) + 5).toString();
      mockAdapter.setHandler((options) {
        return ResponseBody.fromString(
          jsonEncode({'build_number': differentBuildNumber}),
          200,
          headers: {
            Headers.contentTypeHeader: ['text/plain'],
          },
        );
      });

      await updateService.checkForUpdate();

      expect(updateService.updateAvailable.value, isTrue);
    });

    test('ignores response without build_number', () async {
      mockAdapter.reply(200, {'other_field': 'value'});

      await updateService.checkForUpdate();

      expect(updateService.updateAvailable.value, isFalse);
    });

    test('fails silently on network error or server exception', () async {
      mockAdapter.replyError(DioExceptionType.connectionError);

      await updateService.checkForUpdate();

      expect(updateService.updateAvailable.value, isFalse);
    });

    test('skips checking if updateAvailable is already true', () async {
      updateService.updateAvailable.value = true;

      await updateService.checkForUpdate();

      expect(mockAdapter.requests, isEmpty);
    });

    test('startChecking runs initial check and periodic timer, can be stopped', () async {
      mockAdapter.reply(200, {'build_number': kAppBuildNumber});

      updateService.startChecking(interval: const Duration(milliseconds: 100));
      await pumpEventQueue();

      // Initial check was triggered
      expect(mockAdapter.requests.length, 1);

      updateService.stop();
    });
  });
}
