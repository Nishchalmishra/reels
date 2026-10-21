package arivihan.technologies.doubtbuzzter2.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewRenderProcess;
import android.webkit.WebViewRenderProcessClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;

/**
 * Keeps a full-screen WebView from stranding the user on an endless loader.
 * <p>
 * Extracted from {@code WebViewByUrlActivity}, where every part of it was driven by a field
 * report. The screens here load the same personalised-path web app and had the same three
 * gaps: a WebView hidden with {@code GONE} while loading, a reveal that depended entirely on
 * {@code onPageFinished}, and no watchdog at all — so a wedged renderer or a hung request
 * left the loader spinning with no way out but the back button.
 * <p>
 * What it does, in the order it matters:
 * <ol>
 *   <li><b>Never GONE.</b> {@link #onLoadStarted()} puts the WebView at {@code INVISIBLE}.
 *       A GONE view is skipped during measure and layout, so the WebView stays 0×0 and
 *       Chromium cannot commit a navigation into an empty viewport — the load sits at 10%
 *       until the renderer is declared unresponsive.</li>
 *   <li><b>A second reveal path.</b> {@code onPageFinished} waits for the last subresource,
 *       so one hung font or image keeps the loader over a page that mounted long ago. A probe
 *       every {@link #HEARTBEAT_INTERVAL_MS} asks the document for its state and reveals as
 *       soon as {@code #root} has children. Pages with no {@code #root} report -1 and keep
 *       the onPageFinished-only behaviour.</li>
 *   <li><b>A byte-aware watchdog.</b> A slow load must not be torn down as a stall: the
 *       deadline samples this uid's received bytes and extends while they flow, up to a hard
 *       cap. Flat bytes means genuinely stuck.</li>
 *   <li><b>Recovery.</b> One automatic rebuild ({@code recreate()} after killing the renderer
 *       process), then an in-app retry prompt. Never a hand-off to an external browser: these
 *       pages drive navigation, tokens and back-handling through their {@code AndroidInterface}
 *       JS bridge and are broken outside the app.</li>
 * </ol>
 * Wiring, from the host activity: {@link #attach(Bundle)} after the WebView is configured,
 * {@link #onLoadStarted()} around {@code loadUrl}, the {@code onPage*}/{@code onRenderProcessGone}
 * hooks from the {@code WebViewClient}, and the four lifecycle forwards.
 *
 * @see WebViewDiagnostics for the deeper instrumentation used while chasing a specific report
 */
public class WebViewLoadGuard {

    private static final String TAG = "WebViewGuard";

    /** Survives the {@code recreate()} that resets every field — same Intent instance. */
    private static final String EXTRA_RECOVERY_ATTEMPTS = "webview_guard_recovery_attempts";
    private static final int MAX_RECOVERY_ATTEMPTS = 1;

    private static final long DEFAULT_LOAD_TIMEOUT_MS = 45000;
    private static final long DEFAULT_STALL_RX_THRESHOLD_BYTES = 64 * 1024;
    private static final long DEFAULT_STALL_HARD_CAP_MS = 120000;
    private static final long DEFAULT_CANARY_TIMEOUT_MS = 6000;
    private static final long HEARTBEAT_INTERVAL_MS = 3000;

    /** Called once per load, from whichever reveal path gets there first. */
    public interface RevealCallback {
        void onRevealPage(String reason);
    }

    private final Activity activity;
    private final WebView webView;
    private final String screenTag;
    private final RevealCallback revealCallback;

    /** Anchor for the "internet is slow" notice; null suppresses it. */
    @Nullable
    private final View snackbarAnchor;

    private final Handler watchdogHandler = new Handler(Looper.getMainLooper());
    /**
     * Own handler because the canary must survive the points where the heartbeat and watchdog
     * handlers are cleared wholesale.
     */
    private final Handler canaryHandler = new Handler(Looper.getMainLooper());
    private final Handler heartbeatHandler = new Handler(Looper.getMainLooper());

    private long loadStartMs;
    /** Start of the attempt the watchdog is guarding — the hard cap counts from here. */
    private long watchdogAttemptStartMs;
    /** -1 when {@link TrafficStats} is unsupported, in which case extensions never happen. */
    private long rxBytesAtWatchdogArm = -1;

    /**
     * Whether a mounted {@code #root} may reveal the page ahead of the load callbacks. Turn it
     * off on a screen whose reveal does more than swap views — the doubt chat pushes context
     * into the page from there, and a mounted DOM is not yet proof that the page has registered
     * the JS function that receives it.
     */
    private boolean earlyRevealEnabled = true;

    private boolean revealed;
    private boolean pageLoaded;
    private boolean hasError;
    private boolean slowNetworkNoticeShown;
    private volatile int lastProgress;

    /** Any reply from the renderer at all — canary, document probe, or a page callback. */
    private boolean rendererSignOfLife;

