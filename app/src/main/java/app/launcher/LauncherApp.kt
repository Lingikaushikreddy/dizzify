package app.launcher

import android.app.Application
import app.launcher.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@LauncherApp)
            modules(appModule)
        }
    }
}