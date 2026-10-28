package com.rpay.v2.data.model


/**
 * Generic communication payload used for sending messages
 * between the device and backend or external services.
 */
data class CommunicationModel (
    val msgKey: String,
    val data: Any?,
    val apiKey: String,
    val terminalSN: String,
)

data class SendPreferencesData(
    val enableDebug: Boolean? = null,
    val enableAdminLock: Boolean? = null,
    val paymentReceiptWaitingDuration: String? = "",
    val pulseWidth: String? = "",
    val mobileNumber: String? = "",
    val tcpPort: String? = "",
    val pulsePeriod: String? = "",
    val systemType: String? = "",
    val pulseNumber: String? = "",
    val gameCost: String? = "",
    val serverDomain: String? = "",
    val pulseType: String? = "",
    val devicePassword: String? = "",
   // val apiKey: String? = "",
    //val systemOutService: Boolean? = null,

    val enableAutoRefund: Boolean? = null,
)

data class SubscriptionCheckData(
    val IsSubscriptionValid: Boolean? = null,
)

data class NearPayAuthUpdateData(
    val Tid: String? = null,
    val Token: String? = null
)

data class TerminalSetting(
    val TimeoutSec: Int,
)

data class MachineStatus(
    val isAttached : Boolean,
    val serverTime : String,
    val lastResponseElapsed: String
)
