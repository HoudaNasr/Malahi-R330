package com.rpay.v2.data.request

data class GetNearPayTokenRequest(
    val terminalSN: String? = null,
    val tid: String? = null
)