    private int recoveryAttempts;
    /** One recovery per activity instance — the triggers must not stack. */
    private boolean recoveryTriggered;
    private boolean activityInForeground;
    /**
     * Recovery parked because it fired while the activity was not visible; {@link #onStart()}
     * replays it. A renderer spawned in the background is handed straight to the OEM freezer,
     * which wastes the single auto-recreate.
     */
    private String pendingRecoveryTrigger;
    private boolean pendingRecoveryEvenIfLoaded;

    /** @see #registerDebugRendererReceiver() — debug builds only, null in release. */
    private BroadcastReceiver debugRendererReceiver;
    private static final String DEBUG_ACTION_WEDGE = "arivihan.debug.WEDGE_RENDERER";
    private static final String DEBUG_ACTION_KILL = "arivihan.debug.KILL_RENDERER";

    private Dialog stallRetryDialog;
    /**
     * True from the moment retries are exhausted until the user acts. Kept separate from the
     * dialog instance because onStop dismisses it — this flag is what lets onStart put the
     * prompt back instead of stranding the user on a dead loader.
     */
    private boolean stallRetryPending;
    private Dialog webViewUpdateDialog;

    public WebViewLoadGuard(@NonNull Activity activity,
                            @NonNull WebView webView,
                            @NonNull String screenTag,
                            @NonNull RevealCallback revealCallback,
                            @Nullable View snackbarAnchor) {
        this.activity = activity;
        this.webView = webView;
        this.screenTag = screenTag;
        this.revealCallback = revealCallback;
        this.snackbarAnchor = snackbarAnchor;
    }

