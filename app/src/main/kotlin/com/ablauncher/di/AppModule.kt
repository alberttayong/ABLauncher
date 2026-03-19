package com.ablauncher.di

import android.content.Context
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.repository.AppRepository
import com.ablauncher.data.repository.CalendarRepository
import com.ablauncher.data.repository.NewsRepository
import com.ablauncher.data.repository.RecentAppsRepository
import com.ablauncher.data.repository.WallpaperRepository
import com.ablauncher.data.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): PreferencesDataStore = PreferencesDataStore(context)

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context
    ): AppRepository = AppRepository(context)

    @Provides
    @Singleton
    fun provideRecentAppsRepository(
        @ApplicationContext context: Context,
        appRepository: AppRepository
    ): RecentAppsRepository = RecentAppsRepository(context, appRepository)

    @Provides
    @Singleton
    fun provideWallpaperRepository(
        @ApplicationContext context: Context,
        prefsDataStore: PreferencesDataStore
    ): WallpaperRepository = WallpaperRepository(context, prefsDataStore)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideWeatherRepository(
        @ApplicationContext context: Context,
        httpClient: OkHttpClient
    ): WeatherRepository = WeatherRepository(context, httpClient)

    @Provides
    @Singleton
    fun provideCalendarRepository(
        @ApplicationContext context: Context
    ): CalendarRepository = CalendarRepository(context)

    @Provides
    @Singleton
    fun provideNewsRepository(
        httpClient: OkHttpClient
    ): NewsRepository = NewsRepository(httpClient)
}
