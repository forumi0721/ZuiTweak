package kr.stonecold.zuitweak.hooks

import android.content.Context
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*
import java.util.Locale

@Suppress("unused")
class HookEnableHotspot : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.COMMON,
            title = LanguageUtil.getString(R.string.hook_enable_hotspot_title),
            description = LanguageUtil.getString(R.string.hook_enable_hotspot_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.settings")
    override val hookTargetPackageOptional: Array<String> = emptyArray()
    override val hookTargetPackageRes: Array<String> = arrayOf("com.android.settings")

    override var updateRes: ((resparam: XC_InitPackageResources.InitPackageResourcesParam) -> Unit)? = { resparam ->
        if (resparam.packageName == "com.android.settings") {
            val language = Locale.getDefault().language

            if (language == "ko") {
                val resKey = "cel_wifi_hotspot_checkbox_text"
                val hotspotCheckboxText = when (language) {
                    "ko" -> "핫스팟"
                    else -> "Hotspot"
                }
                resparam.res.setReplacement(resparam.packageName, "string", resKey, hotspotCheckboxText)

                XposedUtil.xposedDebug(tag, "Successfully replaced ${resparam.packageName}.$resKey: $hotspotCheckboxText")
            }
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.settings" -> {
                hookTopLevelTetherPreferenceControllerGetAvailabilityStatus(lpparam)
                hookWifiEnterpriseRestrictionUtilsIsWifiTetheringAllowed(lpparam)
                hookWifiTetherPreferenceControllerIsAvailable(lpparam)
                hookLenovoUtilsIsSupportTether(lpparam)
            }
        }
    }

    private fun hookTopLevelTetherPreferenceControllerGetAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.homepage.controller.TopLevelTetherPreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = XC_MethodReplacement.returnConstant(0)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookWifiEnterpriseRestrictionUtilsIsWifiTetheringAllowed(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settingslib.wifi.WifiEnterpriseRestrictionUtils"
        val methodName = "isWifiTetheringAllowed"
        val parameterTypes = arrayOf<Any>(Context::class.java)
        val callback = XC_MethodReplacement.returnConstant(true)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookWifiTetherPreferenceControllerIsAvailable(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.wifi.tether.WifiTetherPreferenceController"
        val methodName = "isAvailable"
        val parameterTypes = emptyArray<Any>()
        val callback = XC_MethodReplacement.returnConstant(true)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookLenovoUtilsIsSupportTether(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.common.utils.LenovoUtils"
        val methodName = "isSupportTether"
        val parameterTypes = arrayOf<Any>(Context::class.java)
        val callback = XC_MethodReplacement.returnConstant(true)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }
}
