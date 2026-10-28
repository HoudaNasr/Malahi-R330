package com.rpay.v2.domain.request_factory

import com.rpay.v2.data.datasource.local.AppPreferencesDataSource
import com.rpay.v2.data.request.GetNearPayTokenRequest
import javax.inject.Inject

class NearPayTokenRequestFactory @Inject constructor(
    private val prefs: AppPreferencesDataSource
) {

    suspend fun create(tid: String): GetNearPayTokenRequest {
        return GetNearPayTokenRequest(
            terminalSN = prefs.getTerminalSn(),
            tid = tid
        )
    }
}