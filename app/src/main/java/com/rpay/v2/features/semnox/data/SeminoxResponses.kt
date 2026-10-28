package com.rpay.v2.features.semnox.data

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String
)

// Machine Models
data class Machine(
    @SerializedName("MachineId")
    val machineId: Int,
    @SerializedName("MachineName")
    val machineName: String,
    @SerializedName("GameName")
    val gameName: String,
    @SerializedName("Price")
    val price: Double,
    @SerializedName("VipPrice")
    val vipPrice: Double
)


data class GamePlayData(
    @SerializedName("GameplayId")
    val gameplayId: Int,
    @SerializedName("MachineId")
    val machineId: Int,
    @SerializedName("CardNumber")
    val cardNumber: String,
    @SerializedName("PlayDate")
    val playDate: String,
    @SerializedName("CommitGamePlay")
    val commitGamePlay: Boolean
)
