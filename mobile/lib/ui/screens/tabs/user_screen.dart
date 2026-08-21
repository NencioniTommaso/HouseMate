import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/user_provider.dart';
import '../../../state/auth_provider.dart';
import '../../../state/expense_provider.dart';
import '../../../state/chore_provider.dart';
import '../../../state/household_provider.dart';
import '../../../shared/dto/user/request/user_update_request_dto.dart';
import '../../popups/dialog_confirm_action.dart';

class UserScreen extends StatefulWidget {
  const UserScreen({super.key});

  @override
  State<UserScreen> createState() => _UserScreenState();
}

class _UserScreenState extends State<UserScreen> {
  bool _isEditing = false;
  final TextEditingController _firstNameController = TextEditingController();
  final TextEditingController _lastNameController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _ibanController = TextEditingController();
  final TextEditingController _paymentLinkController = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _refreshData();
    });
  }

  @override
  void dispose() {
    _firstNameController.dispose();
    _lastNameController.dispose();
    _emailController.dispose();
    _ibanController.dispose();
    _paymentLinkController.dispose();
    super.dispose();
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
      backgroundColor: const Color(0xFFF4F6F8),
      body: RefreshIndicator(
        onRefresh: _refreshData,
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.fromLTRB(24, 24, 24, 8),
          child: _buildScreenContent(),
        ),
      ),
    );
  }

  Widget _buildScreenContent() {
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
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF2C3E50)),
            ),
            const SizedBox(height: 20),

            // Performance Cards
            if (hasHousehold) ...[
              Row(
                children: [
                  _buildPerformanceCard(
                    "Net cash flow\n(this month):",
                    "€ ${cashFlow.toStringAsFixed(2)}",
                    cashFlow < 0 ? const Color(0xFFE74C3C) : const Color(0xFF4CAF50),
                  ),
                  const SizedBox(width: 8),
                  _buildPerformanceCard(
                    "Pending Tasks:",
                    "$pending",
                    const Color(0xFF4CAF50),
                  ),
                  const SizedBox(width: 8),
                  _buildPerformanceCard(
                    "Expired Tasks:",
                    "$overdue",
                    const Color(0xFFE74C3C),
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
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF2C3E50)),
                      ),
                      const Spacer(),
                      if (!_isEditing)
                        ElevatedButton(
                          onPressed: () {
                            setState(() {
                              _isEditing = true;
                              _firstNameController.text = user?.name ?? "";
                              _lastNameController.text = user?.surname ?? "";
                              _emailController.text = user?.email ?? "";
                              _ibanController.text = user?.iban ?? "";
                              _paymentLinkController.text = user?.paymentLink ?? "";
                            });
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF3498DB),
                            foregroundColor: Colors.white,
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(5)),
                            elevation: 0,
                          ),
                          child: const Text("Edit"),
                        )
                      else ...[
                        ElevatedButton(
                          onPressed: () {
                            setState(() {
                              _isEditing = false;
                            });
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFFECF0F1),
                            foregroundColor: const Color(0xFF7F8C8D),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(5),
                              side: const BorderSide(color: Color(0xFFE0E0E0)),
                            ),
                            elevation: 0,
                          ),
                          child: const Text("Cancel"),
                        ),
                        const SizedBox(width: 8),
                        ElevatedButton(
                          onPressed: userProv.isLoading
                              ? null
                              : () async {
                                  final request = UserUpdateRequestDTO(
                                    name: _firstNameController.text.trim(),
                                    surname: _lastNameController.text.trim(),
                                    email: _emailController.text.trim(),
                                    iban: _ibanController.text.trim(),
                                    paymentLink: _paymentLinkController.text.trim(),
                                  );
                                  final success = await userProv.updateUserProfile(request);
                                  if (success) {
                                    setState(() {
                                      _isEditing = false;
                                    });
                                  }
                                },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF4CAF50),
                            foregroundColor: Colors.white,
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(5)),
                            elevation: 0,
                          ),
                          child: userProv.isLoading
                              ? const SizedBox(
                                  height: 20, width: 20,
                                  child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                                )
                              : const Text("Save"),
                        ),
                      ],
                    ],
                  ),
                  const Divider(),
                  const SizedBox(height: 10),
                  _buildInfoField("First Name", user?.name ?? "---", _firstNameController),
                  _buildInfoField("Last Name", user?.surname ?? "---", _lastNameController),
                  _buildInfoField("Email", user?.email ?? "---", _emailController),
                  _buildInfoField("IBAN", user?.iban ?? "Not set", _ibanController),
                  _buildInfoField("Payment Link", user?.paymentLink ?? "Not set", _paymentLinkController),
                  const SizedBox(height: 30),
                  const Divider(),
                  const SizedBox(height: 15),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      if (hasHousehold) ...[
                        ElevatedButton(
                          onPressed: () {
                            showConfirmActionDialog(
                              context: context,
                              title: 'Leave Household',
                              message: 'Are you sure you want to leave your current household? You will lose access to all shared data.',
                              onConfirm: () async {
                                final success = await householdProv.leaveHousehold();
                                if (success && context.mounted) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    const SnackBar(content: Text("Successfully left the household")),
                                  );
                                }
                              },
                            );
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFFE74C3C),
                            foregroundColor: Colors.white,
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(5)),
                            elevation: 0,
                          ),
                          child: const Text("Leave Household", style: TextStyle(fontWeight: FontWeight.bold)),
                        ),
                        const SizedBox(width: 20),
                      ],
                      ElevatedButton(
                        onPressed: () {
                          showConfirmActionDialog(
                            context: context,
                            title: 'Logout',
                            message: 'Are you sure you want to log out?',
                            onConfirm: () {
                              // 1. Wipe all data from memory
                              context.read<HouseholdProvider>().clear();
                              context.read<ExpenseProvider>().clear();
                              context.read<ChoreProvider>().clear();
                              context.read<UserProvider>().clear();

                              // 2. Clear token and update auth state
                              context.read<AuthProvider>().logout();
                            },
                          );
                        },
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFFE74C3C),
                          foregroundColor: Colors.white,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(5)),
                          elevation: 0,
                        ),
                        child: const Text("Logout", style: TextStyle(fontWeight: FontWeight.bold)),
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
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 12, color: Color(0xFF7F8C8D), fontWeight: FontWeight.bold),
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

  Widget _buildInfoField(String label, String value, TextEditingController controller) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF2C3E50), fontSize: 13)),
          const SizedBox(height: 4),
          if (!_isEditing)
            Text(value, style: const TextStyle(fontSize: 16, color: Color(0xFF2C3E50)))
          else
            TextField(
              controller: controller,
              decoration: InputDecoration(
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(5),
                  borderSide: const BorderSide(color: Color(0xFFE0E0E0)),
                ),
                filled: true,
                fillColor: Colors.white,
                contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                isDense: true,
              ),
            ),
        ],
      ),
    );
  }
}
