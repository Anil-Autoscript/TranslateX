package com.translatex.ai.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.translatex.ai.model.Language
import com.translatex.ai.model.SupportedLanguages

/**
 * Bottom-sheet language picker with instant search.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorSheet(
    onLanguageSelected: (Language) -> Unit,
    onDismiss:          () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(query) {
        if (query.isBlank()) SupportedLanguages
        else SupportedLanguages.filter {
            it.englishName.contains(query, ignoreCase = true) ||
            it.nativeName.contains(query, ignoreCase = true) ||
            it.code.contains(query, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                "Select Language",
                style     = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier  = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(12.dp))

            // Search field
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                placeholder   = { Text("Search language…") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                singleLine    = true,
                shape         = RoundedCornerShape(16.dp),
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Language list
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.heightIn(max = 440.dp)
            ) {
                items(filtered, key = { it.code }) { lang ->
                    LanguageListItem(
                        language = lang,
                        onClick  = {
                            onLanguageSelected(lang)
                            onDismiss()
                        }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LanguageListItem(language: Language, onClick: () -> Unit) {
    Row(
        modifier            = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(language.flag, fontSize = 26.sp)
        Column {
            Text(
                text  = language.englishName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text  = language.nativeName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}
