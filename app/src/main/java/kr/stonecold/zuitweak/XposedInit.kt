package kr.stonecold.zuitweak

import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.common.*
import kr.stonecold.zuitweak.hooks.*

@Suppress("unused")
class XposedInit : IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    val tag: String = this.javaClass.simpleName

    companion object {
        var modulePath: String? = null
        lateinit var handleLoadPackagePackages: Array<String>
        lateinit var handleInitPackageResourcesPackages: Array<String>
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
        if (startupParam == null) {
            return
        }

        modulePath = startupParam.modulePath
        XposedUtil.xposedDebug(tag, "Module Path: $modulePath")

        val hooks = HookManager.getAllHooks()

        handleLoadPackagePackages = hooks
            .filterIsInstance<HookBaseHandleLoadPackage>()
            .filter { hook -> !hook.menuItem.isDebug || BuildConfig.DEBUG }
            .flatMap { it.hookTargetPackage.asList() + it.hookTargetPackageOptional.asList() }
            .distinct()
            .toTypedArray()
        XposedUtil.xposedDebug(tag, "HandleLoadPackage Packages: ${handleLoadPackagePackages.joinToString(", ")}")

        handleInitPackageResourcesPackages = hooks
            .filterIsInstance<HookBaseHandleInitPackageResources>()
            .filter { hook -> !hook.menuItem.isDebug || BuildConfig.DEBUG }
            .flatMap { it.hookTargetPackage.asList() + it.hookTargetPackageOptional.asList() }
            .distinct()
            .toTypedArray()
        XposedUtil.xposedDebug(tag, "HandleInitPackageResources Packages: ${handleInitPackageResourcesPackages.joinToString(", ")}")
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (Constants.deviceModel != "TB371FC" && Constants.deviceModel != "TB320FC") {
            XposedUtil.xposedError(tag, "Device Model: ${Constants.deviceModel} - This device is not supported")
            return
        }

        if (lpparam == null) {
            return
        }

        handleLoadPackageCustom(lpparam)

        if (handleLoadPackagePackages.isEmpty() || !handleLoadPackagePackages.contains(lpparam.packageName)) {
            return
        }

        val hooks = HookManager.getAllHooks()

        val filterHooks = hooks.filterIsInstance<HookBaseHandleLoadPackage>()
            .filter { hook -> hook.hookTargetPackage.contains(lpparam.packageName) || hook.hookTargetPackageOptional.contains(lpparam.packageName) }
            .filter { hook -> !hook.menuItem.isDebug || BuildConfig.DEBUG }

        if (filterHooks.isEmpty()) {
            return
        }

        XposedUtil.xposedInfo(tag, "HandleLoadPackage Loaded App: ${lpparam.packageName}")

        XposedPrefsUtil.reload()

        filterHooks.forEach { hook ->
            val localTag = "${hook.javaClass.simpleName}[${lpparam.packageName}]"
            try {
                XposedUtil.xposedDebug(localTag, "Checking hook")

                val (isEnabled, message) = hook.isEnabledHook(lpparam.packageName)

                if (isEnabled) {
                    XposedUtil.xposedInfo(localTag, "handleLoadPackage triggered")
                    hook.handleLoadPackage(lpparam)
                } else {
                    XposedUtil.xposedDebug(localTag, "handleLoadPackage not triggered: $message")
                }
            } catch (e: Exception) {
                XposedUtil.xposedException(localTag, "Error during handleLoadPackage: ${e.message}")
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleLoadPackageCustom(lpparam: XC_LoadPackage.LoadPackageParam) {
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam?) {
        if (Constants.deviceModel != "TB371FC" && Constants.deviceModel != "TB320FC") {
            XposedUtil.xposedError(tag, "Device Model: ${Constants.deviceModel} - This device is not supported")
            return
        }

        if (resparam == null) {
            return
        }

        XposedPrefsUtil.reload()

        handleInitPackageResourcesCustom(resparam)

        if (handleInitPackageResourcesPackages.isEmpty() || !handleInitPackageResourcesPackages.contains(resparam.packageName)) {
            return
        }

        val hooks = HookManager.getAllHooks()

        val filterHooks = hooks.filterIsInstance<HookBaseHandleInitPackageResources>()
            .filter { hook -> hook.hookTargetPackage.contains(resparam.packageName) || hook.hookTargetPackageOptional.contains(resparam.packageName) }
            .filter { hook -> !hook.menuItem.isDebug || BuildConfig.DEBUG }

        if (filterHooks.isEmpty()) {
            return
        }

        XposedUtil.xposedInfo(tag, "HandleInitPackageResources Loaded App: ${resparam.packageName}")

        filterHooks.forEach { hook ->
            val localTag = "${hook.javaClass.simpleName}[${resparam.packageName}]"
            try {
                XposedUtil.xposedDebug(localTag, "Checking hook")

                val (isEnabled, message) = hook.isEnabledHook(resparam.packageName)

                if (isEnabled) {
                    XposedUtil.xposedInfo(localTag, "handleInitPackageResources triggered")
                    hook.handleInitPackageResources(resparam)
                } else {
                    XposedUtil.xposedDebug(localTag, "handleInitPackageResources not triggered: $message")
                }
            } catch (e: Exception) {
                XposedUtil.xposedException(localTag, "Error during handleInitPackageResources: ${e.message}")
            }
        }
    }

    private fun handleInitPackageResourcesCustom(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        val hooks = HookManager.getAllHooks()

        val filterHooks = hooks.filterIsInstance<HookBaseHandleLoadPackage>()
            .filter { hook -> hook.updateRes != null && hook.hookTargetPackageRes.contains(resparam.packageName) }
            .filter { hook -> !hook.menuItem.isDebug || BuildConfig.DEBUG }

        if (filterHooks.isEmpty()) {
            return
        }

        XposedUtil.xposedInfo(tag, "HandleInitPackageResources Loaded App: ${resparam.packageName}")

        filterHooks.forEach { hook ->
            val localTag = "${hook.javaClass.simpleName}[${resparam.packageName}]"
            try {
                XposedUtil.xposedDebug(localTag, "Checking hook")

                val (isEnabled, message) = hook.isEnabledHookRes(resparam.packageName)

                if (isEnabled) {
                    XposedUtil.xposedInfo(localTag, "updateRes triggered")
                    hook.updateRes?.invoke(resparam)
                } else {
                    XposedUtil.xposedDebug(localTag, "updateRes not triggered: $message")
                }
            } catch (e: Exception) {
                XposedUtil.xposedException(localTag, "Error during updateRes: ${e.message}")
            }
        }
    }
}
