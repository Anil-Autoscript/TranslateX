package com.translatex.ai.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.translatex.ai.ui.theme.Primary
import com.translatex.ai.ui.theme.PrimaryVariant
import kotlinx.coroutines.delay

/**
 * Animated splash screen shown once on launch.
 * Uses a pulsing scale animation on the logo text.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.85f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(2_200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Primary, PrimaryVariant))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "🌐",
                fontSize   = 72.sp,
                modifier   = Modifier.scale(scale)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text       = "TranslateX AI",
                style      = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color      = androidx.compose.ui.graphics.Color.White,
                    fontSize   = 32.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text  = "Translate the world, instantly",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f)
                )
            )
        }
    }
}
