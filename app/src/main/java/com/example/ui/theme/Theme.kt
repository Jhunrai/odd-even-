package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = OddCyan,
    onPrimary = Color.Black,
    primaryContainer = NavySurfaceVariant,
    onPrimaryContainer = OddCyan,
    secondary = EvenAmber,
    onSecondary = Color.Black,
    secondaryContainer = NavySurfaceVariant,
    onSecondaryContainer = EvenAmber,
    tertiary = AccentPurple,
    background = NavyDark,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    outlineVariant = NavyBorder.copy(alpha = 0.5f)
)

private val LightColorScheme = darkColorScheme(
    // We enforce an intentional sleek dark terminal look as the primary theme
    primary = OddCyan,
    onPrimary = Color.Black,
    primaryContainer = NavySurfaceVariant,
    onPrimaryContainer = OddCyan,
    secondary = EvenAmber,
    onSecondary = Color.Black,
    secondaryContainer = NavySurfaceVariant,
    onSecondaryContainer = EvenAmber,
    tertiary = AccentPurple,
    background = NavyDark,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    outlineVariant = NavyBorder.copy(alpha = 0.5f)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = NavyDark.toArgb()
                window.navigationBarColor = NavyDark.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
