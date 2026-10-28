package com.rpay.v2.navigation

sealed class Destination(val route: String) {

    // Machine Game
    object MachineGame : Destination("machine_game")

    // Vending Machine
    object VmcPosStartsSession : Destination("vending_machine_pos_starts_session")
    object VmcMachineStartsSession : Destination("vending_machine_machine_starts_session")

    // QR Code Systems
    object VendingQR : Destination("vending_qr")
    object VendingQRPayment : Destination("vending_qr_payment")
    object MachineQRPayment : Destination("machine_qr_payment")

    // Photo Machine
    object KioskMachine : Destination("kiosk_machine")
    object SemnoxMachine : Destination("semnox_machine")
    object SettingsScreen : Destination("settings_screen")
    object AdminSettingsScreen : Destination("admin_settings_screen")

}