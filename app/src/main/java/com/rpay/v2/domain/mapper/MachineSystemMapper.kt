package com.rpay.v2.domain.mapper

import com.rpay.v2.features.systemselector.domain.GeneralSystemType

object MachineSystemMapper {

    private const val MACHINE_VENDING = "vending_machine"
    private const val MACHINE_GAME = "game_machine"

    private const val PAYMENT_BANK_CARD = "BankCard"
    private const val PAYMENT_QR = "QRCode"
    private const val PAYMENT_SEMNOX = "Semnox"
    private const val PAYMENT_QR_AND_BANK_CARD = "$PAYMENT_BANK_CARD,$PAYMENT_QR"
    private const val SEMNOX_AND_BANK_CARD = "$PAYMENT_BANK_CARD,$PAYMENT_SEMNOX"
    private const val PAYMENT_RPAY_POINTS = "RpayPoints"

    data class BackendMachineConfig(
        val machineTypeLookup: String,
        val paymentMethodsLookup: String?,
        val isPosStartSession: Boolean?
    )

    /**
     * Backend -> App
     */
    fun toGeneralSystemType(
        machineTypeLookup: String?,
        paymentMethodsLookup: String?,
        isPosStartSession: Boolean?
    ): GeneralSystemType {

        val payments = paymentMethodsLookup
            ?.split(",")
            ?.map { it.trim() }
            ?.toSet()
            ?: emptySet()

        if (PAYMENT_SEMNOX in payments) {
            return GeneralSystemType.SEMNOX_MACHINE
        }

        if (PAYMENT_QR in payments) {
            return when (machineTypeLookup) {
                MACHINE_VENDING ->
                    GeneralSystemType.VENDING_QR_PAYMENT

                MACHINE_GAME ->
                    GeneralSystemType.MACHINE_QR_PAYMENT

                else ->
                    GeneralSystemType.SETTINGS_SCREEN
            }
        }

        return when (machineTypeLookup) {

            MACHINE_GAME ->
                GeneralSystemType.MACHINE_GAME

            MACHINE_VENDING -> {
                if (PAYMENT_BANK_CARD in payments) {
                    if (isPosStartSession == true) {
                        GeneralSystemType.VMC_POS_STARTS_SESSION
                    } else {
                        GeneralSystemType.VMC_MACHINE_STARTS_SESSION
                    }
                } else {
                    GeneralSystemType.VENDING_MACHINE
                }
            }

            else ->
                GeneralSystemType.SETTINGS_SCREEN
        }
    }

    /**
     * App -> Backend
     */
    fun toBackendConfig(
        type: GeneralSystemType
    ): BackendMachineConfig {

        return when (type) {

            GeneralSystemType.MACHINE_GAME ->
                BackendMachineConfig(
                    machineTypeLookup = MACHINE_GAME,
                    paymentMethodsLookup = PAYMENT_BANK_CARD,
                    isPosStartSession = null
                )

            GeneralSystemType.VENDING_MACHINE ->
                BackendMachineConfig(
                    machineTypeLookup = MACHINE_VENDING,
                    paymentMethodsLookup = PAYMENT_BANK_CARD,
                    isPosStartSession = null
                )

            GeneralSystemType.VMC_POS_STARTS_SESSION ->
                BackendMachineConfig(
                    machineTypeLookup = MACHINE_VENDING,
                    paymentMethodsLookup = PAYMENT_BANK_CARD,
                    isPosStartSession = true
                )

            GeneralSystemType.VMC_MACHINE_STARTS_SESSION ->
                BackendMachineConfig(
                    machineTypeLookup = MACHINE_VENDING,
                    paymentMethodsLookup = PAYMENT_BANK_CARD,
                    isPosStartSession = false
                )

            GeneralSystemType.VENDING_QR ->
                BackendMachineConfig(
                    machineTypeLookup = MACHINE_VENDING,
                    paymentMethodsLookup = PAYMENT_QR,
                    isPosStartSession = null
                )

            GeneralSystemType.VENDING_QR_PAYMENT ->
                BackendMachineConfig(
                    machineTypeLookup = MACHINE_VENDING,
                    paymentMethodsLookup = PAYMENT_QR_AND_BANK_CARD,
                    isPosStartSession = null
                )

            GeneralSystemType.MACHINE_QR_PAYMENT ->
                BackendMachineConfig(
                    machineTypeLookup = MACHINE_GAME,
                    paymentMethodsLookup = PAYMENT_QR_AND_BANK_CARD,
                    isPosStartSession = null
                )

            GeneralSystemType.KIOSK_MACHINE ->
                BackendMachineConfig(
                    machineTypeLookup = "photo_machine",
                    paymentMethodsLookup = null,
                    isPosStartSession = null
                )

            GeneralSystemType.SEMNOX_MACHINE ->
                BackendMachineConfig(
                    machineTypeLookup = MACHINE_GAME,
                    paymentMethodsLookup = SEMNOX_AND_BANK_CARD,
                    isPosStartSession = null
                )

            else ->
                BackendMachineConfig(
                    machineTypeLookup = MACHINE_VENDING,
                    paymentMethodsLookup = null,
                    isPosStartSession = null
                )
        }
    }
}