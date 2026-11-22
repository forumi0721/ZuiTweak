package kr.stonecold.zuitweak.common

import kr.stonecold.zuitweak.BuildConfig

@Suppress("unused")
object Constants {
    const val APPLICATION_ID = BuildConfig.APPLICATION_ID
    const val PREFS_NAME = "feature_config"
    const val LANGAUGE_PREFS_NAME = "langauge_config"

    val deviceModel: String by lazy {
        return@lazy Util.getModel()
    }

    val deviceRegion: String by lazy {
        return@lazy Util.getProperty("ro.config.lgsi.region", "UNKNOWN").uppercase()
    }

    val deviceVersion: String by lazy {
        return@lazy Util.getProperty("ro.com.zui.version", "UNKNOWN").uppercase()
    }
}
