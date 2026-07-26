import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/expense_provider.dart';
import '../../../state/auth_provider.dart';
import '../../widgets/expense_item_element.dart';
import '../../widgets/debt_item_element.dart';
import '../../widgets/popups/sheet_create_expense.dart';
import '../../widgets/popups/sheet_settle_debt.dart';
import '../../../shared/enums/user_transaction_role.dart';

class ExpensesScreen extends StatefulWidget {
  const ExpensesScreen({super.key});

  @override
  State<ExpensesScreen> createState() => _ExpensesScreenState();
}

class _ExpensesScreenState extends State<ExpensesScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ExpenseProvider>().loadExpenseDashboard();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<ExpenseProvider>(
      builder: (context, provider, child) {
        return Scaffold(
          backgroundColor: Colors.grey.shade100,
          body: RefreshIndicator(
            onRefresh: () => provider.loadExpenseDashboard(),
            child: _buildScreenContent(provider),
          ),
        );
      },
    );
  }

  Widget _buildScreenContent(ExpenseProvider provider) {
    if (provider.isLoading && provider.recentExpenses.isEmpty && provider.debts.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.errorMessage != null && provider.recentExpenses.isEmpty && provider.debts.isEmpty) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          const SizedBox(height: 200),
          Center(child: Text(provider.errorMessage!)),
        ],
      );
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
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  "Expenses",
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                ),
                Text(
                  "This Month: ${provider.overview?.expenseCount ?? 0} expenses",
                  style: const TextStyle(color: Colors.grey),
                ),
              ],
            ),
            const Spacer(),
            ElevatedButton(
              onPressed: () => showCreateExpenseSheet(context),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.blue,
                foregroundColor: Colors.white,
              ),
              child: const Text("+ Add Expense"),
            ),
          ],
        ),
        const SizedBox(height: 15),

        // Summary Cards
        Row(
          children: [
            _buildSummaryCard("You Owe", "€ ${youOwe.toStringAsFixed(2)}", Colors.red),
            const SizedBox(width: 10),
            _buildSummaryCard("You Are Owed", "€ ${youAreOwed.toStringAsFixed(2)}", Colors.green),
          ],
        ),
        const SizedBox(height: 25),

        if (provider.debts.isNotEmpty) ...[
          const Text(
            "Your Debts",
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          ...provider.debts.map((debt) => Padding(
                padding: const EdgeInsets.only(bottom: 8.0),
                child: DebtItemElement(
                  debt: debt,
                  onPay: (d) => showSettleDebtSheet(context, d),
                ),
              )),
          const SizedBox(height: 20),
        ],

        const Text(
          "Recent Activity",
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 10),
        if (provider.recentExpenses.isEmpty)
          const Center(child: Padding(
            padding: EdgeInsets.all(20.0),
            child: Text("No recent expenses"),
          ))
        else
          ...provider.recentExpenses.map((expense) => Padding(
                padding: const EdgeInsets.only(bottom: 8.0),
                child: ExpenseItemElement(
                  expense: expense,
                  currentUserId: currentUserId,
                ),
              )),
      ],
    );
  }

  Widget _buildSummaryCard(String title, String amount, Color amountColor) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(15),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.grey.shade300),
        ),
        child: Column(
          children: [
            Text(title, style: const TextStyle(color: Colors.grey)),
            const SizedBox(height: 5),
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
