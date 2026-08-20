import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/expense_provider.dart';
import '../../../../state/household_provider.dart';
import '../../../../shared/dto/expense/request/expense_create_request.dart';
import '../../../../shared/dto/expense/request/expense_share_request_dto.dart';
import '../../../../shared/enums/expense_split_type.dart';

void showCreateExpenseSheet(BuildContext context) {
  final TextEditingController descriptionController = TextEditingController();
  final TextEditingController amountController = TextEditingController();
  ExpenseSplitType selectedSplitType = ExpenseSplitType.equalSplit;
  
  // Track which members are involved and their share values if needed
  final Map<String, bool> involvedMembers = {};
  final Map<String, TextEditingController> shareControllers = {};

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return StatefulBuilder(
        builder: (context, setModalState) {
          final householdProv = context.read<HouseholdProvider>();
          final members = householdProv.currentHousehold?.memberships ?? [];

          // Initialize members if not already done
          if (involvedMembers.isEmpty && members.isNotEmpty) {
            for (var m in members) {
              involvedMembers[m.user.id] = true;
              shareControllers[m.user.id] = TextEditingController();
            }
          }

          return Container(
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
                  const Text(
                    'Add New Expense',
                    style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 24),
                  TextField(
                    controller: descriptionController,
                    decoration: const InputDecoration(
                      labelText: 'Description',
                      border: OutlineInputBorder(),
                      hintText: 'e.g. Pizza Night',
                    ),
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: amountController,
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(
                      labelText: 'Total Amount',
                      prefixText: '€ ',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 16),
                  DropdownButtonFormField<ExpenseSplitType>(
                    value: selectedSplitType,
                    decoration: const InputDecoration(
                      labelText: 'Split Strategy',
                      border: OutlineInputBorder(),
                    ),
                    items: ExpenseSplitType.values.map((type) {
                      return DropdownMenuItem(
                        value: type,
                        child: Text(type.name.toUpperCase().replaceAll('_', ' ')),
                      );
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) {
                        setModalState(() {
                          selectedSplitType = val;
                        });
                      }
                    },
                  ),
                  const SizedBox(height: 24),
                  const Text(
                    'Who is involved?',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  ...members.map((m) {
                    final userId = m.user.id;
                    return CheckboxListTile(
                      title: Text("${m.user.name} ${m.user.surname}"),
                      value: involvedMembers[userId],
                      onChanged: (val) {
                        setModalState(() {
                          involvedMembers[userId] = val ?? false;
                        });
                      },
                      secondary: selectedSplitType != ExpenseSplitType.equalSplit && involvedMembers[userId] == true
                          ? SizedBox(
                              width: 80,
                              child: TextField(
                                controller: shareControllers[userId],
                                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                                decoration: InputDecoration(
                                  hintText: selectedSplitType == ExpenseSplitType.shares ? 'Shares' : 'Amount',
                                  isDense: true,
                                ),
                              ),
                            )
                          : null,
                    );
                  }),
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
                                    final double? totalAmount = double.tryParse(amountController.text);
                                    if (totalAmount == null || totalAmount <= 0) {
                                      ScaffoldMessenger.of(context).showSnackBar(
                                        const SnackBar(content: Text('Please enter a valid total amount')),
                                      );
                                      return;
                                    }

                                    final List<ExpenseShareRequestDTO> shares = [];
                                    for (var userId in involvedMembers.keys) {
                                      if (involvedMembers[userId] == true) {
                                        double? shareVal;
                                        if (selectedSplitType != ExpenseSplitType.equalSplit) {
                                          shareVal = double.tryParse(shareControllers[userId]!.text);
                                        }
                                        shares.add(ExpenseShareRequestDTO(
                                          userId: userId,
                                          share: shareVal,
                                        ));
                                      }
                                    }

                                    if (shares.isEmpty) {
                                      ScaffoldMessenger.of(context).showSnackBar(
                                        const SnackBar(content: Text('At least one person must be involved')),
                                      );
                                      return;
                                    }

                                    final request = ExpenseCreateRequestDTO(
                                      description: descriptionController.text,
                                      amount: totalAmount,
                                      splitType: selectedSplitType,
                                      shares: shares,
                                    );

                                    final success = await provider.createExpense(request);
                                    if (success && context.mounted) {
                                      Navigator.pop(context);
                                    }
                                  },
                            child: provider.isLoading
                                ? const SizedBox(
                                    height: 20,
                                    width: 20,
                                    child: CircularProgressIndicator(strokeWidth: 2))
                                : const Text('Add Expense'),
                          ),
                        ],
                      );
                    },
                  ),
                  const SizedBox(height: 24),
                ],
              ),
            ),
          );
        },
      );
    },
  );
}
