package com.utama.findfutsall.data.model

data class BookingResponse(
    val success: Boolean,
    val message: String,
    val booking_id: Int?
)