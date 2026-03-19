package com.ablauncher.domain.usecase

import com.ablauncher.data.model.AppInfo
import com.ablauncher.data.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    operator fun invoke(query: String = ""): Flow<List<AppInfo>> =
        appRepository.apps.map { apps ->
            if (query.isBlank()) apps
            else apps.filter { it.appName.contains(query, ignoreCase = true) }
        }
}
