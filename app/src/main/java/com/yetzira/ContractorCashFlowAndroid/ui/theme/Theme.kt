package com.yetzira.ContractorCashFlowAndroid.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTonalElevationEnabled
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BudgetBlue,
    onPrimary = OnBluePrimary,
    secondary = Color(0xFF8E8E93),
    onSecondary = Color.White,
    tertiary = Color(0xFF9AA0A6),
    onTertiary = Color.White,
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF2C2C2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2C2C2E),
    surfaceVariant = Color(0xFFF7F7FA),
    onSurfaceVariant = Color(0xFF8E8E93),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF2F2F7),
    surfaceContainerHigh = Color(0xFFEFEFF4),
    outlineVariant = Color(0xFFC6C6C8),
    error = Color(0xFFFF3B30)
)

private val DarkColorScheme = darkColorScheme(
    primary = BudgetBlueDark,
    onPrimary = Color.White,
    secondary = Color(0xFFAEAEB2),
    onSecondary = Color.White,
    tertiary = Color(0xFF8E8E93),
    onTertiary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color(0xFFE5E5EA),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFE5E5EA),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF8E8E93),
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF2C2C2E),
    surfaceContainerHigh = Color(0xFF3A3A3C),
    outlineVariant = Color(0xFF38383A),
    error = Color(0xFFFF453A)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KablanProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    CompositionLocalProvider(
        LocalRippleConfiguration provides null,
        LocalTonalElevationEnabled provides false
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = KablanProShapeScheme,
            content = content
        )
    }
}