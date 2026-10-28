package com.rpay.v2.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationProvider @Inject constructor(@ApplicationContext context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    var lastLatitude: Double? = null
        private set
    var lastLongitude: Double? = null
        private set

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation() {
        try {
            val location = suspendCancellableCoroutine { cont ->
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        cont.resume(loc)
                    }.addOnFailureListener {
                        cont.resume(null)
                    }
            }

            if (location != null) {
                lastLatitude = location.latitude
                lastLongitude = location.longitude
                Log.d("LocationProvider", "Location: $lastLatitude, $lastLongitude")
            } else {
                Log.d("LocationProvider", "Location not available")
            }
        } catch (e: Exception) {
            Log.e("LocationProvider", "Error fetching location", e)
        }
    }
}