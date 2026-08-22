import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/chore_provider.dart';
import '../../../../state/household_provider.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/utils/format_utils.dart';
import '../../widgets/shared/app_button.dart';
import '../../widgets/shared/app_header.dart';

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
        left: AppSpacing.l,
        right: AppSpacing.l,
        top: AppSpacing.l,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Header
          AppHeader(
            title: 'Add Assignment',
            action: IconButton(
              onPressed: () => Navigator.pop(context),
              icon: const Icon(Icons.close, color: AppColors.textSecondary),
            ),
          ),
          const SizedBox(height: AppSpacing.l),

          // Chore Dropdown
          const Text(
            'Chore to assign:',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
              fontSize: 13,
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          DropdownButtonFormField<String>(
            value: _selectedChoreId,
            hint: const Text('Select chore...', style: TextStyle(color: AppColors.textHint)),
            decoration: InputDecoration(
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(AppSpacing.radiusS),
                borderSide: const BorderSide(color: AppColors.border),
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: AppSpacing.m),
            ),
            items: chores.map((c) => DropdownMenuItem(
              value: c.id,
              child: Text(c.description),
            )).toList(),
            onChanged: (val) => setState(() => _selectedChoreId = val),
          ),
          const SizedBox(height: AppSpacing.m),

          // User Dropdown
          const Text(
            'Assign to:',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
              fontSize: 13,
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          DropdownButtonFormField<String>(
            value: _selectedUserId,
            hint: const Text('Select user...', style: TextStyle(color: AppColors.textHint)),
            decoration: InputDecoration(
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(AppSpacing.radiusS),
                borderSide: const BorderSide(color: AppColors.border),
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: AppSpacing.m),
            ),
            items: members.map((m) => DropdownMenuItem(
              value: m.user.id,
              child: Text("${m.user.name} ${m.user.surname}"),
            )).toList(),
            onChanged: (val) => setState(() => _selectedUserId = val),
          ),
          const SizedBox(height: AppSpacing.m),

          // Due Date Picker
          const Text(
            'Due date:',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
              fontSize: 13,
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          OutlinedButton.icon(
            onPressed: () => _pickDateAndTime(context),
            icon: const Icon(Icons.calendar_today, color: AppColors.primary, size: 18),
            label: Text(
              _selectedDeadline == null
                  ? 'Select due date...'
                  : FormatUtils.formatDateTime(_selectedDeadline!),
              style: TextStyle(
                color: _selectedDeadline == null ? AppColors.textHint : AppColors.textPrimary,
              ),
            ),
            style: OutlinedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: AppSpacing.m, horizontal: AppSpacing.m),
              alignment: Alignment.centerLeft,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(AppSpacing.radiusS),
              ),
              side: const BorderSide(color: AppColors.border),
            ),
          ),

          // Validation Error
          if (_validationError != null)
            Padding(
              padding: const EdgeInsets.only(top: AppSpacing.s),
              child: Text(
                _validationError!,
                style: const TextStyle(color: AppColors.danger, fontSize: 13),
                textAlign: TextAlign.center,
              ),
            ),

          const SizedBox(height: AppSpacing.xl),

          // Actions
          Row(
            children: [
              Expanded(
                child: AppButton(
                  label: 'Create',
                  isLoading: choreProv.isLoading,
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
                ),
              ),
              const SizedBox(width: AppSpacing.m),
              Expanded(
                child: AppButton(
                  label: 'Cancel',
                  variant: AppButtonVariant.secondary,
                  onPressed: () => Navigator.pop(context),
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.l),
        ],
      ),
    );
  }
}
