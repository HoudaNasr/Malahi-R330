package com.rpay.v2.domain

import android.app.Activity

interface ActivityProvider {
    val currentActivity: Activity?
}