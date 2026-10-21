package arivihan.technologies.doubtbuzzter2.activity.web_view;

import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.DYNAMIC_URL;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageOptions;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.ImageCropperActivity;
import arivihan.technologies.doubtbuzzter2.activity.UnlockActivity;
import arivihan.technologies.doubtbuzzter2.activity.video_screens.neet.FullScreenVideoActivity;
import arivihan.technologies.doubtbuzzter2.activity.web_view.interfaces.WebViewNeetWeakTopicSelectionInterface;
import arivihan.technologies.doubtbuzzter2.common.BundleParamConstants;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.customView.video_player.MiniVideoPlayer;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.WebViewLoadGuard;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.player.MiniVideoPlayerConfig;

public class WebViewNeetWeakTopicSelectionActivity extends BaseActivity {

    public static final String TAG = WebViewByUrlActivity.class.getSimpleName();

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
    private Dialog technicalErrorPopUp, webviewFailureDialog;
    private DatabaseReference technicalErrorPopUpDatabaseRef;
    private ValueEventListener technicalPopUpListener;

    private Handler timeoutHandler = new Handler();
    private boolean pageLoadedSuccessfully = false;
    private boolean hasError = false;
    //    private long timeoutDuration = 15000; // in milliseconds
    private WebViewLoadGuard loadGuard;
    NetworkStatusLiveData networkStatusLiveData;
    // Data Variables
    private String finalUrl;
    private NavigationType receivedNavigationType = NavigationType.APP;

    private enum NavigationType {
        APP,
        DEEPLINK
    }

    ;

    private ValueEventListener realtimeListener;
    private DatabaseReference realtimeRef;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<Intent> cropLauncher;

    private Uri cameraUri;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_neet_weak_topic_selection);


        finalUrl = extractFinalUrl(getIntent());

//        allowOrientation = getIntent().getBooleanExtra("allowOrientation", false);

//        if (allowOrientation && MyApplication.getFirebaseRemoteConfig().getBoolean("live_class_secure")) {
//            secureScreen();
//        }

        finalUrl = appendUserCentricData(finalUrl);
        Log.d(TAG, "finalUrl: " + finalUrl);

        if (receivedNavigationType == NavigationType.DEEPLINK) {
            long count = MyApplication.getLongData(PreferenceKeys.NEET_WEAK_TOPIC_SELECTION_VIDEO_COUNT.getKey());
            long remoteCount = MyApplication.getFirebaseRemoteConfig().getLong("to_show_neet_weak_topic_video_limit");

            Log.d(TAG, "Video show: " + count + ", " + remoteCount);
            if (count < remoteCount && MyApplication.isNetworkAvailable()) {
                Intent intent = new Intent(this, FullScreenVideoActivity.class);
                startActivity(intent);
                MyApplication.saveData(PreferenceKeys.NEET_WEAK_TOPIC_SELECTION_VIDEO_COUNT.getKey(), count + 1);
            }
            Log.d(TAG, "Video show: " + MyApplication.getLongData(PreferenceKeys.NEET_WEAK_TOPIC_SELECTION_VIDEO_COUNT.getKey()) + ", " + remoteCount);
        }

        initPickers();
        rl_parent_layout = findViewById(R.id.rl_parent_layout);
        webView_lottie_animation = findViewById(R.id.webView_lottie_animation);
        webView = findViewById(R.id.webview);
        mini_video_player = findViewById(R.id.mini_video_player);

