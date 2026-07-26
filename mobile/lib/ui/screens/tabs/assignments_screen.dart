import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../state/chore_provider.dart';
import '../../widgets/chore_assignment_item_element.dart';
import '../../widgets/popups/sheet_create_assignment.dart';

class AssignmentsScreen extends StatefulWidget {
  const AssignmentsScreen({super.key});

  @override
  State<AssignmentsScreen> createState() => _AssignmentsScreenState();
}

class _AssignmentsScreenState extends State<AssignmentsScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ChoreProvider>().loadAssignments();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<ChoreProvider>(
      builder: (context, provider, child) {
        return Scaffold(
          backgroundColor: Colors.grey.shade100,
          body: RefreshIndicator(
            onRefresh: () => provider.loadAssignments(),
            child: _buildScreenContent(provider),
          ),
        );
      },
    );
  }

  Widget _buildScreenContent(ChoreProvider provider) {
    if (provider.isLoading && provider.assignments.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.errorMessage != null && provider.assignments.isEmpty) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          const SizedBox(height: 200),
          Center(child: Text(provider.errorMessage!)),
        ],
      );
    }

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
                  "Overview: ${provider.overview?.pendingAssignments ?? 0} pending, ${provider.overview?.overdueAssignments ?? 0} overdue",
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

        if (provider.assignments.isEmpty)
          const Center(
            child: Padding(
              padding: EdgeInsets.only(top: 100),
              child: Text("No assignments found"),
            ),
          )
        else
          ...provider.assignments.map((assignment) => ChoreAssignmentItemElement(
                assignment: assignment,
                onStatusToggle: () {
                  // TODO: Implement status update
                },
              )),
      ],
    );
  }
}
