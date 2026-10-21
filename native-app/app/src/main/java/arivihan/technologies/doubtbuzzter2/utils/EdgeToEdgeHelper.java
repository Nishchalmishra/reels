package arivihan.technologies.doubtbuzzter2.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;

import java.util.List;

/**
 * Draws every screen edge to edge.
 *
 * <p>Applied to all activities from {@code ActivityLifecycleListener#onActivityCreated}, so screens
 * do not have to opt in one by one. The system bars are made transparent and the activity's own
 * layout is padded by whatever the bars currently cover, so its background — a header colour, a
 * gradient, a toolbar — keeps painting behind the bars while its content stays clear of them.
 *
 * <p>Because the padding is driven by the live insets, screens that hide the bars (immersive video,
 * fullscreen web view) collapse to zero padding on their own — they need no special casing.
 */
public final class EdgeToEdgeHelper {

    /**
     * Screens that position their own content against the window edges — Compose screens that
     * consume {@code WindowInsets} in their layout, for example. They still get transparent system
     * bars, but {@link #apply(Activity)} leaves their content root alone so insets are not applied
     * twice.
     */
    public interface SelfManagedInsets {
    }

    /** The area the system bars (and a display cutout) can cover. */
    private static final int SAFE_AREA =
            WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();

    /**
     * The keyboard.
     *
     * <p>Drawing edge to edge means the window no longer resizes itself when the keyboard opens —
     * {@code android:windowSoftInputMode="adjustResize"} only applies while the decor fits the
     * system windows, which is exactly what edge to edge turns off. The keyboard arrives as an
     * inset instead, and padding for it is what puts the resizing back.
     */
    private static final int KEYBOARD = WindowInsetsCompat.Type.ime();

    private EdgeToEdgeHelper() {
    }

    /** Configures transparent system bars before the activity creates its content view. */
    public static void prepareWindow(@NonNull Activity activity) {
        // The bar icons follow what each theme already declares, falling back to the theme's own
        // lightness, so a screen on a dark theme keeps light icons.
        boolean lightTheme = resolveFlag(activity, androidx.appcompat.R.attr.isLightTheme, true);
        boolean lightStatusBar =
                resolveFlag(activity, android.R.attr.windowLightStatusBar, lightTheme);
        boolean lightNavigationBar =
                resolveFlag(activity, android.R.attr.windowLightNavigationBar, lightTheme);

        if (activity instanceof ComponentActivity) {
            EdgeToEdge.enable(
                    (ComponentActivity) activity,
                    lightStatusBar
                            ? SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                            : SystemBarStyle.dark(Color.TRANSPARENT),
                    // The navigation bar keeps the platform scrims, which is what keeps the
                    // buttons readable on the API levels that cannot enforce contrast themselves.
                    lightNavigationBar
                            ? SystemBarStyle.light(
                                    EdgeToEdge.getDefaultLightScrim(),
                                    EdgeToEdge.getDefaultDarkScrim())
                            : SystemBarStyle.dark(EdgeToEdge.getDefaultDarkScrim()));
        } else {
            // ActivityLifecycleCallbacks also sees activities supplied by libraries. They do not
            // all extend ComponentActivity, but they should not reintroduce an opaque system-bar
            // band just because they use the platform Activity base class.
            Window window = activity.getWindow();
            WindowCompat.setDecorFitsSystemWindows(window, false);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
            WindowInsetsControllerCompat controller =
                    WindowCompat.getInsetsController(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(lightStatusBar);
            controller.setAppearanceLightNavigationBars(lightNavigationBar);
        }
    }

    /** Configures the window and applies the appropriate inset strategy to the content root. */
    public static void apply(@NonNull Activity activity) {
        prepareWindow(activity);

        if (activity instanceof SelfManagedInsets) return;

        padForSystemBars(contentRoot(activity));
    }

    public static void applyToDialog(@Nullable Dialog dialog) {
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window == null) return;

        // Off on every API level, so the padding below is applied exactly once: the platform stops
        // insetting the decor itself and hands the insets down instead.
        WindowCompat.setDecorFitsSystemWindows(window, false);
        // Ignored from API 35 on, where the bars are always transparent; below that this is what
        // stops an opaque band being painted over the dialog.
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        padForSystemBars(contentRoot(window.findViewById(android.R.id.content)));
    }

    /**
     * The activity's own layout, rather than the frame it was added to.
     *
     * <p>The frame draws nothing, so padding it would inset the layout as a whole and leave the
     * window background showing as a band above and below. Padding the layout instead lets the
     * layout's own background run behind the bars.
     *
     * <p>Falls back to the frame for the screens that do not have exactly one layout in it —
     * there is no single view to pad there, and the frame at least keeps content out from under
     * the bars.
     */
    @Nullable
    private static View contentRoot(@NonNull Activity activity) {
        return contentRoot(activity.findViewById(android.R.id.content));
    }

