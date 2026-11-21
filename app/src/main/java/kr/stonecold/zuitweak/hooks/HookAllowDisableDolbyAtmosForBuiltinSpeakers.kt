package kr.stonecold.zuitweak.hooks

import android.content.Context
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*
import java.util.Locale

@Suppress("unused")
class HookAllowDisableDolbyAtmosForBuiltinSpeakers : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.UNFUCKZUI,
            title = LanguageUtil.getString(R.string.hook_allow_disable_dolby_atmos_title),
            description = LanguageUtil.getString(R.string.hook_allow_disable_dolby_atmos_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.settings", "com.android.systemui")
    override val hookTargetPackageOptional: Array<String> = emptyArray()
    override val hookTargetPackageRes: Array<String> = arrayOf("com.android.settings", "com.android.systemui")

    override var updateRes: ((resparam: XC_InitPackageResources.InitPackageResourcesParam) -> Unit)? = { resparam ->
        if (resparam.packageName == "com.android.settings" || resparam.packageName == "com.android.systemui") {
            val language = Locale.getDefault().language

            if (language == "ko") {
                val resKey = when (resparam.packageName) {
                    "com.android.settings" -> "dolby_switch_summary"
                    "com.android.systemui" -> "doblyAtmos_title_desc"
                    else -> ""
                }
                val resVal = when (language) {
                    "ko" -> "태블릿 스피커를 사용 중일 때도 Dolby Atmos를 끌 수 있습니다"
                    else -> "When the tablet speaker is in use, Dolby Atmos can be turned off"
                }
                resparam.res.setReplacement(resparam.packageName, "string", resKey, resVal)

                XposedUtil.xposedDebug(tag, "Successfully replaced ${resparam.packageName}.$resKey: $resVal")
            }
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.settings" -> {
                hookDolbyAtmosFragmentIsHeadsetConnected(lpparam)
                hookDolbyAtmosUtilsIsHeadsetConnected(lpparam)
            }

            "com.android.systemui" -> {
                hookQDolbyAtmosTileIsHeadSetConnect(lpparam)
                hookQDolbyAtmosTileIsHeadSetConnect2(lpparam)
                hookQDolbyAtmosDetailViewIsHeadSetConnect(lpparam)
            }
        }
    }

    private fun hookDolbyAtmosFragmentIsHeadsetConnected(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.sound.dolby.DolbyAtmosFragment"
        val methodName = "isHeadsetConnected"
        val parameterTypes = emptyArray<Any>()
        val callback = XC_MethodReplacement.returnConstant(true)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookDolbyAtmosUtilsIsHeadsetConnected(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.sound.dolby.DolbyAtmosUtils"
        val methodName = "isHeadsetConnected"
        val parameterTypes = arrayOf<Any>(Context::class.java)
        val callback = XC_MethodReplacement.returnConstant(true)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookQDolbyAtmosTileIsHeadSetConnect(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.qs.tiles.QDolbyAtmosTile"
        val methodName = "isHeadSetConnect"
        val parameterTypes = emptyArray<Any>()
        val callback = XC_MethodReplacement.returnConstant(true)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookQDolbyAtmosTileIsHeadSetConnect2(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.qs.tiles.QDolbyAtmosTile"
        val methodName = "isHeadSetConnect\$2"
        val parameterTypes = emptyArray<Any>()
        val callback = XC_MethodReplacement.returnConstant(true)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookQDolbyAtmosDetailViewIsHeadSetConnect(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.qs.tiles.QDolbyAtmosDetailView"
        val methodName = "isHeadSetConnect"
        val parameterTypes = emptyArray<Any>()
        val callback = XC_MethodReplacement.returnConstant(true)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }
}
