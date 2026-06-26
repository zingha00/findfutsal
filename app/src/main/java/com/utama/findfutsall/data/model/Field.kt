package com.utama.findfutsall.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Field(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("price")
    val price: Int,

    @SerializedName("rating")
    val rating: Float = 0f,

    @SerializedName("photo")
    val photo: String? = null,

    @SerializedName("category")
    val category: String = "",

    @SerializedName("distance")
    val distance: String? = null,

    val phone: String? = null,
    val description: String? = null,
    val facilities: String? = null,
    val openTime: String? = null,
    val closeTime: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean = true,

    @SerializedName("status")
    val status: String = "active",

    // BARU: link Google Maps milik venue, diteruskan dari level owner
    @SerializedName("maps_link")
    val mapsLink: String? = null
) : Parcelable