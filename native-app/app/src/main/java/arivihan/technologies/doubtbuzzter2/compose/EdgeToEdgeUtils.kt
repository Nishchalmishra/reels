package arivihan.technologies.doubtbuzzter2.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable

/**
 * Insets for the Compose screens.
 *
 * `EdgeToEdgeHelper` puts every activity behind the system bars on all API levels, so these are
 * always the real bar insets - a Compose screen has to hold its own content clear of them.
 */
object ArivihanWindowInsets {
    val Default: WindowInsets
        @Composable
        get() = WindowInsets.systemBars

    val StatusBars: WindowInsets
        @Composable
        get() = WindowInsets.statusBars

    val NavigationBars: WindowInsets
        @Composable
        get() = WindowInsets.navigationBars
}
