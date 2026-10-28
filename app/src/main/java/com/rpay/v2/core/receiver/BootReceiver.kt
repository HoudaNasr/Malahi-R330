package com.rpay.v2.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.presentation.launcher.presentation.LauncherActivity

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            FileLogger.log(context = context , "BootBroadcastReceiver " , "QUICKBOOT_POWERON")
            val launchIntent = Intent(context, LauncherActivity::class.java).apply {
                // FLAG_ACTIVITY_NEW_TASK is mandatory when starting from outside an activity
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Ensures that if the activity is already running, it's brought to the front
                // instead of initializing a brand new stack
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            try {
                FileLogger.log(context = context , "BootBroadcastReceiver " , "try")

                context.startActivity(launchIntent)
            } catch (e: Exception) {
                FileLogger.log(context = context , "BootBroadcastReceiver " , e.message.toString())
                e.printStackTrace()
                // Fallback: If WizarPOS blocks background activity launches,
                // you would need to start a foreground service here instead, which then opens the UI.
            }
        }
    }
}