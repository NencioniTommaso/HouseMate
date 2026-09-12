import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/chore_provider.dart';
import '../../../../state/household_provider.dart';
import '../../../../state/auth_provider.dart';
import '../dialog_confirm_action.dart';
import 'sheet_create_chore.dart';

void showChoresListSheet(BuildContext context) {
  // Refresh household chores
  context.read<ChoreProvider>().loadHouseholdChores();

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return const _ChoresListSheetContent();
    },
  );
}

class _ChoresListSheetContent extends StatelessWidget {
  const _ChoresListSheetContent();

  @override
  Widget build(BuildContext context) {
    final choreProv = context.watch<ChoreProvider>();
    final householdProv = context.watch<HouseholdProvider>();
    final authProv = context.watch<AuthProvider>();
    
    final chores = choreProv.householdChores;
    final isUserAdmin = householdProv.isAdmin(authProv.currentUser?.id ?? "");

    return Container(
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.8,
      ),
      decoration: const BoxDecoration(
        color: Color(0xFFF4F6F8), // Background Light
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      padding: const EdgeInsets.all(24.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Chores List',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF2C3E50),
                ),
              ),
              IconButton(
                onPressed: () => Navigator.pop(context),
                icon: Container(
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(4),
                    border: Border.all(color: const Color(0xFFE0E0E0)),
                  ),
                  child: const Icon(Icons.close, size: 20, color: Color(0xFF7F8C8D)),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          if (choreProv.isLoading && chores.isEmpty)
            const Expanded(
              child: Center(child: CircularProgressIndicator()),
            )
          else
            Expanded(
              child: ListView.separated(
              itemCount: chores.length,
              separatorBuilder: (context, index) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                final chore = chores[index];
                return Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: const Color(0xFFE0E0E0)),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withValues(alpha: 0.05),
                        blurRadius: 5,
                        offset: const Offset(0, 2),
                      ),
                    ],
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              chore.description,
                              style: const TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                                color: Color(0xFF2C3E50),
                              ),
                            ),
                            Text(
                              'Frequency: ${chore.frequencyDays == 0 ? "not periodical" : chore.frequencyDays == 1 ? "every day" : "every ${chore.frequencyDays} days"}',
                              style: const TextStyle(color: Color(0xFF95A5A6), fontSize: 11),
                            ),
                          ],
                        ),
                      ),
                      ElevatedButton(
                        onPressed: isUserAdmin ? () {
                          showConfirmActionDialog(
                            context: context,
                            title: 'Delete Chore',
                            message: 'Are you sure you want to delete this chore definition?',
                            onConfirm: () => choreProv.deleteChore(chore.id),
                          );
                        } : null,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFFE74C3C),
                          foregroundColor: Colors.white,
                          minimumSize: const Size(0, 32),
                          padding: const EdgeInsets.symmetric(horizontal: 12),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(5),
                          ),
                          elevation: 0,
                        ),
                        child: const Text('Delete', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
          const SizedBox(height: 24),
          Center(
            child: ElevatedButton(
              onPressed: () => showCreateChoreSheet(context),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF3498DB), // Light blue
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 12),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(5),
                ),
                elevation: 0,
              ),
              child: const Text(
                '+ Create New Chore',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
