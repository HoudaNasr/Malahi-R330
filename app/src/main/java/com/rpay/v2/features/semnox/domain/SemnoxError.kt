package com.rpay.v2.features.semnox.domain

sealed class SemnoxError {
    object NotConfigured : SemnoxError()
    object Network : SemnoxError()
    object Unauthorized : SemnoxError()
    data class Api(val message: String) : SemnoxError()
    object MachineNotSelected : SemnoxError()
    data class Unknown(val message: String) : SemnoxError()
}