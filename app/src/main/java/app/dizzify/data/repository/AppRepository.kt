package app.dizzify.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import app.dizzify.data.AppModel
import app.dizzify.helper.PrivateSpaceHelper
import app.dizzify.helper.getAppsList
import app.dizzify.helper.resolveUser
import app.dizzify.settings.LauncherSettings
import app.dizzify.settings.LauncherState
import app.dizzify.settings.toggleHidden
import io.github.mlmgames.settings.core.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val settingsRepo: SettingsRepository<LauncherSettings>,
    private val stateRepo: SettingsRepository<LauncherState>,
    coroutineScope: CoroutineScope
) {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    private val _appListAll = MutableStateFlow<List<AppModel>>(emptyList())
    val appListAll: StateFlow<List<AppModel>> = _appListAll.asStateFlow()

    private val _appList = MutableStateFlow<List<AppModel>>(emptyList())
    val appList: StateFlow<List<AppModel>> = _appList.asStateFlow()

    private val _hiddenApps = MutableStateFlow<List<AppModel>>(emptyList())
    val hiddenApps: StateFlow<List<AppModel>> = _hiddenApps.asStateFlow()

    init {
        // Reload apps when icon pack or icon visibility changes
        coroutineScope.launch {
            settingsRepo.flow
                .map { it.iconPack to it.showAppIcons }
                .distinctUntilChanged()
                .drop(1)
                .collect {
                    loadApps()
                    loadHiddenApps()
                }
        }

        // Reload when hidden apps set changes (so visible list updates)
        coroutineScope.launch {
            stateRepo.flow
                .map { it.hiddenApps }
                .distinctUntilChanged()
                .drop(1)
                .collect {
                    loadApps()
                    loadHiddenApps()
                }
        }
    }


    suspend fun loadApps() = withContext(Dispatchers.IO) {
        val appsVisible = getAppsList(
            context = context,
            settingsRepo = settingsRepo,
            stateRepo = stateRepo,
            includeRegularApps = true,
            includeHiddenApps = false
        )

        // "All apps" (including hidden) is useful for search + internal mapping
        _appListAll.value = getAppsList(
            context = context,
            settingsRepo = settingsRepo,
            stateRepo = stateRepo,
            includeRegularApps = true,
            includeHiddenApps = true
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val privateSpaceHelper = PrivateSpaceHelper(context)
            if (privateSpaceHelper.isPrivateSpaceLocked()) {
                val privateSpaceUser = privateSpaceHelper.getPrivateSpaceUser()
                if (privateSpaceUser != null) {
                    _appList.value = appsVisible.filter { it.user != privateSpaceUser }
                    return@withContext
                }
            }
        }

        _appList.value = appsVisible
    }

    suspend fun loadHiddenApps() = withContext(Dispatchers.IO) {
        _hiddenApps.value = getAppsList(
            context = context,
            settingsRepo = settingsRepo,
            stateRepo = stateRepo,
            includeRegularApps = false,
            includeHiddenApps = true
        )
    }

    suspend fun toggleAppHidden(app: AppModel) = withContext(Dispatchers.IO) {
        val appKey = app.getKey()
        stateRepo.toggleHidden(appKey)
        // observers will reload lists, but we can also eager-refresh:
        loadApps()
        loadHiddenApps()
    }

    suspend fun launchApp(appModel: AppModel) = withContext(Dispatchers.Main) {
        try {
            val cls = appModel.activityClassName?.takeIf { it.isNotBlank() }
                ?: throw AppLaunchException("Missing activityClassName for ${appModel.appLabel}")

            val component = ComponentName(appModel.appPackage, cls)

            val user = appModel.resolveUser(context)

            launcherApps.startMainActivity(component, user, null, null)
        } catch (e: SecurityException) {
            throw AppLaunchException("Security error launching ${appModel.appLabel}", e)
        } catch (e: Exception) {
            throw AppLaunchException("Failed to launch ${appModel.appLabel}", e)
        }
    }

    class AppLaunchException(message: String, cause: Throwable? = null) : Exception(message, cause)
}