package com.rpay.v2.di

import com.rpay.v2.core.network.RestfulApis
import com.rpay.v2.features.semnox.data.network.SemnoxBaseUrlInterceptor
import com.rpay.v2.features.semnox.data.network.SemnoxRestfulApi
import com.rpay.cm30.v2.core.utils.Helper
import com.rpay.v2.BuildConfig
import com.rpay.v2.data.repository.NetworkRepositoryImpl
import com.rpay.v2.domain.repository.NetworkRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providekeyInteceptor(): Interceptor {
        return Interceptor { chain ->
            val original: Request = chain.request()
            val request: Request = original.newBuilder()
                .header("X-Api-Key", BuildConfig.API_KEY)
                .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        keyInterceptor: Interceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // connection timeout
            .readTimeout(60, TimeUnit.SECONDS)    // read timeout
            .writeTimeout(60, TimeUnit.SECONDS)   // write timeout
            .addInterceptor(keyInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }



    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionApi(retrofit: Retrofit): RestfulApis {
        return retrofit.create(RestfulApis::class.java)
    }

    @Provides
    @Singleton
    @Named("SemnoxOkHttp")
    fun provideSemnoxOkHttp(
        interceptor: SemnoxBaseUrlInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(httpLoggingInterceptor)
            .build()

    @Provides
    @Singleton
    @Named("SemnoxRetrofit")
    fun provideSemnoxRetrofit(
        @Named("SemnoxOkHttp") client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://localhost/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideSemnoxApi(
        @Named("SemnoxRetrofit") retrofit: Retrofit): SemnoxRestfulApi {
        return retrofit.create(SemnoxRestfulApi::class.java)
    }

}
