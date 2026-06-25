package com.translatex.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.translatex.ai.viewmodel.TranslationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TranslationViewModel,
    onBack:    () -> Unit
) {
    val darkMode by viewModel.darkMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Appearance ─────────────────────────────────────────────
            SettingsSectionTitle("Appearance")

            ThemeSelector(selected = darkMode, onSelected = viewModel::setDarkMode)

            // ── Storage ────────────────────────────────────────────────
            SettingsSectionTitle("Storage")

            SettingsAction(
                icon    = Icons.Default.DeleteSweep,
                title   = "Clear history",
                summary = "Remove all non-favourited translations",
                onClick = viewModel::clearHistory
            )

            // ── About ──────────────────────────────────────────────────
            SettingsSectionTitle("About")

            SettingsInfo(title = "Version", value = "1.0.0")
            SettingsInfo(title = "App",     value = "TranslateX AI")
            SettingsInfo(title = "Backend", value = "GitHub Actions")
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun ThemeSelector(selected: String, onSelected: (String) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Theme", style = MaterialTheme.typography.titleSmall)
            listOf("system" to "System default", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = (selected == value),
                        onClick  = { onSelected(value) }
                    )
                    Text(label, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsAction(
    icon:    androidx.compose.ui.graphics.vector.ImageVector,
    title:   String,
    summary: String,
    onClick: () -> Unit
) {
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.error)
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
        }
    }
}

@Composable
private fun SettingsInfo(title: String, value: String) {
    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(
            modifier                = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement   = Arrangement.SpaceBetween,
            verticalAlignment       = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(value,  style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        }
    }
}
