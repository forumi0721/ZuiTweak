# Add only application-specific rules required by this app.
# Keep rules for Xposed, Compose, JNI, or reflection belong in the app that needs them.

-keep class de.robv.android.xposed.** { *; }
-keepnames class de.robv.android.xposed.** { *; }
-keepclassmembers class de.robv.android.xposed.** { *; }
-keep class kr.stonecold.** { *; }
-keepnames class kr.stonecold.** { *; }
-keepclassmembers class kr.stonecold.** { *; }
