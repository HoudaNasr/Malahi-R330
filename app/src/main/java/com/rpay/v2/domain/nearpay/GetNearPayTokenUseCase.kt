package com.rpay.v2.domain.nearpay

import com.rpay.v2.data.response.GetNearPayTokenResponse
import com.rpay.v2.domain.repository.NearPayRepo
import com.rpay.v2.domain.request_factory.NearPayTokenRequestFactory
import javax.inject.Inject

class GetNearPayTokenUseCase @Inject constructor(
    private val repository: NearPayRepo,
    private val requestFactory: NearPayTokenRequestFactory
) {

    suspend operator fun invoke(tid: String): Result<GetNearPayTokenResponse> {
        val request = requestFactory.create(tid)
        return repository.getToken(request)
    }
}