package com.rpay.v2.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MachineTerminalInfoResponse(
    @SerialName("machineId")
    val machineId: String,

    @SerialName("hasGift")
    val hasGift: Boolean,

    @SerialName("isMultiPlayer")
    val isMultiPlayer: Boolean,

    @SerialName("machineName")
    val machineName: String? = null,

    @SerialName("totalAvailableGiftCount")
    val totalAvailableGiftCount: Int? = null,

    @SerialName("cost")
    val cost: Double? = 0.0,

    @SerialName("pulseNumber")
    val pulseNumber: Int? = null,

    @SerialName("pulsePeriod")
    val pulsePeriod: Int? = null,

    @SerialName("pulseWidth")
    val pulseWidth: Int? = null,

    @SerialName("systemType")
    val systemType: String? = null,

    @SerialName("pulseType")
    val pulseType: String? = null,

    @SerialName("enableAutoRefund")
    val enableAutoRefund: Boolean? = null,

    @SerialName("isSubscriptionValid")
    val isSubscriptionValid: Boolean? = true,

    @SerialName("isActive")
    val isActive: Boolean? = true,

    @SerialName("terminalId")
    val terminalId: String,

    @SerialName("terminalName")
    val terminalName: String? = null,

    @SerialName("terminalSN")
    val terminalSN: String? = null,

    @SerialName("serverDomain")
    val serverDomain: String? = null,

    @SerialName("tcpPort")
    val tcpPort: Int? = null,

    @SerialName("devicePassword")
    val devicePassword: String? = null,

    @SerialName("mobileNumber")
    val mobileNumber: String? = null,

    @SerialName("companyId")
    val companyId: String,

    @SerialName("branchId")
    val branchId: String,

    @SerialName("branchNameEn")
    val branchNameEn: String? = null,

    @SerialName("branchNameAr")
    val branchNameAr: String? = null,

    @SerialName("branchGeofence")
    val branchGeofence: String? = null,

    @SerialName("semnoxBaseUrl")
    val semnoxBaseUrl: String? = null,

    @SerialName("semnoxUsername")
    val semnoxUsername: String? = null,

    @SerialName("semnoxPassword")
    val semnoxPassword: String? = null,

    @SerialName("semnoxPrice")
    val semnoxPrice: Int? = null,

    @SerialName("isPosStartSession")
    val isPosStartSession: Boolean? = null,

    @SerialName("paymentMethodsLookup")
    val paymentMethodsLookup: String? = null,

    @SerialName("machineTypeLookup")
    val machineTypeLookup : String? = null
)
