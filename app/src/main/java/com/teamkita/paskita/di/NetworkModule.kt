package com.teamkita.paskita.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.teamkita.paskita.BuildConfig
import com.teamkita.paskita.data.network.ApiRajaOngkir
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    fun providesBaseUrl(): String {
        return BuildConfig.BASE_URL
    }

    fun providesGson(): Gson {
        return GsonBuilder().create()
    }

    fun providesLoggingInterceptor(): HttpLoggingInterceptor {
        return if (BuildConfig.DEBUG) HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        else HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
    }

    fun providesOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(logging).build()
    }

    fun providesConvertFactory(gson: Gson): GsonConverterFactory {
        return GsonConverterFactory.create(gson)
    }

    fun providesRxJava2CallAdapterFactory(): RxJava2CallAdapterFactory {
        return RxJava2CallAdapterFactory.create()
    }

    fun providesRetrofit(
        baseURL: String,
        converterFactory: GsonConverterFactory,
        rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
        okHttpClient: OkHttpClient
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(rxJava2CallAdapterFactory)
            .client(okHttpClient)
            .build()
    }

    fun providesAPIService(): ApiRajaOngkir {
        val baseURL = providesBaseUrl()
        val gson = providesGson()
        val loggingInterceptor = providesLoggingInterceptor()
        val okHttpClient = providesOkHttpClient(loggingInterceptor)
        val converterFactory = providesConvertFactory(gson)
        val rxJava2CallAdapterFactory = providesRxJava2CallAdapterFactory()

        val retrofit = providesRetrofit(baseURL, converterFactory, rxJava2CallAdapterFactory, okHttpClient)
        return retrofit.create(ApiRajaOngkir::class.java)
    }
}
