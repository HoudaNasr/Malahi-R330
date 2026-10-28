package com.rpay.v2.features.semnox.data

import com.google.gson.annotations.SerializedName

// Authentication Models
data class LoginRequest(
    @SerializedName("LoginId")
    val loginId: String,
    @SerializedName("Password")
    val password: String
)

// Gameplay Models
data class GamePlayRequest(
    @SerializedName("MachineId")
    val machineId: Int,
    @SerializedName("MachineReference")
    val machineReference: String,
    @SerializedName("CardNumber")
    val cardNumber: String,
    @SerializedName("PlayDate")
    val playDate: String,
    @SerializedName("CommitGamePlay")
    val commitGamePlay: Boolean
)