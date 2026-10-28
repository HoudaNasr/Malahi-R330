package com.rpay.v2.data.response

import com.google.gson.annotations.SerializedName

data class ValidateQrCodeInfoResponse(
    @SerializedName("isValid")
    val isValid: Boolean? = null,

    @SerializedName("discountPercentage")
    val discountPercentage: Double? = null,

    @SerializedName("qrHistoryOperationId")
    val qrHistoryOperationId: String? = null
)

