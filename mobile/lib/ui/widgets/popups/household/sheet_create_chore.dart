import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/chore_provider.dart';
import '../../../../state/household_provider.dart';

void showCreateChoreSheet(BuildContext context) {
  final TextEditingController nameController = TextEditingController();
  final TextEditingController frequencyController = TextEditingController();

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
              'Create New Chore',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            TextField(
              controller: nameController,
              decoration: const InputDecoration(
                labelText: 'Chore Name',
                border: OutlineInputBorder(),
                hintText: 'e.g. Clean the kitchen',
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: frequencyController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Frequency (in days)',
                border: OutlineInputBorder(),
                hintText: '0 for non-periodical',
              ),
            ),
            const SizedBox(height: 24),
            Consumer<ChoreProvider>(
              builder: (context, provider, child) {
                return ElevatedButton(
                  onPressed: provider.isLoading
                      ? null
                      : () async {
                          final householdId = context.read<HouseholdProvider>().currentHousehold?.id;
                          if (householdId == null) return;
                          
                          final success = await provider.createChore(
                            nameController.text,
                            int.tryParse(frequencyController.text) ?? 0,
                            householdId,
                          );
                          if (success && context.mounted) {
                            Navigator.pop(context);
                          }
                        },
                  child: provider.isLoading
                      ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
                      : const Text('Create'),
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
