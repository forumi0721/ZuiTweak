package kr.stonecold.zuitweak.hooks

import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

@Suppress("unused")
class HookEnableBatteryOverheatNotify : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.ROW,
            title = LanguageUtil.getString(R.string.hook_enable_battery_overheat_notify_title),
            description = LanguageUtil.getString(R.string.hook_enable_battery_overheat_notify_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = arrayOf("ROW")
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.settings")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.settings" -> {
                hookZuiBatteryOverheatPreferenceControllergetAvailabilityStatus(lpparam)
                hookZuiBatteryOverheatPreferenceController17getAvailabilityStatus(lpparam)
            }
        }
    }

    private fun hookZuiBatteryOverheatPreferenceControllergetAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.fuelgauge.ZuiBatteryOverheatPreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = XC_MethodReplacement.returnConstant(0)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookZuiBatteryOverheatPreferenceController17getAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.battery.ZuiBatteryOverheatPreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = XC_MethodReplacement.returnConstant(0)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }
}
