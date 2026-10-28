package com.rpay.v2.features.pay.domain

import javax.inject.Inject

class RevesePaymentUseCase @Inject constructor(
    private val repo: PaymentRepository
) {
    suspend operator fun invoke(transactionId: String, timeout: Long): PaymentResult {
        return repo.reverse(transactionId, timeout)
    }
}