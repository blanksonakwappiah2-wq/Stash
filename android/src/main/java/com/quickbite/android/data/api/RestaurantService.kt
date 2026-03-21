package com.quickbite.android.data.api

import com.quickbite.android.data.model.Restaurant
import retrofit2.Response
import retrofit2.http.*

interface RestaurantService {
    
    @GET("restaurants")
    suspend fun getAllRestaurants(): Response<List<Restaurant>>
    
    @GET("restaurants/{id}")
    suspend fun getRestaurantById(@Path("id") restaurantId: Long): Response<Restaurant>
    
    @POST("restaurants")
    suspend fun createRestaurant(@Body restaurant: Map<String, Any>): Response<Restaurant>
    
    @PUT("restaurants/{id}")
    suspend fun updateRestaurant(
        @Path("id") restaurantId: Long,
        @Body restaurant: Map<String, Any>
    ): Response<Restaurant>
    
    @DELETE("restaurants/{id}")
    suspend fun deleteRestaurant(@Path("id") restaurantId: Long): Response<Unit>
}
