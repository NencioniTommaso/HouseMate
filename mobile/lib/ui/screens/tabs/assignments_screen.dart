import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/chore_provider.dart';
import '../../../state/household_provider.dart';
import '../../../shared/enums/chore_status.dart';
import '../../popups/assignments/sheet_create_assignment.dart';
import '../../widgets/chore_assignment_item_element.dart';

class AssignmentsScreen extends StatefulWidget {
  const AssignmentsScreen({super.key});

  @override
  State<AssignmentsScreen> createState() => _AssignmentsScreenState();
}

class _AssignmentsScreenState extends State<AssignmentsScreen> {
  final Set<ChoreStatus> _selectedStatuses = {};
  String? _selectedUserId;
  final TextEditingController _descriptionController = TextEditingController();
  Timer? _debounceTimer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _applyFilters(isInitialLoad: true);
    });
  }

  @override
  void dispose() {
    _descriptionController.dispose();
    _debounceTimer?.cancel();
    super.dispose();
  }

  void _onFilterChanged() {
    if (_debounceTimer?.isActive ?? false) _debounceTimer!.cancel();
    _debounceTimer = Timer(const Duration(milliseconds: 500), () {
      _applyFilters();
    });
  }

  void _applyFilters({bool isInitialLoad = false}) {
    context.read<ChoreProvider>().loadAssignments(
          statuses: _selectedStatuses.isEmpty ? null : _selectedStatuses.toList(),
          assigneeId: _selectedUserId,
          descriptionContains: _descriptionController.text.trim().isEmpty
              ? null
              : _descriptionController.text.trim(),
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

  @override
  Widget build(BuildContext context) {
    return Consumer2<ChoreProvider, HouseholdProvider>(
      builder: (context, choreProv, householdProv, child) {
        return Scaffold(
          backgroundColor: Colors.grey.shade100,
          body: RefreshIndicator(
            onRefresh: () async {
              _applyFilters();
              // Keep showing the spinner until the provider finishes
              while (choreProv.isLoading) {
                await Future.delayed(const Duration(milliseconds: 100));
              }
            },
            child: _buildScreenContent(choreProv, householdProv),
          ),
        );
      },
    );
  }

  Widget _buildScreenContent(ChoreProvider choreProv, HouseholdProvider householdProv) {
    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.all(16.0),
      children: [
        // Header
        Row(
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  "Assignments",
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                ),
                Text(
                  "Overview: ${choreProv.overview?.pendingAssignments ?? 0} pending, ${choreProv.overview?.overdueAssignments ?? 0} overdue",
                  style: const TextStyle(color: Colors.grey),
                ),
              ],
            ),
            const Spacer(),
            ElevatedButton(
              onPressed: () => showCreateAssignmentSheet(context),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.blue,
                foregroundColor: Colors.white,
              ),
              child: const Text("+ Add"),
            ),
          ],
        ),
        const SizedBox(height: 20),

        // Search Filters Section - ALWAYS VISIBLE
        _buildFiltersSection(householdProv),
        const SizedBox(height: 20),

        if (choreProv.isLoading && choreProv.assignments.isEmpty)
          const Center(
            child: Padding(
              padding: EdgeInsets.only(top: 50),
              child: CircularProgressIndicator(),
            ),
          )
        else if (choreProv.errorMessage != null && choreProv.assignments.isEmpty)
          Center(
            child: Padding(
              padding: const EdgeInsets.only(top: 50),
              child: Text(choreProv.errorMessage!),
            ),
          )
        else if (!choreProv.isLoading && choreProv.assignments.isEmpty)
          const Center(
            child: Padding(
              padding: EdgeInsets.only(top: 100),
              child: Text("No assignments found"),
            ),
          )
        else ...[
          if (choreProv.isLoading)
            const Padding(
              padding: EdgeInsets.only(bottom: 16),
              child: Center(child: LinearProgressIndicator()),
            ),
          ...choreProv.assignments.map((assignment) => ChoreAssignmentItemElement(
                assignment: assignment,
                onStatusToggle: () {
                  // TODO: Implement status update
                },
              )),
        ],
      ],
    );
  }

  Widget _buildFiltersSection(HouseholdProvider householdProv) {
    final memberships = householdProv.currentHousehold?.memberships ?? [];

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.grey.shade200),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Search Filters',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF1E3A5F),
                ),
              ),
              TextButton(
                onPressed: _clearFilters,
                style: TextButton.styleFrom(
                  foregroundColor: Colors.grey.shade700,
                  padding: EdgeInsets.zero,
                  minimumSize: const Size(50, 30),
                  tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                ),
                child: const Text('Clear', style: TextStyle(fontSize: 14)),
              ),
            ],
          ),
          const SizedBox(height: 12),
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
          const SizedBox(height: 12),
          // User, Description Row (Horizontally Scrollable)
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                // User Dropdown
                SizedBox(
                  width: 150,
                  child: DropdownButtonFormField<String>(
                    value: _selectedUserId,
                    isExpanded: true,
                    decoration: InputDecoration(
                      hintText: 'User...',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12),
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
                const SizedBox(width: 8),
                // Description Textfield
                SizedBox(
                  width: 200,
                  child: TextField(
                    controller: _descriptionController,
                    onChanged: (_) => _onFilterChanged(),
                    decoration: InputDecoration(
                      hintText: 'Description...',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12),
                    ),
                  ),
                ),
              ],
            ),
          ),
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