//        timeoutDuration = MyApplication.getFirebaseRemoteConfig().getLong("web_view_by_url_loading_timeout");
//        if (timeoutDuration == 0)
//            timeoutDuration = 15000;

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
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.addJavascriptInterface(new WebViewNeetWeakTopicSelectionInterface(this, webView), "AndroidInterface");
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        how_to_video_view = findViewById(R.id.how_to_video_view);

        loadGuard = new WebViewLoadGuard(this, webView, TAG, this::revealWebView, rl_parent_layout);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                try {
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
                loadGuard.onMainFrameError("code=" + error.getErrorCode() + ", " + error.getDescription());
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (request.isForMainFrame()) {
                    hasError = true;
                    loadGuard.onMainFrameError("HTTP " + errorResponse.getStatusCode());
                }
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                hasError = true;
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

        // Deliberately no WebChromeClient: this screen has never had one, and attaching a bare
        // one changes how the page's JS dialogs behave. The guard reads progress off the
        // WebView instead.

        loadGuard.attach(savedInstanceState);

        Log.d(TAG, "WebView Url: " + finalUrl);
        loadGuard.onLoadStarted(finalUrl);
        webView.loadUrl(finalUrl);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedClick();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

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

    /**
     * The single loader→page handover, driven by {@link WebViewLoadGuard}: it fires from
     * whichever reveal path arrives first — {@code onPageFinished}, or the DOM probe once the
     * app has mounted — and only ever once per load.
     */
    private void revealWebView(String reason) {
        try {
            Log.d(TAG, "reveal: " + reason);
            pageLoadedSuccessfully = true;
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyHowTo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loadGuard != null) loadGuard.onStop();
        dismissTechnicalErrorPopUp();
        dismissWebViewFailureDialog();
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (loadGuard != null) loadGuard.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        destroyHowTo();
        dismissTechnicalErrorPopUp();
        dismissWebViewFailureDialog();

        if (realtimeRef != null && realtimeListener != null) {
            realtimeRef.removeEventListener(realtimeListener);
        }
        if (loadGuard != null) {
            loadGuard.onDestroy();
            loadGuard.destroyWebView();
        }
        super.onDestroy();
    }


    private void destroyHowTo() {
        if (how_to_video_view != null)
            how_to_video_view.setVisibility(View.GONE);
        if (howToVideoView != null)
            howToVideoView.remove();
    }

    public void backToPreviousActivity() {
        finish();
    }

    private void onBackPressedClick() {
        try {

//            int orientation = getResources().getConfiguration().orientation;
//
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                return;
//            }

            if (howToVideoView != null && howToVideoView.videoView != null
                    && howToVideoView.videoView.getVisibility() == View.VISIBLE && howToVideoView.expanded) {
                howToVideoView.resize();
                return;
            }
            if (!isBackRestricted) {
                if (webView.canGoBack()) {
                    webView.goBack(); // Go back in WebView history
                } else {
                    finish();
                }
            } else if (StringUtils.isNotEmpty(onBackMessage)) {
//                CommonMethods.showNegativeSnackbar(rl_parent_layout, onBackMessage, 3000);
                if (StringUtils.isNotEmpty(onBackJsMethodCallBack)) {
                    //                    String javascript = "javascript:showSuggestion();";
                    webView.evaluateJavascript(onBackJsMethodCallBack, null);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        runOnUiThread(this::onBackPressedClick);
    }

    public void setJsMethodCallBack(String methodName) {
        this.onBackJsMethodCallBack = methodName;
    }

    public void showPositiveSnackBarMessage(String message) {
        CommonMethods.showPositiveSnackbar(rl_parent_layout, message, 3000);
    }

    public void showNegativeSnackBarMessage(String message) {
        CommonMethods.showNegativeSnackbar(rl_parent_layout, message, 3000);
    }

    public void setOrientationLandscape(boolean isLandscape) {
//        if (isLandscape && !isLandscape()) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        } else if (!isLandscape && !isPortrait()) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
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
        boolean appendUserId = getIntent().getBooleanExtra("appendUserId", true);
        boolean appendUserName = getIntent().getBooleanExtra("appendUserName", true);
        boolean appendUserGender = getIntent().getBooleanExtra("appendUserGender", false);
        boolean appendUserImageUrl = getIntent().getBooleanExtra("appendUserImageUrl", false);
        boolean appendBoard = getIntent().getBooleanExtra("appendBoard", true);
        boolean appendClassId = getIntent().getBooleanExtra("appendClassId", true);
        boolean appendCourseId = getIntent().getBooleanExtra("appendCourseId", true);
        boolean appendToken = getIntent().getBooleanExtra("appendToken", true);
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
        url = appendParamToUrl(url, "navigationType", receivedNavigationType.name());
        url = appendBatchParams(url);
        url = appendParamToUrl(url, "subscriptionType", MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey()));
        url = appendParamToUrl(url, "versionCode", String.valueOf(DeviceInfoUtil.getVersionCode(this)));
        miniVideoUrl = getIntent().getStringExtra("miniVideoUrl");
        Log.d("ShowMINIVIDEO", "ShowMINIVIDEO " + miniVideoUrl);
        return url;
    }

    private String extractFinalUrl(Intent intent) {
        Uri uri = intent.getData(); // Handle deep link case
        String baseUrl = "";
        Map<String, String> params = new HashMap<>();

        if (uri != null && uri.isHierarchical()) {
            // Extract query params from deep link
            Log.d(TAG, "DeepLink: ");
            receivedNavigationType = NavigationType.DEEPLINK;
            for (String key : uri.getQueryParameterNames()) {
                String value = uri.getQueryParameter(key);
                if (value != null) {
                    params.put(key, value);
                }
            }

            // Get the actual URL to load in WebView
            if (params.containsKey("url")) {
                try {
                    baseUrl = URLDecoder.decode(params.get("url"), "UTF-8");
                    params.remove("url"); // remove from query params so we can append remaining if needed
                } catch (Exception e) {
                    e.printStackTrace();
                    baseUrl = params.get("url");
                }
            }

        } else {
            // Case: normal launch with extras
            Log.d(TAG, "Extras: ");
            receivedNavigationType = NavigationType.APP;
            baseUrl = intent.getStringExtra("url");
            Log.d(TAG, "URL: " + baseUrl);
            Serializable serializable = intent.getSerializableExtra("params");
            if (serializable instanceof Map<?, ?> extras) {
                Log.d(TAG, "Params: " + extras.toString());
                for (Map.Entry<?, ?> entry : extras.entrySet()) {
                    params.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
            }
        }

        return buildUrlWithParams(baseUrl, params);
    }

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
        try {
            String batchId = String.valueOf(MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey()));
            if (StringUtils.isNotEmpty(batchId)) url = appendParamToUrl(url, "batchId", batchId);
            String batchName = MyApplication.getStringData(PreferenceKeys.BATCH_NAME.getKey());
            if (StringUtils.isNotEmpty(batchName))
                url = appendParamToUrl(url, "batchName", batchName);
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            if (StringUtils.isNotEmpty(batchCode))
                url = appendParamToUrl(url, "batchCode", batchCode);
            String examId = String.valueOf(MyApplication.getIntData(PreferenceKeys.EXAM_ID.getKey()));
            if (StringUtils.isNotEmpty(examId)) url = appendParamToUrl(url, "examId", examId);
            String examName = MyApplication.getStringData(PreferenceKeys.EXAM_NAME.getKey());
            if (StringUtils.isNotEmpty(examName)) url = appendParamToUrl(url, "examName", examName);
            String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
            if (StringUtils.isNotEmpty(examCode)) url = appendParamToUrl(url, "examCode", examCode);
            String streamId = String.valueOf(MyApplication.getIntData(PreferenceKeys.STREAM_ID.getKey()));
            if (StringUtils.isNotEmpty(streamId)) url = appendParamToUrl(url, "streamId", streamId);
            String streamName = MyApplication.getStringData(PreferenceKeys.STREAM_NAME.getKey());
            if (StringUtils.isNotEmpty(streamName))
                url = appendParamToUrl(url, "streamName", streamName);
            String streamCode = MyApplication.getStringData(PreferenceKeys.STREAM_CODE.getKey());
            if (StringUtils.isNotEmpty(streamCode))
                url = appendParamToUrl(url, "streamCode", streamCode);
            String gradeId = String.valueOf(MyApplication.getIntData(PreferenceKeys.GRADE_ID.getKey()));
            if (StringUtils.isNotEmpty(gradeId)) url = appendParamToUrl(url, "gradeId", gradeId);
            String gradeName = MyApplication.getStringData(PreferenceKeys.GRADE_NAME.getKey());
            if (StringUtils.isNotEmpty(gradeName))
                url = appendParamToUrl(url, "gradeName", gradeName);
            String gradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());
            if (StringUtils.isNotEmpty(gradeCode))
                url = appendParamToUrl(url, "gradeCode", gradeCode);
            String yearId = String.valueOf(MyApplication.getIntData(PreferenceKeys.YEAR_ID.getKey()));
            if (StringUtils.isNotEmpty(yearId)) url = appendParamToUrl(url, "yearId", yearId);
            String yearName = MyApplication.getStringData(PreferenceKeys.YEAR_NAME.getKey());
            if (StringUtils.isNotEmpty(yearName)) url = appendParamToUrl(url, "yearName", yearName);
            String yearCode = MyApplication.getStringData(PreferenceKeys.YEAR_CODE.getKey());
            if (StringUtils.isNotEmpty(yearCode)) url = appendParamToUrl(url, "yearCode", yearCode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return url;
    }

    private void setMiniVideoPlayer() {
        Log.d(TAG, "setMiniVideoPlayer: ");
        try {
            howToVideoView = HowToVideoView.getInstance(this);

            if (howToVideoView.videoView == null || howToVideoView.videoView != how_to_video_view) {
                howToVideoView.setView(this, how_to_video_view);
            }

            if (howToVideoView.videoView.getVisibility() != View.VISIBLE) {
                howToVideoView.setUrl(miniVideoUrl);
                howToVideoView.setType(DYNAMIC_URL, false);
                howToVideoView.setButtonText(getString(R.string.start_now));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void openSubscriptionScreen(String jsonDto) {
        try {
            Gson gson = new Gson();
            SubscriptionLockPopupDto subscriptionLockPopupDto = gson.fromJson(jsonDto, SubscriptionLockPopupDto.class);

            String finalSource = "neet_weak_topic";
            Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntentWithSource(this, finalSource);
            intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
            subscriptionScreenLauncher.launch(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    public void startRealtimeDbListener(String path) {
        realtimeRef = FirebaseDatabase.getInstance().getReference(path);

        realtimeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    if (snapshot.exists()) {
                        ObjectMapper mapper = new ObjectMapper();
                        String json = mapper.writeValueAsString(snapshot.getValue());

                        Log.d(TAG, "Listener Value: " + json);
                        String safeJson = json.replace("'", "\\'");

                        String callback =
                                "javascript:onRealtimeDbUpdate('" + path + "','" + safeJson + "')";

                        runOnUiThread(() ->
                                webView.evaluateJavascript(callback, null)
                        );
                    }
                } catch (Exception e) {
                    Log.e(TAG, "JSON error", e);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Listener cancelled: " + error.getMessage());
            }
        };

        realtimeRef.addValueEventListener(realtimeListener);
    }

    public void setTopicSelection(String topicId) {
        MyApplication.saveData(PreferenceKeys.ADAPTIVE_SELECTED_TOPIC_ID.getKey(), topicId);
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
                            sendUrisToWebView(urls);
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraUri != null) {
//                        sendUrisToWebView(
//                                Collections.singletonList(cameraUri.toString())
//                        );
                        openCropActivity(cameraUri);
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
                        }
                    }
                }
        );
    }

    public void openGallery(int maxCount) {
        if (maxCount <= 0) {
            return;
        }
        Intent intent = new Intent(this, ImageCropperActivity.class);
        intent.putExtra("allowedImages", maxCount);
//        intent.putExtra("remainingImages", maxCount);
        intent.putExtra("errorText", getString(maxCount>1?R.string.oops_sirf_images_allowed_hain_multiple:R.string.oops_sirf_images_allowed_hain_single));
        galleryLauncher.launch(intent);
    }

    public void listenKeyboardEnter() {
        View view = getCurrentFocus();
        if (view == null) return;

        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {

                notifyEnterPressedToWebView();
                return true; // consume event
            }
            return false;
        });
    }

    public void goToHomeScreen() {
        Intent intent = new Intent(this, HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void openGuideVideo() {
        Intent intent = new Intent(this, FullScreenVideoActivity.class);
        intent.putExtra("close", true);
        startActivity(intent);
        finish();
    }

    private void notifyEnterPressedToWebView() {
        runOnUiThread(() ->
                webView.evaluateJavascript(
                        "window.onKeyboardEnterPressed && window.onKeyboardEnterPressed();",
                        null
                )
        );
    }

    public void minimizeKeyboard() {
        CommonMethods.hideKeyboard(this, getCurrentFocus());
    }

    public void openCamera() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    101
            );
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,
                "camera_" + System.currentTimeMillis());

        cameraUri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        cameraLauncher.launch(cameraUri);
    }

