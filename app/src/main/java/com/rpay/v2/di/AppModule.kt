package com.rpay.v2.di

import android.content.Context
import com.google.gson.Gson
import com.rpay.v2.data.AndroidAppInfoProvider
import com.rpay.v2.data.datasource.local.AppPreferencesDataSource
import com.rpay.v2.domain.AppInfoProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppContext(
        @ApplicationContext context: Context
    ): Context {
        return context
    }

    @Provides
    @Singleton
    fun providePrefs(@ApplicationContext context: Context): AppPreferencesDataSource {
        return AppPreferencesDataSource(context)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class AppModule {
        @Binds
        @Singleton
        abstract fun bindAppInfoProvider(
            impl: AndroidAppInfoProvider
        ): AppInfoProvider
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object GsonModule {
        @Provides
        @Singleton
        fun provideGson(): Gson {
            return Gson()
        }
    }
}