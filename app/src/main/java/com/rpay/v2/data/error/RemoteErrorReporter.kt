package com.rpay.v2.data.error

import android.util.Log
import com.rpay.v2.core.network.RestfulApis
import com.rpay.v2.data.request.ExceptionRequest
import com.rpay.v2.di.ApplicationScope
import com.rpay.v2.di.IoDispatcher
import com.rpay.v2.domain.AppInfoProvider
import com.rpay.v2.domain.error.ErrorReporter
import com.rpay.v2.domain.hardware.DeviceInfoProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

// data/error/RemoteErrorReporter.kt
@Singleton
class RemoteErrorReporter @Inject constructor(
    private val api: RestfulApis,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val deviceInfo: AppInfoProvider,
    @ApplicationScope private val scope: CoroutineScope,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ErrorReporter {

    override fun report(error: Throwable, source: String) {
        if (error is CancellationException) return
        send(source, "${error.javaClass.simpleName}: ${error.message ?: "no message"}\n" +
                error.stackTraceToString().take(4000))
    }

    override fun report(message: String, source: String) = send(source, message)

    private fun send(source: String, details: String) {
        scope.launch(io) {
            runCatching {
                api.logException(
                    ExceptionRequest(
                        error = "$source | $details",
                        versionName = deviceInfo.version,
                        terminalSn = deviceInfoProvider.getSerialNumber()?.lowercase() ?: "Unkown",
                        date = System.currentTimeMillis().toString()
                    )
                )
            }.onFailure { Log.w("ErrorReporter", "Could not send report", it) }
        }
    }
}