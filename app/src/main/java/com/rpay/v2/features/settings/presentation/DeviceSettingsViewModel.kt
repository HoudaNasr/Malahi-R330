package com.rpay.v2.features.settings.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpay.v2.core.utils.enums.SystemType
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.features.semnox.data.Machine
import com.rpay.v2.features.semnox.domain.SemnoxResult
import com.rpay.v2.service.SemnoxGameManager
import com.rpay.cm30.v2.core.utils.Helper.toPulseType
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.data.response.TerminalDto
import com.rpay.v2.domain.hardware.KioskManager
import com.rpay.v2.domain.nearpay.GetNearPayTerminalsUseCase
import com.rpay.v2.domain.nearpay.GetNearPayTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject


/**
 * ViewModel responsible for managing device settings.
 *
 * It holds editable configuration values, reacts to UI events,
 * and persists settings using [AppPreferencesRepository].
 */
@HiltViewModel
class DeviceSettingsViewModel @Inject constructor(
    private val prefs: AppPreferencesRepository,
    private val semnoxManager: SemnoxGameManager,
    private val kioskManager: KioskManager,
    private val getNearPayTerminalsUseCase: GetNearPayTerminalsUseCase,
    private val getNearPayTokenUseCase: GetNearPayTokenUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val TAG = "DeviceSettingsViewModel"
    private var autoCloseJob: Job? = null

    /** Backing state for device settings screen */
    private val _state = MutableStateFlow(DeviceSettingsState())
    private val _semnoxState = MutableStateFlow(SemnoxSettingsState())
    val state: StateFlow<DeviceSettingsState> = _state
    val semnoxState: StateFlow<SemnoxSettingsState> = _semnoxState

    private val _uiEffects = MutableSharedFlow<DeviceSettingsUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()
    var isConnected = false
    var isAssgined = false


    /**
     * Loads persisted settings when the ViewModel is created.
     */
    init {
        viewModelScope.launch {
            _state.value = loadInitialState()
            _semnoxState.value = loadSemnoxState()
        }
    }

    /**
     * Handles all UI events coming from the device settings screen.
     * Updates the local state based on user input.
     */
    fun onEvent(event: DeviceSettingsEvent) {
        when (event) {

            is DeviceSettingsEvent.SystemTypeChanged ->
                update { copy(systemType = event.value) }

            is DeviceSettingsEvent.PlusNumberChanged ->
                update { copy(pulseNumber = event.value) }

            is DeviceSettingsEvent.PulsePeriodChanged ->
                update { copy(pulsePeriod = event.value) }

            is DeviceSettingsEvent.PulseWidthChanged ->
                update { copy(pulseWidth = event.value) }

            is DeviceSettingsEvent.PulseTypeChanged ->
                update { copy(pulseType = event.value) }

            is DeviceSettingsEvent.GameCostChanged ->
                update { copy(gameCost = event.value) }

            is DeviceSettingsEvent.ServerDomainChanged ->
                update { copy(serverDomain = event.value) }

            is DeviceSettingsEvent.TcpPortChanged ->
                update { copy(tcpPort = event.value) }

            is DeviceSettingsEvent.DevicePasswordChanged ->
                update { copy(devicePassword = event.value) }

            is DeviceSettingsEvent.MobileNumberChanged ->
                update { copy(mobileNumber = event.value) }

            is DeviceSettingsEvent.WaitDurationChanged ->
                update { copy(waitDuration = event.value) }

            is DeviceSettingsEvent.SystemOutChanged ->
                update { copy(systemOutOfService = event.value) }

            is DeviceSettingsEvent.AutoRefundChanged ->
                update { copy(enableAutoRefund = event.value) }

            is DeviceSettingsEvent.BackClicked -> TODO()
            is DeviceSettingsEvent.RebootClicked -> {
                kioskManager.reboot()
            }

            is DeviceSettingsEvent.SaveClicked -> {
                saveSettings()
            }

            is DeviceSettingsEvent.TestMdbClicked -> TODO()
            is DeviceSettingsEvent.TestPulseClicked -> TODO()
            is DeviceSettingsEvent.DisconnectSemnoxMachineClicked -> {
                disconnectSemnoxMachine()
            }

            is DeviceSettingsEvent.EnableSemnoxChanged -> {
                _semnoxState.update { it.copy(enableSemnox = event.value) }
                viewModelScope.launch {
                    prefs.setIsSemnoxEnabled(event.value)
                }
            }

            is DeviceSettingsEvent.FetchSemnoxMachinesClicked -> {
                fetchSemnoxMachines()
            }

            is DeviceSettingsEvent.SemnoxBaseUrlChanged -> {
                _semnoxState.update { it.copy(semnoxBaseUrl = event.value) }
            }

            is DeviceSettingsEvent.SemnoxPasswordChanged -> {
                _semnoxState.update { it.copy(semnoxPassword = event.value) }
            }

            is DeviceSettingsEvent.SemnoxUsernameChanged -> {
                _semnoxState.update { it.copy(semnoxUsername = event.value) }
            }

            DeviceSettingsEvent.ClearSemnoxMachine -> {
                log("clearSelectedMachine")
                clearSelectedMachine()
            }

            DeviceSettingsEvent.HideMachineDialog -> {
                _semnoxState.update {
                    it.copy(showMachineDialog = false)
                }
            }

            is DeviceSettingsEvent.SemnoxMachineSelected -> {
                semnoxMachineSelected(event.machine)
            }

            is DeviceSettingsEvent.TerminalSelected -> {
                terminalSelected(event.terminal)
            }

            DeviceSettingsEvent.HideTerminalDialog -> {
                _state.update {
                    it.copy(showTerminalsDialog = false)
                }
            }

            DeviceSettingsEvent.UnlockDeviceClicked -> {
                _state.update {
                    it.copy(
                        showPasswordDialog = true,
                        passwordPurpose = PasswordPurpose.UNLOCK,
                        passwordError = null
                    )
                }
            }

            is DeviceSettingsEvent.EnableIsAssigned -> {
                isAssgined = event.value
                _state.update { it.copy(enableTerminalAssigned = event.value) }
            }

            is DeviceSettingsEvent.EnableIsConnected -> {
                isConnected = event.value
                _state.update { it.copy(enableTerminalConnected = event.value) }
            }

            is DeviceSettingsEvent.SetAutoCloseTimeout -> {
                startAutoCloseTimer(event.value)
            }

            DeviceSettingsEvent.AdminSettingsClicked -> {
                viewModelScope.launch {
                    _uiEffects.emit(
                        DeviceSettingsUiEffect.NavigateToAdminSettings
                    )
                }
            }

            DeviceSettingsEvent.PasswordDialogDismissed -> {
                _state.update {
                    it.copy(
                        showPasswordDialog = false,
                        passwordPurpose = null,
                        passwordError = null
                    )
                }
            }

            is DeviceSettingsEvent.PasswordSubmitted -> {
                viewModelScope.launch {

                    val purpose = state.value.passwordPurpose

                    if (validatePassword(event.password, purpose)) {

                        _state.update {
                            it.copy(
                                showPasswordDialog = false,
                                passwordPurpose = null,
                                passwordError = null
                            )
                        }

                        when (purpose) {
                            PasswordPurpose.ADMIN -> {
                                _uiEffects.emit(
                                    DeviceSettingsUiEffect.NavigateToAdminSettings
                                )
                            }

                            PasswordPurpose.UNLOCK -> {
                               kioskManager.unlock()
                            }

                            null -> Unit
                        }

                    } else {
                        _state.update {
                            it.copy(
                                passwordError = "Incorrect password"
                            )
                        }
                    }
                }
            }

            DeviceSettingsEvent.UnlockClicked -> {
                _state.update {
                    it.copy(
                        showPasswordDialog = true,
                        passwordPurpose = PasswordPurpose.UNLOCK,
                        passwordError = null
                    )
                }
            }
        }
    }

    private suspend fun validatePassword(
        password: String,
        purpose: PasswordPurpose?
    ): Boolean {
        val devicePassword = prefs.getDevicePassword()

        val expectedPassword = when (purpose) {
            PasswordPurpose.ADMIN -> devicePassword + "Admin!#%"
            PasswordPurpose.UNLOCK -> devicePassword + "Unlock!#%"
            null -> return false
        }

        return password == expectedPassword
    }

    /**
     * Persists current settings state to local storage.
     * Called when the user confirms saving settings.
     */
    fun saveSettings() {
        viewModelScope.launch {
            val s = state.value
            val semnoxState = _semnoxState.value
            val pulseType = s.pulseType
            prefs.setSystemType(SystemType.valueOf(s.systemType.uppercase()))
            prefs.setPulseNumber(s.pulseNumber)
            prefs.setPulsePeriod(s.pulsePeriod)
            prefs.setPulseWidth(s.pulseWidth)
            prefs.setPulseType(pulseType)
            prefs.setGameCost(s.gameCost)
            prefs.setServerDomain(s.serverDomain)
            prefs.setTcpPort(s.tcpPort)
            prefs.setDevicePass(s.devicePassword)
            prefs.setMobileNumber(s.mobileNumber)
            prefs.setWaitDuration(s.waitDuration)
            prefs.setEnablePageLock(s.enablePageLock)
            prefs.setIsSystemOutService(s.systemOutOfService)
            prefs.setAutoRefund(s.enableAutoRefund)
            prefs.saveSemnoxApiBaseUrl(semnoxState.semnoxBaseUrl)
            prefs.saveSemnoxUsername(semnoxState.semnoxUsername)
            prefs.saveSemnoxPassword(semnoxState.semnoxPassword)
        }
    }


    /**
     * Used to perform immutable state updates in response to UI events.
     */
    private fun update(reducer: DeviceSettingsState.() -> DeviceSettingsState) {
        _state.update { it.reducer() }
    }

    /**
     * Loads device settings from persistent storage
     * and builds the initial UI state for the settings screen.
     *
     * @return Initial [DeviceSettingsState] populated from preferences
     */
    private suspend fun loadInitialState(): DeviceSettingsState {
        return DeviceSettingsState(
            deviceSn = prefs.getTerminalSn(),
            systemType = prefs.getSystemType(),
            pulseNumber = prefs.getPulseNumber(),
            pulsePeriod = prefs.getPulsePeriod(),
            pulseWidth = prefs.getPulseWidth(),
            pulseType = prefs.getPulseType().toPulseType(),
            gameCost = prefs.getGameCost(),
            serverDomain = prefs.getServerDomain(),
            tcpPort = prefs.getTcpPort(),
            devicePassword = prefs.getDevicePassword(),
            mobileNumber = prefs.getMobileNumber(),
            waitDuration = prefs.getPaymentWaitDuration(),
            enablePageLock = prefs.getEnablePageLock(),
            systemOutOfService = prefs.isSystemOutService(),
            enableAutoRefund = prefs.getAutoRefund(),
        )
    }

    private suspend fun loadSemnoxState(): SemnoxSettingsState {
        return SemnoxSettingsState(
            enableSemnox = prefs.isSemnoxEnabled(),
            semnoxBaseUrl = prefs.getSemnoxApiBaseUrl(),
            semnoxUsername = prefs.getSemnoxUsername(),
            semnoxPassword = prefs.getSemnoxPassword(),
            selectedSemnoxMachineName =
                prefs.getSemnoxSelectedMachineName()
                    ?: "No machine selected"
        )
    }


    private fun clearSelectedMachine() {
        log("clearSelectedMachine")
        viewModelScope.launch {
            prefs.clearSemnoxSelectedMachine()
            _semnoxState.update {
                it.copy(
                    selectedSemnoxMachineName = "",
                    showMachineDialog = false
                )
            }
        }
    }

    private fun semnoxMachineSelected(machine: Machine) {
        log(machine.toString())
        viewModelScope.launch {

            prefs.saveSemnoxSelectedMachineId(machine.machineId)
            prefs.saveSemnoxSelectedMachineName(machine.machineName)
            prefs.saveSemnoxSelectedMachineReference(machine.machineName)
            prefs.saveSemnoxSelectedMachinePrice(machine.price.toFloat())
            log("Price : ${prefs.getSemnoxSelectedMachinePrice().toString()}")

            _semnoxState.update {
                it.copy(
                    selectedSemnoxMachineName = machine.machineName,
                    showMachineDialog = false
                )
            }
        }
    }

    private fun terminalSelected(terminalDto: TerminalDto) {
        log(
            "TerminalSelected ${
                terminalDto.toString()
            }"
        )
        _state.update {
            it.copy(
                showTerminalsDialog = false
            )
        }
        fetchNearPayToken(terminalDto.tid!!)
    }

    private fun fetchSemnoxMachines() {
        val s = _semnoxState.value
        if (
            s.semnoxBaseUrl.isBlank() ||
            s.semnoxUsername.isBlank() ||
            s.semnoxPassword.isBlank()
        ) {
            viewModelScope.launch {
                emitError("Please enter API credentials first")
            }
            return
        }

        val url = s.semnoxBaseUrl.trim()
        val parsed = url.toHttpUrlOrNull()
        if (parsed == null) {
            viewModelScope.launch {
                emitError("Invalid URL. Use format: https://192.163.145.130:72")
            }
            return
        }
        viewModelScope.launch {
            prefs.saveSemnoxApiBaseUrl(url)
            prefs.saveSemnoxUsername(s.semnoxUsername)
            prefs.saveSemnoxPassword(s.semnoxPassword)

            _state.update { it.copy(isLoading = true) }

            when (val result = semnoxManager.fetchMachines()) {

                is SemnoxResult.Success -> {
                    // NEXT STEP: show dialog / picker
                    _semnoxState.update {
                        it.copy(
                            semnoxMachines = result.data,
                            showMachineDialog = true,
                        )
                    }
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }

                is SemnoxResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                    viewModelScope.launch {
                        emitError(result.error.toString())
                    }
                }

            }
        }
    }

    private fun fetchNearPayTerminals() {
        viewModelScope.launch {

            _state.update {
                it.copy(isLoading = true)
            }

            getNearPayTerminalsUseCase(
                isConnected = isConnected,
                isAssigned = isAssgined,
                limit = 20
            )
                .onSuccess { response ->

                    _state.update {
                        it.copy(
                            isLoading = false,
                            showTerminalsDialog = true,
                            terminals = response.terminals.orEmpty()
                        )
                    }
                }
                .onFailure { error ->

                    _state.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                    viewModelScope.launch {
                        emitError(error.message?.toString()!!)
                    }
                }
        }
    }

    private fun fetchNearPayToken(tid: String) {
        viewModelScope.launch {

            _state.update {
                it.copy(isLoading = true)
            }

            getNearPayTokenUseCase(tid)
                .onSuccess { response ->

                    _state.update {
                        log("NearPayTokenIs: ${response.data?.token}")
                        prefs.setNearPayToken(response.data?.token!!)
                        it.copy(
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->

                    _state.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                    viewModelScope.launch {
                        emitError(error.message?.toString()!!)
                    }
                }
        }
    }

    fun normalizeUrl(input: String): String {
        var url = input.trim()

        // If user didn't type scheme, default to https
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }

        return url
    }

    private fun disconnectSemnoxMachine() {
        viewModelScope.launch {
            prefs.clearSemnoxSelectedMachine()
            _semnoxState.update {
                it.copy(
                    selectedSemnoxMachineName = "No machine selected"
                )
            }
        }
    }

    private fun startAutoCloseTimer(timeout: Int?) {
        autoCloseJob?.cancel()

        if (timeout == null) {
            return
        }

        autoCloseJob = viewModelScope.launch {
            delay(timeout * 1000L)

            _uiEffects.emit(
                DeviceSettingsUiEffect.NavigateBack
            )
        }
    }

    private fun log(message: String) {
        FileLogger.log(context = context, TAG, message = message)
    }

    private suspend fun emitError(error: String) {
        _uiEffects.emit(DeviceSettingsUiEffect.ShowError(error))
    }
}