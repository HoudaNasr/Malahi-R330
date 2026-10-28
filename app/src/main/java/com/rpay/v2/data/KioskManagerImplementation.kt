package com.rpay.v2.data

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.domain.ActivityProvider
import com.rpay.v2.domain.hardware.KioskManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * R330 kiosk controls.
 *
 * The R330 SDK has no system-device API (no reboot, Wi-Fi, Bluetooth or mobile-data control),
 * so lock/unlock use Android lock task mode and the rest only reads state with standard Android APIs.
 * Setters return false: a normal app can't change these on Android 14.
 */
class KioskManagerImplementation @Inject constructor(
    private val activityProvider: ActivityProvider,
    @ApplicationContext private val context: Context
) : KioskManager {

    private var TAG: String = "KioskManagerImplementation"

    override fun lock() {
        val activity = activityProvider.currentActivity ?: return

        try {

            activity.startLockTask()

        } catch (e: Exception) {

        }
    }

    override fun unlock() {
        val activity = activityProvider.currentActivity ?: return

        try {
            activity.stopLockTask()
        } catch (e: Exception) {
        }
    }

    override fun reboot() {
        FileLogger.log(context = context, tag = TAG, "RebootPos: not supported on R330 (no system API)")
    }

    override suspend fun isWifiEnabled(): Boolean = try {
        context.getSystemService(WifiManager::class.java)?.isWifiEnabled == true
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get Wi-Fi state", e)
        false
    }

    override suspend fun setWifiEnabled(enabled: Boolean): Boolean {
        Log.w(TAG, "setWifiEnabled($enabled) not supported on R330")
        return false
    }

    @Suppress("MissingPermission")
    override suspend fun isBluetoothEnabled(): Boolean = try {
        context.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get Bluetooth state", e)
        false
    }

    override suspend fun setBluetoothEnabled(enabled: Boolean): Boolean {
        Log.w(TAG, "setBluetoothEnabled($enabled) not supported on R330")
        return false
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    override suspend fun isMobileDataEnabled(): Boolean = try {
        context.getSystemService(TelephonyManager::class.java)?.isDataEnabled == true
    } catch (e: Exception) {
        Log.e(TAG, "Failed to read mobile data state", e)
        false
    }

    override suspend fun setMobileDataEnabled(enabled: Boolean): Boolean {
        Log.w(TAG, "setMobileDataEnabled($enabled) not supported on R330")
        return false
    }
}
