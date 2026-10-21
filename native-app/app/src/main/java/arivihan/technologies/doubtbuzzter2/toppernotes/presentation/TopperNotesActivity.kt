package arivihan.technologies.doubtbuzzter2.toppernotes.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.Window
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.fragment.app.commit
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.hypot
import kotlin.math.max

/**
 * Standalone host for [TopperNotesFragment].
 *
 * Supports per-activity dark theme for PRO toppers with a circular reveal
 * animation, matching the pattern from [ChapterListSubscribedActivity].
 */
@AndroidEntryPoint
class TopperNotesActivity : AppCompatActivity(), EdgeToEdgeHelper.SelfManagedInsets {

    private var revealOverlay: View? = null
    private var currentDarkMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Restore theme state before super.onCreate to avoid flicker
        currentDarkMode = savedInstanceState?.getBoolean(KEY_DARK_MODE, false) ?: false
        applyNightMode(currentDarkMode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topper_notes)

        revealOverlay = findViewById(R.id.reveal_overlay)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topper_notes_container, TopperNotesFragment.newInstance())
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_DARK_MODE, currentDarkMode)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Update container background when uiMode changes without recreation
        val container = findViewById<View>(R.id.topper_notes_container)
        container?.setBackgroundColor(
            if (currentDarkMode) DARK_BG_COLOR else WHITE_BG_COLOR
        )
    }

    /**
     * Runs a circular reveal animation from [cx],[cy] to transition between
     * light and dark themes. Called by [TopperNotesFragment] on topper selection change.
     *
     * @param toDark true to reveal into dark theme, false for light
     * @param cx center X of the reveal origin (screen coordinates converted to overlay-local)
     * @param cy center Y of the reveal origin
     * @param onRevealEnd callback fired after the reveal animation completes
     */
    fun runCircularReveal(toDark: Boolean, cx: Int, cy: Int, onRevealEnd: () -> Unit) {
        if (currentDarkMode == toDark) {
            onRevealEnd()
            return
        }

        val overlay = revealOverlay
        if (overlay == null || !overlay.isAttachedToWindow) {
            applyNightMode(toDark)
            currentDarkMode = toDark
            onRevealEnd()
            return
        }

        overlay.setBackgroundColor(if (toDark) DARK_BG_COLOR else WHITE_BG_COLOR)

        val maxRadius = hypot(
            max(cx, overlay.width - cx).toDouble(),
            max(cy, overlay.height - cy).toDouble()
        ).toFloat()

        overlay.visibility = View.VISIBLE
        val anim = ViewAnimationUtils.createCircularReveal(overlay, cx, cy, 0f, maxRadius)
        anim.duration = REVEAL_DURATION_MS
        anim.interpolator = DecelerateInterpolator()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                currentDarkMode = toDark
                applyNightMode(toDark)
                onRevealEnd()
                overlay.postDelayed({
                    overlay.visibility = View.INVISIBLE
                }, 100)
            }
        })
        anim.start()
    }

    /**
     * Applies dark theme instantly without animation. Used on first load when the
     * initial topper is already PRO so no reveal is needed.
     */
    fun applyThemeInstant(isDark: Boolean) {
        if (currentDarkMode == isDark) return
        currentDarkMode = isDark
        applyNightMode(isDark)
    }

    private fun applyNightMode(isDark: Boolean) {
        delegate.localNightMode = if (isDark)
            AppCompatDelegate.MODE_NIGHT_YES
        else
            AppCompatDelegate.MODE_NIGHT_NO

        setSystemBarsStyle(this, !isDark)
    }

    private fun setSystemBarsStyle(activity: Activity, lightMode: Boolean) {
        val window: Window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = lightMode
        controller.isAppearanceLightNavigationBars = lightMode
    }

    private companion object {
        const val KEY_DARK_MODE = "topper_notes_dark_mode"
        const val REVEAL_DURATION_MS = 450L
        const val DARK_BG_COLOR = 0xFF141414.toInt()
        const val WHITE_BG_COLOR = 0xFFFFFFFF.toInt()
    }
}
