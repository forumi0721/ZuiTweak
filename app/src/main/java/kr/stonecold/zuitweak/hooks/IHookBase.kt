package kr.stonecold.zuitweak.hooks

import android.content.Context
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.common.*

interface IHookBase {
    val tag: String
        get() = this::class.java.simpleName

    val menuItem: HookMenuItem

    val hookTargetDevice: Array<String>
    val hookTargetRegion: Array<String>
    val hookTargetVersion: Array<String>

    val hookTargetPackage: Array<String>
    val hookTargetPackageOptional: Array<String>

    fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam)

    fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam)
}

data class HookMenuItem(
    val category: HookMenuCategory,
    val title: String,
    val description: String,
    val defaultSelected: Boolean = true,
    val isDebug: Boolean = false
)

enum class HookMenuCategory {
    COMMON,
    ROW,
    PRC,
    DEVICE,
    UNFUCKZUI,
    DEVELOPMENT,
}

abstract class HookBase : IHookBase {
    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = emptyArray()
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    open fun isEnabledCustomCheck(): Boolean {
        return true
    }

    fun isEnabledHook(packageName: String): Pair<Boolean, String> {
        val isHookPrefsEnabled = XposedPrefsUtil.isFeatureEnabled(this.javaClass.simpleName, menuItem.defaultSelected)
        val isPackageMatch = hookTargetPackage.isEmpty() || (hookTargetPackage.contains(packageName) || hookTargetPackageOptional.contains(packageName))
        val isDeviceMatch = hookTargetDevice.isEmpty() || hookTargetDevice.contains(Constants.deviceModel)
        val isRegionMatch = hookTargetRegion.isEmpty() || hookTargetRegion.contains(Constants.deviceRegion)
        val isVersionMatch = hookTargetVersion.isEmpty() || hookTargetVersion.contains(Constants.deviceVersion)
        val isEnabledCustom = isEnabledCustomCheck()

        val result = isHookPrefsEnabled && isPackageMatch && isDeviceMatch && isRegionMatch && isVersionMatch && isEnabledCustom
        var message = ""
        val detailInfo = mutableListOf<String>()
        if (!isHookPrefsEnabled) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("PrefEnabled: $isHookPrefsEnabled")
        }
        if (!isPackageMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("PackageMatch: $isPackageMatch")
        }
        if (!isDeviceMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("DeviceMatch: $isDeviceMatch")
        }
        if (!isRegionMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("RegionMatch: $isRegionMatch")
        }
        if (!isVersionMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("VersionMatch: $isVersionMatch")
        }
        if (!isEnabledCustom) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("CustomEnabled: $isEnabledCustom")
        }
        if (detailInfo.isNotEmpty()) {
            message = detailInfo.joinToString(", ")
        }

        return Pair(result, message)
    }

    fun isEnabledMenu(context: Context): Pair<Boolean, String> {
        val isPackageMatch = hookTargetPackage.all { pkg -> Util.isPackageInstalled(context, pkg) }
        val isDeviceMatch = hookTargetDevice.isEmpty() || hookTargetDevice.contains(Constants.deviceModel)
        val isRegionMatch = hookTargetRegion.isEmpty() || hookTargetRegion.contains(Constants.deviceRegion)
        val isVersionMatch = hookTargetVersion.isEmpty() || hookTargetVersion.contains(Constants.deviceVersion)
        val isEnabledCustom = isEnabledCustomCheck()

        val result = isPackageMatch && isDeviceMatch && isRegionMatch && isVersionMatch && isEnabledCustom
        var message = ""
        val detailInfo = mutableListOf<String>()
        if (!isPackageMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("PackageMatch: $isPackageMatch")
        }
        if (!isDeviceMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("DeviceMatch: $isDeviceMatch")
        }
        if (!isRegionMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("RegionMatch: $isRegionMatch")
        }
        if (!isVersionMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("VersionMatch: $isVersionMatch")
        }
        if (!isEnabledCustom) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("CustomEnabled: $isEnabledCustom")
        }
        if (detailInfo.isNotEmpty()) {
            message = detailInfo.joinToString(", ")
        }

        return Pair(result, message)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {}

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {}
}

abstract class HookBaseHandleLoadPackage : HookBase() {
    open val hookTargetPackageRes: Array<String> = emptyArray()

    open var updateRes: ((resparam: XC_InitPackageResources.InitPackageResourcesParam) -> Unit)? = null

    fun isEnabledHookRes(packageName: String): Pair<Boolean, String> {
        val isHookPrefsEnabled = XposedPrefsUtil.isFeatureEnabled(this.javaClass.simpleName, menuItem.defaultSelected)
        val isPackageMatch = hookTargetPackageRes.contains(packageName)
        val isDeviceMatch = hookTargetDevice.isEmpty() || hookTargetDevice.contains(Constants.deviceModel)
        val isRegionMatch = hookTargetRegion.isEmpty() || hookTargetRegion.contains(Constants.deviceRegion)
        val isVersionMatch = hookTargetVersion.isEmpty() || hookTargetVersion.contains(Constants.deviceVersion)
        val isEnabledCustom = isEnabledCustomCheck()

        val result = isHookPrefsEnabled && isPackageMatch && isDeviceMatch && isRegionMatch && isVersionMatch && isEnabledCustom
        var message = ""
        val detailInfo = mutableListOf<String>()
        if (!isHookPrefsEnabled) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("PrefEnabled: $isHookPrefsEnabled")
        }
        if (!isPackageMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("PackageMatch: $isPackageMatch")
        }
        if (!isDeviceMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("DeviceMatch: $isDeviceMatch")
        }
        if (!isRegionMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("RegionMatch: $isRegionMatch")
        }
        if (!isVersionMatch) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("VersionMatch: $isVersionMatch")
        }
        if (!isEnabledCustom) {
            @Suppress("KotlinConstantConditions")
            detailInfo.add("CustomEnabled: $isEnabledCustom")
        }
        if (detailInfo.isNotEmpty()) {
            message = detailInfo.joinToString(", ")
        }

        return Pair(result, message)
    }
}

abstract class HookBaseHandleInitPackageResources : HookBase()
