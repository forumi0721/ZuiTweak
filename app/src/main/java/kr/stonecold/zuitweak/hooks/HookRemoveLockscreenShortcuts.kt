package kr.stonecold.zuitweak.hooks

import android.view.View
import android.widget.ImageView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*


@Suppress("unused")
class HookRemoveLockscreenShortcuts : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.COMMON,
            title = LanguageUtil.getString(R.string.hook_remove_lockscreen_shortcuts_title),
            description = LanguageUtil.getString(R.string.hook_remove_lockscreen_shortcuts_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.systemui")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.systemui" -> {
                hookKeyguardQuickAffordanceInteractorIsUsingRepository(lpparam)
                hookKeyguardBottomAreaViewBinderUpdateButton(lpparam)
                hookKeyguardBottomAreaViewBinderUpdateButton17(lpparam)
            }
        }
    }

    private fun hookKeyguardQuickAffordanceInteractorIsUsingRepository(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.keyguard.domain.interactor.KeyguardQuickAffordanceInteractor"
        val methodName = "isUsingRepository"
        val parameterTypes = emptyArray<Any>()
        val callback = XC_MethodReplacement.returnConstant(false)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookKeyguardBottomAreaViewBinderUpdateButton(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.keyguard.ui.binder.KeyguardBottomAreaViewBinder"
        val methodName = "updateButton"
        val parameterTypes = arrayOf<Any>("android.widget.ImageView", "com.android.systemui.keyguard.ui.viewmodel.KeyguardQuickAffordanceViewModel", "com.android.systemui.plugins.FalsingManager", "kotlin.jvm.functions.Function1", "com.android.systemui.statusbar.VibratorHelper", "com.android.keyguard.KeyguardUpdateMonitor")
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val viewModel = param.args[1]
                    if (viewModel != null) {
                        val useLongPress =
                            XposedHelpers.callMethod(viewModel, "getUseLongPress") as Boolean
                        if (useLongPress) {
                            val view = param.args[0] as ImageView
                            view.visibility = View.GONE
                            param.result = null
                        }
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookKeyguardBottomAreaViewBinderUpdateButton17(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.keyguard.ui.binder.KeyguardBottomAreaViewBinder"
        val methodName = "access\$updateButton"
        val parameterTypes = arrayOf<Any>("android.widget.ImageView", "com.android.systemui.keyguard.ui.viewmodel.KeyguardQuickAffordanceViewModel", "com.android.systemui.plugins.FalsingManager", "kotlin.jvm.functions.Function1", "com.android.systemui.statusbar.VibratorHelper")
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val viewModel = param.args[1]
                    if (viewModel != null) {
                        val useLongPress = try {
                            XposedHelpers.getBooleanField(viewModel, "useLongPress")
                        } catch (t: Throwable) {
                            XposedHelpers.callMethod(viewModel, "getUseLongPress") as Boolean
                        }
                        if (useLongPress) {
                            val view = param.args[0] as ImageView
                            view.visibility = View.GONE
                            param.result = null
                        }
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

}
