package arivihan.technologies.doubtbuzzter2.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.rajat.pdfviewer.PdfRendererView;
import com.rajat.pdfviewer.util.CacheStrategy;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonDialogs;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.customView.DoubtResponseDialog;
import arivihan.technologies.doubtbuzzter2.customView.DrawingView;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.feature.FeatureEnum;
import arivihan.technologies.doubtbuzzter2.helper.RemoteConfigUtils;
import arivihan.technologies.doubtbuzzter2.helper.PdfLoaderTextProvider;

import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.manager.PDFCircleDailyCounterManager;
import arivihan.technologies.doubtbuzzter2.model.NotesDoubtRequestBody;
import arivihan.technologies.doubtbuzzter2.model.remote_config.pdf_circle.PdfCircleConfigLanguageModel;
import arivihan.technologies.doubtbuzzter2.model.remote_config.pdf_circle.PdfCircleConfigurationModel;
import arivihan.technologies.doubtbuzzter2.service.PdfDownloadManager;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyJsonRequest;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class PdfViewerActivity extends BaseActivity {

    private static final String TAG = PdfViewerActivity.class.getSimpleName();

    private ConstraintLayout parentLayout;
    private DrawingView drawingView;
    private PdfRendererView pdfRendererView;
    private CardView cv_pencil;
    private View view_black_overlay;
    private LottieAnimationView lav_pencil_stars, lav_pencil_gradient;
    private LinearLayout ll_pencil;
    private TextView tv_pencil_text;
    private boolean isDrawingEnabled = false;
    int textAnimationPosition = 0;
    private Runnable textAnimationRunnable;
    private ImageView backButton;
    private LinearLayout back_button_layout;
    private TextView pdfTitle;
    private ImageView iv_gradient;

    // Progress UI
    private View downloadLayout;
    private ProgressBar progressBar;
    private TextSwitcher text_switcher;
    private TextView progressText, progressMessage;
    private int currentPageNumber = -1;
    private boolean isPencilExpandAnimationStarted = false;
    private boolean isPdfCircleVisible = false;
    private ValueAnimator expandAnimator;
    private ValueAnimator collapseAnimator;
    private PdfCircleConfigurationModel pdfCircleConfigurationModel;
    private PdfCircleConfigLanguageModel pdfCircleConfigLanguageModel;
    private AnimatorSet gradientAnimation;
    private boolean isSharedPreferenceUpdated = false;

    //resume download
    private NetworkStatusLiveData networkStatusLiveData;
    private boolean shouldResumeDownloadWhenOnline = false;
    private String pendingDownloadUrl;
    private String pendingDownloadKey;
    private File pendingDownloadFile;
    private boolean isPdfDownloadCompleted = false;

    private Handler pdfPencilAnimationHandler = new Handler(Looper.getMainLooper());
    private Runnable pdfPencilAnimationRunnable = () -> pauseAnimation();
    private String pdfUrl;
    private String pdfPath;
    private String notesType;
    private String subjectName;
    private String pdfName;
    private String pdfUrlForOffline;
    private boolean isPdfCircleForceLock;
    private int noteId = -1;

    private String getEnvironmentUrl(String prod, String stage, String dev, String fallback) {
        String url = CommonMethods.isEnvironmentProdRelease() ? prod
                : (CommonMethods.isEnvironmentStage() ? stage : dev);
        return StringUtils.isNotEmpty(url) ? url : fallback;
    }

    private String getMlApiUrl() {
        if (pdfCircleConfigurationModel == null) return "https://ml-pdf-circle-prod.arivihan.com";
        return getEnvironmentUrl(
                pdfCircleConfigurationModel.getMlApiUrlProd(),
                pdfCircleConfigurationModel.getMlApiUrlStage(),
                pdfCircleConfigurationModel.getMlApiUrl(),
                "https://ml-pdf-circle-prod.arivihan.com"
        );
    }

    private String getWebViewUrl() {
        if (pdfCircleConfigurationModel == null) return "https://tutor-prod.arivihan.com/pdf-circle-view";
        return getEnvironmentUrl(
                pdfCircleConfigurationModel.getWebViewUrlProd(),
                pdfCircleConfigurationModel.getWebViewUrlStage(),
                pdfCircleConfigurationModel.getWebViewUrl(),
                "https://tutor-prod.arivihan.com/pdf-circle-view"
        );
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        secureScreen();

        Log.d(TAG, "PdfViewerActivity started");
        parentLayout = findViewById(R.id.parentLayout);
        back_button_layout = findViewById(R.id.back_button_layout);
        drawingView = findViewById(R.id.drawingView);
        pdfRendererView = findViewById(R.id.pdfView);

        ll_pencil = findViewById(R.id.ll_pencil);
        cv_pencil = findViewById(R.id.cv_pencil);
        lav_pencil_stars = findViewById(R.id.lav_pencil_stars);
        lav_pencil_gradient = findViewById(R.id.lav_pencil_gradient);
        view_black_overlay = findViewById(R.id.view_black_overlay);
        tv_pencil_text = findViewById(R.id.tv_pencil_text);

        backButton = findViewById(R.id.back_button);
        pdfTitle = findViewById(R.id.pdfTitle);
        iv_gradient = findViewById(R.id.iv_gradient);

        tv_pencil_text.setVisibility(View.GONE);

        text_switcher = findViewById(R.id.text_switcher);

        downloadLayout = findViewById(R.id.downloadContainer);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        progressMessage = findViewById(R.id.progressMessage);

        try {
            Gson gson = new Gson();
            String pdfCircleConfigJson = MyApplication.getFirebaseRemoteConfig().getString("pdf_circle_config");
            String pdfCircleConfigLanJson = MyApplication.getFirebaseRemoteConfig().getString("pdf_circle_config_lang_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

            pdfCircleConfigurationModel = gson.fromJson(pdfCircleConfigJson, PdfCircleConfigurationModel.class);
            pdfCircleConfigLanguageModel = gson.fromJson(pdfCircleConfigLanJson, PdfCircleConfigLanguageModel.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (pdfCircleConfigLanguageModel != null) {
            tv_pencil_text.setText(StringUtils.isNotEmpty(pdfCircleConfigLanguageModel.getPencilExpandedText()) ? pdfCircleConfigLanguageModel.getPencilExpandedText() : getString(R.string.jha_doubt_he_vaha_circle_banao));
        }

        int expandedPdfPencilCounter = (pdfCircleConfigurationModel != null) ? pdfCircleConfigurationModel.getPdfExpandedShowCounter() : 2;
        int localExpandedPdfPencilCounter = MyApplication.getIntData(PreferenceKeys.PDF_PENCIL_EXPANDED_COUNTER.getKey());

        text_switcher.setFactory(() -> {
            TextView textView = new TextView(this);
            textView.setTextSize(16);
            textView.setLayoutParams(
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER
                    )
            );
            textView.setTextColor(getResources().getColor(android.R.color.black));
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setGravity(Gravity.CENTER);
            return textView;
        });
        ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.PDF_VIEWER_SCREEN.name(), "SCREEN");
        text_switcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_in));
        text_switcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_out));

        pdfUrl = getIntent().getStringExtra("pdfUrl");
        pdfPath = getIntent().getStringExtra("pdfPath");
        notesType = getIntent().getStringExtra("notesType");
        subjectName = StringUtils.isNotEmpty(getIntent().getStringExtra("subjectName"))
                ? getIntent().getStringExtra("subjectName")
                : "Subject";
        pdfName = getIntent().getStringExtra("pdfTitle");
        pdfUrlForOffline = getIntent().getStringExtra("pdfUrlForOffline");
        isPdfCircleForceLock = getIntent().getBooleanExtra("isPdfCircleForceLock", false);
        noteId = getIntent().getIntExtra("noteId", -1);

        Log.d(TAG, "Received pdfUrl: " + pdfUrl);
        Log.d(TAG, "Received pdfPath: " + pdfPath);
        Log.d(TAG, "Received notesType: " + notesType);
        Log.d(TAG, "Received subjectName: " + subjectName);
        Log.d(TAG, "Received pdfTitle: " + pdfName);
        Log.d(TAG, "Received isPdfCircleForceLock: "+isPdfCircleForceLock);

        pdfRendererView.setZoomEnabled(true);
        drawingView.setDrawingEnabled(false);
        drawingView.setPdfCircleConfigurationModel(pdfCircleConfigurationModel);
        drawingView.setCLOSE_THRESHOLD((pdfCircleConfigurationModel != null && pdfCircleConfigurationModel.getPdfClosedCircleThreshold() != 0) ? CommonMethods.dpToPx(this, (int) pdfCircleConfigurationModel.getPdfClosedCircleThreshold()) : CommonMethods.dpToPx(this, 100));

        pdfRendererView.setStatusListener(new PdfRendererView.StatusCallBack() {


            @Override
            public void onPdfRenderSuccess() {

            }

            @Override
            public void onPdfRenderStart() {

            }

            @Override
            public void onPdfLoadStart() {

            }

            @Override
            public void onPdfLoadProgress(int i, long l, @Nullable Long aLong) {

            }

            @Override
            public void onPdfLoadSuccess(@NonNull String s) {

            }

            @Override
            public void onError(@NonNull Throwable throwable) {

            }

            public void onPageChanged(int pageIndex, int totalPages) {
                currentPageNumber = pageIndex + 1; // PDF pages are 0-indexed
                Log.d(TAG, "Current PDF page: " + currentPageNumber + "/" + totalPages);
            }
        });

        setupAnimations(tv_pencil_text, lav_pencil_gradient);

        if (localExpandedPdfPencilCounter < expandedPdfPencilCounter) {
            expandText(tv_pencil_text, lav_pencil_gradient);
        }

        cv_pencil.setOnClickListener(v -> {
            try {
                if (!isDrawingEnabled) {
                    if (CommonMethods.isOnline(this)) {

                        Map<String, String> params = new HashMap<>();
                        params.put("pdf_url", StringUtils.isNotEmpty(pdfUrl) ? pdfUrl : "");
                        params.put("pdf_path", StringUtils.isNotEmpty(pdfPath) ? pdfPath : "");
                        params.put("pdf_title", StringUtils.isNotEmpty(pdfName) ? pdfName : "");
                        params.put("notes_type", StringUtils.isNotEmpty(notesType) ? notesType : "");
                        params.put("subject_name", StringUtils.isNotEmpty(subjectName) ? subjectName : "");
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.pencil_icon_click.getActionName(), params);
                        if (PDFCircleDailyCounterManager.isPDFCircleCounterTodayValid(PDFCircleDailyCounterManager.PREF_DAILY_COUNTER_NAME, PDFCircleDailyCounterManager.getPdfCircleLimit()) || SubscriptionType.isProSubscription()) {
                            cv_pencil.setEnabled(false);
                            try {
                                setPdfPencilEnabled();
                                isDrawingEnabled = !isDrawingEnabled;
                                iv_gradient.setVisibility(isDrawingEnabled ? View.VISIBLE : View.GONE);
                                drawingView.setDrawingEnabled(isDrawingEnabled);

                                if (isDrawingEnabled) {
                                    if (iv_gradient.getWidth() == 0 || iv_gradient.getHeight() == 0) {
                                        iv_gradient.post(() -> animateImageRadial(iv_gradient, view_black_overlay, 800, 300, true));
                                    } else {
                                        animateImageRadial(iv_gradient, view_black_overlay, 800, 300, true);
                                    }
                                    if (!isPencilExpandAnimationStarted) {
                                        expandText(tv_pencil_text, lav_pencil_gradient);
                                    }
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                cv_pencil.setEnabled(true);
                            }
                        } else {
                            if (!MyApplication.isCompetitiveProfile()) {
                                if (SubscriptionType.isBasicSubscription()) {
                                    String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                                    boolean isHindi = "hi".equalsIgnoreCase(lang);
                                    String btnText = isHindi ? "₹51 में जॉइन करें" : "Join at Rs 51";
                                    CommonDialogs.showDoubtLimitDialog(this, "", btnText);
                                } else if (SubscriptionNavigationHelper.hideProPlanSubscription()) {
                                    CommonDialogs.MessageLimitDialog.show(this, CommonDialogs.MessageLimitDialog.Type.DOUBT_SESSION);
                                } else {
                                    SubscriptionNavigationHelper.navigateToSubscriptionScreenWithLimitExceed(this, FeatureEnum.PDF_CIRCLE, (int) PDFCircleDailyCounterManager.getPdfCircleLimit());
                                }
                            } else {
                                CommonMethods.showNegativeSnackbar(parentLayout, (pdfCircleConfigLanguageModel != null && StringUtils.isNotEmpty(pdfCircleConfigLanguageModel.getPdfCircleLimitExceedMessage())) ? pdfCircleConfigLanguageModel.getPdfCircleLimitExceedMessage() : getString(R.string.aaj_ki_limit_poori_ho_gayi_hai), 3000);
                            }
                        }
                    } else {
                        CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.internet_failure_error), 3000);
                    }
                } else {
                    isDrawingEnabled = !isDrawingEnabled;
                    if (isPencilExpandAnimationStarted) {
                        collapseText(tv_pencil_text, lav_pencil_gradient);
                    }
                    view_black_overlay.setVisibility(View.GONE);
                    drawingView.setDrawingEnabled(false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        back_button_layout.setOnClickListener(v -> onBackPressedActivity());

        setPdfCacheToMl(StringUtils.isNotEmpty(pdfUrl) ? pdfUrl : pdfUrlForOffline, MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));

        try {
            boolean isFeatureVisible = true;
            boolean isBatchEligible = true;

            // Old Remote Config based visibility
            if (StringUtils.isNotEmpty(notesType)) {
                isFeatureVisible = RemoteConfigUtils.isFeatureVisible("pdf_viewer_activity", notesType);
            }
            // New subscription + batch based visibility
            isBatchEligible = SubscriptionNavigationHelper.isPdfPencilVisibleByBatch();
            // PDF Circle is visible only when both conditions are satisfied
            isPdfCircleVisible = isFeatureVisible && isBatchEligible;

            // Force lock override
            if (isPdfCircleForceLock) {
                isPdfCircleVisible = SubscriptionNavigationHelper.hasActiveSubscriptionByBatch();
            }

            cv_pencil.setVisibility(isPdfCircleVisible ? View.VISIBLE : View.GONE);
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating PDF pencil visibility", e);
            // Default to hidden on error for safety
            isPdfCircleVisible = false;
            cv_pencil.setVisibility(View.GONE);
        }
        pdfTitle.setText(pdfName != null ? pdfName : "PDF Document");

        drawingView.setDoubtFlowListener(
                new DrawingView.DoubtFlowListener() {

                    @Override
                    public void onDrawingImageCaptured(File file) {
                        uploadDrawingViewImageToFireBaseStore(file, MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
                                , MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey()),
                                notesType, pdfName, MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                                MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey()));
                    }

                    @Override
                    public void onDoubtCaptured(File file) {

                        try {
                            if (!CommonMethods.isOnline(PdfViewerActivity.this)) {
                                CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.internet_failure_error), 3000);
                                return;
                            }

                            NotesDoubtRequestBody notesDoubtRequestBody = new NotesDoubtRequestBody(
                                    MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()),
                                    MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey()),
                                    StringUtils.isNotEmpty(pdfUrl) ? pdfUrl : pdfUrlForOffline,
                                    currentPageNumber,
                                    subjectName,
                                    pdfName,
                                    MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                                    MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey()),
                                    notesType,
                                    MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())
                            );
                            if (isDrawingEnabled) {
                                cv_pencil.performClick();
                            }
                            if (isPencilExpandAnimationStarted) {
                                collapseText(tv_pencil_text, lav_pencil_gradient);
                            }
                            DoubtResponseDialog dialog = new DoubtResponseDialog(
                                    PdfViewerActivity.this,
                                    notesDoubtRequestBody,
                                    file,
                                    getMlApiUrl(),
                                    getWebViewUrl(),
                                    (pdfCircleConfigurationModel != null) ? pdfCircleConfigurationModel.getWebViewJsFunctionName() : null,
                                    new DoubtResponseDialog.DoubtResponseDialogListener() {
                                        @Override
                                        public void onError(String message) {
                                            CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.please_try_again), 3000);
                                        }

                                        @Override
                                        public void onBackPressed(View view, boolean dismissDialog) {
                                            try {
                                                if (!dismissDialog) {
                                                    CommonMethods.showNegativeSnackbar(view, (pdfCircleConfigLanguageModel != null && StringUtils.isNotEmpty(pdfCircleConfigLanguageModel.getPdfCircleOnBackMessage())) ? pdfCircleConfigLanguageModel.getPdfCircleOnBackMessage() : getString(R.string.aapka_answer_ban_raha_he), 3000);
                                                } else {
                                                    MyVolleySingleton.getInstance(PdfViewerActivity.this).getRequestQueue().cancelAll(DoubtResponseDialog.TAG);
                                                }
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onApiResponse() {
                                            try {
                                                PDFCircleDailyCounterManager.tryIncrementPDFCircleCounter(PDFCircleDailyCounterManager.PREF_DAILY_COUNTER_NAME, PDFCircleDailyCounterManager.getPdfCircleLimit());
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                            );
                            dialog.show();
                            if (!isSharedPreferenceUpdated) {
                                // this condition is to update the counter single time
                                MyApplication.saveData(PreferenceKeys.PDF_PENCIL_EXPANDED_COUNTER.getKey(), MyApplication.getIntData(PreferenceKeys.PDF_PENCIL_EXPANDED_COUNTER.getKey()) + 1);
                            }
                            isSharedPreferenceUpdated = true;
                            startPdfPencilAnimationDelay();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }

                    @Override
                    public void onWrongCircleDraw() {
                        try {
                            if (!CommonMethods.isOnline(PdfViewerActivity.this)) {
                                CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.internet_failure_error), 3000);
                                return;
                            }

                            CommonMethods.showNegativeSnackbar(parentLayout,
                                    (pdfCircleConfigLanguageModel != null &&
                                            StringUtils.isNotEmpty(pdfCircleConfigLanguageModel.getPdfWrongCircleMessage()))
                                            ? pdfCircleConfigLanguageModel.getPdfWrongCircleMessage()
                                            : getString(R.string.jo_samaj_na_aaye_vaha_circle_banao), 3000);

                            Map<String, String> params = new HashMap<>();
                            params.put("pdf_url", StringUtils.isNotEmpty(pdfUrl) ? pdfUrl : "");
                            params.put("pdf_path", StringUtils.isNotEmpty(pdfPath) ? pdfPath : "");
                            params.put("subject", StringUtils.isNotEmpty(subjectName) ? subjectName : "");
                            params.put("page_number", String.valueOf(currentPageNumber));
                            params.put("chapter_name", pdfName);
                            params.put("type_of_notes", notesType);
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.pdf_circle_drawing_view_failed.getActionName(), params);
                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onError() {
                        try {
                            if (!CommonMethods.isOnline(PdfViewerActivity.this)) {
                                CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.internet_failure_error), 3000);
                                return;
                            }
                            CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.something_went_wrong), 3000);
                        } catch (Exception ignored) {
                        }
                    }
                }
        );


        Log.d(TAG, "Received pdfPath: " + pdfPath + ", pdfUrl: " + pdfUrl + ", pdfTitle: " + pdfName);

        setupPdfDownloadNetworkObserver();

        if (pdfPath != null && !pdfPath.isEmpty()) {
            openLocalPdf(pdfPath);
        } else if (pdfUrl != null && !pdfUrl.isEmpty()) {
            Log.d(TAG, "onCreate pdfUREL: " + pdfUrl);
            openPdfFromUrl(pdfUrl, pdfName);
        } else {
            Log.e(TAG, "No PDF path or URL provided");
        }

        animateLoaderText();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedActivity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }


    private void setPdfCacheToMl(String pdfUrl, String userId) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("pdf_url", pdfUrl);
            jsonBody.put("user_id", userId);

            Log.d(TAG, "Request: " + jsonBody.toString());
            VolleyJsonRequest request = new VolleyJsonRequest(
                    Request.Method.POST,
                    getMlApiUrl() + UrlRoutes.ML_PDF_CIRCLE_LOAD,
                    jsonBody,
                    response -> Log.d(TAG, "Response: " + response.toString()),
                    error -> Log.d(TAG, "Error: " + error.toString())
            );
            request.setTag(TAG);
            MyVolleySingleton.getInstance(this).getRequestQueue().add(request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAnimations();
    }

    private void setPdfPencilEnabled() {
        try {
            pdfPencilAnimationHandler.postDelayed(() -> cv_pencil.setEnabled(true), 1200);
        } catch (Exception ex) {
            ex.printStackTrace();
            cv_pencil.setEnabled(true);
        }
    }

    private void onBackPressedActivity() {
        if (isDrawingEnabled) {
            cv_pencil.performClick();
        } else {
            finish();
        }
    }

    private void openLocalPdf(String filePath) {
        new Thread(() -> {
            try {
                File pdfFile = new File(filePath);
                if (!pdfFile.exists()) {
                    Log.d(TAG, "Local PDF file does not exist: " + filePath);
                    return;
                }

                runOnUiThread(() -> {
                    try {
                        shouldResumeDownloadWhenOnline = false;
                        isPdfDownloadCompleted = true;
                        pdfRendererView.initWithFile(pdfFile, CacheStrategy.MINIMIZE_CACHE);
                        downloadLayout.setVisibility(View.GONE);
                        pdfRendererView.setVisibility(View.VISIBLE);
                        cv_pencil.setVisibility(isPdfCircleVisible ? View.VISIBLE : View.GONE);
                        releaseTextSwitcher(text_switcher);
                        Log.d(TAG, "Opened local PDF: " + filePath);
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening local PDF", e);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading PDF file", e);
            }
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void openPdfFromUrl(String url, String title) {

        String configStr = MyApplication.getFirebaseRemoteConfig().getString("openPdfFromUrl");

        boolean cacheEnabled = true;
        String filenameRegex = "[^a-zA-Z0-9._\\-\\u0900-\\u097F]";
        int maxRetries = 3;
        int retryDelayFactor = 2000;

        try {
            JSONObject jsonObject = new JSONObject(configStr);
            cacheEnabled = jsonObject.optBoolean("pdf_cache_enabled", true);
            filenameRegex = jsonObject.optString("filename_regex", filenameRegex);
            maxRetries = jsonObject.optInt("max_retries", maxRetries);
            retryDelayFactor = jsonObject.optInt("retry_delay_factor", retryDelayFactor);
        } catch (JSONException e) {
            Log.w(TAG, "Remote config JSON parse failed, using defaults", e);
        }

        if (url == null || url.trim().isEmpty()) {
            Log.e(TAG, "Invalid URL");
            return;
        }

        String safeTitle = (title != null && !title.trim().isEmpty()) ? title : String.valueOf(url.hashCode());
        safeTitle = safeTitle.replaceAll(filenameRegex, "_");

        String hashPart;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", hashBytes[i]));
            }
            hashPart = sb.toString();
        } catch (Exception e) {
            Log.w(TAG, "Hash failed, fallback to url.hashCode()");
            hashPart = String.valueOf(url.hashCode());
        }

        String pdfName = safeTitle + "_" + hashPart + ".pdf";
        File file = new File(getCacheDir(), pdfName);
        File completionMarker = getPdfCompletionMarker(file);
        Log.d(TAG, "Cache filename: " + pdfName);

        pendingDownloadUrl = url;
        pendingDownloadKey = pdfName;
        pendingDownloadFile = file;
        shouldResumeDownloadWhenOnline = false;
        isPdfDownloadCompleted = false;

        String downloadKey = pdfName;
        PdfDownloadManager manager = PdfDownloadManager.getInstance();
        PdfDownloadManager.DownloadTask existingTask = manager.getTask(downloadKey);

        if (cacheEnabled && file.exists() && file.length() > 0 && completionMarker.exists()) {
            Log.d(TAG, "PDF already cached, opening: " + file.getAbsolutePath());
            isPdfDownloadCompleted = true;
            openLocalPdf(file.getAbsolutePath());
            return;
        }

        if (existingTask != null && existingTask.isDownloading()) {

            downloadLayout.setVisibility(View.VISIBLE);
            pdfRendererView.setVisibility(View.GONE);
            cv_pencil.setVisibility(View.GONE);

            progressBar.setProgress(existingTask.getProgress());
            progressText.setText(existingTask.getProgress() + "%");

            manager.download(downloadKey, url, file, createDownloadListener());

            return;
        }

        downloadLayout.setVisibility(View.VISIBLE);
        pdfRendererView.setVisibility(View.GONE);
        cv_pencil.setVisibility(View.GONE);
        progressBar.setProgress(0);
        progressText.setText("0%");

        manager.download(downloadKey, url, file, createDownloadListener());

    }

    private void setupPdfDownloadNetworkObserver() {
        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            try {
                if (isOnline) {
                    if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                        CommonMethods.showPositiveSnackbar(parentLayout, getString(R.string.InternetConnection));
                        MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                        MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    }

                    if (!isPdfDownloadCompleted
                            && StringUtils.isNotEmpty(pendingDownloadUrl)
                            && (shouldResumeDownloadWhenOnline || downloadLayout.getVisibility() == View.VISIBLE)) {
                        shouldResumeDownloadWhenOnline = false;
                        resumePendingPdfDownload();
                    }
                } else {
                    if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                        CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.NoInternetConnection));
                        MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                        MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                    }

                    PdfDownloadManager.DownloadTask downloadTask = !StringUtils.isEmpty(pendingDownloadKey)
                            ? PdfDownloadManager.getInstance().getTask(pendingDownloadKey)
                            : null;
                    if (downloadTask != null && downloadTask.isDownloading()) {
                        shouldResumeDownloadWhenOnline = true;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void markDownloadComplete(File file) {
        try {
            File completionMarker = getPdfCompletionMarker(file);
            if (!completionMarker.exists()) {
                completionMarker.createNewFile();
            }
        } catch (Exception ex) {
            Log.w(TAG, "Failed to create PDF completion marker", ex);
        }
    }

    private File getPdfCompletionMarker(File file) {
        return new File(file.getAbsolutePath() + ".complete");
    }

    private void resumePendingPdfDownload() {
        if (StringUtils.isEmpty(pendingDownloadUrl) || pendingDownloadFile == null) return;
        if (isPdfDownloadCompleted) return;

        PdfDownloadManager manager = PdfDownloadManager.getInstance();
        PdfDownloadManager.DownloadTask task = StringUtils.isNotEmpty(pendingDownloadKey)
                ? manager.getTask(pendingDownloadKey)
                : null;

        if (task != null && task.isDownloading()) return;

        File completionMarker = getPdfCompletionMarker(pendingDownloadFile);
        if (pendingDownloadFile.exists() && pendingDownloadFile.length() > 0 && completionMarker.exists()) {
            openLocalPdf(pendingDownloadFile.getAbsolutePath());
            return;
        }

        downloadLayout.setVisibility(View.VISIBLE);
        pdfRendererView.setVisibility(View.GONE);
        cv_pencil.setVisibility(View.GONE);
        shouldResumeDownloadWhenOnline = false;

        manager.download(pendingDownloadKey, pendingDownloadUrl, pendingDownloadFile, createDownloadListener());
    }

    private boolean isNetworkRelatedDownloadError(Exception e) {
        if (e == null) {
            return false;
        }

        if (e instanceof UnknownHostException
                || e instanceof ConnectException
                || e instanceof SocketTimeoutException) {
            return true;
        }

        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        String lowerCaseMessage = message.toLowerCase();
        return lowerCaseMessage.contains("network is unreachable")
                || lowerCaseMessage.contains("failed to connect")
                || lowerCaseMessage.contains("connection reset")
                || lowerCaseMessage.contains("connection timed out")
                || lowerCaseMessage.contains("timeout");
    }

    private PdfDownloadManager.DownloadListener createDownloadListener() {

        return new PdfDownloadManager.DownloadListener() {

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {

                    if (progress >= 0 && progress <= 100) {
                        progressBar.setProgress(progress);
                        progressText.setText(progress + "%");
                    } else {
                        progressText.setText(progress + " KB");
                    }
                });
            }

            @Override
            public void onSuccess(File file) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    try {
                        shouldResumeDownloadWhenOnline = false;
                        pdfRendererView.initWithFile(file, CacheStrategy.MINIMIZE_CACHE);
                        downloadLayout.setVisibility(View.GONE);
                        pdfRendererView.setVisibility(View.VISIBLE);
                        cv_pencil.setVisibility(isPdfCircleVisible ? View.VISIBLE : View.GONE);
                        isPdfDownloadCompleted = true;
                        markDownloadComplete(file);
                        releaseTextSwitcher(text_switcher);
                        markNoteAsViewed();
                    } catch (Exception e) {
                        progressText.setText("-");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    boolean isNetworkError = isNetworkRelatedDownloadError(e);
                    shouldResumeDownloadWhenOnline = isNetworkError;
                    isPdfDownloadCompleted = false;
                    progressMessage.setText(getString(R.string.download_failed_please_try_again));
                    CommonMethods.showNegativeSnackbar(
                            parentLayout,
                            isNetworkError
                                    ? getString(R.string.error_no_internet_connection)
                                    : getString(R.string.download_failed_please_try_again),
                            3000
                    );
                });
            }
        };
    }


    private void markNoteAsViewed() {
        if (isFinishing() || isDestroyed()) return;
        if (noteId <= 0) return;
        try {
            JSONObject body = new JSONObject();
            body.put("subjectName", StringUtils.isNotEmpty(subjectName) ? subjectName : "");
            body.put("notesType", StringUtils.isNotEmpty(notesType) ? notesType : "");
            body.put("noteId", noteId);
            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    UrlRoutes.AV_NOTES_BY_USER,
                    body,
                    response -> Log.d(TAG, "markNoteAsViewed success: " + response),
                    error -> Log.d(TAG, "markNoteAsViewed error: " + error.toString())
            );
            MyVolleySingleton.getInstance(this).getRequestQueue().add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateLoaderText() {
        List<String> resolved = PdfLoaderTextProvider.getLoaderTexts(notesType);

        ArrayList<String> textList = new ArrayList<>();
        if (!resolved.isEmpty()) {
            textList.addAll(resolved);
        }

        // fallback text
        if (textList.isEmpty()) {
            textList.add(getString(R.string.bss_thodi_hi_der_me_aapke_notes_open_ho_jayenge));
        }

        text_switcher.setVisibility(View.VISIBLE);
        progressMessage.setVisibility(View.GONE);

        text_switcher.setText(textList.get(textAnimationPosition));

        textAnimationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || isDestroyed()) return;
                if (text_switcher == null || text_switcher.getChildCount() == 0) return;
                try {
                    textAnimationPosition = (textAnimationPosition + 1) % textList.size();
                    text_switcher.clearAnimation();
                    text_switcher.setText("");
                    text_switcher.setText(textList.get(textAnimationPosition).trim());
                    text_switcher.postDelayed(this, 4000); // every 4 seconds
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (text_switcher != null) text_switcher.setVisibility(View.GONE);
                    if (progressMessage != null) progressMessage.setVisibility(View.VISIBLE);
                }
            }
        };
        text_switcher.postDelayed(textAnimationRunnable, 4000);
    }


    private void releaseTextSwitcher(TextSwitcher textSwitcher) {
        try {
            if (textSwitcher == null) {
                return;
            }
            if (textAnimationRunnable != null) {
                textSwitcher.removeCallbacks(textAnimationRunnable);
                textAnimationRunnable = null;
            }
            textSwitcher.clearAnimation();
            textSwitcher.setInAnimation(null);
            textSwitcher.setOutAnimation(null);
            textSwitcher.removeAllViews();
            textSwitcher.setVisibility(View.GONE);
        } catch (Exception ignored) {
        }
    }

    private void setupAnimations(final TextView textView, final LottieAnimationView lottieAnimationView) {
        // Measure required width for wrap_content
        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthSpec, heightSpec);
        final int targetWidth = textView.getMeasuredWidth();

        // Expand animation (0 -> wrap_content)
        expandAnimator = ValueAnimator.ofInt(0, targetWidth);
        expandAnimator.setDuration(400);
        expandAnimator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = textView.getLayoutParams();
            lp.width = val;
            textView.setLayoutParams(lp);
        });

        expandAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                textView.setVisibility(View.VISIBLE);
                lottieAnimationView.setVisibility(View.GONE);
                isPencilExpandAnimationStarted = true;
            }
        });

        // Collapse animation (wrap_content -> 0)
        collapseAnimator = ValueAnimator.ofInt(targetWidth, 0);
        collapseAnimator.setDuration(400);
        collapseAnimator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = textView.getLayoutParams();
            lp.width = val;
            textView.setLayoutParams(lp);
        });
        collapseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                textView.setVisibility(View.GONE);
                lottieAnimationView.setVisibility(View.VISIBLE);
                isPencilExpandAnimationStarted = false;
            }
        });
    }

    public void expandText(TextView textView, LottieAnimationView lottieAnimationView) {
        if (expandAnimator == null) setupAnimations(textView, lottieAnimationView);
        if (expandAnimator.isRunning() || expandAnimator.isStarted()) {
            return;
        }
        expandAnimator.start();
    }

    public void collapseText(TextView textView, LottieAnimationView lottieAnimationView) {
        if (collapseAnimator == null) setupAnimations(textView, lottieAnimationView);
        if (collapseAnimator.isRunning() || collapseAnimator.isStarted()) {
            return;
        }
        collapseAnimator.start();
    }

    private void startPdfPencilAnimationDelay() {
        pdfPencilAnimationHandler.postDelayed(pdfPencilAnimationRunnable, 10_000);
    }

    private void cancelPdfPencilAnimationDelay() {
        pdfPencilAnimationHandler.removeCallbacks(pdfPencilAnimationRunnable);
    }

    public void pauseAnimation() {
        try {

            lav_pencil_stars.cancelAnimation();
            lav_pencil_stars.clearAnimation();
            lav_pencil_stars.setProgress(0f);

            lav_pencil_gradient.pauseAnimation();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void releaseAnimations() {
        try {

            if (lav_pencil_stars != null) {
                lav_pencil_stars.cancelAnimation();
                lav_pencil_stars.removeAllAnimatorListeners();
                lav_pencil_stars.clearAnimation();
            }

            if (lav_pencil_gradient != null) {
                lav_pencil_gradient.cancelAnimation();
                lav_pencil_gradient.removeAllAnimatorListeners();
                lav_pencil_gradient.clearAnimation();
            }

            if (expandAnimator != null) {
                expandAnimator.cancel();
                expandAnimator.removeAllListeners();
            }

            if (collapseAnimator != null) {
                collapseAnimator.cancel();
                collapseAnimator.removeAllListeners();
            }

            if (gradientAnimation != null) {
                gradientAnimation.cancel();
                gradientAnimation.removeAllListeners();
            }

            if (drawingView != null) {
                drawingView.clearAnimation();
                drawingView = null;
            }

            if (pdfRendererView != null) {
                pdfRendererView.clearAnimation();
                pdfRendererView = null;
            }

            if (view_black_overlay != null) {
                view_black_overlay.clearAnimation();
            }

            cancelPdfPencilAnimationDelay();

            if (textAnimationRunnable != null && text_switcher != null) {
                text_switcher.removeCallbacks(textAnimationRunnable);
                textAnimationRunnable = null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void animateImageRadial(final ImageView imageView, final View targetView,
                                    long expandDuration, long fadeDuration, boolean isGradientVisible) {
        try {
            if (gradientAnimation != null && gradientAnimation.isRunning()) {
                return;
            }

            imageView.setVisibility(View.VISIBLE);
            imageView.setAlpha(1f);

            int cx = imageView.getWidth() / 2;
            int cy = imageView.getHeight();

            float startRadius = 0f;
            float endRadius = (float) Math.hypot(imageView.getWidth(), imageView.getHeight());

            Animator reveal = ViewAnimationUtils.createCircularReveal(imageView, cx, cy, startRadius, endRadius);
            reveal.setDuration(expandDuration);

            ObjectAnimator fadeInTarget = ObjectAnimator.ofFloat(targetView, "alpha", 0f, 1f);
            fadeInTarget.setDuration(fadeDuration);

            ObjectAnimator fadeOutImage = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f);
            fadeOutImage.setDuration(fadeDuration);

            gradientAnimation = new AnimatorSet();
            gradientAnimation.playSequentially(reveal, fadeInTarget, fadeOutImage);

            targetView.setVisibility(View.INVISIBLE);
            targetView.setAlpha(0f);

            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isDrawingEnabled) {
                        targetView.setVisibility(View.VISIBLE);
                    } else {
                        targetView.setVisibility(View.GONE);
                    }
                }
            });

            gradientAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    imageView.setVisibility(View.GONE);
                    gradientAnimation = null;
                    if (isDrawingEnabled) {
                        targetView.setVisibility(View.VISIBLE);
                    } else {
                        targetView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    gradientAnimation = null;
                }
            });

            gradientAnimation.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            targetView.setVisibility(View.VISIBLE);
            targetView.setAlpha(1f);
        }
    }


    private void uploadDrawingViewImageToFireBaseStore(
            File file,
            String userId,
            String number,
            String notesType,
            String pdfName,
            String classId,
            String selectedCourseId) {

        if (file == null || !file.exists()) {
            Log.e(TAG, "Firebase Upload: File is null or does not exist");
            return;
        }

        // ── ISOLATION ──
        // Ensure Firebase failures never disrupt the main Doubt Flow UI.
        // We run this in a background thread to prevent any IO blocking.
        new Thread(() -> {
            try {
                // Read file bytes into memory first so the upload doesn't depend
                // on the file/stream staying open. The file may be deleted by
                // DoubtResponseDialog after the ML API responds.
                byte[] fileBytes;
                try (FileInputStream stream = new FileInputStream(file)) {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = stream.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    fileBytes = baos.toByteArray();
                }

                String filename = "drawing_" + System.currentTimeMillis() + ".png";
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference()
                        .child("drawing_images/" + userId + "/" + filename);

                UploadTask uploadTask = storageRef.putBytes(fileBytes);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Log.d(TAG, "Image uploaded to Firebase Storage: " + imageUrl);

                        pushDrawingMetadataToFirestore(
                                userId, number, notesType, pdfName,
                                classId, selectedCourseId, imageUrl,
                                MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())
                        );
                    }).addOnFailureListener(e -> Log.e(TAG, "Failed to get download URL", e));
                }).addOnFailureListener(e -> {
                    // ── ROBUSTNESS ──
                    // Log failure (likely Billing 402) but do nothing else.
                    // The user flow is handled separately in DoubtResponseDialog.
                    Log.e(TAG, "Firebase Storage upload failed (Archive Flow): " + e.getMessage());
                });
            } catch (Exception e) {
                Log.e(TAG, "Archive Flow Error: " + e.getMessage());
            }
        }).start();
    }


    private void pushDrawingMetadataToFirestore(String userId, String number,
                                                String notesType, String pdfName,
                                                String classId, String selectedCourseId,
                                                String imageUrl, String language) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put(PreferenceKeys.USER_ID.getKey(), userId);
        data.put(PreferenceKeys.PHONE_NUMBER.getKey(), number);
        data.put("notesType", notesType);
        data.put("pdfName", pdfName);
        data.put(PreferenceKeys.CLASS_ID.getKey(), classId);
        data.put(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), selectedCourseId);
        data.put("imageUrl", imageUrl);
        data.put("timestamp", System.currentTimeMillis());
        data.put("language", language);


        db.collection("pdf_circle_drawing_captures")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Firestore updated: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore update failed", e);
                });
    }
}
