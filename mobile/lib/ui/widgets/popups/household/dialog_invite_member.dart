import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../../../../state/household_provider.dart';
import '../../../../state/auth_provider.dart';

void showInviteMemberDialog(BuildContext context) {
  // Trigger loading the code
  context.read<HouseholdProvider>().getInviteCode();

  showDialog(
    context: context,
    builder: (BuildContext context) {
      return Consumer2<HouseholdProvider, AuthProvider>(
        builder: (context, provider, authProv, child) {
          final isUserAdmin = provider.isAdmin(authProv.currentUser?.id ?? "");

          return AlertDialog(
            title: const Text('Invite a Member'),
            content: provider.isLoading && provider.invitationCode == null
                ? const SizedBox(
                    height: 100,
                    child: Center(child: CircularProgressIndicator()),
                  )
                : Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Text(
                        'Share this code with your housemate. It will grant them access to this household.',
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 24),
                      Container(
                        padding: const EdgeInsets.symmetric(
                            vertical: 12, horizontal: 24),
                        decoration: BoxDecoration(
                          color: Colors.grey.shade100,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: Colors.grey.shade300),
                        ),
                        child: SelectableText(
                          provider.invitationCode?.invitationCode ?? '------',
                          style: const TextStyle(
                            fontSize: 22,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 2.0,
                          ),
                          textAlign: TextAlign.center,
                        ),
                      ),
                      const SizedBox(height: 16),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          if (provider.invitationCode != null)
                            TextButton.icon(
                              onPressed: () {
                                Clipboard.setData(ClipboardData(
                                    text: provider.invitationCode!.invitationCode));
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                      content: Text('Code copied to clipboard!')),
                                );
                              },
                              icon: const Icon(Icons.copy, size: 18),
                              label: const Text('Copy'),
                            ),
                          if (isUserAdmin)
                            TextButton.icon(
                              onPressed: provider.isLoading 
                                ? null 
                                : () => provider.refreshInvitationCode(),
                              icon: provider.isLoading 
                                ? const SizedBox(width: 14, height: 14, child: CircularProgressIndicator(strokeWidth: 2))
                                : const Icon(Icons.refresh, size: 18),
                              label: const Text('Refresh'),
                            ),
                        ],
                      ),
                    ],
                  ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Close'),
              ),
            ],
          );
        },
      );
    },
  );
}
