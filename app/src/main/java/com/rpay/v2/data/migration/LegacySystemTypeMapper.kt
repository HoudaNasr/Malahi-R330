package com.rpay.v2.data.migration

import com.rpay.v2.features.systemselector.domain.GeneralSystemType

object LegacySystemTypeMapper {
    private const val MACHINE_TYPE = "machine_game"
    private const val VENDING_TYPE = "vending_machine"
    private const val VENDING_QR_TYPE = "vending_machine_qr_code"
    private const val VENDING_QR_PAYMENT_TYPE = "vending_machine_qr_payment"
    private const val MACHINE_QR_PAYMENT_TYPE = "machine_qr_payment"
    private const val MACHINE_SEMNOX_PAYMENT_TYPE = "machine_semnox_payment"
    private const val MACHINE_POINTS_PAYMENT_TYPE = "machine_points_payment"
    private const val VENDING_MACHINE_POINTS_PAYMENT_TYPE =
        "vending_machine_points_payment"

    fun toGeneralSystemType(
        legacySystemType: String?
    ): GeneralSystemType {

        return when (legacySystemType) {

            MACHINE_TYPE ->
                GeneralSystemType.MACHINE_GAME

            VENDING_TYPE ->
                GeneralSystemType.VENDING_MACHINE

            VENDING_QR_TYPE ->
                GeneralSystemType.VENDING_QR

            VENDING_QR_PAYMENT_TYPE ->
                GeneralSystemType.VENDING_QR_PAYMENT

            MACHINE_QR_PAYMENT_TYPE ->
                GeneralSystemType.MACHINE_QR_PAYMENT

            MACHINE_SEMNOX_PAYMENT_TYPE ->
                GeneralSystemType.SEMNOX_MACHINE

            MACHINE_POINTS_PAYMENT_TYPE ->
                GeneralSystemType.MACHINE_GAME

            VENDING_MACHINE_POINTS_PAYMENT_TYPE ->
                GeneralSystemType.VENDING_MACHINE

            else ->
                GeneralSystemType.MACHINE_GAME
        }
    }
}