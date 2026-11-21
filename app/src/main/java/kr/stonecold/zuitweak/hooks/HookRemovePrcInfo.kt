package kr.stonecold.zuitweak.hooks

import android.R.attr.classLoader
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*


@Suppress("unused")
class HookRemovePrcInfo : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.PRC,
            title = LanguageUtil.getString(R.string.hook_remove_prc_info_title),
            description = LanguageUtil.getString(R.string.hook_remove_prc_info_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = arrayOf("PRC")
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.settings")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.settings" -> {
                hookAboutDeviceFragmentUpdateRowPreference(lpparam)
                hookAboutDeviceFragmentUpdateLenovoService(lpparam)
                hookAboutDeviceFragmentCheckIntentTblenovoCenter(lpparam)
            }
        }
    }

    private fun hookAboutDeviceFragmentUpdateRowPreference(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.deviceinfo.aboutphone.AboutDeviceFragment"
        val methodName = "updateRowPreference"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam) {
                try {
                    XposedHelpers.callMethod(param.thisObject, "removePreference", "legal_container")
                    XposedHelpers.callMethod(param.thisObject, "removePreference", "regulatory_information")
                    XposedHelpers.callMethod(param.thisObject, "removePreference", "key_shenqi_serve_line")
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookAboutDeviceFragmentUpdateLenovoService(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.deviceinfo.aboutphone.AboutDeviceFragment"
        val methodName = "updateLenovoService"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam) {
                try {
                    XposedHelpers.callMethod(param.thisObject, "removePreference", "key_lenovo_serve")
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookAboutDeviceFragmentCheckIntentTblenovoCenter(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.deviceinfo.aboutphone.AboutDeviceFragment"
        val methodName = "checkIntentTblenovoCenter"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam) {
                try {
                    XposedHelpers.callMethod(param.thisObject, "removePreference", "feed_back")
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }
}
