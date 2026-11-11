import 'package:flutter/material.dart';
import '../../shared/dto/expense/response/debt_response_dto.dart';
import '../../shared/enums/user_transaction_role.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import '../../shared/utils/format_utils.dart';
import 'shared/app_card.dart';
import 'shared/app_button.dart';

class DebtItemElement extends StatelessWidget {
  final DebtResponseDTO debt;
  final Function(DebtResponseDTO) onPay;

  const DebtItemElement({
    super.key,
    required this.debt,
    required this.onPay,
  });

  @override
  Widget build(BuildContext context) {
    final bool isOwed = debt.userTransactionRole == UserTransactionRole.creditor;
    final String title = isOwed
        ? "${debt.involvedName} owes you"
        : "Debt to ${debt.involvedName}";
    final Color amountColor = isOwed ? AppColors.success : AppColors.danger;

    return AppCard(
      margin: const EdgeInsets.only(bottom: AppSpacing.s),
      padding: const EdgeInsets.all(AppSpacing.m),
      child: Row(
        children: [
          Expanded(
            child: Text(
              title,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.textPrimary),
            ),
          ),
          Text(
            FormatUtils.formatCurrency((debt.amount as num).toDouble()),
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: amountColor,
            ),
          ),
          if (!isOwed) ...[
            const SizedBox(width: AppSpacing.m),
            AppButton(
              label: "Pay",
              onPressed: debt.amount > 0 ? () => onPay(debt) : null,
            ),
          ],
        ],
      ),
    );
  }
}
