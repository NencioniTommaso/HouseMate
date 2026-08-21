import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/chore_provider.dart';
import '../../../../state/household_provider.dart';

void showCreateChoreSheet(BuildContext context) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return const _CreateChoreSheetContent();
    },
  );
}

class _CreateChoreSheetContent extends StatefulWidget {
  const _CreateChoreSheetContent();

  @override
  State<_CreateChoreSheetContent> createState() => _CreateChoreSheetContentState();
}

class _CreateChoreSheetContentState extends State<_CreateChoreSheetContent> {
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _frequencyController = TextEditingController();

  @override
  void dispose() {
    _nameController.dispose();
    _frequencyController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final choreProv = context.watch<ChoreProvider>();

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
          // Header with X button
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'New Chore',
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF1E3A5F),
                ),
              ),
              InkWell(
                onTap: () => Navigator.pop(context),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: Colors.grey.shade100,
                    borderRadius: BorderRadius.circular(4),
                    border: Border.all(color: Colors.grey.shade300),
                  ),
                  child: const Text(
                    'X',
                    style: TextStyle(
                      color: Colors.grey,
                      fontWeight: FontWeight.bold,
                      fontSize: 14,
                    ),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),

          // Chore name field
          const Text(
            'Chore name:',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Color(0xFF1E3A5F),
              fontSize: 16,
            ),
          ),
          const SizedBox(height: 8),
          TextField(
            controller: _nameController,
            autofocus: true,
            decoration: InputDecoration(
              hintText: 'Enter chore name...',
              hintStyle: TextStyle(color: Colors.grey.shade400),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide(color: Colors.grey.shade300),
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: 12),
            ),
          ),
          const SizedBox(height: 16),

          // Frequency field
          const Text(
            'Frequency (in days):',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Color(0xFF1E3A5F),
              fontSize: 16,
            ),
          ),
          const SizedBox(height: 8),
          TextField(
            controller: _frequencyController,
            keyboardType: TextInputType.number,
            decoration: InputDecoration(
              hintText: '0 for non-periodical',
              hintStyle: TextStyle(color: Colors.grey.shade400),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide(color: Colors.grey.shade300),
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: 12),
            ),
          ),
          const SizedBox(height: 32),

          // Bottom action
          ElevatedButton(
            onPressed: choreProv.isLoading
                ? null
                : () async {
                    final householdId = context.read<HouseholdProvider>().currentHousehold?.id;
                    if (householdId == null) return;
                    
                    if (_nameController.text.trim().isEmpty) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Please enter a chore name')),
                      );
                      return;
                    }

                    final success = await choreProv.createChore(
                      _nameController.text.trim(),
                      int.tryParse(_frequencyController.text) ?? 0,
                      householdId,
                    );
                    if (success && context.mounted) {
                      Navigator.pop(context);
                    }
                  },
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF3498DB),
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(vertical: 16),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
            ),
            child: choreProv.isLoading
                ? const SizedBox(
                    height: 20,
                    width: 20,
                    child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                  )
                : const Text('Create', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }
}
