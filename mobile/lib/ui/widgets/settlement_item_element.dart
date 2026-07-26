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
        ? Colors.green
        : Colors.red;

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Row(
        children: [
          const Icon(Icons.handshake, color: Colors.teal),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  settlement.description ?? "Settlement",
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  detailsText,
                  style: TextStyle(color: Colors.grey.shade600),
                ),
              ],
            ),
          ),
          Text(
            "€ ${settlement.amount.toStringAsFixed(2)}",
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
