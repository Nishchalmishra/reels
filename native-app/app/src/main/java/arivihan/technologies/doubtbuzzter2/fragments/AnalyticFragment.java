package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.FEATURE_ENABLED;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_ALL_SUBJECT_PROGRESS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_ONE_SUBJECT_PROGRESS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_TEST_ANALYSIS;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.TestSeriesAnalysisActivity;
import arivihan.technologies.doubtbuzzter2.adapter.ChapterProgressAnalyticsAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.DateSubjectTimeReportAnalyticsAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SortSubjectNameAnalyticAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SubjectName_CompletionReport_Analytics_Adapter;
import arivihan.technologies.doubtbuzzter2.adapter.subjectCompletionAnalyticAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.listener.TapTargetListener;
import arivihan.technologies.doubtbuzzter2.model.ApiResult;
import arivihan.technologies.doubtbuzzter2.model.ChapterProgressDto;
import arivihan.technologies.doubtbuzzter2.model.GetAllSubjectProgressAnalyticsModel;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.SubjectProgressDto;
import arivihan.technologies.doubtbuzzter2.model.SubjectTimeReportModel;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class AnalyticFragment extends Fragment implements SortSubjectNameAnalyticAdapter.OnSubjectSortClick, TapTargetListener {

    private static final String TAG = "AnalyticFragment";
    RelativeLayout ParentLayout, selectSubject;
    ProgressBar progressBarGraph;
    ProgressBar progressBar1, progressBar2, progressBar3, progressBar4;
    TextView subjectCompletion, subjectTimeReport;
    TextView selectDate, testAnalysis;
    CardView subjectTimeReportCard;
    LineChart lineChart;
    LinearLayout linearLayoutCompletionReportForOne, linearLayoutCompletionReportForAll;
    TextView doubtAsked, videoWatched, totalHours, selectSubjectText;
    TextView testAttempted, correctAnswer, timeEfficiency, hard, medium, easy, totalHard, totalMedium, totalEasy;
    RelativeLayout relativeLayoutGraphOneSubjectCompletion, relativeLayoutGraphAllSubjectCompletion, relativeLayoutGraphSubjectCompletion, relativeLayoutNumberSubjectCompletion, relativeLayoutNumberSubjectReport, relativeLayoutGraphReport;
    boolean graphViewCompletion = true;
    boolean graphViewReport = true;
    ImageView yellowCircle, blueCircle, greenCircle, redCircle;
    RecyclerView recyclerViewSubjectCompletion;
    RecyclerView recyclerViewSubjectReport;
    RecyclerView recyclerViewSubjectReportForOneSubject;
    RecyclerView recyclerViewSubjectReportData;
    RecyclerView recyclerViewOneSubjectCompletionGraph;
    RecyclerView recyclerViewSubjectCompletionSubjectName;
    ArrayList<SubjectProgressDto> modelSubCompletion = new ArrayList<>();
    ArrayList<SubjectProgressDto> modelSubCompletion2 = new ArrayList<>();
    ArrayList<SubjectTimeReportModel> modelSubReportData = new ArrayList<>();
    SortSubjectNameAnalyticAdapter DialogSubjectName;
    ArrayList<String> modelSubReport = new ArrayList<>();
    ArrayList<ChapterProgressDto> modelChapterProgress = new ArrayList<>();
    ArrayList<String> Usersubject = new ArrayList<>();
    ArrayList<String> subjectId = new ArrayList<>();
    LottieAnimationView lottieAnimationViewNoChapter;
    TextView videoAnalytics;
    String selectedMonth = "";
    String selectedYear = "";
    String selectedDay = "";
    String FromDate = "";
    String ToDate = "";
    private DatePickerDialog datePickerDialog;
    Calendar calendarMain = Calendar.getInstance();
//    private TapTargetSequence analyticFragment;

    ProgressBar progressBarDataLoading;
    String selectedSubject = "Overall";

    Gson gson = new Gson();
    Dialog subjectDialog;

    private LineChart mChart;
    private LinearLayout backLayout;

    HomeScreenViewModel homeScreenViewModel;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytic, container, false);

        ParentLayout = view.findViewById(R.id.ParentRelativeLayout);
        progressBar1 = view.findViewById(R.id.progress1);
        progressBar2 = view.findViewById(R.id.progress2);
        progressBar3 = view.findViewById(R.id.progress3);
        progressBar4 = view.findViewById(R.id.progress4);
        yellowCircle = view.findViewById(R.id.circle4);
        testAnalysis = view.findViewById(R.id.TestAnalysis);
        redCircle = view.findViewById(R.id.redCircle);
        blueCircle = view.findViewById(R.id.blueCircle);
        greenCircle = view.findViewById(R.id.greenCircle);
        videoAnalytics = view.findViewById(R.id.videoAnalytics);
        videoWatched = view.findViewById(R.id.videoWatched);
        progressBarGraph = view.findViewById(R.id.progress_dataLoading2);
        totalHours = view.findViewById(R.id.totalHours);
        doubtAsked = view.findViewById(R.id.doubtAsked);
        lineChart = view.findViewById(R.id.lineChat);
        subjectTimeReportCard = view.findViewById(R.id.subjectTimeRepostCard);
        selectDate = view.findViewById(R.id.selectedDates);
        subjectCompletion = view.findViewById(R.id.CompleteionView);
        subjectTimeReport = view.findViewById(R.id.ReportView);
        selectSubject = view.findViewById(R.id.selectSubject);
        selectSubjectText = view.findViewById(R.id.selectedSubject);
        testAttempted = view.findViewById(R.id.testAttempted);
        correctAnswer = view.findViewById(R.id.correctAnswerPer);
        timeEfficiency = view.findViewById(R.id.timeEfficiency);
        hard = view.findViewById(R.id.Hard);
        medium = view.findViewById(R.id.Medium);
        easy = view.findViewById(R.id.Easy);
        totalHard = view.findViewById(R.id.numberHard);
        totalMedium = view.findViewById(R.id.numberMedium);
        totalEasy = view.findViewById(R.id.numberEasy);
        backLayout = view.findViewById(R.id.back_button_layout);
        backLayout.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        linearLayoutCompletionReportForAll = view.findViewById(R.id.ForAllSubjectNumber);
        linearLayoutCompletionReportForOne = view.findViewById(R.id.ForOneSubjectNumber);

        relativeLayoutNumberSubjectCompletion = view.findViewById(R.id.subjectCompletionReposNumberView);
        relativeLayoutGraphSubjectCompletion = view.findViewById(R.id.subjectCompletionReportGraphView);
        recyclerViewSubjectCompletionSubjectName = view.findViewById(R.id.subjectNameNumberViewRecyclerview);
        relativeLayoutGraphAllSubjectCompletion = view.findViewById(R.id.ForAllSubject);
        relativeLayoutGraphReport = view.findViewById(R.id.subjectTimeReportGraphView);
        relativeLayoutGraphOneSubjectCompletion = view.findViewById(R.id.ForOneSubject);

        recyclerViewSubjectReportForOneSubject = view.findViewById(R.id.recyclerviewSubjectCompletionOneSubject);
        recyclerViewOneSubjectCompletionGraph = view.findViewById(R.id.oneSubjectRecyclerview);
        relativeLayoutNumberSubjectReport = view.findViewById(R.id.subjectTimeReportNumberView);
        recyclerViewSubjectCompletion = view.findViewById(R.id.recyclerviewSubjectCompletion);
        recyclerViewSubjectReport = view.findViewById(R.id.recyclerviewSubjectTimeReport);
        recyclerViewSubjectReportData = view.findViewById(R.id.recyclerviewSubjectTimeReportDate);
        mChart = view.findViewById(R.id.lineChat);
        progressBarDataLoading = view.findViewById(R.id.progress_dataLoading);
        lottieAnimationViewNoChapter = view.findViewById(R.id.NoChapter);

        MyApplication myapp =  (MyApplication) getActivity().getApplication();
        homeScreenViewModel = myapp.getHomeScreenViewModel();

        // Recyclerview Initializer--
        recyclerViewSubjectCompletion.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerViewSubjectReport.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerViewSubjectReportData.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewOneSubjectCompletionGraph.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerViewSubjectCompletionSubjectName.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerViewSubjectReportForOneSubject.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        //dialog setUp--
        subjectDialog = new Dialog(getActivity());
        subjectDialog.setContentView(R.layout.custom_dialogue_subjectselection);
        subjectDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        subjectDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // changing view from graph to number and number to graph--
        subjectCompletion.setOnClickListener(view1 -> {
            if (graphViewCompletion) {
                relativeLayoutNumberSubjectCompletion.setVisibility(View.VISIBLE);
                relativeLayoutGraphSubjectCompletion.setVisibility(View.GONE);
                if (selectedSubject.equals("Overall")) {
                    linearLayoutCompletionReportForAll.setVisibility(View.VISIBLE);
                    linearLayoutCompletionReportForOne.setVisibility(View.GONE);
                } else {
                    linearLayoutCompletionReportForAll.setVisibility(View.GONE);
                    linearLayoutCompletionReportForOne.setVisibility(View.VISIBLE);
                }
                subjectCompletion.setText(MyApplication.getInstance().getString(R.string.graphView));
                graphViewCompletion = false;
            } else {
                relativeLayoutNumberSubjectCompletion.setVisibility(View.GONE);
                relativeLayoutGraphSubjectCompletion.setVisibility(View.VISIBLE);
                if (selectedSubject.equals("Overall")) {
                    relativeLayoutGraphAllSubjectCompletion.setVisibility(View.VISIBLE);
                    relativeLayoutGraphOneSubjectCompletion.setVisibility(View.GONE);
                } else {
                    relativeLayoutGraphAllSubjectCompletion.setVisibility(View.GONE);
                    relativeLayoutGraphOneSubjectCompletion.setVisibility(View.VISIBLE);
                }
                subjectCompletion.setText(MyApplication.getInstance().getString(R.string.numberView));
                graphViewCompletion = true;
            }
        });

        // Not working at the momnet VISISBILTY : GONE
        subjectTimeReport.setOnClickListener(v -> {
            if (graphViewReport) {
                relativeLayoutNumberSubjectReport.setVisibility(View.GONE);
                relativeLayoutGraphReport.setVisibility(View.VISIBLE);
                subjectTimeReport.setText(MyApplication.getInstance().getString(R.string.graphView));
                graphViewReport = false;
            } else {
                relativeLayoutNumberSubjectReport.setVisibility(View.VISIBLE);
                relativeLayoutGraphReport.setVisibility(View.GONE);
                subjectTimeReport.setText(MyApplication.getInstance().getString(R.string.numberView));
                graphViewReport = true;
            }
        });


        // sort according to the subject---
        selectSubject.setOnClickListener(v -> {
            subjectDialog.show();

            RecyclerView recyclerView = subjectDialog.findViewById(R.id.recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(DialogSubjectName);

        });

        testAnalysis.setVisibility(View.GONE);
        checkAndUpdateChapterWiseAnalysis(testAnalysis);

        testAnalysis.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TestSeriesAnalysisActivity.class);
            startActivity(intent);
        });

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);

        Calendar calendarX = Calendar.getInstance();
        getCurrentDateOnceCall(calendarX);

        // Getting data from Api--
        getSubjectCompletionData();
        getTestSeriesData("");
        getYourSubscribedSubject();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, 1, 15);
        long startFrom = calendar.getTimeInMillis();
        Calendar date = Calendar.getInstance();
        long endTo = date.getTimeInMillis();

        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();

        CalendarConstraints.DateValidator dateValidatorMin = DateValidatorPointForward.from(startFrom);
        CalendarConstraints.DateValidator dateValidatorMax = DateValidatorPointBackward.before(endTo);

        ArrayList<CalendarConstraints.DateValidator> listValidators = new ArrayList<CalendarConstraints.DateValidator>();
        listValidators.add(dateValidatorMin);
        listValidators.add(dateValidatorMax);
        CalendarConstraints.DateValidator validators = CompositeDateValidator.allOf(listValidators);
        constraintsBuilderRange.setValidator(validators);


        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(R.string.select_ending_date_of_analytic_week);
        builder.setCalendarConstraints(constraintsBuilderRange.build());
        MaterialDatePicker materialDatePicker = builder.build();


        materialDatePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Long>) selection -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selection);
            SimpleDateFormat simpleformat = new SimpleDateFormat("dd-MM-yyyy");
            String date1 = simpleformat.format(cal.getTime());

            int month = Integer.parseInt(date1.substring(3, 5));

            selectedMonth = makeDateString(month - 1);

            ToDate = date1;

            cal.add(Calendar.DATE, -6);
            date1 = simpleformat.format(cal.getTime());
            FromDate = date1;

            cal.add(Calendar.DATE, 6);

            //getSubjectTimeReportData();
            getCurrentDate(cal);
            getDate(cal);

        });


        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                materialDatePicker.show(getActivity().getSupportFragmentManager(), "Tag");
            }
        });

        testAttempted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String courseName = MyApplication.getStringData(PreferenceKeys.COURSE_NAME.getKey());
                String testId = courseName != null ? courseName.toLowerCase() : "";
                String testTitle = "";
                if (courseName != null) {
                    testTitle = courseName.equalsIgnoreCase("jee") ? "JEE All India" : "NEET All India";
                }
                CommonNavigator.navigateToTestSeriesListActivity(getActivity(),
                        testId, testTitle, null, false, null, false, false, TAG);
            }
        });

        if (CommonMethods.isOnline(getActivity())) {
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
        return view;
    }

    private void checkAndUpdateChapterWiseAnalysis(TextView testAnalysis) {
        final Map<String, Object> map = new HashMap<>();
        map.put("featureKey", "ANALYTICS");
        map.put("key", "CHAPTER_WISE_ANALYSIS");
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(FEATURE_ENABLED, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<ApiResult> token = new TypeToken<ApiResult>() {
                    };
                    ApiResult apiResult = gson.fromJson(response.toString(), token.getType());
                    if (apiResult.isSuccess()) {
                        testAnalysis.setVisibility(View.VISIBLE);
                    }

                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {

            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest);


    }

    // set Current date by Default
    private void getCurrentDate(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleformat = new SimpleDateFormat("dd-MM-yyyy");
        ToDate = simpleformat.format(cal.getTime());

        cal.add(Calendar.DATE, -6);

        FromDate = simpleformat.format(cal.getTime());

        selectedDay = String.valueOf(day);
        selectedYear = String.valueOf(year);
        selectedMonth = makeDateString(month);

        calendarMain.set(year, month, day);
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        String yearEnd = String.valueOf(calendar.get(Calendar.YEAR));
        int monthEnd = calendar.get(Calendar.MONTH);
        int dayEnd = calendar.get(Calendar.DAY_OF_MONTH);


        selectDate.setText(dayEnd + " " + makeDateString(monthEnd).substring(0, 3) + " '" + yearEnd.substring(2, 4) + " - " + selectedDay + " " + selectedMonth.substring(0, 3) + " '" + selectedYear.substring(2, 4));
        getDate(cal);
    }

    // set Current date by Default
    private void getCurrentDateOnceCall(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleformat = new SimpleDateFormat("dd-MM-yyyy");
        ToDate = simpleformat.format(cal.getTime());

        cal.add(Calendar.DATE, -6);

        FromDate = simpleformat.format(cal.getTime());

        selectedDay = String.valueOf(day);
        selectedYear = String.valueOf(year);
        selectedMonth = makeDateString(month);

        calendarMain.set(year, month, day);
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        String yearEnd = String.valueOf(calendar.get(Calendar.YEAR));
        int monthEnd = calendar.get(Calendar.MONTH);
        int dayEnd = calendar.get(Calendar.DAY_OF_MONTH);


        selectDate.setText(dayEnd + " " + makeDateString(monthEnd).substring(0, 3) + " '" + yearEnd.substring(2, 4) + " - " + selectedDay + " " + selectedMonth.substring(0, 3) + " '" + selectedYear.substring(2, 4));
        getDate(cal);
        // getSubjectTimeReportData();
    }


    // Set Date Recyclerview
    private void getDate(Calendar calendarMain) {
        modelSubReport.clear();

        modelSubReport.add(calendarMain.get(Calendar.DAY_OF_MONTH) + " " + makeDateString(calendarMain.get(Calendar.MONTH)).substring(0, 3));
        calendarMain.add(Calendar.DATE, 1);
        modelSubReport.add(calendarMain.get(Calendar.DAY_OF_MONTH) + " " + makeDateString(calendarMain.get(Calendar.MONTH)).substring(0, 3));
        calendarMain.add(Calendar.DATE, 1);
        modelSubReport.add(calendarMain.get(Calendar.DAY_OF_MONTH) + " " + makeDateString(calendarMain.get(Calendar.MONTH)).substring(0, 3));
        calendarMain.add(Calendar.DATE, 1);
        modelSubReport.add(calendarMain.get(Calendar.DAY_OF_MONTH) + " " + makeDateString(calendarMain.get(Calendar.MONTH)).substring(0, 3));
        calendarMain.add(Calendar.DATE, 1);
        modelSubReport.add(calendarMain.get(Calendar.DAY_OF_MONTH) + " " + makeDateString(calendarMain.get(Calendar.MONTH)).substring(0, 3));
        calendarMain.add(Calendar.DATE, 1);
        modelSubReport.add(calendarMain.get(Calendar.DAY_OF_MONTH) + " " + makeDateString(calendarMain.get(Calendar.MONTH)).substring(0, 3));
        calendarMain.add(Calendar.DATE, 1);
        modelSubReport.add(calendarMain.get(Calendar.DAY_OF_MONTH) + " " + makeDateString(calendarMain.get(Calendar.MONTH)).substring(0, 3));
        DateSubjectTimeReportAnalyticsAdapter adapter = new DateSubjectTimeReportAnalyticsAdapter(modelSubReport);
        recyclerViewSubjectReport.setAdapter(adapter);
    }

    private void getTestSeriesData(String subjectId) {

        if (homeScreenViewModel.getAnalyticsFragmentTestSeriesData() != null) {
            GetAllSubjectProgressAnalyticsModel list = homeScreenViewModel.getAnalyticsFragmentTestSeriesData();
            hard.setText(String.valueOf(list.getData().getNoOfHard()));
            totalHard.setText("/" + String.valueOf(list.getData().getTotalHard()));
            medium.setText(String.valueOf(list.getData().getNoOfMedium()));
            totalMedium.setText("/" + String.valueOf(list.getData().getTotalMedium()));
            easy.setText(String.valueOf(list.getData().getNoOfEasy()));
            totalEasy.setText("/" + String.valueOf(list.getData().getTotalEasy()));
            testAttempted.setText(String.valueOf(list.getData().getTestsAttempted()));
            correctAnswer.setText(String.valueOf(list.getData().getCorrectAnswerPercentage()));
            // at present we have total question not efficiency-----
            timeEfficiency.setText(String.valueOf(list.getData().getTotalQuestion()));
            return;
        }

        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        if (!subjectId.isEmpty())
            map.put("subjectId", subjectId);
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_TEST_ANALYSIS, map), null,
                response -> {
                    TypeToken<GetAllSubjectProgressAnalyticsModel> token = new TypeToken<GetAllSubjectProgressAnalyticsModel>() {
                    };
                    GetAllSubjectProgressAnalyticsModel list = gson.fromJson(response.toString(), token.getType());

                    homeScreenViewModel.setAnalyticsFragmentTestSeriesData(list);

                    hard.setText(String.valueOf(list.getData().getNoOfHard()));
                    totalHard.setText("/" + String.valueOf(list.getData().getTotalHard()));
                    medium.setText(String.valueOf(list.getData().getNoOfMedium()));
                    totalMedium.setText("/" + String.valueOf(list.getData().getTotalMedium()));
                    easy.setText(String.valueOf(list.getData().getNoOfEasy()));
                    totalEasy.setText("/" + String.valueOf(list.getData().getTotalEasy()));
                    testAttempted.setText(String.valueOf(list.getData().getTestsAttempted()));
                    correctAnswer.setText(String.valueOf(list.getData().getCorrectAnswerPercentage()));
                    // at present we have total question not efficiency-----
                    timeEfficiency.setText(String.valueOf(list.getData().getTotalQuestion()));

                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                // CommonMethods.showNegativeSnackbar(ParentLayout,error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);

    }

    private void getYourSubscribedSubject() {
        Usersubject.clear();
        homeScreenViewModel.getSubjectList().observeForever(this::updateSubject);
    }

    private void updateSubject(List<SubjectResponse> subjectModels) {
        if (subjectModels != null) {
            subjectId.clear();
            Usersubject.clear();


            for (SubjectResponse sm : subjectModels) {
                subjectId.add(sm.getId());
                Usersubject.add(sm.getSubjectName());
            }

            subjectId.add("Overall");
            Usersubject.add(MyApplication.getInstance().getString(R.string.overall));

            DialogSubjectName = new SortSubjectNameAnalyticAdapter(Usersubject, subjectId, AnalyticFragment.this::onsubjectSortClick, Usersubject.size() - 1);
        }
    }


    private void getOneSubjectCompletionData(String subjectId) {

        modelChapterProgress.clear();
        modelSubCompletion2.clear();


        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        map.put("subjectId", subjectId);
        Log.d("SubscribedSubject", "id = "+ subjectId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_ONE_SUBJECT_PROGRESS, map), null,
                response -> {
                    TypeToken<GetAllSubjectProgressAnalyticsModel> token = new TypeToken<GetAllSubjectProgressAnalyticsModel>() {
                    };
                    GetAllSubjectProgressAnalyticsModel list = gson.fromJson(response.toString(), token.getType());


                    doubtAsked.setText(list.getData().getLectureAnalytics().getDoubtAsked() + "");
                    totalHours.setText(list.getData().getLectureAnalytics().getTotalHours());
                    videoWatched.setText(list.getData().getLectureAnalytics().getVideoWatched() + "");


                    for (ChapterProgressDto cp : list.getData().getChapterProgressDtoList()) {
                        modelChapterProgress.add(cp);
                        SubjectProgressDto sm = new SubjectProgressDto();
                        if (cp.getChapterName().length() > 12)
                            sm.setSubjectName(cp.getChapterName().substring(0, 11) + "..");
                        else
                            sm.setSubjectName(cp.getChapterName());
                        sm.setTotalChapter(cp.getTotalMicrolecture());
                        sm.setChapterCovered(cp.getMicrolectureCovered());
                        sm.setProgress(cp.getProgress());
                        modelSubCompletion2.add(sm);
                    }

                    if (modelChapterProgress.size() == 0)
                        lottieAnimationViewNoChapter.setVisibility(View.VISIBLE);
                    else
                        lottieAnimationViewNoChapter.setVisibility(View.GONE);

                    progressBarDataLoading.setVisibility(View.GONE);
                    ChapterProgressAnalyticsAdapter adapter = new ChapterProgressAnalyticsAdapter(modelChapterProgress);
                    recyclerViewOneSubjectCompletionGraph.setAdapter(adapter);
                    subjectCompletionAnalyticAdapter adapter2 = new subjectCompletionAnalyticAdapter(modelSubCompletion2);
                    recyclerViewSubjectReportForOneSubject.setAdapter(adapter2);

                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);


    }

    private void setGraphCompletionData(GetAllSubjectProgressAnalyticsModel list) {
        ArrayList<String> sub = new ArrayList<>();

        doubtAsked.setText(list.getData().getLectureAnalytics().getDoubtAsked() + "");
        totalHours.setText(list.getData().getLectureAnalytics().getTotalHours());
        videoWatched.setText(list.getData().getLectureAnalytics().getVideoWatched() + "");

        modelSubCompletion.clear();
        //showing subject name on Graph Mode in Right Side----
        for (SubjectProgressDto s : list.getData().getSubjectProgressDtoList()) {
            modelSubCompletion.add(s);
            if (s.getSubjectName().length() > 15)
                sub.add(s.getSubjectName().substring(0, 14));
            else
                sub.add(s.getSubjectName());
        }

        SubjectName_CompletionReport_Analytics_Adapter adapter = new SubjectName_CompletionReport_Analytics_Adapter(sub);
        recyclerViewSubjectCompletionSubjectName.setAdapter(adapter);

        if (sub.size() > 0) {
            progressBar1.setProgress(modelSubCompletion.get(0).getProgress());
        }
        if (sub.size() > 1) {
            progressBar2.setProgress(modelSubCompletion.get(1).getProgress());
        }
        if (sub.size() > 2) {
            progressBar3.setProgress(modelSubCompletion.get(2).getProgress());
        }
        if (sub.size() > 3) {
            progressBar4.setProgress(modelSubCompletion.get(3).getProgress());
        } else {
            progressBar4.setVisibility(View.GONE);
        }


        subjectCompletionAnalyticAdapter adapter2 = new subjectCompletionAnalyticAdapter(modelSubCompletion);
        recyclerViewSubjectCompletion.setAdapter(adapter2);

        progressBarDataLoading.setVisibility(View.GONE);
        if (selectedSubject.equals("Overall"))
            relativeLayoutGraphAllSubjectCompletion.setVisibility(View.VISIBLE);

    }

    private void getSubjectCompletionData() {


        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_ALL_SUBJECT_PROGRESS, map), null,
                response -> {
                    TypeToken<GetAllSubjectProgressAnalyticsModel> token = new TypeToken<GetAllSubjectProgressAnalyticsModel>() {
                    };
                    GetAllSubjectProgressAnalyticsModel list = gson.fromJson(response.toString(), token.getType());

                    homeScreenViewModel.setAnalyticsFragmentSubjectCompletionData(list);

                    setGraphCompletionData(list);
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
    }

    private String makeDateString(int month) {
        if (month == 0) {
            return "January";
        }
        if (month == 1) {
            return "February";
        }
        if (month == 2) {
            return "March";
        }
        if (month == 3) {
            return "April";
        }
        if (month == 4) {
            return "May";
        }
        if (month == 5) {
            return "June";
        }
        if (month == 6) {
            return "July";
        }
        if (month == 7) {
            return "August";
        }
        if (month == 8) {
            return "September";
        }
        if (month == 9) {
            return "October";
        }
        if (month == 10) {
            return "November";
        }
        return "December";


    }


    @Override
    public void onsubjectSortClick(String id, String name, int position) {
        selectedSubject = name;
        subjectDialog.dismiss();
        subjectTimeReport.setText(MyApplication.getInstance().getString(R.string.numberView));
        lottieAnimationViewNoChapter.setVisibility(View.GONE);
        relativeLayoutNumberSubjectCompletion.setVisibility(View.GONE);
        relativeLayoutGraphSubjectCompletion.setVisibility(View.VISIBLE);
        relativeLayoutGraphAllSubjectCompletion.setVisibility(View.GONE);
        relativeLayoutGraphOneSubjectCompletion.setVisibility(View.GONE);
        linearLayoutCompletionReportForAll.setVisibility(View.GONE);
        linearLayoutCompletionReportForOne.setVisibility(View.GONE);
        DialogSubjectName = new SortSubjectNameAnalyticAdapter(Usersubject, subjectId, AnalyticFragment.this::onsubjectSortClick, position);
        if (selectedSubject.equals(MyApplication.getInstance().getString(R.string.overall))) {
            selectSubjectText.setText(MyApplication.getInstance().getString(R.string.subject_all));
            relativeLayoutGraphAllSubjectCompletion.setVisibility(View.VISIBLE);
            relativeLayoutGraphOneSubjectCompletion.setVisibility(View.GONE);
            getSubjectCompletionData();
            getTestSeriesData("");
            graphViewCompletion = true;
        } else {
            selectSubjectText.setText(name);
            subjectTimeReportCard.setVisibility(View.GONE);
            relativeLayoutGraphAllSubjectCompletion.setVisibility(View.GONE);
            relativeLayoutGraphOneSubjectCompletion.setVisibility(View.VISIBLE);
            progressBarDataLoading.setVisibility(View.VISIBLE);
            getOneSubjectCompletionData(id);
            getTestSeriesData(id);
            graphViewCompletion = true;
        }
    }

    @Override
    public void backPressed() {
    }

}