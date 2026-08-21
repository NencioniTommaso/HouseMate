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
        style: const TextStyle(color: Color(0xFFE74C3C), fontWeight: FontWeight.bold),
      );
    } else if (expense.amount - userShare > 0) {
      shareLabel = Text(
        "You are owed: € ${(expense.amount - userShare).toStringAsFixed(2)}",
        style: const TextStyle(color: Color(0xFF4CAF50), fontWeight: FontWeight.bold),
      );
    } else {
      shareLabel = const Text(
        "Personal expense",
        style: TextStyle(color: Color(0xFF7F8C8D)),
      );
    }

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
            child: const Icon(Icons.shopping_cart_outlined, color: Color(0xFF2C3E50), size: 24),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  expense.description,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF2C3E50),
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  "Paid by ${expense.payerFullName} • $dateString",
                  style: const TextStyle(color: Color(0xFF95A5A6), fontSize: 11),
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
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF2C3E50),
                ),
              ),
              const SizedBox(height: 4),
              shareLabel,
            ],
          ),
        ],
      ),
    );
  }
}
