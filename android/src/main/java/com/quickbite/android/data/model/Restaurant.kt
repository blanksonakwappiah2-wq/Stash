package com.quickbite.android.data.model

import com.google.gson.annotations.SerializedName

data class Restaurant(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("contact") val contact: String,
    @SerializedName("category") val category: String? = null,
    @SerializedName("website") val website: String? = null,
    @SerializedName("owner") val owner: User? = null
)
