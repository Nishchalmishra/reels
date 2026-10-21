package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_REPORT_QUESTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_ASYNC_v2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_STARTED;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_v2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TooltipCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.MCQAndSubjectiveQuestionFragment;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.asat.ASATActivity;
import arivihan.technologies.doubtbuzzter2.adapter.InstructionHeaderAdapter;
import arivihan.technologies.doubtbuzzter2.common.ArivihanCustomDialog;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.GridPopupDialog;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.fragments.LongAnswerQuestionFragment2;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.TooltipHelper;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.offline.OfflineTestManager;
import arivihan.technologies.doubtbuzzter2.listener.CaseStudyListener;
import arivihan.technologies.doubtbuzzter2.listener.GridListener;
import arivihan.technologies.doubtbuzzter2.listener.OptionListener;
import arivihan.technologies.doubtbuzzter2.model.QAnsInfo;
import arivihan.technologies.doubtbuzzter2.model.QuestionType;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesAnalytics;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import arivihan.technologies.doubtbuzzter2.model.UserMockAnswerDto;
import arivihan.technologies.doubtbuzzter2.model.UserOngoingTestStateDto;
import arivihan.technologies.doubtbuzzter2.model.offline.OfflineTestSeriesModel;
import arivihan.technologies.doubtbuzzter2.model.offline.OfflineTestSubmissionStatus;
import arivihan.technologies.doubtbuzzter2.model.offline.OfflineTestTypes;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TestSeriesConfig;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TestSeriesWarningConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class MockQuestionFInal extends BaseActivity implements GridListener, OptionListener, CaseStudyListener, HowToVideoView.RemoveListener {

    private static final String TAG = MockQuestionFInal.class.getSimpleName();
    Animator revealAnimator;
    private RelativeLayout caseStudyFab;
    private MaterialCardView caseStudyCard;
    private TextView caseStudyText;
    private TextView caseStudyNumText;
    Handler handler = new Handler();
    Runnable runnable;
    private ArivihanCustomDialog caseStudyDialog;
    Gson gson = new Gson();
    private LottieAnimationView lottieAnimationView;
    private Dialog progressDialog;
    private TextView toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    public static CountDownTimer countDownTimer;
    private List<TestSeriesQuestionDto> list;
    private ViewPagerAdapter adapter;
    TestSeriesAnalytics testSeriesAnalyticsReview = null;
    private LinearLayout image;
    private GridPopupDialog fr;
    TextView timeRemaining;
    private long timeTaken;
    private LinearLayout submit;
    ArrayList<List<String>> instructionList = new ArrayList<>();
    InstructionHeaderAdapter instructionHeaderAdapter;
    private Dialog dialog;
    private String testId;
    private String id;
    private String testTitle;
    private String testSeriesName;
    private boolean attempted;
    String Type = "";
    LottieAnimationView timeUpAnimation;
    boolean testSubmitted = false;
    private String instructions;
    ImageView image_clock, reportBtn;
    LinearLayout backActivityBtn;
    private boolean timeUp;
    private AlertDialog no_internet_connection;
    private ProgressBar progressBar;
    private RelativeLayout progress, parentLayout;
    private CardView submitAnswer, resumeMockTest;
    private String reportText;
    private Dialog analysingDialog;
    long timeToAttempt;
    int selectedTab;
    private Dialog matchTheseDialog;
    private boolean isPaused;
    private CardView how_to_video_icon;
    private HowToVideoView howToVideoView;
    private View how_to_video_view;
    private Handler howToHandler;
    private Runnable howToRunnable;
    private boolean show=true;
    private String asatId, processInfoId;
    private boolean showMatch=true, isAsat;
    NetworkStatusLiveData networkStatusLiveData;
    private int testExitCounter;
    private Dialog attemptWarningDialog;
    private int exitCount = 0;
    private final int EXIT_LIMIT = 3;
    private boolean isAppInForeground = true;
    private boolean singleAttempt, autoSubmitEnabled = false;
//    private boolean isUserNavigatingAway = false;
    private TestSeriesConfig testSeriesConfig;

    @Override
    public void onResume() {
        super.onResume();
        getData();
        if(howToVideoView!=null && howToVideoView.videoView!=null) {
            howToVideoView.resumePlayer();
        }

//        Log.d(TAG, "onResume: dialogVisibility: "+testSeriesConfig.isTestInterruptionDialogVisibility()+", isAttempt: "+isTestAttempt()+", isAppInForeground: "+isAppInForeground);
        if(testSeriesConfig!=null && testSeriesConfig.isTestInterruptionDialogVisibility() && isTestAttempt() && singleAttempt) {
            if (!isAppInForeground) {
                exitCount++;
                if (exitCount >= ((testSeriesConfig.getExitAllowedCount()>0)? testSeriesConfig.getExitAllowedCount() : EXIT_LIMIT)) {
                    //Submit logic
                    autoSubmitEnabled = true;
                    dismissAllDialogs();
                    showSubmitTestDialog(autoSubmitEnabled);
//                    submitTest();
                } else {
                    // warning dialog
                    showAttemptWarningDialog(null);
                }
            }
            isAppInForeground = true;
        }

        Log.d(TAG,"OfflineData: "+((OfflineTestManager.getModelById(testId)!=null)?OfflineTestManager.getModelById(testId).toString():OfflineTestManager.getAllModels().size()));
    }

    private void getData() {
        if (Type != null && Type.equals("ATTEMPT")) {
            if (!testSubmitted && isPaused) {
                isPaused = false;
                UserOngoingTestStateDto userOngoingTestStateDto =
                        MyApplication.getObject(getString(R.string.UserOngoingTestState));
                if (userOngoingTestStateDto != null) {
                    Log.d("UserOngoingTestStateDto", userOngoingTestStateDto.toString());
                    if (userOngoingTestStateDto.getMap() != null && !userOngoingTestStateDto.getMap().isEmpty()) {
                        setUserAnswer(userOngoingTestStateDto);
                    }
                }
            }
        }
    }

    private void setUserAnswer(UserOngoingTestStateDto userOngoingTestStateDto){
        for(TestSeriesQuestionDto testSeriesQuestionDto : list){
            if(userOngoingTestStateDto.getMap().containsKey(testSeriesQuestionDto.getQuestionId())){
                testSeriesQuestionDto.setUserAnswer(userOngoingTestStateDto.getMap().get(testSeriesQuestionDto.getQuestionId()));
            }
        }
        adapter.notifyDataSetChanged();
        selectedTab = userOngoingTestStateDto.getLastSelectedTab();
        tabLayout.post(() -> {
            tabLayout.selectTab(tabLayout.getTabAt(selectedTab));
            viewPager.setCurrentItem(selectedTab, true);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        saveOngoingTestState();
        saveOfflineData();
        if(matchTheseDialog!=null)
            matchTheseDialog.dismiss();
        if(howToVideoView!=null && howToVideoView.videoView!=null)
            howToVideoView.pausePlayer();
        dismissAttemptWarningDialog();
        isAppInForeground = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        //Disable Screen Schots----

        setContentView(R.layout.activity_mock_question);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                long startTime = System.currentTimeMillis();
                Log.d("OnBackPressed", "onBackPressed: started");

                if(howToVideoView!=null && howToVideoView.videoView!=null
                        && howToVideoView.videoView.getVisibility()==View.VISIBLE && howToVideoView.expanded ) {
                    howToVideoView.resize();
                }else if (Type != null && Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                    destroyHowTo();
                    finish();
                } else {
                    CommonMethods.showNegativeSnackbar(image,
                            getString(R.string.not_allowed_to_back));
                }

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                Log.d("OnBackPressed", "onBackPressed: completed in " + duration + " ms");
            }
        });

        MyApplication.getInstance().getTooltipViewModel().updateTooltipViewModel();
        updateFragmentForTooltip();
        secureScreen();
        MyApplication.saveObject(getString(R.string.UserOngoingTestState), null);
        parentLayout = findViewById(R.id.ParentRelativeLayout);
        testId = getIntent().getStringExtra("testId");
        Log.d("TestId", testId);
        MyApplication.setCaseStudyGroupList(testId,null);
        id = getIntent().getStringExtra("Id");
        processInfoId = getIntent().getStringExtra("processInfoId");
        asatId = getIntent().getStringExtra("asatId");
        isAsat = getIntent().getBooleanExtra("isAsat", false);
        Log.d("AsatID" , "Id = " +asatId);
        testTitle = getIntent().getStringExtra("testTitle");
        testSeriesName = getIntent().getStringExtra("testSeriesName");
        image_clock = findViewById(R.id.image_clock);
        attempted = getIntent().getBooleanExtra("attempted", false);
        singleAttempt = getIntent().getBooleanExtra("singleAttempt", false);
        testSeriesAnalyticsReview = MyApplication.getTestSeriesAnalytics("testQuestionResult");
        startTest(testId);
        toolbar = findViewById(R.id.toolbar);
        Type = getIntent().getStringExtra(PreferenceKeys.RESPONSE.getKey());
        how_to_video_view = findViewById(R.id.how_to_video_view);
        how_to_video_icon = findViewById(R.id.how_to_video_icon);
        if (MyApplication.getFirebaseRemoteConfig().getBoolean("hide_how_to_video_icon_test") ||
                MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos"))
            how_to_video_icon.setVisibility(View.GONE);
        how_to_video_icon.setOnClickListener(view -> {
            openExpandedVideo();
        });
        timeRemaining = findViewById(R.id.timeRemaining);
        timeUpAnimation = findViewById(R.id.TimeUpAnimation);
        Type = MyApplication.getStringData(PreferenceKeys.RESPONSE.getKey());
        list = MyApplication.getTestSeriesQuestionList("questions");
        instructions = getIntent().getStringExtra("instruction");
        timeToAttempt = getIntent().getLongExtra("timeToAttempt", 3600000);
        image = findViewById(R.id.image);
        submit = findViewById(R.id.submit);
        reportBtn = findViewById(R.id.report_btn);
        backActivityBtn = findViewById(R.id.back_activity_btn);
        setTestIdInCustomKeys();

        Log.d(TAG, " id: "+id+", testId: "+testId+", attempt: "+attempted+", type:" +Type+", TestName: "+testTitle+", TestSeriesName: "+testSeriesName);
        instructionList.addAll(CommonMethods.convertToList(instructions));
        instructionHeaderAdapter = new InstructionHeaderAdapter(instructionList, this);

        fr = new GridPopupDialog(list, this, instructionHeaderAdapter);

        String firebaseConfigJsonStr = MyApplication.getFirebaseRemoteConfig()
                .getString("testseries_config_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        Gson gson = new Gson();
        testSeriesConfig = gson.fromJson(firebaseConfigJsonStr, TestSeriesConfig.class);

        findViewById(R.id.overview_btn).setOnClickListener(v -> {
            try {

                if (fr == null) {
                    fr = new GridPopupDialog(list, this, instructionHeaderAdapter);
                }
                if (!fr.isVisible()) {
                    fr.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
                    fr.show(getSupportFragmentManager(), "example");

                }
            } catch (Exception e) {
                Log.d("Error", "error");
            }

        });


        reportBtn.setOnClickListener(v -> showReportSolutionDialog());

        backActivityBtn.setOnClickListener(v -> {
            if (Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                finish();
            } else {
                showSubmitTestDialog(autoSubmitEnabled);
            }
        });

        if (Type == null) {
            if (!isFinishing()) {
                CommonMethods.showNegativeSnackbar(image, getString(R.string.encountered_some_error));
            }
            getOnBackPressedDispatcher().onBackPressed();
        }


        submit.setOnClickListener(v -> {
            if (!isFinishing()) {
                showSubmitTestDialog(autoSubmitEnabled);
            }
        });


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        if(Type.equals(PreferenceKeys.RESPONSE.getKey())) {
            for (int j = 0; j < list.size(); j++) {
                setupCustomTab(j);
            }
        }else{
            tabLayout.setupWithViewPager(viewPager);
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                TooltipHelper.getInstance().dismiss();
                selectedTab = position;
                if(show || showMatch) startHowToHandler();
                updateFragmentForTooltip();
                tabLayout.selectTab(tabLayout.getTabAt(position));
                if (Type != null && Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                    applyCustomFont(selectedTab);
                    toolbar.setText(CommonMethods.getTimeInHourAndMinute(list.get(position).getTimeTaken(), MockQuestionFInal.this));
                }
                if(matchTheseDialog!=null)
                    matchTheseDialog.dismiss();
                TestSeriesQuestionDto testSeriesQuestionDto = list.get(position);
                if(Type!=null && !Type.equals(PreferenceKeys.RESPONSE.getKey()) &&
                        testSeriesQuestionDto.getQuestionType().equalsIgnoreCase(QuestionType.MATCH_THE_COLUMN.name()))
                        setMatchTheseDialog();

                if (testSeriesQuestionDto.getQuestionType().equalsIgnoreCase(QuestionType.LONG_ANSWERS.name()) ||
                        testSeriesQuestionDto.getQuestionType().equalsIgnoreCase(QuestionType.SHORT_ANSWERS.name()) ||
                        testSeriesQuestionDto.getQuestionType().equalsIgnoreCase(QuestionType.VERY_SHORT_ANSWERS.name()) ||
                        testSeriesQuestionDto.getQuestionType().equalsIgnoreCase(QuestionType.MATCH_THE_COLUMN.name())) {
                    LongAnswerQuestionFragment2 childFragment = (LongAnswerQuestionFragment2) adapter.instantiateItem(viewPager, position);
                    childFragment.updateView(position,position==list.size()-1);
                } else {
                    MCQAndSubjectiveQuestionFragment childFragment = (MCQAndSubjectiveQuestionFragment) adapter.instantiateItem(viewPager, position);
                    childFragment.updateView(position,position==list.size()-1);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {}

        });

        for (int i=0; i<tabLayout.getTabCount(); i++) {
            TooltipCompat.setTooltipText(Objects.
                    requireNonNull(tabLayout.
                            getTabAt(i)).view, null);
        }
        tabLayout.setTabRippleColor(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.mock_light_grey)));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (Type != null && Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                    applyCustomFont(selectedTab);
                }
                selectedTab = tab.getPosition();
                BoardQuestionActivity.isMatchTheseSubmitted = false;
                viewPager.setCurrentItem(selectedTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


        handleTimer(timeToAttempt);
        if (Type != null && Type.equals(PreferenceKeys.RESPONSE.getKey())) {
            reportBtn.setVisibility(View.VISIBLE);
            submit.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            timeRemaining.setVisibility(View.GONE);
            toolbar.setText(CommonMethods.getTimeInHourAndMinute(list.get(viewPager.getCurrentItem()).getTimeTaken(), MockQuestionFInal.this));
            try {
                countDownTimer.cancel();
            } catch (Exception e) {

            }
        }

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(image, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);

                    if ((timeUp && !testSubmitted) || autoSubmitEnabled) {
                        if (no_internet_connection != null)
                            no_internet_connection.dismiss();
                        submitTest();
                    } else {
                        if (no_internet_connection != null)
                            no_internet_connection.dismiss();
                    }

                } else {
                    if (no_internet_connection != null)
                        no_internet_connection.dismiss();
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(image, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                    showAlert();
                }
            }
        });

        if (CommonMethods.isOnline(this)) {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);

            }
        } else {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        }
        setInProgressDialog();

        secureScreen();
    }

    @Override
    public void onRemove() {
        MyApplication.saveData(PreferenceKeys.HIDE_TOOLTIP_FOR_HOW_TO.getKey(),false);
        TooltipHelper.getInstance().onHowToRemove();
    }

    @Override
    public void onBottomSheetOpen() {
        if(howToVideoView!=null)
            howToVideoView.pausePlayer();
    }

    @Override
    public void onBottomSheetClose() {
        if(howToVideoView!=null)
            howToVideoView.resumePlayer();
    }

    private boolean isTestAttempt() {
        return (Type != null && Type.equals("ATTEMPT"));
    }

    private void saveOfflineData() {
        if(isTestAttempt() && !testSubmitted) {
            UserMockAnswerDto userMockAnswerDto = getUserMockAnswerDto();
            OfflineTestSeriesModel offlineTestSeriesModel = new OfflineTestSeriesModel();
            offlineTestSeriesModel.setId(testId);
            offlineTestSeriesModel.setTestSeresName(testSeriesName);
            offlineTestSeriesModel.setTestSeriesId(testId);
            offlineTestSeriesModel.setTestId(testId);
            offlineTestSeriesModel.setBoard(userMockAnswerDto.isBoard());
            offlineTestSeriesModel.setSingleAttempt(singleAttempt);
            offlineTestSeriesModel.setStatus(OfflineTestSubmissionStatus.PENDING);
            offlineTestSeriesModel.setTestType((userMockAnswerDto.getAsatId() != null || isAsat) ? OfflineTestTypes.ASAT : OfflineTestTypes.TEST_SERIES);
            offlineTestSeriesModel.setUserMockAnswerDto(userMockAnswerDto);

            OfflineTestManager.saveOrUpdateModel(offlineTestSeriesModel);
        }
    }
    private void removeOfflineTestData() {
        OfflineTestManager.deleteModelById(testId);
    }

    private void startHowToHandler() {
        if (howToHandler != null) {
            howToHandler.removeCallbacksAndMessages(null);
        }

        TestSeriesQuestionDto dto = list.get(selectedTab);
        boolean isSAVType = dto.getQuestionType().equalsIgnoreCase(QuestionType.LONG_ANSWERS.name()) ||
                dto.getQuestionType().equalsIgnoreCase(QuestionType.SHORT_ANSWERS.name()) ||
                dto.getQuestionType().equalsIgnoreCase(QuestionType.VERY_SHORT_ANSWERS.name());

        boolean shouldShowHowTo = !MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos");
        boolean isHowToViewUninitialized = (howToVideoView == null || howToVideoView.videoView == null || howToVideoView.player == null);

        if (shouldShowHowTo && isHowToViewUninitialized) {
            howToVideoView = HowToVideoView.getInstance(MockQuestionFInal.this);
            howToVideoView.setRemoveListener(this);
            howToVideoView.setView(MockQuestionFInal.this, how_to_video_view);

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

            if ("RESPONSE".equals(Type)) {
                if (isSAVType && show) {
                    show = false;
                    initializeHowToVideo(HOW_TO_TYPE.QUESTION_TYPE_SAV_ANALYSIS_TEST, true, R.string.Analyse_Now, lang);
                } else if (dto.getQuestionType().equalsIgnoreCase(QuestionType.MATCH_THE_COLUMN.name()) && showMatch) {
                    showMatch = false;
                    initializeHowToVideo(HOW_TO_TYPE.QUESTION_TYPE_MATCH_TEST_ANALYSIS, true, R.string.Analyse_Now, lang);
                }
            } else {
                if (isSAVType && show) {
                    show = false;
                    initializeHowToVideo(HOW_TO_TYPE.QUESTION_TYPE_SAV_TEST_ATTEMPT, false, R.string.start_now, lang);
                } else if (dto.getQuestionType().equalsIgnoreCase(QuestionType.MATCH_THE_COLUMN.name()) && showMatch) {
                    showMatch = false;
                    initializeHowToVideo(HOW_TO_TYPE.QUESTION_TYPE_MATCH_TEST_ATTEMPT, false, R.string.start_now, lang);
                }
            }
        }
    }
    private void initializeHowToVideo(HOW_TO_TYPE type, boolean isAnalysis, int buttonTextResId, String lang) {
        String suffix = isAnalysis ? "_ANALYSIS_COUNT" : "_COUNT";
        String key = type.name() + suffix + lang;
        int count = MyApplication.getIntData(key);
        if (count < 0) count = 0;

        Log.d("debugcount", "getHowToVideoType: " + count + " type: " + type.name());
        
        if (count == 0 && checkVideoUrlIsAvailable(type)) {
            MyApplication.saveData(PreferenceKeys.HIDE_TOOLTIP_FOR_HOW_TO.getKey(), true);

            howToHandler = new Handler();
            howToRunnable = () -> {
                howToVideoView.setType(type, false);
                howToVideoView.setButtonText(getString(buttonTextResId));
                MyApplication.saveData(key, 1);
            };
            howToHandler.postDelayed(howToRunnable, 2000);
        }
    }
    
    private boolean checkVideoUrlIsAvailable(HOW_TO_TYPE type) {
        return StringUtils.isNotEmpty(howToVideoView.getUrlBasedOnType(type));
    }


    private void logFirebaseEvent(String contentType, String mode){
        Map<String,String> params = new HashMap<>();
        params.put("action", "practice_how_to_card_click");
        params.put("contentType", contentType);
        params.put("action", "test_how_to_card_click");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.test_how_to_image_click.getActionName(), params);
    }

    private void openExpandedVideo() {
        MyApplication.saveData(PreferenceKeys.HIDE_TOOLTIP_FOR_HOW_TO.getKey(),true);
        TooltipHelper.getInstance().onHowToRemove();
        TestSeriesQuestionDto dto = list.get(selectedTab);
        howToVideoView = HowToVideoView.getInstance(MockQuestionFInal.this);
        howToVideoView.setRemoveListener(this);
        howToVideoView.setView(MockQuestionFInal.this, how_to_video_view);
        boolean b = dto.getQuestionType().equalsIgnoreCase(QuestionType.LONG_ANSWERS.name()) ||
                dto.getQuestionType().equalsIgnoreCase(QuestionType.SHORT_ANSWERS.name()) ||
                dto.getQuestionType().equalsIgnoreCase(QuestionType.VERY_SHORT_ANSWERS.name());
        if (Type != null && Type.equals(PreferenceKeys.RESPONSE.getKey())) {
            logFirebaseEvent(dto.getQuestionType(),"analysis");
            if (b) {
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_SAV_ANALYSIS_TEST, true);
            } else if(dto.getQuestionType().equalsIgnoreCase(QuestionType.MATCH_THE_COLUMN.name())){
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_MATCH_TEST_ANALYSIS, true);
            }else{
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_A_ANALYSIS, true);
            }
            howToVideoView.setButtonText(getString(R.string.Analyse_Now));
        } else {
            logFirebaseEvent(dto.getQuestionType(),"attempt");

            if (b) {
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_SAV_TEST_ATTEMPT, true);
            } else if(dto.getQuestionType().equalsIgnoreCase(QuestionType.MATCH_THE_COLUMN.name())) {
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_MATCH_TEST_ATTEMPT, true);
            }else{
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_A_ATTEMPT_TEST, true);
            }
            howToVideoView.setButtonText(getString(R.string.start_now));
        }
    }

    private void updateFragmentForTooltip() {
        TooltipHelper.getInstance().dismiss();
        MyApplication.getInstance().getTooltipViewModel().updateFragment(selectedTab,this);
    }

    private void setMatchTheseDialog() {
        boolean showMatchTheseDialog = MyApplication.getBoolData(PreferenceKeys.SHOW_MATCH_THESE_DIALOG.getKey());
        if (showMatchTheseDialog) {
            matchTheseDialog = new Dialog(this);
            matchTheseDialog.setContentView(R.layout.match_these_dialog);
            matchTheseDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
            matchTheseDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lottieAnimationView = matchTheseDialog.findViewById(R.id.lottie);
            lottieAnimationView.setRotation(90);
            lottieAnimationView.setAnimation(R.raw.drag_and_drop);
            lottieAnimationView.playAnimation();
            matchTheseDialog.findViewById(R.id.close).setOnClickListener(view -> {
                lottieAnimationView.pauseAnimation();
                matchTheseDialog.dismiss();
            });
            matchTheseDialog.findViewById(R.id.close_btn).setOnClickListener(view -> {
                lottieAnimationView.pauseAnimation();
                matchTheseDialog.dismiss();
            });
            matchTheseDialog.show();
            MyApplication.saveData(PreferenceKeys.SHOW_MATCH_THESE_DIALOG.getKey(), false);
        }
    }

    private void handleTimer(long time){
        countDownTimer = new CountDownTimer(
                time, 1000) {
            public void onTick(long millisUntilFinished) {
                Log.d("countDownTime", "" + millisUntilFinished);
                Log.d("timeToAttempt", "" + time);
                timeTaken = millisUntilFinished;
                MyApplication.saveData(PreferenceKeys.TIME_TAKEN.getKey(), timeTaken);
                toolbar.setText(CommonMethods.getTimeInHourAndMinute(millisUntilFinished, MockQuestionFInal.this));
                if (millisUntilFinished < 600000) {
                    toolbar.setTextColor(getResources().getColor(R.color.red));
                    image_clock.setImageDrawable(getResources().getDrawable(R.drawable.time_outline_red));
                }

            }

            public void onFinish() {
                timeUp = true;
                timeTaken = time;
                Vibration();
                if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {

                    if (!testSubmitted) {
                        if (!isFinishing()) {
//                            showSubmitTestDialog();
                        }
//                        submitTest();
                        showTestFinishDialog();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                submitTest();
                            }
                        }, 3000);
                    }
                }
                else {
                    CommonMethods.showNegativeSnackbar(image, getString(R.string.NoInternetConnection) + "   " + getString(R.string.TimeUp) + "!!");
                    viewPager.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    timeUpAnimation.setVisibility(View.VISIBLE);
                    showAlert();
                }
            }
        };
        countDownTimer.start();
    }

    private void showTestFinishDialog() {
        Dialog finishDialog = new Dialog(MockQuestionFInal.this);
        finishDialog.setContentView(R.layout.dialog_finish_mock_test);
        finishDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        finishDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        finishDialog.setCancelable(false);
        finishDialog.show();
    }

    private void setInProgressDialog() {
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.arivihan_dialog);
        progressDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        lottieAnimationView = progressDialog.findViewById(R.id.lottie);
        lottieAnimationView.setAnimation(R.raw.edit_routine_lottie_star);
    }

    private void showSubmitTestDialog(boolean autoSubmit) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = new Dialog(MockQuestionFInal.this);
        dialog.setContentView(R.layout.mock_test_submit_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        submitAnswer = dialog.findViewById(R.id.submit_mock_test);
        resumeMockTest = dialog.findViewById(R.id.resume_mock_test);
        progressBar = dialog.findViewById(R.id.progress_dataLoading);
        progress = dialog.findViewById(R.id.progress);

        TextView answeredQuestions = dialog.findViewById(R.id.answered_question_count);
        TextView markedQuestions = dialog.findViewById(R.id.marked_question_count);
        TextView unansweredQuestions = dialog.findViewById(R.id.unanswered_question_count);

        int answeredCount = 0;
        int markedCount = 0;
        int unansweredCount = 0;

        for (TestSeriesQuestionDto item : list) {
            if (item.getUserAnswer() != null) {
                answeredCount++;
            }
            if (item.isMarked()) {
                markedCount++;
            }
            if (item.getUserAnswer() == null) {
                unansweredCount++;
            }
        }

        answeredQuestions.setText(MessageFormat.format("{0} {1}", answeredCount, getString(R.string.answered)));
        markedQuestions.setText(MessageFormat.format("{0} {1}", markedCount, getString(R.string.marked)));
        unansweredQuestions.setText(MessageFormat.format("{0} {1}", unansweredCount, getString(R.string.Unanswered)));

        resumeMockTest.setOnClickListener(v -> {
            dialog.dismiss();
        });

        submitAnswer.setOnClickListener(view -> {
            if (!testSubmitted) {
                submitAnswer.setVisibility(View.GONE);
                resumeMockTest.setVisibility(View.GONE);
                if(!submitTest()) {
                    submitAnswer.setVisibility(View.VISIBLE);
                    resumeMockTest.setVisibility(View.VISIBLE);
                }
            }
        });

        if(autoSubmit) {
            submitAnswer.performClick();
        }

        dialog.show();
    }

    private void showReportSolutionDialog() {
        Dialog solutionDialog = new Dialog(MockQuestionFInal.this);
        solutionDialog.setContentView(R.layout.dialog_report_mock_test_solution);
        solutionDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        solutionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        solutionDialog.setCancelable(false);

        solutionDialog.findViewById(R.id.close_btn).setOnClickListener(v -> solutionDialog.dismiss());

        TextView reportText1 = solutionDialog.findViewById(R.id.report_text_1);
        TextView reportText2 = solutionDialog.findViewById(R.id.report_text_2);
        TextView reportText3 = solutionDialog.findViewById(R.id.report_text_3);
        RelativeLayout progress = solutionDialog.findViewById(R.id.progress);

        reportText1.setOnClickListener(v -> {
            reportText1.setTextColor(getResources().getColor(R.color.arivihan_secondary_color));
            reportText = reportText1.getText().toString();
            reportText2.setTextColor(getResources().getColor(R.color.black));
            reportText3.setTextColor(getResources().getColor(R.color.black));
        });

        reportText2.setOnClickListener(v -> {
            reportText1.setTextColor(getResources().getColor(R.color.black));
            reportText2.setTextColor(getResources().getColor(R.color.arivihan_secondary_color));
            reportText = reportText2.getText().toString();
            reportText3.setTextColor(getResources().getColor(R.color.black));
        });

        reportText3.setOnClickListener(v -> {
            reportText1.setTextColor(getResources().getColor(R.color.black));
            reportText2.setTextColor(getResources().getColor(R.color.black));
            reportText3.setTextColor(getResources().getColor(R.color.arivihan_secondary_color));
            reportText = reportText3.getText().toString();
        });

        EditText reportInput = solutionDialog.findViewById(R.id.report_text_input);
        reportInput.setOnFocusChangeListener((v, w) -> {
            reportText1.setTextColor(getResources().getColor(R.color.black));
            reportText2.setTextColor(getResources().getColor(R.color.black));
            reportText3.setTextColor(getResources().getColor(R.color.arivihan_secondary_color));
        });

        solutionDialog.findViewById(R.id.report_question).setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            JSONObject body = new JSONObject();
            try {
                body.put("description", "Question ID :: " + list.get(viewPager.getCurrentItem()).getQuestionId());
                body.put("issue", reportText == null ? reportInput.getText() : reportText);
                body.put("testSeriesCategory", MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()).toString());
                body.put("testSeriesId", testId);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            MyJsonObjectRequest submitReport = new MyJsonObjectRequest(Request.Method.POST,
                    TEST_REPORT_QUESTION,
                    body,
                    response -> {
                        Log.i("REPORTRESPONSE", response.toString());
                        CommonMethods.showPositiveSnackbar(parentLayout, getString(R.string.Report_Submitted));
                        progress.setVisibility(View.GONE);
                        solutionDialog.dismiss();
                    },
                    new VolleyGlobalErrorListener(getApplicationContext()) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.i("REPORTRESPONSE", error.toString());
                        }
                    });

            MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(submitReport);
        });

        solutionDialog.show();
    }

    private boolean submitTest() {
        if (!CommonMethods.isOnline(this)) {
            CommonMethods.showNegativeSnackbar(image, getString(R.string.NoInternetConnection));
            return false;
        } else {
            if (progress != null && progressBar != null && submitAnswer != null && resumeMockTest != null) {
                progress.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                submitAnswer.setVisibility(View.GONE);
                resumeMockTest.setVisibility(View.GONE);
            }

            Map<String, QAnsInfo> map = new HashMap<>();
            boolean board = false;
            int numberOfSavAttempted =0;
            for (TestSeriesQuestionDto testSeriesQuestionDto : list) {
                if (StringUtils.isEmpty(testSeriesQuestionDto.getUserAnswer())) {
                    continue;
                }
                if (testSeriesQuestionDto.getQuestionTypeEnum() == QuestionType.LONG_ANSWERS
                        || testSeriesQuestionDto.getQuestionTypeEnum() == QuestionType.SHORT_ANSWERS) {
                    board = true;
                    numberOfSavAttempted++;
                }
                QAnsInfo qAnsInfo = new QAnsInfo(testSeriesQuestionDto.getUserAnswer(),
                        testSeriesQuestionDto.getTimeTaken());
                map.put(testSeriesQuestionDto.getQuestionId(), qAnsInfo);
            }
            UserMockAnswerDto userMockAnswerDto = new UserMockAnswerDto(id,testTitle,testSeriesName,testId, timeTaken, map,numberOfSavAttempted,true, asatId, processInfoId, board);
            if (!board) {
                submitNew(userMockAnswerDto);
            } else {
                submitAsync(userMockAnswerDto);
            }
            return true;
        }
    }

    private UserMockAnswerDto getUserMockAnswerDto() {
        Map<String, QAnsInfo> map = new HashMap<>();
        boolean board = false;
        int numberOfSavAttempted =0;
        for (TestSeriesQuestionDto testSeriesQuestionDto : list) {
            if (StringUtils.isEmpty(testSeriesQuestionDto.getUserAnswer())) {
                continue;
            }
            if (testSeriesQuestionDto.getQuestionTypeEnum() == QuestionType.LONG_ANSWERS
                    || testSeriesQuestionDto.getQuestionTypeEnum() == QuestionType.SHORT_ANSWERS) {
                board = true;
                numberOfSavAttempted++;
            }
            QAnsInfo qAnsInfo = new QAnsInfo(testSeriesQuestionDto.getUserAnswer(), testSeriesQuestionDto.getTimeTaken());
            map.put(testSeriesQuestionDto.getQuestionId(), qAnsInfo);
        }
        return new UserMockAnswerDto(id,testTitle,testSeriesName,testId, timeTaken, map,numberOfSavAttempted,true, asatId, processInfoId, board);
    }

    private void saveOngoingTestState(){
        if (Type != null && Type.equals("ATTEMPT")) {
            if (!testSubmitted) {
                isPaused=true;
                HashMap<String, String> map = new HashMap<>();
                for (TestSeriesQuestionDto testSeriesQuestionDto : list) {
                    if (StringUtils.isEmpty(testSeriesQuestionDto.getUserAnswer())) {
                        continue;
                    }

                    map.put(testSeriesQuestionDto.getQuestionId(), testSeriesQuestionDto.getUserAnswer());
                }
                UserOngoingTestStateDto userOngoingTestStateDto = UserOngoingTestStateDto.builder()
                        .lastSelectedTab(selectedTab)
                        .map(map)
                        .build();
                MyApplication.saveObject(getString(R.string.UserOngoingTestState), userOngoingTestStateDto);
            }
        }
    }

    private void showAlert() {
        no_internet_connection = new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please enable internet connection. Mock test requires active internet connection.")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert).create();

        if (!isFinishing()) {
            no_internet_connection.show();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (no_internet_connection != null) {
            no_internet_connection.dismiss();
        }
        dismissAllDialogs();


    }

    private void setTestIdInCustomKeys() {
        try {
            FirebaseCrashlytics crashlytics = MyApplication.getFirebaseInstance();

            if (Type != null && Type.equals("ATTEMPT")) {
                crashlytics.setCustomKey("type", "ATTEMPT");
            } else if (Type != null && Type.equals("REATTEMPT")) {
                crashlytics.setCustomKey("type", "REATTEMPT");
            } else if (Type != null && Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                crashlytics.setCustomKey("type", PreferenceKeys.RESPONSE.getKey());
            } else {
                crashlytics.setCustomKey("type", "UNKNOWN");
            }

            crashlytics.setCustomKey("testId", "" + testId);
            if (!CollectionUtils.isEmpty(list)) {
                MyApplication.getFirebaseInstance().log("Total number of question ::  " + list.size());
            } else {
                MyApplication.getFirebaseInstance().log("Total number of question is null ");

            }

        } catch (Exception e) {
            Log.d("Error", "Error");
        }
    }

    private void startTest(String testId) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("testSeriesId", testId);
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(TEST_SERIES_STARTED, map), null,
                    response -> {
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
        } catch (Exception e) {
            Log.d("Error", "Error");
        }
    }

    private void saveOfflineTestData() {

    }

    private void Vibration() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        else
            v.vibrate(1000);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle oldInstanceState) {
        super.onSaveInstanceState(oldInstanceState);
        if (oldInstanceState != null) {
            oldInstanceState.clear();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Map<String, Integer> map;
        try {
            String limitMap = getIntent().getStringExtra("limit");
            if (!StringUtils.isEmpty(limitMap)) {
                TypeToken<Map<String, Integer>> token = new TypeToken<Map<String, Integer>>() {
                };
                map = gson.fromJson(limitMap, token.getType());
            } else {
                map = new HashMap<>();
            }
        } catch (Exception e) {
            map = new HashMap<>();
        }

        if (!CollectionUtils.isEmpty(list)) {
            int i = 1;
            for (TestSeriesQuestionDto dto : list) {
                dto.setTitle("" + i);
                dto.setFragNumber(i);
                if (testSeriesAnalyticsReview != null) {
                    adapter.addFrag(MCQAndSubjectiveQuestionFragment.newInstance(i, dto, testSeriesAnalyticsReview.getTestSeriesQuestionDtoList().get(i - 1), map, false,testId, viewPager.getCurrentItem() == i), dto.getTitle());
                } else {
                    if (dto.getQuestionType().equalsIgnoreCase(QuestionType.LONG_ANSWERS.name()) ||
                            dto.getQuestionType().equalsIgnoreCase(QuestionType.SHORT_ANSWERS.name()) ||
                            dto.getQuestionType().equalsIgnoreCase(QuestionType.VERY_SHORT_ANSWERS.name()) ||
                            dto.getQuestionType().equalsIgnoreCase(QuestionType.MATCH_THE_COLUMN.name())) {
                        boolean isReviewMode = Type != null && Type.equals(PreferenceKeys.RESPONSE.getKey());
                        adapter.addFrag(LongAnswerQuestionFragment2.newInstance(i, dto, null, map, isReviewMode,testId, viewPager.getCurrentItem() == i), dto.getTitle());
                    } else {
                        adapter.addFrag(MCQAndSubjectiveQuestionFragment.newInstance(i, dto, null, map, false, testId, viewPager.getCurrentItem() == i), dto.getTitle());
                    }
                }
                i++;

            }
            viewPager.setAdapter(adapter);
        }
    }

    private void setupCustomTab(int pos) {
        TabLayout.Tab tab = tabLayout.newTab();
        View customTab = LayoutInflater.from(this).inflate(R.layout.custom_tab_view, null);
        TextView tabNumber = customTab.findViewById(R.id.tab_text);
        tabNumber.setText(String.valueOf(pos + 1));
        MaterialCardView cardView = customTab.findViewById(R.id.tab_card);
        int percent = 0;
        try {
            percent = (list.get(pos).getUserScore() * 100) / list.get(pos).getScore();
        } catch (Exception e) {
            Log.e("updateTab", e.getMessage(), e);
        }
        if(list.get(pos).getUserAnswer()!=null) {
            if (percent >= 50)
                cardView.setCardBackgroundColor(getResources().getColor(R.color.right_tab));
            else
                cardView.setCardBackgroundColor(getResources().getColor(R.color.wrong_tab));
        }
        tab.setCustomView(customTab);
        tabLayout.addTab(tab, pos);

    }

    private void applyCustomFont(int position) {
        Typeface customFontSelected = ResourcesCompat.getFont(this, R.font.urbanist_bold);
        TextView tabNumber = tabLayout.getTabAt(position).getCustomView().findViewById(R.id.tab_text);
        tabNumber.setTypeface(customFontSelected);
        Typeface customFontUnselected = ResourcesCompat.getFont(this, R.font.urbanist_medium);
        for (int i = 0; i < list.size(); i++) {
            if (i != position) {
                TextView tabNumberUnselected = tabLayout.getTabAt(i).getCustomView().findViewById(R.id.tab_text);
                tabNumberUnselected.setTypeface(customFontUnselected);
            }
        }
    }

    @Override
    public void clicked(int position) {
        viewPager.setCurrentItem(position);
    }

    @Override
    public void nextPage(int i) {
        if (i < list.size()) {
            viewPager.setCurrentItem(i);
        } else if (!isFinishing() && !Type.equals(PreferenceKeys.RESPONSE.getKey())) {
            showSubmitTestDialog(autoSubmitEnabled);
        }
    }

    @Override
    public void stopTimer() {
        countDownTimer.cancel();
    }

    @Override
    public void startTimer() {
        handleTimer(timeTaken);
    }

    @Override
    public void onViewSolutionClick() {
        for (TestSeriesQuestionDto testSeriesQuestionDto : list) {
            testSeriesQuestionDto.setHideViewSolution(true);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public int getSelected() {
        return viewPager.getCurrentItem();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    private void submitAsync(UserMockAnswerDto userMockAnswerDto) {
        testSubmitted = true;

        try {
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                    TEST_SERIES_ASYNC_v2, new JSONObject(gson.toJson(userMockAnswerDto)),
                    response -> {

                        removeOfflineTestData();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        //Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
                        //showAnimationView();
                        Map<String, String> params = new HashMap<>();
                        params.put("testSeriesId",testId);
                        params.put("testSeriesName",testSeriesName);
                        params.put(PreferenceKeys.TIME_TAKEN.getKey(),String.valueOf(userMockAnswerDto.getT()));
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.Test_series_attempt.getActionName(), params);
                        MyApplication.saveObject(getString(R.string.UserOngoingTestState), null);
                        setAnalysingDialog();
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
        } catch (JSONException e) {

        }
    }

    @Override
    public void performClickOnCaseStudy(String caseStudyGroup, String imageUrl,
                                        String caseStudyText, int[] location, int width, int height){
        List<String> group = MyApplication.getCaseStudyGroupList(testId);
        if(CollectionUtils.isEmpty(group))
            group=new ArrayList<>();
        String key = testId + caseStudyGroup;
        Log.d("LAFragmenet", "key check " + key);
        if(!group.contains(key)){
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
            handler = new Handler();
            List<String> finalGroup = group;
            runnable = () -> {
                Log.d("LAFragmenet", "case study open  ->  key check " + key);

                openCaseStudyDialog(imageUrl, caseStudyText, location, width, height);
                finalGroup.add(key);
                MyApplication.setCaseStudyGroupList(testId, finalGroup);
            };
            handler.postDelayed(runnable,1000);
        }
    }

    @Override
    public void openCaseStudyDialog(String imageUrl, String caseStudyText, int[] location, int width, int height) {

        if (revealAnimator != null && revealAnimator.isRunning()){
            revealAnimator.cancel();
        }
        if (caseStudyDialog != null && caseStudyDialog.isShowing()) {
            caseStudyDialog.dismiss();
        }
        if (imageUrl != null || caseStudyText != null) {
            caseStudyDialog = new ArivihanCustomDialog(this);
            caseStudyDialog.setContentView(R.layout.case_study_layout);
            caseStudyDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            EdgeToEdgeHelper.applyToDialog(caseStudyDialog);
            caseStudyDialog.setCancelable(false);
            View dialogView = caseStudyDialog.findViewById(R.id.case_study_layout);
            ImageView caseStudyImageView = caseStudyDialog.findViewById(R.id.case_study_image);
            ShimmerFrameLayout shimmerFrameLayout = caseStudyDialog.findViewById(R.id.shimmer_layout);

            TextView textView = caseStudyDialog.findViewById(R.id.case_study_text);
            textView.setMovementMethod(new ScrollingMovementMethod());
            if(imageUrl != null) {
                shimmerFrameLayout.startShimmer();
                caseStudyImageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageUrl).addListener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.d("ShimmerDataReload", "onLoadFailed");
                        shimmerFrameLayout.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("ShimmerDataReload", "onResourceReady");
                        shimmerFrameLayout.setVisibility(View.GONE);

                        return false;
                    }
                }).into(caseStudyImageView);
            }else {
                shimmerFrameLayout.setVisibility(View.GONE);  // Stop shimmer effect
                textView.setVisibility(View.VISIBLE);
                String text = caseStudyText.trim();
                text =  text + "\n\n\n\n";  // Adds two new lines before and after the text

                textView.setText(text);
            }
            caseStudyDialog.show();

            caseStudyImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    caseStudyImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int centerX = location[0] + (width / 2);
                    int centerY = location[1] + (height / 5);
                    int finalRadius = Math.max(dialogView.getWidth(), dialogView.getHeight());
                    revealAnimator = ViewAnimationUtils.createCircularReveal(dialogView, centerX, centerY, 0, finalRadius);
                    revealAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    revealAnimator.setDuration(800);
                    revealAnimator.start();

                    return true;
                }
            });

            RelativeLayout hide = caseStudyDialog.findViewById(R.id.hide_case_study_fab_1);
            hide.setVisibility(View.VISIBLE);
            hide.setOnClickListener(view -> {
                dismissDialogWithAnimation(location, width, height);
            });

            caseStudyDialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    hide.performClick();
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public void dismissDialogWithAnimation(int[] location, int width, int height) {
        if (caseStudyDialog == null) {
            return;
        }
        try {
            View dialogView = caseStudyDialog.findViewById(R.id.case_study_layout);
            int centerX = location[0] + (width / 2);
            int centerY = location[1] + (height / 5);
            float initialRadius = (float) Math.hypot(dialogView.getWidth(), dialogView.getHeight());
            Animator hideAnimator = ViewAnimationUtils.createCircularReveal(dialogView, centerX, centerY, initialRadius, 0);
            hideAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            hideAnimator.setDuration(500);
            hideAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (caseStudyDialog != null) {
                        caseStudyDialog.dismiss();
                        caseStudyDialog = null;
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            hideAnimator.start();
        } catch (Exception e) {
            caseStudyDialog.dismiss();
            Log.e("Error", "In Closing " + e.getMessage(), e);
        }
    }

    private void submitNew(UserMockAnswerDto userMockAnswerDto) {
        testSubmitted = true;
//        Log.d(TAG, "submitNew: "+ gson.toJson(userMockAnswerDto));
        try {
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(
                    Request.Method.POST,
                    TEST_SERIES_v2,
                    new JSONObject(gson.toJson(userMockAnswerDto)),
                    response -> {
                        removeOfflineTestData();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        if (userMockAnswerDto.getAsatId() != null || isAsat) {
                            Intent intent = new Intent(this, ASATActivity.class);
                            intent.putExtra(ASATActivity.ASAT_OPEN_RESULT, true);
                            intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, RedirectionSource.ASAT);
                            destroyHowTo();
                            startActivity(intent);

                            Map<String, String> params = new HashMap<>();
                            params.put("testSeriesId", testId);
                            params.put("testSeriesName", testSeriesName);
                            params.put(PreferenceKeys.TIME_TAKEN.getKey(), String.valueOf(userMockAnswerDto.getT()));
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_test_submit.getActionName(), params);
                            MyApplication.saveObject(getString(R.string.UserOngoingTestState), null);

                            finish();
                            return;
                        }

                        if (getIntent().getBooleanExtra("timeRestrictedEntry", false)) {
                            Intent intent = new Intent(this, TestResultActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        Intent intent = new Intent(this, MockTestAnalysisActivity.class);
                        intent.putExtra("testSeriesId", testId);
                        intent.putExtra("testSeriesListId", id);
                        intent.putExtra("afterAttempt", true);
                        intent.putExtra("testSeriesName", testSeriesName);
                        intent.putExtra("fromGlobalSearch", getIntent().getBooleanExtra("fromGlobalSearch", false));
                        intent.putExtra("fromHalfYearly", getIntent().getBooleanExtra("fromHalfYearly", false));
                        destroyHowTo();
                        startActivity(intent);

                        Map<String, String> params = new HashMap<>();
                        params.put("testSeriesId", testId);
                        params.put("testSeriesName", testSeriesName);
                        params.put(PreferenceKeys.TIME_TAKEN.getKey(), String.valueOf(userMockAnswerDto.getT()));
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.Test_series_submit.getActionName(), params);

                        MyApplication.saveObject(getString(R.string.UserOngoingTestState), null);
                        finish();
                    },
                    new VolleyGlobalErrorListener(this) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.d("posterror", "postError: "+error);
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            testSubmitted = false;
                            if (error instanceof NetworkError) {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.slow_internet_error),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.something_went_wrong),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            myJsonObjectRequest2.setRetryPolicy(new DefaultRetryPolicy(
                    15000, // 15 sec timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

        } catch (JSONException ignored) {}
    }


    private void destroyHowTo(){
        if(howToHandler!=null)
            howToHandler.removeCallbacksAndMessages(null);
        how_to_video_view.setVisibility(View.GONE);
        if(howToVideoView!=null)
            howToVideoView.remove();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        isAppInForeground = false;
    }

    // onBackPressed logic moved to OnBackPressedCallback in onCreate

    @Override
    protected void onDestroy() {
        try {
            countDownTimer.cancel();
        } catch (Exception ignored) {}
        if (Type != null && Type.equals("ATTEMPT")) {
            if (!testSubmitted)
                submitTest();
        }

        if (no_internet_connection != null) {
            no_internet_connection.dismiss();
        }
        dismissAllDialogs();
        super.onDestroy();
    }

    private void dismissAllDialogs() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (analysingDialog != null && analysingDialog.isShowing()) {
            analysingDialog.dismiss();
            analysingDialog = null;
        }
        dismissAttemptWarningDialog();
    }

    @Override
    public void setSelected(int i) {
    }


    @Override
    public TestSeriesQuestionDto getDto(int fragNumber) {
        return list.get(fragNumber);
    }

    @Override
    public void submitAnswer() {

    }

    public void moveToNextPage() {
        if ((viewPager.getCurrentItem()) + 1 != list.size()) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    }

    private void setAnalysingDialog() {
        if(!isFinishing() && !isDestroyed()) {
            try {
                analysingDialog = new Dialog(this);
                analysingDialog.setContentView(R.layout.mock_test_analysing_dialog);
                analysingDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                analysingDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
                analysingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                analysingDialog.setCancelable(false);
                analysingDialog.findViewById(R.id.close_btn).setOnClickListener(v -> {
                    if (analysingDialog != null && analysingDialog.isShowing()) {
                        analysingDialog.dismiss();
                        analysingDialog = null;
                    }
                    finish();
                });
                analysingDialog.show();
                new Handler().postDelayed(() -> {
                    if (analysingDialog != null && analysingDialog.isShowing()) {
                        analysingDialog.dismiss();
                        analysingDialog = null;
                    }
                    finish();
                }, 3000);
            } catch (Exception e) {
                Log.d("setAnalysingDialog: ", e.getMessage());
            }
        }
    }

    private void showAttemptWarningDialog(DialogInterface.OnDismissListener listener) {
        try {
            dismissAttemptWarningDialog();

            attemptWarningDialog = new Dialog(this);
            attemptWarningDialog.setContentView(R.layout.dialog_test_series_context_warning);
            attemptWarningDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            attemptWarningDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            attemptWarningDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            attemptWarningDialog.setCancelable(false);

            String firebaseJsonStr = MyApplication.getFirebaseRemoteConfig()
                    .getString("testseries_warning_dialog_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

            ImageView iv_cancel = attemptWarningDialog.findViewById(R.id.iv_cancel);
            TextView tv_title = attemptWarningDialog.findViewById(R.id.tv_title);
            TextView tv_subtitle = attemptWarningDialog.findViewById(R.id.tv_subtitle);
            LottieAnimationView lottie_alert = attemptWarningDialog.findViewById(R.id.lottie_alert);

            Gson gson = new Gson();
            TestSeriesWarningConfig testSeriesWarningConfig = gson.fromJson(firebaseJsonStr, TestSeriesWarningConfig.class);

            if(StringUtils.isNotEmpty(testSeriesWarningConfig.getTitle())) {
                tv_title.setText(StyledTextBuilder.fromJson(this, testSeriesWarningConfig.getTitle()));
            }
            if(StringUtils.isNotEmpty(testSeriesWarningConfig.getSubTitle())) {
                tv_subtitle.setText(StyledTextBuilder.fromJson(this, testSeriesWarningConfig.getSubTitle()));
            }

            lottie_alert.setProgress(0f);
            lottie_alert.playAnimation();

            iv_cancel.setOnClickListener(v->{
                dismissAttemptWarningDialog();
            });

            attemptWarningDialog.setOnDismissListener(l->{
                lottie_alert.cancelAnimation();
                lottie_alert.clearAnimation();
                if(listener!=null) {
                    listener.onDismiss(attemptWarningDialog);
                }
            });

            Handler dismissHandler = new Handler(Looper.getMainLooper());

            Runnable dismissRunnable = this::dismissAttemptWarningDialog;

            dismissHandler.postDelayed(dismissRunnable, 5000);

            attemptWarningDialog.show();
        } catch (Exception ex){
            ex.printStackTrace();

        }
    }

    private void dismissAttemptWarningDialog() {
        if(attemptWarningDialog!=null && attemptWarningDialog.isShowing()) {
            attemptWarningDialog.dismiss();
            attemptWarningDialog = null;
        }
    }

}
