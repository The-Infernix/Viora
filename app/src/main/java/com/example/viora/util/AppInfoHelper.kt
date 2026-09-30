package com.example.viora.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

object AppInfoHelper {
    data class AppInfo(
        val packageName: String,
        val name: String,
        val category: String
    )

    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val name = resolveInfo.loadLabel(pm).toString()
                val appInfo = resolveInfo.activityInfo.applicationInfo
                AppInfo(
                    packageName = packageName,
                    name = name,
                    category = getCategory(appInfo)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name }
    }

    private fun getCategory(appInfo: ApplicationInfo): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> "SOCIAL"
                ApplicationInfo.CATEGORY_VIDEO -> "ENTERTAINMENT"
                ApplicationInfo.CATEGORY_AUDIO -> "ENTERTAINMENT"
                ApplicationInfo.CATEGORY_GAME -> "GAMES"
                ApplicationInfo.CATEGORY_NEWS -> "NEWS"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> "PRODUCTIVITY"
                else -> "OTHER"
            }
        } else "OTHER"
    }

    fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            appInfo.loadLabel(pm).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName.substringAfterLast('.')
        }
    }
}
