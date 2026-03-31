package com.yetzira.ContractorCashFlowAndroid.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BudgetBlue,
    onPrimary = OnBluePrimary,
    secondary = SecondaryLight,
    onSecondary = Color.White,
    tertiary = TertiaryLight,
    onTertiary = Color.White,
    background = Color(0xFFF2F2F7),       // iOS systemGroupedBackground
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),           // iOS systemBackground
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF2F2F7),    // iOS secondarySystemGroupedBackground
    onSurfaceVariant = Color(0xFF8E8E93),
    outlineVariant = Color(0xFFC6C6C8),    // iOS separator
    error = Color(0xFFFF3B30)
)

private val DarkColorScheme = darkColorScheme(
    primary = BudgetBlueDark,
    onPrimary = Color.White,
    secondary = SecondaryDark,
    onSecondary = Color.White,
    tertiary = TertiaryDark,
    onTertiary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF8E8E93),
    outlineVariant = Color(0xFF38383A),
    error = Color(0xFFFF453A)
)

@Composable
fun KablanProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // Disabled so BudgetBlue palette always shows
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = KablanProShapeScheme,
        content = content
    )
}