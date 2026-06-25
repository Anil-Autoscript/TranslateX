package com.translatex.ai.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.translatex.ai.navigation.TranslateXNavGraph
import com.translatex.ai.ui.theme.TranslateXTheme
import com.translatex.ai.viewmodel.TranslationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: TranslationViewModel = hiltViewModel()
            val darkModePref by viewModel.darkMode.collectAsState()

            val isDark = when (darkModePref) {
                "dark"  -> true
                "light" -> false
                else    -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            TranslateXTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                TranslateXNavGraph(
                    navController = navController,
                    viewModel     = viewModel
                )
            }
        }
    }
}
