package app.dizzify.platform

import android.content.Context
import android.content.pm.PackageManager

object Device {
    fun isTv(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
}
