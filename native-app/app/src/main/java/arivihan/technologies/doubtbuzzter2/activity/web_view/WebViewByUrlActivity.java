package arivihan.technologies.doubtbuzzter2.activity.web_view;

import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.DYNAMIC_URL;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.Settings;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageOptions;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.ImageCropperActivity;
import arivihan.technologies.doubtbuzzter2.activity.UnlockActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.customView.video_player.MiniVideoPlayer;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.WebViewLoadGuard;
import arivihan.technologies.doubtbuzzter2.helper.WebviewByUrlInterface;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.TestGenerationStatus;
import arivihan.technologies.doubtbuzzter2.model.dialog.PopUpDto;
import arivihan.technologies.doubtbuzzter2.model.firebase.rdb.TechnicalErrorPopUp;
import arivihan.technologies.doubtbuzzter2.model.player.MiniVideoPlayerConfig;

public class WebViewByUrlActivity extends BaseActivity {

    public static final String TAG = WebViewByUrlActivity.class.getSimpleName();

    /**
     * True when `origin` is the same host the activity was asked to load — i.e. our own web app.
     * Mirrors the host comparison shouldOverrideUrlLoading already uses, so the mic/camera are
     * only ever handed to our content and never to a page the WebView happened to navigate to.
     */
    private boolean isTrustedMediaOrigin(String origin) {
        try {
            String requestHost = Uri.parse(origin).getHost();
            String appHost = Uri.parse(finalUrl).getHost();
            return appHost != null && appHost.equals(requestHost);
        } catch (Exception ignored) {
            return false;
        }
    }

    // UI Variable
    private RelativeLayout rl_parent_layout;
    private WebView webView;
    private LottieAnimationView webView_lottie_animation;
    private MiniVideoPlayer mini_video_player;
    private MiniVideoPlayerConfig miniVideoPlayerConfig;

    private HowToVideoView howToVideoView;
    private View how_to_video_view;

    // Data Variable
    private String miniVideoUrl;
    private boolean isBackRestricted;
    private String onBackMessage;
    private String onBackJsMethodCallBack;
    private boolean allowOrientation;
    private Dialog technicalErrorPopUp, webviewFailureDialog;
    private DatabaseReference technicalErrorPopUpDatabaseRef;
    private ValueEventListener technicalPopUpListener;

    private Handler timeoutHandler = new Handler();
    private boolean pageLoadedSuccessfully = false;
    private boolean hasError = false;
    private long timeoutDuration = 15000; // in milliseconds
    private WebViewLoadGuard loadGuard;
    NetworkStatusLiveData networkStatusLiveData;
    // Data Variables
    private String finalUrl;
    private ValueCallback<Uri[]> filePathCallback;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<Intent> cropLauncher;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    private static final int MAX_FILE_UPLOAD_COUNT = 1;
    private static final int MAX_IMAGE_UPLOAD_COUNT = 5;

    private Uri cameraUri;
    private int filePickerMaxCount = MAX_FILE_UPLOAD_COUNT;

    private final ActivityResultLauncher<Intent> subscriptionScreenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> neetCompletionScreenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        Intent data = result.getData();
                        boolean skip = data.getBooleanExtra("skip", false);
                        boolean back = data.getBooleanExtra("back", false);
                        if (skip || back) {
                            setNeetCompletionScreenResult();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
    );

    // Backs onShowFileChooser below — WebView hands off the picked-file Intent result here since
    // it (unlike the media-permission flow) needs the full ActivityResult, not just a permission grant.
    private final ActivityResultLauncher<Intent> fileChooserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (filePathCallback == null) return;
                Uri[] results = null;
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    ClipData clipData = data.getClipData();
                    if (clipData != null) {
                        results = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            results[i] = clipData.getItemAt(i).getUri();
                        }
                    } else if (data.getData() != null) {
                        results = new Uri[]{data.getData()};
                    }
                }
                // Even if results is null, we call onReceiveValue to dismiss the webview's internal state.
                // The user specifically asked for an "empty response array", but for the standard 
                // onShowFileChooser, null is the signal for cancellation. If we send an empty array 
                // here, we satisfy the requirement while adhering to the WebView API contract.
                filePathCallback.onReceiveValue(results != null ? results : new Uri[0]);
                filePathCallback = null;
            }
    );

    private final ActivityResultLauncher<String[]> mediaPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean audioGranted = Boolean.TRUE.equals(result.get(Manifest.permission.RECORD_AUDIO));
                boolean cameraRequested = result.containsKey(Manifest.permission.CAMERA);
                boolean cameraGranted = Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA));

                webView.post(() -> webView.evaluateJavascript(
                        "javascript:if(typeof window.onAndroidMediaPermissionResult==='function')" +
                                "{window.onAndroidMediaPermissionResult(" + audioGranted + ");}",
                        null));

                // If this was a camera-involved permission request and it was denied/cancelled,
                // send the empty array ack to the webview as per requirements.
                if (cameraRequested && !cameraGranted) {
                    sendUrisToWebView(Collections.emptyList());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_by_url);

        finalUrl = extractFinalUrl(getIntent());
        boolean openExternally = getIntent().getBooleanExtra("openExternally", false);

        allowOrientation = getIntent().getBooleanExtra("allowOrientation", false);

        if (allowOrientation && MyApplication.getFirebaseRemoteConfig().getBoolean("live_class_secure")) {
            secureScreen();
        }
