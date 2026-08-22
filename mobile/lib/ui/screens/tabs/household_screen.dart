import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/household_provider.dart';
import '../../../state/chore_provider.dart';
import '../../popups/household/dialog_invite_member.dart';
import '../../popups/household/sheet_chores_list.dart';
import '../../popups/household/sheet_join_household.dart';
import '../../popups/household/sheet_manage_members.dart';
import '../../popups/household/sheet_shopping_lists.dart';
import '../../popups/no-household/sheet_create_household.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/constants/app_strings.dart';
import '../../widgets/shared/app_button.dart';
import '../../widgets/shared/app_header.dart';

class HouseholdScreen extends StatefulWidget {
  const HouseholdScreen({super.key});

  @override
  State<HouseholdScreen> createState() => _HouseholdScreenState();
}

class _HouseholdScreenState extends State<HouseholdScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<HouseholdProvider>().loadHouseholdData();
      context.read<ChoreProvider>().loadHouseholdChores();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<HouseholdProvider>(
      builder: (context, provider, child) {
        return Scaffold(
          body: RefreshIndicator(
            onRefresh: () => provider.refreshAll(),
            child: _buildScreenContent(provider),
          ),
        );
      },
    );
  }

  Widget _buildScreenContent(HouseholdProvider provider) {
    if (provider.isLoading && provider.currentHousehold == null) {
      return const Center(child: CircularProgressIndicator());
    }

    if (!provider.hasHousehold) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          SizedBox(height: MediaQuery.of(context).size.height * 0.15),
          Padding(
            padding: const EdgeInsets.all(AppSpacing.xl),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.house_outlined, size: 80, color: AppColors.textDisabled),
                const SizedBox(height: AppSpacing.m),
                const Text(
                  "You don't belong to a household yet.",
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 18, color: AppColors.textSecondary),
                ),
                const SizedBox(height: AppSpacing.xl),
                AppButton(
                  label: 'Create a Household',
                  fullWidth: true,
                  onPressed: () => showCreateHouseholdSheet(context),
                ),
                const SizedBox(height: AppSpacing.m),
                AppButton(
                  label: 'Join an Existing Household',
                  variant: AppButtonVariant.secondary,
                  fullWidth: true,
                  onPressed: () => showJoinHouseholdSheet(context),
                ),
              ],
            ),
          ),
        ],
      );
    }

    if (provider.errorMessage != null && provider.currentHousehold == null) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          const SizedBox(height: 200),
          Center(child: Text(provider.errorMessage!)),
        ],
      );
    }

    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppSpacing.l),
      physics: const AlwaysScrollableScrollPhysics(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // 1. The Headers
          AppHeader(
            title: AppStrings.yourHousehold,
            subtitle: provider.currentHousehold?.name ?? 'Unknown',
            centered: true,
          ),
          const SizedBox(height: AppSpacing.xl),

          // 2. The Four Action Buttons
          _buildDesktopStyleButton(AppStrings.manageMembers, () {
            showManageMembersSheet(context);
          }),
          const SizedBox(height: AppSpacing.m),

          _buildDesktopStyleButton(AppStrings.choresList, () {
            showChoresListSheet(context);
          }),
          const SizedBox(height: AppSpacing.m),

          _buildDesktopStyleButton(AppStrings.shoppingLists, () {
            showShoppingListsSheet(context);
          }),
          const SizedBox(height: AppSpacing.m),

          _buildDesktopStyleButton(AppStrings.inviteMember, () {
            showInviteMemberDialog(context);
          }),
        ],
      ),
    );
  }

  // Helper method updated to use AppButton with custom styling for this screen's prominent actions
  Widget _buildDesktopStyleButton(String text, VoidCallback onPressed) {
    return AppButton(
      label: text,
      variant: AppButtonVariant.secondary,
      fullWidth: true,
      onPressed: onPressed,
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.l),
      fontSize: 20,
    );
  }
}
