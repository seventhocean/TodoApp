package com.arttest.todo.ui.theme

import androidx.compose.ui.graphics.Color

// Material You 颜色系统 - Purple/Violet 种子色 (#6750A4)
// Light Mode 调色板

// 核心背景色
val mdBackground = Color(0xFFFFFBFE)        // 略带暖色的米白，不是纯白
val mdOnBackground = Color(0xFF1C1B1F)       // 带暖色的近黑色

// 主色（Primary）- 种子色
val mdPrimary = Color(0xFF6750A4)
val mdOnPrimary = Color(0xFFFFFFFF)
val mdPrimaryContainer = Color(0xFFEADDFF)
val mdOnPrimaryContainer = Color(0xFF21005D)

// 次色容器（Secondary Container）
val mdSecondaryContainer = Color(0xFFE8DEF8)
val mdOnSecondaryContainer = Color(0xFF1D192B)
val mdSecondary = Color(0xFF625B71)
val mdOnSecondary = Color(0xFFFFFFFF)

// 第三色（Tertiary）- 互补的玫瑰色
val mdTertiary = Color(0xFF7D5260)
val mdOnTertiary = Color(0xFFFFFFFF)
val mdTertiaryContainer = Color(0xFFFFD8E4)
val mdOnTertiaryContainer = Color(0xFF31111D)

// 表面色调系统（Tonal Surfaces）
val mdSurface = Color(0xFFFFFBFE)
val mdOnSurface = Color(0xFF1C1B1F)
val mdSurfaceContainer = Color(0xFFF3EDF7)           // 用于卡片背景
val mdSurfaceContainerHigh = Color(0xFFECE6F0)       // 高一级表面
val mdSurfaceContainerHighest = Color(0xFFE6E0E9)    // 最高级表面
val mdSurfaceContainerLow = Color(0xFFF0EAF4)        // 用于输入框
val mdSurfaceContainerLowest = Color(0xFFFFFBFE)     // 最低级表面
val mdSurfaceVariant = Color(0xFFE7E0EC)             // 变体表面
val mdOnSurfaceVariant = Color(0xFF49454F)           // 次要文字

// 边框和分割线
val mdOutline = Color(0xFF79747E)
val mdOutlineVariant = Color(0xFFCAC4D0)

// 错误状态
val mdError = Color(0xFFB3261E)
val mdOnError = Color(0xFFFFFFFF)
val mdErrorContainer = Color(0xFFF9DEDC)
val mdOnErrorContainer = Color(0xFF410E0B)

// 状态层透明度（State Layers）
object StateLayer {
    const val hoverOnSolid = 0.08f           // 实心元素 hover
    const val pressedOnSolid = 0.12f         // 实心元素 pressed
    const val hoveredOnTransparent = 0.08f   // 透明元素 hover
    const val focusedOnTransparent = 0.12f   // 透明元素 focus
}

// 阴影透明度
object ShadowAlpha {
    const val default = 0.15f
    const val subtle = 0.08f
    const val elevated = 0.20f
}
