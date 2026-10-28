package com.rpay.cm30.v2.core.utils

import com.rpay.v2.core.utils.enums.PulseType
import com.rpay.v2.features.systemselector.domain.GeneralSystemType
import com.rpay.v2.navigation.Destination
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Helper {

    const val ACTION_RECIEVED_TIMEOUT =
        "ACTION_RECIEVED_TIMEOUT"

    const val TIMEOUT_SEC =
        "TIMEOUT_SEC"

    const val MSG_MDB_INITIAL_COMPLETE: Int = 1 // Initial state complete
    const val MSG_MDB_BEGINE_SESSION: Int = MSG_MDB_INITIAL_COMPLETE + 1 // Begin session
    const val MSG_MDB_VEND: Int = MSG_MDB_BEGINE_SESSION + 1 // Vend process
    const val MSG_MDB_END_SESSION: Int = MSG_MDB_VEND + 1 // End session


    const val MSG_INITIAL_COMPLETE: Int = 1
    const val MSG_RESET: Int = 2
    const val MSG_VEND_REQUEST: Int = 3
    const val MSG_VEND_SUCCESS: Int = 4
    const val MSG_VEND_FAILURE: Int = 5
    const val MSG_VEND_CANCEL: Int = 6
    const val MSG_SESSION_COMPLETE: Int = 7
    const val SERVICE_ID = 127
    const val CHANNEL_NAME = "App Protection Service"
    const val CHANNEL_ID = "return_to_app_service"
    const val PaymentBroadcaster = "NEARPAY_PAYMENT_BROADCAST"
    const val BaseURL: String = "https://remoteposapi.malahiremote.com/api/"
    const val API_KEY: String =
        "d0043da8-3e92-4b24-88a6-f50f549c5d6a-e6850be7-4743-4f82-9952-0a95ea5b457d"

    // public static final String BaseURL = "https://z14vr8r9-44303.euw.devtunnels.ms/api/";
    // public static final String TcpBaseURL = "10.tcp.eu.ngrok.io";
    //public static int ServerPort = 25198;
    const val TcpBaseURL: String = "malahiremote.com"
    // production
    var ServerPort: Int = 5050

    fun getCurrentDateTimeString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
    fun showToast(
        context: android.content.Context,
        message: String,
        duration: Int = android.widget.Toast.LENGTH_SHORT
    ) {
        android.widget.Toast.makeText(context, message, duration).show()
    }

    fun String.toPulseType(): PulseType =
        when {
            contains("positive", ignoreCase = true) -> PulseType.POSITIVE
            contains("negative", ignoreCase = true) -> PulseType.NEGATIVE
            else -> PulseType.NEGATIVE
        }
}

fun GeneralSystemType.toRoute(): String {
    return when (this) {
        GeneralSystemType.KIOSK_MACHINE -> Destination.KioskMachine.route
        GeneralSystemType.VMC_POS_STARTS_SESSION -> Destination.VmcPosStartsSession.route
        GeneralSystemType.VMC_MACHINE_STARTS_SESSION -> Destination.VmcMachineStartsSession.route
        GeneralSystemType.MACHINE_GAME -> Destination.MachineGame.route
        GeneralSystemType.VENDING_QR -> Destination.VendingQR.route
        GeneralSystemType.VENDING_QR_PAYMENT -> Destination.VendingQRPayment.route
        GeneralSystemType.MACHINE_QR_PAYMENT -> Destination.MachineQRPayment.route
        GeneralSystemType.SETTINGS_SCREEN -> Destination.SettingsScreen.route
        GeneralSystemType.ADMIN_SETTINGS_SCREEN -> Destination.AdminSettingsScreen.route
        GeneralSystemType.SEMNOX_MACHINE -> Destination.SemnoxMachine.route
        GeneralSystemType.VENDING_MACHINE -> TODO()
    }
}


