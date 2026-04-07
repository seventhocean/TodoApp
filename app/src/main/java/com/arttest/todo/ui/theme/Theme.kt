package com.arttest.todo.ui.theme

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

// Material You 亮色主题配色
private val LightColorScheme = lightColorScheme(
    primary = mdPrimary,
    onPrimary = mdOnPrimary,
    primaryContainer = mdPrimaryContainer,
    onPrimaryContainer = mdOnPrimaryContainer,
    secondary = mdSecondary,
    onSecondary = mdOnSecondary,
    secondaryContainer = mdSecondaryContainer,
    onSecondaryContainer = mdOnSecondaryContainer,
    tertiary = mdTertiary,
    onTertiary = mdOnTertiary,
    tertiaryContainer = mdTertiaryContainer,
    onTertiaryContainer = mdOnTertiaryContainer,
    background = mdBackground,
    onBackground = mdOnBackground,
    surface = mdSurface,
    onSurface = mdOnSurface,
    surfaceContainer = mdSurfaceContainer,
    surfaceContainerHigh = mdSurfaceContainerHigh,
    surfaceContainerHighest = mdSurfaceContainerHighest,
    surfaceContainerLow = mdSurfaceContainerLow,
    surfaceContainerLowest = mdSurfaceContainerLowest,
    surfaceVariant = mdSurfaceVariant,
    onSurfaceVariant = mdOnSurfaceVariant,
    outline = mdOutline,
    outlineVariant = mdOutlineVariant,
    error = mdError,
    onError = mdOnError,
    errorContainer = mdErrorContainer,
    onErrorContainer = mdOnErrorContainer
)

// Material You 暗色主题配色
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),
    surfaceContainerLow = Color(0xFF1D1B20),
    surfaceContainerLowest = Color(0xFF141218),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC
)

@Composable
fun ToDoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // 支持 Android 12+ 动态取色
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Android 12+ 支持动态取色（Material You 核心特性）
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
            // 设置状态栏颜色与背景融合
            window.statusBarColor = Color.Transparent.toArgb()
            // 亮色主题使用深色状态栏图标
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Typography // 使用带 Google Fonts 的版本
        } else {
            simpleTypography // 旧版本使用系统字体
        },
        content = content
    )
}

// 扩展颜色访问器 - 方便访问表面色调
object MaterialYouColors {
    val surfaceContainer: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    val surfaceContainerLow: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow

    val surfaceContainerHigh: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

    val surfaceContainerHighest: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest
}
