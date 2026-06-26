package com.utama.findfutsall.data.model

data class PaymentMethod(
    val id: Int,
    val type: String,
    val accountNumber: String,
    val accountName: String,
    val isDefault: Boolean = false
)