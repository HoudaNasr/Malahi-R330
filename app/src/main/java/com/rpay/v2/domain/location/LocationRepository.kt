package com.rpay.v2.domain.location


interface LocationRepository {
    suspend fun getCurrentLocation(): GeoLocation?
}