package com.ablauncher.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.ablauncher.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentAppsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appRepository: AppRepository
) {
    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    fun hasUsagePermission(): Boolean {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000L
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )
        return stats != null && stats.isNotEmpty()
    }

    fun requestUsagePermission() {
        context.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /** Returns a Flow of the [limit] most recently used apps, sorted newest-first */
    fun getRecentApps(limit: Int = 8): Flow<List<AppInfo>> = flow {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(24)
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )
        val recentPackages = stats
            .filter { it.packageName != context.packageName }
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.lastTimeUsed }
            .take(limit)

        val allApps = appRepository.apps.value
        val result = recentPackages.mapNotNull { stat ->
            allApps.find { it.packageName == stat.packageName }
                ?.copy(lastUsedTimestamp = stat.lastTimeUsed)
        }
        emit(result)
    }
}
