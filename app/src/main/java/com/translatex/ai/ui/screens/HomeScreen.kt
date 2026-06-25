package com.translatex.ai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.translatex.ai.model.TranslationResult
import com.translatex.ai.ui.components.*
import com.translatex.ai.viewmodel.TranslationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel:             TranslationViewModel,
    onNavigateToHistory:   () -> Unit,
    onNavigateToSettings:  () -> Unit
) {
    val inputText        by viewModel.inputText.collectAsState()
    val sourceLang       by viewModel.sourceLang.collectAsState()
    val targetLang       by viewModel.targetLang.collectAsState()
    val translationResult by viewModel.translationResult.collectAsState()

    var showSourceSheet  by remember { mutableStateOf(false) }
    var showTargetSheet  by remember { mutableStateOf(false) }

    val context          = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isLoading = translationResult is TranslationResult.Loading

    // Handle error snackbar
    LaunchedEffect(translationResult) {
        if (translationResult is TranslationResult.Error) {
            snackbarHostState.showSnackbar(
                (translationResult as TranslationResult.Error).message
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Translate",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Outlined.History, "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Language row ─────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LanguageChip(
                    language = sourceLang,
                    onClick  = { showSourceSheet = true },
                    modifier = Modifier.weight(1f)
                )
                SwapButton(
                    onClick   = { viewModel.swapLanguages() },
                    modifier  = Modifier.padding(horizontal = 8.dp)
                )
                LanguageChip(
                    language = targetLang,
                    onClick  = { showTargetSheet = true },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Input card ───────────────────────────────────────────────
            Card(
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    OutlinedTextField(
                        value         = inputText,
                        onValueChange = viewModel::onInputChanged,
                        placeholder   = { Text("Type your text…", color = MaterialTheme.colorScheme.onSurface.copy(0.4f)) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        maxLines      = 8,
                        shape         = RoundedCornerShape(16.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedBorderColor   = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )

                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "${inputText.length}/5000",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                        )
                        Row {
                            if (inputText.isNotBlank()) {
                                IconButton(onClick = { viewModel.onInputChanged("") }) {
                                    Icon(Icons.Default.Clear, "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(onClick = {
                                val clip = (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                    .primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                if (clip.isNotBlank()) viewModel.onInputChanged(clip)
                            }) {
                                Icon(Icons.Default.ContentPaste, "Paste", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // ── Translate button ─────────────────────────────────────────
            TranslateButton(
                onClick   = viewModel::translate,
                isLoading = isLoading
            )

            // ── Output card ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = translationResult is TranslationResult.Success,
                enter   = fadeIn() + slideInVertically(),
                exit    = fadeOut()
            ) {
                val translated = (translationResult as? TranslationResult.Success)?.translatedText ?: ""
                val favorites  by viewModel.favorites.collectAsState()
                val isFav      = favorites.any { it.translatedText == translated }

                TranslationOutputCard(
                    translatedText = translated,
                    targetLanguage = targetLang,
                    isFavorite     = isFav,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Translation", translated))
                    },
                    onShare = {
                        context.startActivity(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, translated)
                            }.let { Intent.createChooser(it, "Share translation") }
                        )
                    },
                    onSpeak  = { /* TTS integration point */ },
                    onToggleFav = {
                        // The last inserted history item is auto-saved; we toggle it
                    }
                )
            }

            Spacer(Modifier.height(80.dp)) // bottom padding
        }
    }

    // ── Language bottom sheets ────────────────────────────────────────────
    if (showSourceSheet) {
        LanguageSelectorSheet(
            onLanguageSelected = { viewModel.setSourceLanguage(it) },
            onDismiss          = { showSourceSheet = false }
        )
    }
    if (showTargetSheet) {
        LanguageSelectorSheet(
            onLanguageSelected = { viewModel.setTargetLanguage(it) },
            onDismiss          = { showTargetSheet = false }
        )
    }
}
