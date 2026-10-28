package com.rpay.v2.di

import android.content.Context
import com.rpay.v2.core.network.RestfulApis
import com.rpay.v2.data.datasource.local.TransactionDao
import com.rpay.v2.data.repository.TransactionRepositoryImpl
import com.rpay.v2.data.transactions.domain.CreateTransactionUseCase
import com.rpay.v2.domain.transactions.TransactionRepository
import com.rpay.cm30.v2.domain.usecase.GetTransactionsUseCase
import com.rpay.v2.data.datasource.local.AppPreferencesDataSource
import com.rpay.v2.data.kiosk.UsbAccessoryTransportData
import com.rpay.v2.domain.kiosk.TransportData
import com.rpay.v2.domain.nearpay.GetNearPayTerminalsUseCase
import com.rpay.v2.domain.nearpay.GetNearPayTokenUseCase
import com.rpay.v2.domain.repository.NearPayRepo
import com.rpay.v2.domain.request_factory.NearPayTerminalsRequestFactory
import com.rpay.v2.domain.request_factory.NearPayTokenRequestFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTransactionRepository(
        dao: TransactionDao
    ): TransactionRepository = TransactionRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideGetTransactionsUseCase(
        repo: TransactionRepository
    ) = GetTransactionsUseCase(repo)



    @Provides
    @Singleton
    fun provideGetNearPayTerminalsUseCase(
        repo: NearPayRepo,
        requestFactory: NearPayTerminalsRequestFactory
    ) = GetNearPayTerminalsUseCase(
        repository = repo,
        requestFactory = requestFactory
    )

    @Provides
    @Singleton
    fun provideGetNearPayTokenUseCase(
        repo: NearPayRepo,
        requestFactory: NearPayTokenRequestFactory
    ) = GetNearPayTokenUseCase(
        repository = repo,
        requestFactory = requestFactory
    )

    @Provides
    @Singleton
    fun provideNearPayTerminalsRequestFactory(
        prefs: AppPreferencesDataSource
    ) = NearPayTerminalsRequestFactory(prefs)

    @Provides
    @Singleton
    fun provideNearPayTokenRequestFactory(
        prefs: AppPreferencesDataSource
    ) = NearPayTokenRequestFactory(prefs)

    @Provides
    @Singleton
    fun provideTransportData(
        @ApplicationContext context: Context
    ): TransportData = UsbAccessoryTransportData(
        context = context
    )
}