//        if (allowOrientation) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }

        finalUrl = appendUserCentricData(finalUrl);
        Log.d(TAG, "finalUrl: " + finalUrl);

        initPickers();
        rl_parent_layout = findViewById(R.id.rl_parent_layout);
        webView_lottie_animation = findViewById(R.id.webView_lottie_animation);
        webView = findViewById(R.id.webview);
        mini_video_player = findViewById(R.id.mini_video_player);

        timeoutDuration = MyApplication.getFirebaseRemoteConfig().getLong("web_view_by_url_loading_timeout");
        if (timeoutDuration == 0)
            timeoutDuration = 15000;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setTextZoom(100);
        webView.setHapticFeedbackEnabled(false);
        webView.setOnLongClickListener(v -> true);
        webView.setLongClickable(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setBuiltInZoomControls(false); // Hide built-in zoom controls
        webView.getSettings().setDisplayZoomControls(false);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.addJavascriptInterface(new WebviewByUrlInterface(this, webView), "AndroidInterface");
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setOnTouchListener((v, event) -> event.getPointerCount() > 1);

        how_to_video_view = findViewById(R.id.how_to_video_view);

        loadGuard = new WebViewLoadGuard(this, webView, TAG, this::revealWebView, rl_parent_layout);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                try {
                    String reqHost = request.getUrl().getHost();
                    String appHost = Uri.parse(finalUrl).getHost();
                    if (appHost != null && appHost.equals(reqHost)) {
                        return false; // same host — let WebView handle it
                    }
                } catch (Exception ignored) {
                }
                openUrlInBrowser(request.getUrl().toString());
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                try {
                    registerWebViewFailureEvent(StringUtils.isNotEmpty(error.toString()) ? error.toString() : "", "onReceivedSslError");
                    loadGuard.onMainFrameError("ssl error: " + error);
                } catch (Exception ignored) {
                } finally {
                    // Must always resolve the handler — this used to call neither cancel()
                    // nor proceed(), which leaves the load hanging forever with nothing
                    // surfaced anywhere.
                    hasError = true;
                    handler.cancel();
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                try {
                    hasError = false;
                    pageLoadedSuccessfully = false;

                    // Alpha is reset alongside visibility: the reveal animates the loader to
                    // alpha 0, and without restoring it a second navigation shows a "visible"
                    // loader that is fully transparent.
                    webView_lottie_animation.setAlpha(1f);
                    webView_lottie_animation.setVisibility(View.VISIBLE);
                    loadGuard.onPageStarted(url);

                    // Kept alongside the guard's watchdog rather than replaced by it: this one
                    // is the live-class escape hatch, showing the remote-configured popup that
                    // offers a route to the live stream. It is content, not recovery — the
                    // guard's rebuild/retry still runs behind it on its own schedule.
                    boolean setTimeOutDialog = MyApplication.getFirebaseRemoteConfig().getBoolean("web_view_by_url_loading_timeout_dialog_enable");
                    if (allowOrientation && setTimeOutDialog) {
                        timeoutHandler.postDelayed(() -> {
                            if (!pageLoadedSuccessfully && !isFinishing() && !isDestroyed()) {
                                hasError = true;
                                showWebViewFailureDialog();
                            }
                        }, timeoutDuration);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadGuard.onPageFinished(url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // Subresource failures are reported but must not blank the screen — only a
                // main-frame failure means the page itself did not load.
                if (!request.isForMainFrame()) return;
                hasError = true;
                registerWebViewFailureEvent(StringUtils.isNotEmpty(error.toString()) ? error.toString() : "", "onReceivedError");
                loadGuard.onMainFrameError("code=" + error.getErrorCode() + ", " + error.getDescription());
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (request.isForMainFrame()) {
                    hasError = true;
                    registerWebViewFailureEvent("Server error: " + errorResponse.getStatusCode(), "onReceivedHttpError");
                    loadGuard.onMainFrameError("HTTP " + errorResponse.getStatusCode());
                }
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                hasError = true;
                registerWebViewFailureEvent("Web page crashed", "onRenderProcessGone");
                return loadGuard.onRenderProcessGone(detail); // true prevents the app crash
            }

            /**
             * Google Safe Browsing blocking the page fires none of the other error callbacks,
             * so without this the block is indistinguishable from a blank screen.
             */
            @Override
            public void onSafeBrowsingHit(WebView view, WebResourceRequest request,
                                          int threatType, SafeBrowsingResponse callback) {
                if (request.isForMainFrame()) {
                    loadGuard.onSafeBrowsingHit(threatType);
                }
                callback.showInterstitial(true);
            }

        });

        // Hand the page the mic/camera when it asks for them.
        //
        // The app already holds RECORD_AUDIO and CAMERA, but a WebView does NOT pass those through
        // to its content on its own — without onPermissionRequest the page's getUserMedia() is
        // denied silently, which is why the live-DPP Zoom meeting answered "Unable to Join Audio"
        // on every tap. Only the resources the page actually asked for are granted, and only for
        // our own web app's origin, so this can't hand the mic to arbitrary content the WebView
        // may end up on.
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                loadGuard.onProgressChanged(newProgress);
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    String origin = request.getOrigin() != null ? request.getOrigin().toString() : "";
                    if (!isTrustedMediaOrigin(origin)) {
                        Log.w(TAG, "Denying media permission to untrusted origin: " + origin);
                        request.deny();
                        return;
                    }
                    List<String> allowed = new ArrayList<>();
                    for (String resource : request.getResources()) {
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)
                                || PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) {
                            allowed.add(resource);
                        }
                    }
                    if (allowed.isEmpty()) {
                        request.deny();
                    } else {
                        Log.d(TAG, "Granting " + allowed + " to " + origin);
                        request.grant(allowed.toArray(new String[0]));
                    }
                });
            }

            // Without this, tapping an <input type="file"> in the page (e.g. the OMR upload
            // picker) does nothing — a WebView never opens a file chooser on its own.
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams fileChooserParams) {
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = callback;

                try {
                    fileChooserLauncher.launch(fileChooserParams.createIntent());
                } catch (Exception e) {
                    filePathCallback = null;
                    Toast.makeText(WebViewByUrlActivity.this, "Cannot open file chooser", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });

        loadGuard.attach(savedInstanceState);

        Log.d(TAG, "WebView Url: " + finalUrl);
        loadGuard.onLoadStarted(finalUrl);
        webView.loadUrl(finalUrl);

//        cv_open_in_browser.setOnClickListener(v->{
//            openUrlInBrowser(finalUrl);
//        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedClick();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        if (openExternally) {
            openUrlInBrowser(finalUrl);
            finish();
            return;
        }

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            try {
                if (isOnline) {
                    if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                        CommonMethods.showPositiveSnackbar(rl_parent_layout, getString(R.string.InternetConnection));
                        MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                        MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                        webView.evaluateJavascript("javascript:networkStatusChanged(true);", null);
                    }
                } else {
                    if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                        CommonMethods.showNegativeSnackbar(rl_parent_layout, getString(R.string.NoInternetConnection));
                        MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                        MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                        webView.evaluateJavascript("javascript:networkStatusChanged(false);", null);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFullScreenBasedOnOrientation();
        if (allowOrientation) {
            attachTechnicalPopUpListener();
        }
    }

    private void registerWebViewFailureEvent(String error, String triggeredFrom) {
        Bundle bundle = new Bundle();
        bundle.putString("error", error);
        bundle.putString("triggeredFrom", triggeredFrom);
        bundle.putString("app_version", MyApplication.getAppVersionName(WebViewByUrlActivity.this));

        MyApplication.logFirebaseEvent("web_view_By_url_page_failure", bundle);
    }

    private void onBackPressedClick() {
        try {

            int orientation = getResources().getConfiguration().orientation;

            // In landscape back exits fullscreen, unless page has JS handler which decides (e.g. live-DPP meeting minimizes to floating window).
            boolean webHandlesBack = isBackRestricted && StringUtils.isNotEmpty(onBackJsMethodCallBack);

            if (orientation == Configuration.ORIENTATION_LANDSCAPE && !webHandlesBack) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return;
            }

            if (howToVideoView != null && howToVideoView.videoView != null
                    && howToVideoView.videoView.getVisibility() == View.VISIBLE && howToVideoView.expanded) {
                howToVideoView.resize();
                return;
            }
            if (!isBackRestricted) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            } else if (StringUtils.isNotEmpty(onBackMessage)) {
                // Empty block - message display handled elsewhere
            } else if (StringUtils.isNotEmpty(onBackJsMethodCallBack)) {
                webView.evaluateJavascript(onBackJsMethodCallBack, null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyFullScreenBasedOnOrientation();

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && rl_parent_layout != null) {
            rl_parent_layout.setPadding(0, 0, 0, 0);
            rl_parent_layout.post(() -> {
                rl_parent_layout.requestLayout();
                if (webView != null) {
                    webView.requestLayout();
                }
            });
        }
    }

    /**
     * The single loader→page handover, driven by {@link WebViewLoadGuard}: it fires from
     * whichever reveal path arrives first — {@code onPageFinished}, or the DOM probe once the
     * app has mounted — and only ever once per load.
     * <p>
     * The live-class timeout popup is dismissed here too: if the page lands after that dialog
     * went up, the dialog has nothing left to offer.
     */
    private void revealWebView(String reason) {
        try {
            Log.d(TAG, "reveal: " + reason);
            pageLoadedSuccessfully = true;
            timeoutHandler.removeCallbacksAndMessages(null);
            dismissWebViewFailureDialog();
            webView_lottie_animation.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        webView_lottie_animation.setVisibility(View.GONE);
                        webView.setAlpha(0f);
                        webView.setVisibility(View.VISIBLE);
                        webView.animate().alpha(1f).setDuration(300).start();
                        setMiniVideoPlayer();
                    }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (loadGuard != null) loadGuard.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyHowTo();
        detachTechnicalPopUpListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loadGuard != null) loadGuard.onStop();
        dismissTechnicalErrorPopUp();
        dismissWebViewFailureDialog();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (loadGuard != null) loadGuard.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        destroyHowTo();
        timeoutHandler.removeCallbacksAndMessages(null);
        dismissTechnicalErrorPopUp();
        dismissWebViewFailureDialog();
        detachTechnicalPopUpListener();
        reportPersonalisedPathProgress();
        if (loadGuard != null) {
            loadGuard.onDestroy();
            // Tears down the WebView and its wedged/dead renderer explicitly. Matters most on
            // the recovery path: recreate() builds a fresh WebView, and the stuck one must be
            // released rather than left for GC still holding a renderer process.
            loadGuard.destroyWebView();
        }
        super.onDestroy();
    }

    /**
     * Report a webview-based child-task (live lecture / demo / class notes) as viewed when
     * this screen closes. No-op unless launched from the personalised path (nodeId extra
     * present), so the path's own host webview is unaffected.
     */
    private void reportPersonalisedPathProgress() {
        try {
            arivihan.technologies.doubtbuzzter2.helper.PersonalisedPathProgressManager
                    .reportFromActivity(this,
                            arivihan.technologies.doubtbuzzter2.helper.PersonalisedPathProgressManager.STATUS_COMPLETED,
                            100, 1);
        } catch (Exception ignored) {
        }
    }


    private void destroyHowTo() {
        if (how_to_video_view != null)
            how_to_video_view.setVisibility(View.GONE);
        if (howToVideoView != null)
            howToVideoView.remove();
    }

    public void openExternalUrl(String url) {
        openUrlInBrowser(url);
    }

    public void openMeet(String meetLink) {
        CommonNavigator.openGoogleMeet(this, meetLink);
    }

    public void backToPreviousActivity() {
        finish();
    }

    public void changeBackPressed(boolean isBackRestricted) {
        this.isBackRestricted = isBackRestricted;
    }

    public void setNeetCompletionScreenResult() {
        try {
            webView.evaluateJavascript("javascript:setNeetCompletionScreenResult(true);", null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void pressBack() {
        onBackPressedClick();
    }

    public void setJsMethodCallBack(String methodName) {
        this.onBackJsMethodCallBack = methodName;
    }

    public void showPositiveSnackBarMessage(String message) {
        if (StringUtils.isNotEmpty(message)) {
            CommonMethods.showPositiveSnackbar(rl_parent_layout, message, 3000);
        }
    }

    public void showNegativeSnackBarMessage(String message) {
        if (StringUtils.isNotEmpty(message)) {
            CommonMethods.showNegativeSnackbar(rl_parent_layout, message, 3000);
        }
    }

    public void setOrientationLandscape(boolean isLandscape) {
        if (isLandscape && !isLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (!isLandscape && !isPortrait()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void shareLiveClass() {
        String message = MyApplication.getFirebaseRemoteConfig().getString("live_class_share_message_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
//        Bundle savedBundle = getIntent().getBundleExtra("savedBundle");
//        if(savedBundle!=null) {
//
//        }
        CommonMethods.shareText(this, message);
    }

    public void shareLiveClass(String message) {
        if (StringUtils.isNotEmpty(message)) {
            CommonMethods.shareText(this, message);
        } else {
            CommonMethods.showNegativeSnackbar(rl_parent_layout, getString(R.string.no_message_available), 2000);
        }
    }

    public void requestMediaPermission(boolean video) {
        MyApplication.saveData("mic_permission_ever_requested", true);
        List<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.RECORD_AUDIO);
        if (video) perms.add(Manifest.permission.CAMERA);
        mediaPermissionLauncher.launch(perms.toArray(new String[0]));
    }

    public void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Log.w(TAG, "openAppSettings failed: " + e.getMessage());
        }
    }

    public void setFirebaseEvent(String eventName, Map<String, String> eventParams) {
        try {
            if (eventParams == null || eventParams.isEmpty()) {
                MyApplication.logFirebaseBundleCreation(eventName);
            } else {
                MyApplication.logFirebaseBundleCreation(eventName, eventParams);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String appendUserCentricData(String url) {
        boolean appendUserId = getIntent().getBooleanExtra("appendUserId", false);
        boolean appendUserName = getIntent().getBooleanExtra("appendUserName", false);
        boolean appendUserGender = getIntent().getBooleanExtra("appendUserGender", false);
        boolean appendUserImageUrl = getIntent().getBooleanExtra("appendUserImageUrl", false);
        boolean appendBoard = getIntent().getBooleanExtra("appendBoard", false);
        boolean appendClassId = getIntent().getBooleanExtra("appendClassId", true);
        boolean appendCourseId = getIntent().getBooleanExtra("appendCourseId", true);
        boolean appendToken = getIntent().getBooleanExtra("appendToken", false);
        isBackRestricted = getIntent().getBooleanExtra("isBackRestricted", false);
        onBackMessage = getIntent().getStringExtra("onBackMessage");

        if (appendUserId) {
            String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            if (StringUtils.isNotEmpty(userId)) {
                url = appendParamToUrl(url, PreferenceKeys.USER_ID.getKey(), userId);
            }
        }
        if (appendUserGender) {
            String userGender = MyApplication.getStringData(PreferenceKeys.GENDER.getKey());
            if (StringUtils.isNotEmpty(userGender)) {
                url = appendParamToUrl(url, "userGender", userGender);
            }
        }
        if (appendUserName) {
            String userName = MyApplication.getStringData(PreferenceKeys.NAME.getKey());
            if (StringUtils.isNotEmpty(userName)) {
                url = appendParamToUrl(url, "userName", userName);
            }
        }
        if (appendClassId) {
            String classId = MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey());
            if (StringUtils.isNotEmpty(classId)) {
                url = appendParamToUrl(url, PreferenceKeys.CLASS_ID.getKey(), classId);
            }
        }
        if (appendCourseId) {
            String courseId = MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey());
            if (StringUtils.isNotEmpty(courseId)) {
                url = appendParamToUrl(url, PreferenceKeys.COURSE_ID.getKey(), courseId);
            }
        }

        if (appendUserImageUrl) {
            String userImageUrl = MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey());
            if (StringUtils.isNotEmpty(userImageUrl)) {
                url = appendParamToUrl(url, "imageUrl", CommonMethods.encodeUrl(userImageUrl));
            }
        }

        if (appendToken) {
            url = appendParamToUrl(url, "token", MyApplication.getFCMToken());
        }
        if (appendBoard) {
            url = appendParamToUrl(url, PreferenceKeys.BOARD.getKey(), String.valueOf(MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())));
        }

        url = appendParamToUrl(url, "env", EnvironmentManager.getEnvironmentNameByUrl(MyApplication.getEndPoint()).toLowerCase());
        url = appendParamToUrl(url, "lang", MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        url = appendBatchParams(url);
        url = appendParamToUrl(url, "subscriptionType", MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey()));
        url = appendParamToUrl(url, "versionCode", String.valueOf(DeviceInfoUtil.getVersionCode(this)));
        miniVideoUrl = getIntent().getStringExtra("miniVideoUrl");
        Log.d("ShowMINIVIDEO", "ShowMINIVIDEO " + miniVideoUrl);

        String testId = getIntent().getStringExtra("testId");
        String action = getIntent().getStringExtra("action");
        String testName = getIntent().getStringExtra("testName");
        if (StringUtils.isNotEmpty(testId)) url = appendParamToUrl(url, "testId", testId);
        if (StringUtils.isNotEmpty(action)) url = appendParamToUrl(url, "action", action);
        if (StringUtils.isNotEmpty(testName)) url = appendParamToUrl(url, "testName", testName);

        return url;
    }

    /**
     * Build the final URL by merging base URL and query params.
     */
    private String extractFinalUrl(Intent intent) {
        Uri uri = intent.getData();
        String baseUrl = "";
        Map<String, String> params = new HashMap<>();

        try {
            if (uri != null && uri.isHierarchical()) {
                // 🔹 Deep link handling
                for (String key : uri.getQueryParameterNames()) {
                    String value = uri.getQueryParameter(key);
                    if (value != null) {
                        params.put(key, value);
                    }
                }

                if (params.containsKey("url")) {
                    baseUrl = URLDecoder.decode(params.get("url"), "UTF-8");
                    params.remove("url");
                }

            } else {
                // 🔹 Normal intent flow
                baseUrl = intent.getStringExtra("url");
                Log.d(TAG, "URL: " + baseUrl);

                // =========================
                // ✅ CASE 1: Serializable Map (existing)
                // =========================
                Serializable serializable = intent.getSerializableExtra("params");
                if (serializable instanceof Map<?, ?> extras) {
                    Log.d(TAG, "Params (Map): " + extras.toString());
                    for (Map.Entry<?, ?> entry : extras.entrySet()) {
                        params.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                    }
                }

                // =========================
                // ✅ CASE 2: JSON String (NEW FIX)
                // =========================
                try {
                    String paramsJson = intent.getStringExtra("params");
                    if (paramsJson != null) {
                        Log.d(TAG, "Params (JSON String): " + paramsJson);
                        try {
                            Gson gson = new Gson();
                            Map<String, Object> map = gson.fromJson(paramsJson, Map.class);
                            if (map != null) {
                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    params.put(entry.getKey(), String.valueOf(entry.getValue()));
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing params JSON", e);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // =========================
                // ✅ CASE 3: Nested extras (activity_params_json)
                // =========================
                Bundle extras = intent.getExtras();
                if (extras != null && extras.containsKey("activity_params_json")) {
                    String activityParamsJson = extras.getString("activity_params_json");
                    Log.d(TAG, "Params (activity_params_json): " + activityParamsJson);

                    try {
                        Gson gson = new Gson();
                        Map<String, Object> map = gson.fromJson(activityParamsJson, Map.class);
                        if (map != null) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                params.put(entry.getKey(), String.valueOf(entry.getValue()));
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing activity_params_json", e);
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in extractFinalUrl", e);
        }

        Log.d(TAG, "Final Params Map: " + params.toString());

        return buildUrlWithParams(baseUrl, params);
    }

    /**
     * Appends parameters properly, handling '?' and '&' edge cases.
     */
    private String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) return "";

        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();

        // If base URL already contains ?, parse and preserve it
        Uri baseUri = Uri.parse(baseUrl);
        Set<String> existingParams = baseUri.getQueryParameterNames();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!existingParams.contains(entry.getKey())) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        return builder.build().toString();
    }

    private void openUrlInBrowser(String url) {
        try {
            Log.d(TAG, "URL: " + url);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show();
        }
    }

    private String appendParamToUrl(String url, String key, String value) {
        try {
            Uri uri = Uri.parse(url);
            Uri.Builder builder = uri.buildUpon();

            // Add '?' if missing and parameter if not already present
            if (uri.getQueryParameter(key) == null) {
                builder.appendQueryParameter(key, value);
            }
            return builder.build().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }
    }

    private String appendBatchParams(String url) {
        // ID fields - stored as int
        String batchId = String.valueOf(MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey()));
        String examId = String.valueOf(MyApplication.getIntData(PreferenceKeys.EXAM_ID.getKey()));
        String streamId = String.valueOf(MyApplication.getIntData(PreferenceKeys.STREAM_ID.getKey()));
        String gradeId = String.valueOf(MyApplication.getIntData(PreferenceKeys.GRADE_ID.getKey()));
        String yearId = String.valueOf(MyApplication.getIntData(PreferenceKeys.YEAR_ID.getKey()));

        // String fields
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        String appVersion = BuildConfig.VERSION_NAME;

        String batchName = MyApplication.getStringData(PreferenceKeys.BATCH_NAME.getKey());
        String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        String examName = MyApplication.getStringData(PreferenceKeys.EXAM_NAME.getKey());
        String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
        String streamName = MyApplication.getStringData(PreferenceKeys.STREAM_NAME.getKey());
        String streamCode = MyApplication.getStringData(PreferenceKeys.STREAM_CODE.getKey());
        String gradeName = MyApplication.getStringData(PreferenceKeys.GRADE_NAME.getKey());
        String gradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());
        String yearName = MyApplication.getStringData(PreferenceKeys.YEAR_NAME.getKey());
        String yearCode = MyApplication.getStringData(PreferenceKeys.YEAR_CODE.getKey());
        String hidProPlanSubscription = String.valueOf(SubscriptionNavigationHelper.hideProPlanSubscription());

        if (StringUtils.isNotEmpty(batchId)) url = appendParamToUrl(url, "batchId", batchId);
        if (StringUtils.isNotEmpty(batchName)) url = appendParamToUrl(url, "batchName", batchName);
        if (StringUtils.isNotEmpty(batchCode)) url = appendParamToUrl(url, "batchCode", batchCode);
        if (StringUtils.isNotEmpty(examId)) url = appendParamToUrl(url, "examId", examId);
        if (StringUtils.isNotEmpty(examName)) url = appendParamToUrl(url, "examName", examName);
        if (StringUtils.isNotEmpty(examCode)) url = appendParamToUrl(url, "examCode", examCode);
        if (StringUtils.isNotEmpty(streamId)) url = appendParamToUrl(url, "streamId", streamId);
        if (StringUtils.isNotEmpty(streamName))
            url = appendParamToUrl(url, "streamName", streamName);
        if (StringUtils.isNotEmpty(streamCode))
            url = appendParamToUrl(url, "streamCode", streamCode);
        if (StringUtils.isNotEmpty(gradeId)) url = appendParamToUrl(url, "gradeId", gradeId);
        if (StringUtils.isNotEmpty(gradeName)) url = appendParamToUrl(url, "gradeName", gradeName);
        if (StringUtils.isNotEmpty(gradeCode)) url = appendParamToUrl(url, "gradeCode", gradeCode);
        if (StringUtils.isNotEmpty(yearId)) url = appendParamToUrl(url, "yearId", yearId);
        if (StringUtils.isNotEmpty(yearName)) url = appendParamToUrl(url, "yearName", yearName);
        if (StringUtils.isNotEmpty(yearCode)) url = appendParamToUrl(url, "yearCode", yearCode);
        if (StringUtils.isNotEmpty(hidProPlanSubscription)) url = appendParamToUrl(url, "hidProPlanSubscription", hidProPlanSubscription);
        if (StringUtils.isNotEmpty(deviceModel)) url = appendParamToUrl(url, "deviceModel", deviceModel);
        if (StringUtils.isNotEmpty(appVersion)) url = appendParamToUrl(url, "appVersion", appVersion);

        return url;
    }

    private void setMiniVideoPlayer() {
        Log.d(TAG, "setMiniVideoPlayer: ");
        try {
            howToVideoView = HowToVideoView.getInstance(this);

            if (howToVideoView.videoView == null || howToVideoView.videoView != how_to_video_view) {
                howToVideoView.setView(this, how_to_video_view);
            }

            if (howToVideoView.videoView.getVisibility() != View.VISIBLE && StringUtils.isEmpty(miniVideoUrl)) {
                howToVideoView.setUrl(miniVideoUrl);
                howToVideoView.setType(DYNAMIC_URL, false);
                howToVideoView.setButtonText(getString(R.string.start_now));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void applyFullScreenBasedOnOrientation() {
        if (isLandscape()) {
            enableFullScreen();
        } else {
            disableFullScreen();
        }
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private boolean isPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private void enableFullScreen() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(window, window.getDecorView());
        insetsController.hide(WindowInsetsCompat.Type.statusBars());
        insetsController.hide(WindowInsetsCompat.Type.navigationBars());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void disableFullScreen() {
        Window window = getWindow();
        // Stays false so the screen keeps drawing edge to edge; showing the bars again brings the
        // insets back and EdgeToEdgeHelper's padding on the content root follows them.
        WindowCompat.setDecorFitsSystemWindows(window, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            window.setAttributes(lp);
        }

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(window, window.getDecorView());
        insetsController.show(WindowInsetsCompat.Type.statusBars());
        insetsController.show(WindowInsetsCompat.Type.navigationBars());
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Clear landscape inset padding and request fresh layout pass to prevent WebView width mismatch (fixes blank strip on portrait after landscape fullscreen).
        if (rl_parent_layout != null) {
            rl_parent_layout.setPadding(0, 0, 0, 0);
            rl_parent_layout.post(() -> {
                ViewCompat.requestApplyInsets(rl_parent_layout);
                rl_parent_layout.requestLayout();
                if (webView != null) {
                    webView.requestLayout();
                }
            });
        }

    }

    private void attachTechnicalPopUpListener() {
        if (technicalPopUpListener != null) return; // already attached

        String chapterId = null;
        Serializable serializable = getIntent().getSerializableExtra("params");
        if (serializable instanceof Map<?, ?> extras) {
            chapterId = (String) extras.get("chapterId");
        }
        if (StringUtils.isEmpty(chapterId))
            return;

        Log.d(TAG, "---> ChapterId: " + chapterId);
        technicalErrorPopUpDatabaseRef = FirebaseDatabase.getInstance().getReference("chapter_live_technical_update/" + chapterId);

        technicalPopUpListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!snapshot.exists()) {
                        Log.d(TAG, "No model data found.");
                        return;
                    }

                    TechnicalErrorPopUp model = snapshot.getValue(TechnicalErrorPopUp.class);
                    setTechnicalErrorPopUp(model);
                } catch (Exception e) {
                    Log.e(TAG, "Error reading poster data", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Listener cancelled: " + error.getMessage());
            }
        };

        technicalErrorPopUpDatabaseRef.addValueEventListener(technicalPopUpListener);
    }

    private void setTechnicalErrorPopUp(TechnicalErrorPopUp model) {
        if (model == null) {
            Log.w(TAG, "Model data is null or invalid");
            return;
        }

        Log.d(TAG, "Model Data: " + model.toString());
        long now = System.currentTimeMillis();
        long expiry = model.getUpdatedAt() + 500000;

        if (now > expiry || StringUtils.isEmpty(model.getUrl())) {
            Log.d(TAG, "Model expired, removing..." + now);
//                        removeTechnicalPopUpFromFirebaseRDB();
            return;
        }

        showTechnicalErrorPopup(model.getUrl());

    }

    private void detachTechnicalPopUpListener() {
        if (technicalErrorPopUpDatabaseRef != null && technicalPopUpListener != null) {
            technicalErrorPopUpDatabaseRef.removeEventListener(technicalPopUpListener);
            technicalPopUpListener = null;
        }
    }

//    private void removeTechnicalPopUpFromFirebaseRDB() {
//        if (technicalErrorPopUpDatabaseRef == null)
//            technicalErrorPopUpDatabaseRef = FirebaseDatabase.getInstance().getReference("users/SubscriptionPopUp/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
//        technicalErrorPopUpDatabaseRef.removeValue((error, ref) -> {
//            if (error != null) {
//                Log.e(TAG, "Failed to remove : " + error.getMessage());
//            } else {
//                Log.d(TAG, "Removed successfully");
//            }
//        });
//    }


    public void openSubscriptionScreen(String jsonDto) {
        String finalSource = "";
        if (StringUtils.isNotEmpty(jsonDto)) {
            try {
                JSONObject jsonObject = new JSONObject(jsonDto);
                if (jsonObject.has("source")) {
                    finalSource = jsonObject.getString("source");
                    Log.d(TAG, "openSubscriptionScreen() called with: jsonDto = [" + jsonDto + "]");
                }
            } catch (Exception ignored) {
            }
        }

        if (StringUtils.isEmpty(finalSource)) {
            finalSource = "webview_url";
        }
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntentWithSource(this, finalSource);
        if (StringUtils.isNotEmpty(jsonDto)) {
            try {
                Gson gson = new Gson();
                SubscriptionLockPopupDto subscriptionLockPopupDto = gson.fromJson(jsonDto, SubscriptionLockPopupDto.class);
                intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
            } catch (Exception ignored) {

            }
        }
        subscriptionScreenLauncher.launch(intent);
    }

    public void openNeetCompletionScreen(String type) {
        try {
            Intent intent = new Intent(this, UnlockActivity.class);
            intent.putExtra(UnlockActivity.BUNDLE_CONDITION_TYPE, type);
            neetCompletionScreenLauncher.launch(intent);
//            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setTopicSelection(String topicId) {
        MyApplication.saveData(PreferenceKeys.ADAPTIVE_SELECTED_TOPIC_ID.getKey(), topicId);
    }

    public void onTestStarted(String requestId) {
        if (StringUtils.isEmpty(requestId)) return;
        Map<String, Object> update = new HashMap<>();
        update.put("status", TestGenerationStatus.DISMISSED.name());
        MyApplication.getTestGenerationDatabaseReference(requestId).updateChildren(update);
    }


    public void openCropActivity(Uri sourceUri) {

        Intent cropIntent = new Intent(this, CropImageActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, sourceUri);

        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON_TOUCH;

        bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, options);
        cropIntent.putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle);

        cropLauncher.launch(cropIntent);
    }

    private void initPickers() {

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        ArrayList<String> urls =
                                result.getData().getStringArrayListExtra("url_list");

                        if (urls != null && !urls.isEmpty()) {
                            if (urls.size() > MAX_IMAGE_UPLOAD_COUNT) {
                                CommonMethods.showNegativeSnackbar(rl_parent_layout,
                                        getString(R.string.oops_sirf_images_allowed_hain_multiple, MAX_IMAGE_UPLOAD_COUNT), 3000);
                                return;
                            }
                            sendUrisToWebView(urls);
                        } else {
                            sendUrisToWebView(Collections.emptyList());
                        }
                    } else {
                        // Handle cancel/dismiss
                        sendUrisToWebView(Collections.emptyList());
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraUri != null) {
                        openCropActivity(cameraUri);
                    } else {
                        // Handle cancel/dismiss
                        sendUrisToWebView(Collections.emptyList());
                    }
                });

        cropLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        CropImage.ActivityResult cropResult =
                                CropImage.getActivityResult(result.getData());

                        if (cropResult != null) {
                            Uri croppedUri = cropResult.getUri();
                            sendUrisToWebView(
                                    Collections.singletonList(croppedUri.toString())
                            );
                        } else {
                            sendUrisToWebView(Collections.emptyList());
                        }
                    } else {
                        // Handle cancel/dismiss
                        sendUrisToWebView(Collections.emptyList());
                    }
                }
        );

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        sendFilesToWebView(Collections.emptyList());
                        return;
                    }

                    Intent data = result.getData();
                    List<Uri> uris = new ArrayList<>();

                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            uris.add(data.getClipData().getItemAt(i).getUri());
                        }
                    } else if (data.getData() != null) {
                        uris.add(data.getData());
                    }

                    if (uris.isEmpty()) {
                        sendFilesToWebView(Collections.emptyList());
                        return;
                    }

                    if (uris.size() > filePickerMaxCount) {
                        boolean allImages = true;
                        for (Uri u : uris) {
                            String mime = getContentResolver().getType(u);
                            if (mime == null || !mime.startsWith("image/")) {
                                allImages = false;
                                break;
                            }
                        }
                        int resId = allImages
                                ? (filePickerMaxCount > 1 ? R.string.oops_sirf_images_allowed_hain_multiple : R.string.oops_sirf_images_allowed_hain_single)
                                : R.string.oops_sirf_1_file_allowed;
                        CommonMethods.showNegativeSnackbar(rl_parent_layout,
                                getString(resId, filePickerMaxCount), 3000);
                        // Even if it fails validation, we should send an empty array to acknowledge the process ended
                        sendFilesToWebView(Collections.emptyList());
                        return;
                    }

                    List<String> uriStrings = new ArrayList<>();
                    for (Uri uri : uris) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {
                            // not all providers support persistable permission; read still works for this session
                        }
                        uriStrings.add(uri.toString());
                    }
                    sendFilesToWebView(uriStrings);
                }
        );

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Re-trigger camera logic now that we have permission
                        openCamera();
                    } else {
                        // Permission denied, send empty array ack
                        sendUrisToWebView(Collections.emptyList());
                    }
                }
        );
    }

    public void openGallery(int maxCount) {
        if (maxCount <= 0) {
            return;
        }
        int allowedCount = Math.min(maxCount, MAX_IMAGE_UPLOAD_COUNT);
        Intent intent = new Intent(this, ImageCropperActivity.class);
        intent.putExtra("allowedImages", allowedCount);
        intent.putExtra("errorText", getString(
                allowedCount > 1 ? R.string.oops_sirf_images_allowed_hain_multiple : R.string.oops_sirf_images_allowed_hain_single,
                allowedCount));
        galleryLauncher.launch(intent);
    }

    public void openCamera() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,
                "camera_" + System.currentTimeMillis());

        cameraUri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        cameraLauncher.launch(cameraUri);
    }

    // Opens system file picker allowing images and PDFs. Files are capped at MAX_FILE_UPLOAD_COUNT (1).
    public void openFilePicker(int maxCount) {
        filePickerMaxCount = MAX_FILE_UPLOAD_COUNT;

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

        filePickerLauncher.launch(intent);
    }

    public void sendUrisToWebView(List<String> uris) {

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<ImagePayload> payloadList = new ArrayList<>();

                for (String uriString : uris) {
                    Uri uri = Uri.parse(uriString);

                    String base64 = convertUriToBase64(uri);
                    payloadList.add(new ImagePayload(uriString, base64));
                }

                String json = new Gson().toJson(payloadList);

                String js =
                        "window.onImagesSelected && window.onImagesSelected(" + json + ");";

                runOnUiThread(() ->
                        webView.evaluateJavascript(js, null)
                );

                Log.d(TAG, "Images sent to WebView: " + payloadList.size());

            } catch (Exception e) {
                Log.e(TAG, "Error converting images", e);
                CommonMethods.showNegativeSnackbar(rl_parent_layout, getString(R.string.something_went_wrong), 3000);
            }
        });
    }

    // Handles files picked via openFilePicker() - images go through the same compression pipeline
    // as gallery/camera; PDFs are sent as raw base64 since they can't be decoded as a bitmap.
    public void sendFilesToWebView(List<String> uriStrings) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<FilePayload> payloadList = new ArrayList<>();

                for (String uriString : uriStrings) {
                    Uri uri = Uri.parse(uriString);
                    String mimeType = getContentResolver().getType(uri);
                    boolean isPdf = "application/pdf".equals(mimeType);

                    String base64 = isPdf ? convertUriToRawBase64(uri) : convertUriToBase64(uri);
                    payloadList.add(new FilePayload(uriString, base64, mimeType, getFileName(uri)));
                }

                String json = new Gson().toJson(payloadList);

                String js = "window.onFilesSelected && window.onFilesSelected(" + json + ");";

                runOnUiThread(() ->
                        webView.evaluateJavascript(js, null)
                );

                Log.d(TAG, "Files sent to WebView: " + payloadList.size());

            } catch (Exception e) {
                Log.e(TAG, "Error converting files", e);
                CommonMethods.showNegativeSnackbar(rl_parent_layout, getString(R.string.something_went_wrong), 3000);
            }
        });
    }

    private String getFileName(Uri uri) {
        String name = "file";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex);
                }
            }
        } catch (Exception ignored) {
        }
        return name;
    }

    private String convertUriToRawBase64(Uri uri) throws IOException {
        try (InputStream in = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while (in != null && (read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
        }
    }

    private String convertUriToBase64(Uri imageUri) throws IOException {

        // compression quality
        int quality;
        try {
            quality = Integer.parseInt(
                    MyApplication.getFirebaseRemoteConfig()
                            .getString("compression_image_factor")
            );
        } catch (Exception e) {
            quality = 80;
        }
        quality = Math.max(0, Math.min(100, quality));

        // Read image bounds
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try (InputStream boundsStream =
                     getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(boundsStream, null, options);
        }

        // Calculate sample size
        options.inSampleSize = calculateInSampleSize(options, 1080, 1080);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565; // lower memory

        Bitmap bitmap;
        try (InputStream bitmapStream =
                     getContentResolver().openInputStream(imageUri)) {
            bitmap = BitmapFactory.decodeStream(bitmapStream, null, options);
        }

        if (bitmap == null) {
            throw new IOException("Failed to decode bitmap");
        }

        // Compress bitmap
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Bitmap.CompressFormat format =
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        ? Bitmap.CompressFormat.WEBP_LOSSY
                        : Bitmap.CompressFormat.JPEG;

        bitmap.compress(format, quality, outputStream);

        byte[] compressedBytes = outputStream.toByteArray();

        return Base64.encodeToString(compressedBytes, Base64.NO_WRAP);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static class ImagePayload {
        public String uri;
        public String base64;

        public ImagePayload(String uri, String base64) {
            this.uri = uri;
            this.base64 = base64;
        }
    }

    public static class FilePayload {
        public String uri;
        public String base64;
        public String mimeType;
        public String fileName;

        public FilePayload(String uri, String base64, String mimeType, String fileName) {
            this.uri = uri;
            this.base64 = base64;
            this.mimeType = mimeType;
            this.fileName = fileName;
        }
    }

    private void showTechnicalErrorPopup(String url) {
        try {
            dismissTechnicalErrorPopUp();
            Gson gson = new Gson();
            String technicalErrorStr = MyApplication.getFirebaseRemoteConfig().getString("live_class_technical_error_pop_up_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            PopUpDto popUpDto = gson.fromJson(technicalErrorStr, PopUpDto.class);
            technicalErrorPopUp = new Dialog(this);
            technicalErrorPopUp.setContentView(R.layout.dialog_live_class_technical_error);
            technicalErrorPopUp.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            technicalErrorPopUp.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            technicalErrorPopUp.setCancelable(false);
            technicalErrorPopUp.setCanceledOnTouchOutside(false);

            Window window = technicalErrorPopUp.getWindow();
            if (window != null) {
                DisplayMetrics metrics = new DisplayMetrics();
                window.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int orientation = getResources().getConfiguration().orientation;
                int width;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    width = (int) (metrics.widthPixels * 0.50);  // 60%
                } else {
                    width = (int) (metrics.widthPixels * 0.80);  // 85%
                }

                window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            }

            TextView title = technicalErrorPopUp.findViewById(R.id.title);
            TextView subtitle = technicalErrorPopUp.findViewById(R.id.subtitle);
            TextView btnTextView = technicalErrorPopUp.findViewById(R.id.btnText);
            CardView cv_confirm = technicalErrorPopUp.findViewById(R.id.cv_okay);
            ImageView iv_close = technicalErrorPopUp.findViewById(R.id.iv_close);

            LottieAnimationView animation = technicalErrorPopUp.findViewById(R.id.animation);

            title.setVisibility(View.GONE);
            subtitle.setVisibility(View.GONE);
            btnTextView.setVisibility(View.GONE);

            String titleText = popUpDto.getTitle();
            String subTitle = popUpDto.getSubTitle();
            String buttonText = popUpDto.getBtnName();

            if (StringUtils.isNotEmpty(titleText)) {
                title.setText(CommonMethods.isValidJson(titleText) ?
                        StyledTextBuilder.fromJson(this, titleText) : titleText);
                title.setVisibility(View.VISIBLE);
            }

            if (StringUtils.isNotEmpty(subTitle)) {
                subtitle.setText(CommonMethods.isValidJson(subTitle) ?
                        StyledTextBuilder.fromJson(this, subTitle) : subTitle);
                subtitle.setVisibility(View.VISIBLE);
            }

            if (StringUtils.isNotEmpty(buttonText)) {
                btnTextView.setText(CommonMethods.isValidJson(buttonText) ?
                        StyledTextBuilder.fromJson(this, buttonText) : buttonText);
                btnTextView.setVisibility(View.VISIBLE);
            }

            cv_confirm.setOnClickListener(view -> {
                if (StringUtils.isNotEmpty(url)) {
                    technicalErrorPopUp.dismiss();
                    openUrlInBrowser(url);
                    finish();
                }
            });

            iv_close.setOnClickListener(v -> {
                technicalErrorPopUp.dismiss();
            });

            technicalErrorPopUp.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showWebViewFailureDialog() {
        String popupStr = MyApplication.getFirebaseRemoteConfig().getString("web_view_by_url_failure_popup_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        if (StringUtils.isEmpty(popupStr))
            return;

        PopUpDto popUpDto = new Gson().fromJson(popupStr, PopUpDto.class);
        if (popUpDto == null)
            return;
        popUpDto.setClassName(finalUrl);
        try {
            dismissWebViewFailureDialog();
            webviewFailureDialog = new Dialog(this);
            webviewFailureDialog.setContentView(R.layout.dialog_alert_title_subtitle_button);
            webviewFailureDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            webviewFailureDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            webviewFailureDialog.setCancelable(true);
            webviewFailureDialog.setCanceledOnTouchOutside(true);

            TextView title = webviewFailureDialog.findViewById(R.id.title);
            TextView subtitle = webviewFailureDialog.findViewById(R.id.subtitle);
            TextView btnTextView = webviewFailureDialog.findViewById(R.id.btnText);
            CardView cv_confirm = webviewFailureDialog.findViewById(R.id.cv_okay);
            ImageView iv_close = webviewFailureDialog.findViewById(R.id.iv_close);

            String titleText = popUpDto.getTitle();
            String subTitle = popUpDto.getSubTitle();
            String buttonText = popUpDto.getBtnName();

            title.setVisibility(View.GONE);
            subtitle.setVisibility(View.GONE);
            btnTextView.setVisibility(View.GONE);

            if (StringUtils.isNotEmpty(titleText)) {
                title.setText(CommonMethods.isValidJson(titleText) ?
                        StyledTextBuilder.fromJson(this, titleText) : titleText);
                title.setVisibility(View.VISIBLE);
            }

            if (StringUtils.isNotEmpty(subTitle)) {
                subtitle.setText(CommonMethods.isValidJson(subTitle) ?
                        StyledTextBuilder.fromJson(this, subTitle) : subTitle);
                subtitle.setVisibility(View.VISIBLE);
            }

            if (StringUtils.isNotEmpty(buttonText)) {
                btnTextView.setText(CommonMethods.isValidJson(buttonText) ?
                        StyledTextBuilder.fromJson(this, buttonText) : buttonText);
                btnTextView.setVisibility(View.VISIBLE);
            }

            cv_confirm.setOnClickListener(view -> {
                dismissWebViewFailureDialog();
                CommonMethods.navigateFromActivityConfig(this, popUpDto.getNavigationType(), popUpDto.getClassName(), popUpDto.getActivityParams());
                finish();
            });

            iv_close.setOnClickListener(v -> {
                webviewFailureDialog.dismiss();
            });

            webviewFailureDialog.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void dismissTechnicalErrorPopUp() {
        if (technicalErrorPopUp != null && technicalErrorPopUp.isShowing()) {
            technicalErrorPopUp.dismiss();
            technicalErrorPopUp = null;
        }
    }

    private void dismissWebViewFailureDialog() {
        if (webviewFailureDialog != null && webviewFailureDialog.isShowing()) {
            webviewFailureDialog.dismiss();
            webviewFailureDialog = null;
        }
    }


}