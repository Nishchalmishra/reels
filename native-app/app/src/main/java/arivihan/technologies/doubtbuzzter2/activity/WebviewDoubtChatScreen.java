package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_CHAT_HISTORY;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TUTOR_SUPPORT_CHECK_USER_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_DOUBT_STATUS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.CategorizePreviousDoubtListAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.DoubtChatHost;
import arivihan.technologies.doubtbuzzter2.helper.WebViewDoubtChatInterface;
import arivihan.technologies.doubtbuzzter2.helper.WebViewLoadGuard;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.model.DoubtCategoryObject;
import arivihan.technologies.doubtbuzzter2.model.ArivihanSession;
import arivihan.technologies.doubtbuzzter2.model.ArivihanSessionHistoryData;
import arivihan.technologies.doubtbuzzter2.model.DoubtChatHistoryData;
import arivihan.technologies.doubtbuzzter2.model.DoubtChatLimitationCheckDto;
import arivihan.technologies.doubtbuzzter2.model.DoubtSolutionStatus;
import arivihan.technologies.doubtbuzzter2.model.WebChatRecord;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class WebviewDoubtChatScreen extends AppCompatActivity implements DoubtChatHost {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------
    private static final String TAG = WebviewDoubtChatScreen.class.getSimpleName();
    private static final int FILE_REQUEST_CODE       = 1;
    private static final int REQUEST_CODE_MIC        = 2;
    private static final int CAMERA_PERMISSION_CODE  = 100;
    private static final int GALLERY_CODE            = 118;
    private static final int CAPTURE_IMAGE_CODE      = 19;
    private static final int DEFAULT_IMAGE_QUALITY   = 80;
    private static final int IMAGE_MAX_DIMENSION     = 1080;
    private static final long SLOW_LOAD_THRESHOLD_MS = 3_000;
    private static final float MAX_IMAGE_SIZE_MB     = 15f;
    private String lastLoadedUrl = "";

    // -------------------------------------------------------------------------
    // Intent extras — grouped in a plain holder so onCreate stays readable
    // -------------------------------------------------------------------------
    /** All extras read from the launching Intent. */
    private static final class IntentExtras {
        String chatSessionId;
        boolean openOldChat;
        String subject;
        String subjectId;
        String subjectName;
        String response;           // intentResponse
        String userQuery;
        boolean showResponseBubble;
        String origin;
        String title;              // intentTitle
        String extractedText;
        String screenshotUrl;
        String requestId;
        boolean allowBack;
        boolean isMockTestDoubt;
        String question;
        String answer;
        String type;
        String status;
        boolean navigatingFromMyDoubt;
        boolean pseudoLive;
        boolean videoRequest;
        String doubtText;
        boolean openDoubtChatForCqnq;
        String idealAnswer;
        String analysisOfAnswer;
        String explanationOfQuestion;
        String obtainedMarks;
        String totalMarks;
        String questionType;
        String imageExplanationLatex;
        String imageQuestion;
        String imageAnswer;
        String pdfTitle;
        int pageNumber;
        String pageContext;
        String chapterCode;
        String microlectureCode;


    }

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------
    private DrawerLayout       drawerLayout;
    private RelativeLayout     drawerSubscriptionLayout;
    private RelativeLayout     webViewLoader;
    private LottieAnimationView webViewLottieAnimation;
    private RecyclerView       previousDoubtRecyclerView;
    private WebView            webView;
    private CardView           subscriptionButton;
    private TextView           chatUserName;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------
    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private IntentExtras extras;
    private DoubtChatLimitationCheckDto doubtChatLimitationCheckDto;
    private DoubtChatHistoryData        doubtChatHistoryData;
    private final ArrayList<DoubtCategoryObject> categoryLists = new ArrayList<>();

    private Dialog   subscriptionDialog;
    private boolean  showingSubscriptionDialog = false;
    private boolean  showingSuggestion         = false;
    private int      webViewLoadProgress       = 0;
    private long     webviewStartTime;
    private WebViewLoadGuard loadGuard;

    // File upload (from WebChromeClient)
    private ValueCallback<Uri[]> mUploadMessage;

    // Firebase
    private DatabaseReference  dataRef;
    private ValueEventListener firebaseListener;

    // Activity-result launchers
    private ActivityResultLauncher<Intent> cropActivityLauncher;
    private ActivityResultLauncher<Intent> subscriptionLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    // -------------------------------------------------------------------------
    // Lifecycle — onCreate
    // -------------------------------------------------------------------------
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_doubt_chat_screen);
        bindViews();
        readIntentExtras();
        registerActivityLaunchers();

        chatUserName.setText(MyApplication.getStringData(PreferenceKeys.NAME.getKey()));

        checkSubscription();
        loadPreviousDoubts();
        setupWebView(savedInstanceState);
        setupFirebaseListener();
        setupBackHandler();
    }

    // -------------------------------------------------------------------------
    // Lifecycle — resume / stop / destroy
    // -------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        if (loadGuard != null) loadGuard.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataRef != null && firebaseListener != null) {
            dataRef.addValueEventListener(firebaseListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loadGuard != null) loadGuard.onStop();
        removeFirebaseListener();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (loadGuard != null) loadGuard.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissSubscriptionDialog();
        removeFirebaseListener();
        if (loadGuard != null) {
            loadGuard.onDestroy();
            loadGuard.destroyWebView();
        }
    }

    // -------------------------------------------------------------------------
    // Initialisation helpers
    // -------------------------------------------------------------------------
    private void bindViews() {
        drawerLayout              = findViewById(R.id.drawer_layout);
        drawerSubscriptionLayout  = findViewById(R.id.subscription_layout);
        subscriptionButton        = findViewById(R.id.subscribe_now_btn);
        previousDoubtRecyclerView = findViewById(R.id.previous_doubts_recycler_view);
        webViewLoader             = findViewById(R.id.webview_loader);
        webViewLottieAnimation    = findViewById(R.id.webView_lottie_animation);
        chatUserName              = findViewById(R.id.chat_user_name);
        webView                   = findViewById(R.id.webview);

        subscriptionButton.setOnClickListener(v -> launchSubscriptionPage());
    }

    private void readIntentExtras() {
        extras = new IntentExtras();

        // chatSessionId may arrive via deep-link or regular extra
        Uri deepLink = getIntent().getData();
        extras.chatSessionId = (deepLink != null)
                ? deepLink.getQueryParameter("chatSessionId")
                : getIntent().getStringExtra("chatSessionId");

        extras.openOldChat            = getIntent().getBooleanExtra("openOldChat", false);
        extras.subject                = getIntent().getStringExtra("subject");
        extras.subjectId              = getIntent().getStringExtra("subjectId");
        extras.subjectName            = getIntent().getStringExtra("subjectName");
        extras.response               = getIntent().getStringExtra("response");
        extras.userQuery              = getIntent().getStringExtra("userQuery");
        extras.showResponseBubble     = getIntent().getBooleanExtra("showResponseBubble", false);
        extras.origin                 = getIntent().getStringExtra("origin");
        extras.title                  = getIntent().getStringExtra("title");
        extras.extractedText          = getIntent().getStringExtra("extractedText");
        extras.screenshotUrl          = getIntent().getStringExtra("screenshotUrl");
        extras.requestId              = getIntent().getStringExtra("requestId");
        extras.allowBack              = getIntent().getBooleanExtra("allowBack", false);
        extras.isMockTestDoubt        = getIntent().getBooleanExtra("isMockTestDoubt", false);
        extras.question               = getIntent().getStringExtra("question");
        extras.answer                 = getIntent().getStringExtra("answer");
        extras.type                   = getIntent().getStringExtra("type");
        extras.status                 = getIntent().getStringExtra("status");
        extras.navigatingFromMyDoubt  = getIntent().getBooleanExtra("navigatingFromMyDoubt", false);
        extras.pseudoLive             = getIntent().getBooleanExtra("pseudoLive", false);
        extras.videoRequest           = getIntent().getBooleanExtra("videoRequest", false);
        extras.doubtText              = getIntent().getStringExtra("doubt_text");
        extras.openDoubtChatForCqnq   = getIntent().getBooleanExtra("openDoubtChatForCqnq", false);
        extras.idealAnswer            = getIntent().getStringExtra("idealAnswer");
        extras.analysisOfAnswer       = getIntent().getStringExtra("analysisOfAnswer");
        extras.explanationOfQuestion  = getIntent().getStringExtra("explanationOfQuestion");
        extras.obtainedMarks          = getIntent().getStringExtra("obtainedMarks");
        extras.totalMarks             = getIntent().getStringExtra("totalMarks");
        extras.questionType           = getIntent().getStringExtra("questionType");
        extras.imageExplanationLatex  = getIntent().getStringExtra("imageExplanationLatex");
        extras.imageQuestion          = getIntent().getStringExtra("imageQuestion");
        extras.imageAnswer            = getIntent().getStringExtra("imageAnswer");
        extras.pdfTitle               = getIntent().getStringExtra("pdfTitle");
        extras.pageNumber             = getIntent().getIntExtra("pageNumber", 0);
        extras.microlectureCode       = getIntent().getStringExtra("microlectureCode");
        extras.chapterCode      =      getIntent().getStringExtra("chapterCode");
        extras.pageContext=getIntent().getStringExtra("pageContext");
    }

    private void registerActivityLaunchers() {
        subscriptionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        boolean subscribed = result.getData().getBooleanExtra("isSubscribed", false);
                        if (subscribed) {
                            Log.d(TAG, "Subscription updated — recreating activity");
                            recreate();
                        }
                    }
                });

        cropActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        Log.d(TAG, "Crop cancelled or failed");
                        return;
                    }
                    try {
                        ArrayList<String> urls = result.getData().getStringArrayListExtra("url_list");
                        String questionText    = result.getData().getStringExtra("question");
                        Uri uri  = Uri.parse(urls.get(0));
                        File file = new File(uri.getPath());
                        float sizeMb = file.length() / (1024f * 1024f);
                        if (sizeMb > MAX_IMAGE_SIZE_MB) {
                            Toast.makeText(this, "Image size larger than 15 MB", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String base64 = compressAndConvertToBase64(uri);
                        MyApplication.saveData(PreferenceKeys.ABCD.getKey(), base64);
                        evaluateJavascript(
                                "processCroppedImageWithQuestion('" + base64 + "','" + questionText + "');");
                    } catch (Exception e) {
                        Log.e(TAG, "Crop result error", e);
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null
                            && result.getData().getData() != null) {
                        openCropActivity(result.getData().getData().toString());
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        openCropActivity(MyApplication.getStringData(
                                PreferenceKeys.CAMERA_CAPTURE_URI.getKey()));
                    }
                });
    }

    // -------------------------------------------------------------------------
    // WebView setup
    // -------------------------------------------------------------------------
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(@Nullable Bundle savedInstanceState) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setTextZoom(100);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // Block multi-touch zoom gestures
        webView.setOnTouchListener((v, event) -> event.getPointerCount() > 1);
        webView.setHapticFeedbackEnabled(false);
        webView.setOnLongClickListener(v -> true);
        webView.setLongClickable(false);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        webView.addJavascriptInterface(
                new WebViewDoubtChatInterface(this, drawerLayout, webView),
                "AndroidInterface");

        // Early reveal is off here: onWebViewFullyLoaded pushes tooltips, the suggestion and
        // the chat context into the page, and a mounted #root is not yet proof that the page
        // has registered the JS handlers that receive them. This screen keeps its own
        // progress-100 signal; what it gains from the guard is the stall watchdog.
        loadGuard = new WebViewLoadGuard(this, webView, TAG,
                reason -> onWebViewFullyLoaded(), drawerLayout)
                .setEarlyRevealEnabled(false);

        webView.setWebViewClient(buildWebViewClient());
        webView.setWebChromeClient(buildWebChromeClient());

        loadGuard.attach(savedInstanceState);
        String url = buildWebViewUrl();
        loadGuard.onLoadStarted(url);
        webView.loadUrl(url);
    }

    private WebViewClient buildWebViewClient() {
        return new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Bundle bundle = new Bundle();
                bundle.putString("error", StringUtils.defaultString(error.toString()));
                bundle.putString("app_version", MyApplication.getAppVersionName(WebviewDoubtChatScreen.this));
                MyApplication.logFirebaseEvent("doubt_chat_ssl_error", bundle);
                loadGuard.onMainFrameError("ssl error: " + error);
                handler.cancel();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webviewStartTime = System.currentTimeMillis();
                loadGuard.onPageStarted(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "onPageFinished: " + url);
                if (System.currentTimeMillis() - webviewStartTime > SLOW_LOAD_THRESHOLD_MS) {
                    Toast.makeText(WebviewDoubtChatScreen.this,
                            R.string.taking_longer_than_usual_please_try_again,
                            Toast.LENGTH_SHORT).show();
                }
                // Second reveal path alongside progress==100: onPageFinished can land first
                // when the page has no measurable progress ticks left to report.
                loadGuard.onPageFinished(url);
                if (extras.openDoubtChatForCqnq) {
                    pushAskMoreContext(view);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // Subresource failures must not blank the screen — only a main-frame failure
                // means the page itself did not load.
                if (request.isForMainFrame()) {
                    loadGuard.onMainFrameError("code=" + error.getErrorCode()
                            + ", " + error.getDescription());
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                            WebResourceResponse errorResponse) {
                if (request.isForMainFrame()) {
                    loadGuard.onMainFrameError("HTTP " + errorResponse.getStatusCode());
                }
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
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
        };
    }

    private WebChromeClient buildWebChromeClient() {
        return new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView wv, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, FILE_REQUEST_CODE);
                    return false;
                }
                mUploadMessage = filePathCallback;
                try {
                    galleryLauncher.launch(fileChooserParams.createIntent());
                } catch (ActivityNotFoundException e) {
                    mUploadMessage = null;
                    Toast.makeText(WebviewDoubtChatScreen.this,
                            "Cannot open file chooser", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                String[] resources = request.getResources();
                if (resources.length != 1) {
                    super.onPermissionRequest(request);
                    return;
                }
                switch (resources[0]) {
                    case PermissionRequest.RESOURCE_AUDIO_CAPTURE:
                        grantOrRequestWebPermission(request,
                                Manifest.permission.RECORD_AUDIO, REQUEST_CODE_MIC);
                        break;
                    case PermissionRequest.RESOURCE_VIDEO_CAPTURE:
                        grantOrRequestWebPermission(request,
                                Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
                        break;
                    default:
                        super.onPermissionRequest(request);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                webViewLoadProgress = newProgress;
                loadGuard.onProgressChanged(newProgress);
                handleWebViewLoadingState();
            }
        };
    }

    /**
     * Grants a WebView permission immediately if the native permission is already
     * granted, otherwise triggers the system permission request.
     */
    private void grantOrRequestWebPermission(PermissionRequest request,
                                             String nativePermission, int requestCode) {
        if (hasPermission(nativePermission)) {
            request.grant(request.getResources());
        } else {
            requestPermission(nativePermission, requestCode);
        }
    }

    // -------------------------------------------------------------------------
    // URL construction
    // -------------------------------------------------------------------------
    private String buildWebViewUrl() {
        String url;
        String env = EnvironmentManager.getEnvironmentNameByUrl(
                MyApplication.getEndPoint()).toLowerCase();
        if (extras.openDoubtChatForCqnq) {
            String remoteKey = "ask_more_web_web_view_url"
                    + "_" + env;
            String baseUrl = MyApplication.getFirebaseRemoteConfig().getString(remoteKey);
            url=buildUrlWithParams(baseUrl);
            ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.ASK_DOUBT_SCREEN.name(), AnalyticsEvent.ASK_DOUBT_PRACTICE.name());
        } else if ("PDF_CIRCLE".equals(extras.origin)) {
            String remoteKey = "ask_more_pdf_circle"
                    + "_" + env;
            String baseUrl = MyApplication.getFirebaseRemoteConfig().getString(remoteKey);
            ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.ASK_DOUBT_SCREEN.name(), AnalyticsEvent.ASK_DOUBT_PDF_CIRCLE.name());
            url=buildUrlWithParams(baseUrl);
        } else {
            url = buildStandardChatUrl();
        }
        lastLoadedUrl = url;
        return url;
    }

    private String buildStandardChatUrl() {
        boolean isOldChatSupported = MyApplication.getFirebaseRemoteConfig()
                .getBoolean("doubt_chat_old_chat_supported");

        String env = EnvironmentManager.getEnvironmentNameByUrl(
                MyApplication.getEndPoint()).toLowerCase();

        String remoteKey = "doubt_chat_web_web_view_url"
                + "_" + env
                + ((isOldChatSupported && extras.openOldChat) ? "" : "_v2");
        String baseUrl = MyApplication.getFirebaseRemoteConfig().getString(remoteKey);
        ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.ASK_DOUBT_SCREEN.name(),AnalyticsEvent.ASK_DOUBT_SCREEN.name());
        return buildUrlWithParams(baseUrl);
    }

    private String buildUrlWithParams(String baseUrl) {
        String env = EnvironmentManager.getEnvironmentNameByUrl(
                MyApplication.getEndPoint()).toLowerCase();

        String courseId = MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())
                ? "2"
                : MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey());

        String url = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter("token",            MyApplication.getFCMToken())
                .appendQueryParameter("userId",           MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
                .appendQueryParameter("board",            String.valueOf(MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())))
                .appendQueryParameter("courseId",         courseId)
                .appendQueryParameter("classId",          MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()))
                .appendQueryParameter("class",            MyApplication.getStringData(PreferenceKeys.CLASS_NAME.getKey()))
                .appendQueryParameter("subject",          nullToEmpty(extras.subject))
                .appendQueryParameter("subjectId",        nullToEmpty(extras.subjectId))
                .appendQueryParameter("subjectName",      nullToEmpty(extras.subjectName))
                .appendQueryParameter("origin",           "home_ask_doubt")
                .appendQueryParameter("language",         MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()))
                .appendQueryParameter("username",         MyApplication.getStringData(PreferenceKeys.NAME.getKey()))
                .appendQueryParameter("chatSessionId",    nullToEmpty(extras.chatSessionId))
                .appendQueryParameter("env",              env)
                .appendQueryParameter("batchId",          String.valueOf(MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey())))
                .appendQueryParameter("batchName",        MyApplication.getStringData(PreferenceKeys.BATCH_NAME.getKey()))
                .appendQueryParameter("batchCode",        MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey()))
                .appendQueryParameter("hidProPlanSubscription", String.valueOf(SubscriptionNavigationHelper.hideProPlanSubscription()))
                .appendQueryParameter("examId",           String.valueOf(MyApplication.getIntData(PreferenceKeys.EXAM_ID.getKey())))
                .appendQueryParameter("examName",         MyApplication.getStringData(PreferenceKeys.EXAM_NAME.getKey()))
                .appendQueryParameter("examCode",         MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey()))
                .appendQueryParameter("streamId",         String.valueOf(MyApplication.getIntData(PreferenceKeys.STREAM_ID.getKey())))
                .appendQueryParameter("streamName",       MyApplication.getStringData(PreferenceKeys.STREAM_NAME.getKey()))
                .appendQueryParameter("streamCode",       MyApplication.getStringData(PreferenceKeys.STREAM_CODE.getKey()))
                .appendQueryParameter("gradeId",          String.valueOf(MyApplication.getIntData(PreferenceKeys.GRADE_ID.getKey())))
                .appendQueryParameter("gradeName",        MyApplication.getStringData(PreferenceKeys.GRADE_NAME.getKey()))
                .appendQueryParameter("gradeCode",        MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey()))
                .appendQueryParameter("yearId",           String.valueOf(MyApplication.getIntData(PreferenceKeys.YEAR_ID.getKey())))
                .appendQueryParameter("yearName",         MyApplication.getStringData(PreferenceKeys.YEAR_NAME.getKey()))
                .appendQueryParameter("yearCode",         MyApplication.getStringData(PreferenceKeys.YEAR_CODE.getKey()))
                .appendQueryParameter("subscriptionType", (MyApplication.isCompetitiveProfile() && SubscriptionNavigationHelper.hasActiveSubscriptionByBatch())? SubscriptionType.PRO.name() : MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey()))
                .appendQueryParameter("versionCode", String.valueOf(DeviceInfoUtil.getVersionCode(this)))
                .build().toString();

        Log.i(TAG, "WebView URL: " + url);
        return url;
    }

    // -------------------------------------------------------------------------
    // WebView loading state
    // -------------------------------------------------------------------------
    private void handleWebViewLoadingState() {
        if (webViewLoadProgress < 100) {
            runOnUiThread(() -> {
                // INVISIBLE, never GONE — a GONE view is skipped during layout, leaving the
                // WebView 0×0, and Chromium cannot commit a navigation into an empty viewport.
                if (webView.getVisibility() != View.VISIBLE) {
                    webView.setVisibility(View.INVISIBLE);
                }
                if (!webViewLottieAnimation.isAnimating()) {
                    webViewLottieAnimation.playAnimation();
                }
                webViewLoader.setVisibility(View.VISIBLE);
            });
        } else {
            // Through the guard rather than straight to onWebViewFullyLoaded, so the reveal
            // stays once-only and the stall watchdog is stood down with it.
            runOnUiThread(() -> loadGuard.revealNow("progress-100"));
        }
    }

    private void onWebViewFullyLoaded() {
        webView.setVisibility(View.VISIBLE);
        webViewLottieAnimation.cancelAnimation();
        webViewLoader.setVisibility(View.GONE);

        showTooltipsIfNeeded();
        showSuggestionIfNeeded();
        injectContextIfNeeded();
        logLoadTime();
    }

    private void showTooltipsIfNeeded() {
        if (!showingSuggestion
                && MyApplication.getBoolData(PreferenceKeys.SHOW_DOUBT_SUGGESTION.getKey())
                && !MyApplication.getBoolData(PreferenceKeys.SHOW_DOUBT_SCREEN_TOOLTIPS.getKey())
                && doubtChatHistoryData != null
                && doubtChatHistoryData.getList() != null
                && !doubtChatHistoryData.getList().isEmpty()) {
            evaluateJavascript("showTooltips(true);");
            MyApplication.saveData(PreferenceKeys.SHOW_DOUBT_SCREEN_TOOLTIPS.getKey(), true);
        }
    }

    private void showSuggestionIfNeeded() {
        if (!MyApplication.getBoolData(PreferenceKeys.SHOW_DOUBT_SUGGESTION.getKey())) {
            evaluateJavascript("showSuggestion();");
            MyApplication.saveData(PreferenceKeys.SHOW_DOUBT_SUGGESTION.getKey(), true);
            showingSuggestion = true;
        }
    }

    private void injectContextIfNeeded() {
        if (extras.response == null || extras.response.isEmpty()) return;

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("userQuery",          extras.userQuery);
        ctx.put("subject",          extras.subject);
        ctx.put("response",           extras.response);
        ctx.put("showResponseBubble", extras.showResponseBubble);
        ctx.put("origin",             extras.origin);
        ctx.put("title",              extras.title);
        ctx.put("extractedText",      extras.extractedText);
        ctx.put("screenshotUrl",      extras.screenshotUrl);
        ctx.put("requestId",          extras.requestId);
        ctx.put("pdfTitle",           extras.pdfTitle);
        ctx.put("pageNumber",         extras.pageNumber);
        ctx.put("pageContext",        extras.pageContext);
        Log.d(TAG, "Injecting chat context: " + ctx);
        try {
            String json = objectMapper.writeValueAsString(ctx);

            // ── combined log ──────────────────────────────────────────────────
            Log.i(TAG, "┌─── WebView final URL ────────────────────────────────");
            Log.i(TAG, "│  " + lastLoadedUrl);
            Log.i(TAG, "├─── Injected context ────────────────────────────────");
            Log.i(TAG, "│  " + json);
            Log.i(TAG, "└─────────────────────────────────────────────────────");

            evaluateJavascript("setContextForChat(" + json + ");");
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to serialize chat context", e);
        }
    }

    private void logLoadTime() {
        try {
            Bundle bundle = new Bundle();
            // We no longer have startTime here; log a marker instead.
            bundle.putLong("progress_reached_100_at_ms", System.currentTimeMillis());
            MyApplication.logFirebaseEvent("time_taken_to_load_doubt_webview", bundle);
        } catch (Exception ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // Firebase
    // -------------------------------------------------------------------------
    private void setupFirebaseListener() {
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        dataRef = FirebaseDatabase.getInstance()
                .getReference("users/doubt_chat_refresh/" + userId);

        firebaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String sessionId = extras.chatSessionId;
                if (sessionId == null) return;
                Object val = snapshot.child(sessionId).getValue();
                if ("yes".equals(val)) {
                    evaluateJavascript("reloadHistoryForNewResponse();");
                    dataRef.child(sessionId).removeValue()
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to clear Firebase refresh flag", e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase listener cancelled: " + error.getMessage());
            }
        };
    }

    private void removeFirebaseListener() {
        if (dataRef != null && firebaseListener != null) {
            dataRef.removeEventListener(firebaseListener);
        }
    }

    // -------------------------------------------------------------------------
    // Back handler
    // -------------------------------------------------------------------------
    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Subscription
    // -------------------------------------------------------------------------
    private void launchSubscriptionPage() {
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.DOUBT_CHAT_JOIN);
        subscriptionLauncher.launch(intent);
    }

    private void checkSubscription() {
        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                TUTOR_SUPPORT_CHECK_USER_SUBSCRIPTION,
                null,
                response -> {
                    doubtChatLimitationCheckDto = gson.fromJson(
                            response.toString(),
                            new TypeToken<DoubtChatLimitationCheckDto>() {}.getType());

                    Log.i(TAG, "Subscription check: " + doubtChatLimitationCheckDto);
                    boolean subscribed = doubtChatLimitationCheckDto.isSubscriptionStatus();
                    MyApplication.saveData(PreferenceKeys.DOUBT_CHAT_SUBSCRIBED.getKey(), subscribed);

                    if (subscribed) {
                        collapseSubscriptionBanner();
                    }

                    if (!isFinishing() && !isDestroyed()) {
                        boolean limitReached = !subscribed
                                && doubtChatLimitationCheckDto.getChatSessionsCount()
                                >= doubtChatLimitationCheckDto.getSessionsLimitPerUser();

                        if (limitReached) {
                            evaluateJavascript("showDoubtSubscriptionDialog();");
                            MyApplication.saveData(PreferenceKeys.WEB_VIEW_TOUCH_DISABLED.getKey(), true);
                        } else {
                            MyApplication.saveData(PreferenceKeys.WEB_VIEW_TOUCH_DISABLED.getKey(), false);
                        }
                    }
                    handleWebViewLoadingState();
                },
                new VolleyGlobalErrorListener(getApplicationContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "Subscription check error: " + error);
                        handleWebViewLoadingState();
                    }
                });

        request.setTag("WEBVIEW_DOUBT_CHAT_SCREEN");
        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void collapseSubscriptionBanner() {
        drawerSubscriptionLayout.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams lp =
                (RelativeLayout.LayoutParams) drawerSubscriptionLayout.getLayoutParams();
        lp.height = 0;
        drawerSubscriptionLayout.setLayoutParams(lp);
    }

    public void showDialogForSubscription() {
        if (isFinishing() || isDestroyed() || showingSubscriptionDialog) return;

        subscriptionDialog = new Dialog(this);
        subscriptionDialog.setContentView(R.layout.custom_dialog_lock_box);
        subscriptionDialog.getWindow().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        subscriptionDialog.getWindow().setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subscriptionDialog.setCancelable(true);

        TextView header = subscriptionDialog.findViewById(R.id.header);
        header.setText(R.string.chat_limit_reached_popup_text);

        MaterialCardView subscriptionLayout = subscriptionDialog.findViewById(R.id.subscriptionLayout);
        subscriptionLayout.setOnClickListener(v -> launchSubscriptionPage());

        ImageView cancel = subscriptionDialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            subscriptionDialog.dismiss();
            showingSubscriptionDialog = false;
            Toast.makeText(this, R.string.buy_plan_to_continue, Toast.LENGTH_SHORT).show();
        });

        subscriptionDialog.setOnCancelListener(v -> showingSubscriptionDialog = false);
        subscriptionDialog.show();
        showingSubscriptionDialog = true;
    }

    private void dismissSubscriptionDialog() {
        if (subscriptionDialog != null && subscriptionDialog.isShowing()) {
            subscriptionDialog.dismiss();
        }
    }

    // -------------------------------------------------------------------------
    // Previous doubts
    // -------------------------------------------------------------------------
    private void loadPreviousDoubts() {
        Map<String, Object> params = new HashMap<>();
        params.put(PreferenceKeys.USER_ID.getKey(),
                MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        params.put("subjectId", "");
        params.put("sortOrder", false);
        params.put("query", "");

        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(DOUBT_CHAT_HISTORY, params),
                null,
                response -> {
                    Log.i(TAG, "Previous doubts: " + response);
                    ArivihanSessionHistoryData historyData = gson.fromJson(
                            response.toString(),
                            new TypeToken<ArivihanSessionHistoryData>() {}.getType());
                    List<WebChatRecord> records = new ArrayList<>();
                    if (historyData != null && historyData.getList() != null) {
                        for (ArivihanSession session : historyData.getList()) {
                            records.add(session.toWebChatRecord());
                        }
                    }
                    doubtChatHistoryData = new DoubtChatHistoryData();
                    doubtChatHistoryData.setList(records);
                    renderPreviousDoubtsList(records);
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "loadPreviousDoubts error: " + error);
                    }
                });

        request.setTag("DOUBT_CHAT_SCREEN");
        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void renderPreviousDoubtsList(List<WebChatRecord> list) {
        divideIntoCategories(list);

        previousDoubtRecyclerView.setNestedScrollingEnabled(false);
        previousDoubtRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        previousDoubtRecyclerView.setAdapter(
                new CategorizePreviousDoubtListAdapter(this, categoryLists, true));

        CardView chatHistoryBtn = findViewById(R.id.chat_history_btn);
        if (list.isEmpty()) {
            TextView textView = findViewById(R.id.previous_chat_text);
            textView.setText(getString(R.string.no_chat_history));
            chatHistoryBtn.setVisibility(View.GONE);
        } else {
            chatHistoryBtn.bringToFront();
            chatHistoryBtn.setVisibility(View.VISIBLE);
            chatHistoryBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, DoubtHistoryActivity.class)));
        }
    }

    private void divideIntoCategories(List<WebChatRecord> list) {
        Map<String, ArrayList<WebChatRecord>> grouped = new HashMap<>();
        categoryLists.clear();

        for (WebChatRecord record : list) {
            String category = record.getHistoryCategory();
            grouped.computeIfAbsent(category, k -> new ArrayList<>()).add(record);
        }
        for (Map.Entry<String, ArrayList<WebChatRecord>> entry : grouped.entrySet()) {
            categoryLists.add(new DoubtCategoryObject(entry.getKey(), entry.getValue()));
        }
        Collections.reverse(categoryLists);
    }

    // -------------------------------------------------------------------------
    // Doubt status update
    // -------------------------------------------------------------------------
    private void updateDoubtStatus() {
        Map<String, Object> params = new HashMap<>();
        params.put("chatSessionId", extras.chatSessionId);
        params.put("status", DoubtSolutionStatus.SOLVED_SEEN.name());

        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(UPDATE_DOUBT_STATUS, params),
                null,
                response -> Log.i(TAG, "updateDoubtStatus: " + response),
                new VolleyGlobalErrorListener(getApplicationContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "updateDoubtStatus error: " + error);
                    }
                });

        request.setTag("DOUBT_CHAT_SCREEN");
        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    // -------------------------------------------------------------------------
    // AskMore (CQNQ) context bridge
    // -------------------------------------------------------------------------
    /**
     * Serialises the current CQNQ context and pushes it into the WebView via the
     * JS bridge. Called from onPageFinished so it re-pushes on every load; the
     * React receiver (window.showAskMoreContext) is idempotent.
     */
    private void pushAskMoreContext(WebView view) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("userId",       MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        ctx.put("sessionId",    nullToEmpty(extras.chatSessionId));
        ctx.put("studentName",  MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
        ctx.put("className",    MyApplication.getStringData(PreferenceKeys.CLASS_NAME.getKey()));
        ctx.put("phoneNumber",  MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey()));
        ctx.put("questionType", nullToEmpty(extras.questionType));
        ctx.put("batchCode",    MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey()));
        ctx.put("hidProPlanSubscription",String.valueOf(SubscriptionNavigationHelper.hideProPlanSubscription()));
        ctx.put("subject",      nullToEmpty(extras.subject));
        ctx.put("chapterCode",      nullToEmpty(extras.chapterCode));
        ctx.put("microlectureCode",      nullToEmpty(extras.microlectureCode));
        ctx.put("question",     nullToEmpty(extras.question));
        ctx.put("idealAnswer",  nullToEmpty(extras.idealAnswer));
        ctx.put("studentAnswer",nullToEmpty(extras.answer));
        ctx.put("analysisOfAnswer",      nullToEmpty(extras.analysisOfAnswer));
        ctx.put("explanationOfQuestion", nullToEmpty(extras.explanationOfQuestion));
        ctx.put("totalMarks",   parseDoubleOrZero(extras.totalMarks));
        ctx.put("obtainedMarks",parseDoubleOrZero(extras.obtainedMarks));
        ctx.put("language",     MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        ctx.put("imageUrl",     "");
        ctx.put("imageExplanationLatex", nullToEmpty(extras.imageExplanationLatex));
        ctx.put("imageQuestion",         nullToEmpty(extras.imageQuestion));
        ctx.put("imageAnswer",           nullToEmpty(extras.imageAnswer));

        try {
            String json = objectMapper.writeValueAsString(ctx);
            view.evaluateJavascript("javascript:showAskMoreContext(" + json + ");", null);
            Log.d(TAG, "Pushed askMore context (" + json.length() + " chars)");
            Log.d(TAG, "Pushed askMore context "+json);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to serialize askMore context", e);
        }
    }

    // -------------------------------------------------------------------------
    // Camera / gallery / crop
    // -------------------------------------------------------------------------
    public void checkCameraPermission(String permission, int requestCode) {
        if (hasPermission(permission)) {
            captureImage();
        } else {
            requestPermission(permission, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureImage();
        }
    }

    private void captureImage() {
        try {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile  = File.createTempFile("photo", ".jpg", storageDir);
            Uri imageUri    = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider", imageFile);
            MyApplication.saveData(PreferenceKeys.CAMERA_CAPTURE_URI.getKey(), imageUri.toString());

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Log.e(TAG, "captureImage failed", e);
        }
    }

    public void launchGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void openCropActivity(String imageUri) {
        Intent intent = new Intent(this, ImageCropperActivity.class);
        intent.putExtra("imageUri", imageUri);
        cropActivityLauncher.launch(intent);
    }

    // -------------------------------------------------------------------------
    // Image compression
    // -------------------------------------------------------------------------
    private String compressAndConvertToBase64(Uri imageUri) throws IOException {
        int quality = resolveCompressionQuality();

        // Step 1: measure dimensions without loading pixels
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        try (InputStream s = getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(s, null, opts);
        }

        opts.inSampleSize     = calculateInSampleSize(opts, IMAGE_MAX_DIMENSION, IMAGE_MAX_DIMENSION);
        opts.inJustDecodeBounds = false;

        Bitmap bitmap;
        try (InputStream s = getContentResolver().openInputStream(imageUri)) {
            bitmap = BitmapFactory.decodeStream(s, null, opts);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Bitmap.CompressFormat fmt = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    ? Bitmap.CompressFormat.WEBP_LOSSY
                    : Bitmap.CompressFormat.PNG;
            bitmap.compress(fmt, quality, baos);
        } finally {
            bitmap.recycle();
        }

        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    private int resolveCompressionQuality() {
        try {
            int q = Integer.parseInt(
                    MyApplication.getFirebaseRemoteConfig().getString("compression_image_factor"));
            return Math.max(0, Math.min(100, q));
        } catch (NumberFormatException e) {
            return DEFAULT_IMAGE_QUALITY;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options opts, int reqWidth, int reqHeight) {
        int height = opts.outHeight;
        int width  = opts.outWidth;
        int sampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfH = height / 2;
            int halfW = width  / 2;
            while (halfH / sampleSize >= reqHeight && halfW / sampleSize >= reqWidth) {
                sampleSize *= 2;
            }
        }
        return sampleSize;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    /** Evaluate a JS expression (no leading "javascript:" prefix needed). */
    private void evaluateJavascript(String expression) {
        webView.evaluateJavascript("javascript:" + expression, null);
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
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

    private static double parseDoubleOrZero(String s) {
        if (s == null) return 0;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void backToPreviousActivity() {
        finish();
    }

    /**
     * Called by {@link WebViewDoubtChatInterface} to update the active chat session ID
     * (e.g. when a new session is created inside the WebView).
     */
    public void setChatSessionId(String chatSessionId) {
        if (extras != null) {
            extras.chatSessionId = chatSessionId;
        }
    }
}