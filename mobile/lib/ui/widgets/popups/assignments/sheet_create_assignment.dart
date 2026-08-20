import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../../../state/chore_provider.dart';
import '../../../../state/household_provider.dart';

void showCreateAssignmentSheet(BuildContext context) {
  final TextEditingController descriptionController = TextEditingController();
  String? selectedAssigneeId;
  DateTime? selectedDate;

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return StatefulBuilder(
        builder: (context, setModalState) {
          final householdProv = context.read<HouseholdProvider>();
          final members = householdProv.currentHousehold?.memberships ?? [];

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
                  'Add Assignment',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 24),
                TextField(
                  controller: descriptionController,
                  decoration: const InputDecoration(
                    labelText: 'Chore Description',
                    border: OutlineInputBorder(),
                    hintText: 'e.g. Wash the dishes',
                  ),
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<String>(
                  value: selectedAssigneeId,
                  decoration: const InputDecoration(
                    labelText: 'Assign to',
                    border: OutlineInputBorder(),
                  ),
                  items: members.map((m) {
                    return DropdownMenuItem(
                      value: m.user.id,
                      child: Text("${m.user.name} ${m.user.surname}"),
                    );
                  }).toList(),
                  onChanged: (val) {
                    setModalState(() {
                      selectedAssigneeId = val;
                    });
                  },
                ),
                const SizedBox(height: 16),
                OutlinedButton.icon(
                  onPressed: () async {
                    final date = await showDatePicker(
                      context: context,
                      initialDate: DateTime.now(),
                      firstDate: DateTime.now(),
                      lastDate: DateTime.now().add(const Duration(days: 365)),
                    );
                    if (date != null) {
                      setModalState(() {
                        selectedDate = date;
                      });
                    }
                  },
                  icon: const Icon(Icons.calendar_today),
                  label: Text(selectedDate == null
                      ? 'Select Due Date'
                      : 'Due: ${DateFormat('yyyy-MM-dd').format(selectedDate!)}'),
                ),
                const SizedBox(height: 24),
                Consumer<ChoreProvider>(
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
                          onPressed: provider.isLoading || selectedAssigneeId == null
                              ? null
                              : () async {
                                  final success = await provider
                                      .createChoreAndAssignment(
                                    descriptionController.text,
                                    selectedAssigneeId!,
                                    selectedDate,
                                    householdProv.currentHousehold!.id,
                                  );
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
                              : const Text('Create Assignment'),
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
    },
  );
}
