package com.ablauncher.ui.apptray

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.model.AppInfo
import com.ablauncher.data.repository.AppRepository
import com.ablauncher.domain.usecase.GetInstalledAppsUseCase
import com.ablauncher.domain.usecase.UpdateTaskbarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppTrayViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val updateTaskbarUseCase: UpdateTaskbarUseCase,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredApps: StateFlow<List<AppInfo>> = combine(
        getInstalledAppsUseCase(),
        _searchQuery
    ) { apps, query ->
        if (query.isBlank()) apps
        else apps.filter { it.appName.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()

    private val _showAppOptions = MutableStateFlow(false)
    val showAppOptions: StateFlow<Boolean> = _showAppOptions.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onAppLongPress(packageName: String) {
        _selectedApp.value = appRepository.apps.value.find { it.packageName == packageName }
        _showAppOptions.value = true
    }

    fun dismissAppOptions() {
        _showAppOptions.value = false
        _selectedApp.value = null
    }

    fun launchApp(packageName: String) {
        val intent = appRepository.getLaunchIntent(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent?.let { context.startActivity(it) }
    }

    fun pinToTaskbar(packageName: String) {
        viewModelScope.launch {
            updateTaskbarUseCase.pinApp(packageName)
        }
        dismissAppOptions()
    }

    fun unpinFromTaskbar(packageName: String) {
        viewModelScope.launch {
            updateTaskbarUseCase.unpinApp(packageName)
        }
        dismissAppOptions()
    }
}
