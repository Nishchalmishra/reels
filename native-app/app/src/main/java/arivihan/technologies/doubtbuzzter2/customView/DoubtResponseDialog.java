package arivihan.technologies.doubtbuzzter2.customView;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.DoubtOptionsAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.enums.DoubtOptionType;
import arivihan.technologies.doubtbuzzter2.model.DoubtOption;
import arivihan.technologies.doubtbuzzter2.model.NotesDoubtRequestBody;
import arivihan.technologies.doubtbuzzter2.model.NotesDoubtResponseBody;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyMultipartRequest;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
public class DoubtResponseDialog extends Dialog implements DoubtOptionsAdapter.OnOptionClickListener {

    public static final String TAG = DoubtResponseDialog.class.getSimpleName();
    private static final long DEFAULT_ANIMATION_DURATION = 500L;
    private static final float DEFAULT_INITIAL_HEIGHT_RATIO = 0.35f;
    private static final float EXPAND_THRESHOLD_RATIO = 0.8f;
    private static final long DOUBLE_PRESS_INTERVAL = 500; // in ms
    private long lastBackPressedTime = 0;
    String optionSelected = "";
    private boolean hasPageLoadedOnce = false;
    private static final String DEFAULT_OPTION_TYPE = "DETAILED_EXPLANATION";
    @Override
    public void onOptionClicked(String optionType) {
        optionSelected = optionType;
        // Hide options and show loading
        layoutDoubtOptions.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        doubtProgressAnimation.setVisibility(View.VISIBLE);

        // Start text switcher animation again
        startTextSwitcherAnimation();

        // Make API call
        fetchAnswerFromApi(screenshotFile);
    }

    private enum State {LOADING, OPTIONS, CONTENT_READY, ERROR, DISMISSED}

    private State currentState = State.LOADING;

    private final long animationDuration;
    private final float initialHeightRatio;

    private ValueAnimator currentAnimator;

    private CardView  cv_close;
    private RelativeLayout dialogContainer;
    private TextSwitcher tvTitle;
    private WebView wvContent;
    private LottieAnimationView doubtProgressAnimation;

    private final NotesDoubtRequestBody requestBody;
    private final File screenshotFile;

    private NotesDoubtResponseBody notesResponse;
    private String apiUrl;
    private String webViewUrl;
    private String webViewJsFunctionName;
    private DoubtResponseDialogListener doubtResponseDialogListener;
    private LinearLayout layoutLoading;
    private LinearLayout layoutDoubtOptions;
    private RecyclerView rvDoubtOptions;
    private TextView tvOptionsTitle;
    private DoubtOptionsAdapter doubtOptionsAdapter;
    private Handler textSwitcherHandler;
    private Runnable textSwitcherRunnable;
    private String autoSelectedOption = DEFAULT_OPTION_TYPE;
    private static final String DEFAULT_JS_FUNCTION_NAME = "showNotesDoubtResponse";

    @SuppressLint("MissingInflatedId")
    public DoubtResponseDialog(@NonNull Context context,
                               @NonNull NotesDoubtRequestBody requestBody,
                               @Nullable Object screenshotFile, String apiUrl, String webViewUrl,
                               String webViewJsFunctionName,
                               DoubtResponseDialogListener doubtResponseDialogListener) {
        super(context);

        this.animationDuration = DEFAULT_ANIMATION_DURATION;
        this.initialHeightRatio = DEFAULT_INITIAL_HEIGHT_RATIO;
        this.requestBody = requestBody;
        this.screenshotFile = (File) screenshotFile;
        this.apiUrl = apiUrl;
        this.webViewUrl = webViewUrl;
        this.webViewJsFunctionName = (webViewJsFunctionName != null && !webViewJsFunctionName.isEmpty())
                ? webViewJsFunctionName : DEFAULT_JS_FUNCTION_NAME;
        this.doubtResponseDialogListener = doubtResponseDialogListener;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.layout_doubt_response_bottomsheet, null);
        setContentView(view);

        bindViews(view);
        setupViews();
        configureWindow();

