package com.ablauncher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.ablauncher.data.repository.AppRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ABLauncherApp : Application() {

    @Inject
    lateinit var appRepository: AppRepository

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_REPLACED -> {
                    appRepository.refreshApps()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerPackageChangeReceiver()
    }

    private fun registerPackageChangeReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(packageChangeReceiver, filter)
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            unregisterReceiver(packageChangeReceiver)
        } catch (_: IllegalArgumentException) { /* not registered */ }
    }
}
