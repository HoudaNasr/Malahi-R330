package com.rpay.v2.di

import com.rpay.v2.data.ActivityProviderImpl
import com.rpay.v2.domain.ActivityProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityProviderModule {

    @Binds
    abstract fun bindActivityProvider(
        impl: ActivityProviderImpl
    ): ActivityProvider
}