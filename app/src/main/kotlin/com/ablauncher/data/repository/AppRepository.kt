package com.ablauncher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.ablauncher.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    init {
        refreshApps()
    }

    fun refreshApps() {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList: List<ResolveInfo> = pm.queryIntentActivities(
            launcherIntent,
            PackageManager.GET_META_DATA
        )
        val appList = resolveInfoList
            .filterNot { it.activityInfo.packageName == context.packageName }
            .map { ri ->
                AppInfo(
                    packageName = ri.activityInfo.packageName,
                    appName = ri.loadLabel(pm).toString(),
                    icon = ri.loadIcon(pm)
                )
            }
            .sortedBy { it.appName.lowercase() }
        _apps.value = appList
    }

    fun getLaunchIntent(packageName: String): Intent? =
        context.packageManager.getLaunchIntentForPackage(packageName)
}
