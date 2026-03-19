package com.ablauncher.domain.usecase

import com.ablauncher.data.model.AppInfo
import com.ablauncher.data.repository.RecentAppsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentAppsUseCase @Inject constructor(
    private val recentAppsRepository: RecentAppsRepository
) {
    operator fun invoke(limit: Int = 8): Flow<List<AppInfo>> =
        recentAppsRepository.getRecentApps(limit)
}
