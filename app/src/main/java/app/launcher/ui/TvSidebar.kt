package app.launcher.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class TvSection(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Apps("Apps", Icons.Default.Apps),
    Hidden("Hidden", Icons.Default.VisibilityOff),
    Settings("Settings", Icons.Default.Settings),
}

@Composable
fun TvSidebar(
    selected: TvSection,
    onSelect: (TvSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(280.dp).fillMaxHeight(),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Launcher", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            TvSection.entries.forEach { section ->
                val isSelected = section == selected
                FilledTonalButton(
                    onClick = { onSelect(section) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(section.icon, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(section.label)
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