        // Show doubt options immediately on dialog creation
        showDoubtOptions();
    }

    private void bindViews(View view) {
        dialogContainer = view.findViewById(R.id.dialogContainer);
        tvTitle = view.findViewById(R.id.tvTitle);
        wvContent = view.findViewById(R.id.wvContent);
        doubtProgressAnimation = view.findViewById(R.id.doubt_loading_animation);

        cv_close = view.findViewById(R.id.cv_close);
        layoutLoading = view.findViewById(R.id.doubt_loading_layout);
        layoutDoubtOptions = view.findViewById(R.id.layout_doubt_options);
        rvDoubtOptions = view.findViewById(R.id.rv_doubt_options);
        tvOptionsTitle = view.findViewById(R.id.tv_options_title);

        setupDoubtOptions();


        cv_close.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void setupDoubtOptions() {
        String currentLanguage = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();
        String jsonStr = remoteConfig.getString("doubt_options");
        try {
            JSONObject root = new JSONObject(jsonStr);
            JSONArray jsonArray = root.getJSONArray(currentLanguage);

            String optionTitle = null;
            List<DoubtOption> options = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.has("optionTitle")) {
                    optionTitle = obj.getString("optionTitle");
                    continue;
                }
                String type = obj.getString("type");
                String text = obj.getString("text");
                DoubtOptionType optionType = DoubtOptionType.valueOf(type);
                options.add(new DoubtOption(optionType, text));
            }

//            // Always capture first option for auto-select
//            if (!options.isEmpty()) {
//                autoSelectedOption = options.get(0).getType().name();
//            }

            tvOptionsTitle.setText(optionTitle);
            doubtOptionsAdapter = new DoubtOptionsAdapter(options, this);
            rvDoubtOptions.setAdapter(doubtOptionsAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void setupViews() {
        tvTitle.setFactory(() -> {
            TextView textView = new TextView(getContext());
            textView.setTextSize(16);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            textView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.urbanist_semi_bold));
            textView.setPadding(8, 0, 8, 0);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            return textView;
        });

        WebSettings settings = wvContent.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        WebView.setWebContentsDebuggingEnabled(true);
        wvContent.addJavascriptInterface(new AndroidInterface(getContext()), "AndroidInterface");

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setTextZoom(100);
        wvContent.setHapticFeedbackEnabled(false);
        wvContent.setOnLongClickListener(v -> true);
        wvContent.setLongClickable(false);
        wvContent.setOverScrollMode(View.OVER_SCROLL_NEVER);

        wvContent.setVisibility(View.GONE);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setOnKeyListener((dialog, keyCode, event) -> {
            if (!isContentReady() && keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    long currentTime = System.currentTimeMillis();

                    if (currentTime - lastBackPressedTime < DOUBLE_PRESS_INTERVAL) {
                        // Double press
                        if (doubtResponseDialogListener != null) {
                            doubtResponseDialogListener.
                                    onBackPressed(getWindow().getDecorView().findViewById(android.R.id.content), true);
                        }
                        dismiss();
                    } else {
                        // Single press
                        if (doubtResponseDialogListener != null) {
                            doubtResponseDialogListener.onBackPressed(getWindow().getDecorView().findViewById(android.R.id.content), false);
                        }
                    }

                    lastBackPressedTime = currentTime;
                    return true; // consume the event
                }
                return true; // still consume UP
            }
            return false;
        });
    }

    private void startTextSwitcherAnimation() {
        stopTextSwitcherAnimation();
        cv_close.setVisibility(View.GONE);
        String[] texts = getWaitingTexts();

        textSwitcherHandler = new Handler(Looper.getMainLooper());
        textSwitcherRunnable = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (currentState == State.LOADING) {
                    tvTitle.setText(texts[index]);
                    index = (index + 1) % texts.length;
                    textSwitcherHandler.postDelayed(this, 2000);
                }
            }
        };
        textSwitcherHandler.post(textSwitcherRunnable);
    }

    private void stopTextSwitcherAnimation() {
        if (textSwitcherHandler != null && textSwitcherRunnable != null) {
            textSwitcherHandler.removeCallbacks(textSwitcherRunnable);
        }
    }

    /**
     * Fetches waiting texts from Firebase Remote Config (single JSON key).
     * Falls back to English or defaults if missing.
     */
    private String[] getWaitingTexts() {
        FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();

        String rawJson = remoteConfig.getString("notes_doubt_text_switcher_list");

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

        try {
            JSONObject jsonObject = new JSONObject(rawJson);

            if (jsonObject.has(lang)) {
                JSONArray arr = jsonObject.getJSONArray(lang);
                return jsonArrayToArray(arr);
            } else if (jsonObject.has("en")) {
                JSONArray arr = jsonObject.getJSONArray("en");
                return jsonArrayToArray(arr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String[]{
                getContext().getString(R.string.waiting_for_response),
                getContext().getString(R.string.waiting_for_response),
                getContext().getString(R.string.waiting_for_response)
        };
    }

    private String[] jsonArrayToArray(JSONArray arr) throws JSONException {
        String[] texts = new String[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            texts[i] = arr.getString(i);
        }
        return texts;
    }

    private boolean isContentReady() {
        return currentState == State.CONTENT_READY;
    }

    private void fetchAnswerFromApi(File fileToUpload) {
        currentState = State.LOADING;
        Log.d("API_CALL", "Requesting: " + apiUrl + UrlRoutes.ML_PDF_CIRCLE_EXPLAIN);

        if (fileToUpload == null || !fileToUpload.exists()) {
            Log.e(TAG, "API Error: Local screenshot file missing before request started.");
            handleApiError("Local file not found. Please try capturing again.");
            return;
        }

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.POST,
                apiUrl + UrlRoutes.ML_PDF_CIRCLE_EXPLAIN,
                response -> {
                    try {
                        if (!this.isShowing() || currentState == State.DISMISSED) {
                            return;
                        }

                        String responseString = new String(response.data);
                        Log.d("API_SUCCESS", responseString);

                        notesResponse =
                                new Gson().fromJson(responseString, NotesDoubtResponseBody.class);
                        if (doubtResponseDialogListener != null) {
                            doubtResponseDialogListener.onApiResponse();
                        }
                        handleApiSuccess(notesResponse);

                    } catch (Exception e) {
                        Log.e("API_PARSE_ERROR", "Failed parsing", e);
                        handleApiError("Parsing error: " + e.getMessage());
                    } finally {
                        // ── PERSISTENCE ──
                        // File is deleted only AFTER a terminal response (success or parse error)
                        deleteScreenshotFile();
                    }
                },
                error -> {
                    Log.e("API_ERROR", error.toString());
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        Log.e("API_ERROR_BODY", new String(error.networkResponse.data));
                    }
                    // ── ROBUSTNESS ──
                    // If it's a network error, don't delete the file immediately
                    // so the user might retry.
                    handleApiError("Network error occurred");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("pdf_url", requestBody.getPdfUrl());
                params.put("subject", requestBody.getSubject());
                params.put("user_id", requestBody.getUserId());
                params.put("class_name", requestBody.getStudentClass());
                params.put("language", requestBody.getLanguage());
                params.put("page_number", String.valueOf(requestBody.getPageNumber()));
                params.put("phone_number", requestBody.getPhoneNumber());
                params.put("exam_type", requestBody.getExamType());
                params.put("chapter_name", requestBody.getChapterName());
                params.put("type_of_notes", requestBody.getTypeOfNotes());
                params.put("option_selected", optionSelected);
                Log.d("API_SUCCESS", "getParams: " + params.toString());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.pdf_circle_request_initiated.getActionName(), params);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> fileParams = new HashMap<>();
                if (screenshotFile != null && screenshotFile.exists()) {
                    try {
                        byte[] data = getFileDataFromFile(screenshotFile);
                        if (data != null && data.length > 0) {
                            fileParams.put("screenshot", new DataPart(
                                    "pdf_poc_Image.jpeg", data, "image/jpeg"));
                        } else {
                            Log.e("API_FILE_ERROR", "Screenshot file is empty.");
                        }
                    } catch (IOException e) {
                        Log.e("API_FILE_ERROR", "Failed to read screenshot file", e);
                    }
                } else {
                    Log.e("API_FILE_ERROR", "Screenshot file missing during background read.");
                }
                return fileParams;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        request.setTag(TAG);

        MyVolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    private byte[] getFileDataFromFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        }
    }

    private void handleApiSuccess(NotesDoubtResponseBody notesResponse) {
        if (currentState == State.DISMISSED) return;
        try {
            Log.d("TAG", "WebViewUrl: " + webViewUrl);

            stopTextSwitcherAnimation();

            String urlToLoad = android.net.Uri.parse(webViewUrl).buildUpon()
                    .appendQueryParameter("toShowAskMoreButton", "true")
                    .build().toString();
            wvContent.loadUrl(urlToLoad);

            wvContent.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    if (hasPageLoadedOnce) return;
                    hasPageLoadedOnce = true;

                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        notesResponse.setUserId(
                                MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
                        );

                        Map<String, Object> ctx = new HashMap<>();
                        ctx.put("userId",       MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
                        ctx.put("studentName",  MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
                        ctx.put("className",    MyApplication.getStringData(PreferenceKeys.CLASS_NAME.getKey()));
                        ctx.put("phoneNumber",  MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey()));
                        ctx.put("userQuery",          notesResponse.getUserQuery());
                        ctx.put("subject",            requestBody.getSubject());
                        ctx.put("response",           notesResponse.getResponse());
                        ctx.put("showResponseBubble", true);
                        ctx.put("origin",             "PDF_CIRCLE");
                        ctx.put("title",              notesResponse.getTitle());
                        ctx.put("batchCode",    MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey()));
                        ctx.put("extractedText",      notesResponse.getOcr_text());
                        ctx.put("screenshotUrl",      notesResponse.getScreenshot_url());
                        ctx.put("requestId",          notesResponse.getRequest_id());
                        ctx.put("pdfTitle",           requestBody.getChapterName());
                        ctx.put("pageNumber",         requestBody.getPageNumber());
                        ctx.put("pageContext",        notesResponse.getPageContext());

                        String encodedCtx = objectMapper.writeValueAsString(ctx);

                        Log.i("handleApiSuccess", "┌─── Injecting " + webViewJsFunctionName + " ───");
                        Log.i("handleApiSuccess", "│  " + encodedCtx);
                        Log.i("handleApiSuccess", "└──────────────────────────────────");

                        // ── Call JS function with context, then show & expand ──────
                        // Use retry mechanism because onPageFinished can fire before
                        // the page's JS framework defines the function.
                        injectJsData(webViewJsFunctionName, encodedCtx, 0);


                    } catch (JsonProcessingException e) {
                        handleApiError("JSON parsing error: " + e.getMessage());
                    }
                }
            });

            Map<String, String> eventsParams = getFirebaseEventsParams();
            eventsParams.put("screenshotUrl",    notesResponse.getScreenshot_url());
            eventsParams.put("requestId",        notesResponse.getRequest_id());
            eventsParams.put("processing_time",  String.valueOf(notesResponse.getProcessing_time_ms()));
            eventsParams.put("option_selected",  optionSelected);
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.pdf_circle_request_completed.getActionName(), eventsParams
            );

        } catch (Exception e) {
            handleApiError("Parsing error: " + e.getMessage());
        }
    }

    private static final int MAX_INJECT_RETRIES = 10;
    private static final long INJECT_RETRY_DELAY_MS = 200;

    /**
     * Polls for the JS function availability before calling it.
     * onPageFinished fires before the page's JS framework is fully initialised,
     * so we retry up to MAX_INJECT_RETRIES times with INJECT_RETRY_DELAY_MS gaps.
     */
    private void injectJsData(String functionName, String encodedData, int attempt) {
        if (currentState == State.DISMISSED || wvContent == null) return;

        String checkJs = "(typeof " + functionName + " === 'function') ? 'ready' : 'not_ready'";
        wvContent.evaluateJavascript(checkJs, value -> {
            if ("\"ready\"".equals(value)) {
                wvContent.evaluateJavascript(
                        functionName + "(" + encodedData + ");",
                        v -> {
                            wvContent.setVisibility(View.VISIBLE);
                            expandDialogAfterWebViewLoad();
                        }
                );
            } else if (attempt < MAX_INJECT_RETRIES) {
                new Handler(Looper.getMainLooper()).postDelayed(
                        () -> injectJsData(functionName, encodedData, attempt + 1),
                        INJECT_RETRY_DELAY_MS
                );
            } else {
                Log.w(TAG, functionName + " not defined after " + MAX_INJECT_RETRIES + " retries, injecting anyway");
                wvContent.evaluateJavascript(
                        functionName + "(" + encodedData + ");",
                        v -> {
                            wvContent.setVisibility(View.VISIBLE);
                            expandDialogAfterWebViewLoad();
                        }
                );
            }
        });
    }

    /**
     * Expands dialog after WebView finishes loading
     */
    private void expandDialogAfterWebViewLoad() {
        Log.d("expandDialogAfterWebViewLoad", "expandDialogAfterWebViewLoad: ");

        layoutLoading.setVisibility(View.GONE);
        tvTitle.setVisibility(View.GONE);
        doubtProgressAnimation.setVisibility(View.GONE);
        doubtProgressAnimation.cancelAnimation();

        currentState = State.CONTENT_READY;
        setCanceledOnTouchOutside(false);
        setCancelable(true);

        // Start height at 30% of screen (preview mode)
        int previewHeight = (int) (getScreenHeight() * 0.3f);
        setDialogHeight(previewHeight);

        // Fade + slide-in effect
        dialogContainer.setAlpha(0f);
        dialogContainer.setTranslationY(30);

        dialogContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Wait until layout is ready
        dialogContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                dialogContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (currentState != State.CONTENT_READY) return;

                // Measure height
                dialogContainer.measure(
                        View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );

                int measuredHeight = dialogContainer.getMeasuredHeight();
                int maxHeight = (int) (getScreenHeight() * 0.8f);

                Log.d("expandDialogAfterWebViewLoad", "Measured height: " + measuredHeight);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    animateHeightTransition(previewHeight, Math.min(measuredHeight, maxHeight));
                }, 1000);
            }
        });
    }

    private void animateHeightTransition(int fromHeight, int toHeight) {
        cv_close.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofInt(fromHeight, toHeight);
        animator.setDuration(400); // smooth animation
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int animatedHeight = (Integer) animation.getAnimatedValue();
            setDialogHeight(animatedHeight);
        });

        animator.start();
    }

    private void setDialogHeight(int height) {
        Window window = getWindow();
        if (window == null) return;

        View decorView = window.getDecorView();
        if (decorView == null || !decorView.isAttachedToWindow()) return;

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);
    }

