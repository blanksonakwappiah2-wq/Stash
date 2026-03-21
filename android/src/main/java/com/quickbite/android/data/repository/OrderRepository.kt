package com.quickbite.android.data.repository

import com.quickbite.android.data.api.RetrofitClient
import com.quickbite.android.data.model.Order

class OrderRepository {
    
    suspend fun placeOrder(orderData: Map<String, Any>): Result<Order> {
        return try {
            val response = RetrofitClient.orderService.placeOrder(orderData)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to place order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOrdersByCustomer(customerId: Long): Result<List<Order>> {
        return try {
            val response = RetrofitClient.orderService.getOrdersByCustomer(customerId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateOrderStatus(orderId: Long, status: String): Result<Order> {
        return try {
            val response = RetrofitClient.orderService.updateOrderStatus(orderId, status)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to update status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
