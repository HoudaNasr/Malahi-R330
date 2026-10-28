package com.rpay.v2.presentation.home.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.domain.AppInfoProvider
import com.rpay.v2.domain.hardware.DeviceInfoProvider
import com.rpay.v2.domain.location.LocationRepository
import com.rpay.v2.features.systemselector.domain.GeneralSystemType
import com.rpay.v2.features.systemselector.domain.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val prefs: AppPreferencesRepository,
    private val appInfoProvider: AppInfoProvider
) : ViewModel() {

    private val locationUpdateIntervalMillis = 60_000L
    private var TAG = "HomeViewModelTag"
    private var isFirstStart = true
    private var locationUpdateJob: Job? = null

    val systemType: StateFlow<GeneralSystemType?> =
        settingsRepository.getGeneralSystemType() // Flow<String?>
            .map { savedValue ->
                savedValue?.let { GeneralSystemType.Companion.from(it) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.Companion.WhileSubscribed(5000),
                null
            )

    suspend fun onAppStarted() {
        prefs.setTerminalSn(deviceInfoProvider.getSerialNumber())
        prefs.saveVersionName(appInfoProvider.version.toString())
    }



    fun startLocationUpdates() {
        if (locationUpdateJob?.isActive == true) return

        locationUpdateJob = viewModelScope.launch {

            if (isFirstStart) {
                isFirstStart = false

                Log.d(TAG, "First location start - waiting 2 minutes")
                delay(120_000L)
                Log.d(TAG, "2 minutes passed")
            }

            while (isActive) {
                updateLocation()
                delay(locationUpdateIntervalMillis)
            }
        }
    }

    private suspend fun updateLocation() {
        val location = locationRepository.getCurrentLocation()

        location?.let {
            prefs.setLatLng(gpoLocation = it)
        }
    }

    fun stopLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = null
    }


    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            startLocationUpdates()
        }
    }
}