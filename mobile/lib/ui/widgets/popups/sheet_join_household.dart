import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/household_provider.dart';

void showJoinHouseholdSheet(BuildContext context) {
  final TextEditingController codeController = TextEditingController();

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
          left: 24,
          right: 24,
          top: 24,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'Join a Household',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            const Text(
              'Enter the invitation code provided by your housemate.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 24),
            TextField(
              controller: codeController,
              autofocus: true,
              decoration: const InputDecoration(
                labelText: 'Invitation Code',
                hintText: 'Enter the code provided by your housemate',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 24),
            Consumer<HouseholdProvider>(
              builder: (context, provider, child) {
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    if (provider.errorMessage != null)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 16),
                        child: Text(
                          provider.errorMessage!,
                          style: const TextStyle(color: Colors.red),
                          textAlign: TextAlign.center,
                        ),
                      ),
                    ElevatedButton(
                      onPressed: provider.isLoading
                          ? null
                          : () async {
                              final success = await provider
                                  .joinHousehold(codeController.text);
                              if (success && context.mounted) {
                                Navigator.pop(context);
                              }
                            },
                      child: provider.isLoading
                          ? const SizedBox(
                              height: 20,
                              width: 20,
                              child: CircularProgressIndicator(
                                  strokeWidth: 2))
                          : const Text('Join'),
                    ),
                  ],
                );
              },
            ),
            const SizedBox(height: 24),
          ],
        ),
      );
    },
  );
}
