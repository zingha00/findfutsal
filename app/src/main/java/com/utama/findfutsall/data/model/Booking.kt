package com.utama.findfutsall.data.model

data class Booking(
    val id: Int,
    val fieldName: String,
    val courtName: String,
    val date: String,
    val time: String,
    val price: String,
    val status: String,
    val fieldPhoto: String?
)