package com.rpay.v2.data

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.rpay.v2.domain.hardware.DeviceInfoProvider
import java.io.File
import javax.inject.Inject

/**
 * R330 serial number.
 *
 * Android 14 hides Build.SERIAL from normal apps, and the R330 SDK has no device-info API,
 * so this tries the same sources as the R330 Link demo, in order:
 * Build.getSerial() (needs READ_PHONE_STATE + privileged/device owner), Build.SERIAL, system properties,
 * then the USB gadget serial (the number the kiosk PC sees, e.g. H10P77624BL1292).
 *
 * Every step is logged under "DeviceInfoProvider". Filter with: adb logcat -s DeviceInfoProvider
 */
class DeviceInfoProviderImpl @Inject constructor() : DeviceInfoProvider {

    @SuppressLint("PrivateApi", "HardwareIds", "MissingPermission")
    override fun getSerialNumber(): String? {
        Log.d(TAG, "getSerialNumber(): start (sdk=${Build.VERSION.SDK_INT}, model=${Build.MODEL}, firmware=${Build.DISPLAY})")

        // 1) Official API: only privileged / device owner / carrier-privileged apps on Android 10+
        try {
            val serial = Build.getSerial()
            Log.d(TAG, "1) Build.getSerial() = '$serial'")
            if (isUsable(serial)) {
                Log.i(TAG, "serial found via Build.getSerial(): '${serial.trim()}'")
                return serial.trim()
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "1) Build.getSerial() denied: ${e.message}")
        } catch (e: LinkageError) {
            Log.w(TAG, "1) Build.getSerial() not available: ${e.message}")
        }

        // 2) Legacy field: "unknown" for normal apps since Android 10
        @Suppress("DEPRECATION")
        val legacySerial = Build.SERIAL
        Log.d(TAG, "2) Build.SERIAL = '$legacySerial'")
        if (isUsable(legacySerial)) {
            Log.i(TAG, "serial found via Build.SERIAL: '${legacySerial.trim()}'")
            return legacySerial.trim()
        }

        // 3) System properties (SELinux may return "" for each one)
        for (key in SERIAL_PROPERTIES) {
            val value = getSystemProperty(key)
            Log.d(TAG, "3) property $key = '$value'")
            if (isUsable(value)) {
                Log.i(TAG, "serial found via property $key: '${value!!.trim()}'")
                return value.trim()
            }
        }

        // 4) USB gadget serial files (what the kiosk PC sees over the cable)
        for (path in USB_SERIAL_FILES) {
            val value = readFirstLine(path)
            Log.d(TAG, "4) file $path = '$value'")
            if (isUsable(value)) {
                Log.i(TAG, "serial found via file $path: '${value!!.trim()}'")
                return value.trim()
            }
        }

        // TEMPORARY: the R330 hides every source from normal apps (see log above).
        // Fall back to this test terminal's label serial until the serial is provisioned per terminal.
        // Remove before shipping: every R330 would otherwise report the same SN.
        Log.e(TAG, "getSerialNumber(): no readable source on this device, using TEMP fallback '$TEMP_FALLBACK_SERIAL'")
        return TEMP_FALLBACK_SERIAL.lowercase()
    }

    @SuppressLint("PrivateApi")
    private fun getSystemProperty(key: String): String? {
        return try {
            val sp = Class.forName("android.os.SystemProperties")
            val get = sp.getMethod("get", String::class.java, String::class.java)
            get.invoke(null, key, "") as String?
        } catch (t: Throwable) {
            Log.w(TAG, "getSystemProperty($key) failed: ${t.javaClass.simpleName}: ${t.message}")
            null
        }
    }

    private fun readFirstLine(path: String): String? {
        val file = File(path)
        if (!file.exists()) {
            Log.d(TAG, "readFirstLine($path): file not found (or not visible to the app)")
            return null
        }
        return try {
            file.bufferedReader().use { it.readLine() }
        } catch (t: Throwable) {
            Log.w(TAG, "readFirstLine($path) failed: ${t.javaClass.simpleName}: ${t.message}")
            null
        }
    }

    private fun isUsable(value: String?): Boolean {
        val v = value?.trim().orEmpty()
        val usable = v.isNotEmpty() &&
                !v.equals(Build.UNKNOWN, ignoreCase = true) &&
                v != "0123456789ABCDEF"
        if (!usable && value != null) {
            Log.v(TAG, "isUsable('$value') = false")
        }
        return usable
    }

    private companion object {
        const val TAG = "DeviceInfoProvider"

        /** TEMPORARY: serial from the label of the test R330 (SN:H10P77624BL1292). Remove before shipping. */
        const val TEMP_FALLBACK_SERIAL = "H10P77624BL1292"

        val SERIAL_PROPERTIES = listOf(
            "ro.serialno",
            "ro.boot.serialno",
            "persist.sys.device.sn",
            "ro.sys.sn",
            "ro.vendor.serialno",
            "persist.vendor.sn",
            "ro.boot.sn"
        )
        val USB_SERIAL_FILES = listOf(
            "/config/usb_gadget/g1/strings/0x409/serialnumber",
            "/sys/class/android_usb/android0/iSerial"
        )
    }
}