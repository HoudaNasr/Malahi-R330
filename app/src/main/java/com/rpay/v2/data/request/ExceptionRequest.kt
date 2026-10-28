package com.rpay.v2.data.request

import com.google.gson.annotations.SerializedName

data class ExceptionRequest(
    @SerializedName("terminalSn")
    val terminalSn: String,
    @SerializedName("versionName")
    val versionName: String,
    @SerializedName("error")
    var error: String,
    @SerializedName("date")
    var date: String,
)