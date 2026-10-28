package com.rpay.v2.di


import com.rpay.v2.data.DeviceInfoProviderImpl
import com.rpay.v2.data.FlavorConfigImpl
import com.rpay.v2.data.KioskManagerImplementation
import com.rpay.v2.data.location.LocationRepositoryImpl
import com.rpay.v2.domain.config.UiConfigProvider
import com.rpay.v2.domain.hardware.DeviceInfoProvider
import com.rpay.v2.domain.hardware.KioskManager
import com.rpay.v2.domain.location.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class R330Binder {

    @Binds
    abstract fun bindDeviceInfoProvider(
        impl: DeviceInfoProviderImpl
    ): DeviceInfoProvider

    @Binds
    abstract fun bindlocationProvider(
        impl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindKioskManager(
        impl: KioskManagerImplementation
    ): KioskManager

    @Binds
    abstract fun bindUiConfigProvider(
        impl: FlavorConfigImpl
    ): UiConfigProvider

}
