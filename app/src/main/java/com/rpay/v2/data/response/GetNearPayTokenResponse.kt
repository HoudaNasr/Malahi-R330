package com.rpay.v2.data.response

data class GetNearPayTokenResponse(
    val data: NearPayTokenDataDto?,
    val succeeded: Boolean,
    val errors: List<String>?,
    val httpStatusCode: Int
)

data class NearPayTokenDataDto(
    val token: String,
    val tid: String
)