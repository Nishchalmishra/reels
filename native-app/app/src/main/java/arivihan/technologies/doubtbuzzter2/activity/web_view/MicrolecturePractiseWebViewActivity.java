package arivihan.technologies.doubtbuzzter2.activity.web_view;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MICRO_LECTURE_PRACTICE_LIST_BY_TYPE;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.util.UnstableApi;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.PracticeProblemsActivity;
import arivihan.technologies.doubtbuzzter2.activity.VideoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.web_view.interfaces.MicrolecturePractiseWebviewInterface;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.competitive.AdaptivePath;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.WebViewLoadGuard;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureEntity;
import arivihan.technologies.doubtbuzzter2.model.PracticeStatus;
import arivihan.technologies.doubtbuzzter2.model.Questions;
import arivihan.technologies.doubtbuzzter2.model.VideoDetails;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MicrolecturePractiseWebViewActivity extends AppCompatActivity {

    private static final String TAG = MicrolecturePractiseWebViewActivity.class.getSimpleName();

    // UI Variable
    private RelativeLayout rl_parent_layout;
    private WebView webView;
    private LottieAnimationView webView_lottie_animation;

    private boolean isBackRestricted;
    private String onBackMessage;
    private String onBackJsMethodCallBack;

    ArrayList<FactoryShipQnADto> qnADtoList;

    String chapterId;
    Gson gson;
    boolean reattempt;
    boolean review;
    int firstUnattemptedIndex;

    int practicePosInCurrentMicroLecture;
    ConstraintLayout continuity_layout;
    ImageView closeContinuityLayout;
    LinearLayout progressionLayout;
    TextView progressionText;
    ConstraintLayout continuityPlayLayout;
    CircularProgressIndicator circularProgressIndicator;
    LinearLayout skipToNextLayout;
    TextView skipToNextText;
    ArrayList<MicroLectureDto> list = new ArrayList<>();
    MicroLectureDto progressionDto;
    MicroLectureDto skipDto;
    int currentMicroLecturePos;
    int calculateMicroLecturePos;
    int calculatePracticePos;
    int progressionMicroLecturePos;
    int skipMicroLecturePos;
    int practicePosInProgression;
    int practicePosInSkip;
    boolean calculateStatus;
    boolean skipToPractice;
    boolean progressionIsPractice;
    private Handler handler = new Handler();
    private int circularProgress = 0;
    private final int totalDuration = 5000;  // 5 seconds
    private final int stepDuration = 50;
    private boolean pageLoaded = false;
    private WebViewLoadGuard loadGuard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_microlecture_practise_web_view);

        String finalUrl = getIntent().getStringExtra("url");
        if(StringUtils.isEmpty(finalUrl)) {
            finalUrl = CommonMethods.getPersonalizedPathUrl() + AdaptivePath.MICROLECTURE_PRACTISE.path(); // Default Practise
        }

        finalUrl = appendUserCentricData(finalUrl);
        Log.d(TAG, "finalUrl: "+finalUrl);

        finalUrl = appendPractiseParams(finalUrl);

        rl_parent_layout = findViewById(R.id.rl_parent_layout);
        webView_lottie_animation = findViewById(R.id.webView_lottie_animation);
        webView = findViewById(R.id.webview);

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
        webView.addJavascriptInterface(new MicrolecturePractiseWebviewInterface(this, webView), "AndroidInterface");

        loadGuard = new WebViewLoadGuard(this, webView, TAG, this::revealWebView, rl_parent_layout);

