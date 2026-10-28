package com.rpay.v2.data.kiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rpay.v2.service.ServiceUtils

class WatchdogReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!AppStateTracker.isForeground && !ServiceUtils.isServiceRunning(context , ReturnToAppService::class.java)) {
            val serviceIntent =
                Intent(context, ReturnToAppService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}