package com.utama.findfutsall.data.model

data class OwnerBooking(
    val id: Int,
    val customerName: String,
    val userName: String,
    val userPhone: String,
    val userPhoto: String = "",
    val userEmail: String = "",
    val fieldName: String,
    val fieldAddress: String = "",
    val fieldPhoto: String = "",
    val playDate: String,
    val startTime: String,
    val endTime: String,
    val totalPrice: Double,
    val status: String,
    val createdAt: String
)