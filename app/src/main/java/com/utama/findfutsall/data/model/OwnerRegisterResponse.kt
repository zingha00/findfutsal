package com.utama.findfutsall.data.model

data class OwnerRegisterResponse(
    val success: Boolean,
    val message: String,
    val data: OwnerData? = null
)

data class OwnerData(
    val owner_id: Int,
    val user_id: Int,
    val nama_lapangan: String,
    val status: String
)