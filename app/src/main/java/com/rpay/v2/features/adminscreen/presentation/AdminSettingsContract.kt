package com.rpay.v2.features.adminscreen.presentation

import androidx.compose.runtime.Immutable
import com.rpay.v2.core.base.UiIntent
import com.rpay.v2.core.base.UiState

object AdminSettingsContract {

    @Immutable
    data class State (
        val isLoading: Boolean = false,

        // Session
        val sessionRemainingSeconds: Int = 90,
        val sessionTimeoutSeconds: Int = 90,

        // Device controls
        val isWifiEnabled: Boolean = false,
        val isMobileDataEnabled: Boolean = false,
        val isBluetoothEnabled: Boolean = false,

        // Permissions / device capabilities
        val isCameraEnabled: Boolean = false,
        val isLocationEnabled: Boolean = false,

        // Internet
        val pingState: PingState = PingState.Idle,

        // Uninstall
        val isUninstallEnabled: Boolean = false,
        val showUninstallPasswordDialog: Boolean = false,
        val uninstallPasswordError: String? = null,

        val errorMessage: String? = null
    ): UiState

    sealed interface PingState {
        data object Idle : PingState
        data object Testing : PingState
        data class Success(val latencyMs: Long) : PingState
        data class Failed(val message: String? = null) : PingState
    }

    sealed interface Intent: UiIntent {

        data object Init : Intent

        data object BackClicked : Intent

        // Device controls
        data object WifiClicked : Intent
        data object MobileDataClicked : Intent
        data object BluetoothClicked : Intent
        data object CameraClicked : Intent
        data object LocationClicked : Intent

        // Connectivity
        data object ConnectivitySettingsClicked : Intent
        data object PingInternetClicked : Intent


        // Session
        data object RefreshSessionClicked : Intent
        data object SessionExpired : Intent

        // State
        data object RefreshStates : Intent
    }

    sealed interface UiEffect {

        data object NavigateBack : UiEffect

        data object OpenConnectivitySettings : UiEffect
        data object RequestCameraPermission : UiEffect
        data object RequestLocationPermission : UiEffect

        data object OpenAppPermissionSettings : UiEffect

        data class ShowToast(
            val message: String
        ) : UiEffect
    }
}