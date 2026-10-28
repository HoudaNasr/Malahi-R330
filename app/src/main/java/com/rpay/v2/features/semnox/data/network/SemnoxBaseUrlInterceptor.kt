package com.rpay.v2.features.semnox.data.network

import com.rpay.v2.data.repository.AppPreferencesRepository
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class SemnoxBaseUrlInterceptor @Inject constructor(
    private val prefs: AppPreferencesRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val baseUrl = runBlocking {
            prefs.getSemnoxApiBaseUrl()
        }

        val parsedBaseUrl = baseUrl.toHttpUrlOrNull()
            ?: throw IllegalStateException("Semnox API base URL is not configured")

        val newUrl = chain.request().url.newBuilder()
            .scheme(parsedBaseUrl.scheme)
            .host(parsedBaseUrl.host)
            .port(parsedBaseUrl.port)
            .build()

        val newRequest = chain.request()
            .newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}