    @Nullable
    private static View contentRoot(@Nullable View content) {
        if (content instanceof ViewGroup && ((ViewGroup) content).getChildCount() == 1) {
            return ((ViewGroup) content).getChildAt(0);
        }
        return content;
    }

    /** Keeps {@code view}'s content clear of the system bars by padding it with their insets. */
    public static void padForSystemBars(@Nullable View view) {
        if (view == null) return;

        // What the layout asks for itself. The bars are added on top of it rather than replacing
        // it, so a layout that declares its own padding keeps it.
        final int left = view.getPaddingLeft();
        final int top = view.getPaddingTop();
        final int right = view.getPaddingRight();
        final int bottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (paddedView, insets) -> {
            Insets safeArea = insets.getInsets(SAFE_AREA);
            paddedView.setPadding(
                    left + safeArea.left,
                    top + safeArea.top,
                    right + safeArea.right,
                    bottom + bottomInset(insets, safeArea));

            // Hand children the remaining insets; the root has already accounted for the safe
            // area, so nested views do not pad for it again.
            return new WindowInsetsCompat.Builder(insets)
                    .setInsets(SAFE_AREA, Insets.NONE)
                    .setInsets(KEYBOARD, Insets.NONE)
                    .build();
        });
        ViewCompat.setWindowInsetsAnimationCallback(view, new WindowInsetsAnimationCompat.Callback(
                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
            @NonNull
            @Override
            public WindowInsetsCompat onProgress(
                    @NonNull WindowInsetsCompat insets,
                    @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                return insets;
            }
        });

