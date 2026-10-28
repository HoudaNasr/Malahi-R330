package com.rpay.v2.features.systemselector.domain

enum class GeneralSystemType(val value: String) {
    SETTINGS_SCREEN("settings_screen"),
    ADMIN_SETTINGS_SCREEN("admin_settings_screen"),
    MACHINE_GAME("machine_game"),
    VENDING_MACHINE("vending_machine"),
    VMC_POS_STARTS_SESSION("vending_machine_pos_starts_session"),
    VMC_MACHINE_STARTS_SESSION("vending_machine_machine_starts_session"),
    VENDING_QR("vending_qr"),
    VENDING_QR_PAYMENT("vending_qr_payment"),
    MACHINE_QR_PAYMENT("machine_qr_payment"),
    KIOSK_MACHINE("kiosk_machine"),
    SEMNOX_MACHINE("semnox_machine");


    companion object {
        fun from(value: String): GeneralSystemType? =
            values().find { it.value == value }
    }
}