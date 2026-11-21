package kr.stonecold.zuitweak.hooks

import android.content.Context
import androidx.core.net.toUri
import android.content.Intent
import android.view.KeyEvent
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.Constants
import kr.stonecold.zuitweak.common.LanguageUtil
import kr.stonecold.zuitweak.common.XposedUtil


@Suppress("unused")
class HookChangeKeyboardHotkeys : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.COMMON,
            title = LanguageUtil.getString(R.string.hook_change_keyboard_hotkeys_title),
            description = LanguageUtil.getString(R.string.hook_change_keyboard_hotkeys_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("android")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                hookKeyboardZuiKeyInputPolicyInterceptKeyBeforeDispatchingBefore(lpparam)
            }
        }
    }

    private fun hookKeyboardZuiKeyInputPolicyInterceptKeyBeforeDispatchingBefore(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.zui.server.input.keyboard.key.policy.KeyboardZuiKeyInputPolicy"
        val methodName = "interceptKeyBeforeDispatchingBefore"
        val parameterTypes = arrayOf<Any>(KeyEvent::class.java, Int::class.java)
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val keyEvent = param.args[0] as KeyEvent
                    val keyCode = keyEvent.keyCode
                    val repeatCount = keyEvent.repeatCount
                    val z = keyEvent.action == 0

                    if (keyCode != 33 && keyCode != 47) {
                        return
                    }

                    val mIsLanguageSwitchKeyPressed = XposedHelpers.getBooleanField(param.thisObject, "mIsLanguageSwitchKeyPressed")

                    when (keyCode) {
                        33 -> {
                            val mOOBECompleted = XposedHelpers.getBooleanField(param.thisObject, "mOOBECompleted")
                            if (mOOBECompleted && z && repeatCount == 0 && (mIsLanguageSwitchKeyPressed || keyEvent.isMetaPressed)) {
                                val context = XposedHelpers.getObjectField(param.thisObject, "mContext") as Context
                                val intent = Intent().apply {
                                    action = Intent.ACTION_VIEW
                                    data = "content://com.android.externalstorage.documents/root/primary".toUri()
                                    setClassName("com.android.documentsui", "com.android.documentsui.files.FilesActivity")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }

                                try {
                                    context.startActivity(intent)
                                } catch (e: Throwable) {
                                    XposedUtil.xposedException(tag, "Failed to start activity! package name : com.android.documentsui")
                                }

                                val kbInputUtils = XposedHelpers.callMethod(param.thisObject, "getKbInputUtils")
                                XposedHelpers.callMethod(kbInputUtils, "sendBigdataZKB115", "wine", keyEvent);
                                param.result = true
                            }
                        }

                        47 -> {
                            if (z && repeatCount == 0 && (mIsLanguageSwitchKeyPressed || keyEvent.isMetaPressed)) {
                                val context = XposedHelpers.getObjectField(param.thisObject, "mContext") as Context
                                val intent = Intent().apply {
                                    action = Intent.ACTION_ASSIST
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }

                                try {
                                    context.startActivity(intent)
                                } catch (e: Throwable) {
                                    XposedUtil.xposedException(tag, "Failed to start activity! package name : ASSISTANT")
                                }

                                param.result = true
                            }
                        }
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun runnableKeyEvent(obj: Any, event: KeyEvent) {
        try {
            val kbInputUtils = XposedHelpers.callMethod(obj, "getKbInputUtils")
            XposedHelpers.callMethod(kbInputUtils, "checkIfSendToBigData", event)

            if (XposedHelpers.callMethod(kbInputUtils, "isEventFromLenovoKeyboard", event) as Boolean) {
                XposedHelpers.callMethod(kbInputUtils, "checkIfSendUsageDuration")
            }
        } catch (e: Throwable) {
            XposedUtil.xposedException(tag, "Error handling KeyEvent: ${e.message}")
        }
    }
}
