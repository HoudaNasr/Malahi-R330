package com.rpay.v2.data.request

import com.google.gson.annotations.SerializedName

data class TransactionRequest(
    @SerializedName("amount")
    var amount: Double?, // Equivalent to C# decimal?

    @SerializedName("transactionTypeLookup")
    var transactionTypeLookup: String? = null,

    @SerializedName("terminalSN")
    var terminalSN: String?,

    @SerializedName("date")
    var date: String?,

    @SerializedName("transactionId")
    var transactionId: String? = null,

    @SerializedName("giftCount")
    var giftCount: Int? = null,

    @SerializedName("lat")
    var lat: Double? = null,

    @SerializedName("lng")
    var lng: Double? = null,


    @SerializedName("slotCode")
    var slotCode: String? = null,

    @SerializedName("bankCard")
    var bankCard: String? = null,

    @SerializedName("quantity")
    var quantity: Int? = null,


    @SerializedName("tid")
    var tid: String? = null,

    @SerializedName("info")
    var info: String? = null,

)
