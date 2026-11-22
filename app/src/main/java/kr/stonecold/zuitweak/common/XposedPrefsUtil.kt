package kr.stonecold.zuitweak.common

import de.robv.android.xposed.XSharedPreferences

@Suppress("unused")
object XposedPrefsUtil {
    private const val TAG = "XposedPrefsUtil"

    private val xsp: XSharedPreferences? by lazy {
        try {
            XSharedPreferences(Constants.APPLICATION_ID, Constants.PREFS_NAME).apply {
                makeWorldReadable()
            }
        } catch (t: Throwable) {
            XposedUtil.xposedDebug(TAG, "Failed to initialize XSharedPreferences: ${t.message}")
            null
        }
    }

    fun isFeatureEnabled(featureName: String, defaultValue: Boolean = false): Boolean {
        return getOptionValue(featureName, defaultValue)
    }

    fun reload() {
        val prefs = xsp ?: return
        try {
            if (!prefs.file.canRead()) {
                prefs.makeWorldReadable()
            }
            if (prefs.file.canRead() && prefs.hasFileChanged()) {
                prefs.reload()
            }
        } catch (t: Throwable) {
            XposedUtil.xposedDebug(TAG, "Failed to reload XSharedPreferences: ${t.message}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getOptionValue(key: String, defaultValue: T): T {
        val prefs = xsp ?: return defaultValue
        reload()

        return try {
            if (!prefs.file.canRead()) {
                return defaultValue
            }
            when (defaultValue) {
                is Boolean -> prefs.getBoolean(key, defaultValue) as T
                is Int -> prefs.getInt(key, defaultValue) as T
                is Float -> prefs.getFloat(key, defaultValue) as T
                is Long -> prefs.getLong(key, defaultValue) as T
                is String -> (prefs.getString(key, defaultValue) ?: defaultValue) as T
                else -> throw IllegalArgumentException("Unsupported type.")
            }
        } catch (t: Throwable) {
            XposedUtil.xposedDebug(TAG, "Failed to read preference '$key': ${t.message}")
            defaultValue
        }
    }
}
