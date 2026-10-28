package com.rpay.v2.features.settings.presentation

import com.rpay.v2.core.utils.enums.PulseType
import com.rpay.v2.data.response.TerminalDto
import com.rpay.v2.features.semnox.data.Machine

data class DeviceSettingsState(
    val deviceSn: String = "",
    val systemType: String = "MDB",
    val pulseNumber: String = "1",
    val pulsePeriod: String = "150",
    val pulseWidth: String = "100",
    val pulseType: PulseType = PulseType.NEGATIVE,

    val gameCost: String = "1",

    val serverDomain: String = "",
    val tcpPort: String = "0",
    val devicePassword: String = "135246$",
    val mobileNumber: String ="",
    val waitDuration: String = "1",

    val enablePageLock: Boolean = false,
    val systemOutOfService: Boolean = false,
    val enableAutoRefund: Boolean = true,

    val enableTerminalConnected: Boolean = false,
    val enableTerminalAssigned: Boolean = false,


    val showTerminalsDialog: Boolean = false,

    val terminals: List<TerminalDto> = emptyList(),
    val isLoading: Boolean = false,

    val showPasswordDialog: Boolean = false,
    val passwordPurpose: PasswordPurpose? = null,
    val passwordError: String? = null

    )

data class SemnoxSettingsState(
    val semnoxMachines: List<Machine> = emptyList(),
    val showMachineDialog: Boolean = false,
    val semnoxBaseUrl: String = "",
    val semnoxUsername: String = "",
    val semnoxPassword: String = "",
    val enableSemnox: Boolean = false,
    val selectedSemnoxMachineName: String = "No machine selected",
    )

sealed class DeviceSettingsUiEffect {
    data class ShowError(val message: String) : DeviceSettingsUiEffect()
    data class ShowToast(val message: String) : DeviceSettingsUiEffect()
    object NavigateBack : DeviceSettingsUiEffect()
    object NavigateToAdminSettings : DeviceSettingsUiEffect()
}
