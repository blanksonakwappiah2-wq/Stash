package com.quickbite.android.data.api

import com.quickbite.android.data.model.Order
import com.quickbite.android.data.model.Restaurant
import com.quickbite.android.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    
    @POST("users/login")
    suspend fun login(@Body credentials: Map<String, String>): Response<User>
    
    @POST("users/register")
    suspend fun register(@Body userData: Map<String, String>): Response<User>
    
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Long): Response<User>
    
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") userId: Long, @Body userData: Map<String, String>): Response<User>
    
    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>
}
