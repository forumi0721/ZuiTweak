package kr.stonecold.zuitweak.hooks

import android.animation.ArgbEvaluator
import android.app.AndroidAppHelper
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.service.notification.StatusBarNotification
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import com.android.internal.util.ContrastColorUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

@Suppress("unused")
class HookChangeNotificationIcon : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.UNFUCKZUI,
            title = LanguageUtil.getString(R.string.hook_change_notification_icon_title),
            description = LanguageUtil.getString(R.string.hook_change_notification_icon_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.systemui")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    private var systemUiContext: Context? = null
    private var pm: PackageManager? = null
    private val isCtsMode: ThreadLocal<Boolean?> = ThreadLocal.withInitial { null }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.systemui" -> {
                hookApplicationOnCreate(lpparam)
                hookXSystemUtilIsCTSGTSTest(lpparam)
                hookBatteryMeterViewOnDarkChanged(lpparam)
                hookDarkIconDispatcherImplConstructor(lpparam)
                hookStatusBarIconViewSetStaticDrawableColor(lpparam)
                hookNotificationIconAreaControllerGenerateIconLayoutParams(lpparam)
                hookZuiCoreImplClearStatusBarIcon(lpparam)
                hookNotificationShelfUpdateResources(lpparam)
                hookNotificationIconContainerInitResources(lpparam)
                hookNotificationHeaderViewWrapperOnContentUpdated(lpparam)
                hookNotificationInfoBindHeader(lpparam)
                hookNotificationIconAreaControllerOnDarkChanged(lpparam)
            }
        }
    }

    private fun hookApplicationOnCreate(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = Application::class.java
        val methodName = "onCreate"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    systemUiContext = param.thisObject as Context
                    pm = systemUiContext!!.packageManager
                } catch (e: Throwable) {
                    val className = clazz.name
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, clazz, methodName, *parameterTypes, callback)
    }

    private fun hookXSystemUtilIsCTSGTSTest(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.util.XSystemUtil"
        val methodName = "isCTSGTSTest"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val mode = isCtsMode.get()
                    if (mode != null) {
                        param.result = mode == true
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookBatteryMeterViewOnDarkChanged(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.battery.BatteryMeterView"
        val methodName = "onDarkChanged"
        val parameterTypes = arrayOf<Any>(ArrayList::class.java, Float::class.java, Int::class.java)
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    isCtsMode.set(java.lang.Boolean.FALSE)
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    isCtsMode.remove()
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookDarkIconDispatcherImplConstructor(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.statusbar.phone.DarkIconDispatcherImpl"
        val methodName = "" //Constructor
        val parameterTypes = arrayOf<Any>(
            Context::class.java,
            "com.android.systemui.statusbar.phone.LightBarTransitionsController\$Factory",
            "com.android.systemui.dump.DumpManager"
        )
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    XposedHelpers.setIntField(param.thisObject, "mDarkModeIconColorSingleTone", -0x21000000)
                    XposedHelpers.setIntField(param.thisObject, "mDarkModeIconColorSingleToneCts", -0x21000000)
                    XposedHelpers.setIntField(param.thisObject, "mLightModeIconColorSingleTone", -0x1)
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookStatusBarIconViewSetStaticDrawableColor(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.statusbar.StatusBarIconView"
        val methodName = "setStaticDrawableColor"
        val parameterTypes = arrayOf<Any>(Int::class.java)
        val callback = object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam) {
                try {
                    val color = param.args[0] as Int
                    XposedHelpers.setIntField(param.thisObject, "mDrawableColor", color)
                    XposedHelpers.callMethod(param.thisObject, "setColorInternal", color)
                    XposedHelpers.callMethod(param.thisObject, "updateContrastedStaticColor")
                    XposedHelpers.setIntField(param.thisObject, "mIconColor", color)
                    val mDozer = XposedHelpers.getObjectField(param.thisObject, "mDozer")
                    if (mDozer != null) {
                        XposedHelpers.callMethod(mDozer, "setColor", color)
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookNotificationIconAreaControllerGenerateIconLayoutParams(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.statusbar.phone.NotificationIconAreaController"
        val methodName = "generateIconLayoutParams"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val lp = param.result as FrameLayout.LayoutParams
                    val ctx = getSystemUiContext()
                    val m = ctx!!.resources.displayMetrics
                    val h = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, m)
                    val p = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, m)
                    lp.height = h.toInt()
                    lp.width += (p * 2).toInt()
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookZuiCoreImplClearStatusBarIcon(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.statusbar.phone.ZuiCoreImpl"
        val methodName = "clearStatusBarIcon"
        val parameterTypes = emptyArray<Any>()
        val callback = XC_MethodReplacement.returnConstant(null)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookNotificationHeaderViewWrapperOnContentUpdated(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper"
        val methodName = "onContentUpdated"
        val parameterTypes = arrayOf<Any>("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow")
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val row = param.args[0]
                    val entry = XposedHelpers.callMethod(row, "getEntry")
                    val sbn = XposedHelpers.callMethod(entry, "getSbn") as StatusBarNotification
                    val mIcon = XposedHelpers.getObjectField(param.thisObject, "mIcon") as ImageView

                    @Suppress("LocalVariableName")
                    val KEY_BACKGROUND_UNFUCKED = 1145141919
                    val scale = 24.0f / 34.0f
                    if (mIcon.getTag(KEY_BACKGROUND_UNFUCKED) != java.lang.Boolean.TRUE) {
                        val d = ShapeDrawable()
                        d.shape = OvalShape()
                        d.paint.color = -0xcccccd
                        mIcon.background = d
                        val lp = mIcon.layoutParams
                        lp.width = Math.round(lp.width * scale)
                        lp.height = Math.round(lp.height * scale)
                        if (lp is MarginLayoutParams) {
                            lp.marginStart += Math.round(lp.width * ((1.0f - scale) * 0.75f))
                        }
                        mIcon.requestLayout()
                        mIcon.setTag(KEY_BACKGROUND_UNFUCKED, java.lang.Boolean.TRUE)
                    }

                    val isDark = isDark()
                    val orgColor = sbn.notification.color
                    val bgColor = if (orgColor == 0 || orgColor == 1) {
                        getSystemAccentColor()
                    } else {
                        orgColor
                    }
                    val fgColor = ContrastColorUtil.resolveContrastColor(getSystemUiContext(), 0, bgColor, !isDark)

                    val cachingIconView = XposedHelpers.findClass("com.android.internal.widget.CachingIconView", lpparam.classLoader)
                    XposedHelpers.callMethod(cachingIconView, "setBackgroundColor", fgColor)
                    XposedHelpers.callMethod(cachingIconView, "setOriginalIconColor", bgColor)
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookNotificationShelfUpdateResources(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.statusbar.NotificationShelf"
        val methodName = "updateResources"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val mShelfIcons = XposedHelpers.getObjectField(param.thisObject, "mShelfIcons")
                    XposedHelpers.callMethod(mShelfIcons, "setInNotificationIconShelf", true)
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookNotificationIconContainerInitResources(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.statusbar.phone.NotificationIconContainer"
        val methodName = "initResources"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val context = XposedHelpers.getObjectField(param.thisObject, "mContext") as Context
                    val themedContext: Context = ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault_DayNight)
                    themedContext.obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary)).use { attrs ->
                        val color = attrs.getColorStateList(0)!!.defaultColor
                        XposedHelpers.setIntField(param.thisObject, "mThemedTextColorPrimary", color)
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookNotificationInfoBindHeader(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.statusbar.notification.row.NotificationInfo"
        val methodName = "bindHeader"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val mPackageName = XposedHelpers.getObjectField(param.thisObject, "mPackageName") as String
                    lateinit var mPkgIcon: Drawable
                    try {
                        val info = pm!!.getApplicationInfo(
                            mPackageName,
                            PackageManager.MATCH_UNINSTALLED_PACKAGES
                                    or PackageManager.MATCH_DISABLED_COMPONENTS
                                    or PackageManager.MATCH_DIRECT_BOOT_UNAWARE
                                    or PackageManager.MATCH_DIRECT_BOOT_AWARE
                        )
                        mPkgIcon = pm!!.getApplicationIcon(info)
                    } catch (e: PackageManager.NameNotFoundException) {
                        mPkgIcon = pm!!.defaultActivityIcon
                    }
                    val view = param.thisObject as View
                    val pkgIconRes = XposedHelpers.getStaticIntField(XposedHelpers.findClass("com.android.systemui.R\$id", lpparam.classLoader), "pkg_icon")
                    val icon = view.findViewById<View>(pkgIconRes) as ImageView
                    icon.setImageDrawable(mPkgIcon)
                    XposedHelpers.setObjectField(param.thisObject, "mPkgIcon", mPkgIcon)
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookNotificationIconAreaControllerOnDarkChanged(lpparam: XC_LoadPackage.LoadPackageParam) {
        val argbEvaluator = ArgbEvaluator()

        val className = "com.android.systemui.statusbar.phone.NotificationIconAreaController"
        val methodName = "onDarkChanged"
        val parameterTypes = arrayOf<Any>(java.util.ArrayList::class.java, Float::class.java, Int::class.java)
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val darkIntensity = param.args[1] as Float
                    val mIconTint = argbEvaluator.evaluate(darkIntensity, -0x1, -0x4e000000)
                    param.args[2] = mIconTint
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun getSystemUiContext(): Context? {
        if (systemUiContext == null) {
            try {
                systemUiContext = AndroidAppHelper.currentApplication()
            } catch (ignored: Throwable) {
            }
        }
        return systemUiContext
    }

    private fun isDark(): Boolean {
        return isDark(getSystemUiContext())
    }

    private fun isDark(ctx: Context?): Boolean {
        return (ctx!!.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    private fun getSystemAccentColor(): Int {
        return getSystemUiContext()!!.getColor(android.R.color.system_accent1_500)
    }
}
