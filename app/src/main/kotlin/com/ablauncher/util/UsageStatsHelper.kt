package com.ablauncher.util

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.concurrent.TimeUnit

object UsageStatsHelper {
    fun queryRecentPackages(
        context: Context,
        withinHours: Int = 24,
        limit: Int = 8
    ): List<Pair<String, Long>> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(withinHours.toLong())
        return usm
            .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.lastTimeUsed }
            .take(limit)
            .map { it.packageName to it.lastTimeUsed }
    }
}
