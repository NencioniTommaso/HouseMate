import 'package:flutter/foundation.dart';
import 'reload_helper_stub.dart'
    if (dart.library.js_interop) 'reload_helper_web.dart'
    if (dart.library.html) 'reload_helper_web.dart';

/// Reloads the active web application window from the server.
/// Safe to call on all platforms (no-op on non-web platforms).
void reloadApp() {
  if (kIsWeb) {
    reloadAppPlatform();
  }
}
