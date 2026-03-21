package com.quickbite.android.data.model

import com.google.gson.annotations.SerializedName

data class MenuItem(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("restaurant") val restaurant: Restaurant? = null
)
