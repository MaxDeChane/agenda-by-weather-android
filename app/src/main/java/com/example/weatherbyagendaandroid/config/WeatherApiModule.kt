package com.example.weatherbyagendaandroid.config

import android.content.Context
import com.example.weatherbyagendaandroid.cache.HttpCacheStore
import com.example.weatherbyagendaandroid.dao.WeatherApiDao
import com.example.weatherbyagendaandroid.dao.adapter.LocalDateTimeJsonAdapter
import com.example.weatherbyagendaandroid.dao.adapter.OffsetDateTimeJsonAdapter
import com.example.weatherbyagendaandroid.interceptor.HttpCacheInterceptor
import com.example.weatherbyagendaandroid.presentation.domain.DateTimeFilter
import com.example.weatherbyagendaandroid.presentation.domain.TemperatureFilter
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilter
import com.example.weatherbyagendaandroid.presentation.domain.WeatherKeywordFilter
import com.example.weatherbyagendaandroid.presentation.domain.WindFilter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherApiModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpCacheInterceptor(HttpCacheStore(context)))
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi() = Moshi.Builder()
        .add(OffsetDateTimeJsonAdapter())
        .add(LocalDateTimeJsonAdapter())
        .add(PolymorphicJsonAdapterFactory.of(WeatherFilter::class.java, "type")
            .withSubtype(DateTimeFilter::class.java, DateTimeFilter::class.simpleName)
            .withSubtype(TemperatureFilter::class.java, TemperatureFilter::class.simpleName)
            .withSubtype(WindFilter::class.java, WindFilter::class.simpleName)
            .withSubtype(WeatherKeywordFilter::class.java, WeatherKeywordFilter::class.simpleName)
        )
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    @Named("weatherApiRetrofit")
    fun provideWeatherApiRetrofit(moshi: Moshi, okHttpClient: OkHttpClient) = Retrofit.Builder()
        .baseUrl("https://api.weather.gov/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideWeatherApiDao(@Named("weatherApiRetrofit") weatherApiRetrofit: Retrofit) =
        weatherApiRetrofit.create(WeatherApiDao::class.java)
}