//    public void sendUrisToWebView(List<String> uris) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            String json = mapper.writeValueAsString(uris);
//
//            String js = "javascript:onImagesSelected(" + json + ")";
//
//            runOnUiThread(() ->
//                    webView.evaluateJavascript(js, null)
//            );
//
//            Log.d(TAG, "URIs List : " + uris.size() + ", " + uris.toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public boolean isImageSizeValid(Uri uri) {

        File file = new File(uri.getPath());
        float lengthmb = (float) (file.length() / 1024) / 1024;

        return (lengthmb <= 10) ;

    }
    public void sendUrisToWebView(List<String> uris) {

        boolean isSizeValid = true;
        for(String uri : uris) {
            if(!isImageSizeValid(Uri.parse(uri))) {
                isSizeValid = false;
            }
        }

        if(!isSizeValid) {
            CommonMethods.showNegativeSnackbar(rl_parent_layout, "Upload failed: Images 10MB se badi hain.", 3000);
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<ImagePayload> payloadList = new ArrayList<>();

                for (String uriString : uris) {
                    Uri uri = Uri.parse(uriString);

                    String base64 = convertUriToBase64(uri);
                    payloadList.add(new ImagePayload(uriString, base64));
                }

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(payloadList);

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

        // 2️⃣ Read image bounds
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try (InputStream boundsStream =
                     getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(boundsStream, null, options);
        }

        // 3️⃣ Calculate sample size
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
                        : Bitmap.CompressFormat.JPEG; // IMPORTANT FIX

        bitmap.compress(format, quality, outputStream);

        byte[] compressedBytes = outputStream.toByteArray();

        // Convert to Base64
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

    public static class ImagePayload {
        public String uri;
        public String base64;

        public ImagePayload(String uri, String base64) {
            this.uri = uri;
            this.base64 = base64;
        }
    }

}