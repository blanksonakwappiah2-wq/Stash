package com.quickbite.android.data.repository

import com.quickbite.android.data.api.RetrofitClient
import com.quickbite.android.data.model.User

class UserRepository {
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = RetrofitClient.userService.login(
                mapOf("email" to email, "password" to password)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<User> {
        return try {
            val response = RetrofitClient.userService.register(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password,
                    "role" to role
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserById(userId: Long): Result<User> {
        return try {
            val response = RetrofitClient.userService.getUserById(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
