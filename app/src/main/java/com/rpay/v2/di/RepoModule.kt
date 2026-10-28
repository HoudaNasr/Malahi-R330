package com.rpay.v2.di

import com.rpay.v2.data.location.LocationRepositoryImpl
import com.rpay.v2.data.migration.LegacyPreferencesRepositoryImpl
import com.rpay.v2.data.repository.MachineRepositoryImpl
import com.rpay.v2.data.repository.NearPayRepositoryImpl
import com.rpay.v2.data.repository.NetworkRepositoryImpl
import com.rpay.v2.domain.location.LocationRepository
import com.rpay.v2.domain.repository.LegacyPreferencesRepository
import com.rpay.v2.domain.repository.MachineRepository
import com.rpay.v2.domain.repository.NearPayRepo
import com.rpay.v2.domain.repository.NetworkRepository
import com.rpay.v2.features.pay.data.PaymentRepositoryImp
import com.rpay.v2.features.pay.data.QrScanningRepositoryImp
import com.rpay.v2.features.pay.domain.PaymentRepository
import com.rpay.v2.features.pay.domain.QrScanningRepository
import com.rpay.v2.features.systemselector.data.SettingsRepoImp
import com.rpay.v2.features.systemselector.domain.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        impl: PaymentRepositoryImp
    ): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindMachineRepository(
        impl: MachineRepositoryImpl
    ): MachineRepository


    @Binds
    @Singleton
    abstract fun bindNearPayRepository(
        impl: NearPayRepositoryImpl
    ): NearPayRepo


    @Binds
    @Singleton
    abstract fun bindQrRepository(
        impl: QrScanningRepositoryImp
    ): QrScanningRepository

    @Binds
    @Singleton
    abstract fun bindSystemsTypeRepo(
        impl: SettingsRepoImp
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindLegacyPreferencesRepository(
        impl: LegacyPreferencesRepositoryImpl
    ): LegacyPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(
        repository: NetworkRepositoryImpl
    ): NetworkRepository

}