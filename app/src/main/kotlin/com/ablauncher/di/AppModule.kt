package com.ablauncher.di

import android.content.Context
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.repository.AppRepository
import com.ablauncher.data.repository.RecentAppsRepository
import com.ablauncher.data.repository.WallpaperRepository
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
}
