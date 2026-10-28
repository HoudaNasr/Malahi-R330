package com.rpay.v2.domain.request_factory

import com.rpay.v2.data.datasource.local.AppPreferencesDataSource
import com.rpay.v2.data.request.GetTerminalsRequest
import javax.inject.Inject

class NearPayTerminalsRequestFactory @Inject constructor(
    private val prefs: AppPreferencesDataSource
) {

    suspend fun create(
        isConnected: Boolean? = true,
        isAssigned: Boolean? = true,
        page: Int = 1,
        limit: Int = 20
    ): GetTerminalsRequest {
        val tid = if (prefs.getDeviceTid().isNullOrBlank()) {
            null
        } else {
            prefs.getDeviceTid()
        }
        return GetTerminalsRequest(
            terminalSN = prefs.getTerminalSn(),
            tid,
            isConnected = isConnected,
            isAssigned = isAssigned,
            page = page,
            limit = limit
        )
    }
}