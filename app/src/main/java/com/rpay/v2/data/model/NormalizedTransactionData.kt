package com.rpay.v2.data.model

data class NormalizedTransactionData(
    val amount: Double?,
    val transactionId: String?,
    val date: String,
    val cardNumber: String?,
    val tid: String?
)