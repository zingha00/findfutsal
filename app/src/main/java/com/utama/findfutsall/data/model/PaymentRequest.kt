package com.utama.findfutsall.data.model

data class PaymentRequest(
    val booking_id: Int,
    val method: String,
    val amount: Int
)