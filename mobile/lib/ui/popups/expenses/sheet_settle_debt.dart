import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../../../state/expense_provider.dart';
import '../../../../state/household_provider.dart';
import '../../../../shared/dto/expense/response/debt_response_dto.dart';
import 'package:collection/collection.dart';

void showSettleDebtSheet(BuildContext context, DebtResponseDTO debt) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return _SettleDebtSheetContent(debt: debt);
    },
  );
}

class _SettleDebtSheetContent extends StatefulWidget {
  final DebtResponseDTO debt;
  const _SettleDebtSheetContent({required this.debt});

  @override
  State<_SettleDebtSheetContent> createState() => _SettleDebtSheetContentState();
}

class _SettleDebtSheetContentState extends State<_SettleDebtSheetContent> {
  late TextEditingController _amountController;
  late TextEditingController _descriptionController;
  late double _currentValue;

  @override
  void initState() {
    super.initState();
    _currentValue = widget.debt.amount;
    _amountController = TextEditingController(text: _currentValue.toStringAsFixed(2));
    _descriptionController = TextEditingController();
  }

  @override
  void dispose() {
    _amountController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  void _updateAmount(double val) {
    setState(() {
      _currentValue = val;
      _amountController.text = _currentValue.toStringAsFixed(2);
    });
  }

  @override
  Widget build(BuildContext context) {
    final expenseProv = context.watch<ExpenseProvider>();
    final householdProv = context.read<HouseholdProvider>();
    
    // Find creditor user info
    final membership = householdProv.currentHousehold?.memberships.firstWhereOrNull(
      (m) => m.user.id == widget.debt.involvedId,
    );
    final creditor = membership?.user;

    return Padding(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom,
        left: 24,
        right: 24,
        top: 24,
      ),
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Settle Debt',
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF1E3A5F),
                  ),
                ),
                InkWell(
                  onTap: () => Navigator.pop(context),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: Colors.grey.shade100,
                      borderRadius: BorderRadius.circular(4),
                      border: Border.all(color: Colors.grey.shade300),
                    ),
                    child: const Text(
                      'X',
                      style: TextStyle(
                        color: Colors.grey,
                        fontWeight: FontWeight.bold,
                        fontSize: 14,
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),

            const Text(
              'Amount to pay back:',
              style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1E3A5F)),
            ),
            const SizedBox(height: 8),
            Slider(
              value: _currentValue,
              min: 0,
              max: widget.debt.amount,
              activeColor: const Color(0xFF3498DB),
              onChanged: _updateAmount,
            ),
            const SizedBox(height: 16),

            const Text(
              'Exact amount:',
              style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1E3A5F)),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _amountController,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              onChanged: (val) {
                final double? parsed = double.tryParse(val);
                if (parsed != null && parsed >= 0 && parsed <= widget.debt.amount) {
                  setState(() {
                    _currentValue = parsed;
                  });
                }
              },
              decoration: InputDecoration(
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                contentPadding: const EdgeInsets.symmetric(horizontal: 12),
              ),
            ),
            const SizedBox(height: 16),

            const Text(
              'Description:',
              style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1E3A5F)),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _descriptionController,
              decoration: InputDecoration(
                hintText: 'Add a message (optional)...',
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                contentPadding: const EdgeInsets.symmetric(horizontal: 12),
              ),
            ),
            const SizedBox(height: 24),

            const Text(
              'Creditor\'s payment information:',
              style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1E3A5F)),
            ),
            const SizedBox(height: 12),
            if (creditor?.iban != null && creditor!.iban!.isNotEmpty)
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.account_balance, size: 20),
                title: Text(creditor.iban!, style: const TextStyle(fontSize: 14)),
                trailing: IconButton(
                  icon: const Icon(Icons.copy, size: 18),
                  onPressed: () {
                    Clipboard.setData(ClipboardData(text: creditor.iban!));
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('IBAN copied to clipboard')),
                    );
                  },
                ),
              )
            else
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 4),
                child: Text('Creditor has no IBAN', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
              ),

            if (creditor?.paymentLink != null && creditor!.paymentLink!.isNotEmpty)
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.link, size: 20),
                title: InkWell(
                  onTap: () async {
                    final url = Uri.parse(creditor.paymentLink!);
                    if (await canLaunchUrl(url)) {
                      await launchUrl(url, mode: LaunchMode.externalApplication);
                    }
                  },
                  child: Text(
                    creditor.paymentLink!,
                    style: const TextStyle(
                      fontSize: 14,
                      color: Colors.blue,
                      decoration: TextDecoration.underline,
                    ),
                  ),
                ),
              )
            else
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 4),
                child: Text('Creditor has no payment link', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
              ),

            const SizedBox(height: 32),

            Row(
              children: [
                ElevatedButton(
                  onPressed: expenseProv.isLoading || _currentValue <= 0
                      ? null
                      : () async {
                          final success = await expenseProv.settleDebt(
                            widget.debt.debtId,
                            widget.debt.involvedId,
                            _currentValue,
                          );
                          if (success && context.mounted) {
                            Navigator.pop(context);
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Settlement successful!')),
                            );
                          }
                        },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF3498DB),
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                  child: expenseProv.isLoading
                      ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                      : const Text('Settle Debt'),
                ),
                const SizedBox(width: 12),
                ElevatedButton(
                  onPressed: () => Navigator.pop(context),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.grey.shade200,
                    foregroundColor: Colors.grey.shade600,
                    padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                    elevation: 0,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                  child: const Text('Cancel'),
                ),
              ],
            ),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }
}
