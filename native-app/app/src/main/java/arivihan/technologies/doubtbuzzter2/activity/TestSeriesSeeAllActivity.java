package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CONFIGURATION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SUBSCRIBED_SUBJECT;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.SortSubjectNameAnalyticAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.TestAnalyticsAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.analytics.TestAnalysisSubjectTabAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.analytics.TestSeriesAnalyticsAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.model.Configuration;
import arivihan.technologies.doubtbuzzter2.model.ConfigurationResult;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.TestAnalyticsData;
import arivihan.technologies.doubtbuzzter2.model.TestChapterAnalysisDetails;
import arivihan.technologies.doubtbuzzter2.model.UserDetailTestAnalyticsModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class TestSeriesSeeAllActivity extends BaseActivity implements SortSubjectNameAnalyticAdapter.OnSubjectSortClick, TestAnalysisSubjectTabAdapter.SubjectTabClick {

    private static final String TAG = TestSeriesSeeAllActivity.class.getSimpleName();
    LinearLayout back;
    RelativeLayout selectSubject;
    ArrayList<String> Usersubject = new ArrayList<>();
    LinearLayout linearLayout;
    ArrayList<String> subjectId = new ArrayList<>();
    RelativeLayout ParentLayout;
    TextView selectSubjectText;
    ProgressBar progressBar;
    MaterialCardView cv_section_1, cv_section_2;
    RecyclerView rv_section_1, rv_section_2, rv_subjects;
    TextView tv_section_2, subject_name;
    SortSubjectNameAnalyticAdapter DialogSubjectName;
    Gson gson = new Gson();
    ImageView filter;
    String selectedSubject = "Overall";
    String textMessageBody = "";
    String testSeriesType;
    Dialog dialog;
    boolean strength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_series_analysis);

        back = findViewById(R.id.back_arrow);
        selectSubject = findViewById(R.id.selectSubject);
        linearLayout = findViewById(R.id.linearLayout);
        filter = findViewById(R.id.filter);
        progressBar = findViewById(R.id.progress_dataLoading);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        selectSubjectText = findViewById(R.id.selectedSubject);

        subject_name = findViewById(R.id.subject_name);
        cv_section_1 = findViewById(R.id.cv_section_1);
        cv_section_2 = findViewById(R.id.cv_section_2);
        rv_section_1 = findViewById(R.id.rv_section_1);
        rv_section_2 = findViewById(R.id.rv_section_2);
        tv_section_2 = findViewById(R.id.tv_section_2);
        rv_subjects = findViewById(R.id.rv_subjects);
        //getConfigurationResult();
        //getYourSubscribedSubject();
        String testSeriesId = getIntent().getStringExtra("testSeriesId");
        String type = getIntent().getStringExtra("type");
        String summaryType = getIntent().getStringExtra("summaryType");
        String categoryId = getIntent().getStringExtra("categoryId");
        strength = getIntent().getBooleanExtra("strength", false);
        testSeriesType = getIntent().getStringExtra("testSeriesType");
