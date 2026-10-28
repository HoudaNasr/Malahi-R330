package com.rpay.v2.domain.usecase

import com.rpay.v2.domain.repository.NetworkRepository
import javax.inject.Inject

class PingInternetUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(): Result<Long> {
        return networkRepository.pingInternet()
    }
}