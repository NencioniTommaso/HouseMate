import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/expense_provider.dart';
import '../../../../shared/enums/user_transaction_role.dart';
import '../../widgets/debt_item_element.dart';

void showYouAreOwedSheet(BuildContext context) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return const _YouAreOwedSheetContent();
    },
  );
}

class _YouAreOwedSheetContent extends StatelessWidget {
  const _YouAreOwedSheetContent();

  @override
  Widget build(BuildContext context) {
    final expenseProv = context.watch<ExpenseProvider>();
    final owedToMe = expenseProv.debts
        .where((d) => d.userTransactionRole == UserTransactionRole.creditor)
        .toList();

    return Container(
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.8,
      ),
      decoration: const BoxDecoration(
        color: Color(0xFFF5F5F5),
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      padding: const EdgeInsets.all(24.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'You are Owed',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF1E3A5F),
                ),
              ),
              IconButton(
                onPressed: () => Navigator.pop(context),
                icon: Container(
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(4),
                    border: Border.all(color: Colors.grey.shade300),
                  ),
                  child: const Icon(Icons.close, size: 20, color: Colors.grey),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          if (expenseProv.isLoading && owedToMe.isEmpty)
            const Expanded(
              child: Center(child: CircularProgressIndicator()),
            )
          else if (owedToMe.isEmpty)
            const Expanded(
              child: Center(child: Text('No one owes you money!')),
            )
          else
            Expanded(
              child: ListView.separated(
                itemCount: owedToMe.length,
                separatorBuilder: (context, index) => const SizedBox(height: 12),
                itemBuilder: (context, index) {
                  final debt = owedToMe[index];
                  return DebtItemElement(
                    debt: debt,
                    onPay: (_) {}, // Button hidden for creditors
                  );
                },
              ),
            ),
        ],
      ),
    );
  }
}
