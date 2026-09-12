import 'package:flutter/material.dart';
import '../theme/app_colors.dart';

class UiService {
  final GlobalKey<ScaffoldMessengerState> messengerKey = GlobalKey<ScaffoldMessengerState>();
  final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();
  
  OverlayEntry? _currentOverlay;

  void showSuccess(String message) {
    _showOverlayToast(message, AppColors.success);
  }

  void showError(String message) {
    _showOverlayToast(message, AppColors.danger);
  }

  void showInfo(String message) {
    _showOverlayToast(message, AppColors.info);
  }

  void _showOverlayToast(String message, Color backgroundColor) {
    // 1. Remove any existing toast
    _currentOverlay?.remove();
    
    // 2. Create the new OverlayEntry
    _currentOverlay = OverlayEntry(
      builder: (context) => _OverlayToastWidget(
        message: message,
        backgroundColor: backgroundColor,
        onDismiss: () {
          _currentOverlay?.remove();
          _currentOverlay = null;
        },
      ),
    );

    // 3. Insert into the navigator's overlay
    final overlay = navigatorKey.currentState?.overlay;
    if (overlay != null) {
      overlay.insert(_currentOverlay!);
    }
  }
}

class _OverlayToastWidget extends StatefulWidget {
  final String message;
  final Color backgroundColor;
  final VoidCallback onDismiss;

  const _OverlayToastWidget({
    required this.message,
    required this.backgroundColor,
    required this.onDismiss,
  });

  @override
  State<_OverlayToastWidget> createState() => _OverlayToastWidgetState();
}

class _OverlayToastWidgetState extends State<_OverlayToastWidget> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _opacity;
  late Animation<Offset> _offset;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 300),
    );

    _opacity = CurvedAnimation(parent: _controller, curve: Curves.easeIn);
    _offset = Tween<Offset>(
      begin: const Offset(0, 0.5),
      end: Offset.zero,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOutBack));

    _controller.forward();

    // Auto-dismiss after 3 seconds
    Future.delayed(const Duration(seconds: 3), () {
      if (mounted) {
        _controller.reverse().then((_) => widget.onDismiss());
      }
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Positioned(
      bottom: 50,
      left: 20,
      right: 20,
      child: Material(
        color: Colors.transparent,
        child: FadeTransition(
          opacity: _opacity,
          child: SlideTransition(
            position: _offset,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              decoration: BoxDecoration(
                color: widget.backgroundColor,
                borderRadius: BorderRadius.circular(8),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.2),
                    blurRadius: 10,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Text(
                widget.message,
                style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w500),
                textAlign: TextAlign.center,
              ),
            ),
          ),
        ),
      ),
    );
  }
}
