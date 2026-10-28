package com.rpay.v2.features.semnox.domain

sealed class SemnoxResult<out T> {
    data class Success<T>(val data: T) : SemnoxResult<T>()
    data class Error(val error: SemnoxError) : SemnoxResult<Nothing>()
}