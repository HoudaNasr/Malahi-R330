package com.rpay.v2.domain.model

data class LegacySettings(
    val generalSystemType: String,
    val systemType: String,

    val terminalSn: String,
    val deviceTid: String,
    val nearPayToken: String,
    val devicePassword: String,

    val serverDomain: String,
    val tcpPort: String,
    val mobileNumber: String,

    val pulseNumber: String,
    val gameCost: String,

    val pulsePeriod: String,
    val pulseWidth: String,

    val latitude: String,
    val longitude: String,

    val enableDebug: Boolean,
    val enableAdminLock: Boolean,
    val systemOutService: Boolean,
    val enableAutoRefund: Boolean,

    val paymentReceiptWaitingDuration: String,

    val pulseType: String,

    val isPosStartsSession: Boolean,

    val enableSemnox: Boolean,
    val semnoxApiBaseUrl: String,
    val semnoxUsername: String,
    val semnoxPassword: String,
    val semnoxSelectedMachineName: String,
    val semnoxSelectedMachineReference: String,
    val semnoxSelectedMachinePrice: Float,
    val semnoxSelectedMachineId: String
)
