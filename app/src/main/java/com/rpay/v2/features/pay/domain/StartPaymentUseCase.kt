package com.rpay.v2.features.pay.domain

import javax.inject.Inject

class StartPaymentUseCase @Inject constructor(
    private val repo: PaymentRepository
) {
    suspend operator fun invoke(amount: Long, timeout: Long): PaymentResult {
        return repo.startPayment(amount, timeout)
    }
}