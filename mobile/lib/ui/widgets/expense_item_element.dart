import 'package:flutter/material.dart';
import '../../shared/dto/expense/response/expense_response_dto.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import '../../shared/utils/format_utils.dart';
import 'shared/app_card.dart';

class ExpenseItemElement extends StatelessWidget {
  final ExpenseResponseDTO expense;
  final String currentUserId;

  const ExpenseItemElement({
    super.key,
    required this.expense,
    required this.currentUserId,
  });

  @override
  Widget build(BuildContext context) {
    final bool hasTime = expense.date != null && (expense.date!.hour != 0 || expense.date!.minute != 0);
    
    final dateString = expense.date != null
        ? (hasTime ? FormatUtils.formatExpenseDateTime(expense.date!) : FormatUtils.formatShortDate(expense.date!))
        : 'Unknown Date';

    // Find the share for the current user
    final userShare = expense.shares
        .where((share) => share.userId == currentUserId)
        .map((share) => (share.amount as num).toDouble())
        .fold(0.0, (previousValue, element) => previousValue + element);

    Widget shareLabel;
    if (expense.payerId != currentUserId) {
      shareLabel = Text(
        "Your share: ${FormatUtils.formatCurrency(userShare)}",
        style: const TextStyle(color: AppColors.danger, fontWeight: FontWeight.bold, fontSize: 12),
      );
    } else if ((expense.amount as num).toDouble() - userShare > 0.01) {
      shareLabel = Text(
        "You are owed: ${FormatUtils.formatCurrency((expense.amount as num).toDouble() - userShare)}",
        style: const TextStyle(color: AppColors.success, fontWeight: FontWeight.bold, fontSize: 12),
      );
    } else {
      shareLabel = const Text(
        "Personal expense",
        style: TextStyle(color: AppColors.textSecondary, fontSize: 12),
      );
    }

    return AppCard(
      padding: const EdgeInsets.all(AppSpacing.l),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          // 1. Cart Icon (Centered)
          Container(
            padding: const EdgeInsets.all(AppSpacing.s),
            decoration: BoxDecoration(
              color: AppColors.background,
              borderRadius: BorderRadius.circular(AppSpacing.radiusS),
            ),
            child: const Icon(Icons.shopping_cart_outlined, color: AppColors.primary, size: 28),
          ),
          const SizedBox(width: AppSpacing.m),
          
          // 2. Middle Content (Description, Paid By, Date + Share Label Row)
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  expense.description,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  "Paid by ${expense.payerFullName}",
                  style: const TextStyle(
                    color: AppColors.textPrimary,
                    fontWeight: FontWeight.w500,
                    fontSize: 13,
                  ),
                ),
                Text(
                  dateString,
                  style: const TextStyle(color: AppColors.textSecondary, fontSize: 12),
                ),
                const SizedBox(height: AppSpacing.s),
                shareLabel,
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.m),

          // 3. Amount (Centered)
          Text(
            FormatUtils.formatCurrency((expense.amount as num).toDouble()),
            style: const TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }
}
