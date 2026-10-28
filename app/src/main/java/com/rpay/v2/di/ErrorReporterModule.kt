package com.rpay.v2.di

import com.rpay.v2.data.error.RemoteErrorReporter
import com.rpay.v2.domain.error.ErrorReporter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ErrorReporterModule {

    @Binds
    @Singleton
    abstract fun bindErrorReporter(
        implementation: RemoteErrorReporter
    ): ErrorReporter

    companion object {

        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(): CoroutineScope {
            return CoroutineScope(
                SupervisorJob()
            )
        }

        @Provides
        @Singleton
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher {
            return Dispatchers.IO
        }
    }
}