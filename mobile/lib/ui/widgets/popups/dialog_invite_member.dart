import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../../../state/household_provider.dart';

void showInviteMemberDialog(BuildContext context) {
  // Trigger loading the code
  context.read<HouseholdProvider>().getInviteCode();

  showDialog(
    context: context,
    builder: (BuildContext context) {
      return Consumer<HouseholdProvider>(
        builder: (context, provider, child) {
          return AlertDialog(
            title: const Text('Invite a Member'),
            content: provider.isLoading
                ? const SizedBox(
                    height: 100,
                    child: Center(child: CircularProgressIndicator()),
                  )
                : Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Text(
                        'Share this 6-digit code with your housemate. It will grant them access to this household.',
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
                          icon: const Icon(Icons.copy),
                          label: const Text('Copy to Clipboard'),
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
