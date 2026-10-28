package com.rpay.v2.data

import android.annotation.SuppressLint
import android.os.Build
import com.rpay.v2.domain.hardware.DeviceInfoProvider
import java.io.File
import javax.inject.Inject

/**
 * R330 serial number.
 *
 * Android 14 hides Build.SERIAL from normal apps, and the R330 SDK has no device-info API,
 * so this tries the same sources as the R330 Link demo, in order:
 * Build.getSerial() (needs READ_PHONE_STATE), Build.SERIAL, system properties,
 * then the USB gadget serial (the number the kiosk PC sees, e.g. H10P77624BL1292).
 */
class DeviceInfoProviderImpl @Inject constructor() : DeviceInfoProvider {

    @SuppressLint("PrivateApi", "HardwareIds", "MissingPermission")
    override fun getSerialNumber(): String? {
        try {
            val serial = Build.getSerial()
            if (isUsable(serial)) return serial.trim()
        } catch (_: SecurityException) {
        } catch (_: LinkageError) {
        }

        @Suppress("DEPRECATION")
        if (isUsable(Build.SERIAL)) return Build.SERIAL.trim()

        for (key in SERIAL_PROPERTIES) {
            val value = getSystemProperty(key)
            if (isUsable(value)) return value!!.trim()
        }

        for (path in USB_SERIAL_FILES) {
            val value = readFirstLine(path)
            if (isUsable(value)) return value!!.trim()
        }
        return ""
    }

    @SuppressLint("PrivateApi")
    private fun getSystemProperty(key: String): String? {
        return try {
            val sp = Class.forName("android.os.SystemProperties")
            val get = sp.getMethod("get", String::class.java, String::class.java)
            get.invoke(null, key, "") as String?
        } catch (t: Throwable) {
            null
        }
    }

    private fun readFirstLine(path: String): String? {
        return try {
            File(path).bufferedReader().use { it.readLine() }
        } catch (t: Throwable) {
            null
        }
    }

    private fun isUsable(value: String?): Boolean {
        val v = value?.trim().orEmpty()
        return v.isNotEmpty() &&
                !v.equals(Build.UNKNOWN, ignoreCase = true) &&
                v != "0123456789ABCDEF"
    }

    private companion object {
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
