package kr.stonecold.zuitweak.common

import android.content.Context
import androidx.core.content.edit
import kr.stonecold.zuitweak.ZuiTweakApplication

@Suppress("unused")
object LanguageUtil {
    private const val PREF_LANGUAGE_KEY = "pref_language"

    fun setLanguage(language: String) {
        val context = ZuiTweakApplication.appContext
        val languagePreferences = context.getSharedPreferences(Constants.LANGAUGE_PREFS_NAME, Context.MODE_PRIVATE)

        languagePreferences.edit { putString(PREF_LANGUAGE_KEY, language) }
    }

    fun getLanguage(): String? {
        val context = ZuiTweakApplication.appContext
        val languagePreferences = context.getSharedPreferences(Constants.LANGAUGE_PREFS_NAME, Context.MODE_PRIVATE)

        return languagePreferences.getString(PREF_LANGUAGE_KEY, "ko")
    }

    fun getString(id: Int): String {
        var result: String
        try {
            val context = ZuiTweakApplication.appContext
            result = context.getString(id)
        } catch (e: Exception) {
            result = id.toString()
        }

        return result
    }
}
