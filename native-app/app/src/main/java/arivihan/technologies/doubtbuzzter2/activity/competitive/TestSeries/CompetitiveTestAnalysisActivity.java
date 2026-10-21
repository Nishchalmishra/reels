package arivihan.technologies.doubtbuzzter2.activity.competitive.TestSeries;


import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_BOARD_ANALYSIS;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_BOARD_CHAPETR_ANALYSIS;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_JEE_ANALYSIS;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_JEE_CHAPETR_ANALYSIS;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_NEET_ANALYSIS;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_NEET_CHAPETR_ANALYSIS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_CATEGORY_WISE_USER_SCORE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_CATEGORY_WISE_USER_SECTIONAL_SUMMARY;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_CATEGORY_WISE_USER_COMPARISON_SCORE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_CATEGORY_WISE_USER_TOPIC_SUMMARY;


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mancj.slimchart.SlimChart;


import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.activity.MockQuestionFInal;
import arivihan.technologies.doubtbuzzter2.activity.TestSeriesSeeAllActivity;
import arivihan.technologies.doubtbuzzter2.adapter.DotsAxisAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.MockAnalysisSubjectCardAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.MockAnalysisSubjectTagAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.MockAnalysisSummaryAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.MockAttemptSpeedAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ScoreXAxisScaleAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.constants.TestSeriesSection;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE;
import arivihan.technologies.doubtbuzzter2.model.AttemptAnalysisData;
import arivihan.technologies.doubtbuzzter2.model.ChapterSummaryDetail;
import arivihan.technologies.doubtbuzzter2.model.SectionalSummary;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesSetDto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesTypeDto;
import arivihan.technologies.doubtbuzzter2.model.UserChapterSummary;
import arivihan.technologies.doubtbuzzter2.model.UserScore;
import arivihan.technologies.doubtbuzzter2.model.UserScoreComparison;
import arivihan.technologies.doubtbuzzter2.model.UserTopicSummary;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TestAnalysisConfig;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TestAnalysisConfigModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CompetitiveTestAnalysisActivity extends BaseActivity implements
        MockAnalysisSubjectTagAdapter.MockAnalysisSectionTabClick,
        MockAnalysisSummaryAdapter.ChapterSummarySeeAllListener {

    private static final String TAG = CompetitiveTestAnalysisActivity.class.getSimpleName();
    public static final String ANALYSIS_TYPE = "ANALYSIS_TYPE";
    public static enum AnalysisType {
        TESTSERIES,
        ASAT,
    };
    String categoryId;
    String testSeriesName;
    TextView message;
    TextView score;
    double userTestScore;
    TextView cutoff;
    TextView cutoffText;
    RecyclerView sectionSummarySubjectTagRV;
    MockAnalysisSubjectTagAdapter mockAnalysisSubjectTagAdapter;
    RecyclerView sectionSummarySubjectCardRV;
    MockAnalysisSubjectCardAdapter mockAnalysisSubjectCardAdapter;


    ShimmerFrameLayout analysisLoadingShimmer,shimmer_layout_analysis;

    MaterialCardView materialCardView;
    RecyclerView sectionAverageSpeedRV;
    MockAttemptSpeedAdapter mockAttemptSpeedAdapter;
    ScrollView scrollView;
    boolean isChartShown;
    boolean isGraphShown;

    RecyclerView chapterSummaryTabsRV;
    RecyclerView topicSummaryTabsRV;
    MockAnalysisSubjectTagAdapter mockChapterAnalysisTagAdapter;
    MockAnalysisSubjectTagAdapter mockTopicAnalysisTagAdapter;

    RecyclerView chapterSummaryRV;
    RecyclerView topicSummaryRV;
    MockAnalysisSummaryAdapter mockAnalysisSummaryAdapter;
    MockAnalysisSummaryAdapter mockTopicAnalysisSummaryAdapter;
    Gson gson;
    SlimChart slimChart;
    LinearLayout scoreComparisonGraphView;
    RecyclerView scoreComparisonTabs;
    RecyclerView xAxisScaleRV;
    RecyclerView xAxisDotRV;
    String sectionalSummaryType = "OVERALL";
    String chapterSummaryType = "OVERALL";
    String topicSummaryType = "OVERALL";
    String scoreSummaryType = "OVERALL";
    TextView sectionScore;
    ConstraintLayout mockHeader;
    LinearLayout sectionalSummaryLayout;
    LinearLayout chapterSummaryLayout;
    LinearLayout topicSummaryLayout;
    LinearLayout scoreComparisonLayout;
    LinearLayout graphView;
    ProgressBar yourProgress;
    ProgressBar topperProgress;
    ProgressBar averageProgress;
    UserScoreComparison scoreComparison;
    TextView talkToUs;
    TextView testSeriesTitle;
    LinearLayout back_arrow, talk_to_us_layout;
    MaterialCardView view_solution_card;
    boolean sectionTabLoaded;
    boolean chapterTabLoaded;
    boolean topicTabLoaded;
    boolean scoreTabLoaded;
    private ProgressDialog dialog;
    boolean afterAttempt;
    ArrayList<TestSeriesQuestionDto> testSeriesQuestionList;
    JSONObject limitMap;
    String subject;

    Map<String, UserScoreComparison> scoreComparisonMap = new HashMap<>();
    Map<String, UserChapterSummary> chapterSummaryMap = new HashMap<>();
    Map<String, UserTopicSummary> topiSummaryMap = new HashMap<>();
    Map<String, SectionalSummary> sectionalSummaryMap = new HashMap<>();
    TestSeriesSetDto set;
    String testSeriesListId = "";
    boolean fromGlobalSearch;
    boolean fromHalfYearly;
    private String analysisType;
    TestAnalysisConfig testAnalysisConfig;

    private HowToVideoView howToVideoView;
    private View how_to_video_view;
    private ImageView how_to_poster;
    private HOW_TO_TYPE howToType;
    private String posterUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_test);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(howToVideoView!=null && howToVideoView.videoView!=null
                        && howToVideoView.videoView.getVisibility()==View.VISIBLE && howToVideoView.expanded ) {
                    howToVideoView.resize();
                }else if (afterAttempt && !fromGlobalSearch && !fromHalfYearly) {
                    destroyHowTo();
                    CommonNavigator.navigateToTestSeriesListActivity(CompetitiveTestAnalysisActivity.this,
                            MyApplication.getStringData(PreferenceKeys.SELECTED_TEST_SERIES_ID.getKey()),
                            MyApplication.getStringData(PreferenceKeys.SELECTED_TEST_SERIES_TITLE.getKey()),
                            null, false, null, false, true, TAG);
                    finish();
                    MyApplication.saveData(PreferenceKeys.SELECTED_TEST_SERIES_ID.getKey(), null);
                    MyApplication.saveData(PreferenceKeys.SELECTED_TEST_SERIES_TITLE.getKey(), null);
                } else {
                    finish();
                }
            }
        });

        getDeeplinkData();
        how_to_video_view = findViewById(R.id.how_to_video_view);
        how_to_poster = findViewById(R.id.how_to_poster);
        scrollView = findViewById(R.id.scroll_view);
        message = findViewById(R.id.message);
        score = findViewById(R.id.user_score_value);
        sectionScore = findViewById(R.id.score_value_text);
        cutoff = findViewById(R.id.cut_off_value);
        cutoffText = findViewById(R.id.cut_off);
        slimChart = findViewById(R.id.slimChart);
        analysisLoadingShimmer = findViewById(R.id.analysis_loading_shimmer);
        shimmer_layout_analysis=findViewById(R.id.shimmer_layout_analysis);
        mockHeader = findViewById(R.id.mock_header);
        sectionalSummaryLayout = findViewById(R.id.sectional_summary_layout);
        chapterSummaryLayout = findViewById(R.id.chapter_wise_summary);
        topicSummaryLayout = findViewById(R.id.topic_wise_summary);
        scoreComparisonLayout = findViewById(R.id.score_comparison);
        graphView = findViewById(R.id.graph_view);
        scoreComparisonGraphView = findViewById(R.id.score_comparison_graph_view);
        yourProgress = scoreComparisonGraphView.findViewById(R.id.your_score_progress);
        topperProgress = scoreComparisonGraphView.findViewById(R.id.topper_score_progress);
        averageProgress = scoreComparisonGraphView.findViewById(R.id.average_score_progress);
        testSeriesTitle = findViewById(R.id.mock_test_title);

        talk_to_us_layout = findViewById(R.id.talk_to_us_layout);
        talkToUs = findViewById(R.id.talk_to_us);
        talkToUs.setOnClickListener(v -> {
            try {
                String url = "https://api.whatsapp.com/send?phone=" + MyApplication.getPhoneNumberWith91();
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.whatsapp");
                intent.setData(uri);
                destroyHowTo();
                startActivity(intent);

            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, getResources().getString(R.string.whatsapp_not_found), Toast.LENGTH_SHORT).show();
            }
        });

        dialog = new ProgressDialog(CompetitiveTestAnalysisActivity.this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        if (StringUtils.isNotEmpty(testSeriesName))
            testSeriesTitle.setText(testSeriesName);

        afterAttempt = getIntent().getBooleanExtra("afterAttempt", false);
        fromGlobalSearch = getIntent().getBooleanExtra("fromGlobalSearch", false);
        fromHalfYearly = getIntent().getBooleanExtra("fromHalfYearly", false);
        analysisType = getIntent().getStringExtra(ANALYSIS_TYPE);
        gson = new Gson();
        back_arrow = findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        view_solution_card = findViewById(R.id.view_solution_card);
        view_solution_card.setVisibility(View.GONE);

        try {
            String jsonString = MyApplication.getFirebaseRemoteConfig().getString("analytics_screen_config");
            TestAnalysisConfigModel testAnalysisConfigModel = new Gson().fromJson(jsonString, TestAnalysisConfigModel.class);

            testAnalysisConfig = testAnalysisConfigModel.getDefaultConfig(); // Default
            if(!StringUtils.isEmpty(analysisType)) {
                if(analysisType.equals(AnalysisType.ASAT.name())) {
                    testAnalysisConfig = testAnalysisConfigModel.getAsatConfig();
                }
            }

        } catch (Exception ignored){}
        shimmer_layout_analysis.startShimmer();
        shimmer_layout_analysis.setVisibility(View.VISIBLE);
        getUserScore();
        getTopicSummary(topicSummaryType);
        getUserScoreComparison("");
        getSectionalSummary(sectionalSummaryType);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                Rect scrollBounds = new Rect();
                scrollView.getHitRect(scrollBounds);

                if (slimChart.getLocalVisibleRect(scrollBounds) && !isChartShown) {
                    isChartShown = true;
                    slimChart.playStartAnimation();
                }

                if (graphView.getLocalVisibleRect(scrollBounds) && !isGraphShown) {
                    isGraphShown = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        yourProgress.setProgress(scoreComparison.getSeries().get(0).getData().get(0), true);
                        topperProgress.setProgress(scoreComparison.getSeries().get(0).getData().get(1), true);
                        averageProgress.setProgress(scoreComparison.getSeries().get(0).getData().get(2), true);
                    } else {
                        yourProgress.setProgress(scoreComparison.getSeries().get(0).getData().get(0));
                        topperProgress.setProgress(scoreComparison.getSeries().get(0).getData().get(1));
                        averageProgress.setProgress(scoreComparison.getSeries().get(0).getData().get(2));
                    }
                }

            });
        } else
            slimChart.playStartAnimation();

        getHowToVideoType(testSeriesListId);
        how_to_poster.setOnClickListener(view -> {
            logFirebaseEvent();
            howToVideoView = HowToVideoView.getInstance(CompetitiveTestAnalysisActivity.this);
            howToVideoView.setView(CompetitiveTestAnalysisActivity.this,how_to_video_view);
            howToVideoView.setType(howToType,true);
            howToVideoView.setButtonText(getString(R.string.Analyse_Now));
        });

    }

    private void logFirebaseEvent(){
        Map<String,String> params = new HashMap<>();
        params.put("action", "analysis_how_to_poster_click");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.analysis_how_to_poster_click.getActionName(), params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(howToVideoView!=null && howToVideoView.videoView!=null) {
            howToVideoView.resumePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(howToVideoView!=null && howToVideoView.videoView!=null)
            howToVideoView.pausePlayer();
    }

    private void getHowToVideoType(String testId) {
        Log.d("debugcount", "getHowToVideoType: "+MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()));
        try {
            if (testId == null || testId.isEmpty()) {
                return;
            }
            if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())) {
                if (testId.toLowerCase().contains("chapter"))
                    howToType = TEST_BOARD_CHAPETR_ANALYSIS;
                else
                    howToType = TEST_BOARD_ANALYSIS;
            } else {
                if (testId.toLowerCase().contains("chapter")) {
                    if(("1").equals(MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())))
                        howToType = TEST_JEE_CHAPETR_ANALYSIS;
                    else if(("3").equals(MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())))
                        howToType = TEST_NEET_CHAPETR_ANALYSIS;
                } else {
                    if(("1").equals(MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())))
                        howToType = TEST_JEE_ANALYSIS;
                    else if(("3").equals(MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())))
                        howToType = TEST_NEET_ANALYSIS;
                }
            }

        } catch (Exception e) {
            howToType = null;
            Log.e("VideoUrlError", "Error getting video URL", e);
        }

        if(howToType!=null) {
            howToVideoView = HowToVideoView.getInstance(this);
            howToVideoView.setView(this, how_to_video_view);
        }

        if(howToType != null &&  !MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos")){
            posterUrl = howToVideoView.getHowToPosterUrl("TEST_ANALYSIS");
            if(StringUtils.isNotEmpty(posterUrl)){
                how_to_poster.setVisibility(View.VISIBLE);
                Glide.with(this).load(posterUrl).into(how_to_poster);
            }
            int count = MyApplication.getIntData(howToType.name()+"_ANALYSIS_COUNT"+MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            Log.d("debugcount", "getHowToVideoType: "+count+"type"+howToType.name());
            if(count<0)
                count=0;
            if(count==0) {
                howToVideoView.setType(howToType, false);
                howToVideoView.setButtonText(getString(R.string.Analyse_Now));
                MyApplication.saveData(howToType.name()+"_ANALYSIS_COUNT"+MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()),1);
            }
        }
    }

    private void getDeeplinkData() {
        Uri deepLinkData = getIntent().getData();
        if (deepLinkData != null) {
            Log.i("DeeplinkMockTest", "DeeplinkData : :" + deepLinkData);
            categoryId = deepLinkData.getQueryParameter("testSeriesCategoryId");
            Log.i("DeeplinkMockTest", "testSeriesId" + categoryId);
            testSeriesName = deepLinkData.getQueryParameter("Name");
            Log.i("DeeplinkMockTest", "testSeriesName" + categoryId);

            if (StringUtils.isEmpty(categoryId)) {
                testSeriesListId = getIntent().getStringExtra("testSeriesListId");
                categoryId = getIntent().getStringExtra("testSeriesId");
                testSeriesName = getIntent().getStringExtra("testSeriesName");
            }
        } else {
            testSeriesListId = getIntent().getStringExtra("testSeriesListId");
            categoryId = getIntent().getStringExtra("testSeriesCategoryId");
            testSeriesName = getIntent().getStringExtra("testSeriesName");

            Log.d(TAG, "getDeeplinkData: "+categoryId+"testSeriesName"+testSeriesName);
        }
    }

    private void startReviewMode() {
        Intent intent = new Intent(this, MockQuestionFInal.class);
        intent.putExtra("testId", categoryId);
        intent.putExtra("combined", true);
        intent.putExtra("Id", getIntent().getStringExtra("Id"));

        String instruction = CommonMethods.convertToString(set.getInstructions());
        intent.putExtra("instruction", instruction);

        intent.putExtra("attempted", true);
        intent.putExtra("subject", subject);

        if (limitMap != null) {
            intent.putExtra("limit", limitMap.toString());
        }

        intent.putExtra(PreferenceKeys.RESPONSE.getKey(), PreferenceKeys.RESPONSE.getKey());
        MyApplication.saveData(PreferenceKeys.RESPONSE.getKey(), PreferenceKeys.RESPONSE.getKey());
        dialog.dismiss();
        destroyHowTo();
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        closeAllDialog();
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        super.onDestroy();
    }

    private void destroyHowTo(){
        how_to_video_view.setVisibility(View.GONE);
        if(howToVideoView!=null)
            howToVideoView.remove();
    }

    private void closeAllDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog.cancel();
        }
        dialog = null;
    }



    private void updateCombinedView(List<TestSeriesTypeDto> testSeriesTypes) {
        Map<String, Integer> map = new HashMap<>();
        for (TestSeriesTypeDto testSeriesType : testSeriesTypes) {
            subject = testSeriesType.getSubject();
            Map<String, Integer> limit = testSeriesType.getLimit();
            if (limit != null && !limit.isEmpty()) {
                for (String s : limit.keySet()) {
                    map.put(s, limit.get(s));
                }
            }

            ArrayList<TestSeriesQuestionDto> datas = (ArrayList<TestSeriesQuestionDto>) testSeriesType.getTestSeriesQuestionList();
            if (CollectionUtils.isEmpty(testSeriesQuestionList)) {
                testSeriesQuestionList = new ArrayList<>();
            }
            testSeriesQuestionList.clear();
            testSeriesQuestionList.addAll(datas);
        }

        try {
            if (map != null && !map.isEmpty()) {
                limitMap = new JSONObject(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setScoreComparison(UserScoreComparison userScoreComparison) {
        if (!scoreTabLoaded) {
            scoreTabLoaded = true;
            scoreComparisonTabs = findViewById(R.id.score_comparison_tabs);
            scoreComparisonTabs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            MockAnalysisSubjectTagAdapter mockAnalysisSubjectTagAdapter = new MockAnalysisSubjectTagAdapter(this,
                    userScoreComparison.getTabs(), TestSeriesSection.SCORE, this);
            scoreComparisonTabs.setAdapter(mockAnalysisSubjectTagAdapter);
        }
        if (CollectionUtils.isEmpty(userScoreComparison.getTabs())) {
            scoreComparisonTabs.setVisibility(View.GONE);
        }

        xAxisScaleRV = scoreComparisonGraphView.findViewById(R.id.x_axis_scales_recyler_view);
        xAxisScaleRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        xAxisScaleRV.setNestedScrollingEnabled(false);
        ArrayList<String> score = new ArrayList<>(userScoreComparison.getXasisRange());
        Collections.reverse(score);
        ScoreXAxisScaleAdapter scoreXAxisScaleAdapter = new ScoreXAxisScaleAdapter(this, score);
        xAxisScaleRV.setAdapter(scoreXAxisScaleAdapter);

        xAxisDotRV = scoreComparisonGraphView.findViewById(R.id.x_axis_dots_recycler_view);
        xAxisDotRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        xAxisDotRV.setNestedScrollingEnabled(false);
        DotsAxisAdapter dotsAxisAdapter = new DotsAxisAdapter(this, userScoreComparison.getXasisRange());
        xAxisDotRV.setAdapter(dotsAxisAdapter);

        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);

        if (graphView.getLocalVisibleRect(scrollBounds) && !isGraphShown) {
            isGraphShown = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                yourProgress.setProgress(userScoreComparison.getSeries().get(0).getData().get(0), true);
                topperProgress.setProgress(userScoreComparison.getSeries().get(0).getData().get(1), true);
                averageProgress.setProgress(userScoreComparison.getSeries().get(0).getData().get(2), true);
            } else {
                yourProgress.setProgress(userScoreComparison.getSeries().get(0).getData().get(0));
                topperProgress.setProgress(userScoreComparison.getSeries().get(0).getData().get(1));
                averageProgress.setProgress(userScoreComparison.getSeries().get(0).getData().get(2));
            }
        }

    }


    private void setSlimChart(SectionalSummary sectionalSummary) {

        int p1 = sectionalSummary.getAttemptList().get(0).getPercentage();
        int p2 = sectionalSummary.getAttemptList().get(1).getPercentage();
        int p3 = sectionalSummary.getAttemptList().get(2).getPercentage();

        int[] colors = new int[3];
        colors[0] = getResources().getColor(R.color.mock_light_grey);
        colors[1] = Math.max(p2, p1) == p1 ? getResources().getColor(R.color.mock_green) :
                getResources().getColor(R.color.mock_red);
        colors[2] = Math.max(p2, p1) == p2 ? getResources().getColor(R.color.mock_green) :
                getResources().getColor(R.color.mock_red);

        slimChart.setColors(colors);

        //Create array for your stats
        final float[] stats = new float[3];
        stats[0] = 100;
        stats[1] = Math.max(p2, p1) + Math.min(p2, p1);
        stats[2] = Math.min(p2, p1);
        slimChart.setStats(stats);

//        slimChart.setColors(colors);
//        slimChart.setStats(stats);
        slimChart.setRoundEdges(true);
        slimChart.setStartAnimationDuration(1000);
        sectionScore.setText(CommonMethods.cleanDecimalNumber(sectionalSummary.getUserScore())
                + "/" + sectionalSummary.getTotalScore());
        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        if (slimChart.getLocalVisibleRect(scrollBounds) && !isChartShown) {
            isChartShown = true;
            slimChart.playStartAnimation();
        }
    }

    private void getUserScore() {
        Map<String, Object> map = new HashMap<>();
        map.put("categoryId", categoryId);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(TEST_SERIES_CATEGORY_WISE_USER_SCORE, map), null,
                response -> {
                    try {
                        TypeToken<UserScore> token = new TypeToken<UserScore>() {
                        };
                        UserScore userScore = gson.fromJson(response.toString(), token.getType());
                        message.setText(userScore.getMessage());
                        score.setText(CommonMethods.cleanDecimalNumber(userScore.getPrecisionUserScore()) + "/" + userScore.getTotalScore());
                        userTestScore=userScore.getPrecisionUserScore();
                        if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())) {
                            if (StringUtils.isNotEmpty(userScore.getUserRank()) && StringUtils.isNotEmpty(userScore.getTotalUser())) {
                                cutoff.setText(userScore.getUserRank() + "/" + userScore.getTotalUser());
                            } else {
                                cutoff.setVisibility(View.GONE);
                                cutoffText.setVisibility(View.GONE);
                            }
                        }else{
                            cutoff.setVisibility(View.GONE);
                            cutoffText.setVisibility(View.GONE);
                        }
//                        if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())) {
//                            cutoff.setVisibility(View.GONE);
//                            cutoffText.setVisibility(View.GONE);
//                        } else {
//                            if (MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()).equals("1"))
//
//                            else if (MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()).equals("3"))
//                                cutoff.setText("130-142");
//                        }

                        mockHeader.setVisibility(View.VISIBLE);
                        analysisLoadingShimmer.setVisibility(View.GONE);

                    } catch (Exception e) {
                        Log.e("getUserScore: ", e.toString());
                    }

                },
                error -> {
//                    Log.e( "getUserScore: ",error.getMessage() );
                });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }
    private void getSectionalSummary(String type) {
        if(dialog.isShowing()){
            dialog.dismiss();
        }
        dialog.show();
        Map<String, Object> map = new HashMap<>();
        map.put("categoryId", categoryId);
        if (StringUtils.isNotEmpty(type)) {
            map.put("type", type);
        }
        if (StringUtils.isNotEmpty(testSeriesListId)) {
            map.put("testSeriesListId", testSeriesListId);
        }
        Log.i("USERDETAILS", "UD::" + map.toString());
        Log.i("USERDETAILS", "UD::" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(TEST_SERIES_CATEGORY_WISE_USER_SECTIONAL_SUMMARY, map), null,
                response -> {
                    try {
                        if(dialog!=null)
                            dialog.dismiss();

                        TypeToken<SectionalSummary> token = new TypeToken<SectionalSummary>() {
                        };
                        SectionalSummary sectionalSummary = gson.fromJson(response.toString(), token.getType());
                        Log.d(TAG, "getSectionalSummary: "+sectionalSummary);
                        shimmer_layout_analysis.stopShimmer();
                        shimmer_layout_analysis.setVisibility(View.GONE);
                        sectionalSummaryMap.put(type, sectionalSummary);
                        setSectionalSummary(sectionalSummary);
                        isChartShown = false;
                        setSlimChart(sectionalSummary);
                        sectionalSummaryLayout.setVisibility(View.VISIBLE);
                        analysisLoadingShimmer.setVisibility(View.GONE);

                    } catch (Exception e) {
                        if(dialog!=null)
                            dialog.dismiss();
                        shimmer_layout_analysis.stopShimmer();
                        shimmer_layout_analysis.setVisibility(View.GONE);
                        Log.e("getUserScore: ", e.toString());
                    }

                },
                error -> {
                    shimmer_layout_analysis.stopShimmer();
                    shimmer_layout_analysis.setVisibility(View.GONE);
                    Log.e("getUserScore: ", error.toString());
                });

        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }
    private void getTopicSummary(String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("categoryId", categoryId);
        if (StringUtils.isNotEmpty(type)) {
            map.put("type", type);
        }
        if (StringUtils.isNotEmpty(testSeriesListId)) {
            map.put("testSeriesListId", testSeriesListId);
        }
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(TEST_SERIES_CATEGORY_WISE_USER_TOPIC_SUMMARY, map), null,
                response -> {
                    try {
                        TypeToken<UserTopicSummary> token = new TypeToken<UserTopicSummary>() {
                        };
                        UserTopicSummary userTopicSummary = gson.fromJson(response.toString(), token.getType());

                        Log.i("CHAPTERSUM", userTopicSummary.toString());

                        if (!CollectionUtils.isEmpty(userTopicSummary.getList())) {
                            topiSummaryMap.put(type, userTopicSummary);
                            for(ChapterSummaryDetail detail:userTopicSummary.getList()) {
                                if(!CollectionUtils.isEmpty(detail.getDataList())) {
                                    setTopicSummary(userTopicSummary, type);
                                    analysisLoadingShimmer.setVisibility(View.GONE);
                                    topicSummaryLayout.setVisibility(View.VISIBLE);
                                    break;
                                }
                            }
                        } else {
                            topicSummaryLayout.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        Log.e("getUserScore: ", e.toString());
                    }

                },
                error -> {
                    dialog.dismiss();

                });

        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }


    private void getUserScoreComparison(String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("categoryId", categoryId);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(TEST_SERIES_CATEGORY_WISE_USER_COMPARISON_SCORE, map), null,
                response -> {
                    try {
                        TypeToken<UserScoreComparison> token = new TypeToken<UserScoreComparison>() {
                        };
                        UserScoreComparison userScoreComparison = gson.fromJson(response.toString(), token.getType());
                        scoreComparisonMap.put(type, userScoreComparison);
                        isGraphShown = false;
                        scoreComparison = userScoreComparison;
                        setScoreComparison(userScoreComparison);
                        scoreComparisonLayout.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e("getUserScore: ", e.toString());
                    }
                    analysisLoadingShimmer.setVisibility(View.GONE);
                },
                error -> {
                    analysisLoadingShimmer.setVisibility(View.GONE);
                    dialog.dismiss();

                    //Log.e( "getUserScore: ",error.getMessage().toString());
                });

        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);

    }



    private void setChapterSummary(UserChapterSummary userChapterSummary, String type) {
        if (!chapterTabLoaded) {
            chapterTabLoaded = true;
            chapterSummaryTabsRV = findViewById(R.id.chapter_summary_tabs_rv);
            chapterSummaryTabsRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            mockChapterAnalysisTagAdapter = new MockAnalysisSubjectTagAdapter(this, userChapterSummary.getTabs(), TestSeriesSection.CHAPTER, this);
            chapterSummaryTabsRV.setAdapter(mockChapterAnalysisTagAdapter);
            if (CollectionUtils.isEmpty(userChapterSummary.getTabs())) {
                chapterSummaryTabsRV.setVisibility(View.GONE);
            }
        }

        chapterSummaryRV = findViewById(R.id.chapter_summary_rv);
        chapterSummaryRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        chapterSummaryRV.setNestedScrollingEnabled(false);
        mockAnalysisSummaryAdapter = new MockAnalysisSummaryAdapter(this, userChapterSummary.getList(), this, type);
        mockAnalysisSummaryAdapter.setSummaryType("CHAPTER");
        chapterSummaryRV.setAdapter(mockAnalysisSummaryAdapter);
    }

    private void setTopicSummary(UserTopicSummary userTopicSummary, String type) {
        if (!topicTabLoaded) {
            topicTabLoaded = true;
            topicSummaryTabsRV = findViewById(R.id.topic_summary_tabs_rv);
            topicSummaryTabsRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            mockTopicAnalysisTagAdapter = new MockAnalysisSubjectTagAdapter(this, userTopicSummary.getTabs(), TestSeriesSection.TOPIC, this);
            topicSummaryTabsRV.setAdapter(mockTopicAnalysisTagAdapter);
            if (CollectionUtils.isEmpty(userTopicSummary.getTabs())) {
                topicSummaryTabsRV.setVisibility(View.GONE);
            }
        }

        topicSummaryRV = findViewById(R.id.topic_summary_rv);
        topicSummaryRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        topicSummaryRV.setNestedScrollingEnabled(false);
        mockTopicAnalysisSummaryAdapter = new MockAnalysisSummaryAdapter(this, userTopicSummary.getList(), this, type);
        mockTopicAnalysisSummaryAdapter.setSummaryType("TOPIC");
        topicSummaryRV.setAdapter(mockTopicAnalysisSummaryAdapter);
    }

    private void setSectionalSummary(SectionalSummary sectionalSummary) {
        if (!sectionTabLoaded) {
            sectionTabLoaded = true;
            sectionSummarySubjectTagRV = findViewById(R.id.section_subject_tag_rv);
            sectionSummarySubjectTagRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            mockAnalysisSubjectTagAdapter = new MockAnalysisSubjectTagAdapter(this, sectionalSummary.getTabs(), TestSeriesSection.SECTION, this);
            sectionSummarySubjectTagRV.setAdapter(mockAnalysisSubjectTagAdapter);
        }
        if (CollectionUtils.isEmpty(sectionalSummary.getTabs())) {
            sectionSummarySubjectTagRV.setVisibility(View.GONE);
        }

        sectionSummarySubjectCardRV = findViewById(R.id.section_subject_card_rv);
        sectionSummarySubjectCardRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        sectionSummarySubjectCardRV.setNestedScrollingEnabled(false);
        mockAnalysisSubjectCardAdapter = new MockAnalysisSubjectCardAdapter(this, sectionalSummary.getSectionList());
        sectionSummarySubjectCardRV.setAdapter(mockAnalysisSubjectCardAdapter);

        if (sectionalSummary.getAverageSpeeds() != null && !sectionalSummary.getAverageSpeeds().isEmpty()) {
            materialCardView = findViewById(R.id.average_speed_card);
            materialCardView.setVisibility(View.VISIBLE);
            sectionAverageSpeedRV = findViewById(R.id.section_average_speed_rv);
            sectionAverageSpeedRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            mockAttemptSpeedAdapter = new MockAttemptSpeedAdapter(this, sectionalSummary.getAverageSpeeds());
            sectionAverageSpeedRV.setAdapter(mockAttemptSpeedAdapter);
        }

        AttemptAnalysisData attemptAnalysisData = sectionalSummary.getAttemptList().get(0);
        AttemptAnalysisData attemptAnalysisData1 = sectionalSummary.getAttemptList().get(1);
        AttemptAnalysisData attemptAnalysisData2 = sectionalSummary.getAttemptList().get(2);

        View view1 = findViewById(R.id.correct_attempt);
        TextView t1 = view1.findViewById(R.id.attempt_status);
        t1.setText(attemptAnalysisData.getTitle());
        TextView s1 = view1.findViewById(R.id.attempt_detail);
        s1.setText(attemptAnalysisData.getSubtitle());
        CardView cardView = view1.findViewById(R.id.color_tag);
        cardView.setCardBackgroundColor(getResources().getColor(R.color.mock_green));

        View view2 = findViewById(R.id.incorrect_attempt);
        TextView t2 = view2.findViewById(R.id.attempt_status);
        t2.setText(attemptAnalysisData1.getTitle());
        TextView s2 = view2.findViewById(R.id.attempt_detail);
        s2.setText(attemptAnalysisData1.getSubtitle());
        CardView cardView2 = view2.findViewById(R.id.color_tag);
        cardView2.setCardBackgroundColor(getResources().getColor(R.color.mock_red));

        View view3 = findViewById(R.id.not_attempt);
        TextView t3 = view3.findViewById(R.id.attempt_status);
        t3.setText(attemptAnalysisData2.getTitle());
        TextView s3 = view3.findViewById(R.id.attempt_detail);
        s3.setText(attemptAnalysisData2.getSubtitle());
        CardView cardView3 = view3.findViewById(R.id.color_tag);
        cardView3.setCardBackgroundColor(getResources().getColor(R.color.mock_light_grey));
    }

    @Override
    public void sectionClicked(String type, TestSeriesSection section) {
        Log.d(TAG, "sectionClicked: "+type+"section"+section);

        switch (section) {
            case SCORE:
                if (scoreComparisonMap.containsKey(type)) {
                    isGraphShown = false;
                    setScoreComparison(scoreComparisonMap.get(type));
                } else {
                    dialog.show();
                    getUserScoreComparison(type);
                }
                break;
            case CHAPTER:
                if (chapterSummaryMap.containsKey(type)) {
                    setChapterSummary(chapterSummaryMap.get(type), type);
                } else {
                    dialog.show();
                }
                break;
            case TOPIC:
                if (topiSummaryMap.containsKey(type)) {
                    setTopicSummary(topiSummaryMap.get(type), type);
                } else {
                    dialog.show();
                    getTopicSummary(type);
                }
                break;
            case SECTION:
                if (sectionalSummaryMap.containsKey(type)) {
                    isChartShown = false;
                    setSectionalSummary(sectionalSummaryMap.get(type));
                    setSlimChart(sectionalSummaryMap.get(type));
                } else {
                    dialog.show();
                    getSectionalSummary(type);
                }
                break;
        }
    }

    @Override
    public void seeAllClick(String type, boolean strength,String summaryType) {
        Intent intent = new Intent(this, TestSeriesSeeAllActivity.class);
        Log.d(TAG, "seeAllClick: ");
        intent.putExtra("categoryId", categoryId);
        intent.putExtra("type", type);
        intent.putExtra("strength", strength);
        intent.putExtra("summaryType", summaryType);
//        intent.putExtra("competitive", true);
        destroyHowTo();
        startActivity(intent);
    }
}