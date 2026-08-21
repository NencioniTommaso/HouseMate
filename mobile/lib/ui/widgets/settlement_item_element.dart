import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../shared/dto/expense/response/settlement_response_dto.dart';
import '../../shared/enums/user_transaction_role.dart';

class SettlementItemElement extends StatelessWidget {
  final SettlementResponseDTO settlement;

  const SettlementItemElement({
    super.key,
    required this.settlement,
  });

  @override
  Widget build(BuildContext context) {
    final dateString = settlement.date != null
        ? DateFormat('dd MMM').format(settlement.date!)
        : 'Unknown Date';

    final String detailsText = settlement.userTransactionRole == UserTransactionRole.creditor
        ? "From ${settlement.involvedName} • $dateString"
        : "To ${settlement.involvedName} • $dateString";

    final Color amountColor = settlement.userTransactionRole == UserTransactionRole.creditor
        ? const Color(0xFF4CAF50)
        : const Color(0xFFE74C3C);

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: const Color(0xFFE0E0E0)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 5,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: Colors.grey.shade50,
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Icon(Icons.payments_outlined, color: Color(0xFF2C3E50), size: 24),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  settlement.description ?? "Settlement",
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF2C3E50),
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  detailsText,
                  style: const TextStyle(color: Color(0xFF95A5A6), fontSize: 11),
                ),
              ],
            ),
          ),
          Text(
            "€ ${settlement.amount.toStringAsFixed(2)}",
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: amountColor,
            ),
          ),
        ],
      ),
    );
  }
}
