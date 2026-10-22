package com.nutrisim.india.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary             = Saffron,
    onPrimary           = Color(0xFF1A0800),
    primaryContainer    = Color(0xFF4A2000),
    onPrimaryContainer  = Color(0xFFFFDDB5),
    secondary           = AccentTeal,
    onSecondary         = Color(0xFF00363D),
    secondaryContainer  = Color(0xFF004F58),
    onSecondaryContainer= Color(0xFF80F0FF),
    tertiary            = AccentPink,
    error               = ErrorRed,
    background          = BackgroundDark,
    onBackground        = TextPrimary,
    surface             = SurfaceDark,
    onSurface           = TextPrimary,
    surfaceVariant      = CardDark,
    onSurfaceVariant    = TextSecondary,
    outline             = DividerDark
)

@Composable
fun NutriSimTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = NutriTypography,
        content     = content
    )
}
