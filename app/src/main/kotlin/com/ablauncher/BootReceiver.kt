package com.ablauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives BOOT_COMPLETED so the launcher is ready immediately after device restart.
 * The system will automatically bring the launcher to foreground via CATEGORY_HOME,
 * but this receiver ensures any deferred initialisation can be triggered if needed.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Launcher will be invoked automatically via CATEGORY_HOME intent filter.
            // No additional action needed here.
        }
    }
}
