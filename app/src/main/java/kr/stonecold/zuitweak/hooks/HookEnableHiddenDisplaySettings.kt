package kr.stonecold.zuitweak.hooks

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

@Suppress("unused")
class HookEnableHiddenDisplaySettings : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.COMMON,
            title = LanguageUtil.getString(R.string.hook_enable_hidden_display_settings_title),
            description = LanguageUtil.getString(R.string.hook_enable_hidden_display_settings_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.settings")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.settings" -> {
                hookAutoColorPreferenceControllerGetAvailabilityStatus(lpparam)
                hookGameEnhancePreferenceControllerGetAvailabilityStatus(lpparam)
                hookVideoColorEnhancePreferenceControllerGetAvailabilityStatus(lpparam)
                hookVideoQualityEnhancePreferenceControllerGetAvailabilityStatus(lpparam)
            }
        }
    }

    private fun hookAutoColorPreferenceControllerGetAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.display.AutoColorPreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "param.result: ${param.result}")
                    param.result = 0
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookGameEnhancePreferenceControllerGetAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.display.GameEnhancePreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "param.result: ${param.result}")
                    param.result = 0
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookVideoColorEnhancePreferenceControllerGetAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.display.VideoColorEnhancePreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "param.result: ${param.result}")
                    param.result = 0
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookVideoQualityEnhancePreferenceControllerGetAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.display.VideoQualityEnhancePreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "param.result: ${param.result}")
                    param.result = 0
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

}
