package com.ablauncher.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

object PackageUtils {
    fun getAppIcon(context: Context, packageName: String): Drawable? =
        runCatching {
            context.packageManager.getApplicationIcon(packageName)
        }.getOrNull()

    fun getAppLabel(context: Context, packageName: String): String? =
        runCatching {
            val appInfo = context.packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
            )
            context.packageManager.getApplicationLabel(appInfo).toString()
        }.getOrNull()

    fun isPackageInstalled(context: Context, packageName: String): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        }.getOrDefault(false)
}
