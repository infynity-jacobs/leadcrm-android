package com.infynity.leadcrm.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Infynity CRM design system
 *
 * Primary: teal/green
 * Accent: blue
 * Surfaces: clean white / cool light gray
 * Text: dark slate
 */

private val InfynityPrimary = Color(0xFF1B4FD1)

// CRM semantic colours

val LeadNew = Color(0xFF2563EB)
val LeadContacted = Color(0xFF0891B2)
val LeadFollowUp = Color(0xFFF59E0B)
val LeadInterested = Color(0xFF16A34A)
val LeadConverted = Color(0xFF7C3AED)
val LeadLost = Color(0xFF64748B)

val TaskPending = Color(0xFFF59E0B)
val TaskCompleted = Color(0xFF16A34A)
val TaskCancelled = Color(0xFFDC2626)

val CalendarMeeting = Color(0xFF2563EB)
val CalendarCall = Color(0xFF0891B2)
val CalendarFollowUp = Color(0xFFF59E0B)
val CalendarInstallation = Color(0xFF16A34A)
val CalendarPayment = Color(0xFF7C3AED)

private val InfynityPrimaryDark = Color(0xFF6F8FE8)

private val InfynityAccent = Color(0xFF3B82F6)
private val InfynityAccentDark = Color(0xFF60A5FA)

private val LightBackground = Color(0xFFF8FAFC)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFF1F5F9)

private val DarkBackground = Color(0xFF0F172A)
private val DarkSurface = Color(0xFF1E293B)
private val DarkSurfaceVariant = Color(0xFF334155)

private val DarkText = Color(0xFF1F2937)
private val LightText = Color(0xFFF8FAFC)
private val MutedText = Color(0xFF64748B)

private val LightScheme = lightColorScheme(
    primary = InfynityPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = Color(0xFF123B91),

    secondary = InfynityAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A8A),

    tertiary = Color(0xFF8B5CF6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF4C1D95),

    background = LightBackground,
    onBackground = DarkText,

    surface = LightSurface,
    onSurface = DarkText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = MutedText,

    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),

    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),

    scrim = Color(0x66000000)
)

private val DarkScheme = darkColorScheme(
    primary = InfynityPrimaryDark,
    onPrimary = Color(0xFF071B4A),
    primaryContainer = Color(0xFF123B91),
    onPrimaryContainer = Color(0xFFC5D5FF),

    secondary = InfynityAccentDark,
    onSecondary = Color(0xFF002F6C),
    secondaryContainer = Color(0xFF1E40AF),
    onSecondaryContainer = Color(0xFFDBEAFE),

    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF2E1065),
    tertiaryContainer = Color(0xFF5B21B6),
    onTertiaryContainer = Color(0xFFEDE9FE),

    background = DarkBackground,
    onBackground = LightText,

    surface = DarkSurface,
    onSurface = LightText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),

    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),

    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF475569),

    scrim = Color(0x99000000)
)

private val InfynityShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

private val InfynityTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = TextStyle(
        fontSize = 23.sp,
        lineHeight = 29.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium
    )
)

@Composable
fun InfynityCrmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = if (darkTheme) {
        DarkScheme
    } else {
        LightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = InfynityTypography,
        shapes = InfynityShapes,
        content = content
    )
}
