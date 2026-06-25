package com.translatex.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.translatex.ai.data.local.entity.TranslationEntity
import com.translatex.ai.model.languageByCode
import com.translatex.ai.ui.components.EmptyState
import com.translatex.ai.viewmodel.TranslationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: TranslationViewModel,
    onBack:    () -> Unit,
    onReuse:   (TranslationEntity) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Favourites", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            EmptyState(
                "⭐",
                "No favourites yet.\nTap the heart icon on any translation to save it here.",
                Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier       = Modifier.padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites, key = { it.id }) { item ->
                    FavoriteItem(
                        entity     = item,
                        onClick    = { onReuse(item) },
                        onRemove   = { viewModel.toggleFavorite(item) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun FavoriteItem(
    entity:  TranslationEntity,
    onClick: () -> Unit,
    onRemove:() -> Unit
) {
    val srcLang = languageByCode(entity.sourceLanguageCode)
    val tgtLang = languageByCode(entity.targetLanguageCode)

    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${srcLang.flag} ${srcLang.englishName} → ${tgtLang.flag} ${tgtLang.englishName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    entity.sourceText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style    = MaterialTheme.typography.bodyMedium
                )
                Text(
                    entity.translatedText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color    = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Favorite, "Remove favourite", tint = Color(0xFFE11D48))
            }
        }
    }
}
