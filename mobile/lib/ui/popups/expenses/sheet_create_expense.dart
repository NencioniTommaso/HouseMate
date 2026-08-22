import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/expense_provider.dart';
import '../../../../state/household_provider.dart';
import '../../../../shared/dto/expense/request/expense_create_request.dart';
import '../../../../shared/dto/expense/request/expense_share_request_dto.dart';
import '../../../../shared/enums/expense_split_type.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/utils/expense_split_calculator.dart';
import '../../widgets/shared/app_button.dart';
import '../../widgets/shared/app_text_field.dart';
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

    final newCalculated = ExpenseSplitCalculator.calculate(
      totalAmount: totalAmount,
      type: _selectedSplitType,
      memberIds: members,
      isIncluded: _isIncluded,
      shareCounts: _shareCounts,
      customAmounts: _customAmounts,
      adjustmentAmounts: _adjustmentAmounts,
    );

    final newError = ExpenseSplitCalculator.validate(
      totalAmount: totalAmount,
      type: _selectedSplitType,
      memberIds: members,
      calculatedShares: newCalculated,
    );

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
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: AppColors.textPrimary),
                ),
                IconButton(
                  onPressed: () => Navigator.pop(context),
                  icon: const Icon(Icons.close, color: AppColors.textSecondary),
                ),
              ],
            ),
            const SizedBox(height: AppSpacing.l),

            AppTextField(
              label: 'Description:',
              controller: _descriptionController,
              hintText: 'e.g., Grocery shopping...',
            ),
            const SizedBox(height: AppSpacing.m),

            AppTextField(
              label: 'Amount:',
              controller: _amountController,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              prefixText: '€ ',
              hintText: '0.00',
            ),
            const SizedBox(height: AppSpacing.m),

            const Text('Split method:', style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.textPrimary, fontSize: 13)),
            const SizedBox(height: AppSpacing.xs),
            DropdownButtonFormField<ExpenseSplitType>(
              value: _selectedSplitType,
              decoration: InputDecoration(
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppSpacing.radiusS), borderSide: const BorderSide(color: AppColors.border)),
                contentPadding: const EdgeInsets.symmetric(horizontal: AppSpacing.m),
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
            const SizedBox(height: AppSpacing.l),

            // Horizontal Member List
            SizedBox(
              height: _selectedSplitType == ExpenseSplitType.adjustment ? 200 : 180,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: members.length,
                separatorBuilder: (context, index) => const SizedBox(width: AppSpacing.s),
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
                padding: const EdgeInsets.only(top: AppSpacing.m),
                child: Text(_validationError!, style: const TextStyle(color: AppColors.danger, fontSize: 13), textAlign: TextAlign.center),
              ),

            const SizedBox(height: AppSpacing.xl),

            Row(
              children: [
                Expanded(
                  child: AppButton(
                    label: 'Create',
                    onPressed: _validationError != null || _descriptionController.text.isEmpty || _amountController.text.isEmpty
                        ? null
                        : _handleCreate,
                  ),
                ),
                const SizedBox(width: AppSpacing.m),
                Expanded(
                  child: AppButton(
                    label: 'Cancel',
                    variant: AppButtonVariant.secondary,
                    onPressed: () => Navigator.pop(context),
                  ),
                ),
              ],
            ),
            const SizedBox(height: AppSpacing.l),
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
