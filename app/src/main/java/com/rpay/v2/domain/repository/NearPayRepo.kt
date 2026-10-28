package com.rpay.v2.domain.repository

import com.rpay.v2.data.request.GetNearPayTokenRequest
import com.rpay.v2.data.request.GetTerminalsRequest
import com.rpay.v2.data.response.GetNearPayTokenResponse
import com.rpay.v2.data.response.GetTerminalsResponse
import com.rpay.v2.data.response.TerminalsDataDto

interface NearPayRepo {
    suspend fun getTerminals(
        request: GetTerminalsRequest
    ): Result<TerminalsDataDto>

    suspend fun getToken(
        request: GetNearPayTokenRequest
    ): Result<GetNearPayTokenResponse>
}