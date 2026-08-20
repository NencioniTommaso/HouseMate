import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../../../state/household_provider.dart';
import '../../../../state/auth_provider.dart';
import '../dialog_confirm_action.dart';

void showManageMembersSheet(BuildContext context) {
  // Refresh member data
  context.read<HouseholdProvider>().loadHouseholdData();

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return const _ManageMembersSheetContent();
    },
  );
}

class _ManageMembersSheetContent extends StatelessWidget {
  const _ManageMembersSheetContent();

  @override
  Widget build(BuildContext context) {
    final householdProv = context.watch<HouseholdProvider>();
    final authProv = context.watch<AuthProvider>();
    
    final memberships = householdProv.currentHousehold?.memberships ?? [];
    final currentUser = authProv.currentUser;
    final isUserAdmin = householdProv.isAdmin(currentUser?.id ?? "");

    return Container(
      padding: const EdgeInsets.all(24.0),
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.8,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Manage Members',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF1E3A5F),
                ),
              ),
              IconButton(
                onPressed: () => Navigator.pop(context),
                icon: Container(
                  decoration: BoxDecoration(
                    color: Colors.grey.shade200,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: const Icon(Icons.close, size: 20, color: Colors.grey),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          if (householdProv.isLoading && memberships.isEmpty)
            const Expanded(
              child: Center(child: CircularProgressIndicator()),
            )
          else
            Expanded(
              child: ListView.separated(
              itemCount: memberships.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                final membership = memberships[index];
                final user = membership.user;
                final isMe = user.id == currentUser?.id;

                return Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: Colors.grey.shade200),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.05),
                        blurRadius: 4,
                        offset: const Offset(0, 2),
                      ),
                    ],
                  ),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '${user.name} ${user.surname}',
                              style: const TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                                color: Color(0xFF1E3A5F),
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              user.email,
                              style: TextStyle(color: Colors.grey.shade600, fontSize: 13),
                            ),
                            if (user.iban != null)
                              Text(
                                user.iban!,
                                style: TextStyle(color: Colors.grey.shade600, fontSize: 13),
                              ),
                            if (user.paymentLink != null)
                              GestureDetector(
                                onTap: () async {
                                  final link = user.paymentLink!.trim();
                                  if (link.isEmpty) return;

                                  final url = Uri.parse(link);
                                  try {
                                    final launched = await launchUrl(
                                      url,
                                      mode: LaunchMode.externalApplication,
                                    );
                                    if (!launched && context.mounted) {
                                      ScaffoldMessenger.of(context).showSnackBar(
                                        const SnackBar(content: Text('Could not open payment link')),
                                      );
                                    }
                                  } catch (e) {
                                    if (context.mounted) {
                                      ScaffoldMessenger.of(context).showSnackBar(
                                        SnackBar(content: Text('Error: $e')),
                                      );
                                    }
                                  }
                                },
                                child: Text(
                                  user.paymentLink!,
                                  style: const TextStyle(
                                    color: Colors.blue,
                                    fontSize: 13,
                                    decoration: TextDecoration.underline,
                                  ),
                                ),
                              ),
                          ],
                        ),
                      ),
                      if (isMe)
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                          decoration: BoxDecoration(
                            color: Colors.green,
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: const Text(
                            'You',
                            style: TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 12,
                            ),
                          ),
                        )
                      else if (isUserAdmin)
                        ElevatedButton(
                          onPressed: () {
                            showConfirmActionDialog(
                              context: context,
                              title: 'Remove Member',
                              message: 'Are you sure you want to remove ${user.name} from the household?',
                              onConfirm: () => householdProv.removeMember(user.id),
                            );
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.red,
                            foregroundColor: Colors.white,
                            minimumSize: const Size(0, 32),
                            padding: const EdgeInsets.symmetric(horizontal: 12),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(6),
                            ),
                          ),
                          child: const Text('Remove', style: TextStyle(fontSize: 12)),
                        ),
                    ],
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
