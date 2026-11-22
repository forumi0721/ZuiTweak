package kr.stonecold.zuitweak.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import kr.stonecold.zuitweak.HookOption

@Suppress("unused")
object SharedPrefsUtil {
    private const val TAG = "SharedPrefsUtil"
    private lateinit var sharedPreferences: SharedPreferences

    var isInitialized: Boolean = false
        private set

    @SuppressLint("WorldReadableFiles")
    fun init(context: Context) {
        if (isInitialized && ::sharedPreferences.isInitialized) {
            return
        }

        try {
            val targetContext = context.applicationContext ?: context
            @Suppress("DEPRECATION")
            sharedPreferences = targetContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_WORLD_READABLE)
            isInitialized = true
        } catch (e: SecurityException) {
            Log.d(TAG, "Module disabled or xposedsharedprefs inactive: ${e.message}")
            isInitialized = false
        } catch (e: Exception) {
            Log.d(TAG, "Module disabled due to an exception: ${e.message}")
            isInitialized = false
        }
    }

    fun syncOptions(options: List<HookOption>, isSupportedDevice: Boolean) {
        if (!isInitialized) {
            return
        }

        sharedPreferences.edit(commit = true) {
            for (option in options) {
                if (!isSupportedDevice || !option.isEnabledOption) {
                    if (sharedPreferences.contains(option.key)) {
                        remove(option.key)
                    }
                } else if (!sharedPreferences.contains(option.key)) {
                    putBoolean(option.key, option.defaultEnabled)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getOptionValue(key: String, defaultValue: T): T {
        if (!isInitialized) {
            throw UninitializedPropertyAccessException("SharedPrefsUtil has not been initialized. Call init(context) first.")
        }

        return when (defaultValue) {
            is Boolean -> sharedPreferences.getBoolean(key, defaultValue) as T
            is Int -> sharedPreferences.getInt(key, defaultValue) as T
            is Float -> sharedPreferences.getFloat(key, defaultValue) as T
            is Long -> sharedPreferences.getLong(key, defaultValue) as T
            is String -> (sharedPreferences.getString(key, defaultValue) ?: defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported type.")
        }
    }

    fun <T> setOptionValue(key: String, value: T) {
        if (!isInitialized) {
            throw UninitializedPropertyAccessException("SharedPrefsUtil has not been initialized. Call init(context) first.")
        }

        sharedPreferences.edit(commit = true) {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                else -> throw IllegalArgumentException("Unsupported type.")
            }
        }
    }

    fun deleteOptionValue(key: String) {
        if (!isInitialized) {
            throw UninitializedPropertyAccessException("SharedPrefsUtil has not been initialized. Call init(context) first.")
        }

        if (sharedPreferences.contains(key)) {
            sharedPreferences.edit(commit = true) {
                remove(key)
            }
        }
    }

    fun getAllOptions(): Map<String, *> {
        if (!isInitialized) {
            throw UninitializedPropertyAccessException("SharedPrefsUtil has not been initialized. Call init(context) first.")
        }

        return sharedPreferences.all
    }

    fun clearAllOptions() {
        if (!isInitialized) {
            throw UninitializedPropertyAccessException("SharedPrefsUtil has not been initialized. Call init(context) first.")
        }

        sharedPreferences.edit(commit = true) {
            clear()
        }
    }
}
