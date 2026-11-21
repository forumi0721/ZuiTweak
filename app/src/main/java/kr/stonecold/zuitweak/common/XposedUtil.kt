package kr.stonecold.zuitweak.common

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.BuildConfig

@Suppress("unused")
object XposedUtil {
    enum class LogLevel(val level: Int) {
        TRACE(0),
        DEBUG(1),
        VERBOSE(2),
        INFO(3),
        WARNING(4),
        ERROR(5),
        FATAL(6),
        NONE(7)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var currentLogLevel: LogLevel = if (BuildConfig.DEBUG) {
        LogLevel.DEBUG
    } else {
        LogLevel.INFO
    }

    fun xposedTrace(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.TRACE.level) {
            XposedBridge.log("$tag: $message")
        }
    }

    fun xposedDebug(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.DEBUG.level) {
            XposedBridge.log("$tag: $message")
        }
    }

    fun xposedVerbose(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.VERBOSE.level) {
            XposedBridge.log("$tag: $message")
        }
    }

    fun xposedInfo(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.INFO.level) {
            XposedBridge.log("$tag: $message")
        }
    }

    fun xposedWarning(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.WARNING.level) {
            XposedBridge.log("$tag: $message")
        }
    }

    fun xposedError(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.ERROR.level) {
            XposedBridge.log("$tag: $message")
        }
    }

    fun xposedFatal(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.FATAL.level) {
            XposedBridge.log("$tag: $message")
        }
    }

    fun xposedException(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.ERROR.level) {
            XposedBridge.log("$tag: $message")
        }
    }

    fun xposedHookMessage(tag: String, lpparam: XC_LoadPackage.LoadPackageParam, className: String, methodName: String, param: MethodHookParam, beforeAfter: String?) {
        val beforeAfterMessage = if (beforeAfter.isNullOrEmpty()) "" else " $beforeAfter"
        xposedDebug("$tag[${lpparam.packageName}]", "$className.$methodName$beforeAfterMessage args=[${param.args.joinToString(", ")}], result=[${param.result}]")
    }

    @JvmStatic
    fun getClassMethodName(className: String, methodName: String, vararg parameterTypes: Any): String {
        val parameterTypeNames = parameterTypes.mapNotNull { param ->
            when (param) {
                is Class<*> -> {
                    if (param.isArray) {
                        (param.componentType?.name ?: param.name) + "[]"
                    } else {
                        param.name
                    }
                }

                is String -> param
                else -> null
            }
        }.toTypedArray()

        val qualifiedName = if (methodName.isEmpty()) className else "$className.$methodName"
        val classMethodName = "$qualifiedName(${parameterTypeNames.joinToString(",") { it.split(".").last() }})"

        return classMethodName
    }

    @JvmStatic
    fun executeHook(tagHook: String, lpparam: XC_LoadPackage.LoadPackageParam, clazzOrClassName: Any, methodName: String, vararg parameterTypesAndCallback: Any) {
        require(clazzOrClassName is Class<*> || (clazzOrClassName is String && clazzOrClassName.isNotEmpty())) {
            "Unsupported type: ${clazzOrClassName::class.simpleName}"
        }
        require(parameterTypesAndCallback.isNotEmpty() && parameterTypesAndCallback.last() is XC_MethodHook) {
            "No callback defined"
        }

        val tag = "${tagHook}[${lpparam.packageName}]"
        val parameterTypes = parameterTypesAndCallback.dropLast(1).toTypedArray()
        val callback = parameterTypesAndCallback.last() as XC_MethodHook
        val className = if (clazzOrClassName is Class<*>) clazzOrClassName.name else clazzOrClassName as String
        val classMethodName = getClassMethodName(className, methodName, *parameterTypes)

        var successRegister = true
        try {
            if (Util.getMethod(clazzOrClassName, lpparam.classLoader, methodName, *parameterTypes) == null) {
                xposedError(tag, "Failed to register hook for $classMethodName: class or method was not found")
                successRegister = false
            } else {
                if (clazzOrClassName is Class<*>) {
                    if (methodName.isNotEmpty()) {
                        XposedHelpers.findAndHookMethod(clazzOrClassName, methodName, *parameterTypes, callback)
                    } else {
                        XposedHelpers.findAndHookConstructor(clazzOrClassName, *parameterTypes, callback)
                    }
                } else {
                    if (methodName.isNotEmpty()) {
                        XposedHelpers.findAndHookMethod(className, lpparam.classLoader, methodName, *parameterTypes, callback)
                    } else {
                        XposedHelpers.findAndHookConstructor(className, lpparam.classLoader, *parameterTypes, callback)
                    }
                }
            }
        } catch (e: Throwable) {
            val stackTraceString = e.stackTrace.joinToString("\n") { element ->
                "at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})"
            }
            xposedException(tag, "Failed to register hook for $classMethodName: ${e.message}\nStack Trace:\n${stackTraceString}")
            successRegister = false
        }

        if (successRegister) {
            xposedInfo(tag, "Successfully registered hook for $classMethodName")
        }
    }

    @JvmStatic
    fun handleHookException(tag: String, e: Throwable, clazzOrClassName: Any, methodName: String, vararg parameterTypes: Any) {
        if (currentLogLevel.level < LogLevel.ERROR.level) {
            return
        }

        require(clazzOrClassName is Class<*> || (clazzOrClassName is String && clazzOrClassName.isNotEmpty())) {
            "Unsupported type: ${clazzOrClassName::class.simpleName}"
        }

        val className = if (clazzOrClassName is Class<*>) clazzOrClassName.name else clazzOrClassName as String
        val classMethodName = this.getClassMethodName(className, methodName, *parameterTypes)
        val stackTraceString = e.stackTrace.joinToString("\n") { element ->
            "at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})"
        }

        xposedException(
            tag, "Error occurred while hooking $classMethodName: ${e.message}\nStack Trace:\n${stackTraceString}"
        )
    }
}
