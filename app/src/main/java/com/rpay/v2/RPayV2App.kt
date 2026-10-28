package com.rpay.v2


import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.rpay.v2.data.ActivityProviderImpl
import com.rpay.v2.data.kiosk.AppStateTracker
import com.rpay.v2.data.kiosk.ReturnToAppService
import com.rpay.v2.data.kiosk.WatchdogAlarm
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltAndroidApp
class RPayV2App : Application(){
    private var activityCount = 0

    @Inject
    lateinit var activityProviderImpl: ActivityProviderImpl

    override fun onCreate() {
        super.onCreate()
        registerActivityLifeCycleWatcher()
        WatchdogAlarm.schedule(this)
        startReturnToAppService()
        registerActivityLifecycleCallbacks(
            activityProviderImpl
        )

        // Set a global uncaught exception handler to suppress crash dialog
        handleDefaultUncaughtExceptionHandler()
    }

    private fun handleDefaultUncaughtExceptionHandler(){
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("AppCrash", "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)

            // Optional: restart the app from launcher activity
            try {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Thread.sleep(1000)
            } catch (e: Exception) {
                Log.e("AppCrash", "Error restarting app after crash: ${e.message}", e)
            }

            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }


    private fun registerActivityLifeCycleWatcher(){
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                activityCount++
                AppStateTracker.isForeground = true
                Log.d("LifecycleTracker", "STARTED: ${activity.localClassName} | Count: $activityCount")
            }

            override fun onActivityStopped(activity: Activity) {
                activityCount--
                if (activityCount <= 0) {
                    activityCount = 0
                    AppStateTracker.isForeground = false
                    Log.d("LifecycleTracker", "APP HIDDEN | Count: 0")
                }
            }

            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
            }


            override fun onActivityResumed(p0: Activity) {
            }

            override fun onActivityPaused(p0: Activity) {
            }


            override fun onActivitySaveInstanceState(
                p0: Activity,
                p1: Bundle
            ) {
            }

            override fun onActivityDestroyed(p0: Activity) {
            }
        })
    }

    private fun startReturnToAppService() {
        val intent = Intent(this, ReturnToAppService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}