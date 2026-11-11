import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../state/household_provider.dart';
import '../../../../core/utils/ui_service.dart';

void showCreateShoppingListSheet(BuildContext context) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (BuildContext context) {
      return const _CreateShoppingListSheetContent();
    },
  );
}

class _CreateShoppingListSheetContent extends StatefulWidget {
  const _CreateShoppingListSheetContent();

  @override
  State<_CreateShoppingListSheetContent> createState() =>
      _CreateShoppingListSheetContentState();
}

class _CreateShoppingListSheetContentState
    extends State<_CreateShoppingListSheetContent> {
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _itemController = TextEditingController();
  final List<String> _currentItems = [];

  void _addItem() {
    final text = _itemController.text.trim();
    if (text.isNotEmpty) {
      setState(() {
        _currentItems.add(text);
        _itemController.clear();
      });
    }
  }

  void _removeItem(int index) {
    setState(() {
      _currentItems.removeAt(index);
    });
  }

  @override
  Widget build(BuildContext context) {
    final householdProv = context.watch<HouseholdProvider>();

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
          // Header with Back and X buttons
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'New Shopping List',
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF2C3E50),
                ),
              ),
              _buildHeaderButton('X', () => Navigator.pop(context)),
            ],
          ),
          const SizedBox(height: 24),

          // List name field
          const Text(
            'List name:',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Color(0xFF2C3E50),
              fontSize: 16,
            ),
          ),
          const SizedBox(height: 8),
          TextField(
            controller: _nameController,
            decoration: InputDecoration(
              hintText: 'Enter list name...',
              hintStyle: const TextStyle(color: Color(0xFF95A5A6)),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(5),
                borderSide: const BorderSide(color: Color(0xFFE0E0E0)),
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: 12),
            ),
          ),
          const SizedBox(height: 16),

          // Add an item field
          const Text(
            'Add an item:',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Color(0xFF2C3E50),
              fontSize: 16,
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _itemController,
                  onSubmitted: (_) => _addItem(),
                  decoration: InputDecoration(
                    hintText: 'Enter new item...',
                    hintStyle: const TextStyle(color: Color(0xFF95A5A6)),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(5),
                      borderSide: const BorderSide(color: Color(0xFFE0E0E0)),
                    ),
                    contentPadding: const EdgeInsets.symmetric(horizontal: 12),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              ElevatedButton(
                onPressed: _addItem,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF3498DB),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(5),
                  ),
                  minimumSize: const Size(0, 48),
                  elevation: 0,
                ),
                child: const Text('Add', style: TextStyle(fontWeight: FontWeight.bold)),
              ),
            ],
          ),
          const SizedBox(height: 24),

          // Items display area
          Container(
            height: 150,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: const Color(0xFFECF0F1), // Background Neutral
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: const Color(0xFFE0E0E0)),
            ),
            child: _currentItems.isEmpty
                ? const Center(
                    child: Text(
                      'No items added yet',
                      style: TextStyle(color: Color(0xFF95A5A6)),
                    ),
                  )
                : ListView.separated(
                    itemCount: _currentItems.length,
                    separatorBuilder: (context, index) => const SizedBox(height: 8),
                    itemBuilder: (context, index) {
                      return Row(
                        children: [
                          Expanded(
                            child: Text(
                              _currentItems[index],
                              style: const TextStyle(
                                fontWeight: FontWeight.bold,
                                color: Color(0xFF2C3E50),
                              ),
                            ),
                          ),
                          ElevatedButton(
                            onPressed: () => _removeItem(index),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFFE74C3C),
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(horizontal: 12),
                              minimumSize: const Size(0, 32),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(5),
                              ),
                              elevation: 0,
                            ),
                            child: const Text('Remove', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                          ),
                        ],
                      );
                    },
                  ),
          ),
          const SizedBox(height: 32),

          // Bottom actions
          Row(
            children: [
              ElevatedButton(
                onPressed: householdProv.isLoading
                    ? null
                    : () async {
                        if (_nameController.text.trim().isEmpty) {
                          context.read<UiService>().showError('Please enter a list name');
                          return;
                        }
                        final success = await householdProv.createShoppingList(
                          _nameController.text.trim(),
                          _currentItems,
                        );
                        if (success && context.mounted) {
                          Navigator.pop(context);
                        }
                      },
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF3498DB),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(5),
                  ),
                  elevation: 0,
                ),
                child: householdProv.isLoading
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(
                          color: Colors.white,
                          strokeWidth: 2,
                        ),
                      )
                    : const Text('Create'),
              ),
            ],
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _buildHeaderButton(String text, VoidCallback onPressed) {
    return InkWell(
      onTap: onPressed,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: const Color(0xFFECF0F1),
          borderRadius: BorderRadius.circular(4),
          border: Border.all(color: const Color(0xFFE0E0E0)),
        ),
        child: Text(
          text,
          style: const TextStyle(
            color: Color(0xFF7F8C8D),
            fontWeight: FontWeight.bold,
            fontSize: 14,
          ),
        ),
      ),
    );
  }
}
