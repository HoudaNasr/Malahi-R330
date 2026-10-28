package com.rpay.v2.features.settings.presentation

import com.rpay.v2.core.utils.enums.PulseType
import com.rpay.v2.data.response.TerminalDto
import com.rpay.v2.features.semnox.data.Machine
import kotlin.String

sealed class DeviceSettingsEvent {

    data class SystemTypeChanged(val value: String) : DeviceSettingsEvent()
    data class PlusNumberChanged(val value: String) : DeviceSettingsEvent()
    data class PulsePeriodChanged(val value: String) : DeviceSettingsEvent()
    data class PulseWidthChanged(val value: String) : DeviceSettingsEvent()
    data class PulseTypeChanged(val value: PulseType) : DeviceSettingsEvent()

    data class GameCostChanged(val value: String) : DeviceSettingsEvent()

    data class ServerDomainChanged(val value: String) : DeviceSettingsEvent()
    data class TcpPortChanged(val value: String) : DeviceSettingsEvent()
    data class DevicePasswordChanged(val value: String) : DeviceSettingsEvent()

    data class MobileNumberChanged(val value: String) : DeviceSettingsEvent()
    data class WaitDurationChanged(val value: String) : DeviceSettingsEvent()
    data class SetAutoCloseTimeout(val value: Int? = null) : DeviceSettingsEvent()


    data class SystemOutChanged(val value: Boolean) : DeviceSettingsEvent()
    data class AutoRefundChanged(val value: Boolean) : DeviceSettingsEvent()
    data class EnableSemnoxChanged(val value: Boolean) : DeviceSettingsEvent()
    data class EnableIsConnected(val value: Boolean) : DeviceSettingsEvent()
    data class EnableIsAssigned(val value: Boolean) : DeviceSettingsEvent()
    data class SemnoxBaseUrlChanged(val value: String) : DeviceSettingsEvent()
    data class SemnoxUsernameChanged(val value: String) : DeviceSettingsEvent()
    data class SemnoxPasswordChanged(val value: String) : DeviceSettingsEvent()
    object FetchSemnoxMachinesClicked : DeviceSettingsEvent()
    object UnlockDeviceClicked : DeviceSettingsEvent()
    object DisconnectSemnoxMachineClicked : DeviceSettingsEvent()

    data class SemnoxMachineSelected(val machine: Machine) : DeviceSettingsEvent()
    data class TerminalSelected(val terminal: TerminalDto) : DeviceSettingsEvent()
    object ClearSemnoxMachine : DeviceSettingsEvent()

    object HideMachineDialog : DeviceSettingsEvent()
    object HideTerminalDialog : DeviceSettingsEvent()
    object SaveClicked : DeviceSettingsEvent()
    object AdminSettingsClicked : DeviceSettingsEvent()
    object BackClicked : DeviceSettingsEvent()
    object TestMdbClicked : DeviceSettingsEvent()
    object TestPulseClicked : DeviceSettingsEvent()
    object RebootClicked : DeviceSettingsEvent()

    data object UnlockClicked : DeviceSettingsEvent()

    data class PasswordSubmitted(
        val password: String
    ) : DeviceSettingsEvent()

    data object PasswordDialogDismissed : DeviceSettingsEvent()
}