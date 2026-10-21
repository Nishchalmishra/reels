package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CONFIGURATION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SUBSCRIBED_SUBJECT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_TEST_ANALYTICS_CHAPTER;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
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

public class TestSeriesAnalysisActivity extends BaseActivity implements SortSubjectNameAnalyticAdapter.OnSubjectSortClick {

    LinearLayout back;
    RecyclerView recyclerView;
    TestAnalyticsAdapter adapter;
    RelativeLayout selectSubject;
    ArrayList<String> Usersubject = new ArrayList<>();
    LinearLayout linearLayout;
    ArrayList<String> subjectId = new ArrayList<>();
    RelativeLayout ParentLayout;
    TextView selectSubjectText;
    ProgressBar progressBar;
    SortSubjectNameAnalyticAdapter DialogSubjectName;
    List<TestAnalyticsData> list = new ArrayList<>();
    Gson gson = new Gson();
    ImageView filter;
    String selectedSubject = "Overall";
    String textMessageBody = "";

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_series_analysis);

        back = findViewById(R.id.back_arrow);
        recyclerView = findViewById(R.id.recyclerViewTestAnalytics);
        selectSubject = findViewById(R.id.selectSubject);
        linearLayout = findViewById(R.id.linearLayout);
        filter = findViewById(R.id.filter);
        progressBar = findViewById(R.id.progress_dataLoading);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        selectSubjectText = findViewById(R.id.selectedSubject);
        getConfigurationResult();
        getYourSubscribedSubject();

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        getData("");

        selectSubject.setOnClickListener(v -> {

        });

        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        filter.setVisibility(View.GONE);

        filter.setOnClickListener(v -> {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TestSeriesAnalysisActivity.this);
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
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
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

    private void getData(String s) {
        Map<String, Object> map = new HashMap<>();
        map.put("subjectId", s);
//        map.put("testSeriesId","NEETFOUNDATIONHINDIMT1");
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_TEST_ANALYTICS_CHAPTER, map), null,
                response -> {
                    TypeToken<UserDetailTestAnalyticsModel> token = new TypeToken<UserDetailTestAnalyticsModel>() {
                    };
                    UserDetailTestAnalyticsModel model = gson.fromJson(response.toString(), token.getType());
                    list = model.getData();

                    ArrayList<TestChapterAnalysisDetails> l = new ArrayList<>();

                    for (TestAnalyticsData t : list) {
                        l.addAll(t.getTestChapterAnalysisDetails());
                    }


                    adapter = new TestAnalyticsAdapter(l);
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (l.size() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.GONE);
                    }
                    recyclerView.setAdapter(adapter);

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);


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
                        DialogSubjectName = new SortSubjectNameAnalyticAdapter(Usersubject, subjectId, TestSeriesAnalysisActivity.this::onsubjectSortClick, Usersubject.size() - 1);
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
        linearLayout.setVisibility(View.GONE);
        selectedSubject = name;
        dialog.dismiss();
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        DialogSubjectName = new SortSubjectNameAnalyticAdapter(Usersubject, subjectId, TestSeriesAnalysisActivity.this::onsubjectSortClick, position);
        if (selectedSubject.equals("Overall")) {
            selectSubjectText.setText("Subject  All");
            getData("");
        } else {
            selectSubjectText.setText(name);
            getData(Id);
        }
    }
}