package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_QUESTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_RANK_LIST;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.MockTestChapterListAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SubjectScoreAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.LeaderboardMockTestModel;
import arivihan.technologies.doubtbuzzter2.model.MockTestChapterListModel;
import arivihan.technologies.doubtbuzzter2.model.NameAndScoreDto;
import arivihan.technologies.doubtbuzzter2.model.RankListDto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesSetDto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesTypeDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MockTestDetailActivity extends LocaleActivity implements MockTestChapterListAdapter.MockListener{

    private List<MockTestChapterListModel> model = new ArrayList<>();
    LinearLayout back;
    RecyclerView recyclerView;
    String TestMainId = "";
    TextView mockTextTitle;
    List<TestSeriesTypeDto> testSeriesTypeDtos = new ArrayList<>();
    String testId = "";
    ProgressBar progressBar;
    Boolean dataLoaded = false;
    TextView Info;
    RelativeLayout ParentLayout;
    String syllabusInfo = "";
    CardView cardView;
    TextView yourRank;
    RecyclerView subjectScoreRecycler;
    boolean lock;
    Dialog dialog2;
    LinearLayout subjectScoreLayout;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_test_detail);

        testId = getIntent().getStringExtra("testId");

        Log.d("TestSeriesDebug", "testId = " + testId);

        progressBar = findViewById(R.id.progress);
        Info = findViewById(R.id.Info);

        recyclerView = findViewById(R.id.recyclerview);
        back = findViewById(R.id.back_arrow);
        mockTextTitle = findViewById(R.id.MockTextName);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        cardView = findViewById(R.id.rank_card);
        yourRank = findViewById(R.id.rank);
        subjectScoreRecycler = findViewById(R.id.subject_score_recycler);
        mockTextTitle.setText(getIntent().getStringExtra("Title"));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        subjectScoreLayout = findViewById(R.id.subject_score_layout);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });


        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!dataLoaded) {
                    dataLoaded = true;
                    getTestSeriesSets();
                }
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });


        if (CommonMethods.isOnline(this)) {
            if (!dataLoaded) {
                dataLoaded = true;
                getTestSeriesSets();
            }
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

        if (getIntent().getBooleanExtra("isEvaluationInProgress", false)) {
            showAnalysingAnswerDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog2 != null) {
            dialog2.dismiss();
        }
    }

    public void onLockClick() {
        dialog2 = new Dialog(MockTestDetailActivity.this);
        dialog2.setContentView(R.layout.custom_dialog_lock_box);
        dialog2.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.setCancelable(false);
        TextView msg = dialog2.findViewById(R.id.subs);
        msg.setText(R.string.viewSubscription);


        ImageView close = dialog2.findViewById(R.id.cancel);
        TextView title = dialog2.findViewById(R.id.header);
        TextView te = dialog2.findViewById(R.id.subs);
        MaterialCardView card = dialog2.findViewById(R.id.subscriptionLayout);
        te.setText(R.string.view_plans);

        title.setText(getString(R.string.OOpsThisTestIsLocked));
        close.setOnClickListener(v -> dialog2.dismiss());

        card.setOnClickListener(v -> {
            dialog2.dismiss();
            dialog2.cancel();
//                Intent intent = new Intent(MockTestDetailActivity.this, CampaignSubscriptionActivity.class);
////             intent.putExtra("plan_level", "COURSE");
//                startActivity(intent);
            SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.MOCK_TEST_JOIN);
        });


        dialog2.show();
    }


    private void showAnalysingAnswerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_after_subjective_submit_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setDimAmount(0.8f);
        dialog.setCancelable(false);

        TextView textOne = dialog.findViewById(R.id.popup_submit_text_one);
        TextView textTwo = dialog.findViewById(R.id.popup_submit_text_two);

        textOne.setText("Your submitted Mock Test!");
        textTwo.setText("Just relax for a moment.");

        dialog.findViewById(R.id.close_card).setOnClickListener(vw -> {
            finish();
        });

        dialog.show();
    }


    private void getTestSeriesSets() {

        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(),UserId);
        map.put("id", testId);
        map.put("combined", true);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(TEST_SERIES_QUESTION, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<TestSeriesSetDto> token = new TypeToken<TestSeriesSetDto>() {};
                    TestSeriesSetDto dto = gson.fromJson(response.toString(), token.getType());
                    testSeriesTypeDtos.addAll(dto.getTestSeriesTypes());
                    Info.setText(dto.getInfo());
                    TestMainId = dto.getTestSeriesId();
                    syllabusInfo = dto.getTestSeriesName();
                    lock = dto.isLock();
                    loadCombinedData();
                    progressBar.setVisibility(View.GONE);


                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());

            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void loadCombinedData() {
        int totalQuestions = 0;
        boolean isAttempted = false;
        int totalScore = 0;
        long totalTime = 0;
        long myScore = 0;
        List<NameAndScoreDto> nameAndScoreDtoList = new ArrayList<>();
        for (TestSeriesTypeDto t : testSeriesTypeDtos) {
            totalQuestions += t.getTotalQuestion();
            if (!isAttempted) {
                isAttempted = t.isAttempted();
            }
            totalScore += t.getTotalScore();
            totalTime += t.getTotalTimeToAttempt();
            myScore += t.getUserScore();

            nameAndScoreDtoList.add(NameAndScoreDto.builder().score(Math.toIntExact(t.getUserScore()))
                    .subjectName(t.getSubject()).build());
        }

        MockTestChapterListModel m = new MockTestChapterListModel();
        m.setQuestions(String.valueOf(totalQuestions));
        m.setChapter(syllabusInfo);
        m.setAttempted(isAttempted);
        m.setTotalScore(totalScore);
        m.setTime(totalTime);
        m.setYourScore(myScore);
        m.setTestId(TestMainId);
        m.setNameAndScoreDtoList(nameAndScoreDtoList);
        model.add(m);

        if (isAttempted) {
            subjectScoreLayout.setVisibility(View.VISIBLE);
            showScoreOfSubjects(nameAndScoreDtoList);
            showRankOfUser(testId);
            cardView.setVisibility(View.VISIBLE);
        } else {
            subjectScoreLayout.setVisibility(View.GONE);
        }

        MockTestChapterListAdapter Adapter = new MockTestChapterListAdapter(testId,this, model, true, lock, this);
        recyclerView.setAdapter(Adapter);
    }

    private void showScoreOfSubjects(List<NameAndScoreDto> nameAndScoreDtoList) {
        SubjectScoreAdapter subjectScoreAdapter = new SubjectScoreAdapter(nameAndScoreDtoList);
        subjectScoreRecycler.setLayoutManager(new LinearLayoutManager(this));
        subjectScoreRecycler.setAdapter(subjectScoreAdapter);
        cardView.setVisibility(View.VISIBLE);
    }

    private void showRankOfUser(String testId) {

        Map<String, Object> map = new HashMap<>();
        map.put("testId", TestMainId);
        Gson gson = new Gson();

        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(USER_RANK_LIST, map), null,
                response -> {
                    TypeToken<RankListDto> token = new TypeToken<RankListDto>() {
                    };
                    RankListDto rankListDto = gson.fromJson(response.toString(), token.getType());

                    for (LeaderboardMockTestModel leaderboardMockTestModel : rankListDto.getRankDtoList()) {
                        if (leaderboardMockTestModel.isSelf()) {
                            yourRank.setVisibility(View.VISIBLE);
                            yourRank.setText(MessageFormat.format("{0} : {1}",
                                    getString(R.string.your_rank), leaderboardMockTestModel.getRank()));
                            break;
                        }
                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(cardView, error.getMessage());

            }
        });

        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }


    // Issue----- Question is showing '0'....
    private void loadData() {
        for (TestSeriesTypeDto t : testSeriesTypeDtos) {
            MockTestChapterListModel m = new MockTestChapterListModel();
            m.setQuestions(String.valueOf(t.getTotalQuestion()));
            m.setChapter(t.getSubject());
            m.setAttempted(t.isAttempted());
            m.setTotalScore(t.getTotalScore());
            m.setTime(t.getTotalTimeToAttempt());
            m.setYourScore(t.getUserScore());
            m.setTestId(TestMainId);
            model.add(m);
        }
        MockTestChapterListAdapter Adapter = new MockTestChapterListAdapter(testId,this, model,
                false, lock, this);
        recyclerView.setAdapter(Adapter);
    }

}