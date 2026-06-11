package com.utama.findfutsall.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("token")
    val token: String?,

    @SerializedName("user")
    val user: User?
)

data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("username")
    val username: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("nomor_hp")
    val nomorHp: String?,

    @SerializedName("photo")
    val photo: String?,

    @SerializedName("role")
    val role: String,

    @SerializedName("created_at")
    val createdAt: String? = null
) {
    fun isAdmin(): Boolean = role.equals("admin", ignoreCase = true)

    fun toDisplayName(): String = name.ifEmpty { email }
}
