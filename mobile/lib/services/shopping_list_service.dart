import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../shared/dto/items/request/shopping_list_create_request_dto.dart';
import '../shared/dto/items/request/shopping_list_update_request_dto.dart';
import '../shared/dto/items/response/shopping_list_response_dto.dart';

class ShoppingListService {
  final ApiClient apiClient;

  ShoppingListService(this.apiClient);

  Future<ShoppingListResponseDTO> createShoppingList(
      ShoppingListCreateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.post(
        '/shopping-lists',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 201) {
        return ShoppingListResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to create shopping list. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to create shopping list. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<void> deleteShoppingList(String listId) async {
    try {
      final response = await apiClient.dio.delete('/shopping-lists/$listId');

      if (response.statusCode != 204) {
        throw Exception(
            'Failed to delete shopping list. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to delete shopping list. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<ShoppingListResponseDTO> updateListInformation(
      String listId, ShoppingListUpdateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.patch(
        '/shopping-lists/$listId',
        data: requestDTO.toJson(),
      );

      if (response.statusCode == 200) {
        return ShoppingListResponseDTO.fromJson(response.data);
      } else {
        throw Exception(
            'Failed to update shopping list. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to update shopping list. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }

  Future<List<ShoppingListResponseDTO>> getShoppingItemsByHousehold() async {
    try {
      final response = await apiClient.dio.get('/shopping-lists');

      if (response.statusCode == 200) {
        final List<dynamic> data = response.data;
        return data.map((json) => ShoppingListResponseDTO.fromJson(json)).toList();
      } else {
        throw Exception(
            'Failed to retrieve shopping lists for household. Status code: ${response.statusCode} and message: ${response.data}');
      }
    } on DioException catch (e) {
      throw Exception(
          'Failed to retrieve shopping lists for household. Error: ${e.message}, Response: ${e.response?.data}');
    }
  }
}
