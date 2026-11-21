package kr.stonecold.zuitweak.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

@Suppress("unused")
class HookEnableOneVisionSmartSplit : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.ROW,
            title = LanguageUtil.getString(R.string.hook_enable_one_vision_smart_split_title),
            description = LanguageUtil.getString(R.string.hook_enable_one_vision_smart_split_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = arrayOf("ROW")
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.settings")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    val matchCriteria = arrayOf(
        "com.lenovo.settings.onevision.OneVisionSettingsFragment" to emptyArray<String>(),
        "com.lenovo.settings.onevision.EmbeddingAppFragment" to emptyArray<String>(),
        "com.lenovo.settings.onevision.EmbeddingAppFragment\$RecyclerAdapter" to emptyArray<String>(),
        "com.lenovo.settings.onevision.EmbeddingAppFragment\$SettingsObserver" to emptyArray<String>(),
    )

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.settings" -> {
                hookLenovoUtilsIsPrcVersion(lpparam)
                hookLenovoUtilsIsRowVersion(lpparam)
            }
        }
    }

    private fun hookLenovoUtilsIsPrcVersion(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.common.utils.LenovoUtils"
        val methodName = "isPrcVersion"
        val parameterTypes = emptyArray<Class<*>>()
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val stackTrace = Thread.currentThread().stackTrace
                    val calledFromElement = stackTrace.find { element ->
                        matchCriteria.any { (className, methods) ->
                            element.className == className && !methods.contains(element.methodName)
                        }
                    }

                    if (calledFromElement != null) {
                        XposedUtil.xposedDebug(tag, "$methodName method called from class: ${calledFromElement.className}, method: ${calledFromElement.methodName}")
                        param.result = true
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookLenovoUtilsIsRowVersion(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.common.utils.LenovoUtils"
        val methodName = "isRowVersion"
        val parameterTypes = emptyArray<Class<*>>()
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val stackTrace = Thread.currentThread().stackTrace
                    val calledFromElement = stackTrace.find { element ->
                        matchCriteria.any { (className, methods) ->
                            element.className == className && !methods.contains(element.methodName)
                        }
                    }

                    if (calledFromElement != null) {
                        XposedUtil.xposedDebug(tag, "$methodName method called from class: ${calledFromElement.className}, method: ${calledFromElement.methodName}")
                        param.result = false
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }


}
