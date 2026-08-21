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
            backgroundColor: Colors.white,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            title: const Text('Invite a Member', style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF2C3E50))),
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
                        style: TextStyle(color: Color(0xFF2C3E50)),
                      ),
                      const SizedBox(height: 24),
                      Container(
                        padding: const EdgeInsets.symmetric(
                            vertical: 12, horizontal: 24),
                        decoration: BoxDecoration(
                          color: const Color(0xFFECF0F1),
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: const Color(0xFFE0E0E0)),
                        ),
                        child: SelectableText(
                          provider.invitationCode?.invitationCode ?? '------',
                          style: const TextStyle(
                            fontSize: 22,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 2.0,
                            color: Color(0xFF2C3E50),
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
                              style: TextButton.styleFrom(foregroundColor: const Color(0xFF3498DB)),
                              icon: const Icon(Icons.copy, size: 18),
                              label: const Text('Copy', style: TextStyle(fontWeight: FontWeight.bold)),
                            ),
                          if (isUserAdmin)
                            TextButton.icon(
                              onPressed: provider.isLoading 
                                ? null 
                                : () => provider.refreshInvitationCode(),
                              style: TextButton.styleFrom(foregroundColor: const Color(0xFF3498DB)),
                              icon: provider.isLoading 
                                ? const SizedBox(width: 14, height: 14, child: CircularProgressIndicator(strokeWidth: 2))
                                : const Icon(Icons.refresh, size: 18),
                              label: const Text('Refresh', style: TextStyle(fontWeight: FontWeight.bold)),
                            ),
                        ],
                      ),
                    ],
                  ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                style: TextButton.styleFrom(foregroundColor: const Color(0xFF7F8C8D)),
                child: const Text('Close', style: TextStyle(fontWeight: FontWeight.bold)),
              ),
            ],
          );
        },
      );
    },
  );
}
