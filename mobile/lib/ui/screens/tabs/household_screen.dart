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
          backgroundColor: Colors.grey.shade100,
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
          SizedBox(height: MediaQuery.of(context).size.height * 0.2),
          Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.house_outlined, size: 80, color: Colors.grey),
                const SizedBox(height: 16),
                const Text(
                  "You don't belong to a household yet.",
                  style: TextStyle(fontSize: 18, color: Colors.grey),
                ),
                const SizedBox(height: 32),
                ElevatedButton(
                  onPressed: () => showCreateHouseholdSheet(context),
                  child: const Text('Create a Household'),
                ),
                const SizedBox(height: 16),
                OutlinedButton(
                  onPressed: () => showJoinHouseholdSheet(context),
                  child: const Text('Join an Existing Household'),
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
      padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 32.0),
      physics: const AlwaysScrollableScrollPhysics(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // 1. The Headers
          const Text(
            'Your Household',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Color(0xFF2C3E50),
            ),
          ),
          const SizedBox(height: 16),
          Text(
            provider.currentHousehold?.name ?? 'Unknown',
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 16, 
              fontWeight: FontWeight.bold,
              color: Color(0xFF2C3E50),
            ),
          ),
          const SizedBox(height: 32),

          // 2. The Four Action Buttons
          _buildDesktopStyleButton('Manage Members', () {
            showManageMembersSheet(context);
          }),
          const SizedBox(height: 16),

          _buildDesktopStyleButton('Chores List', () {
            showChoresListSheet(context);
          }),
          const SizedBox(height: 16),

          _buildDesktopStyleButton('Shopping Lists', () {
            showShoppingListsSheet(context);
          }),
          const SizedBox(height: 16),

          _buildDesktopStyleButton('Invite Member', () {
            showInviteMemberDialog(context);
          }),
        ],
      ),
    );
  }

  // Helper method to perfectly match your desktop button styling
  Widget _buildDesktopStyleButton(String text, VoidCallback onPressed) {
    return ElevatedButton(
      style: ElevatedButton.styleFrom(
        backgroundColor: Colors.white,
        foregroundColor: const Color(0xFF2C3E50), // Main Dark Blue text
        padding: const EdgeInsets.symmetric(vertical: 24),
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(10),
          side: const BorderSide(color: Color(0xFFE0E0E0)),
        ),
      ),
      onPressed: onPressed,
      child: Text(
        text,
        style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
      ),
    );
  }
}
