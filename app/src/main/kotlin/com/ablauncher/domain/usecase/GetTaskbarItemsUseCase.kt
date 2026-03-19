package com.ablauncher.domain.usecase

import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.model.TaskbarItem
import com.ablauncher.data.repository.AppRepository
import com.ablauncher.data.repository.RecentAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

private const val MAX_TASKBAR_ITEMS = 6

class GetTaskbarItemsUseCase @Inject constructor(
    private val prefsDataStore: PreferencesDataStore,
    private val appRepository: AppRepository,
    private val recentAppsRepository: RecentAppsRepository
) {
    operator fun invoke(): Flow<List<TaskbarItem>> =
        combine(
            prefsDataStore.pinnedTaskbarPackages,
            appRepository.apps,
            recentAppsRepository.getRecentApps(MAX_TASKBAR_ITEMS * 2)
        ) { pinned, allApps, recents ->
            val pinnedItems = pinned.mapIndexedNotNull { index, pkg ->
                if (allApps.any { it.packageName == pkg }) {
                    TaskbarItem(packageName = pkg, position = index, isPinned = true)
                } else null
            }

            val remainingSlots = MAX_TASKBAR_ITEMS - pinnedItems.size
            val pinnedPackages = pinnedItems.map { it.packageName }.toSet()

            val recentItems = if (remainingSlots > 0) {
                recents
                    .filterNot { it.packageName in pinnedPackages }
                    .take(remainingSlots)
                    .mapIndexed { index, app ->
                        TaskbarItem(
                            packageName = app.packageName,
                            position = pinnedItems.size + index,
                            isPinned = false
                        )
                    }
            } else emptyList()

            (pinnedItems + recentItems).take(MAX_TASKBAR_ITEMS)
        }
}
