package com.utama.findfutsall.data.model

data class Booking(
    val id: Int,
    val fieldName: String,
    val courtName: String = "",
    val date: String,
    val time: String,
    val price: String,
    val status: String,
    val fieldPhoto: String? = null,
    val startTime: String = "",
    val endTime: String = "",
    val totalPrice: Double = 0.0,
    val createdAt: String = ""
)