import 'package:mobile/core/utils/ui_service.dart';

class FakeUiService extends UiService {
  final List<String> errorMessages = [];
  final List<String> successMessages = [];
  final List<String> infoMessages = [];

  @override
  void showError(String message) {
    errorMessages.add(message);
  }

  @override
  void showSuccess(String message) {
    successMessages.add(message);
  }

  @override
  void showInfo(String message) {
    infoMessages.add(message);
  }

  void clear() {
    errorMessages.clear();
    successMessages.clear();
    infoMessages.clear();
  }
}
