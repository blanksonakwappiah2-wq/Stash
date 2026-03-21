package com.quickbite.android.data.repository

import com.quickbite.android.data.api.RetrofitClient
import com.quickbite.android.data.model.Restaurant

class RestaurantRepository {
    
    suspend fun getAllRestaurants(): Result<List<Restaurant>> {
        return try {
            val response = RetrofitClient.restaurantService.getAllRestaurants()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch restaurants"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRestaurantById(restaurantId: Long): Result<Restaurant> {
        return try {
            val response = RetrofitClient.restaurantService.getRestaurantById(restaurantId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Restaurant not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
