package com.rpay.v2.features.pay.domain

interface QrScanningRepository {
    suspend fun scanQr(amount: Long, timeout: Long): PaymentResult
    suspend fun reverseQr(amount: Long, timeout: Long): PaymentResult
}