package com.rpay.v2.core.network

import retrofit2.Response


suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.UnknownError
            }
        } else {
            NetworkResult.HttpError(
                code = response.code(),
                message = response.errorBody()?.string()
            )
        }

    } catch (e: Exception) {
        NetworkResult.NetworkError(e)
    }
}