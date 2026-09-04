package com.cliply.core.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CliplyBackground = Color(0xFF0B0F17); val CliplySurface = Color(0xFF161C28); val CliplySurface2 = Color(0xFF1E2636); val CliplyBorder = Color(0xFF273142); val CliplyPrimary = Color(0xFFFF2E63); val CliplySecondary = Color(0xFF7C3AED); val CliplyText = Color(0xFFF8FAFC); val CliplyTextSecondary = Color(0xFF94A3B8); val CliplyTextTertiary = Color(0xFF64748B); val CliplySuccess = Color(0xFF10B981); val CliplyError = Color(0xFFEF4444)
private val DarkColors = darkColorScheme(primary = CliplyPrimary, secondary = CliplySecondary, background = CliplyBackground, surface = CliplySurface, onBackground = CliplyText, onSurface = CliplyText)
@Composable fun CliplyTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = DarkColors, typography = Typography(), content = content) }
