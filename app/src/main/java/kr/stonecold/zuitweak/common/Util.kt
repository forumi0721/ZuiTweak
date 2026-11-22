package kr.stonecold.zuitweak.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

@Suppress("unused")
object Util {
    private val tag = this.javaClass.simpleName

    fun getModel(): String {
        return Build.MODEL
    }

    @SuppressLint("PrivateApi")
    fun getProperty(key: String, defaultValue: String = ""): String {
        var ret = defaultValue

        try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val paramTypes = arrayOf<Class<*>>(String::class.java)
            val getMethod = systemProperties.getMethod("get", *paramTypes)

            val value = getMethod.invoke(null, key) as? String
            if (!value.isNullOrBlank()) {
                ret = value
            }
        } catch (e: IllegalArgumentException) {
            Log.d(tag, "Failed to get system property (IllegalArgumentException): ${e.message}")
        } catch (e: Exception) {
            Log.d(tag, "Failed to get system property (Exception): ${e.message}")
        }

        if (ret.isEmpty()) {
            ret = defaultValue
        }

        return ret
    }

    @SuppressLint("PrivateApi")
    fun setProperty(key: String, value: String) {
        try {
            val systemProperties = Class.forName("android.os.SystemProperties")

            // Parameters Types
            val paramTypes = arrayOf<Class<*>>(String::class.java, String::class.java)
            val setMethod = systemProperties.getMethod("set", *paramTypes)

            // Parameters
            setMethod.invoke(null, key, value) // static method doesn't need an instance
            XposedBridge.log("System property set successfully: $key = $value")
        } catch (e: IllegalArgumentException) {
            XposedBridge.log("Failed to set system property (IllegalArgumentException): ${e.message}")
        } catch (e: ClassNotFoundException) {
            XposedBridge.log("Failed to set system property (ClassNotFoundException): ${e.message}")
        } catch (e: NoSuchMethodException) {
            XposedBridge.log("Failed to set system property (NoSuchMethodException): ${e.message}")
        } catch (e: Exception) {
            XposedBridge.log("Failed to set system property (Exception): ${e.message}")
        } catch (e: Throwable) {
            XposedBridge.log("Failed to set system property (Exception): ${e.message}")
        }
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    @JvmStatic
    fun getClass(clazzOrClassName: Any, classLoader: ClassLoader): Class<*>? {
        require(clazzOrClassName is Class<*> || (clazzOrClassName is String && clazzOrClassName.isNotEmpty())) {
            "Unsupported type: ${clazzOrClassName::class.simpleName}"
        }

        return if (clazzOrClassName is Class<*>) {
            clazzOrClassName
        } else {
            val className = clazzOrClassName as String
            try {
                XposedHelpers.findClass(className, classLoader)
            } catch (e: Throwable) {
                try {
                    Class.forName(className)
                } catch (e: Throwable) {
                    try {
                        Class.forName(className, false, classLoader)
                    } catch (e: Throwable) {
                        null
                    }
                }
            }
        }
    }

    @JvmStatic
    fun getMethod(clazzOrClassName: Any, classLoader: ClassLoader, methodName: String, vararg parameterTypes: Any): Any? {
        val clazz = getClass(clazzOrClassName, classLoader) ?: return null

        val convertedParameterTypes = parameterTypes.mapNotNull { param ->
            when (param) {
                is Class<*> -> param
                is String -> {
                    try {
                        Class.forName(param)
                    } catch (e: Throwable) {
                        getClass(param, classLoader)
                    }
                }

                else -> null
            }
        }.toTypedArray()

        return if (methodName.isEmpty()) {
            try {
                XposedHelpers.findConstructorExact(clazz, *parameterTypes)
            } catch (e: Throwable) {
                try {
                    clazz.getConstructor(*convertedParameterTypes)
                } catch (e: Throwable) {
                    try {
                        clazz.getDeclaredConstructor(*convertedParameterTypes)
                    } catch (e: Throwable) {
                        null
                    }
                }
            }
        } else {
            try {
                XposedHelpers.findMethodExact(clazz, methodName, *parameterTypes)
            } catch (e: Throwable) {
                try {
                    clazz.getMethod(methodName, *convertedParameterTypes)
                } catch (e: Throwable) {
                    try {
                        clazz.getDeclaredMethod(methodName, *convertedParameterTypes)
                    } catch (e: Throwable) {
                        null
                    }
                }
            }
        }
    }
}
