import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/expense_provider.dart';
import '../../../../state/household_provider.dart';
import '../../../../shared/dto/expense/request/expense_create_request.dart';
import '../../../../shared/dto/expense/request/expense_share_request_dto.dart';
import '../../../../shared/enums/expense_split_type.dart';
import '../../widgets/member_split_box.dart';

void showCreateExpenseSheet(BuildContext context) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return const _CreateExpenseSheetContent();
    },
  );
}

class _CreateExpenseSheetContent extends StatefulWidget {
  const _CreateExpenseSheetContent();

  @override
  State<_CreateExpenseSheetContent> createState() => _CreateExpenseSheetContentState();
}

class _CreateExpenseSheetContentState extends State<_CreateExpenseSheetContent> {
  final TextEditingController _descriptionController = TextEditingController();
  final TextEditingController _amountController = TextEditingController();
  ExpenseSplitType _selectedSplitType = ExpenseSplitType.equalSplit;

  final Map<String, bool> _isIncluded = {};
  final Map<String, int> _shareCounts = {};
  final Map<String, double> _customAmounts = {};
  final Map<String, double> _adjustmentAmounts = {};
  final Map<String, double> _calculatedShares = {};

  String? _validationError;

  @override
  void initState() {
    super.initState();
    _amountController.addListener(_recalculatePreviews);
  }

