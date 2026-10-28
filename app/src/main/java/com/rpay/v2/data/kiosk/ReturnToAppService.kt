package com.rpay.v2.data.kiosk

import android.app.Service
import android.content.Intent
import android.util.Log
import com.rpay.cm30.v2.core.utils.Helper.SERVICE_ID
import com.rpay.v2.data.createNotification
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.presentation.home.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ReturnToAppService : Service() {
    private var lastReopenTime = 0L
    private val REOPEN_COOLDOWN_MS = 90_000L
    private var isLoopRunning = false
    private val TAG = "ReturnToAppService"

    private val serviceDelayMs: Long = 90_000

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)


    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started (startId=$startId)")

        startForeground(
            SERVICE_ID,
            createNotification(context = this)
        )

        Log.i(TAG, "Foreground notification started")

        if (!isLoopRunning) {
            isLoopRunning = true

            serviceScope.launch {
                try {
                    while (isActive) {
                        val delayMs = serviceDelayMs
                        delay(delayMs)
                        if (canBringToFront()) {
                            Log.w(TAG, "Reopening app")
                            reopenApp()
                        }
                    }
                } finally {
                    isLoopRunning = false
                }
            }
        }

        return START_STICKY
    }

    private fun reopenApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.EXTRA_FROM_WATCHDOG, true)
        }
        try {
            Log.e(TAG, "startActivity")
            startActivity(intent)
            Log.e(TAG, "startActivity2")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reopen app", e)
        }
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "Service destroyed")
        serviceScope.cancel()
    }

    private fun canBringToFront(): Boolean {
        val now = System.currentTimeMillis()

        if (AppStateTracker.isForeground) {
            Log.d(TAG, "canBringToFront=false (already foreground)")
            return false
        }

        if (now - lastReopenTime < REOPEN_COOLDOWN_MS) {
            Log.d(TAG, "canBringToFront=false (cooldown active)")
            return false
        }

        lastReopenTime = now
        return true
    }
}