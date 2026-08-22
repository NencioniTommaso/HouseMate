import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/user_provider.dart';
import '../../../state/auth_provider.dart';
import '../../../state/expense_provider.dart';
import '../../../state/chore_provider.dart';
import '../../../state/household_provider.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/constants/app_strings.dart';
import '../../../../shared/utils/format_utils.dart';
import '../../widgets/shared/app_button.dart';
import '../../widgets/shared/app_header.dart';
import '../../widgets/shared/app_card.dart';
import '../../widgets/shared/app_text_field.dart';
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
      body: RefreshIndicator(
        onRefresh: _refreshData,
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(AppSpacing.l),
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
            AppHeader(
              title: AppStrings.yourProfile,
              centered: true,
            ),
            const SizedBox(height: AppSpacing.l),

            // Performance Cards
            if (hasHousehold) ...[
              Row(
                children: [
                  _buildPerformanceCard(
                    "Net cash flow\n(this month):",
                    FormatUtils.formatCurrency(cashFlow),
                    cashFlow < 0 ? AppColors.danger : AppColors.success,
                  ),
                  const SizedBox(width: AppSpacing.s),
                  _buildPerformanceCard(
                    "Pending Tasks:",
                    "$pending",
                    AppColors.success,
                  ),
                  const SizedBox(width: AppSpacing.s),
                  _buildPerformanceCard(
                    "Expired Tasks:",
                    "$overdue",
                    AppColors.danger,
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.xl),
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
                        AppStrings.personalInfo,
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: AppColors.textPrimary),
                      ),
                      const Spacer(),
                      if (!_isEditing)
                        AppButton(
                          label: AppStrings.edit,
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
                        )
                      else ...[
                        AppButton(
                          label: AppStrings.cancel,
                          variant: AppButtonVariant.secondary,
                          onPressed: () => setState(() => _isEditing = false),
                        ),
                        const SizedBox(width: AppSpacing.s),
                        AppButton(
                          label: AppStrings.save,
                          variant: AppButtonVariant.primary,
                          isLoading: userProv.isLoading,
                          onPressed: () async {
                            final request = UserUpdateRequestDTO(
                              name: _firstNameController.text.trim(),
                              surname: _lastNameController.text.trim(),
                              email: _emailController.text.trim(),
                              iban: _ibanController.text.trim(),
                              paymentLink: _paymentLinkController.text.trim(),
                            );
                            final success = await userProv.updateUserProfile(request);
                            if (success) {
                              setState(() => _isEditing = false);
                            }
                          },
                        ),
                      ],
                    ],
                  ),
                  const Divider(),
                  const SizedBox(height: AppSpacing.s),
                  _buildInfoField("First Name", user?.name ?? "---", _firstNameController),
                  _buildInfoField("Last Name", user?.surname ?? "---", _lastNameController),
                  _buildInfoField("Email", user?.email ?? "---", _emailController),
                  _buildInfoField("IBAN", user?.iban ?? "Not set", _ibanController),
                  _buildInfoField("Payment Link", user?.paymentLink ?? "Not set", _paymentLinkController),
                  const SizedBox(height: AppSpacing.xl),
                  const Divider(),
                  const SizedBox(height: AppSpacing.m),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      if (hasHousehold) ...[
                        AppButton(
                          label: AppStrings.leaveHousehold,
                          variant: AppButtonVariant.destructive,
                          onPressed: () {
                            showConfirmActionDialog(
                              context: context,
                              title: AppStrings.leaveHousehold,
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
                        ),
                        const SizedBox(width: AppSpacing.m),
                      ],
                      AppButton(
                        label: AppStrings.logout,
                        variant: AppButtonVariant.destructive,
                        onPressed: () {
                          showConfirmActionDialog(
                            context: context,
                            title: AppStrings.logout,
                            message: 'Are you sure you want to log out?',
                            onConfirm: () {
                              context.read<HouseholdProvider>().clear();
                              context.read<ExpenseProvider>().clear();
                              context.read<ChoreProvider>().clear();
                              context.read<UserProvider>().clear();
                              context.read<AuthProvider>().logout();
                            },
                          );
                        },
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
      child: AppCard(
        padding: const EdgeInsets.all(AppSpacing.s),
        child: SizedBox(
          height: 80, // Fixed height for consistency across the Row
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                title,
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 11, color: AppColors.textSecondary, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: AppSpacing.xs),
              Text(
                value,
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                  color: valueColor,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInfoField(String label, String value, TextEditingController controller) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.s),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (!_isEditing) ...[
            Text(label, style: const TextStyle(fontWeight: FontWeight.bold, color: AppColors.textPrimary, fontSize: 13)),
            const SizedBox(height: AppSpacing.xs),
            Text(value, style: const TextStyle(fontSize: 16, color: AppColors.textPrimary)),
          ] else
            AppTextField(
              label: label,
              controller: controller,
            ),
        ],
      ),
    );
  }
}
