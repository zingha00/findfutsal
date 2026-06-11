package com.utama.findfutsall.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("nama_lengkap")
    val namaLengkap: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("nomor_hp")
    val nomorHp: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("password_confirmation")
    val passwordConfirmation: String
)
