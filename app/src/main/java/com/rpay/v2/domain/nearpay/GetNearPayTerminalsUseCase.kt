package com.rpay.v2.domain.nearpay

import com.rpay.v2.data.response.GetTerminalsResponse
import com.rpay.v2.data.response.TerminalsDataDto
import com.rpay.v2.domain.repository.NearPayRepo
import com.rpay.v2.domain.request_factory.NearPayTerminalsRequestFactory
import javax.inject.Inject

class GetNearPayTerminalsUseCase @Inject constructor(
    private val repository: NearPayRepo,
    private val requestFactory: NearPayTerminalsRequestFactory
) {

    suspend operator fun invoke(
        isConnected: Boolean? = false,
        isAssigned: Boolean? = false,
        page: Int = 1,
        limit: Int = 20
    ): Result<TerminalsDataDto> {

        val request = requestFactory.create(
            isConnected = isConnected,
            isAssigned = isAssigned,
            page = page,
            limit = limit
        )

        return repository.getTerminals(request)
    }
}