  @override
  void dispose() {
    _amountController.removeListener(_recalculatePreviews);
    _descriptionController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  void _recalculatePreviews() {
    final double totalAmount = double.tryParse(_amountController.text) ?? 0.0;
    final householdProv = context.read<HouseholdProvider>();
    final members = householdProv.currentHousehold?.memberships.map((m) => m.user.id).toList() ?? [];

    if (totalAmount <= 0) {
      setState(() {
        for (var id in members) {
          _calculatedShares[id] = 0.0;
        }
        _validationError = null;
      });
      return;
    }

    final Map<String, double> newCalculated = {};
    String? newError;

    switch (_selectedSplitType) {
      case ExpenseSplitType.equalSplit:
        final included = members.where((id) => _isIncluded[id] ?? true).toList();
        if (included.isEmpty) {
          for (var id in members) {
            newCalculated[id] = 0.0;
          }
        } else {
          final share = totalAmount / included.length;
          for (var id in members) {
            newCalculated[id] = (_isIncluded[id] ?? true) ? share : 0.0;
          }
        }
        break;

      case ExpenseSplitType.shares:
        int totalShares = 0;
        for (var id in members) {
          totalShares += _shareCounts[id] ?? 0;
        }
        if (totalShares == 0) {
          for (var id in members) {
            newCalculated[id] = 0.0;
          }
        } else {
          final pricePerShare = totalAmount / totalShares;
          for (var id in members) {
            newCalculated[id] = pricePerShare * (_shareCounts[id] ?? 0);
          }
        }
        break;

      case ExpenseSplitType.exactAmount:
        double sum = 0;
        for (var id in members) {
          final amt = _customAmounts[id] ?? 0.0;
          newCalculated[id] = amt;
          sum += amt;
        }
        if ((sum - totalAmount).abs() > 0.01) {
          newError = "Total split (€ ${sum.toStringAsFixed(2)}) does not match total amount";
        }
        break;

      case ExpenseSplitType.adjustment:
        double adjustmentSum = 0;
        for (var id in members) {
          adjustmentSum += _adjustmentAmounts[id] ?? 0.0;
        }
        
        final remainder = totalAmount - adjustmentSum;
        final included = members.where((id) => _isIncluded[id] ?? true).toList();
        
        if (included.isEmpty || remainder < 0) {
           for (var id in members) {
             newCalculated[id] = 0.0;
           }
           if (remainder < 0) {
             newError = "Sum of adjustments exceeds total amount";
           }
        } else {
          final baseShare = remainder / included.length;
          for (var id in members) {
            if (_isIncluded[id] ?? true) {
              newCalculated[id] = baseShare + (_adjustmentAmounts[id] ?? 0.0);
              if (newCalculated[id]! < 0) newCalculated[id] = 0.0;
            } else {
              newCalculated[id] = 0.0;
            }
          }
        }
        break;
    }

    final double calculatedSum = newCalculated.values.fold(0.0, (sum, val) => sum + val);
    if (totalAmount > 0 && (calculatedSum - totalAmount).abs() > 0.01) {
      newError ??= "Total split (€ ${calculatedSum.toStringAsFixed(2)}) does not match total amount";
    }

    setState(() {
      _calculatedShares.addAll(newCalculated);
      _validationError = newError;
    });
  }

  @override
  Widget build(BuildContext context) {
    final householdProv = context.watch<HouseholdProvider>();
    final members = householdProv.currentHousehold?.memberships ?? [];

    // Initialize defaults if empty
    if (members.isNotEmpty && _isIncluded.isEmpty) {
      for (var m in members) {
        _isIncluded[m.user.id] = true;
        _shareCounts[m.user.id] = 1;
        _customAmounts[m.user.id] = 0.0;
        _adjustmentAmounts[m.user.id] = 0.0;
        _calculatedShares[m.user.id] = 0.0;
      }
      // Trigger first recalc
      WidgetsBinding.instance.addPostFrameCallback((_) => _recalculatePreviews());
    }

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
                  'Add Expense',
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF1E3A5F)),
                ),
                IconButton(
                  onPressed: () => Navigator.pop(context),
                  icon: const Icon(Icons.close, color: Colors.grey),
                ),
              ],
            ),
            const SizedBox(height: 24),

            const Text('Description:', style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1E3A5F))),
            const SizedBox(height: 8),
            TextField(
              controller: _descriptionController,
              decoration: InputDecoration(
                hintText: 'e.g., Grocery shopping...',
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                contentPadding: const EdgeInsets.symmetric(horizontal: 12),
              ),
            ),
            const SizedBox(height: 16),

            const Text('Amount:', style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1E3A5F))),
            const SizedBox(height: 8),
            TextField(
              controller: _amountController,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              decoration: InputDecoration(
                hintText: '0.00',
                prefixText: '€ ',
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                contentPadding: const EdgeInsets.symmetric(horizontal: 12),
              ),
            ),
            const SizedBox(height: 16),

            const Text('Split method:', style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1E3A5F))),
            const SizedBox(height: 8),
            DropdownButtonFormField<ExpenseSplitType>(
              value: _selectedSplitType,
              decoration: InputDecoration(
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                contentPadding: const EdgeInsets.symmetric(horizontal: 12),
              ),
              items: [
                DropdownMenuItem(value: ExpenseSplitType.equalSplit, child: const Text('Equal Split')),
                DropdownMenuItem(value: ExpenseSplitType.shares, child: const Text('Weighted Shares Split')),
                DropdownMenuItem(value: ExpenseSplitType.exactAmount, child: const Text('Custom Split')),
                DropdownMenuItem(value: ExpenseSplitType.adjustment, child: const Text('Adjustment Split')),
              ],
              onChanged: (val) {
                setState(() => _selectedSplitType = val!);
                _recalculatePreviews();
              },
            ),
            const SizedBox(height: 24),

            // Horizontal Member List
            SizedBox(
              height: _selectedSplitType == ExpenseSplitType.adjustment ? 200 : 180,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: members.length,
                separatorBuilder: (context, index) => const SizedBox(width: 12),
                itemBuilder: (context, index) {
                  final m = members[index];
                  final uid = m.user.id;
                  return MemberSplitBox(
                    member: m.user,
                    splitType: _selectedSplitType,
                    calculatedAmount: _calculatedShares[uid] ?? 0.0,
                    isIncluded: _isIncluded[uid] ?? true,
                    shareCount: _shareCounts[uid] ?? 1,
                    customAmount: _customAmounts[uid] ?? 0.0,
                    adjustmentAmount: _adjustmentAmounts[uid] ?? 0.0,
                    onToggle: (val) {
                      setState(() => _isIncluded[uid] = val);
                      _recalculatePreviews();
                    },
                    onSharesChanged: (val) {
                      setState(() => _shareCounts[uid] = val);
                      _recalculatePreviews();
                    },
                    onAmountChanged: (val) {
                      setState(() => _customAmounts[uid] = val);
                      _recalculatePreviews();
                    },
                    onAdjustmentChanged: (val) {
                      setState(() => _adjustmentAmounts[uid] = val);
                      _recalculatePreviews();
                    },
                  );
                },
              ),
            ),

            if (_validationError != null)
              Padding(
                padding: const EdgeInsets.only(top: 16),
                child: Text(_validationError!, style: const TextStyle(color: Colors.red, fontSize: 13), textAlign: TextAlign.center),
              ),

            const SizedBox(height: 32),

            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: _validationError != null || _descriptionController.text.isEmpty || _amountController.text.isEmpty
                        ? null
                        : _handleCreate,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF3498DB),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                    ),
                    child: const Text('Create', style: TextStyle(fontWeight: FontWeight.bold)),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () => Navigator.pop(context),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.grey.shade200,
                      foregroundColor: Colors.grey.shade600,
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      elevation: 0,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                    ),
                    child: const Text('Cancel'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }

  void _handleCreate() async {
    final expenseProv = context.read<ExpenseProvider>();
    final double totalAmount = double.tryParse(_amountController.text) ?? 0.0;
    
    final List<ExpenseShareRequestDTO> shares = [];
    final members = context.read<HouseholdProvider>().currentHousehold?.memberships ?? [];

    for (var m in members) {
      final uid = m.user.id;
      double? shareValue;

      switch (_selectedSplitType) {
        case ExpenseSplitType.equalSplit:
          if (_isIncluded[uid] ?? true) shareValue = null; // Backend handles equal
          break;
        case ExpenseSplitType.shares:
          shareValue = (_shareCounts[uid] ?? 0).toDouble();
          break;
        case ExpenseSplitType.exactAmount:
          shareValue = _customAmounts[uid] ?? 0.0;
          break;
        case ExpenseSplitType.adjustment:
          if (_isIncluded[uid] ?? true) {
            shareValue = _adjustmentAmounts[uid] ?? 0.0;
          } else {
             continue; // Exclude from adjustment strategy
          }
          break;
      }

      if (_selectedSplitType == ExpenseSplitType.equalSplit) {
        if (_isIncluded[uid] ?? true) {
          shares.add(ExpenseShareRequestDTO(userId: uid, share: null));
        }
      } else {
        shares.add(ExpenseShareRequestDTO(userId: uid, share: shareValue));
      }
    }

    final request = ExpenseCreateRequestDTO(
      description: _descriptionController.text,
      amount: totalAmount,
      splitType: _selectedSplitType,
      shares: shares,
    );

    final success = await expenseProv.createExpense(request);
    if (success && mounted) {
      Navigator.pop(context);
    }
  }
}
