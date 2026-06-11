package com.utama.findfutsall.data.model

data class BookingRequest(
    val user_id: Int,
    val field_id: Int,
    val customer_name: String,
    val play_date: String,
    val start_time: String,
    val end_time: String,
    val total_price: Int
)