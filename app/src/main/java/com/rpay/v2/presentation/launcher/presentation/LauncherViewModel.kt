package com.rpay.v2.presentation.launcher.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.domain.AppInfoProvider
import com.rpay.v2.domain.hardware.DeviceInfoProvider
import com.rpay.v2.domain.model.MachineSystemsState
import com.rpay.v2.domain.repository.LegacyPreferencesRepository
import com.rpay.v2.domain.repository.MachineRepository
import com.rpay.v2.features.systemselector.presentation.State
import com.rpay.v2.features.systemselector.presentation.SystemsIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val deviceInfoProvider: DeviceInfoProvider,
    private val repository: MachineRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val legacyPreferencesRepository: LegacyPreferencesRepository,
    @ApplicationContext private val context: Context,

    ) : ViewModel() {
    private var started = false

    init {
        if (!started){
            viewModelScope.launch {
//                syncMachineInfo()
            }
            started = true
        }

    }
    private suspend fun syncMachineInfo() {

        val serial =
            deviceInfoProvider.getSerialNumber()
                ?: return

        repository.getMachineInfoByTerminalSerialNo(serial)
            .onSuccess { dto ->
                appPreferencesRepository.syncMachineTerminalInfo(dto = dto)
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
