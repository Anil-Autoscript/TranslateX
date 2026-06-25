package com.translatex.ai.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.translatex.ai.model.Language
import com.translatex.ai.ui.theme.Accent
import com.translatex.ai.ui.theme.Primary
import com.translatex.ai.ui.theme.PrimaryVariant

// ── Language Chip ─────────────────────────────────────────────────────────

/**
 * Tappable chip displaying a language's flag + English name.
 */
@Composable
fun LanguageChip(
    language: Language,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick        = onClick,
        modifier       = modifier,
        shape          = RoundedCornerShape(16.dp),
        color          = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier            = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(language.flag, fontSize = 18.sp)
            Text(
                text  = language.englishName,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Icon(
                imageVector        = Icons.Default.ExpandMore,
                contentDescription = "Select language",
                tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

// ── Swap Button ───────────────────────────────────────────────────────────

@Composable
fun SwapButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledIconButton(
        onClick   = onClick,
        modifier  = modifier.size(48.dp),
        colors    = IconButtonDefaults.filledIconButtonColors(
            containerColor = Primary,
            contentColor   = Color.White
        )
    ) {
        Icon(
            imageVector        = Icons.Default.SwapHoriz,
            contentDescription = "Swap languages"
        )
    }
}

// ── Gradient Translate Button ─────────────────────────────────────────────

@Composable
fun TranslateButton(
    onClick:   () -> Unit,
    isLoading: Boolean,
    modifier:  Modifier = Modifier
) {
    Button(
        onClick  = { if (!isLoading) onClick() },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape  = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(listOf(Primary, PrimaryVariant)),
                    RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color    = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        "Translating…",
                        color  = Color.White,
                        style  = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Translate,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        "Translate",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
            }
        }
    }
}

// ── Translation Output Card ───────────────────────────────────────────────

@Composable
fun TranslationOutputCard(
    translatedText: String,
    targetLanguage: Language,
    isFavorite:     Boolean,
    onCopy:         () -> Unit,
    onShare:        () -> Unit,
    onSpeak:        () -> Unit,
    onToggleFav:    () -> Unit,
    modifier:       Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = "${targetLanguage.flag}  ${targetLanguage.englishName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text  = translatedText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize   = 20.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ActionIconBtn(Icons.Outlined.ContentCopy, "Copy",  onCopy)
                ActionIconBtn(Icons.Outlined.Share,       "Share", onShare)
                ActionIconBtn(Icons.Outlined.VolumeUp,    "Speak", onSpeak)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onToggleFav) {
                    Icon(
                        imageVector        = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint               = if (isFavorite) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionIconBtn(
    icon:              androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc:       String,
    onClick:           () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector        = icon,
            contentDescription = contentDesc,
            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier           = Modifier.size(20.dp)
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────

@Composable
fun EmptyState(icon: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier              = modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(12.dp)
    ) {
        Text(icon, fontSize = 56.sp)
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}
