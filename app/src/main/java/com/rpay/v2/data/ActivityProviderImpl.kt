package com.rpay.v2.data

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.rpay.v2.domain.ActivityProvider
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityProviderImpl @Inject constructor() :
    ActivityProvider,
    Application.ActivityLifecycleCallbacks {

    @Volatile
    private var currentActivityRef: WeakReference<Activity>? = null

    override val currentActivity: Activity?
        get() = currentActivityRef?.get()

    override fun onActivityResumed(activity: Activity) {
        currentActivityRef = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity === activity) {
            currentActivityRef?.clear()
            currentActivityRef = null
        }
    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}