package arivihan.technologies.doubtbuzzter2.compose.ui

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import arivihan.technologies.doubtbuzzter2.R

data class AppComposeTokens(
    val primary: Color,
    val primaryDark: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val primaryTextSize: TextUnit,
    val secondaryTextSize: TextUnit,
    val tertiaryTextSize: TextUnit,
    val smallTextSize: TextUnit
)

val LocalAppComposeTokens = compositionLocalOf {
    AppComposeTokens(
        primary = Color(0xFF00838F),
        primaryDark = Color(0xFF26C6DA),
        secondary = Color(0xFFD81B60),
        background = Color.White,
        surface = Color.White,
        textPrimary = Color.Black,
        textSecondary = Color(0xFF747474),
        divider = Color(0xFFE2E2E2),
        primaryTextSize = 16.sp,
        secondaryTextSize = 15.sp,
        tertiaryTextSize = 14.sp,
        smallTextSize = 12.sp
    )
}

internal fun Context.createAppComposeTokens(
    density: androidx.compose.ui.unit.Density,
    useDarkTheme: Boolean
): AppComposeTokens {
    val isDark = useDarkTheme
    return AppComposeTokens(
        primary = Color(getColorCompat(R.color.arivihan_primary_color)),
        primaryDark = Color(getColorCompat(R.color.arivihan_secondary_color)),
        secondary = Color(getColorCompat(R.color.colorAccent)),
        background = Color(
            getColorCompat(if (isDark) R.color.bg_color_dark else R.color.white)
        ),
        surface = Color(
            getColorCompat(if (isDark) R.color.almost_black else R.color.white)
        ),
        textPrimary = Color(
            getColorCompat(if (isDark) R.color.white else R.color.black)
        ),
        textSecondary = Color(
            getColorCompat(if (isDark) R.color.white_transparent else R.color.dark_grey)
        ),
        divider = Color(
            getColorCompat(if (isDark) R.color.divider_color_dark else R.color.divider_color)
        ),
        primaryTextSize = with(density) { resources.getDimension(R.dimen.primary_text_size).toSp() },
        secondaryTextSize = with(density) { resources.getDimension(R.dimen.secondary_text_size).toSp() },
        tertiaryTextSize = with(density) { resources.getDimension(R.dimen.tertiary_text_size).toSp() },
        smallTextSize = with(density) { resources.getDimension(R.dimen.small_text_size).toSp() }
    )
}

internal fun appColorScheme(tokens: AppComposeTokens, useDarkTheme: Boolean): ColorScheme {
    return if (useDarkTheme) {
        darkColorScheme(
            primary = tokens.primary,
            onPrimary = Color.Black,
            primaryContainer = tokens.primaryDark,
            onPrimaryContainer = Color.Black,
            secondary = tokens.secondary,
            onSecondary = Color.Black,
            background = tokens.background,
            onBackground = tokens.textPrimary,
            surface = tokens.surface,
            onSurface = tokens.textPrimary,
            outline = tokens.divider
        )
    } else {
        lightColorScheme(
            primary = tokens.primary,
            onPrimary = Color.White,
            primaryContainer = tokens.primaryDark,
            onPrimaryContainer = Color.White,
            secondary = tokens.secondary,
            onSecondary = Color.White,
            background = tokens.background,
            onBackground = tokens.textPrimary,
            surface = tokens.surface,
            onSurface = tokens.textPrimary,
            outline = tokens.divider
        )
    }
}

private fun Context.getColorCompat(id: Int): Int = ResourcesCompat.getColor(resources, id, theme)
