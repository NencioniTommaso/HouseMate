import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/expense_provider.dart';
import '../../../../shared/dto/expense/response/debt_response_dto.dart';

void showSettleDebtSheet(BuildContext context, DebtResponseDTO debt) {
  final TextEditingController amountController =
      TextEditingController(text: debt.amount.toStringAsFixed(2));

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
          left: 24,
          right: 24,
          top: 24,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'Settle Debt',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            Text(
              'Confirm payment to ${debt.involvedName}. You can adjust the amount if you are making a partial payment.',
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 24),
            TextField(
              controller: amountController,
              autofocus: true,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              decoration: const InputDecoration(
                labelText: 'Settlement Amount',
                prefixText: '€ ',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 24),
            Consumer<ExpenseProvider>(
              builder: (context, provider, child) {
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    if (provider.errorMessage != null)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 16),
                        child: Text(
                          provider.errorMessage!,
                          style: const TextStyle(color: Colors.red),
                          textAlign: TextAlign.center,
                        ),
                      ),
                    ElevatedButton(
                      onPressed: provider.isLoading
                          ? null
                          : () async {
                              final double? amount =
                                  double.tryParse(amountController.text);
                              if (amount == null || amount <= 0) {
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                      content: Text('Please enter a valid amount')),
                                );
                                return;
                              }

                              final success = await provider.settleDebt(
                                  debt.debtId, debt.involvedId, amount);
                              if (success && context.mounted) {
                                Navigator.pop(context);
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                      content: Text('Settlement successful!')),
                                );
                              }
                            },
                      child: provider.isLoading
                          ? const SizedBox(
                              height: 20,
                              width: 20,
                              child: CircularProgressIndicator(
                                  strokeWidth: 2))
                          : const Text('Confirm Settlement'),
                    ),
                  ],
                );
              },
            ),
            const SizedBox(height: 24),
          ],
        ),
      );
    },
  );
}
