import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../shared/dto/items/response/shopping_list_response_dto.dart';
import '../../../../shared/utils/types/list_item.dart';
import '../../../../state/household_provider.dart';

void showListDetailsSheet(BuildContext context, ShoppingListResponseDTO listData) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (context) => _ListDetailsSheetContent(listData: listData),
  );
}

class _ListDetailsSheetContent extends StatefulWidget {
  final ShoppingListResponseDTO listData;
  const _ListDetailsSheetContent({required this.listData});

  @override
  State<_ListDetailsSheetContent> createState() => _ListDetailsSheetContentState();
}

class _ListDetailsSheetContentState extends State<_ListDetailsSheetContent> {
  late List<ListItem> localItems;

  @override
  void initState() {
    super.initState();
    // Deep copy the items
    localItems = widget.listData.items.map((item) => ListItem(
      itemName: item.itemName,
      isBought: item.isBought,
    )).toList();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(24.0),
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.8,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                widget.listData.name,
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF1E3A5F),
                ),
              ),
              IconButton(
                onPressed: () => Navigator.pop(context),
                icon: Container(
                  decoration: BoxDecoration(
                    color: Colors.grey.shade200,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: const Icon(Icons.close, size: 20, color: Colors.grey),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          if (localItems.isEmpty)
            const Expanded(
              child: Center(
                child: Text('No items in this list.'),
              ),
            )
          else
            Expanded(
              child: ListView.separated(
                itemCount: localItems.length,
                separatorBuilder: (_, __) => const SizedBox(height: 12),
                itemBuilder: (context, index) {
                  final item = localItems[index];
                  return Container(
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: Colors.grey.shade200),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.05),
                          blurRadius: 4,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                    child: CheckboxListTile(
                      title: Text(
                        item.itemName,
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          decoration:
                              item.isBought ? TextDecoration.lineThrough : null,
                          color:
                              item.isBought ? Colors.grey : const Color(0xFF1E3A5F),
                        ),
                      ),
                      value: item.isBought,
                      activeColor: const Color(0xFF1E3A5F),
                      onChanged: (bool? newValue) {
                        setState(() {
                          item.isBought = newValue ?? false;
                        });
                      },
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                    ),
                  );
                },
              ),
            ),
          const SizedBox(height: 16),
          Consumer<HouseholdProvider>(
            builder: (context, provider, child) {
              return ElevatedButton(
                onPressed: provider.isLoading
                    ? null
                    : () async {
                        final success = await provider.updateShoppingList(
                          widget.listData.id,
                          localItems.map((e) => e.isBought).toList(),
                        );
                        if (success && context.mounted) {
                          Navigator.pop(context);
                        }
                      },
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF1E3A5F),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: provider.isLoading
                    ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                    : const Text('Save Changes'),
              );
            },
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }
}
