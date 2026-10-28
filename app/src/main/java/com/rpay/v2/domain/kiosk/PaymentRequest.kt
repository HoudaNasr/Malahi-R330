package com.rpay.v2.domain.kiosk

data class PaymentRequest(
    val transactionReference: String,
    val amountMinorUnits: Long,
    val currencyCode: String
)