//    private void setDialogHeight(int height) {
//        if (getWindow() != null) {
//            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);
//        }
//    }

    private void handleApiError(String message) {
        if (currentState == State.DISMISSED) return;
        Log.e("AnswerAPI", message);

        // Stop text switcher animation
        stopTextSwitcherAnimation();

        if (!MyApplication.isNetworkAvailable()) {
            tvTitle.setText(getContext().getString(R.string.something_went_wrong));
        } else {
            tvTitle.setText(getContext().getString(R.string.apna_internet_connection_check_karke_dobara_koshish_karein));
        }

        layoutLoading.setVisibility(View.VISIBLE);
        layoutDoubtOptions.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        wvContent.setVisibility(View.GONE);
        doubtProgressAnimation.setVisibility(View.GONE);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        Map<String, String> eventsParams = getFirebaseEventsParams();
        eventsParams.put("error_message", message);
        eventsParams.put("option_selected", optionSelected);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.pdf_circle_request_failed.getActionName(),
                eventsParams);
        currentState = State.ERROR;

        if (doubtResponseDialogListener != null) {
            doubtResponseDialogListener.onError("");
        }

        // Auto dismiss after showing error
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 3000);
    }

    private Map<String, String> getFirebaseEventsParams() {
        Map<String, String> params = new HashMap<>();
        params.put("pdf_url", requestBody.getPdfUrl());
        params.put("subject", requestBody.getSubject());
        params.put("user_id", requestBody.getUserId());
        params.put("class_name", requestBody.getStudentClass());
        params.put("language", requestBody.getLanguage());
        params.put("page_number", String.valueOf(requestBody.getPageNumber()));
        params.put("phone_number", requestBody.getPhoneNumber());
        params.put("exam_type", requestBody.getExamType());
        params.put("chapter_name", requestBody.getChapterName());
        params.put("type_of_notes", requestBody.getTypeOfNotes());
        return params;
    }

    private void configureWindow() {
        Window window = getWindow();
        if (window == null) return;
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.y = 25;

        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private int calculateInitialHeight() {
        return (int) (getScreenHeight() * initialHeightRatio);
    }

    private int getScreenHeight() {
        return getContext().getResources().getDisplayMetrics().heightPixels;
    }

    private int getScreenWidth() {
        return getContext().getResources().getDisplayMetrics().widthPixels;
    }

    @UiThread
    private void expandWithThresholdCheck() {
        if (currentState != State.CONTENT_READY) return;

        int initialHeight = calculateInitialHeight();
        int maxHeight = (int) (getScreenHeight() * EXPAND_THRESHOLD_RATIO);

        dialogContainer.measure(
                View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int measuredHeight = dialogContainer.getMeasuredHeight();
        animateHeightTransition(initialHeight, Math.min(measuredHeight, maxHeight));
    }

    private void cancelCurrentAnimation() {
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel();
        }
        currentAnimator = null;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        cleanup();
    }

    private void cleanup() {
        currentState = State.DISMISSED;
        stopTextSwitcherAnimation();
        cancelCurrentAnimation();
        
        // ── ROBUSTNESS ──
        // Cancel all pending Volley requests for this dialog before deleting the file.
        // We use MyVolleySingleton for better lifecycle management.
        MyVolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll(TAG);

        // Delete the file only after we've confirmed no more API reads will happen.
        deleteScreenshotFile();

        if (wvContent != null) {
            wvContent.stopLoading();
            wvContent.clearHistory();
            wvContent.removeAllViews();
            wvContent.destroy();
        }
    }

    private void deleteScreenshotFile() {
        try {
            if (screenshotFile != null && screenshotFile.exists()) {
                boolean deleted = screenshotFile.delete();
                Log.d("FILE_DELETE", "Screenshot file deleted: " + deleted);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showDoubtOptions() {
        Map<String, String> params = getFirebaseEventsParams();
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.pdf_circle_options_shown.getActionName(), params);

        boolean hideDoubtOptions = MyApplication.getFirebaseRemoteConfig().getBoolean("hide_doubt_options");

        if (hideDoubtOptions) {
            onOptionClicked(autoSelectedOption);
            return;
        }

        currentState = State.OPTIONS;
        layoutLoading.setVisibility(View.GONE);
        layoutDoubtOptions.setVisibility(View.VISIBLE);
        wvContent.setVisibility(View.GONE);
        cv_close.setVisibility(View.VISIBLE);
    }

    public interface DoubtResponseDialogListener {
        void onError(String message);

        void onBackPressed(View view, boolean dismissDialog);

        void onApiResponse();
    }

    public class AndroidInterface {

        private final Context context;

        public AndroidInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void openActivity(String activityName) {
            new Handler(Looper.getMainLooper()).post(() -> {
                if ("DoubtChatActivity".equals(activityName)) {
                    CommonNavigator.navigateToDoubtChatWithResponse(
                            context,
                            requestBody.getSubject(),
                            notesResponse.getUserQuery(),
                            notesResponse.getResponse(),
                            notesResponse.getTitle(),
                            notesResponse.getOcr_text(),
                            notesResponse.getScreenshot_url(),
                            true,
                            "PDF_CIRCLE",
                            notesResponse.getRequest_id(),
                            requestBody.getChapterName(),
                            requestBody.getPageNumber(),
                            notesResponse.getPageContext()
                    );
                    dismiss();
                }
            });
        }
    }
}