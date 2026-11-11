import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/expense_provider.dart';
import '../../../../shared/enums/user_transaction_role.dart';
import '../../widgets/debt_item_element.dart';
import 'sheet_settle_debt.dart';

void showYourDebtsSheet(BuildContext context) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return const _YourDebtsSheetContent();
    },
  );
}

class _YourDebtsSheetContent extends StatelessWidget {
  const _YourDebtsSheetContent();

  @override
  Widget build(BuildContext context) {
    final expenseProv = context.watch<ExpenseProvider>();
    final myDebts = expenseProv.debts
        .where((d) => d.userTransactionRole == UserTransactionRole.debtor)
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
                'Your Debts',
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
          if (expenseProv.isLoading && myDebts.isEmpty)
            const Expanded(
              child: Center(child: CircularProgressIndicator()),
            )
          else if (myDebts.isEmpty)
            const Expanded(
              child: Center(child: Text('You have no debts!')),
            )
          else
            Expanded(
              child: ListView.separated(
                itemCount: myDebts.length,
                separatorBuilder: (context, index) => const SizedBox(height: 12),
                itemBuilder: (context, index) {
                  final debt = myDebts[index];
                  return DebtItemElement(
                    debt: debt,
                    onPay: (d) => showSettleDebtSheet(context, d),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }
}
