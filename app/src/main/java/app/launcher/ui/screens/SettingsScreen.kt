package app.launcher.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import app.launcher.LauncherViewModel
import app.launcher.ui.theme.*

sealed class SettingsCategory(
    val title: String,
    val icon: ImageVector,
    val description: String
) {
    data object Appearance : SettingsCategory(
        "Appearance",
        Icons.Outlined.Palette,
        "Theme, icon packs, layout"
    )
    data object HomeScreen : SettingsCategory(
        "Home Screen",
        Icons.Outlined.Home,
        "Favorites, rows, widgets"
    )
    data object AppDrawer : SettingsCategory(
        "App Drawer",
        Icons.Outlined.Apps,
        "Grid size, sorting, search"
    )
    data object Behavior : SettingsCategory(
        "Behavior",
        Icons.Outlined.TouchApp,
        "Gestures, animations"
    )
    data object About : SettingsCategory(
        "About",
        Icons.Outlined.Info,
        "Version, licenses, feedback"
    )
}

@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    
    var selectedCategory by remember { mutableStateOf<SettingsCategory?>(null) }
    
    val categories = listOf(
        SettingsCategory.Appearance,
        SettingsCategory.HomeScreen,
        SettingsCategory.AppDrawer,
        SettingsCategory.Behavior,
        SettingsCategory.About
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LauncherColors.DarkBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Categories list
            SettingsCategoriesList(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                modifier = Modifier
                    .width(400.dp)
                    .fillMaxHeight()
            )
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(LauncherColors.DarkSurfaceVariant)
            )
            
            // Settings content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                selectedCategory?.let { category ->
                    SettingsCategoryContent(
                        category = category,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    // Default: show first category
                    SettingsCategoryContent(
                        category = SettingsCategory.Appearance,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoriesList(
    categories: List<SettingsCategory>,
    selectedCategory: SettingsCategory?,
    onCategorySelected: (SettingsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(LauncherSpacing.lg)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(LauncherSpacing.xl))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(LauncherSpacing.sm)
        ) {
            items(categories) { category ->
                SettingsCategoryItem(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryItem(
    category: SettingsCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> LauncherColors.AccentBlue.copy(alpha = 0.3f)
            isSelected -> LauncherColors.AccentBlue.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        label = "category_bg"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        label = "category_scale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(
                if (isFocused) Modifier.border(
                    width = 2.dp,
                    color = LauncherColors.AccentBlue.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) {
                    onClick()
                    true
                } else false
            }
            .focusable()
            .padding(LauncherSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected || isFocused) 
                        LauncherColors.AccentBlue.copy(alpha = 0.2f)
                    else 
                        LauncherColors.DarkSurfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected || isFocused) 
                    LauncherColors.AccentBlue 
                else 
                    LauncherColors.TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.width(LauncherSpacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected || isFocused) Color.White else LauncherColors.TextPrimary
            )
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyMedium,
                color = LauncherColors.TextSecondary
            )
        }
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LauncherColors.AccentBlue
            )
        }
    }
}

@Composable
private fun SettingsCategoryContent(
    category: SettingsCategory,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    
    LazyColumn(
        modifier = modifier.padding(LauncherSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.md)
    ) {
        item {
            Text(
                text = category.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(LauncherSpacing.lg))
        }
        
        when (category) {
            is SettingsCategory.Appearance -> {
                item {
                    SettingsSection(title = "Theme") {
                        SettingsToggle(
                            title = "Dark Mode",
                            description = "Use dark theme (recommended for TV)",
                            isChecked = true,
                            onCheckedChange = { /* TODO */ }
                        )
                        SettingsToggle(
                            title = "Show App Icons",
                            description = "Display app icons in launcher",
                            isChecked = settings?.showAppIcons ?: true,
                            onCheckedChange = { /* TODO */ }
                        )
                    }
                }
                
                item {
                    SettingsSection(title = "Icon Pack") {
                        SettingsClickable(
                            title = "Icon Pack",
                            description = "Default", // TODO: Remove it, there are no banner packs for TVs
                            onClick = { /* TODO */ }
                        )
                    }
                }
            }
            
            is SettingsCategory.HomeScreen -> {
                item {
                    SettingsSection(title = "Favorites") {
                        SettingsClickable(
                            title = "Edit Favorites",
                            description = "Choose apps for home screen",
                            onClick = { /* TODO */ }
                        )
                    }
                }
            }
            
            is SettingsCategory.AppDrawer -> {
                item {
                    SettingsSection(title = "Search") {
                        SettingsToggle(
                            title = "Search Package Names",
                            description = "Include package names in search",
                            isChecked = settings?.searchIncludePackageNames ?: false,
                            onCheckedChange = { /* TODO */ }
                        )
                    }
                }
                
                item {
                    SettingsSection(title = "Sorting") {
                        SettingsClickable(
                            title = "Sort Order",
                            description = "Alphabetical",
                            onClick = { /* TODO */ }
                        )
                    }
                }
            }
            
            is SettingsCategory.Behavior -> {
                item {
                    SettingsSection(title = "Animations") {
                        SettingsToggle(
                            title = "Enable Animations",
                            description = "Show smooth transitions",
                            isChecked = true,
                            onCheckedChange = { /* TODO */ }
                        )
                    }
                }
            }
            
            is SettingsCategory.About -> {
                item {
                    SettingsSection(title = "App Info") {
                        SettingsInfo(
                            title = "Version",
                            value = "1.0.0"
                        )
                        SettingsInfo(
                            title = "Build",
                            value = "Release"
                        )
                    }
                }
                
                item {
                    SettingsSection(title = "Links") {
                        SettingsClickable(
                            title = "Source Code",
                            description = "View on GitHub",
                            onClick = { /* TODO */ }
                        )
                        SettingsClickable(
                            title = "Report Issue",
                            description = "Submit bug report",
                            onClick = { /* TODO */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = LauncherColors.AccentBlue,
            modifier = Modifier.padding(bottom = LauncherSpacing.sm)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = LauncherColors.DarkSurface
        ) {
            Column(
                modifier = Modifier.padding(LauncherSpacing.md),
                verticalArrangement = Arrangement.spacedBy(LauncherSpacing.sm),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) LauncherColors.DarkSurfaceVariant else Color.Transparent)
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) {
                    onCheckedChange(!isChecked)
                    true
                } else false
            }
            .focusable()
            .padding(LauncherSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = LauncherColors.TextSecondary
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LauncherColors.AccentBlue,
                checkedTrackColor = LauncherColors.AccentBlue.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun SettingsClickable(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) LauncherColors.DarkSurfaceVariant else Color.Transparent)
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) {
                    onClick()
                    true
                } else false
            }
            .focusable()
            .padding(LauncherSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = LauncherColors.TextSecondary
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = LauncherColors.TextSecondary
        )
    }
}

@Composable
private fun SettingsInfo(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(LauncherSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = LauncherColors.TextSecondary
        )
    }
}