package com.rpay.v2.features.pay.domain

interface PaymentRepository {
    suspend fun loginMobile(mobile: String): Boolean
    suspend fun startPayment(amount: Long, timeout: Long): PaymentResult
    suspend fun reverse(transactionId: String, timeout: Long): PaymentResult
}