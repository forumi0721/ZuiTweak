package kr.stonecold.zuitweak.hooks

import android.R.attr.classLoader
import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*
import java.util.Locale


@Suppress("unused")
class HookAddKoreanLanguageSettings : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.PRC,
            title = LanguageUtil.getString(R.string.hook_add_korean_language_settings_title),
            description = LanguageUtil.getString(R.string.hook_add_korean_language_settings_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = arrayOf("PRC")
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.settings")
    override val hookTargetPackageOptional: Array<String> = emptyArray()
    override val hookTargetPackageRes: Array<String> = arrayOf("com.android.settings")

    override var updateRes: ((resparam: XC_InitPackageResources.InitPackageResourcesParam) -> Unit)? = { resparam ->
        if (resparam.packageName == "com.android.settings") {
            val language = Locale.getDefault().language

            if (language == "ko") {
                val resKey = "sound"
                val resVal = "소리"
                resparam.res.setReplacement(resparam.packageName, "string", resKey, resVal)

                XposedUtil.xposedDebug(tag, "Successfully replaced ${resparam.packageName}.$resKey: $resVal")
            }
        }
    }

    override fun isEnabledCustomCheck(): Boolean {
        val zuiKrPatchEnabled = Util.getProperty("ro.stonecold.krpatch.enabled", "false").uppercase()
        val testModeEnabled = Util.getProperty("persist.sys.lenovo.is_test_mode", "false").uppercase()
        return zuiKrPatchEnabled == "TRUE" || testModeEnabled == "TRUE"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.settings" -> {
                hookLocaleListEditorGetUserLocaleList(lpparam)
                hookLocaleListEditorGetAllLocaleList(lpparam)
                hookLenovoUtilsGetChangedName(lpparam)
            }
        }
    }

    private fun hookLocaleListEditorGetUserLocaleList(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.localepicker.LocaleListEditor"
        val methodName = "getUserLocaleList"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val originalList = param.result as List<*>
                    val clazz = XposedHelpers.findClass("com.android.internal.app.LocaleStore", lpparam.classLoader)
                    val koKRLocaleInfo = XposedHelpers.callStaticMethod(clazz, "getLocaleInfo", Locale.forLanguageTag("ko-KR"))

                    if (!originalList.contains(koKRLocaleInfo)) {
                        val newList = ArrayList(originalList)
                        newList.add(koKRLocaleInfo)
                        param.result = newList
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookLocaleListEditorGetAllLocaleList(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.localepicker.LocaleListEditor"
        val methodName = "getAllLocaleList"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val originalList = param.result as List<*>
                    val clazz = XposedHelpers.findClass("com.android.internal.app.LocaleStore", lpparam.classLoader)
                    val koKRLocaleInfo = XposedHelpers.callStaticMethod(clazz, "getLocaleInfo", Locale.forLanguageTag("ko-KR"))

                    if (!originalList.contains(koKRLocaleInfo)) {
                        val newList = ArrayList(originalList)
                        newList.add(koKRLocaleInfo)
                        param.result = newList
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookLenovoUtilsGetChangedName(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.common.utils.LenovoUtils"
        val methodName = "getChangedName"
        val parameterTypes = arrayOf<Any>(
            "com.android.internal.app.LocaleStore\$LocaleInfo",
            Context::class.java
        )
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val result = param.result as String
                    val localeInfo = param.args[0]
                    val localeInfoClass = localeInfo::class.java
                    val getIdMethod = localeInfoClass.getMethod("getId")
                    val id = getIdMethod.invoke(localeInfo) as String

                    if (result.isEmpty() && id == "ko-KR") {
                        param.result = "한국어"
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }
}
