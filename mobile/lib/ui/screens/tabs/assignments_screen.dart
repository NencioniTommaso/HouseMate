import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../../../../state/chore_provider.dart';
import '../../../../state/household_provider.dart';
import '../../../../state/auth_provider.dart';
import '../../../../shared/enums/chore_status.dart';
import '../../../../shared/utils/types/date_range.dart';
import '../../popups/assignments/sheet_create_assignment.dart';
import '../../popups/dialog_confirm_action.dart';
import '../../widgets/chore_assignment_item_element.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/constants/app_strings.dart';
import '../../widgets/shared/app_button.dart';
import '../../widgets/shared/app_header.dart';
import '../../widgets/shared/app_empty_state.dart';
import '../../widgets/shared/app_card.dart';
import '../../widgets/shared/app_text_field.dart';

class AssignmentsScreen extends StatefulWidget {
  const AssignmentsScreen({super.key});

  @override
  State<AssignmentsScreen> createState() => _AssignmentsScreenState();
}

class _AssignmentsScreenState extends State<AssignmentsScreen> {
  // --- Filtering State ---
  final Set<ChoreStatus> _selectedStatuses = {};
  String? _selectedUserId;
  final TextEditingController _descriptionController = TextEditingController();
  Timer? _debounceTimer;
  bool _filtersVisible = true;

  // --- Calendar State ---
  late DateTime _currentWeekStart;
  int _selectedDayIndex = 0;

  static const List<String> _weekdays = [
    'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'
  ];

  @override
  void initState() {
    super.initState();
    
    // Calculate the Monday of the current week
    final now = DateTime.now();
    _currentWeekStart = DateTime(now.year, now.month, now.day)
        .subtract(Duration(days: now.weekday - 1));
        
    // Default the selected bubble to today
    _selectedDayIndex = now.weekday - 1; 

    WidgetsBinding.instance.addPostFrameCallback((_) {
      _applyFilters();
    });
  }

  @override
  void dispose() {
    _descriptionController.dispose();
    _debounceTimer?.cancel();
    super.dispose();
  }

  // --- Filter Logic ---

  void _onFilterChanged() {
    if (_debounceTimer?.isActive ?? false) _debounceTimer!.cancel();
    _debounceTimer = Timer(const Duration(milliseconds: 500), () {
      _applyFilters();
    });
  }

  void _applyFilters() {
    // Construct range for the entire visible week
    final range = DateRange(
      startDate: _currentWeekStart,
      endDate: _currentWeekStart.add(const Duration(days: 6, hours: 23, minutes: 59, seconds: 59)),
    );

    context.read<ChoreProvider>().loadAssignments(
          statuses: _selectedStatuses.isEmpty ? null : _selectedStatuses.toList(),
          assigneeId: _selectedUserId,
          descriptionContains: _descriptionController.text.trim().isEmpty
              ? null
              : _descriptionController.text.trim(),
          dateRange: range,
        );
  }

  void _clearFilters() {
    setState(() {
      _selectedStatuses.clear();
      _selectedUserId = null;
      _descriptionController.clear();
    });
    _applyFilters();
  }

  // --- Date Math Helpers ---

  void _changeWeek(int daysToAdd) {
    setState(() {
      _currentWeekStart = _currentWeekStart.add(Duration(days: daysToAdd));
      // Optionally reset day selection or keep it. Let's reset to Monday for clarity.
      _selectedDayIndex = 0;
    });
    _applyFilters();
  }