//        setUpContinuityLayout();
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("error", StringUtils.isNotEmpty(error.toString()) ? error.toString() : "");
                    bundle.putString("app_version", MyApplication.getAppVersionName(MicrolecturePractiseWebViewActivity.this));

                    MyApplication.logFirebaseEvent("web_view_page_ssl_error", bundle);
                    loadGuard.onMainFrameError("ssl error: " + error);
                } catch (Exception ignored) {
                } finally {
                    // Must always resolve the handler — without this the load stalls forever
                    // with no error surfaced anywhere.
                    handler.cancel();
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // Alpha is reset alongside visibility: the reveal animates the loader to
                // alpha 0, and without restoring it a second navigation shows a "visible"
                // loader that is fully transparent.
                webView_lottie_animation.setAlpha(1f);
                webView_lottie_animation.setVisibility(View.VISIBLE);
                loadGuard.onPageStarted(url);
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
                loadGuard.onMainFrameError("code=" + error.getErrorCode() + ", " + error.getDescription());
                Toast.makeText(MicrolecturePractiseWebViewActivity.this, "Failed to load page", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
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

    }

    private void onBackPressedClick() {
        try {
            if (!pageLoaded) {
                if (webView.canGoBack()) {
                    webView.goBack(); // Go back in WebView history
                } else {
                    finish();
                }
            } else {
                webView.evaluateJavascript("javascript:onBackPress();",null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    /**
     * The single loader→page handover, driven by {@link WebViewLoadGuard}: it fires from
     * whichever reveal path arrives first — {@code onPageFinished}, or the DOM probe once the
     * app has mounted — and only ever once per load.
     */
    private void revealWebView(String reason) {
        try {
            Log.d(TAG, "reveal: " + reason);
            webView_lottie_animation.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        webView_lottie_animation.setVisibility(View.GONE);
                        webView.setAlpha(0f);
                        webView.setVisibility(View.VISIBLE);
                        pageLoaded = true;
                        webView.animate().alpha(1f).setDuration(300).start();
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loadGuard != null) loadGuard.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (loadGuard != null) loadGuard.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (loadGuard != null) {
            loadGuard.onDestroy();
            loadGuard.destroyWebView();
        }
        super.onDestroy();
    }

    public void backToPreviousActivity() {
        finish();
    }

    public void changeBackPressed(boolean isBackRestricted) {
        this.isBackRestricted = isBackRestricted;
    }

    public void pressBack() {
        onBackPressedClick();
    }

    public void setJsMethodCallBack(String methodName) {
        this.onBackJsMethodCallBack = methodName;
    }

    public void showPositiveSnackBarMessage(String message) {
        CommonMethods.showNegativeSnackbar(rl_parent_layout, message, 3000);
    }

    public void showNegativeSnackBarMessage(String message) {
        CommonMethods.showPositiveSnackbar(rl_parent_layout, message, 3000);
    }

    public void showContinuity() {
        runOnUiThread(this::setUpContinuityLayout);

        Log.d(TAG,"--> Show continuity");
    }

    private String appendUserCentricData(String url) {
        boolean appendUserId = getIntent().getBooleanExtra("appendUserId", true);
        boolean appendBoard = getIntent().getBooleanExtra("appendBoard", false);
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
        if (appendToken) {
            url = appendParamToUrl(url, "token", MyApplication.getFCMToken());
        }
        if (appendBoard) {
            url = appendParamToUrl(url, PreferenceKeys.BOARD.getKey(), String.valueOf(MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())));
        }

        url = appendParamToUrl(url, "env", EnvironmentManager.
                getEnvironmentNameByUrl(MyApplication.getEndPoint()).toLowerCase());
        url = appendBatchParams(url);
        url = appendParamToUrl(url, "subscriptionType", MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey()));
        url = appendParamToUrl(url, "versionCode", String.valueOf(DeviceInfoUtil.getVersionCode(this)));
        return url;
    }

    private String appendPractiseParams(String url) {
        firstUnattemptedIndex = getIntent().getIntExtra("firstUnattemptedIndex", 0);
        reattempt = getIntent().getBooleanExtra("reattempt", false);
        review = getIntent().getBooleanExtra("review", false);
        String microLectureId = getIntent().getStringExtra("microLectureId");
        String microLectureName = getIntent().getStringExtra("microLectureName");
        chapterId = getIntent().getStringExtra("chapterId");
        qnADtoList = getIntent().getParcelableArrayListExtra("factoryShipQnADtoList");
        String type = getIntent().getStringExtra("type");

        url = appendParamToUrl(url, "firstUnattemptedIndex", String.valueOf(firstUnattemptedIndex));
        url = appendParamToUrl(url, "reattempt", String.valueOf(reattempt));
        url = appendParamToUrl(url, "review", String.valueOf(review));
        url = appendParamToUrl(url, "microLectureId", microLectureId);
        url = appendParamToUrl(url, "microLectureName", microLectureName);
        url = appendParamToUrl(url, "type", type);
        url = appendParamToUrl(url, "chapterId", chapterId);
        url = appendParamToUrl(url, "type", type);
        url = appendParamToUrl(url, "userName", MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
        url = appendParamToUrl(url, "lang", MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        return url;

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

        return url;
    }


    private void setUpContinuityLayout() {
        list = MyApplication.getMicroLectureList(getString(R.string.micro_lecture_and_practice_list));
        currentMicroLecturePos = getIntent().getIntExtra("currentMicroLecturePos", 0);
        practicePosInCurrentMicroLecture = getIntent().getIntExtra("practicePosInCurrentMicroLecture", 0);
        continuity_layout = findViewById(R.id.continuity_layout);
        closeContinuityLayout = findViewById(R.id.close_continuity_layout);
        progressionLayout = findViewById(R.id.progression_layout);
        progressionText = findViewById(R.id.progression_text);
        continuityPlayLayout = findViewById(R.id.play_layout);
        circularProgressIndicator = findViewById(R.id.continuity_timer);
        skipToNextLayout = findViewById(R.id.skip_to_next_layout);
        skipToNextText = findViewById(R.id.skip_to_next_text);
        closeContinuityLayout.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        calculateMicroLecturePos = currentMicroLecturePos;
        calculatePracticePos = practicePosInCurrentMicroLecture;
        progressionDto = practiceProgression();

        if (progressionDto == null) {
            finish();
            return;
        } else {
            continuity_layout.setVisibility(View.VISIBLE);
            startProgress();
        }

        progressionMicroLecturePos = calculateMicroLecturePos;
        progressionIsPractice = calculateStatus;

        if (progressionDto != null) {
            if (progressionIsPractice) {
                practicePosInProgression = calculatePracticePos;
                progressionText.setText(LectureContentType.getLectureContentType(progressionDto.getQuestions().get(practicePosInProgression).getType()).getType());
            } else {
                progressionText.setText(progressionDto.getTitle());
            }
        }

        Log.d("progressionMicroLectur", String.valueOf(calculateMicroLecturePos));
        Log.d("practicePosInProgre", String.valueOf(practicePosInProgression));
        Log.d("progressionIsPractice", String.valueOf(progressionIsPractice));


        if (progressionIsPractice) {

            Log.d(TAG, "videoProgression starts 1");
            skipDto = videoProgression();
            Log.d(TAG, "videoProgression starts 2");

            skipToPractice = calculateStatus;
            skipMicroLecturePos = calculateMicroLecturePos;
        }
        else {
            Log.d(TAG, "videoProgression while starts");
            Log.d(TAG, "skipToPractice " + skipToPractice);
            Log.d(TAG, "skipMicroLecturePos " + skipMicroLecturePos);
            Log.d(TAG, "list " + list.size());


            while (!skipToPractice && skipMicroLecturePos < list.size()) {
                calculatePracticePos = -1;
                skipDto = practiceProgression();
                skipToPractice = calculateStatus;
                skipMicroLecturePos = calculateMicroLecturePos;
                if (skipDto == null) {
                    break;
                }
                Log.d(TAG, "skipDto " + skipDto);
                Log.d(TAG, "skipToPractice " + skipToPractice);
                Log.d(TAG, "skipMicroLecturePos " + calculateMicroLecturePos);
            }
        }

        if (skipDto != null && skipDto.getQuestions() != null && practicePosInSkip < skipDto.getQuestions().size()) {
            if (skipToPractice) {
                practicePosInSkip = calculatePracticePos;
                skipToNextText.setText(String.format(getString(R.string.Skip_to_Next_Practice), LectureContentType.getLectureContentType(skipDto.getQuestions().get(practicePosInSkip).getType()).getType()));
            } else {
                skipToNextText.setText(String.format(getString(R.string.Skip_to_Next_Lecture)));
            }
        }
        else {
            skipToNextLayout.setVisibility(View.GONE);
        }

        Log.d("skipMicroLecturePos", String.valueOf(skipMicroLecturePos));
        Log.d("practicePosInSkip", String.valueOf(practicePosInSkip));
        Log.d("skipIsPractice", String.valueOf(calculateStatus));

        continuityPlayLayout.setOnClickListener(v->{
            if(progressionDto!=null) {
                handler.removeCallbacksAndMessages(null);
                progressionLayout.performClick();
            }
        });

        progressionLayout.setOnClickListener(view -> {
            if (progressionDto == null || progressionDto.isLocked()) {
                MyApplication.saveData(getString(R.string.progressionIsLocked), true);
                MyApplication.saveData(getString(R.string.progressionLockedMessage), progressionDto.getLockMessage());
                getOnBackPressedDispatcher().onBackPressed();
            } else {
                if (progressionIsPractice) {
                    getQuestionList(progressionDto.getCode(), progressionDto.getQuestions().get(practicePosInProgression).getType(), progressionMicroLecturePos, practicePosInProgression);
                } else {
                    startVideo(progressionDto, progressionMicroLecturePos);
                }
            }
        });
        skipToNextLayout.setOnClickListener(view -> {
            if (skipDto.isLocked()) {
                MyApplication.saveData(getString(R.string.progressionIsLocked), true);
                MyApplication.saveData(getString(R.string.progressionLockedMessage), skipDto.getLockMessage());
                getOnBackPressedDispatcher().onBackPressed();
            } else {
                if (skipToPractice) {
                    getQuestionList(skipDto.getCode(), skipDto.getQuestions().get(practicePosInSkip).getType(), skipMicroLecturePos, practicePosInSkip);
                } else {
                    startVideo(skipDto, skipMicroLecturePos);
                }
            }
        });
    }

    private MicroLectureDto practiceProgression() {
        if (calculateMicroLecturePos < list.size() && list.get(calculateMicroLecturePos)!=null && list.get(calculateMicroLecturePos).getQuestions().size() > calculatePracticePos) {
            for (int i = calculatePracticePos + 1; i < list.get(calculateMicroLecturePos).getQuestions().size(); i++) {
                Questions questions = list.get(calculateMicroLecturePos).getQuestions().get(i);
                if (questions.getStatus().equalsIgnoreCase(PracticeStatus.PARTIAL_COMPLETED.name()) ||
                        questions.getStatus().equalsIgnoreCase(PracticeStatus.TO_DO.name())) {
                    calculateStatus = true;
                    calculatePracticePos = i;
                    return list.get(calculateMicroLecturePos);
                }
            }
            if (!calculateStatus)
                return videoProgression();
        }
        return videoProgression();
    }

    private void startProgress() {
        final long startTime = System.currentTimeMillis();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                circularProgress = (int) ((elapsedTime * 100) / totalDuration);
                circularProgressIndicator.setProgress(circularProgress);
                if (elapsedTime < totalDuration) {
                    handler.postDelayed(this, stepDuration);
                } else {
                    handler.removeCallbacksAndMessages(null);
                    progressionLayout.performClick();
                }
            }
        }, stepDuration);
    }
    private MicroLectureDto videoProgression() {
        if (list.size() > calculateMicroLecturePos) {
            for (int i = calculateMicroLecturePos + 1; i < list.size(); i++) {
                calculateMicroLecturePos = i;
                if (!list.get(calculateMicroLecturePos).isCompleted()) {
                    if (list.get(calculateMicroLecturePos).getVideoDetails().getProgress() == 100) {
                        calculatePracticePos = 0;
                        return practiceProgression();
                    } else {
                        calculateStatus = false;
                    }
                    return list.get(calculateMicroLecturePos);
                }
            }
        }
        return null;
    }

    private void getQuestionList(String microLectureCode, String type, int pos, int practicePos) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("microLectureCode", microLectureCode);
            map.put("type", type);
            map.put("boldFormatting", true);
            MyJsonArrayRequest myJsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(MICRO_LECTURE_PRACTICE_LIST_BY_TYPE, map), null,
                    response -> {
                        TypeToken<List<FactoryShipQnADto>> token = new TypeToken<List<FactoryShipQnADto>>() {
                        };
                        qnADtoList.clear();
                        qnADtoList.addAll(gson.fromJson(response.toString(), token.getType()));
                        setUpQuestion(pos, practicePos,type);
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Log.e("postError: ", error.getMessage());
                }
            });
            myJsonArrayRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonArrayRequest);
        } catch (Exception e) {
            Log.e("ErrorQuestionList", e.getMessage(), e);
        }
    }

    public void startVideo(MicroLectureDto microLectureDto, int pos) {
        if (microLectureDto.isLocked()) {
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            updateSelectionLocally(true, false, microLectureDto.getId(),LectureContentType.VIDEO.name());
            VideoDetails videoDetails = microLectureDto.getVideoDetails();
            playVideo(videoDetails, microLectureDto, getIntent().getBooleanExtra("isBoard", false), pos);
        }
    }

    private void updateSelectionLocally(boolean isLecture, boolean isPractice, String mlId, String lectureContentType) {
        if (StringUtils.isNotEmpty(chapterId)) {
            MicroLectureEntity selectionMicroLectureEntity = new MicroLectureEntity(chapterId, mlId,
                    MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()),lectureContentType,isLecture,isPractice);
            MyApplication.getInstance().getMicroLectureViewModel().insert(selectionMicroLectureEntity);

            MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        }
    }

    private void setUpQuestion(int pos, int practicePos,String type) {
        if (!qnADtoList.isEmpty()) {
            firstUnattemptedIndex = 0;
            for (int i = 0; i < qnADtoList.size(); i++) {
                if (!qnADtoList.get(i).isCompleted()) {
                    firstUnattemptedIndex = i;
                    break;
                }
            }
            openPracticeQuestions(pos, practicePos,type);
        }
    }

    private void openPracticeQuestions(int pos, int practicePos,String practiceType) {
        Log.d("PracticeLaunch", "Opening PracticeProblemsActivity...");
        Log.d("PracticeLaunch", "MicroLecture Position: " + pos);
        Log.d("PracticeLaunch", "Practice Position: " + practicePos);

        String microLectureId = list.get(progressionMicroLecturePos).getId();
        Log.d("PracticeLaunch", "MicroLecture ID: " + microLectureId);
        String microLectureTitle = list.get(progressionMicroLecturePos).getTitle();
        Log.d("PracticeLaunch", "MicroLecture Title: " + microLectureTitle);

        Log.d("PracticeLaunch", "Practice Type: " + practiceType);

        updateSelectionLocally(false, true, microLectureId, practiceType);

        Intent intent = new Intent(this, PracticeProblemsActivity.class);
        intent.putParcelableArrayListExtra("factoryShipQnADtoList", qnADtoList);
        intent.putExtra("microLectureId", microLectureId);
        intent.putExtra("microLectureName", microLectureTitle);
        intent.putExtra("chapterId", chapterId);
        intent.putExtra("review", review);
        intent.putExtra("reattempt", reattempt);
        intent.putExtra("firstUnattemptedIndex", firstUnattemptedIndex);
        intent.putExtra("currentMicroLecturePos", pos);
        intent.putExtra("practicePosInCurrentMicroLecture", practicePos);

        String subjectCode = getIntent().getStringExtra("subjectCode");
        Log.d("PracticeLaunch", "Subject Code: " + subjectCode);
        intent.putExtra("subjectCode", subjectCode);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Log.d("PracticeLaunch", "Starting PracticeProblemsActivity...");
        startActivity(intent);
    }

    private void playVideo(VideoDetails videoDetails, MicroLectureDto microLectureDto, boolean board, int pos) {

        @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(this, VideoPlayerActivity.class);
        MicroLectureContentDto microLectureContentDto = new MicroLectureContentDto();
        microLectureContentDto.setDefaultStartTime(Math.toIntExact(videoDetails.getStartPos()));
        microLectureContentDto.setVideoCompletionEndPos(Math.toIntExact(videoDetails.getEndPos()));
        microLectureContentDto.setVideoCompletionStartPos(Math.toIntExact(videoDetails.getEndPos()));
        microLectureContentDto.setContentUrl(videoDetails.getUrl());
        microLectureContentDto.setVideoUrl(videoDetails.getUrl());
        microLectureContentDto.setId(microLectureDto.getCode());
        microLectureContentDto.setChapterId(chapterId);
        microLectureContentDto.setResumePosition(videoDetails.getResumePosition());
        microLectureContentDto.setLectureContentType(LectureContentType.VIDEO);
        microLectureContentDto.setVideoQualityDtos(videoDetails.getQualityDtos());
        microLectureContentDto.setLanguageAndVideoUrlDtos(videoDetails.getLanguageAndVideoUrlDtos());
        microLectureContentDto.setLikeCount(microLectureDto.getLikeCount());
        microLectureContentDto.setLiked(microLectureDto.getLiked());

        intent.putExtra(PreferenceKeys.BOARD.getKey(), board);
        intent.putExtra("resumePosition", microLectureContentDto.getResumePosition());
        intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
        intent.putExtra("position", Integer.parseInt(String.valueOf(videoDetails.getResumePosition())));
        intent.putExtra("shouldNotResume", !videoDetails.isResume());
        intent.putExtra("startAtPosition", true);
        intent.putExtra("isAskDoubtActivity", false);
        intent.putExtra("video_uri", videoDetails.getUrl());
        intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), videoDetails.getStartPos());
        intent.putExtra("video_ending", videoDetails.getEndPos());
        intent.putExtra("chapterId", chapterId);
        intent.putExtra("subjectCode", getIntent().getStringExtra("subjectCode"));
        intent.putExtra("selectedSubject", getIntent().getStringExtra("selectedSubject"));
        intent.putExtra("selected_chapter_id", microLectureDto.getCode());
        intent.putExtra("replay", microLectureDto.getVideoDetails().getProgress() == 100);
        intent.putExtra("currentMicroLecturePos", pos);
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        startActivity(intent);
        finish();
    }

}
