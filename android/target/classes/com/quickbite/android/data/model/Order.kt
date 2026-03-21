package com.quickbite.android.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class Order(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("customer") val customer: User? = null,
    @SerializedName("restaurant") val restaurant: Restaurant? = null,
    @SerializedName("status") val status: String,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("deliveryFee") val deliveryFee: Double,
    @SerializedName("distance") val distance: Double,
    @SerializedName("deliveryAddress") val deliveryAddress: String,
    @SerializedName("orderTime") val orderTime: LocalDateTime? = null,
    @SerializedName("deliveryTime") val deliveryTime: LocalDateTime? = null,
    @SerializedName("deliveryAgent") val deliveryAgent: User? = null,
    @SerializedName("items") val items: List<OrderItem>? = null
) {
    enum class Status {
        PENDING, CONFIRMED, PREPARING, READY, DELIVERING, DELIVERED
    }
}

data class OrderItem(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("menuItem") val menuItem: MenuItem,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") val price: Double
)
