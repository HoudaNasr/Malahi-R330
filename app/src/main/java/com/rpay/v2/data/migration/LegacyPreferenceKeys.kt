package com.rpay.v2.data.migration

object LegacyPreferenceKeys {

    const val PREFS_NAME = "AppSettings"

    const val GENERAL_SYSTEM_TYPE = "general_system_type"

    const val MACHINE_TYPE = "machine_game"
    const val VENDING_TYPE = "vending_machine"
    const val VENDING_QR_TYPE = "vending_machine_qr_code"
    const val VENDING_QR_PAYMENT_TYPE = "vending_machine_qr_payment"
    const val MACHINE_QR_PAYMENT_TYPE = "machine_qr_payment"
    const val MACHINE_SEMNOX_PAYMENT_TYPE = "machine_semnox_payment"
    const val MACHINE_POINTS_PAYMENT_TYPE = "machine_points_payment"
    const val VENDING_MACHINE_POINTS_PAYMENT_TYPE = "vending_machine_points_payment"

    const val PULSE_NUMBER = "pulseNumber"
    const val PACKAGE_NAME = "packageName"
    const val GAME_COST = "gameCost"
    const val SERVER_DOMAIN = "serverDomain"
    const val TCP_PORT = "tcpPort"
    const val DEVICE_PASSWORD = "devicePassword"
    const val MOBILE_NUMBER = "mobileNumber"

    const val ENABLE_DEBUG = "enableDebug"
    const val ENABLE_ADMIN_LOCK = "enableAdminLock"
    const val SYSTEM_OUT_SERVICE = "systemOutService"
    const val ENABLE_AUTO_REFUND = "enableAutoRefund"

    const val PAYMENT_RECEIPT_WAITING_DURATION = "paymentReceiptWaitingDuration"
    const val PULSE_PERIOD = "pulsePeriod"
    const val PULSE_WIDTH = "pulseWidth"

    const val SYSTEM_TYPE = "systemType"
    const val PULSE_TYPE = "pulseType"
    const val TERMINAL_SN = "terminalSN"

    const val ENABLE_SEMNOX = "enableSemnox"
    const val SEMNOX_API_BASE_URL = "semnoxApiBaseUrl"
    const val SEMNOX_USERNAME = "semnoxUsername"
    const val SEMNOX_PASSWORD = "semnoxPassword"

    const val SEMNOX_SELECTED_MACHINE_NAME = "semnoxSelectedMachineName"
    const val SEMNOX_SELECTED_MACHINE_REFERENCE = "semnoxSelectedMachineReference"
    const val SEMNOX_SELECTED_MACHINE_PRICE = "semnoxSelectedMachinePrice"
    const val SEMNOX_SELECTED_MACHINE_ID = "semnoxSelectedMachineId"

    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"

    const val DEVICE_TID = "deviceTid"
    const val NEARPAY_TOKEN = "nearPayToken"

    const val POS_STARTS_SESSION = "posStartsSession"
}