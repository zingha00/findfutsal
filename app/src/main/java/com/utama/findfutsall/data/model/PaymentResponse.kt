package com.utama.findfutsall.data.model

data class PaymentResponse(
    val success: Boolean,
    val message: String,
    val payment_id: Int?,
    val booking_id: Int?,
    val total_amount: Int?
)