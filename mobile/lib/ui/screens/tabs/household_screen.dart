import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/household_provider.dart';
import '../../../state/auth_provider.dart';
import '../../widgets/member_item_element.dart';
import '../../widgets/popups/sheet_create_household.dart';
import '../../widgets/popups/sheet_join_household.dart';
import '../../widgets/popups/dialog_invite_member.dart';
import '../../widgets/popups/sheet_create_list.dart';

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
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<HouseholdProvider>(
      builder: (context, provider, child) {
        return Scaffold(
          backgroundColor: Colors.grey.shade100,
          body: RefreshIndicator(
            onRefresh: () => provider.loadHouseholdData(),
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
      return Center(
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

    final household = provider.currentHousehold;
    final authProvider = context.read<AuthProvider>();
    final currentUserId = authProvider.currentUser?.id ?? "";

    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.all(16.0),
      children: [
        const SizedBox(height: 20),
        const Center(
          child: Text(
            "Your Household",
            style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
          ),
        ),
        const SizedBox(height: 10),
        Center(
          child: Text(
            household?.name ?? "No Household",
            style: const TextStyle(fontSize: 18, color: Colors.blue, fontWeight: FontWeight.w500),
          ),
        ),
        const SizedBox(height: 30),

        // Quick Actions
        Row(
          children: [
            _buildActionCard(
              context,
              "Shopping",
              Icons.shopping_cart,
              Colors.orange,
              onTap: () => showCreateShoppingListSheet(context),
            ),
            const SizedBox(width: 10),
            _buildActionCard(
              context,
              "Invite",
              Icons.person_add,
              Colors.green,
              onTap: () => showInviteMemberDialog(context),
            ),
          ],
        ),
        const SizedBox(height: 30),

        if (household != null) ...[
          const Text(
            "Members",
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          ...household.memberships.map((member) => Padding(
                padding: const EdgeInsets.only(bottom: 8.0),
                child: MemberItemElement(
                  member: member.user,
                  currentUserId: currentUserId,
                  isAdminMode: false, // Default to false for now
                  onRemove: () {},
                ),
              )),
        ],
      ],
    );
  }

  Widget _buildActionCard(BuildContext context, String text, IconData icon,
      Color color, {required VoidCallback onTap}) {
    return Expanded(
      child: InkWell(
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 20),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(8),
            border: Border.all(color: Colors.grey.shade300),
          ),
          child: Column(
            children: [
              Icon(icon, size: 32, color: color),
              const SizedBox(height: 8),
              Text(text, style: const TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
        ),
      ),
    );
  }
}
