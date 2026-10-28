package com.rpay.v2.domain.hardware

interface KioskManager {
    fun lock()
    fun unlock()
    fun reboot()


    suspend fun isWifiEnabled(): Boolean
    suspend fun setWifiEnabled(enabled: Boolean): Boolean

    suspend fun isBluetoothEnabled(): Boolean
    suspend fun setBluetoothEnabled(enabled: Boolean): Boolean

    suspend fun isMobileDataEnabled(): Boolean
    suspend fun setMobileDataEnabled(enabled: Boolean): Boolean

}