package com.rpay.v2.data.request

import com.google.gson.annotations.SerializedName

data class QrRequest(
    @SerializedName("terminalSn")
    val terminalSn: String,
    @SerializedName("slotCode")
    val slotCode: String?,
    @SerializedName("qrCode")
    val qrCode: String?,
    @SerializedName("lat")
    var lat: Double? = null,
    @SerializedName("lng")
    var lng: Double? = null,
    @SerializedName("date")
    var date: String?, )