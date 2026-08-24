import 'package:flutter/material.dart';
import '../../shared/dto/expense/response/settlement_response_dto.dart';
import '../../shared/enums/user_transaction_role.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import '../../shared/utils/format_utils.dart';
import 'shared/app_card.dart';

class SettlementItemElement extends StatelessWidget {
  final SettlementResponseDTO settlement;

  const SettlementItemElement({
    super.key,
    required this.settlement,
  });

  @override
  Widget build(BuildContext context) {
    final bool hasTime = settlement.date != null && (settlement.date!.hour != 0 || settlement.date!.minute != 0);

    final dateString = settlement.date != null
        ? (hasTime ? FormatUtils.formatExpenseDateTime(settlement.date!) : FormatUtils.formatShortDate(settlement.date!))
        : 'Unknown Date';

    final String directionLabel = settlement.userTransactionRole == UserTransactionRole.creditor
        ? "Received from"
        : "Paid to";

    final Color amountColor = settlement.userTransactionRole == UserTransactionRole.creditor
        ? AppColors.success
        : AppColors.danger;

    return AppCard(
      padding: const EdgeInsets.all(AppSpacing.l),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Container(
            padding: const EdgeInsets.all(AppSpacing.s),
            decoration: BoxDecoration(
              color: AppColors.background,
              borderRadius: BorderRadius.circular(AppSpacing.radiusS),
            ),
            child: const Icon(Icons.payments_outlined, color: AppColors.primary, size: 28),
          ),
          const SizedBox(width: AppSpacing.m),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  settlement.description ?? "Settlement",
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  "$directionLabel ${settlement.involvedName}",
                  style: const TextStyle(
                    color: AppColors.textPrimary,
                    fontWeight: FontWeight.w500,
                    fontSize: 13,
                  ),
                ),
                const SizedBox(height: AppSpacing.s),
                Text(
                  dateString,
                  style: const TextStyle(color: AppColors.textSecondary, fontSize: 12),
                ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.m),

          // Amount (Centered)
          Text(
            FormatUtils.formatCurrency((settlement.amount as num).toDouble()),
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: amountColor,
            ),
          ),
        ],
      ),
    );
  }
}
