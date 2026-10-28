package com.rpay.v2.features.systemselector.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.domain.AppInfoProvider
import com.rpay.v2.domain.hardware.DeviceInfoProvider
import com.rpay.v2.domain.model.MachineSystemsState
import com.rpay.v2.domain.repository.MachineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SystemsViewModel @Inject constructor(
    private val prefs: AppPreferencesRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val appInfoProvider: AppInfoProvider,
    private val repository: MachineRepository,
    @ApplicationContext private val context: Context,

    ) : ViewModel() {

    init {
        viewModelScope.launch {
            onAppStarted()
        }
    }
    private val _state = MutableStateFlow(
        MachineSystemsState()
    )

    val state: StateFlow<MachineSystemsState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow(
        State()
    )
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    fun onIntent(intent: SystemsIntent) {
        when (intent) {

            is SystemsIntent.MainSelected -> {
                _state.update {
                    it.copy(selectedMain = intent.type)
                }
            }

            SystemsIntent.LogoLongPressed -> TODO()
        }
    }

    fun onAppStarted() {
        viewModelScope.launch {
//            syncMachineInfo()
        }
        viewModelScope.launch {

            prefs.setTerminalSn(deviceInfoProvider.getSerialNumber())
            prefs.saveVersionName(appInfoProvider.version.toString())

            val sn = prefs.getTerminalSn()
            val version = prefs.getVersionName()

            // ✅ update state once
            _uiState.update {
                it.copy(
                    deviceSn = sn,
                    appVersion = version
                )
            }
        }
    }
    private suspend fun syncMachineInfo() {

        val serial =
            deviceInfoProvider.getSerialNumber()
                ?: return

        repository.getMachineInfoByTerminalSerialNo(serial)
            .onSuccess { dto ->
                FileLogger.log(
                    context = context,
                    "syncMachineInfo",
                    dto.toString()
                )
            }
            .onFailure {

                FileLogger.log(
                    context = context,
                    "syncMachineInfo",
                    it.message ?: "Unknown error"
                )
            }
    }
}

// Define SystemsEvent and SystemsState in the same file