  String _formatWeekRange() {
    final weekEnd = _currentWeekStart.add(const Duration(days: 6));
    final formatter = DateFormat('d MMM');
    final yearFormatter = DateFormat('yyyy');
    
    String startStr = formatter.format(_currentWeekStart);
    String endStr = formatter.format(weekEnd);
    
    if (_currentWeekStart.year == weekEnd.year) {
      return '$startStr - $endStr ${yearFormatter.format(weekEnd)}';
    } else {
      return '$startStr ${yearFormatter.format(_currentWeekStart)} - $endStr ${yearFormatter.format(weekEnd)}';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Consumer3<ChoreProvider, HouseholdProvider, AuthProvider>(
      builder: (context, choreProv, householdProv, authProv, child) {
        return Scaffold(
          body: _buildScreenContent(choreProv, householdProv, authProv),
        );
      },
    );
  }

  Widget _buildScreenContent(ChoreProvider choreProv, HouseholdProvider householdProv, AuthProvider authProv) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // --- 1. TOP HEADER ---
        Padding(
          padding: const EdgeInsets.all(AppSpacing.l),
          child: AppHeader(
            title: AppStrings.assignmentsTitle,
            subtitle: "Overview: ${choreProv.overview?.pendingAssignments ?? 0} pending, ${choreProv.overview?.overdueAssignments ?? 0} overdue",
            action: AppButton(
              label: AppStrings.newLabel,
              icon: Icons.add,
              onPressed: () => showCreateAssignmentSheet(context),
            ),
          ),
        ),

        // --- 2. SEARCH FILTERS ---
        _buildFiltersSection(householdProv),
        const SizedBox(height: AppSpacing.m),

        // --- 3. WEEK NAVIGATOR ---
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppSpacing.m),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              IconButton(
                icon: const Icon(Icons.arrow_back, size: 28, color: AppColors.primary),
                onPressed: () => _changeWeek(-7),
              ),
              Text(
                _formatWeekRange(),
                style: const TextStyle(
                  fontSize: 16, 
                  fontWeight: FontWeight.bold, 
                  color: AppColors.primary
                ),
              ),
              IconButton(
                icon: const Icon(Icons.arrow_forward, size: 28, color: AppColors.primary),
                onPressed: () => _changeWeek(7),
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.s),

        // --- 4. THE WEEK STRIP ---
        SizedBox(
          height: 80,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: AppSpacing.l),
            itemCount: 7,
            itemBuilder: (context, index) {
              final isSelected = _selectedDayIndex == index;
              final dateForBubble = _currentWeekStart.add(Duration(days: index));

              return GestureDetector(
                onTap: () => setState(() => _selectedDayIndex = index),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  width: 65,
                  margin: const EdgeInsets.only(right: AppSpacing.s),
                  decoration: BoxDecoration(
                    color: isSelected ? AppColors.primary : Colors.white,
                    borderRadius: BorderRadius.circular(AppSpacing.radiusL),
                    border: Border.all(
                      color: isSelected ? Colors.transparent : AppColors.border
                    ),
                    boxShadow: isSelected 
                        ? [BoxShadow(color: Colors.black26, blurRadius: 4, offset: const Offset(0, 2))] 
                        : null,
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        _weekdays[index],
                        style: TextStyle(
                          color: isSelected ? Colors.white70 : AppColors.textSecondary,
                          fontSize: 13,
                        ),
                      ),
                      const SizedBox(height: AppSpacing.xs),
                      Text(
                        '${dateForBubble.day}',
                        style: TextStyle(
                          color: isSelected ? Colors.white : AppColors.textPrimary,
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ),
        const SizedBox(height: AppSpacing.m),
        const Divider(height: 1),

        // --- 5. THE FILTERED LIST ---
        Expanded(
          child: RefreshIndicator(
            onRefresh: () async => _applyFilters(),
            child: _buildAssignmentList(choreProv, householdProv, authProv),
          ),
        ),
      ],
    );
  }