    /**
     * @see #earlyRevealEnabled — the heartbeat probe still runs either way, so a wedged
     * renderer is still detected; only the early hand-over is suppressed.
     */
    public WebViewLoadGuard setEarlyRevealEnabled(boolean enabled) {
        this.earlyRevealEnabled = enabled;
        return this;
    }

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    /**
     * Call after the WebView's settings and clients are configured, before the first load.
     *
     * @param savedInstanceState the activity's, so the recovery count survives a recreate that
     *                           went through process death as well as one that did not
     */
    public void attach(@Nullable Bundle savedInstanceState) {
        // Read from both carriers and take the larger. Either alone would do, but this count
        // is what guards against an infinite recreate() loop, so it must not be able to reset
        // to 0 if one of the two fails to carry across.
        int fromIntent = activity.getIntent() != null
                ? activity.getIntent().getIntExtra(EXTRA_RECOVERY_ATTEMPTS, 0) : 0;
        int fromState = savedInstanceState != null
                ? savedInstanceState.getInt(EXTRA_RECOVERY_ATTEMPTS, 0) : 0;
        recoveryAttempts = Math.max(fromIntent, fromState);
        activityInForeground = true;

        applyViewportSettings();
        applyRendererPriority();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            attachRenderProcessClient();
        }
        attachDownloadListener();
        registerDebugRendererReceiver();
        checkWebViewOutdated();
        log("attached, recoveryAttempts=" + recoveryAttempts);
    }

    /**
     * A response the WebView cannot render — a {@code Content-Disposition: attachment}, or an
     * unexpected content type — is dispatched to the download listener instead of to the page
     * callbacks. With none attached it is dropped on the floor: no {@code onPageFinished}, no
     * error, loader up forever. The watchdog would eventually fire, but it would read the case
     * as a network stall and the rebuild would reproduce it exactly.
     * <p>
     * Only treated as a failure while the page is still loading. After a successful load this
     * is an ordinary download the user asked for (a PDF link, say) and must not disturb them.
     */
    private void attachDownloadListener() {
        try {
            webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
                log("download dispatched: mime=" + mimeType + ", length=" + contentLength
                        + ", disposition=" + contentDisposition);
                if (!pageLoaded) {
                    onMainFrameError("content dispatched as download, not rendered: mime=" + mimeType);
                }
            });
        } catch (Exception ex) {
            log("download listener failed: " + ex);
        }
    }

    /**
     * Call from {@code WebViewClient.onSafeBrowsingHit} for a main-frame hit. Google Safe
     * Browsing blocking the page fires none of the other error callbacks, so without this the
     * block is indistinguishable from a blank screen.
     */
    public void onSafeBrowsingHit(int threatType) {
        hasError = true;
        log("safe browsing hit, threatType=" + threatType);
        registerFailureEvent("safe browsing threatType=" + threatType, "onSafeBrowsingHit");
    }

    /**
     * Debug-only. Lets a renderer stall be forced on a device where adb cannot signal the
     * isolated renderer process — which is any non-rooted phone, i.e. every test device. Both
     * triggers are app-internal calls, so no root is needed:
     * <ul>
     *   <li>{@code adb shell am broadcast -a arivihan.debug.KILL_RENDERER} → terminates the
     *       renderer → {@code onRenderProcessGone} → recovery</li>
     *   <li>{@code adb shell am broadcast -a arivihan.debug.WEDGE_RENDERER} → spins the
     *       renderer main thread → {@code onRenderProcessUnresponsive} → recovery</li>
     * </ul>
     * Never registered in a release build, so the exported receiver never ships.
     */
    private void registerDebugRendererReceiver() {
        if (!BuildConfig.DEBUG) return;
        try {
            debugRendererReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    log("debug trigger received: " + action);
                    try {
                        if (DEBUG_ACTION_KILL.equals(action)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                WebViewRenderProcess rp = webView.getWebViewRenderProcess();
                                log("debug terminate() -> " + (rp != null && rp.terminate()));
                            }
                        } else if (DEBUG_ACTION_WEDGE.equals(action)) {
                            // A wedge only recovers while a load is in flight, so clear the
                            // loaded flag first — otherwise recover() skips a finished page.
                            pageLoaded = false;
                            recoveryTriggered = false;
                            webView.evaluateJavascript("while(true){}", null);
                        }
                    } catch (Exception ex) {
                        log("debug trigger failed: " + ex);
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(DEBUG_ACTION_WEDGE);
            filter.addAction(DEBUG_ACTION_KILL);
            // Exported so an adb shell broadcast (a different uid) can reach it.
            ContextCompat.registerReceiver(activity, debugRendererReceiver, filter,
                    ContextCompat.RECEIVER_EXPORTED);
            log("debug renderer receiver registered");
        } catch (Exception ex) {
            log("debug receiver failed: " + ex);
        }
    }

    /**
     * Both default to false, which makes the WebView size the viewport from its own width and
     * ignore the page's {@code <meta name="viewport" content="width=device-width">}. These are
     * responsive builds that assume it is honoured; without this the first layout uses the
     * wrong width and the page renders at the wrong scale until corrected.
     */
    private void applyViewportSettings() {
        try {
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLoadWithOverviewMode(true);
        } catch (Exception ex) {
            log("viewport settings failed: " + ex);
        }
    }

    /**
     * Keeps the renderer at {@code IMPORTANT} priority <b>even while the WebView is not
     * visible</b>. These screens load with the WebView invisible behind a loader, and the
     * default policy waives the renderer's priority whenever the WebView is not visible — so
     * the renderer runs its entire load at the lowest priority. On an OEM that aggressively
     * starves low-priority processes (the suspected trigger for the vivo stall) that is
     * plausibly what wedges it, so we decline the waiver.
     */
    private void applyRendererPriority() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
            boolean keepImportant = MyApplication.getFirebaseRemoteConfig()
                    .getBoolean("web_view_keep_renderer_priority");
            webView.setRendererPriorityPolicy(
                    WebView.RENDERER_PRIORITY_IMPORTANT, /* waivedWhenNotVisible = */ !keepImportant);
        } catch (Exception ex) {
            log("renderer priority failed: " + ex);
        }
    }

    /**
     * A renderer that is alive but wedged — an infinite loop or a blocking sync call in the
     * page's JS — fires no error callback and never reaches onPageFinished. This is the only
     * API that reports it, and without it the case is indistinguishable from a slow network.
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void attachRenderProcessClient() {
        try {
            webView.setWebViewRenderProcessClient(new WebViewRenderProcessClient() {
                @Override
                public void onRenderProcessUnresponsive(@NonNull WebView view,
                                                        WebViewRenderProcess renderer) {
                    log("renderer unresponsive");
                    registerFailureEvent("renderer unresponsive", "onRenderProcessUnresponsive");
                    // Only while a load is in flight — a page that finished and then goes
                    // briefly unresponsive under heavy JS must not be torn down.
                    recover("renderer-unresponsive", false);
                }

                @Override
                public void onRenderProcessResponsive(@NonNull WebView view,
                                                      WebViewRenderProcess renderer) {
                    log("renderer recovered");
                }
            });
        } catch (Exception ex) {
            log("render process client failed: " + ex);
        }
    }

    // ---------------------------------------------------------------------
    // WebViewClient hooks
    // ---------------------------------------------------------------------

    /**
     * Call immediately before {@code loadUrl}, with the same URL. Arms everything, and hides
     * the WebView the one way that still lets it lay out.
     * <p>
     * Armed here and not only in {@code onPageStarted} because a main request that hangs before
     * the navigation commits never fires onPageStarted at all — the exact stall that would
     * otherwise go unguarded.
     */
    public void onLoadStarted(@Nullable String url) {
        loadStartMs = System.currentTimeMillis();
        revealed = false;
        pageLoaded = false;
        hasError = false;
        setWebViewHiddenButLaidOut();

        // loadUrl("") is a silent no-op — not one callback fires, and the renderer is perfectly
        // healthy, so neither the canary nor any error path catches it. Without this check the
        // user waits out the full watchdog and then gets a retry that reproduces it exactly.
        if (url == null || url.trim().isEmpty()) {
            log("empty url — WebView will never call back");
            hasError = true;
            registerFailureEvent("empty url — nothing to load", "onLoadStarted");
            // Straight to the prompt, skipping the automatic rebuild: a fresh WebView would be
            // handed the same empty URL and fail identically, so the retry is pure delay.
            if (!isGone()) showStallRetryDialog();
            return;
        }

        probeRendererAlive();
        scheduleWatchdog();
        startHeartbeat();
    }

    /** Call from {@code WebViewClient.onPageStarted}. */
    public void onPageStarted(String url) {
        rendererSignOfLife = true;
        revealed = false;
        pageLoaded = false;
        hasError = false;
        setWebViewHiddenButLaidOut();
        scheduleWatchdog();
        startHeartbeat();
        log("page started: " + url);
    }

    /** Call from {@code WebViewClient.onPageFinished}. */
    public void onPageFinished(String url) {
        rendererSignOfLife = true;
        reveal("page-finished");
    }

    /** Call from {@code WebChromeClient.onProgressChanged}. */
    public void onProgressChanged(int progress) {
        lastProgress = progress;
        if (progress > 0) rendererSignOfLife = true;
    }

    /**
     * For a screen that already has its own success signal — progress reaching 100, say —
     * rather than {@code onPageFinished}. Routing it through here rather than revealing
     * directly is what keeps the reveal once-only and stands the watchdog down with it.
     */
    public void revealNow(String reason) {
        rendererSignOfLife = true;
        reveal(reason);
    }

    /**
     * Call from {@code onReceivedError} / {@code onReceivedHttpError} <b>only when
     * {@code request.isForMainFrame()}</b>. A subresource failure must never blank the screen.
     */
    public void onMainFrameError(String detail) {
        hasError = true;
        log("main frame error: " + detail);
        registerFailureEvent(detail, "onReceivedError");
    }

    /**
     * Call from {@code WebViewClient.onRenderProcessGone} and return its result — returning
     * true is what stops the system from killing the app.
     */
    public boolean onRenderProcessGone(@Nullable RenderProcessGoneDetail detail) {
        hasError = true;
        boolean crashed = detail != null && detail.didCrash();
        String reason = crashed ? "renderer crashed" : "renderer killed by system (low memory)";
        log(reason);
        registerFailureEvent(reason, "onRenderProcessGone");
        // This WebView is dead and can never be reused, whether or not the page had finished,
        // so recovery runs even post-load.
        recover("renderer-gone", true);
        return true;
    }

    public boolean isPageLoaded() {
        return pageLoaded;
    }

    // ---------------------------------------------------------------------
    // Lifecycle forwards
    // ---------------------------------------------------------------------

    public void onStart() {
        activityInForeground = true;
        if (!pageLoaded && loadStartMs > 0) {
            startHeartbeat();
        }
        // onStop tears every dialog down; without this the user comes back from a task-switch
        // to a spinner that nothing is guarding.
        if (stallRetryPending) {
            showStallRetryDialog();
        }
        if (pendingRecoveryTrigger != null) {
            String trigger = pendingRecoveryTrigger;
            boolean evenIfLoaded = pendingRecoveryEvenIfLoaded;
            pendingRecoveryTrigger = null;
            log("replaying deferred recovery, trigger=" + trigger);
            recover(trigger, evenIfLoaded);
        }
    }

    public void onStop() {
        activityInForeground = false;
        // Off-screen ticking measures nothing; onStart resumes it if the load is unfinished.
        stopHeartbeat();
        dismissStallRetryDialog(); // stallRetryPending survives; onStart re-shows it
        dismissWebViewUpdateDialog();
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(EXTRA_RECOVERY_ATTEMPTS, recoveryAttempts);
    }

    public void onDestroy() {
        watchdogHandler.removeCallbacksAndMessages(null);
        canaryHandler.removeCallbacksAndMessages(null);
        stopHeartbeat();
        dismissStallRetryDialog();
        dismissWebViewUpdateDialog();
        if (debugRendererReceiver != null) {
            try {
                activity.unregisterReceiver(debugRendererReceiver);
            } catch (Exception ignored) {
            }
            debugRendererReceiver = null;
        }
    }

    /**
     * Tears the WebView — and its wedged or dead renderer — down explicitly. Matters most on
     * the recovery path: {@code recreate()} builds a fresh WebView, and the stuck one must be
     * released rather than left for GC still holding a renderer process.
     */
    public void destroyWebView() {
        try {
            if (webView.getParent() instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) webView.getParent()).removeView(webView);
            }
            webView.stopLoading();
            webView.setWebViewClient(new WebViewClient());
            webView.setWebChromeClient(null);
            webView.destroy();
        } catch (Exception ex) {
            log("destroyWebView failed: " + ex);
        }
    }

    // ---------------------------------------------------------------------
    // Reveal
    // ---------------------------------------------------------------------

    /**
     * INVISIBLE, never GONE. A GONE view is skipped during measure and layout entirely, so the
     * WebView would stay 0×0 and Chromium cannot commit a navigation into an empty viewport.
     * INVISIBLE is laid out at full size and simply not drawn, which the loader covers anyway.
     */
    private void setWebViewHiddenButLaidOut() {
        try {
            if (webView.getVisibility() != View.VISIBLE) {
                webView.setVisibility(View.INVISIBLE);
            }
        } catch (Exception ex) {
            log("could not set INVISIBLE: " + ex);
        }
    }

    /**
     * The single loader→page handover. Marks the load successful, which stands down every
     * recovery trigger: a page that is visibly up must never be torn down over a subresource
     * that is still dribbling in.
     */
    private void reveal(String reason) {
        if (revealed) return;
        revealed = true;
        pageLoaded = true;
        stopHeartbeat();
        watchdogHandler.removeCallbacksAndMessages(null);
        canaryHandler.removeCallbacksAndMessages(null);
        // The load kept running behind the retry prompt; if it lands late, the prompt has
        // nothing left to offer.
        stallRetryPending = false;
        dismissStallRetryDialog();
        log("reveal: " + reason + " after " + (System.currentTimeMillis() - loadStartMs) + "ms");
        try {
            revealCallback.onRevealPage(reason);
        } catch (Exception ex) {
            log("reveal callback failed: " + ex);
        }
    }

    // ---------------------------------------------------------------------
    // Watchdog
    // ---------------------------------------------------------------------

    private void scheduleWatchdog() {
        watchdogHandler.removeCallbacksAndMessages(null);
        rxBytesAtWatchdogArm = readUidRxBytes();
        watchdogAttemptStartMs = System.currentTimeMillis();
        watchdogHandler.postDelayed(this::onWatchdogDeadline, loadTimeoutMs());
    }

    /**
     * Deadline hit with the page still unloaded. Before recovering, ask the one signal that
     * tells "slow" apart from "stuck": whether this uid received meaningful bytes during the
     * window. A live download of the page's multi-megabyte bundle moves megabytes per window
     * and background chatter (analytics pings) stays in the low tens of KB, so the threshold
     * separates them cleanly. Extension is capped — an endless loader is worse than the prompt.
     */
    private void onWatchdogDeadline() {
        if (pageLoaded || isGone()) return;

        long rxNow = readUidRxBytes();
        long rxDelta = (rxNow >= 0 && rxBytesAtWatchdogArm >= 0)
                ? rxNow - rxBytesAtWatchdogArm : -1;
        long elapsed = System.currentTimeMillis() - watchdogAttemptStartMs;

        long rxThreshold = remoteLong("web_view_stall_rx_threshold", DEFAULT_STALL_RX_THRESHOLD_BYTES);
        long hardCap = remoteLong("web_view_stall_hard_cap", DEFAULT_STALL_HARD_CAP_MS);

        if (rxDelta >= rxThreshold && elapsed < hardCap) {
            log("watchdog extended: rxDelta=" + rxDelta + "B, elapsed=" + elapsed + "ms");
            showSlowNetworkNoticeOnce();
            rxBytesAtWatchdogArm = rxNow;
            watchdogHandler.postDelayed(this::onWatchdogDeadline, loadTimeoutMs());
            return;
        }

        hasError = true;
        log("watchdog fired: progress=" + currentProgress() + "%, rxDelta=" + rxDelta
                + "B, elapsed=" + elapsed + "ms");
        registerFailureEvent("load timeout after " + elapsed + "ms", "watchdog");
        recover("timeout", false);
    }

    /** -1 when the device does not support per-uid accounting; callers treat that as unknown. */
    private long readUidRxBytes() {
        try {
            return TrafficStats.getUidRxBytes(Process.myUid());
        } catch (Exception ex) {
            return -1;
        }
    }

    /** Once per screen: turns "why is this loader endless" into "my internet is slow". */
    private void showSlowNetworkNoticeOnce() {
        if (slowNetworkNoticeShown || snackbarAnchor == null) return;
        slowNetworkNoticeShown = true;
        try {
            CommonMethods.showPositiveSnackbar(snackbarAnchor,
                    activity.getString(R.string.webview_slow_connection), 3000);
        } catch (Exception ignored) {
        }
    }

    // ---------------------------------------------------------------------
    // Renderer canary + document probe
    // ---------------------------------------------------------------------

    /**
     * Runs a trivial expression against the WebView's initial empty document, before any
     * navigation is started. Its answer depends on nothing but the renderer process — no URL,
     * no network, no server, no page JS — so no reply means the renderer was never servicing
     * anything and every later symptom is downstream of that.
     */
    private void probeRendererAlive() {
        rendererSignOfLife = false;
        try {
            webView.evaluateJavascript("(function(){return 'alive';})();",
                    value -> rendererSignOfLife = true);
        } catch (Exception ex) {
            log("canary threw: " + ex);
        }
        scheduleCanaryDeadline();
    }

    /**
     * Field-proven on a vivo V2432: a renderer frozen by the OEM answers nothing — not the
     * canary, not one document probe, not a page callback — from the first millisecond. If no
     * sign of life has arrived by the deadline the renderer is wedged, and recovery starts now
     * rather than at the 30s system callback. False positives are unlikely because the canary's
     * reply does not depend on the network: a slow link delays the page, never the canary.
     */
    private void scheduleCanaryDeadline() {
        long timeout = remoteLong("web_view_canary_timeout", DEFAULT_CANARY_TIMEOUT_MS);
        canaryHandler.removeCallbacksAndMessages(null);
        canaryHandler.postDelayed(() -> {
            if (rendererSignOfLife || pageLoaded || recoveryTriggered || isGone()) return;
            log("no sign of renderer life within " + timeout + "ms");
            registerFailureEvent("renderer canary timeout", "canary");
            recover("renderer-canary-timeout", false);
        }, timeout);
    }

    /**
     * Ticks while a load is in flight and stops the moment it finishes. Each tick asks the
     * document what state it is actually in, which is both the second reveal path and the only
     * positive record that a stall is a stall rather than a log captured too early.
     */
    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pageLoaded || isGone()) return;
                probeDocumentState();
                heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
            }
        }, HEARTBEAT_INTERVAL_MS);
    }

    private void stopHeartbeat() {
        heartbeatHandler.removeCallbacksAndMessages(null);
    }

    private void probeDocumentState() {
        try {
            webView.evaluateJavascript(
                    "(function(){try{"
                            + "var r=document.getElementById('root');"
                            + "return JSON.stringify({"
                            + "readyState:document.readyState,"
                            + "rootChildren:r?r.childElementCount:-1"
                            + "});}catch(e){return JSON.stringify({probeError:String(e)});}})();",
                    value -> {
                        rendererSignOfLife = true; // any reply at all means it is executing
                        maybeRevealFromDocState(value);
                    });
        } catch (Exception ex) {
            log("document probe threw: " + ex);
        }
    }

    /**
     * Reveal driven by what the page <i>is</i> rather than by load callbacks: the moment the
     * SPA has put children into {@code #root} the app is on screen and the loader has nothing
     * left to wait for.
     */
    private void maybeRevealFromDocState(String evaluateJsResult) {
        if (!earlyRevealEnabled || revealed || recoveryTriggered || hasError) return;
        try {
            // evaluateJavascript hands back a JSON value and the probe JSON.stringifys its own
            // reply, so this is a JSON string literal wrapping a JSON object — decode twice.
            Object unwrapped = new org.json.JSONTokener(evaluateJsResult).nextValue();
            if (!(unwrapped instanceof String)) return;
            org.json.JSONObject doc = new org.json.JSONObject((String) unwrapped);
            if (doc.optInt("rootChildren", -1) > 0) {
                reveal("dom-mounted, readyState=" + doc.optString("readyState"));
            }
        } catch (Exception ignored) {
            // Nothing to add: an unparseable reply is still a sign of life, already recorded.
        }
    }

    // ---------------------------------------------------------------------
    // Recovery
    // ---------------------------------------------------------------------

    /**
     * Single funnel for every way the renderer can strand the load — the watchdog, the canary,
     * {@code onRenderProcessUnresponsive}, and {@code onRenderProcessGone}.
     * <p>
     * First stall on this launch: kill the renderer process, then {@code recreate()}. Second
     * stall (the count survives the recreate): stop auto-cycling and put the choice to the user.
     *
     * @param recoverEvenIfLoaded true only for {@code onRenderProcessGone}, where a finished
     *                            page is still left with a dead, blank WebView
     */
    private void recover(String trigger, boolean recoverEvenIfLoaded) {
        if (!MyApplication.getFirebaseRemoteConfig().getBoolean("web_view_stall_recovery_enable")) {
            log("recovery disabled by remote config, trigger=" + trigger);
            return;
        }
        if (pageLoaded && !recoverEvenIfLoaded) return;
        if (isGone()) return;
        if (recoveryTriggered) return;

        // Recovery while backgrounded is a wasted bullet: an OEM that freezes a background
        // app's renderer hands the replacement one straight to the freezer, burning the single
        // auto-recreate for nothing. Parked instead, and replayed by onStart.
        if (!activityInForeground) {
            if (pendingRecoveryTrigger == null) { // first trigger wins, later ones add nothing
                pendingRecoveryTrigger = trigger;
                pendingRecoveryEvenIfLoaded = recoverEvenIfLoaded;
                log("recovery deferred until foreground, trigger=" + trigger);
            }
            return;
        }
        recoveryTriggered = true;

        stopHeartbeat();
        watchdogHandler.removeCallbacksAndMessages(null);
        canaryHandler.removeCallbacksAndMessages(null);
        log("recovery trigger=" + trigger + ", attemptsSoFar=" + recoveryAttempts);

        if (recoveryAttempts >= MAX_RECOVERY_ATTEMPTS) {
            // No external-browser fallback: the page needs this screen's AndroidInterface
            // bridge for navigation, tokens and back handling, and is broken in Chrome.
            log("retry exhausted -> in-app retry prompt");
            showStallRetryDialog();
            return;
        }

        int nextAttempt = recoveryAttempts + 1;
        if (activity.getIntent() != null) {
            activity.getIntent().putExtra(EXTRA_RECOVERY_ATTEMPTS, nextAttempt); // survives recreate()
        }
        terminateWedgedRenderer("recovery trigger=" + trigger);
        // Posted so it never runs inside a WebView client callback (onRenderProcessGone).
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!isGone()) activity.recreate();
        });
    }

    /**
     * Kills the renderer <b>process</b>, not just the WebView. Required, not belt-and-braces:
     * after a stall-triggered recreate() the brand-new WebView was wedged from its first
     * millisecond too, because Android binds every WebView in the app to the <i>same</i>
     * renderer service — recreate() had simply reattached to the frozen process. SIGKILL is the
     * one thing an OEM freezer cannot block, and with the process gone the next WebView is
     * forced to spawn a fresh one.
     * <p>
     * Fires {@code onRenderProcessGone} as a side effect; the {@code recoveryTriggered} guard
     * keeps that from re-entering recovery.
     */
    private void terminateWedgedRenderer(String context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;
            WebViewRenderProcess rp = webView.getWebViewRenderProcess();
            boolean terminated = rp != null && rp.terminate();
            log(context + " -> terminate()=" + terminated);
        } catch (Exception ex) {
            log(context + " -> terminate failed: " + ex);
        }
    }

    /**
     * Terminal state after auto-recovery is spent: a blocking prompt with exactly two exits —
     * retry, which rebuilds the screen (fresh WebView, fresh renderer, counter left at max so
     * the next stall comes straight back here rather than auto-cycling), or close, which leaves
     * the screen. Deliberately not cancelable: dismissing it would strand the user on a loader
     * that no watchdog is guarding any more.
     * <p>
     * The underlying load is left running while the prompt is up — if the page lands late,
     * {@link #reveal} clears the prompt and the user simply gets the page.
     */
    private void showStallRetryDialog() {
        if (isGone()) return;
        stallRetryPending = true;
        try {
            dismissStallRetryDialog();
            stallRetryDialog = new Dialog(activity);
            stallRetryDialog.setContentView(R.layout.dialog_alert_title_subtitle_button);
            stallRetryDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            stallRetryDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            stallRetryDialog.setCancelable(false);
            stallRetryDialog.setCanceledOnTouchOutside(false);

            TextView title = stallRetryDialog.findViewById(R.id.title);
            TextView subtitle = stallRetryDialog.findViewById(R.id.subtitle);
            TextView btnText = stallRetryDialog.findViewById(R.id.btnText);
            CardView cvConfirm = stallRetryDialog.findViewById(R.id.cv_okay);
            ImageView ivClose = stallRetryDialog.findViewById(R.id.iv_close);

            title.setText(activity.getString(R.string.webview_retry_title));
            title.setVisibility(View.VISIBLE);
            subtitle.setText(activity.getString(R.string.webview_retry_subtitle));
            subtitle.setVisibility(View.VISIBLE);
            btnText.setText(activity.getString(R.string.webview_retry_button));
            btnText.setVisibility(View.VISIBLE);

            cvConfirm.setOnClickListener(v -> {
                log("user chose retry from stall prompt");
                stallRetryPending = false;
                dismissStallRetryDialog();
                // Without the kill, the rebuilt WebView rebinds to the same frozen renderer
                // process and the retry is doomed before it starts.
                terminateWedgedRenderer("user retry");
                if (!isGone()) activity.recreate();
            });

            ivClose.setOnClickListener(v -> {
                log("user closed stall prompt, leaving screen");
                stallRetryPending = false;
                dismissStallRetryDialog();
                activity.finish();
            });

            stallRetryDialog.show();
        } catch (Exception ex) {
            log("stall retry dialog failed: " + ex);
        }
    }

    private void dismissStallRetryDialog() {
        // Leaves stallRetryPending alone on purpose: onStop dismisses every dialog, and the
        // flag is what lets onStart bring this one back.
        try {
            if (stallRetryDialog != null && stallRetryDialog.isShowing()) {
                stallRetryDialog.dismiss();
            }
        } catch (Exception ignored) {
        }
        stallRetryDialog = null;
    }

    // ---------------------------------------------------------------------
    // Outdated WebView provider
    // ---------------------------------------------------------------------

    /**
     * The web bundles these screens load are built for modern Chromium (ES2020+ syntax). A
     * WebView provider older than {@code web_view_min_supported_version} throws a SyntaxError
     * while parsing the bundle and renders a blank page — no error callback fires, so without
     * this check the user just sees an empty screen. Seen in the field on devices stuck on
     * Chrome 70 (2018) as their WebView provider.
     */
    private void checkWebViewOutdated() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
            PackageInfo info = WebView.getCurrentWebViewPackage();
            if (info == null || info.versionName == null) return;

            long minVersion = remoteLong("web_view_min_supported_version", 80);
            int major = parseWebViewMajorVersion(info.versionName);
            if (major <= 0 || major >= minVersion) return;

            log("outdated webview provider " + info.packageName + " v" + info.versionName
                    + " below min " + minVersion);
            registerFailureEvent("outdated webview " + info.packageName + " v" + info.versionName,
                    "checkWebViewOutdated");
            showWebViewUpdateDialog(info.packageName);
        } catch (Exception ex) {
            log("outdated webview check failed: " + ex);
        }
    }

    private int parseWebViewMajorVersion(String versionName) {
        try {
            int dot = versionName.indexOf('.');
            return Integer.parseInt(dot > 0 ? versionName.substring(0, dot) : versionName);
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * Cancelable on purpose: the page still attempts to load underneath, so a user who
     * dismisses the prompt loses nothing over the previous behaviour.
     */
    private void showWebViewUpdateDialog(String providerPackage) {
        try {
            dismissWebViewUpdateDialog();
            webViewUpdateDialog = new Dialog(activity);
            webViewUpdateDialog.setContentView(R.layout.dialog_alert_title_subtitle_button);
            webViewUpdateDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            webViewUpdateDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            TextView title = webViewUpdateDialog.findViewById(R.id.title);
            TextView subtitle = webViewUpdateDialog.findViewById(R.id.subtitle);
            TextView btnText = webViewUpdateDialog.findViewById(R.id.btnText);
            CardView cvConfirm = webViewUpdateDialog.findViewById(R.id.cv_okay);
            ImageView ivClose = webViewUpdateDialog.findViewById(R.id.iv_close);

            title.setText(activity.getString(R.string.webview_update_title));
            title.setVisibility(View.VISIBLE);
            subtitle.setText(activity.getString(R.string.webview_update_subtitle));
            subtitle.setVisibility(View.VISIBLE);
            btnText.setText(activity.getString(R.string.webview_update_button));
            btnText.setVisibility(View.VISIBLE);

            cvConfirm.setOnClickListener(v -> {
                dismissWebViewUpdateDialog();
                openPlayStore(providerPackage);
            });
            ivClose.setOnClickListener(v -> dismissWebViewUpdateDialog());

            webViewUpdateDialog.show();
        } catch (Exception ex) {
            log("update dialog failed: " + ex);
        }
    }

    private void dismissWebViewUpdateDialog() {
        try {
            if (webViewUpdateDialog != null && webViewUpdateDialog.isShowing()) {
                webViewUpdateDialog.dismiss();
            }
        } catch (Exception ignored) {
        }
        webViewUpdateDialog = null;
    }

    private void openPlayStore(String packageName) {
        try {
            activity.startActivity(new android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + packageName)));
        } catch (Exception ex) {
            try {
                activity.startActivity(new android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            } catch (Exception ignored) {
            }
        }
    }

    // ---------------------------------------------------------------------
    // Plumbing
    // ---------------------------------------------------------------------

    private long loadTimeoutMs() {
        return remoteLong("web_view_loading_timeout", DEFAULT_LOAD_TIMEOUT_MS);
    }

    /** Remote config returns 0 for an unset key, which would disable the timer entirely. */
    private long remoteLong(String key, long fallback) {
        try {
            long value = MyApplication.getFirebaseRemoteConfig().getLong(key);
            return value > 0 ? value : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private boolean isGone() {
        return activity.isFinishing() || activity.isDestroyed();
    }

    /**
     * Prefers the value mirrored from {@code onProgressChanged}, and falls back to asking the
     * WebView — screens that never set a {@code WebChromeClient} have no other source. Main
     * thread only, which every caller is.
     */
    private int currentProgress() {
        if (lastProgress > 0) return lastProgress;
        try {
            return webView.getProgress();
        } catch (Exception ex) {
            return -1;
        }
    }

    private void registerFailureEvent(String error, String triggeredFrom) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("screen", screenTag);
            bundle.putString("error", error);
            bundle.putString("triggeredFrom", triggeredFrom);
            bundle.putString("app_version", MyApplication.getAppVersionName(activity));
            MyApplication.logFirebaseEvent("web_view_page_failure", bundle);
        } catch (Exception ignored) {
        }
    }

    private void log(String message) {
        Log.d(TAG, "[" + screenTag + "] " + message);
    }
}
