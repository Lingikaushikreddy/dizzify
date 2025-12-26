package app.launcher.settings

import io.github.mlmgames.settings.core.annotations.CategoryDefinition
import io.github.mlmgames.settings.core.annotations.SchemaVersion
import io.github.mlmgames.settings.core.annotations.Setting
import io.github.mlmgames.settings.core.types.Dropdown
import io.github.mlmgames.settings.core.types.Toggle
import kotlinx.serialization.Serializable

@CategoryDefinition(order = 0)
object General

@CategoryDefinition(order = 1)
object Apps

@CategoryDefinition(order = 2)
object Search

@CategoryDefinition(order = 3)
object Tv

@Serializable
enum class ThemeMode { System, Light, Dark }

@Serializable
enum class SortOrder { AZ, ZA, Recent }

@Serializable
enum class SearchType { Contains, Fuzzy, StartsWith }

@SchemaVersion(version = 1)
@Serializable
data class LauncherSettings(
    @Setting(
        title = "Theme",
        category = General::class,
        type = Dropdown::class,
        key = "theme",
        options = ["System", "Light", "Dark"]
    )
    val theme: ThemeMode = ThemeMode.System,

    @Setting(
        title = "Show App Icons",
        category = General::class,
        type = Toggle::class,
        key = "show_app_icons"
    )
    val showAppIcons: Boolean = true,

    // If you plan a picker UI, this can be Toggle/TextInput/custom later; keep persisted as String for now.
    @Setting(
        title = "Icon Pack",
        description = "Applies to app icons",
        category = General::class,
        type = Dropdown::class,
        key = "icon_pack",
        options = ["Default"] // you’ll override in your UI with real installed packs
    )
    val iconPack: String = "default",

    @Setting(
        title = "Sort Order",
        category = Apps::class,
        type = Dropdown::class,
        key = "sort_order",
        options = ["A-Z", "Z-A", "Recent"]
    )
    val sortOrder: SortOrder = SortOrder.AZ,

    @Setting(
        title = "Search Type",
        category = Search::class,
        type = Dropdown::class,
        key = "search_type",
        options = ["Contains", "Fuzzy", "Starts With"]
    )
    val searchType: SearchType = SearchType.Contains,

    @Setting(
        title = "Search Aliases",
        category = Search::class,
        type = Dropdown::class,
        key = "search_aliases_mode",
        options = ["Off", "Transliteration", "Keyboard Swap", "Both"]
    )
    val searchAliasesMode: Int = 0,

    @Setting(
        title = "Include Package Names in Search",
        category = Search::class,
        type = Toggle::class,
        key = "search_include_package_names"
    )
    val searchIncludePackageNames: Boolean = false,

    @Setting(
        title = "Show Hidden Apps in Search",
        category = Search::class,
        type = Toggle::class,
        key = "show_hidden_apps_on_search"
    )
    val showHiddenAppsOnSearch: Boolean = false,

    @Setting(
        title = "Show Non-TV Apps",
        description = "Apps without LEANBACK entry",
        category = Tv::class,
        type = Toggle::class,
        key = "show_non_tv_apps"
    )
    val showNonTvApps: Boolean = false,
)