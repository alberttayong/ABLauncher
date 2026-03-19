package com.ablauncher.ui.taskbar

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.model.AppInfo
import com.ablauncher.data.model.TaskbarItem
import com.ablauncher.data.repository.AppRepository
import com.ablauncher.domain.usecase.GetTaskbarItemsUseCase
import com.ablauncher.domain.usecase.UpdateTaskbarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskbarViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getTaskbarItemsUseCase: GetTaskbarItemsUseCase,
    private val updateTaskbarUseCase: UpdateTaskbarUseCase,
    private val appRepository: AppRepository
) : ViewModel() {

    val taskbarItems: StateFlow<List<TaskbarItem>> = getTaskbarItemsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()

    private val _showEditSheet = MutableStateFlow(false)
    val showEditSheet: StateFlow<Boolean> = _showEditSheet.asStateFlow()

    fun onAppLongPress(packageName: String) {
        val app = appRepository.apps.value.find { it.packageName == packageName }
        val taskbarItem = taskbarItems.value.find { it.packageName == packageName }
        if (app != null) {
            _selectedApp.value = app.copy(isInTaskbar = taskbarItem?.isPinned == true)
            _showEditSheet.value = true
        }
    }

    fun dismissEditSheet() {
        _showEditSheet.value = false
        _selectedApp.value = null
    }

    fun pinApp(packageName: String) {
        viewModelScope.launch {
            updateTaskbarUseCase.pinApp(packageName)
        }
        dismissEditSheet()
    }

    fun unpinApp(packageName: String) {
        viewModelScope.launch {
            updateTaskbarUseCase.unpinApp(packageName)
        }
        dismissEditSheet()
    }

    fun launchApp(packageName: String) {
        val intent = appRepository.getLaunchIntent(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }

    fun getAppInfo(packageName: String): AppInfo? =
        appRepository.apps.value.find { it.packageName == packageName }
}
