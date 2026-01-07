package com.lura.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LuraIndigo,
    onPrimary = Color.White,
    secondary = LuraSlate,
    onSecondary = LuraGhostWhite,
    tertiary = Pink80,
    background = LuraObsidian,
    surface = LuraObsidian,
    onBackground = LuraGhostWhite,
    onSurface = LuraGhostWhite,
    secondaryContainer = LuraSlate.copy(alpha = 0.3f),
    onSecondaryContainer = LuraGhostWhite,
    surfaceVariant = LuraSlate.copy(alpha = 0.2f),
    onSurfaceVariant = LuraGhostWhite
)

private val LightColorScheme = lightColorScheme(
    primary = LuraIndigo,
    onPrimary = Color.White,
    secondary = LuraSlate,
    onSecondary = Color.White,
    tertiary = Pink40,
    background = LuraGhostWhite,
    surface = LuraGhostWhite,
    onBackground = LuraObsidian,
    onSurface = LuraObsidian,
    secondaryContainer = LuraSlate.copy(alpha = 0.1f),
    onSecondaryContainer = LuraObsidian,
    surfaceVariant = LuraSlate.copy(alpha = 0.1f),
    onSurfaceVariant = LuraObsidian
)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */

@Composable
fun LuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to enforce Lura aesthetics
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
