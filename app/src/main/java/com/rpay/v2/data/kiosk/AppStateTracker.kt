package com.rpay.v2.data.kiosk

import androidx.lifecycle.Lifecycle
object AppStateTracker {

    @Volatile
    var isForeground: Boolean = false

}