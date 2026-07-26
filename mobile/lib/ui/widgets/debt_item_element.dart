import 'package:flutter/material.dart';
import '../../shared/dto/expense/response/debt_response_dto.dart';
import '../../shared/enums/user_transaction_role.dart';

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
    final Color amountColor = isOwed ? Colors.green : Colors.red;

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Row(
        children: [
          Expanded(
            child: Text(
              title,
              style: const TextStyle(fontSize: 16),
            ),
          ),
          Text(
            "€ ${debt.amount.toStringAsFixed(2)}",
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: amountColor,
            ),
          ),
          if (!isOwed) ...[
            const SizedBox(width: 10),
            ElevatedButton(
              onPressed: debt.amount > 0 ? () => onPay(debt) : null,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.blue,
                foregroundColor: Colors.white,
              ),
              child: const Text("Pay"),
            ),
          ],
        ],
      ),
    );
  }
}
