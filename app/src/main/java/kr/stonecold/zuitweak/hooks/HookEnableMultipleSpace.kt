package kr.stonecold.zuitweak.hooks

import android.content.Context
import android.os.Binder
import android.os.ServiceManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

@Suppress("unused")
class HookEnableMultipleSpace : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.ROW,
            title = LanguageUtil.getString(R.string.hook_enable_multiple_space_title),
            description = LanguageUtil.getString(R.string.hook_enable_multiple_space_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = arrayOf("ROW")
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("android", "com.android.settings")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun isEnabledCustomCheck(): Boolean {
        val zuiMultispaceEnabled = Util.getProperty("ro.stonecold.zuimultispace.enabled", "false").uppercase()
        return zuiMultispaceEnabled == "TRUE"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                hookSystemServerAppClone(lpparam)
            }

            "com.android.settings" -> {
                hookSettingsAppClone(lpparam)
            }
        }
    }

    /**
     * Settings app hooks to enable AppClone menu and screens.
     */
    private fun hookSettingsAppClone(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 1. UserHandle.supportsAppClone() -> true
        XposedUtil.executeHook(
            tag, lpparam,
            "android.os.UserHandle",
            "supportsAppClone",
            XC_MethodReplacement.returnConstant(true)
        )

        // 2. AppCloneUtils.supportsAppClone() -> true
        XposedUtil.executeHook(
            tag, lpparam,
            "com.lenovo.settings.applications.appclone.AppCloneUtils",
            "supportsAppClone",
            XC_MethodReplacement.returnConstant(true)
        )

        // 3. LenovoAppHeaderPreferenceController: allow displaying AppClone entry even on ROW
        // (Original code checks: LenovoUtils.isPrcVersion() && AppCloneUtils.supportsAppClone())
        XposedUtil.executeHook(
            tag, lpparam,
            "com.lenovo.common.utils.LenovoUtils",
            "isPrcVersion",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        val stackTrace = Thread.currentThread().stackTrace
                        val isCalledFromAppHeader = stackTrace.any {
                            it.className.startsWith("com.lenovo.settings.applications.LenovoAppHeaderPreferenceController")
                        }
                        if (isCalledFromAppHeader) {
                            param.result = true
                        }
                    } catch (e: Throwable) {
                        XposedUtil.handleHookException(tag, e, "com.lenovo.common.utils.LenovoUtils", "isPrcVersion")
                    }
                }
            }
        )
    }

    /**
     * system_server hooks to bridge Motorola's stubbed AppClone API into AOSP Clone Profile management.
     */
    private fun hookSystemServerAppClone(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 1. UserHandle.supportsAppClone() -> true
        XposedUtil.executeHook(
            tag, lpparam,
            "android.os.UserHandle",
            "supportsAppClone",
            XC_MethodReplacement.returnConstant(true)
        )

        // 2. PackageManagerService initialization & IPackageManagerImpl methods
        val pmsClass = XposedHelpers.findClassIfExists("com.android.server.pm.PackageManagerService", lpparam.classLoader)
        if (pmsClass != null) {
            // Ensure mSupportsAppClone is true after PMS constructor
            XposedBridge.hookAllConstructors(pmsClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        XposedHelpers.setBooleanField(param.thisObject, "mSupportsAppClone", true)
                        XposedUtil.xposedDebug(tag, "PackageManagerService.mSupportsAppClone set to true")
                    } catch (e: Throwable) {
                        XposedUtil.handleHookException(tag, e, "PackageManagerService", "<init>")
                    }
                }
            })
        }

        val ipmsClass = XposedHelpers.findClassIfExists("com.android.server.pm.PackageManagerService\$IPackageManagerImpl", lpparam.classLoader)
        if (ipmsClass != null) {
            // Hook addAppClone(String packageName, String label)
            XposedUtil.executeHook(
                tag, lpparam,
                ipmsClass.name,
                "addAppClone",
                String::class.java, String::class.java,
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any {
                        val packageName = param.args[0] as? String ?: return -1
                        val label = param.args[1] as? String ?: ""
                        XposedUtil.xposedDebug(tag, "addAppClone called for package: $packageName, label: $label")
                        return handleAddAppClone(param.thisObject, packageName, label)
                    }
                }
            )

            // Hook removeAppClone(String packageName, int userId)
            XposedUtil.executeHook(
                tag, lpparam,
                ipmsClass.name,
                "removeAppClone",
                String::class.java, Int::class.javaPrimitiveType!!,
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any? {
                        val packageName = param.args[0] as? String ?: return null
                        val userId = param.args[1] as Int
                        XposedUtil.xposedDebug(tag, "removeAppClone called for package: $packageName, userId: $userId")
                        handleRemoveAppClone(param.thisObject, packageName, userId)
                        return null
                    }
                }
            )

            // Hook getAppCloneLabel(String packageName, int userId)
            XposedUtil.executeHook(
                tag, lpparam,
                ipmsClass.name,
                "getAppCloneLabel",
                String::class.java, Int::class.javaPrimitiveType!!,
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any? {
                        val packageName = param.args[0] as? String ?: return null
                        val userId = param.args[1] as Int
                        return getAppCloneLabel(param.thisObject, packageName, userId)
                    }
                }
            )

            // Hook setAppCloneLabel(String packageName, String label, int userId)
            XposedUtil.executeHook(
                tag, lpparam,
                ipmsClass.name,
                "setAppCloneLabel",
                String::class.java, String::class.java, Int::class.javaPrimitiveType!!,
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any? {
                        return null
                    }
                }
            )
        }
    }

    /**
     * Logic to create/find clone profile and install the existing package into it.
     */
    private fun handleAddAppClone(ipmsInstance: Any, packageName: String, label: String): Int {
        val identity = Binder.clearCallingIdentity()
        try {
            val userManagerService = ServiceManager.getService("user") ?: run {
                XposedUtil.xposedDebug(tag, "UserManagerService is null")
                return -1
            }

            // 1. Check existing clone profiles
            var targetUserId = -1
            val cloneProfiles = XposedHelpers.callMethod(userManagerService, "getAppCloneProfiles") as? List<*>
            if (!cloneProfiles.isNullOrEmpty()) {
                for (profile in cloneProfiles) {
                    if (profile != null) {
                        val id = XposedHelpers.getIntField(profile, "id")
                        if (id in 900..999) {
                            targetUserId = id
                            break
                        }
                    }
                }
            }

            // 2. If no clone profile exists, create one
            if (targetUserId < 0) {
                XposedUtil.xposedDebug(tag, "Creating new clone profile...")
                val newProfile = XposedHelpers.callMethod(
                    userManagerService,
                    "createProfileForUserWithThrow",
                    "Clone Profile",
                    "android.os.usertype.profile.CLONE",
                    0, // flags
                    0, // parent userId = 0 (system user)
                    null // disallowedPackages
                )

                if (newProfile == null) {
                    XposedUtil.xposedDebug(tag, "Failed to create clone profile")
                    return -1
                }
                targetUserId = XposedHelpers.getIntField(newProfile, "id")
                XposedUtil.xposedDebug(tag, "Created clone profile with userId: $targetUserId")

                // Start the new profile user in background
                try {
                    val am = ActivityManagerUtil.getIActivityManager()
                    if (am != null) {
                        XposedHelpers.callMethod(am, "startProfile", targetUserId)
                    }
                } catch (e: Throwable) {
                    XposedUtil.xposedDebug(tag, "startProfile exception: ${e.message}")
                }
            }

            // 3. Install existing package into target clone profile
            val pms = XposedHelpers.getSurroundingThis(ipmsInstance)
            val installResult = XposedHelpers.callMethod(
                pms,
                "installExistingPackageAsUser",
                packageName,
                targetUserId,
                0, // installFlags
                0, // installReason
                null as List<String>? // whiteListedPermissions
            ) as? Int ?: -1

            XposedUtil.xposedDebug(tag, "installExistingPackageAsUser result: $installResult for user $targetUserId")
            return if (installResult == 1) targetUserId else -1
        } catch (e: Throwable) {
            XposedUtil.xposedDebug(tag, "handleAddAppClone failed: ${e.message}")
            return -1
        } finally {
            Binder.restoreCallingIdentity(identity)
        }
    }

    /**
     * Logic to remove the cloned package from the clone profile user.
     */
    private fun handleRemoveAppClone(ipmsInstance: Any, packageName: String, userId: Int) {
        val identity = Binder.clearCallingIdentity()
        try {
            val pms = XposedHelpers.getSurroundingThis(ipmsInstance)
            XposedUtil.xposedDebug(tag, "Deleting package $packageName from clone user $userId")

            // Delete package from user
            XposedHelpers.callMethod(
                pms,
                "deletePackageAsUser",
                packageName,
                -1, // versionCode
                null, // IPackageDeleteObserver
                userId,
                0 // flags
            )
        } catch (e: Throwable) {
            XposedUtil.xposedDebug(tag, "handleRemoveAppClone failed: ${e.message}")
        } finally {
            Binder.restoreCallingIdentity(identity)
        }
    }

    /**
     * Returns label for the cloned app. Defaults to package/app label.
     */
    private fun getAppCloneLabel(ipmsInstance: Any, packageName: String, userId: Int): String? {
        val identity = Binder.clearCallingIdentity()
        try {
            val pms = XposedHelpers.getSurroundingThis(ipmsInstance)
            val context = XposedHelpers.getObjectField(pms, "mContext") as? Context ?: return null
            val pm = context.packageManager
            val appInfo = XposedHelpers.callMethod(pm, "getApplicationInfoAsUser", packageName, 0, userId)
            if (appInfo != null) {
                return pm.getApplicationLabel(appInfo as android.content.pm.ApplicationInfo)?.toString()
            }
            return null
        } catch (e: Throwable) {
            return null
        } finally {
            Binder.restoreCallingIdentity(identity)
        }
    }

    private object ActivityManagerUtil {
        fun getIActivityManager(): Any? {
            return try {
                val amClass = Class.forName("android.app.ActivityManager")
                XposedHelpers.callStaticMethod(amClass, "getService")
            } catch (e: Throwable) {
                null
            }
        }
    }
}
