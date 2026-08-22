import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/expense_provider.dart';
import '../../../state/auth_provider.dart';
import '../../popups/expenses/sheet_create_expense.dart';
import '../../popups/expenses/sheet_your_debts.dart';
import '../../popups/expenses/sheet_you_are_owed.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/constants/app_strings.dart';
import '../../../../shared/utils/format_utils.dart';
import '../../widgets/shared/app_button.dart';
import '../../widgets/shared/app_header.dart';
import '../../widgets/shared/app_empty_state.dart';
import '../../widgets/shared/app_card.dart';
import '../../widgets/shared/app_text_field.dart';
import '../../widgets/expense_item_element.dart';
import '../../widgets/settlement_item_element.dart';
import '../../../shared/enums/user_transaction_role.dart';
import '../../../shared/dto/expense/request/transaction_filter_request_dto.dart';
import '../../../shared/utils/types/date_range.dart';

class ExpensesScreen extends StatefulWidget {
  const ExpensesScreen({super.key});

  @override
  State<ExpensesScreen> createState() => _ExpensesScreenState();
}

class _ExpensesScreenState extends State<ExpensesScreen> {
  // --- Filtering State ---
  UserTransactionRole _roleFilter = UserTransactionRole.all;
  DateTime? _fromDate;
  DateTime? _toDate;
  final TextEditingController _descriptionController = TextEditingController();
  bool _isExpensesMode = true;
  bool _filtersVisible = true;
  Timer? _debounceTimer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ExpenseProvider>().loadExpenseDashboard();
      _applyFilters();
    });
  }

  @override
  void dispose() {
    _descriptionController.dispose();
    _debounceTimer?.cancel();
    super.dispose();
  }

  void _onFilterChanged() {
    if (_debounceTimer?.isActive ?? false) _debounceTimer!.cancel();
    _debounceTimer = Timer(const Duration(milliseconds: 500), () {
      _applyFilters();
    });
  }

  void _applyFilters() {
    final range = (_fromDate != null || _toDate != null)
        ? DateRange(startDate: _fromDate, endDate: _toDate)
        : null;

    final filter = TransactionFilterRequestDTO(
      userTransactionRole: _roleFilter,
      dateRange: range,
      description: _descriptionController.text.trim().isEmpty ? null : _descriptionController.text.trim(),
    );

    if (_isExpensesMode) {
      context.read<ExpenseProvider>().loadFilteredExpenses(filter);
    } else {
      context.read<ExpenseProvider>().loadFilteredSettlements(filter);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<ExpenseProvider>(
      builder: (context, provider, child) {
        return Scaffold(
          body: RefreshIndicator(
            onRefresh: () async {
              await provider.loadExpenseDashboard();
              _applyFilters();
            },
            child: _buildScreenContent(provider),
          ),
        );
      },
    );
  }

  Widget _buildScreenContent(ExpenseProvider provider) {
    // We only show full-screen loader on initial load if no data exists
    if (provider.isLoading && provider.recentExpenses.isEmpty && provider.debts.isEmpty && provider.recentSettlements.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    final authProvider = context.read<AuthProvider>();
    final currentUserId = authProvider.currentUser?.id ?? "";

    // Calculate "You Owe" and "You Are Owed" from debts
    double youOwe = 0;
    double youAreOwed = 0;
    for (var debt in provider.debts) {
      if (debt.userTransactionRole == UserTransactionRole.debtor) {
        youOwe += debt.amount;
      } else if (debt.userTransactionRole == UserTransactionRole.creditor) {
        youAreOwed += debt.amount;
      }
    }

    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.all(AppSpacing.l),
      children: [
        // Header
        AppHeader(
          title: AppStrings.expensesTitle,
          subtitle: "This Month: ${FormatUtils.formatCurrency(provider.overview?.totalAmount ?? 0)} (${provider.overview?.expenseCount ?? 0} expenses)",
          action: AppButton(
            label: AppStrings.newLabel,
            icon: Icons.add,
            onPressed: () => showCreateExpenseSheet(context),
          ),
        ),
        const SizedBox(height: AppSpacing.l),

        // Summary Cards
        Row(
          children: [
            _buildSummaryCard(
              AppStrings.youOwe,
              FormatUtils.formatCurrency(youOwe),
              AppColors.danger,
              onTap: () => showYourDebtsSheet(context),
            ),
            const SizedBox(width: AppSpacing.s),
            _buildSummaryCard(
              AppStrings.youAreOwed,
              FormatUtils.formatCurrency(youAreOwed),
              AppColors.success,
              onTap: () => showYouAreOwedSheet(context),
            ),
          ],
        ),

        if (_filtersVisible) const SizedBox(height: AppSpacing.l),

        _buildFiltersSection(),
        const SizedBox(height: AppSpacing.l),

        // Search Results Header
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              AppStrings.searchResults,
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: AppColors.textPrimary),
            ),
            AppButton(
              label: _filtersVisible ? AppStrings.hide : AppStrings.show,
              variant: AppButtonVariant.secondary,
              onPressed: () => setState(() => _filtersVisible = !_filtersVisible),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.m),

        if (provider.isLoading)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 20),
            child: Center(child: LinearProgressIndicator()),
          ),

        if (_isExpensesMode)
          ...provider.recentExpenses.map((expense) => Padding(
                padding: const EdgeInsets.only(bottom: 12.0),
                child: ExpenseItemElement(
                  expense: expense,
                  currentUserId: currentUserId,
                ),
              ))
        else
          ...provider.recentSettlements.map((settlement) => Padding(
                padding: const EdgeInsets.only(bottom: 12.0),
                child: SettlementItemElement(
                  settlement: settlement,
                ),
              )),

        if (!provider.isLoading && (_isExpensesMode ? provider.recentExpenses.isEmpty : provider.recentSettlements.isEmpty))
          const AppEmptyState(
            message: "No results found matching your filters.",
            icon: Icons.search_off_outlined,
          ),
      ],
    );
  }

  Widget _buildFiltersSection() {
    if (!_filtersVisible) return const SizedBox.shrink();

    return AppCard(
      padding: const EdgeInsets.all(AppSpacing.m),
      backgroundColor: Colors.white,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Role Radio Buttons
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                _buildRadioButton("Debtor", UserTransactionRole.debtor, _roleFilter, (val) {
                  setState(() => _roleFilter = val!);
                  _applyFilters();
                }),
                _buildRadioButton("Creditor", UserTransactionRole.creditor, _roleFilter, (val) {
                  setState(() => _roleFilter = val!);
                  _applyFilters();
                }),
                _buildRadioButton("All", UserTransactionRole.all, _roleFilter, (val) {
                  setState(() => _roleFilter = val!);
                  _applyFilters();
                }),
              ],
            ),
          ),
          const SizedBox(height: AppSpacing.s),
          // Date Pickers Row
          Row(
            children: [
              Expanded(
                child: _buildDatePicker(
                  "From...",
                  _fromDate,
                  maxDate: _toDate,
                  onDatePicked: (date) {
                    setState(() => _fromDate = date);
                    _applyFilters();
                  },
                ),
              ),
              const SizedBox(width: AppSpacing.s),
              Expanded(
                child: _buildDatePicker(
                  "To...",
                  _toDate,
                  minDate: _fromDate,
                  onDatePicked: (date) {
                    setState(() => _toDate = date);
                    _applyFilters();
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.s),
          // Description Search
          AppTextField(
            controller: _descriptionController,
            onChanged: (_) => _onFilterChanged(),
            hintText: "Description...",
            prefixIcon: Icons.search,
          ),
          const SizedBox(height: AppSpacing.s),
          // Mode Toggle (Expenses vs Settlements)
          Row(
            children: [
              _buildModeRadioButton("Expenses", true, _isExpensesMode, (val) {
                setState(() => _isExpensesMode = val!);
                _applyFilters();
              }),
              const SizedBox(width: AppSpacing.m),
              _buildModeRadioButton("Settlements", false, _isExpensesMode, (val) {
                setState(() => _isExpensesMode = val!);
                _applyFilters();
              }),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildRadioButton(String label, UserTransactionRole value, UserTransactionRole groupValue, ValueChanged<UserTransactionRole?> onChanged) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Radio<UserTransactionRole>(
          value: value,
          groupValue: groupValue,
          onChanged: onChanged,
          activeColor: AppColors.secondary,
        ),
        Text(label),
        const SizedBox(width: AppSpacing.s),
      ],
    );
  }

  Widget _buildModeRadioButton(String label, bool value, bool groupValue, ValueChanged<bool?> onChanged) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Radio<bool>(
          value: value,
          groupValue: groupValue,
          onChanged: onChanged,
          activeColor: AppColors.secondary,
        ),
        Text(label),
      ],
    );
  }

  Widget _buildDatePicker(String hint, DateTime? selectedDate, {DateTime? minDate, DateTime? maxDate, required ValueChanged<DateTime?> onDatePicked}) {
    return InkWell(
      onTap: () async {
        final initialDate = selectedDate ?? DateTime.now();
        
        final validFirstDate = minDate ?? DateTime(2020);
        final validLastDate = maxDate ?? DateTime.now().add(const Duration(days: 365));
        
        DateTime validInitialDate = initialDate;
        if (validInitialDate.isBefore(validFirstDate)) {
          validInitialDate = validFirstDate;
        } else if (validInitialDate.isAfter(validLastDate)) {
          validInitialDate = validLastDate;
        }

        final date = await showDatePicker(
          context: context,
          initialDate: validInitialDate,
          firstDate: validFirstDate,
          lastDate: validLastDate,
        );
        onDatePicked(date);
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: AppSpacing.m, vertical: AppSpacing.m),
        decoration: BoxDecoration(
          border: Border.all(color: AppColors.border),
          borderRadius: BorderRadius.circular(AppSpacing.radiusS),
          color: Colors.white,
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              selectedDate == null ? hint : FormatUtils.formatDate(selectedDate),
              style: TextStyle(
                color: selectedDate == null ? AppColors.textHint : AppColors.textPrimary, 
                fontSize: 13
              ),
            ),
            const Icon(Icons.calendar_today, size: 16, color: AppColors.textSecondary),
          ],
        ),
      ),
    );
  }

  Widget _buildSummaryCard(String title, String amount, Color amountColor, {required VoidCallback onTap}) {
    return Expanded(
      child: AppCard(
        onTap: onTap,
        padding: const EdgeInsets.all(AppSpacing.m),
        child: Column(
          children: [
            Text(title, style: const TextStyle(color: AppColors.textSecondary, fontWeight: FontWeight.bold, fontSize: 14)),
            const SizedBox(height: AppSpacing.s),
            Text(
              amount,
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: amountColor,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
