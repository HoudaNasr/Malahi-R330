package com.rpay.v2.data.request

data class GetTerminalsRequest(
    val terminalSN: String? = null,
    val tid: String? = null,
    val isConnected: Boolean? = null,
    val isAssigned: Boolean? = null,
    val from: Long? = null,
    val to: Long? = null,
    val page: Int? = null,
    val limit: Int? = null
)