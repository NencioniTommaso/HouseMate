import 'package:dio/dio.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
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

      return ShoppingListResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> deleteShoppingList(String listId) async {
    try {
      await apiClient.dio.delete('/shopping-lists/$listId');
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<ShoppingListResponseDTO> updateListInformation(
      String listId, ShoppingListUpdateRequestDTO requestDTO) async {
    try {
      final response = await apiClient.dio.patch(
        '/shopping-lists/$listId',
        data: requestDTO.toJson(),
      );

      return ShoppingListResponseDTO.fromJson(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<List<ShoppingListResponseDTO>> getShoppingItemsByHousehold() async {
    try {
      final response = await apiClient.dio.get('/shopping-lists');

      final List<dynamic> data = response.data;
      return data.map((json) => ShoppingListResponseDTO.fromJson(json)).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