        ViewCompat.requestApplyInsets(view);
    }


    /**
     * Edge to edge for a screen whose own artwork fills the top of the window.
     *
     * <p>Nothing is padded at the top, so the artwork draws behind the status bar the way the
     * designs have it. {@code bottomBars} — the views pinned to the bottom of the screen — grow by
     * the navigation bar inset instead, which keeps their background running to the bottom edge
     * with their content above it. {@code topOverlays} are the controls sitting on the artwork, a
     * mute toggle or a back button, that would otherwise end up under the status bar; their top
     * margin grows by it. Nulls are skipped, so a screen can pass a view it only sometimes has.
     *
     * <p>The activity has to implement {@link SelfManagedInsets}, or {@link #apply(Activity)} pads
     * the layout as well and the artwork ends up below the bar again.
     */
    public static void applyArtworkEdgeToEdge(
            @NonNull View root, @Nullable View[] bottomBars, @Nullable View[] topOverlays) {
        applyArtworkEdgeToEdge(root, bottomBars, topOverlays, null);
    }

    /**
     * Edge-to-edge artwork handling with an optional field to reveal when the keyboard opens.
     * The extra behavior is kept here so screens do not install a second, competing insets
     * listener just to scroll a focused field above the IME.
     */
    public static void applyArtworkEdgeToEdge(
            @NonNull View root,
            @Nullable View[] bottomBars,
            @Nullable View[] topOverlays,
            @Nullable View imeScrollTarget) {
        final View[] bars = bottomBars == null ? new View[0] : bottomBars;
        final View[] overlays = topOverlays == null ? new View[0] : topOverlays;

        // What each view asks for itself; the bars are added on top rather than replacing it.
        final int[] barPadding = new int[bars.length];
        final int[] barMargin = new int[bars.length];
        for (int i = 0; i < bars.length; i++) {
            if (bars[i] == null) continue;
            barPadding[i] = bars[i].getPaddingBottom();
            barMargin[i] = bottomMargin(bars[i]);
        }
        final int[] overlayMargin = new int[overlays.length];
        for (int i = 0; i < overlays.length; i++) {
            if (overlays[i] != null) overlayMargin[i] = marginParams(overlays[i]).topMargin;
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets safeArea = insets.getInsets(SAFE_AREA);

            int bottomInset = bottomInset(insets, safeArea);
            for (int i = 0; i < bars.length; i++) {
                liftAboveBottomBars(bars[i], barPadding[i], barMargin[i], bottomInset);
            }
            for (int i = 0; i < overlays.length; i++) {
                View overlay = overlays[i];
                if (overlay == null) continue;
                ViewGroup.MarginLayoutParams params = marginParams(overlay);
                params.topMargin = overlayMargin[i] + safeArea.top;
                overlay.setLayoutParams(params);
            }

            if (imeScrollTarget != null
                    && view instanceof NestedScrollView
                    && imeScrollTarget.hasFocus()) {
                int imeBottom = insets.getInsets(KEYBOARD).bottom;
                if (imeBottom > 0) {
                    NestedScrollView scrollView = (NestedScrollView) view;
                    int margin = (int) (24 * view.getResources().getDisplayMetrics().density + 0.5f);
                    view.post(() -> {
                        Rect targetRect = new Rect();
                        imeScrollTarget.getDrawingRect(targetRect);
                        scrollView.offsetDescendantRectToMyCoords(imeScrollTarget, targetRect);

                        int desiredBottom = scrollView.getHeight() - imeBottom - margin;
                        if (targetRect.bottom > desiredBottom) {
                            scrollView.smoothScrollBy(0, targetRect.bottom - desiredBottom);
                        }
                    });
                }
            }

            return new WindowInsetsCompat.Builder(insets)
                    .setInsets(SAFE_AREA, Insets.NONE)
                    .setInsets(KEYBOARD, Insets.NONE)
                    .build();
        });
        ViewCompat.requestApplyInsets(root);
    }

    public static int bottomInset(@NonNull WindowInsetsCompat insets, @NonNull Insets safeArea) {
        return Math.max(safeArea.bottom, insets.getInsets(KEYBOARD).bottom);
    }

    /**
     * Like {@link #applyArtworkEdgeToEdge(View, View[], View[])} but for activities
     * that use {@code android:windowSoftInputMode="adjustPan"}.
     *
     * <p>The system handles the keyboard by panning the entire window up, so bottom bars
     * only need the navigation bar inset — not the keyboard inset. Using
     * {@link #applyArtworkEdgeToEdge} with {@code adjustPan} would double-handle the
     * keyboard (pan + pad), causing the layout to shift too far.
     */
    public static void applyArtworkEdgeToEdgeAdjustPan(
            @NonNull View root, @Nullable View[] bottomBars, @Nullable View[] topOverlays) {
        final View[] bars = bottomBars == null ? new View[0] : bottomBars;
        final View[] overlays = topOverlays == null ? new View[0] : topOverlays;

        final int[] barPadding = new int[bars.length];
        for (int i = 0; i < bars.length; i++) {
            if (bars[i] != null) barPadding[i] = bars[i].getPaddingBottom();
        }
        final int[] overlayMargin = new int[overlays.length];
        for (int i = 0; i < overlays.length; i++) {
            if (overlays[i] != null) overlayMargin[i] = marginParams(overlays[i]).topMargin;
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets safeArea = insets.getInsets(SAFE_AREA);

            // Nav bar only — adjustPan handles keyboard
            for (int i = 0; i < bars.length; i++) {
                View bar = bars[i];
                if (bar == null) continue;
                bar.setPadding(
                        bar.getPaddingLeft(), bar.getPaddingTop(),
                        bar.getPaddingRight(), barPadding[i] + safeArea.bottom);
            }
            for (int i = 0; i < overlays.length; i++) {
                View overlay = overlays[i];
                if (overlay == null) continue;
                ViewGroup.MarginLayoutParams params = marginParams(overlay);
                params.topMargin = overlayMargin[i] + safeArea.top;
                overlay.setLayoutParams(params);
            }

            return new WindowInsetsCompat.Builder(insets)
                    .setInsets(SAFE_AREA, Insets.NONE)
                    .setInsets(KEYBOARD, Insets.NONE)
                    .build();
        });
        ViewCompat.requestApplyInsets(root);
    }

    /**
     * Keeps a bottom bar clear of whatever covers the bottom of the window.
     *
     * <p>A bar that sizes itself to its content grows by the inset: padding it keeps its background
     * running to the bottom edge with its content above the bars, which is what the gradients and
     * button strips behind these screens are drawn to do.
     *
     * <p>A bar with a fixed height cannot grow that way. Padding one only squeezes its own content
     * inside the height it was given — the view itself stays where it was, under the bars — so the
     * inset has to move it instead, by way of its bottom margin. The full-screen video screens hit
     * this with a 48dp button: it kept its position over the navigation bar while its label crept
     * upwards inside it.
     */
    private static void liftAboveBottomBars(
            @Nullable View bar, int basePadding, int baseMargin, int inset) {
        if (bar == null) return;

        ViewGroup.LayoutParams params = bar.getLayoutParams();
        if (params != null && params.height >= 0
                && params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) params;
            // Only when it actually changes: setLayoutParams asks for another layout pass, and the
            // insets are dispatched again on the back of one.
            if (margins.bottomMargin != baseMargin + inset) {
                margins.bottomMargin = baseMargin + inset;
                bar.setLayoutParams(margins);
            }
            return;
        }

        bar.setPadding(
                bar.getPaddingLeft(),
                bar.getPaddingTop(),
                bar.getPaddingRight(),
                basePadding + inset);
    }

    /** The bar's own bottom margin, or zero for a parent that does not give it margins. */
    private static int bottomMargin(@NonNull View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        return params instanceof ViewGroup.MarginLayoutParams
                ? ((ViewGroup.MarginLayoutParams) params).bottomMargin
                : 0;
    }

    private static ViewGroup.MarginLayoutParams marginParams(@NonNull View view) {
        return (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    }

    private static boolean resolveFlag(
            @NonNull Activity activity, @AttrRes int attr, boolean fallback) {
        TypedValue value = new TypedValue();
        if (!activity.getTheme().resolveAttribute(attr, value, true)
                || value.type != TypedValue.TYPE_INT_BOOLEAN) {
            return fallback;
        }
        return value.data != 0;
    }
}
