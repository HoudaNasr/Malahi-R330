package com.rpay.v2.features.adminscreen.presentation


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.rpay.v2.core.base.BaseViewModel
import com.rpay.v2.domain.hardware.KioskManager
import com.rpay.v2.domain.usecase.PingInternetUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers

import javax.inject.Inject

@HiltViewModel
class AdminSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val kioskManager: KioskManager,
    private val pingInternetUseCase: PingInternetUseCase
) : BaseViewModel<
        AdminSettingsContract.State,
        AdminSettingsContract.Intent
        >(
    initialState = AdminSettingsContract.State()
) {

    private val TAG: String = "AdminSettingsTag"
    private val _uiEffects =
        MutableSharedFlow<AdminSettingsContract.UiEffect>()

    val uiEffects =
        _uiEffects.asSharedFlow()

    private var sessionJob: Job? = null

    override fun handleIntent(
        intent: AdminSettingsContract.Intent
    ) {
        when (intent) {

            AdminSettingsContract.Intent.Init -> {
                init()
            }

            AdminSettingsContract.Intent.BackClicked -> {
                navigateBack()
            }

            AdminSettingsContract.Intent.WifiClicked -> {
                toggleWifi()
            }

            AdminSettingsContract.Intent.MobileDataClicked -> {
                toggleMobileData()
            }

            AdminSettingsContract.Intent.BluetoothClicked -> {
                toggleBluetooth()
            }

            AdminSettingsContract.Intent.CameraClicked -> {
                handleCamera()
            }

            AdminSettingsContract.Intent.LocationClicked -> {
                handleLocation()
            }

            AdminSettingsContract.Intent.ConnectivitySettingsClicked -> {
                openConnectivitySettings()
            }

            AdminSettingsContract.Intent.PingInternetClicked -> {
                pingInternet()
            }



            AdminSettingsContract.Intent.RefreshSessionClicked -> {
                refreshSession()
            }

            AdminSettingsContract.Intent.SessionExpired -> {
                handleSessionExpired()
            }

            AdminSettingsContract.Intent.RefreshStates -> {
                viewModelScope.launch(Dispatchers.IO) {
                    refreshStates()
                }
            }
        }
    }

    private fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshStates()
        }
        startSessionTimer()
    }

    private  suspend fun refreshStates() {
            try {
                val wifiEnabled =
                    kioskManager.isWifiEnabled()

                val bluetoothEnabled =
                    kioskManager.isBluetoothEnabled()

                val mobileDataEnabled =
                    kioskManager.isMobileDataEnabled()

                val cameraEnabled =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                val locationEnabled =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                _state.update {
                    it.copy(
                        isWifiEnabled = wifiEnabled,
                        isMobileDataEnabled = mobileDataEnabled,
                        isBluetoothEnabled = bluetoothEnabled,
                        isCameraEnabled = cameraEnabled,
                        isLocationEnabled = locationEnabled
                    )
                }

            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Failed to refresh admin settings states",
                    e
                )

                _uiEffects.emit(
                    AdminSettingsContract.UiEffect.ShowToast(
                        "Failed to read device state"
                    )
                )
            }
    }

    private fun toggleWifi() {
        viewModelScope.launch(Dispatchers.IO) {
            val newValue = !_state.value.isWifiEnabled

            try {
                val success = kioskManager.setWifiEnabled(newValue)

                if (success) {
                    _state.update {
                        it.copy(isWifiEnabled = newValue)
                    }
                } else {
                    Log.e(TAG, "Failed to set Wi-Fi = $newValue")
                    refreshStates()

                    _uiEffects.emit(
                        AdminSettingsContract.UiEffect.ShowToast(
                            "Failed to change Wi-Fi"
                        )
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle Wi-Fi", e)

                refreshStates()

                _uiEffects.emit(
                    AdminSettingsContract.UiEffect.ShowToast(
                        "Failed to change Wi-Fi"
                    )
                )
            }
        }
    }

    private fun toggleMobileData() {
        viewModelScope.launch(Dispatchers.IO) {
            val newValue = !_state.value.isMobileDataEnabled

            try {
                val success = kioskManager.setMobileDataEnabled(newValue)

                if (success) {
                    _state.update {
                        it.copy(
                            isMobileDataEnabled = newValue
                        )
                    }

                    Log.d(
                        TAG,
                        "Mobile Data changed successfully = $newValue"
                    )
                } else {
                    Log.e(
                        TAG,
                        "Failed to change Mobile Data = $newValue"
                    )

                    refreshStates()

                    _uiEffects.emit(
                        AdminSettingsContract.UiEffect.ShowToast(
                            "Failed to change Mobile Data"
                        )
                    )
                }

            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Failed to toggle Mobile Data",
                    e
                )

                refreshStates()

                _uiEffects.emit(
                    AdminSettingsContract.UiEffect.ShowToast(
                        "Failed to change Mobile Data"
                    )
                )
            }
        }
    }

    private fun toggleBluetooth() {
        viewModelScope.launch(Dispatchers.IO) {
            val newValue = !_state.value.isBluetoothEnabled

            try {
                val success = kioskManager.setBluetoothEnabled(newValue)

                if (success) {
                    _state.update {
                        it.copy(
                            isBluetoothEnabled = newValue
                        )
                    }

                    Log.d(
                        TAG,
                        "Bluetooth changed successfully = $newValue"
                    )
                } else {
                    Log.e(
                        TAG,
                        "Failed to change Bluetooth = $newValue"
                    )

                    refreshStates()

                    _uiEffects.emit(
                        AdminSettingsContract.UiEffect.ShowToast(
                            "Failed to change Bluetooth"
                        )
                    )
                }

            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Failed to toggle Bluetooth",
                    e
                )

                refreshStates()

                _uiEffects.emit(
                    AdminSettingsContract.UiEffect.ShowToast(
                        "Failed to change Bluetooth"
                    )
                )
            }
        }
    }

    private fun handleCamera() {
        val isGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            _state.update {
                it.copy(isCameraEnabled = true)
            }
            return
        }

        viewModelScope.launch {
            _uiEffects.emit(
                AdminSettingsContract.UiEffect.RequestCameraPermission
            )
        }
    }

    private fun handleLocation() {
        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            _state.update {
                it.copy(isLocationEnabled = true)
            }
            return
        }

        viewModelScope.launch {
            _uiEffects.emit(
                AdminSettingsContract.UiEffect.RequestLocationPermission
            )
        }
    }

    private fun openConnectivitySettings() {
        viewModelScope.launch {
            _uiEffects.emit(
                AdminSettingsContract.UiEffect
                    .OpenConnectivitySettings
            )
        }
    }

    private fun pingInternet() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    pingState = AdminSettingsContract.PingState.Testing
                )
            }

            pingInternetUseCase()
                .onSuccess { latency ->
                    _state.update {
                        it.copy(
                            pingState =
                                AdminSettingsContract.PingState.Success(
                                    latencyMs = latency
                                )
                        )
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Internet ping failed", error)

                    _state.update {
                        it.copy(
                            pingState =
                                AdminSettingsContract.PingState.Failed(
                                    message = "Internet unavailable"
                                )
                        )
                    }
                }
        }
    }

    private fun showUninstallPasswordDialog() {
        _state.update {
            it.copy(
                showUninstallPasswordDialog = true,
                uninstallPasswordError = null
            )
        }
    }

    private fun dismissUninstallPasswordDialog() {
        _state.update {
            it.copy(
                showUninstallPasswordDialog = false,
                uninstallPasswordError = null
            )
        }
    }

    private fun submitUninstallPassword(
        password: String
    ) {
        // TODO:
        // Do NOT copy the old KbCommonManager logic here.
        //
        // We need the Wizar Admin / Credentials APIs
        // before implementing this.
    }

    private fun refreshSession() {
        _state.update {
            it.copy(
                sessionRemainingSeconds =
                    it.sessionTimeoutSeconds
            )
        }

        startSessionTimer()
    }

    private fun startSessionTimer() {
        sessionJob?.cancel()

        sessionJob = viewModelScope.launch {

            while (true) {

                delay(1_000)

                val remaining = _state.value.sessionRemainingSeconds

                if (remaining <= 1) {

                    _state.update {
                        it.copy(
                            sessionRemainingSeconds = 0
                        )
                    }

                    handleIntent(
                        AdminSettingsContract.Intent
                            .SessionExpired
                    )

                    break
                }

                _state.update {
                    it.copy(
                        sessionRemainingSeconds =
                            remaining - 1
                    )
                }
            }
        }
    }

    private fun handleSessionExpired() {
        sessionJob?.cancel()

        viewModelScope.launch {
            _uiEffects.emit(
                AdminSettingsContract.UiEffect
                    .NavigateBack
            )
        }
    }

    private fun navigateBack() {
        sessionJob?.cancel()

        viewModelScope.launch {
            _uiEffects.emit(
                AdminSettingsContract.UiEffect
                    .NavigateBack
            )
        }
    }

    override fun onCleared() {
        sessionJob?.cancel()
        super.onCleared()
    }
}