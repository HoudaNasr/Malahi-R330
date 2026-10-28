package com.rpay.v2.service

import android.app.ActivityManager
import android.content.Context

object  ServiceUtils {
    fun isServiceRunning(
        context: Context,
        serviceClass: Class<*>
    ): Boolean {
        val manager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}