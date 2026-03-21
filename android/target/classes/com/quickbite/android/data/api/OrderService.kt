package com.quickbite.android.data.api

import com.quickbite.android.data.model.Order
import retrofit2.Response
import retrofit2.http.*

interface OrderService {
    
    @GET("orders")
    suspend fun getAllOrders(): Response<List<Order>>
    
    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") orderId: Long): Response<Order>
    
    @POST("orders")
    suspend fun placeOrder(@Body order: Map<String, Any>): Response<Order>
    
    @PUT("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Long,
        @Query("status") status: String
    ): Response<Order>
    
    @PUT("orders/{id}/assign")
    suspend fun assignDeliveryAgent(
        @Path("id") orderId: Long,
        @Query("agentId") agentId: Long
    ): Response<Order>
    
    @GET("orders/customer/{customerId}")
    suspend fun getOrdersByCustomer(@Path("customerId") customerId: Long): Response<List<Order>>
    
    @GET("orders/restaurant/{restaurantId}")
    suspend fun getOrdersByRestaurant(@Path("restaurantId") restaurantId: Long): Response<List<Order>>
    
    @GET("orders/agent/{agentId}")
    suspend fun getOrdersByAgent(@Path("agentId") agentId: Long): Response<List<Order>>
}
