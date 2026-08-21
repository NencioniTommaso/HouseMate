import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/expense_provider.dart';
import '../../../state/auth_provider.dart';
import '../../popups/expenses/sheet_create_expense.dart';
import '../../popups/expenses/sheet_your_debts.dart';
import '../../popups/expenses/sheet_you_are_owed.dart';
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
          backgroundColor: Colors.grey.shade100,
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
      padding: const EdgeInsets.all(16.0),
      children: [
        // Header
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              "Expenses",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF1E3A5F)),
            ),
            ElevatedButton.icon(
              icon: const Icon(Icons.add, size: 18),
              label: const Text("New"),
              onPressed: () => showCreateExpenseSheet(context),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF3498DB),
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(5)),
                elevation: 0,
              ),
            ),
          ],
        ),
        Text(
          "This Month: € ${provider.overview?.totalAmount.toStringAsFixed(2) ?? "0.00"} (${provider.overview?.expenseCount ?? 0} expenses)",
          style: const TextStyle(color: Colors.grey, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 15),

        // Summary Cards
        Row(
          children: [
            _buildSummaryCard(
              "You Owe",
              "€ ${youOwe.toStringAsFixed(2)}",
              Colors.red,
              onTap: () => showYourDebtsSheet(context),
            ),
            const SizedBox(width: 10),
            _buildSummaryCard(
              "You Are Owed",
              "€ ${youAreOwed.toStringAsFixed(2)}",
              Colors.green,
              onTap: () => showYouAreOwedSheet(context),
            ),
          ],
        ),
        const SizedBox(height: 25),

        // Filter Controls
        _buildFiltersSection(),
        const SizedBox(height: 25),

        // Search Results Header
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              "Search Results",
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF1E3A5F)),
            ),
            TextButton(
              onPressed: () => setState(() => _filtersVisible = !_filtersVisible),
              style: TextButton.styleFrom(
                foregroundColor: const Color(0xFF7F8C8D),
                backgroundColor: const Color(0xFFECF0F1),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(5)),
                padding: const EdgeInsets.symmetric(horizontal: 12),
              ),
              child: Text(_filtersVisible ? "Hide Filters" : "Show Filters", style: const TextStyle(fontWeight: FontWeight.bold)),
            ),
          ],
        ),
        const SizedBox(height: 10),

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
          const Center(
            child: Padding(
              padding: EdgeInsets.all(40.0),
              child: Text("No results found matching your filters."),
            ),
          ),
      ],
    );
  }

  Widget _buildFiltersSection() {
    if (!_filtersVisible) return const SizedBox.shrink();

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
          const SizedBox(height: 12),
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
              const SizedBox(width: 8),
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
          const SizedBox(height: 12),
          // Description Search
          TextField(
            controller: _descriptionController,
            onChanged: (_) => _onFilterChanged(),
            decoration: InputDecoration(
              hintText: "Description...",
              prefixIcon: const Icon(Icons.search, size: 20),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
              contentPadding: const EdgeInsets.symmetric(horizontal: 12),
            ),
          ),
          const SizedBox(height: 12),
          // Mode Toggle (Expenses vs Settlements)
          Row(
            children: [
              _buildModeRadioButton("Expenses", true, _isExpensesMode, (val) {
                setState(() => _isExpensesMode = val!);
                _applyFilters();
              }),
              const SizedBox(width: 16),
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
          activeColor: const Color(0xFF3498DB),
        ),
        Text(label),
        const SizedBox(width: 8),
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
          activeColor: const Color(0xFF3498DB),
        ),
        Text(label),
      ],
    );
  }

  Widget _buildDatePicker(String hint, DateTime? selectedDate, {DateTime? minDate, DateTime? maxDate, required ValueChanged<DateTime?> onDatePicked}) {
    return InkWell(
      onTap: () async {
        final initialDate = selectedDate ?? DateTime.now();
        
        // Ensure initialDate stays within the constrained range
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
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
        decoration: BoxDecoration(
          border: Border.all(color: Colors.grey.shade400),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              selectedDate == null ? hint : "${selectedDate.day}/${selectedDate.month}/${selectedDate.year}",
              style: TextStyle(color: selectedDate == null ? const Color(0xFF95A5A6) : const Color(0xFF2C3E50), fontSize: 13),
            ),
            const Icon(Icons.calendar_today, size: 16, color: Color(0xFF7F8C8D)),
          ],
        ),
      ),
    );
  }

  Widget _buildSummaryCard(String title, String amount, Color amountColor, {required VoidCallback onTap}) {
    return Expanded(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Container(
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
          child: Column(
            children: [
              Text(title, style: const TextStyle(color: Color(0xFF7F8C8D), fontWeight: FontWeight.bold, fontSize: 14)),
              const SizedBox(height: 8),
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
      ),
    );
  }
}
