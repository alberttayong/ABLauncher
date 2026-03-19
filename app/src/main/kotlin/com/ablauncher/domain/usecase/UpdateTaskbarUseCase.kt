package com.ablauncher.domain.usecase

import com.ablauncher.data.datastore.PreferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateTaskbarUseCase @Inject constructor(
    private val prefsDataStore: PreferencesDataStore
) {
    suspend fun pinApp(packageName: String) {
        val current = prefsDataStore.pinnedTaskbarPackages.first().toMutableList()
        if (packageName !in current) {
            current.add(packageName)
            prefsDataStore.setPinnedApps(current)
        }
    }

    suspend fun unpinApp(packageName: String) {
        val current = prefsDataStore.pinnedTaskbarPackages.first().toMutableList()
        current.remove(packageName)
        prefsDataStore.setPinnedApps(current)
    }

    suspend fun reorderPinnedApps(orderedPackages: List<String>) {
        prefsDataStore.setPinnedApps(orderedPackages)
    }
}
