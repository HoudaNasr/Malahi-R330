package com.rpay.v2.core.network

sealed class NetworkResult<out T> {

    data class Success<T>(val data: T) : NetworkResult<T>()

    data class HttpError(
        val code: Int,
        val message: String? = null
    ) : NetworkResult<Nothing>()

    data class NetworkError(val error: Throwable) : NetworkResult<Nothing>()

    data object UnknownError : NetworkResult<Nothing>()
}