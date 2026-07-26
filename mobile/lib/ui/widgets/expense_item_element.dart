import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../shared/dto/expense/response/expense_response_dto.dart';

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
    final dateString = expense.date != null
        ? DateFormat('dd MMM').format(expense.date!)
        : 'Unknown Date';

    // Find the share for the current user
    final userShare = expense.shares
        .where((share) => share.userId == currentUserId)
        .map((share) => share.amount)
        .fold(0.0, (previousValue, element) => previousValue + element);

    Widget shareLabel;
    if (expense.payerId != currentUserId) {
      shareLabel = Text(
        "Your share: € ${userShare.toStringAsFixed(2)}",
        style: const TextStyle(color: Colors.red),
      );
    } else if (expense.amount - userShare > 0) {
      shareLabel = Text(
        "You are owed: € ${(expense.amount - userShare).toStringAsFixed(2)}",
        style: const TextStyle(color: Colors.green),
      );
    } else {
      shareLabel = Text(
        "Personal expense",
        style: TextStyle(color: Colors.grey.shade600),
      );
    }

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Row(
        children: [
          const Icon(Icons.receipt_long, color: Colors.blue),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  expense.description,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  "Paid by ${expense.payerFullName} • $dateString",
                  style: TextStyle(color: Colors.grey.shade600),
                ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                "€ ${expense.amount.toStringAsFixed(2)}",
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              shareLabel,
            ],
          ),
        ],
      ),
    );
  }
}
