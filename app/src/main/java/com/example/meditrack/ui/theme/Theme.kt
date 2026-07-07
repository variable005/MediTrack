package com.example.meditrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

fun Typography.scale(factor: Float): Typography {
    return Typography(
        displayLarge = displayLarge.copy(fontSize = displayLarge.fontSize * factor, lineHeight = displayLarge.lineHeight * factor),
        displayMedium = displayMedium.copy(fontSize = displayMedium.fontSize * factor, lineHeight = displayMedium.lineHeight * factor),
        displaySmall = displaySmall.copy(fontSize = displaySmall.fontSize * factor, lineHeight = displaySmall.lineHeight * factor),
        headlineLarge = headlineLarge.copy(fontSize = headlineLarge.fontSize * factor, lineHeight = headlineLarge.lineHeight * factor),
        headlineMedium = headlineMedium.copy(fontSize = headlineMedium.fontSize * factor, lineHeight = headlineMedium.lineHeight * factor),
        headlineSmall = headlineSmall.copy(fontSize = headlineSmall.fontSize * factor, lineHeight = headlineSmall.lineHeight * factor),
        titleLarge = titleLarge.copy(fontSize = titleLarge.fontSize * factor, lineHeight = titleLarge.lineHeight * factor),
        titleMedium = titleMedium.copy(fontSize = titleMedium.fontSize * factor, lineHeight = titleMedium.lineHeight * factor),
        titleSmall = titleSmall.copy(fontSize = titleSmall.fontSize * factor, lineHeight = titleSmall.lineHeight * factor),
        bodyLarge = bodyLarge.copy(fontSize = bodyLarge.fontSize * factor, lineHeight = bodyLarge.lineHeight * factor),
        bodyMedium = bodyMedium.copy(fontSize = bodyMedium.fontSize * factor, lineHeight = bodyMedium.lineHeight * factor),
        bodySmall = bodySmall.copy(fontSize = bodySmall.fontSize * factor, lineHeight = bodySmall.lineHeight * factor),
        labelLarge = labelLarge.copy(fontSize = labelLarge.fontSize * factor, lineHeight = labelLarge.lineHeight * factor),
        labelMedium = labelMedium.copy(fontSize = labelMedium.fontSize * factor, lineHeight = labelMedium.lineHeight * factor),
        labelSmall = labelSmall.copy(fontSize = labelSmall.fontSize * factor, lineHeight = labelSmall.lineHeight * factor)
    )
}

fun getColorSchemeForTheme(themeColor: String, darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        when (themeColor) {
            "blue" -> darkColorScheme(
                primary = BluePrimaryDark,
                onPrimary = BlueOnPrimaryDark,
                primaryContainer = BluePrimaryContainerDark,
                onPrimaryContainer = BlueOnPrimaryContainerDark,
                secondary = BlueSecondaryDark,
                secondaryContainer = BlueSecondaryContainerDark,
                tertiary = BlueTertiaryDark,
                tertiaryContainer = BlueTertiaryContainerDark,
                background = DarkBackground,
                surface = DarkSurface,
                onBackground = DarkOnBackground,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                outline = DarkOutline
            )
            "purple" -> darkColorScheme(
                primary = PurplePrimaryDark,
                onPrimary = PurpleOnPrimaryDark,
                primaryContainer = PurplePrimaryContainerDark,
                onPrimaryContainer = PurpleOnPrimaryContainerDark,
                secondary = PurpleSecondaryDark,
                secondaryContainer = PurpleSecondaryContainerDark,
                tertiary = PurpleTertiaryDark,
                tertiaryContainer = PurpleTertiaryContainerDark,
                background = DarkBackground,
                surface = DarkSurface,
                onBackground = DarkOnBackground,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                outline = DarkOutline
            )
            "green" -> darkColorScheme(
                primary = GreenPrimaryDark,
                onPrimary = GreenOnPrimaryDark,
                primaryContainer = GreenPrimaryContainerDark,
                onPrimaryContainer = GreenOnPrimaryContainerDark,
                secondary = GreenSecondaryDark,
                secondaryContainer = GreenSecondaryContainerDark,
                tertiary = GreenTertiaryDark,
                tertiaryContainer = GreenTertiaryContainerDark,
                background = DarkBackground,
                surface = DarkSurface,
                onBackground = DarkOnBackground,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                outline = DarkOutline
            )
            "orange" -> darkColorScheme(
                primary = OrangePrimaryDark,
                onPrimary = OrangeOnPrimaryDark,
                primaryContainer = OrangePrimaryContainerDark,
                onPrimaryContainer = OrangeOnPrimaryContainerDark,
                secondary = OrangeSecondaryDark,
                secondaryContainer = OrangeSecondaryContainerDark,
                tertiary = OrangeTertiaryDark,
                tertiaryContainer = OrangeTertiaryContainerDark,
                background = DarkBackground,
                surface = DarkSurface,
                onBackground = DarkOnBackground,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                outline = DarkOutline
            )
            else -> DarkColorScheme
        }
    } else {
        when (themeColor) {
            "blue" -> lightColorScheme(
                primary = BluePrimary,
                onPrimary = BlueOnPrimary,
                primaryContainer = BluePrimaryContainer,
                onPrimaryContainer = BlueOnPrimaryContainer,
                secondary = BlueSecondary,
                secondaryContainer = BlueSecondaryContainer,
                tertiary = BlueTertiary,
                tertiaryContainer = BlueTertiaryContainer,
                background = LightBackground,
                surface = LightSurface,
                onBackground = LightOnBackground,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                outline = LightOutline
            )
            "purple" -> lightColorScheme(
                primary = PurplePrimary,
                onPrimary = PurpleOnPrimary,
                primaryContainer = PurplePrimaryContainer,
                onPrimaryContainer = PurpleOnPrimaryContainer,
                secondary = PurpleSecondary,
                secondaryContainer = PurpleSecondaryContainer,
                tertiary = PurpleTertiary,
                tertiaryContainer = PurpleTertiaryContainer,
                background = LightBackground,
                surface = LightSurface,
                onBackground = LightOnBackground,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                outline = LightOutline
            )
            "green" -> lightColorScheme(
                primary = GreenPrimary,
                onPrimary = GreenOnPrimary,
                primaryContainer = GreenPrimaryContainer,
                onPrimaryContainer = GreenOnPrimaryContainer,
                secondary = GreenSecondary,
                secondaryContainer = GreenSecondaryContainer,
                tertiary = GreenTertiary,
                tertiaryContainer = GreenTertiaryContainer,
                background = LightBackground,
                surface = LightSurface,
                onBackground = LightOnBackground,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                outline = LightOutline
            )
            "orange" -> lightColorScheme(
                primary = OrangePrimary,
                onPrimary = OrangeOnPrimary,
                primaryContainer = OrangePrimaryContainer,
                onPrimaryContainer = OrangeOnPrimaryContainer,
                secondary = OrangeSecondary,
                secondaryContainer = OrangeSecondaryContainer,
                tertiary = OrangeTertiary,
                tertiaryContainer = OrangeTertiaryContainer,
                background = LightBackground,
                surface = LightSurface,
                onBackground = LightOnBackground,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                outline = LightOutline
            )
            else -> LightColorScheme
        }
    }
}

@Composable
fun MediTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    textSize: String = "medium",
    themeColor: String = "teal",
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && themeColor == "teal" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getColorSchemeForTheme(themeColor, darkTheme)
    }

    val scaleFactor = when (textSize) {
        "small" -> 0.85f
        "large" -> 1.15f
        else -> 1.0f
    }
    val scaledTypography = Typography.scale(scaleFactor)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}