package kr.stonecold.zuitweak.hooks

import de.robv.android.xposed.callbacks.XC_InitPackageResources
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

@Suppress("unused")
class HookRemoveShutdownMessage : HookBaseHandleInitPackageResources() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.COMMON,
            title = LanguageUtil.getString(R.string.hook_remove_shutdown_message_title),
            description = LanguageUtil.getString(R.string.hook_remove_shutdown_message_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.systemui")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun isEnabledCustomCheck(): Boolean {
        return true
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        when (resparam.packageName) {
            "com.android.systemui" -> {
                hookRemoveMessage(resparam)
            }
        }
    }

    private fun hookRemoveMessage(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        val resKey = "shutdown_dialog_tips_message"
        val resVal = ""
        resparam.res.setReplacement(resparam.packageName, "string", resKey, resVal)

        XposedUtil.xposedDebug(tag, "Successfully replaced ${resparam.packageName}.$resKey: $resVal")
    }
}