  Widget _buildAssignmentList(ChoreProvider choreProv, HouseholdProvider householdProv, AuthProvider authProv) {
    if (choreProv.isLoading && choreProv.assignments.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    if (choreProv.errorMessage != null && choreProv.assignments.isEmpty) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          const SizedBox(height: 100),
          Center(child: Text(choreProv.errorMessage!)),
        ],
      );
    }

    // Calculate the exact date selected
    final targetDate = _currentWeekStart.add(Duration(days: _selectedDayIndex));

    // Filter assignments locally for the selected day
    final filteredChores = choreProv.assignments.where((chore) {
      if (chore.dueDate == null) return false;
      return chore.dueDate!.year == targetDate.year &&
             chore.dueDate!.month == targetDate.month &&
             chore.dueDate!.day == targetDate.day;
    }).toList();

    if (!choreProv.isLoading && filteredChores.isEmpty) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: const [
          SizedBox(height: 100),
          AppEmptyState(
            message: AppStrings.assignmentsNoResults,
            icon: Icons.assignment_turned_in_outlined,
          ),
        ],
      );
    }

    final currentUserId = authProv.currentUser?.id ?? "";
    final isAdmin = householdProv.isAdmin(currentUserId);

    return ListView.builder(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.all(16),
      itemCount: filteredChores.length,
      itemBuilder: (context, index) {
        final assignment = filteredChores[index];
        return ChoreAssignmentItemElement(
          assignment: assignment,
          currentUserId: currentUserId,
          isAdmin: isAdmin,
          onStatusToggle: () {
            final newStatus = assignment.status == ChoreStatus.completed
                ? ChoreStatus.pending
                : ChoreStatus.completed;
            showConfirmActionDialog(
              context: context,
              title: newStatus == ChoreStatus.completed ? 'Complete Chore' : 'Reopen Chore',
              message: 'Are you sure you want to change the status of this chore?',
              onConfirm: () => choreProv.updateAssignmentStatus(assignment.assignmentId, newStatus),
            );
          },
          onDelete: () {
            showConfirmActionDialog(
              context: context,
              title: 'Delete Assignment',
              message: 'Are you sure you want to delete this specific assignment instance?',
              onConfirm: () => choreProv.deleteAssignment(assignment.assignmentId),
            );
          },
        );
      },
    );
  }

  Widget _buildFiltersSection(HouseholdProvider householdProv) {
    final memberships = householdProv.currentHousehold?.memberships ?? [];

    return AppCard(
      margin: const EdgeInsets.symmetric(horizontal: AppSpacing.l),
      padding: const EdgeInsets.all(AppSpacing.m),
      backgroundColor: Colors.white,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                AppStrings.searchFilters,
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                ),
              ),
              AppButton(
                label: AppStrings.clear,
                variant: AppButtonVariant.secondary,
                onPressed: _clearFilters,
              ),
              AppButton(
                label: _filtersVisible ? AppStrings.hide : AppStrings.show,
                variant: AppButtonVariant.secondary,
                onPressed: () => setState(() => _filtersVisible = !_filtersVisible),
              ),
            ],
          ),
          if (_filtersVisible) ...[
            const SizedBox(height: AppSpacing.s),
            // Status Checkboxes Row (Horizontally Scrollable)
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  _buildStatusCheckbox(ChoreStatus.pending, 'Pending'),
                  _buildStatusCheckbox(ChoreStatus.completed, 'Completed'),
                  _buildStatusCheckbox(ChoreStatus.overdue, 'Overdue'),
                ],
              ),
            ),
            const SizedBox(height: AppSpacing.s),
            // User, Description Row
            Row(
              children: [
                // User Dropdown
                Expanded(
                  flex: 2,
                  child: DropdownButtonFormField<String>(
                    value: _selectedUserId,
                    isExpanded: true,
                    decoration: InputDecoration(
                      hintText: 'User...',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppSpacing.radiusS)),
                      contentPadding: const EdgeInsets.symmetric(horizontal: AppSpacing.m),
                    ),
                    items: [
                      const DropdownMenuItem<String>(
                        value: null,
                        child: Text('User...'),
                      ),
                      ...memberships.map((m) => DropdownMenuItem(
                            value: m.user.id,
                            child: Text(m.user.name),
                          )),
                    ],
                    onChanged: (val) {
                      setState(() {
                        _selectedUserId = val;
                      });
                      _onFilterChanged();
                    },
                  ),
                ),
                const SizedBox(width: AppSpacing.s),
                // Description Textfield
                Expanded(
                  flex: 3,
                  child: AppTextField(
                    controller: _descriptionController,
                    onChanged: (_) => _onFilterChanged(),
                    hintText: 'Description...',
                    prefixIcon: Icons.search,
                  ),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildStatusCheckbox(ChoreStatus status, String label) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Checkbox(
          value: _selectedStatuses.contains(status),
          onChanged: (val) {
            setState(() {
              if (val == true) {
                _selectedStatuses.add(status);
              } else {
                _selectedStatuses.remove(status);
              }
            });
            _onFilterChanged();
          },
        ),
        Text(label, style: const TextStyle(fontSize: 14)),
        const SizedBox(width: 8),
      ],
    );
  }
}
