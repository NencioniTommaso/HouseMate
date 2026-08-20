import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/user_provider.dart';
import '../../../state/auth_provider.dart';
import '../../../state/expense_provider.dart';
import '../../../state/chore_provider.dart';
import '../../../state/household_provider.dart';
import '../../widgets/popups/dialog_confirm_action.dart';

class UserScreen extends StatefulWidget {
  const UserScreen({super.key});

  @override
  State<UserScreen> createState() => _UserScreenState();
}

class _UserScreenState extends State<UserScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _refreshData();
    });
  }

  Future<void> _refreshData() async {
    await Future.wait([
      context.read<UserProvider>().loadUserProfile(),
      context.read<ExpenseProvider>().loadExpenseDashboard(),
      context.read<ChoreProvider>().loadAssignments(),
    ]);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey.shade100,
      body: RefreshIndicator(
        onRefresh: _refreshData,
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(16.0),
          child: _buildContent(),
        ),
      ),
    );
  }

  Widget _buildContent() {
    return Consumer4<UserProvider, ExpenseProvider, ChoreProvider, HouseholdProvider>(
      builder: (context, userProv, expenseProv, choreProv, householdProv, child) {
        final user = userProv.currentUser;
        
        if (userProv.isLoading && user == null) {
          return const Center(child: Padding(
            padding: EdgeInsets.only(top: 100),
            child: CircularProgressIndicator(),
          ));
        }

        final cashFlow = expenseProv.userNetOverview?.actualCashFlowAmount ?? 0.0;
        final pending = choreProv.overview?.pendingAssignments ?? 0;
        final overdue = choreProv.overview?.overdueAssignments ?? 0;
        final hasHousehold = householdProv.hasHousehold;

        return Column(
          children: [
            const SizedBox(height: 20),
            const Text(
              "Your Profile",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 20),

            // Performance Cards
            if (hasHousehold) ...[
              Row(
                children: [
                  _buildPerformanceCard(
                    "Net cash flow\n(this month):",
                    "€ ${cashFlow.toStringAsFixed(2)}",
                    cashFlow < 0 ? Colors.red : Colors.green,
                  ),
                  const SizedBox(width: 8),
                  _buildPerformanceCard(
                    "Pending Tasks:",
                    "$pending",
                    Colors.green,
                  ),
                  const SizedBox(width: 8),
                  _buildPerformanceCard(
                    "Expired Tasks:",
                    "$overdue",
                    Colors.red,
                  ),
                ],
              ),
              const SizedBox(height: 30),
            ],

            // Personal Info Section
            Container(
              constraints: const BoxConstraints(maxWidth: 500),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Text(
                        "Personal Info",
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                      const Spacer(),
                      ElevatedButton(
                        onPressed: () {},
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.blue,
                          foregroundColor: Colors.white,
                        ),
                        child: const Text("Edit"),
                      ),
                    ],
                  ),
                  const Divider(),
                  const SizedBox(height: 10),
                  _buildInfoRow("First Name", user?.name ?? "---"),
                  _buildInfoRow("Last Name", user?.surname ?? "---"),
                  _buildInfoRow("Email", user?.email ?? "---"),
                  _buildInfoRow("IBAN", user?.iban ?? "Not set"),
                  _buildInfoRow("Payment Link", user?.paymentLink ?? "Not set"),
                  const SizedBox(height: 30),
                  const Divider(),
                  const SizedBox(height: 15),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      ElevatedButton(
                        onPressed: () {},
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.red,
                          foregroundColor: Colors.white,
                        ),
                        child: const Text("Leave Household"),
                      ),
                      const SizedBox(width: 20),
                      ElevatedButton(
                        onPressed: () {
                          showConfirmActionDialog(
                            context: context,
                            title: 'Logout',
                            message: 'Are you sure you want to log out?',
                            onConfirm: () {
                              context.read<AuthProvider>().logout();
                            },
                          );
                        },
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.red,
                          foregroundColor: Colors.white,
                        ),
                        child: const Text("Logout"),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildPerformanceCard(String title, String value, Color valueColor) {
    return Expanded(
      child: Container(
        height: 100,
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.grey.shade300),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 12, color: Colors.grey),
            ),
            const SizedBox(height: 5),
            Text(
              value,
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: valueColor,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(height: 4),
          Text(value),
        ],
      ),
    );
  }
}
