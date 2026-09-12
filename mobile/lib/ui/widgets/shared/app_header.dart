import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';

class AppHeader extends StatelessWidget {
  final String title;
  final String? subtitle;
  final Widget? action;
  final bool centered;

  const AppHeader({
    super.key,
    required this.title,
    this.subtitle,
    this.action,
    this.centered = false,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: centered ? CrossAxisAlignment.center : CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Text(
                title,
                textAlign: centered ? TextAlign.center : TextAlign.start,
                style: const TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textPrimary,
                ),
              ),
            ),
            if (action != null) ...[
              const SizedBox(width: AppSpacing.m),
              action!,
            ],
          ],
        ),
        if (subtitle != null) ...[
          const SizedBox(height: AppSpacing.xs),
          Text(
            subtitle!,
            textAlign: centered ? TextAlign.center : TextAlign.start,
            style: const TextStyle(
              color: AppColors.textSecondary,
              fontWeight: FontWeight.bold,
              fontSize: 13,
            ),
          ),
        ],
      ],
    );
  }
}
