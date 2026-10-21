package arivihan.technologies.doubtbuzzter2.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import arivihan.technologies.doubtbuzzter2.compose.ui.LocalAppComposeTokens
import arivihan.technologies.doubtbuzzter2.compose.ui.appFontFamily
import arivihan.technologies.doubtbuzzter2.compose.ui.appTypography
import arivihan.technologies.doubtbuzzter2.compose.ui.createAppComposeTokens
import arivihan.technologies.doubtbuzzter2.compose.ui.appColorScheme
import arivihan.technologies.doubtbuzzter2.model.ThemeEnum

@Composable
fun ArivihanComposeApp(
    enabled: Boolean = true,
    theme: ThemeEnum? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val useDark = when (theme) {
        ThemeEnum.LIGHT -> false
        ThemeEnum.DARK -> true
        null -> isSystemInDarkTheme()
    }
    val tokens = remember(enabled, density) {
        if (enabled) {
            context.createAppComposeTokens(density, useDark)
        } else {
            null
        }
    }

    val typographyTokens = tokens ?: LocalAppComposeTokens.current

    if (enabled && tokens != null) {
        CompositionLocalProvider(LocalAppComposeTokens provides tokens) {
            MaterialTheme(
                colorScheme = appColorScheme(tokens, useDark),
                typography = appTypography(appFontFamily(), tokens)
            ) { content() }
        }
    } else {
        MaterialTheme(
            colorScheme = if (useDark) darkColorScheme() else lightColorScheme(),
            typography = appTypography(appFontFamily(), typographyTokens),
            content = content
        )
    }
}
