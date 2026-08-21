import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../../../state/chore_provider.dart';
import '../../../../state/household_provider.dart';

void showCreateAssignmentSheet(BuildContext context) {
  // Refresh chore and member data before opening
  context.read<ChoreProvider>().loadHouseholdChores();
  context.read<HouseholdProvider>().loadHouseholdData();

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return const _CreateAssignmentSheetContent();
    },
  );
}

class _CreateAssignmentSheetContent extends StatefulWidget {
  const _CreateAssignmentSheetContent();

  @override
  State<_CreateAssignmentSheetContent> createState() =>
      _CreateAssignmentSheetContentState();
}

class _CreateAssignmentSheetContentState
    extends State<_CreateAssignmentSheetContent> {
  String? _selectedChoreId;
  String? _selectedUserId;
  DateTime? _selectedDeadline;
  String? _validationError;

  Future<void> _pickDateAndTime(BuildContext context) async {
    final DateTime? pickedDate = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );

    if (pickedDate == null) return;
    if (!context.mounted) return;

    final TimeOfDay? pickedTime = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.now(),
    );

    if (pickedTime == null) return;

    final combined = DateTime(
      pickedDate.year,
      pickedDate.month,
      pickedDate.day,
      pickedTime.hour,
      pickedTime.minute,
    );

    _validateDate(combined);

    setState(() {
      _selectedDeadline = combined;
    });
  }

  void _validateDate(DateTime date) {
    final now = DateTime.now();
    final minAllowed = now.add(const Duration(hours: 1));
    if (date.isBefore(minAllowed)) {
      _validationError = "Selected due date is not later than one hour from now";
    } else {
      _validationError = null;
    }
  }

  bool get _isValid =>
      _selectedChoreId != null &&
      _selectedUserId != null &&
      _selectedDeadline != null &&
      _validationError == null;

  @override
  Widget build(BuildContext context) {
    final choreProv = context.watch<ChoreProvider>();
    final householdProv = context.watch<HouseholdProvider>();
    
    final chores = choreProv.householdChores;
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
          // Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Add Assignment',
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF2C3E50),
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

          // Chore Dropdown
          const Text(
            'Chore to assign:',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Color(0xFF2C3E50),
              fontSize: 16,
            ),
          ),
          const SizedBox(height: 8),
          DropdownButtonFormField<String>(
            value: _selectedChoreId,
            hint: const Text('Select chore...', style: TextStyle(color: Color(0xFF95A5A6))),
            decoration: InputDecoration(
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(5),
                borderSide: const BorderSide(color: Color(0xFFE0E0E0)),
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: 12),
            ),
            items: chores.map((c) => DropdownMenuItem(
              value: c.id,
              child: Text(c.description),
            )).toList(),
            onChanged: (val) => setState(() => _selectedChoreId = val),
          ),
          const SizedBox(height: 16),

          // User Dropdown
          const Text(
            'Assign to:',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Color(0xFF2C3E50),
              fontSize: 16,
            ),
          ),
          const SizedBox(height: 8),
          DropdownButtonFormField<String>(
            value: _selectedUserId,
            hint: const Text('Select user...', style: TextStyle(color: Color(0xFF95A5A6))),
            decoration: InputDecoration(
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(5),
                borderSide: const BorderSide(color: Color(0xFFE0E0E0)),
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: 12),
            ),
            items: members.map((m) => DropdownMenuItem(
              value: m.user.id,
              child: Text("${m.user.name} ${m.user.surname}"),
            )).toList(),
            onChanged: (val) => setState(() => _selectedUserId = val),
          ),
          const SizedBox(height: 16),

          // Due Date Picker
          const Text(
            'Due date:',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Color(0xFF2C3E50),
              fontSize: 16,
            ),
          ),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            onPressed: () => _pickDateAndTime(context),
            icon: const Icon(Icons.calendar_today, color: Color(0xFF2C3E50)),
            label: Text(
              _selectedDeadline == null
                  ? 'Select due date...'
                  : DateFormat('dd/MM/yyyy HH:mm').format(_selectedDeadline!),
              style: TextStyle(
                color: _selectedDeadline == null ? const Color(0xFF95A5A6) : Color(0xFF2C3E50),
              ),
            ),
            style: OutlinedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 12),
              alignment: Alignment.centerLeft,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(5),
              ),
              side: const BorderSide(color: Color(0xFFE0E0E0)),
            ),
          ),

          // Validation Error
          if (_validationError != null)
            Padding(
              padding: const EdgeInsets.only(top: 8),
              child: Text(
                _validationError!,
                style: const TextStyle(color: Colors.red, fontSize: 13),
                textAlign: TextAlign.center,
              ),
            ),

          const SizedBox(height: 32),

          // Actions
          Row(
            children: [
              ElevatedButton(
                onPressed: _isValid && !choreProv.isLoading
                    ? () async {
                        final success = await choreProv.createAssignment(
                          _selectedChoreId!,
                          _selectedUserId!,
                          _selectedDeadline!,
                        );
                        if (success && context.mounted) {
                          Navigator.pop(context);
                        }
                      }
                    : null,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF3498DB),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(5),
                  ),
                  elevation: 0,
                ),
                child: choreProv.isLoading
                    ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                    : const Text('Create'),
              ),
              const SizedBox(width: 12),
              ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFFECF0F1),
                  foregroundColor: const Color(0xFF7F8C8D),
                  padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                  elevation: 0,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(5),
                    side: const BorderSide(color: Color(0xFFE0E0E0)),
                  ),
                ),
                child: const Text('Cancel'),
              ),
            ],
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }
}
