package kr.stonecold.zuitweak

import android.app.Application
import android.content.Context

class ZuiTweakApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    companion object {
        lateinit var appContext: Context
            private set

        val isAppContextInitialized: Boolean
            get() = ::appContext.isInitialized
    }
}