//        competitive = getIntent().getBooleanExtra("competitive", false);

        rv_section_1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
        rv_section_2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
        rv_subjects.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false));

        subject_name.setText(strength?getString(R.string.strength):getString(R.string.weakness));

        if (StringUtils.isEmpty(summaryType) || ("CHAPTER").equals(summaryType)) {
            getData(testSeriesId, categoryId, type, strength, UrlRoutes.TEST_SERIES_CHAPTER_ALL_SUMMARY);
        } else {
            getData(testSeriesId, categoryId, type, strength, UrlRoutes.TEST_SERIES_TOPIC_ALL_SUMMARY);
        }

        selectSubject.setVisibility(View.GONE);
        selectSubjectText.setVisibility(View.GONE);
        rv_subjects.setVisibility(View.GONE);
        rv_section_1.setVisibility(View.GONE);
        rv_section_2.setVisibility(View.GONE);
        tv_section_2.setVisibility(View.GONE);

        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        filter.setVisibility(View.GONE);

        filter.setOnClickListener(v -> {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TestSeriesSeeAllActivity.this);
            bottomSheetDialog.setContentView(R.layout.fragment_correct__wrong__seekbar__set_);

            CardView btn = bottomSheetDialog.findViewById(R.id.apply_card);
            SeekBar correct, wrong;
            TextView correctText = bottomSheetDialog.findViewById(R.id.Correct_text);
            TextView wrongText = bottomSheetDialog.findViewById(R.id.wrong_text);
            TextView textMessage = bottomSheetDialog.findViewById(R.id.textMessage);
            textMessage.setText(textMessageBody);

            correct = bottomSheetDialog.findViewById(R.id.corectSeekbar);
            wrong = bottomSheetDialog.findViewById(R.id.wrongSeekbar);

            if (MyApplication.getIntData(PreferenceKeys.CORRECT_PERCENTAGE.getKey()) != -1) {
                correct.setProgress(MyApplication.getIntData(PreferenceKeys.CORRECT_PERCENTAGE.getKey()));
                wrong.setProgress(MyApplication.getIntData(PreferenceKeys.WRONG_PERCENTAGE.getKey()));
                wrongText.setText(MyApplication.getIntData(PreferenceKeys.WRONG_PERCENTAGE.getKey()) + " %");
                correctText.setText(MyApplication.getIntData(PreferenceKeys.CORRECT_PERCENTAGE.getKey()) + " %");
            } else {
                MyApplication.saveData(PreferenceKeys.CORRECT_PERCENTAGE.getKey(), 50);
                MyApplication.saveData(PreferenceKeys.WRONG_PERCENTAGE.getKey(), 50);
                correct.setProgress(MyApplication.getIntData(PreferenceKeys.CORRECT_PERCENTAGE.getKey()));
                wrong.setProgress(MyApplication.getIntData(PreferenceKeys.WRONG_PERCENTAGE.getKey()));
                wrongText.setText(MyApplication.getIntData(PreferenceKeys.WRONG_PERCENTAGE.getKey()) + " %");
                correctText.setText(MyApplication.getIntData(PreferenceKeys.CORRECT_PERCENTAGE.getKey()) + " %");
            }

            correct.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    correctText.setText(progress + " %");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            wrong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    wrongText.setText(progress + " %");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyApplication.saveData(PreferenceKeys.CORRECT_PERCENTAGE.getKey(), correct.getProgress());
                    MyApplication.saveData(PreferenceKeys.WRONG_PERCENTAGE.getKey(), wrong.getProgress());
                    bottomSheetDialog.dismiss();
//                    if (adapter != null) {
//                        adapter.notifyDataSetChanged();
//                    }
                }
            });

            bottomSheetDialog.show();
        });


        //dialog setUp--
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialogue_subjectselection);
        dialog.getWindow().setBackgroundDrawable(this.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        selectSubject.setOnClickListener(v -> {
            dialog.show();
            RecyclerView recyclerView = dialog.findViewById(R.id.recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(DialogSubjectName);
        });

    }

    private void getConfigurationResult() {
        final Map<String, Object> map = new HashMap<>();
        map.put("featureKey", "CHAPTER_WISE_ANALYTICS_FILTER");
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(CONFIGURATION, map), null,
                response -> {
                    try {
                        Gson gson = new Gson();
                        TypeToken<ConfigurationResult> token = new TypeToken<ConfigurationResult>() {
                        };
                        ConfigurationResult configurationResult = gson.fromJson(response.toString(), token.getType());

                        if (configurationResult != null && configurationResult.getData() != null) {
                            Configuration data = configurationResult.getData();

                            Map<String, String> keyValues = data.getKeyValues();
                            String enabled = keyValues.get("CHAPTER_WISE_ANALYTICS_FILTER_ENABLED");

                            if ("true".equalsIgnoreCase(enabled)) {
                                filter.setVisibility(View.VISIBLE);
                                textMessageBody = keyValues.get("CHAPTER_WISE_ANALYTICS_FILTER_MESSAGE");

                                if (StringUtils.isEmpty(textMessageBody)) {
                                    textMessageBody = "";
                                }
                            }

                        }
                    } catch (Exception e) {

                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {

            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);


    }

    private void getData(String testSeriesId, String categoryId, String type, boolean strength, String urlRoute) {
        Map<String, Object> map = new HashMap<>();
        if(StringUtils.isNotEmpty(testSeriesId))
            map.put("testSeriesId", testSeriesId);
        if(StringUtils.isNotEmpty(categoryId))
            map.put("categoryId", categoryId);
        if (StringUtils.isNotEmpty(type)) {
            map.put("type", type);
        }
        map.put("strength", strength);
//        map.put("competitive", competitive);
        Log.d(TAG, "Request: "+map.toString());
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(urlRoute, map), null,
                response -> {
                    Log.d(TAG, "Response: "+response);
                    TypeToken<UserDetailTestAnalyticsModel> token = new TypeToken<UserDetailTestAnalyticsModel>() {
                    };
                    UserDetailTestAnalyticsModel model = gson.fromJson(response.toString(), token.getType());

                    setSubjectWiseData(model.getData());
                },
                error -> {
                    //Log.e( "getUserScore: ",error.getMessage().toString());
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(myJsonObjectRequest);
        requestQueue.start();

    }

    private void setSubjectWiseData(List<TestAnalyticsData> testAnalyticsDataList) {
        try {
            tv_section_2.setText(getString(strength ? R.string.weakness : R.string.strength));

            Log.d(TAG, "Tab List: " + testAnalyticsDataList.size());
            TestAnalysisSubjectTabAdapter testAnalysisSubjectTabAdapter = new TestAnalysisSubjectTabAdapter(this, testAnalyticsDataList, this);
            rv_subjects.setAdapter(testAnalysisSubjectTabAdapter);

            if (testAnalyticsDataList.size() <= 1) {
                rv_subjects.setVisibility(View.VISIBLE);
                rv_section_1.setVisibility(View.GONE);
                rv_section_2.setVisibility(View.GONE);
                tv_section_2.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
            } else {
                rv_subjects.setVisibility(View.VISIBLE);
            }

            testAnalysisSubjectTabAdapter.selectItem(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, getString(R.string.something_went_wrong),Toast.LENGTH_SHORT).show();
        }
        //        ArrayList<TestChapterAnalysisDetails> strengthList = new ArrayList<>();
//        ArrayList<TestChapterAnalysisDetails> weaknessList = new ArrayList<>();
//
//        Log.d(TAG, "List: "+testAnalyticsDataList.size());
//        for (TestAnalyticsData testAnalyticsData : testAnalyticsDataList) {
//            String subject = testAnalyticsData.getSubject();
//            List<TestChapterAnalysisDetails> detailList = testAnalyticsData.getTestChapterAnalysisDetails();
//            Log.d(TAG, "List Size: "+detailList.size());
//            for(TestChapterAnalysisDetails testChapterAnalysisDetails : detailList) {
//                if(testChapterAnalysisDetails.isStrength()) {
//                    strengthList.add(testChapterAnalysisDetails);
//                } else {
//                    weaknessList.add(testChapterAnalysisDetails);
//                }
//            }
////            if(strength) {
////                strengthList.addAll(list);
////            } else {
////                weaknessList.addAll(list);
////            }
//        }
//
//        Log.d(TAG, "Strength: "+strengthList.size());
//        Log.d(TAG, "Weakness: "+weaknessList.size());

    }


    private void getYourSubscribedSubject() {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_SUBSCRIBED_SUBJECT, map), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        TypeToken<List<SubjectResponse>> token = new TypeToken<List<SubjectResponse>>() {
                        };
                        List<SubjectResponse> list = gson.fromJson(response.toString(), token.getType());
                        for (SubjectResponse sm : list) {
                            subjectId.add(sm.getId());
                            Usersubject.add(sm.getSubjectName());
                        }
                        subjectId.add("Overall");
                        Usersubject.add(getString(R.string.overall));
                        DialogSubjectName = new SortSubjectNameAnalyticAdapter(Usersubject, subjectId, TestSeriesSeeAllActivity.this::onsubjectSortClick, Usersubject.size() - 1);
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }


    @Override
    public void onsubjectSortClick(String Id, String name, int position) {
//        linearLayout.setVisibility(View.GONE);
//        selectedSubject = name;
//        dialog.dismiss();
//        recyclerView.setVisibility(View.GONE);
//        progressBar.setVisibility(View.VISIBLE);
//        DialogSubjectName = new SortSubjectNameAnalyticAdapter(Usersubject, subjectId, TestSeriesSeeAllActivity.this::onsubjectSortClick, position);
//        if (selectedSubject.equals("Overall")) {
//            selectSubjectText.setText("Subject  All");
//            getData("");
//        } else {
//            selectSubjectText.setText(name);
//            getData(Id);
//        }
    }

    @Override
    public void subjectClick(TestAnalyticsData testAnalyticsData) {
        ArrayList<TestChapterAnalysisDetails> strengthList = new ArrayList<>();
        ArrayList<TestChapterAnalysisDetails> weaknessList = new ArrayList<>();

        List<TestChapterAnalysisDetails> detailList = testAnalyticsData.getTestChapterAnalysisDetails();
        for(TestChapterAnalysisDetails testChapterAnalysisDetails : detailList) {
            if(testChapterAnalysisDetails.isStrength()) {
                strengthList.add(testChapterAnalysisDetails);
            } else {
                weaknessList.add(testChapterAnalysisDetails);
            }
        }


        TestSeriesAnalyticsAdapter strengthAdapter = new TestSeriesAnalyticsAdapter(TestSeriesSeeAllActivity.this, strengthList);
        TestSeriesAnalyticsAdapter weaknessAdapter = new TestSeriesAnalyticsAdapter(TestSeriesSeeAllActivity.this, weaknessList);
        progressBar.setVisibility(View.GONE);

        if (strengthList.isEmpty() && weaknessList.isEmpty()) {
            rv_section_1.setVisibility(View.GONE);
            rv_section_2.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);

            if(strength) {
                rv_section_1.setBackgroundColor(getColor(R.color.mock_light_green));
                rv_section_2.setBackgroundColor(getColor(R.color.mock_light_red));

                rv_section_1.setAdapter(strengthAdapter);
                rv_section_2.setAdapter(weaknessAdapter);

                rv_section_1.setVisibility(strengthList.isEmpty()?View.GONE:View.VISIBLE);
                rv_section_2.setVisibility(weaknessList.isEmpty()?View.GONE:View.VISIBLE);
                tv_section_2.setVisibility(weaknessList.isEmpty()?View.GONE:View.VISIBLE);
            } else {
                rv_section_1.setBackgroundColor(getColor(R.color.mock_light_red));
                rv_section_2.setBackgroundColor(getColor(R.color.mock_light_green));

                rv_section_1.setAdapter(weaknessAdapter);
                rv_section_2.setAdapter(strengthAdapter);

                rv_section_1.setVisibility(weaknessList.isEmpty()?View.GONE:View.VISIBLE);
                rv_section_2.setVisibility(strengthList.isEmpty()?View.GONE:View.VISIBLE);
                tv_section_2.setVisibility(strengthList.isEmpty()?View.GONE:View.VISIBLE);
            }
        }
    }
}