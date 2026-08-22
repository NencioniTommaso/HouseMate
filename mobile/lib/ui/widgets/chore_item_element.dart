import 'package:flutter/material.dart';
import '../../shared/dto/chore/response/chore_response_dto.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import 'shared/app_card.dart';
import 'shared/app_button.dart';

class ChoreItemElement extends StatelessWidget {
  final ChoreResponseDTO chore;
  final VoidCallback onDelete;
  final bool isAdminMode;

  const ChoreItemElement({
    super.key,
    required this.chore,
    required this.onDelete,
    required this.isAdminMode,
  });

  String _getFrequencyText(int days) {
    if (days == 0) return "Frequency: not periodical";
    if (days == 1) return "Frequency: every day";
    return "Frequency: every $days days";
  }

  @override
  Widget build(BuildContext context) {
    return AppCard(
      margin: const EdgeInsets.only(bottom: AppSpacing.s),
      padding: const EdgeInsets.all(AppSpacing.m),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  chore.description,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  _getFrequencyText(chore.frequencyDays),
                  style: const TextStyle(
                    fontSize: 11,
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
          if (isAdminMode)
            AppButton(
              label: "Delete",
              variant: AppButtonVariant.destructive,
              onPressed: onDelete,
            ),
        ],
      ),
    );
  }
}
