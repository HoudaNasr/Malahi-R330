package com.rpay.v2.data.location

import com.rpay.v2.data.LocationProvider
import com.rpay.v2.domain.location.GeoLocation
import com.rpay.v2.domain.location.LocationRepository
import javax.inject.Inject


class LocationRepositoryImpl @Inject constructor(
    private val provider: LocationProvider
) : LocationRepository {

    override suspend fun getCurrentLocation(): GeoLocation? {
        provider.fetchCurrentLocation()

        val lat = provider.lastLatitude
        val lng = provider.lastLongitude

        return if (lat != null && lng != null) {
            GeoLocation(lat, lng)
        } else {
            null
        }
    }
}
