package com.rpay.v2.data.repository

import com.rpay.v2.domain.repository.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class NetworkRepositoryImpl @Inject constructor() : NetworkRepository {

    override suspend fun pingInternet(): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()

                val connection =
                    URL("https://www.google.com")
                        .openConnection() as HttpURLConnection

                connection.connectTimeout = 5_000
                connection.readTimeout = 5_000
                connection.requestMethod = "HEAD"

                try {
                    val responseCode = connection.responseCode
                    val latency =
                        System.currentTimeMillis() - startTime

                    if (responseCode in 200..399) {
                        Result.success(latency)
                    } else {
                        Result.failure(
                            IOException(
                                "HTTP response code: $responseCode"
                            )
                        )
                    }
                } finally {
                    connection.disconnect()
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}