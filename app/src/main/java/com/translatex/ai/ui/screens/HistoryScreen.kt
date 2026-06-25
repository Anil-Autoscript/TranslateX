package com.translatex.ai.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.translatex.ai.data.local.entity.TranslationEntity
import com.translatex.ai.model.languageByCode
import com.translatex.ai.ui.components.EmptyState
import com.translatex.ai.viewmodel.TranslationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: TranslationViewModel,
    onBack:    () -> Unit,
    onReuse:   (TranslationEntity) -> Unit
) {
    val history by viewModel.history.collectAsState()
    var query   by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(query) { viewModel.setHistoryQuery(query) }

    Scaffold(
        topBar = {
            TopAppBar(
                title        = { Text("History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Outlined.DeleteSweep, "Clear history")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                placeholder   = { Text("Search history…") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = if (query.isNotBlank()) {
                    { IconButton(onClick = { query = "" }) { Icon(Icons.Default.Clear, null) } }
                } else null,
                singleLine    = true,
                shape         = RoundedCornerShape(16.dp),
                modifier      = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            if (history.isEmpty()) {
                EmptyState("📋", "No translations yet.\nStart translating to see your history here.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(history, key = { it.id }) { item ->
                        SwipeToDismissBox(
                            state = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissVal ->
                                    if (dismissVal == EndToStart) {
                                        viewModel.deleteTranslation(item)
                                        true
                                    } else false
                                }
                            ),
                            backgroundContent = {
                                Box(
                                    modifier          = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    contentAlignment  = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444))
                                }
                            },
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            HistoryItem(
                                entity      = item,
                                onClick     = { onReuse(item) },
                                onFavorite  = { viewModel.toggleFavorite(item) }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title  = { Text("Clear history?") },
            text   = { Text("Favourited items will be kept.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun HistoryItem(
    entity:    TranslationEntity,
    onClick:   () -> Unit,
    onFavorite:() -> Unit
) {
    val srcLang = languageByCode(entity.sourceLanguageCode)
    val tgtLang = languageByCode(entity.targetLanguageCode)
    val dateStr = remember(entity.timestamp) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(entity.timestamp))
    }

    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier  = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Text(
                    "${srcLang.flag} → ${tgtLang.flag}  ${srcLang.englishName} → ${tgtLang.englishName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onFavorite, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (entity.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint        = if (entity.isFavorite) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurface.copy(0.5f),
                        modifier    = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                entity.sourceText,
                style    = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                entity.translatedText,
                style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color    = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
            )
        }
    }
}
