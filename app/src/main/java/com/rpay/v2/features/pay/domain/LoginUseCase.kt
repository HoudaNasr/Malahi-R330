package com.rpay.v2.features.pay.domain

import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repo: PaymentRepository
) {
    suspend operator fun invoke(mobile: String): Boolean {
        return repo.loginMobile(mobile)
    }
}