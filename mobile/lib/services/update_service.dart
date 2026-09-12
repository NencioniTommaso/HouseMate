import 'dart:async';
import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../core/constants/app_version.dart';

class UpdateService {
  static final UpdateService _instance = UpdateService._internal();
  factory UpdateService() => _instance;
  UpdateService._internal();

  final ValueNotifier<bool> updateAvailable = ValueNotifier<bool>(false);
  Timer? _pollingTimer;

  void startChecking({Duration interval = const Duration(minutes: 15)}) {
    if (!kIsWeb) return;

    // Initial check on app startup
    checkForUpdate();

    // Background periodic checking
    _pollingTimer?.cancel();
    _pollingTimer = Timer.periodic(interval, (_) => checkForUpdate());
  }

  Future<void> checkForUpdate() async {
    if (!kIsWeb || updateAvailable.value) return;

    try {
      final uri = Uri.base.resolve('/version.json?t=${DateTime.now().millisecondsSinceEpoch}');
      final dio = Dio();
      final response = await dio.get(uri.toString());

      if (response.statusCode == 200 && response.data != null) {
        final dynamic rawData = response.data;
        final Map<String, dynamic> data = rawData is String
            ? jsonDecode(rawData) as Map<String, dynamic>
            : Map<String, dynamic>.from(rawData as Map);

        final String? serverBuildNumber = data['build_number']?.toString();

        if (serverBuildNumber != null && serverBuildNumber != kAppBuildNumber) {
          debugPrint(
            'Version mismatch detected: server ($serverBuildNumber) vs client ($kAppBuildNumber)',
          );
          updateAvailable.value = true;
        }
      }
    } catch (e) {
      // Network drop or offline; fail silently
      debugPrint('Update check failed: $e');
    }
  }

  void stop() {
    _pollingTimer?.cancel();
  }
}
