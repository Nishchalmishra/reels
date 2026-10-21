package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_CRASH_COURSE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_DASHBOARD_VIDEO;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_HIGHLIGHT_WEEK;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.HALF_YEARLY_TODAY_ROUTINE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.HALF_YEARLY_USER_TASK_UPDATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_CHAPTER_DEMO_PROGRESS;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.ReelsActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListByBatchActivity;
import arivihan.technologies.doubtbuzzter2.adapter.HalfYearlyAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.HalfYearlyCompetitiveAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.HalfYearlyCompetitiveHorizontalAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.HalfYearlyCompetitiveSectionAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.HalfYearlyHorizontalAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.HalfYearlySectionAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.HighlistAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.TodayClassAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.common.PlayStoreDialog;
import arivihan.technologies.doubtbuzzter2.customView.HalfYearlyBottomSheetDialog;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.fragments.TodayTaskFragment;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.WrapContentLinearLayoutManager;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyContentType;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyItem;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyPopupData;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlySection;
import arivihan.technologies.doubtbuzzter2.model.HighlistModel;
import arivihan.technologies.doubtbuzzter2.model.HomeScreenCacheData;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.batch.videos.BatchVideoType;
import arivihan.technologies.doubtbuzzter2.enums.batch.videos.VideoOrientationType;
import arivihan.technologies.doubtbuzzter2.model.VideoResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.CrashCourseResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassItemResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.PreExamContentResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.PreExamSubjectResponse;

import org.json.JSONArray;
import org.shadow.apache.commons.lang3.math.NumberUtils;

public class HalfYearlyActivity extends AppCompatActivity implements HalfYearlyBottomSheetDialog.HalfYearlyBottomSheetListener,
        HalfYearlySectionAdapter.HalfYearlySectionInterface, HalfYearlyHorizontalAdapter.HalfYearlyHorizontalInterface, HalfYearlyAdapter.CountDownVisibilityListener,
        HalfYearlyCompetitiveSectionAdapter.CompetitiveSectionInterface, HalfYearlyCompetitiveHorizontalAdapter.CompetitiveHorizontalInterface,
        HighlistAdapter.OnHighlightClick, TodayTaskFragment.TodayTaskListener {

    LinearLayout back, countDown;
    MaterialCardView needHelp;
    RecyclerView recyclerView;
    String examId, examName;
    HalfYearlyAdapter halfYearlyAdapter;
    HalfYearlyCompetitiveAdapter competitiveAdapter;
    private CrashCourseResponse crashCourseResponse;
    private ArrayList<HalfYearlySection> competitiveSections;
    private HalfYearlyItem clickedCompetitiveItem;
    HalfYearlyBottomSheetDialog bottomSheetDialog;
    private Handler handler;
    TextView title;
    private Runnable runnable;
    private TextView hourCountDown, minuteCountDown, secondCountDown;
    private CardView hourCountDownBackground, minuteCountDownBackground, secondCountDownBackground;
    public static String TAG = HalfYearlyActivity.class.getSimpleName();
    ShimmerFrameLayout shimmerFrameLayout;
    ScrollView scrollView;
    private static final int REQUEST_CODE = 1;
    private TodayClassItemResponse clickedHorizontalItem;
    PlayStoreDialog playStoreDialog;
    NetworkStatusLiveData networkStatusLiveData;
    private List<HighlistModel> highlistModelList = new ArrayList<>();
    HighlistAdapter highlistAdapter;

    RecyclerView recyclerViewhighlight;
    ShimmerFrameLayout shimmerHighlights;
    private TextView highlightsText;
    private CardView viral_video_section;
    private TextView text_1, text_2, text_3, tv_need_help;
    private Dialog lectureLiveUpcomingDialog;
    private Dialog lockPopUpDialog;
    //    private FrameLayout today_task_container;
    public static final String KEY_VIDEO_URI = "video_uri";
    private final ActivityResultLauncher<Intent> subscriptionScreenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        //isSubscribed
                        Intent data = result.getData();
                        boolean isSubscribed = data.getBooleanExtra("isSubscribed", false);
                        if (isSubscribed) {

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_half_yearly);

        scrollView = findViewById(R.id.scroll_view);
        shimmerFrameLayout = findViewById(R.id.shimmer_layout);
        hourCountDownBackground = findViewById(R.id.hour_countdown_background);
        minuteCountDownBackground = findViewById(R.id.minute_countdown_background);
        secondCountDownBackground = findViewById(R.id.second_countdown_background);
        hourCountDown = findViewById(R.id.hour_countdown);
        minuteCountDown = findViewById(R.id.minute_countdown);
        secondCountDown = findViewById(R.id.second_countdown);
        viral_video_section = findViewById(R.id.viral_video_section);
        title = findViewById(R.id.title);
        back = findViewById(R.id.back_button_layout);
        countDown = findViewById(R.id.countdown);
        needHelp = findViewById(R.id.need_help);
        tv_need_help = findViewById(R.id.tv_need_help);
        text_1 = findViewById(R.id.text_1);
        text_2 = findViewById(R.id.text_2);
//        today_task_container = findViewById(R.id.today_task_container);

        setNeedHelpContent();
        Uri deepLinkData = getIntent().getData();
        if (deepLinkData != null) {
            examId = deepLinkData.getQueryParameter("examId");
            examName = deepLinkData.getQueryParameter("examName");

            if (examId == null) {
                try {
                    double examIdInt = deepLinkData.getQueryParameter("examId") != null
                            ? Double.parseDouble(deepLinkData.getQueryParameter("examId"))
                            : -1;

                    if (examIdInt != -1) {
                        examId = String.valueOf((int) examIdInt);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {

            examId = getIntent().getStringExtra("examId");
            examName = getIntent().getStringExtra("examName");

            // Fallback: check integer extra
            if (examId == null) {
                double examIdInt = getIntent().getDoubleExtra("examId", -1);
                if (examIdInt != -1) {
                    examId = String.valueOf((int) examIdInt);
                }
            }
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("exam_name", examName);
        params.put(PreferenceKeys.EXAM_ID.getKey(), examId);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.
                crash_course_screen_viewed.getActionName(), params);
        Log.d(TAG, "Crash course params: " + params.toString());
        if (StringUtils.isNotEmpty(examName)) {
            title.setText(examName);
        }
        back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        needHelp.setOnClickListener(view -> {
            try {
                String url = "https://api.whatsapp.com/send?phone=" + MyApplication.getPhoneNumberWith91();
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.whatsapp");
                intent.setData(uri);
                startActivity(intent);

            } catch (ActivityNotFoundException e) {
                Toast.makeText(HalfYearlyActivity.this, getString(R.string.whatsapp_not_found), Toast.LENGTH_SHORT).show();
            }
        });
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            Rect scrollBounds = new Rect();
            scrollView.getHitRect(scrollBounds);
            highlistAdapter.handleRecyclerVisibility(recyclerViewhighlight.getLocalVisibleRect(scrollBounds));
        });
        highlightExtraction();

        recyclerView = findViewById(R.id.parent_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        if (MyApplication.isCompetitiveProfile()) {
            competitiveAdapter = new HalfYearlyCompetitiveAdapter(this, new ArrayList<>(), this, this, this);
            recyclerView.setAdapter(competitiveAdapter);
        } else {
            halfYearlyAdapter = new HalfYearlyAdapter(this, null, this, this, this);
            recyclerView.setAdapter(halfYearlyAdapter);
        }

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(back, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    if (MyApplication.isCompetitiveProfile() ? competitiveSections == null : crashCourseResponse == null)
                        getData();
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(back, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

        getData();
    }

    private void setNeedHelpContent() {
        try {
            Log.d("NeedHelpPopup", "setNeedHelpContent() called...");

            String remoteJson = MyApplication.getFirebaseRemoteConfig()
                    .getString("half_yearly_activity_need_help_v3");

            Log.d("NeedHelpPopup", "Remote JSON received: " + remoteJson);

            if (remoteJson == null || remoteJson.trim().isEmpty()) {
                Log.e("NeedHelpPopup", "Remote JSON is EMPTY!");
                return;
            }

            // 2. Parse Root JSON
            JSONObject root = new JSONObject(remoteJson);
            Log.d("NeedHelpPopup", "Root JSON parsed successfully");

            // 3. Get language + courseId
            String lang = Locale.getDefault().getLanguage();
            String courseId = MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey());

            Log.d("NeedHelpPopup", "Device Language: " + lang);
            Log.d("NeedHelpPopup", "Selected Course ID: " + courseId);

            // 4. Get course object
            JSONObject courseObj = root.optJSONObject(courseId);

            if (courseObj == null) {
                Log.w("NeedHelpPopup", "Course ID not found. Falling back to default...");
                courseObj = root.optJSONObject("default");
            }

            if (courseObj == null) {
                Log.e("NeedHelpPopup", "No course/default object found in JSON!");
                return;
            }

            Log.d("NeedHelpPopup", "Course object found: " + courseObj);

            // 5. Get language object
            JSONObject langObj = courseObj.optJSONObject(lang);

            if (langObj == null) {
                Log.w("NeedHelpPopup", "Language '" + lang + "' not found. Falling back to English...");
                langObj = courseObj.optJSONObject("en");
            }

            if (langObj == null) {
                Log.e("NeedHelpPopup", "No language object found even for English!");
                return;
            }

            Log.d("NeedHelpPopup", "Language object found: " + langObj);

            // 6. Extract strings
            String t1 = langObj.optString("text_1", "");
            String t2 = langObj.optString("text_2", "");
            String tvHelp = langObj.optString("tv_need_help", "");

            Log.d("NeedHelpPopup", "Extracted text_1: " + t1);
            Log.d("NeedHelpPopup", "Extracted text_2: " + t2);
            Log.d("NeedHelpPopup", "Extracted tv_need_help: " + tvHelp);

            // 7. Set UI Text
            setRichText(text_1, t1,
                    R.string.Half_yearly_ke_practice_test_kese_dein);

            setRichText(text_2, t2,
                    R.string.Our_experts_can_answer_all_your_exam_related_questions);

            setRichText(tv_need_help, tvHelp,
                    R.string.talk_to_us);

            Log.d("NeedHelpPopup", "Need Help Content set successfully!");

        } catch (Exception e) {
            Log.e("NeedHelpPopup", "Exception in setNeedHelpContent()", e);
        }
    }


    private void setRichText(TextView tv, String remoteValue, int fallbackRes) {
        if (CommonMethods.isValidJson(remoteValue)) {
            tv.setText(StyledTextBuilder.fromJson(this, remoteValue));
        } else {
            tv.setText(getString(fallbackRes));
        }
    }


    private void setUpViralVideo() {
        recyclerViewhighlight = findViewById(R.id.recyclerViewHighlight);
        shimmerHighlights = findViewById(R.id.shimmer_highlights);

        highlistAdapter = new HighlistAdapter(highlistModelList, this, this);
        recyclerViewhighlight.setLayoutManager(
                new WrapContentLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerViewhighlight.setAdapter(highlistAdapter);

        recyclerViewhighlight.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisible = layoutManager.findFirstVisibleItemPosition();
                    Log.d("issuescrolling", "onScrolled: " + firstVisible);
                    highlistAdapter.handleScroll(firstVisible);
                }
            }
        });
    }


    @Override
    public void onCountDownVisibilityChanged(boolean isVisible) {
        if (isVisible && !MyApplication.getFirebaseRemoteConfig().getBoolean("hide_countDown")) {
            countDown.setVisibility(View.VISIBLE);
            startCountdown();
        }
    }

    private void getData() {
        if (MyApplication.isCompetitiveProfile()) {
            getDataCompetitive();
        } else {
            getDataNonCompetitive();
        }
    }

    private void getDataCompetitive() {
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        Map<String, Object> map = new HashMap<>();
        map.put("examId", examId);
        map.put("callingAfter25Dec", true);

        Log.d(TAG, "Competitive routine request: " + map.toString());
        MyJsonArrayRequest request = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(HALF_YEARLY_TODAY_ROUTINE, map), null,
                response -> {
                    try {
                        Log.d(TAG, "Competitive routine response: " + response);
                        if (isFinishing() || isDestroyed()) return;
                        Gson gson = new Gson();
                        TypeToken<ArrayList<HalfYearlySection>> token = new TypeToken<ArrayList<HalfYearlySection>>() {
                        };
                        ArrayList<HalfYearlySection> sectionList = gson.fromJson(response.toString(), token.getType());
                        if (sectionList != null) {
                            MyApplication.saveData(PreferenceKeys.HIDE_EXAM_NEW_BADGE.getKey(), true);
                            scrollView.setVisibility(View.VISIBLE);
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            onCompetitiveDataLoaded(sectionList);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(back, error.getMessage(), 3);
            }
        });
        request.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void onCompetitiveDataLoaded(ArrayList<HalfYearlySection> sections) {
        this.competitiveSections = sections;
        checkForDemoLectureCompetitive(sections);
//        for (HalfYearlySection section : sections) {
//            if (section.getHalfYearlyPopupData() != null && section.getOngoingDay() != null) {
//                handleBottomSheet(section.getOngoingDay(), section.getHalfYearlyPopupData());
//                break;
//            }
//        }
        competitiveAdapter.updateData(sections);
    }

    public static void checkForDemoLectureCompetitive(ArrayList<HalfYearlySection> sections) {
        if (sections == null) return;
        for (HalfYearlySection section : sections) {
            ArrayList<HalfYearlyItem> items = section.getSectionItems();
            if (items == null) continue;
            List<HalfYearlyItem> completedDemos = new ArrayList<>();
            Iterator<HalfYearlyItem> iterator = items.iterator();
            while (iterator.hasNext()) {
                HalfYearlyItem item = iterator.next();
                if (item.isDemoLecture()) {
                    String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
                    String key = userId + "_" + item.getMicroLectureId();
                    boolean isCompletedStored = MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(key);
                    item.setIsCompleted(isCompletedStored);
                    if (isCompletedStored) {
                        completedDemos.add(item);
                        iterator.remove();
                    }
                }
            }
            if (!completedDemos.isEmpty()) {
                items.addAll(completedDemos);
            }
        }
    }

    private void getDataNonCompetitive() {

        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        int batchId = MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey());
        int batchAddOnId = NumberUtils.isNumber(examId) ? Integer.parseInt(examId) : 0;
        Log.d("examIdCheck", "batchId=" + batchId + " batchAddOnId=" + batchAddOnId);
//        map.put("batchId", batchId);
        map.put("batchAddOnId", batchAddOnId);

        Log.d(TAG, "Crash course request: " + map.toString());
        MyJsonObjectRequest getDataRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_CRASH_COURSE, map), null,
                response -> {
                    try {
                        Log.d(TAG, "Crash course response: " + response);
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        TypeToken<CrashCourseResponse> token = new TypeToken<CrashCourseResponse>() {
                        };
                        CrashCourseResponse crashCourseResponse = gson.fromJson(response.toString(), token.getType());
                        Log.d(TAG, "---> Response parsed");
                        if (crashCourseResponse != null) {
                            Log.d(TAG, "------> " + crashCourseResponse.toString());
                            MyApplication.saveData(PreferenceKeys.HIDE_EXAM_NEW_BADGE.getKey(), true);
                            scrollView.setVisibility(View.VISIBLE);
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            onCrashCourseDataLoaded(crashCourseResponse);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(back, error.getMessage(), 3);
            }
        });
        getDataRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(getDataRequest);
    }

    private void onCrashCourseDataLoaded(CrashCourseResponse response) {
        this.crashCourseResponse = response;
        if (response.getTodayClasses() != null &&
                !CollectionUtils.isEmpty(response.getTodayClasses().getClasses())) {
            checkForDemoLecture(response.getTodayClasses().getClasses());
        }
        halfYearlyAdapter.updateData(response);
    }

    public static void checkForDemoLecture(List<TodayClassItemResponse> liveClassItems) {
        List<TodayClassItemResponse> completedDemos = new ArrayList<>();
        java.util.Iterator<TodayClassItemResponse> iterator = liveClassItems.iterator();
        while (iterator.hasNext()) {
            TodayClassItemResponse item = iterator.next();
            if (item.isDemoLecture()) {
                String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
                String key = userId + "_" + item.getMicroLectureId();
                boolean isCompletedStored = MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(key);
                item.setIsCompleted(isCompletedStored);
                if (isCompletedStored) {
                    completedDemos.add(item);
                    iterator.remove();
                }
            }
        }
        if (!completedDemos.isEmpty()) {
            liveClassItems.addAll(completedDemos);
            int index = 1;
            for (TodayClassItemResponse item : liveClassItems)
                item.setPosition(index++);
        }
    }

    private void updateStatus(String routineId) {
        if (StringUtils.isEmpty(routineId)) return;
        Log.d(PreferenceKeys.UPDATE_STATUS.getKey(), "api call");
        Map<String, Object> map = new HashMap<>();
        map.put("routineId", routineId);
        map.put("routineStatus", "COMPLETED");
        map.put("videoProgress", 0);
        MyJsonObjectRequest taskUpdateRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(HALF_YEARLY_USER_TASK_UPDATE, map), null,
                response -> {
                    Log.d(PreferenceKeys.UPDATE_STATUS.getKey(), "success");
                    MyApplication.saveData(PreferenceKeys.UPDATE_STATUS.getKey(), true);
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.d(PreferenceKeys.UPDATE_STATUS.getKey(), "error");
                CommonMethods.showNegativeSnackbar(back, error.getMessage());
            }
        });
        taskUpdateRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(taskUpdateRequest);
    }

    private void startCountdown() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void updateCountdown() {
        Calendar now = Calendar.getInstance();
        long currentTimeInMillis = now.getTimeInMillis();

        Calendar targetTime = Calendar.getInstance();
        targetTime.set(Calendar.HOUR_OF_DAY, 24);
        targetTime.set(Calendar.MINUTE, 0);
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);

        if (currentTimeInMillis > targetTime.getTimeInMillis()) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        long targetTimeInMillis = targetTime.getTimeInMillis();
        long countdownMillis = targetTimeInMillis - currentTimeInMillis;

        long hours = TimeUnit.MILLISECONDS.toHours(countdownMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(countdownMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(countdownMillis) % 60;


        String textColor = "";
        String backgroundColor = "";

        if (hours < 6) {
            //red
            textColor = "#ED5588";
            backgroundColor = "#FBDDE7";
        } else if (hours < 12) {
            //yellow
            textColor = "#FABC4B";
            backgroundColor = "#FEF2DB";
        } else {
            //green
            textColor = "#28BC84";
            backgroundColor = "#D4F1E6";
        }

        hourCountDown.setTextColor(Color.parseColor(textColor));
        minuteCountDown.setTextColor(Color.parseColor(textColor));
        secondCountDown.setTextColor(Color.parseColor(textColor));

        hourCountDownBackground.setCardBackgroundColor(Color.parseColor(backgroundColor));
        minuteCountDownBackground.setCardBackgroundColor(Color.parseColor(backgroundColor));
        secondCountDownBackground.setCardBackgroundColor(Color.parseColor(backgroundColor));


        hourCountDown.setText(String.format(Locale.ENGLISH, "%02dh", hours));
        minuteCountDown.setText(String.format(Locale.ENGLISH, "%02dm", minutes));
        secondCountDown.setText(String.format(Locale.ENGLISH, "%02ds", seconds));
    }

    @Override
    protected void onResume() {
        super.onResume();
//        startCountdown();
        if (MyApplication.getBoolData(PreferenceKeys.UPDATE_STATUS.getKey())) {
            MyApplication.saveData(PreferenceKeys.UPDATE_STATUS.getKey(), false);
            getData();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null)
            handler.removeCallbacksAndMessages(null);

    }

    @Override
    protected void onDestroy() {
        try {
            if (bottomSheetDialog != null && bottomSheetDialog.isAdded()) {
                bottomSheetDialog.dismiss();
                bottomSheetDialog = null;
            }
            if (playStoreDialog != null && playStoreDialog.isShowing()) {
                playStoreDialog.dismiss();
            }
        } catch (IllegalStateException e) {
            Log.e("HalfYearlyActivity", "Error dismissing dialogs: " + e.getMessage());
        }

        super.onDestroy();
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
    }

    private void handleBottomSheet(int day, HalfYearlyPopupData halfYearlyPopupData) {
        if (isFinishing() || isDestroyed() || getSupportFragmentManager().isDestroyed()) {
            return;
        }
        bottomSheetDialog = new HalfYearlyBottomSheetDialog(this, this, null, null, day, false, halfYearlyPopupData);
        bottomSheetDialog.setCancelable(false);

        if (!getSupportFragmentManager().isStateSaved() && !bottomSheetDialog.isAdded()) {
            bottomSheetDialog.show(getSupportFragmentManager(), "");
        }
    }

    private void navigateToSubscriptionLockScreen(SubscriptionLockPopupDto subscriptionLockPopupDto) {
//        Intent intent = new Intent(this, CampaignSubscriptionActivity.class);
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.HOME_LOCKED_POPUP);
        if (subscriptionLockPopupDto != null) {
            intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
        }
        subscriptionScreenLauncher.launch(intent);
    }

    private void setDemoVideoOpen(String chapterCode, String chapterId) {
        try {

            Map<String, Object> requestParameters = new HashMap<>();
            requestParameters.put("chapterId", chapterId);
            Log.d(TAG, "Progress API Request: " + requestParameters.toString());

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(POST_CHAPTER_DEMO_PROGRESS, requestParameters), null,
                    response -> {
                        Log.d(TAG, "Progress API Response: " + response.toString());
                        if (CommonMethods.isActivitySafe(this)) {
                            getData();
                        } else {
                            Log.w(TAG, "Fragment not in valid state, skipping fetchTodayClassData()");
                        }
                    }, error -> {
                Log.d(TAG, "Progress API Error Response: " + error.toString());
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCloseButtonClick() {
        if (bottomSheetDialog != null && bottomSheetDialog.isAdded()) {
            bottomSheetDialog.dismiss();
        }
    }

    @Override
    public void onStartNowClick(String className, String activityParams) {
        bottomSheetDialog.dismiss();
//        String url = MyApplication.getFirebaseRemoteConfig().getString(getString(R.string.routine_pdf) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
//        if (StringUtils.isNotEmpty(url))
//            openPdf(url);
    }

    @Override
    public void onItemClick(TodayClassItemResponse item) {
        clickedHorizontalItem = item;
        if (item.getContentType() == HalfYearlyContentType.MICROLECTURE_VIDEO) {
            if (item.isLocked() && item.isDemoLecture()) {
                navigateToSubscriptionLockScreen(item.getSubscriptionLockPopupDto());
            } else {
                playVideo(item);
                if (item.isDemoLecture()) {
                    MyApplication.saveSharedPreferencesDoNotDelete(
                            MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_" + item.getMicroLectureId(), true);
                    setDemoVideoOpen(item.getChapterCode(), item.getChapterId());
                }
            }
        } else if (item.getContentType() == HalfYearlyContentType.NOTES) {
            if (StringUtils.isNotEmpty(item.getNotesPdfUrl())) {
                openPdf(item.getNotesPdfUrl(), "lecture.notes", item.getSubject(), item.getTitle());
            }
        }
    }

    @Override
    public void onItemClick(HalfYearlyItem item) {
        clickedCompetitiveItem = item;
        if (item.getContentType() == HalfYearlyContentType.MICROLECTURE_VIDEO) {
            if (item.isLocked() && item.isDemoLecture()) {
                navigateToSubscriptionLockScreen(item.getSubscriptionLockPopupDto());
            } else {
                playVideoCompetitive(item);
                if (item.isDemoLecture()) {
                    MyApplication.saveSharedPreferencesDoNotDelete(
                            MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_" + item.getMicroLectureId(), true);
                    setDemoVideoOpen(item.getChapterCode(), item.getChapterId());
                }
            }
        } else if (item.getContentType() == HalfYearlyContentType.TEST_SERIES) {
            Intent intent = new Intent(this, ActivityTestIntro.class);
            intent.putExtra("Id", item.getContent());
            intent.putExtra("title", item.getTitle());
            startActivity(intent);
        } else if (item.getContentType() == HalfYearlyContentType.NOTES) {
            if (item.getNotesData() != null && StringUtils.isNotEmpty(item.getNotesData().getNotesUrl())) {
                openPdf(item.getNotesData().getNotesUrl(), item.getNotesData().getNotesType(),
                        item.getNotesData().getNotesSubjectName(), item.getTitle());
            }
        }
    }

    @Override
    public void showPopUp(TodayClassAdapter.TodayClassPopUpTypes type, HalfYearlyItem item) {
        try {
            if (type.equals(TodayClassAdapter.TodayClassPopUpTypes.LIVE_UPCOMING)) {
                if (item != null && item.getChapterLiveDto() != null && item.getChapterLiveDto().getUpcomingStatusPopUp() != null) {
                    showLiveUpcomingDialog(item.getChapterLiveDto().getUpcomingStatusPopUp(),item.getChapterStatus());
                }
            } else if (type.equals(TodayClassAdapter.TodayClassPopUpTypes.LOCK_POPUP)) {
                if (item != null && item.isLocked()) {
                    showLockDialog(item.getSubscriptionLockPopupDto());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void showPopUp(TodayClassAdapter.TodayClassPopUpTypes type, TodayClassItemResponse item) {
        try {
            if (type.equals(TodayClassAdapter.TodayClassPopUpTypes.LIVE_UPCOMING)) {
                if (item != null && item.getChapterLiveDto() != null && item.getChapterLiveDto().getUpcomingStatusPopUp() != null) {
                    showLiveUpcomingDialog(item.getChapterLiveDto().getUpcomingStatusPopUp(), item.getChapterStatus());
                }
            } else if (type.equals(TodayClassAdapter.TodayClassPopUpTypes.LOCK_POPUP)) {
                if (item != null && item.isLocked()) {
                    showLockDialog(item.getSubscriptionLockPopupDto());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onSectionClick(Object item) {
        try {
            if (item instanceof PreExamContentResponse content) {
                handleContentNavigation(content);
            } else if (item instanceof PreExamSubjectResponse subject) {
                handleSubjectNavigation(subject);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onSectionClick(HalfYearlyItem item) {
        try {
            HalfYearlyContentType type = item.getContentType();
            if (type == null) type = HalfYearlyContentType.CLASS_PARAMS;
            switch (type) {
                case CHAPTER_LIST:
                case CHAPTER_LIST_NEW: {
                    ActivityNavigationConfig navigationConfig = MyApplication.getBottomNavActivityConfig();
                    String navTitle = getString(R.string.lectures);
                    if (navigationConfig != null && navigationConfig.getParams() != null) {
                        navTitle = (String) navigationConfig.getParams().getOrDefault("title", getString(R.string.lectures));
                    }
//                    CommonNavigator.navigateToChapterListActivity(this,navTitle,
//                            item.getContentId(), item.getTitle(), null, TAG, StringUtils.isNotEmpty(item.getBoardCode()) ? item.getBoardCode() : "", null);
                    CommonNavigator.navigateToChapterListByBatchActivity(this, item.getContentId(), item.getTitle(), null, TAG, navTitle, MyApplication.isCompetitiveProfile()?"CHHALAANG1.0":"");
//                    Intent intent = new Intent(this, ChapterListByBatchActivity.class);
//                    intent.putExtra("SubjectName", item.getTitle());
//                    intent.putExtra("SubjectId", item.getContentId());
//                    intent.putExtra("title", StringUtils.isEmpty(navTitle) ? getString(R.string.lectures) : navTitle);
//                    intent.putExtra("preExamPreparation", true);
//                    intent.putExtra("boardCode", StringUtils.isNotEmpty(item.getBoardCode()) ? item.getBoardCode() : "");
//                    startActivity(intent);
                    break;
                }
                case TEST_SERIES: {
                    CommonNavigator.navigateToTestSeriesListActivity(this,
                            item.getContent(), item.getTitle(),
                            null, true, null, false, false, TAG);
                    break;
                }
                case ALL_NOTES_LIST: {
                    Intent intent = new Intent(this, ToppersNotesActivity.class);
                    intent.putExtra("overall", false);
                    intent.putExtra("preExamPreparation", true);
                    intent.putExtra("categoryId", item.getContentId());
                    intent.putExtra("screenTitle", item.getTitle());
                    startActivity(intent);
                    break;
                }
                case NOTES_LIST:
                case NOTES: {
                    Intent intent = new Intent(this, ToppersNotesActivity.class);
                    intent.putExtra("overall", true);
                    intent.putExtra("preExamPreparation", true);
                    intent.putExtra("categoryId", item.getContentId());
                    intent.putExtra("screenTitle", item.getTitle());
                    startActivity(intent);
                    break;
                }
                case PHYSICS_NOTES:
                case CHEMISTRY_NOTES:
                case BIOLOGY_NOTES:
                case MATHEMATICS_NOTES:
                case TIME_TABLE: {
                    Map<String, String> params = new HashMap<>();
                    params.put("clicked", type.name());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.pre_exam_pdf_clicked.getActionName(), params);
                    if (StringUtils.isNotEmpty(item.getContent())) {
                        openPdf(item.getContent(), "lecture.notes", null, item.getTitle());
                    }
                    break;
                }
                case ASK_DOUBT: {
                    Intent intent = new Intent(this, ChatScreenChooseSubjectForChat.class);
                    intent.putExtra("subscriptionActive", true);
                    HashMap<String, String> eventParams = new HashMap<>();
                    eventParams.put("source", TAG);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.click_ask_doubt.getActionName(), eventParams);
                    startActivity(intent);
                    break;
                }
                default: {
                    HashMap<String, String> eventParams = new HashMap<>();
                    eventParams.put("class_name", item.getClassName());
                    eventParams.put("title", item.getTitle());
                    if (item.getActivityParams() != null)
                        eventParams.put("class_params", item.getActivityParams().toString());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.crash_course_dynamic_content_clicked.getActionName(), eventParams);
                    CommonMethods.navigateFromActivityConfig(this, item.getNavigationType(), item.getClassName(), item.getActivityParams());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleContentNavigation(PreExamContentResponse content) {
        HalfYearlyContentType type = HalfYearlyContentType.fromString(content.getContentType());
        switch (type) {
            case CHAPTER_LIST, CHAPTER_LIST_NEW: {
                ActivityNavigationConfig navigationConfig = MyApplication.getBottomNavActivityConfig();
                String title = getString(R.string.lectures);
                if (navigationConfig != null && navigationConfig.getParams() != null) {
                    title = (String) navigationConfig.getParams().getOrDefault("title", getString(R.string.lectures));
                }

                CommonNavigator.navigateToChapterListByBatchActivity(this, content.getCategoryId(), content.getTitle(), null, TAG, title, null);
//                Intent intent = new Intent(this, ChapterListByBatchActivity.class);
//                intent.putExtra("SubjectName", content.getTitle());
//                intent.putExtra("SubjectId", content.getCategoryId());
//                intent.putExtra("title", StringUtils.isEmpty(title) ? getString(R.string.lectures) : title);
//                intent.putExtra("preExamPreparation", true);
//                intent.putExtra("boardCode", MyApplication.isCompetitiveProfile()?"CHHALAANG1.0":"");
//                startActivity(intent);
                break;
            }
            case TEST_SERIES: {
                CommonNavigator.navigateToTestSeriesListActivity(this,
                        content.getContent(), content.getTitle(),
                        null, true, null, false, false, TAG);
                break;
            }
            case ALL_NOTES_LIST: {
                Intent intent = new Intent(this, ToppersNotesActivity.class);
                intent.putExtra("overall", false);
                intent.putExtra("preExamPreparation", true);
                intent.putExtra("categoryId", content.getCategoryId());
                intent.putExtra("screenTitle", content.getTitle());
                startActivity(intent);
                break;
            }
            case NOTES_LIST:
            case NOTES: {
                Intent intent = new Intent(this, ToppersNotesActivity.class);
                intent.putExtra("overall", true);
                intent.putExtra("preExamPreparation", true);
                intent.putExtra("categoryId", content.getCategoryId());
                intent.putExtra("screenTitle", content.getTitle());
                startActivity(intent);
                break;
            }
            case PHYSICS_NOTES:
            case CHEMISTRY_NOTES:
            case BIOLOGY_NOTES:
            case MATHEMATICS_NOTES:
            case TIME_TABLE: {
                Map<String, String> params = new HashMap<>();
                params.put("clicked", type.name());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.pre_exam_pdf_clicked.getActionName(), params);
                if (StringUtils.isNotEmpty(content.getContent())) {
                    openPdf(content.getContent(), "lecture.notes", null, content.getTitle());
                }
                break;
            }
            case ASK_DOUBT: {
                Intent intent = new Intent(this, ChatScreenChooseSubjectForChat.class);
                intent.putExtra("subscriptionActive", true);
                HashMap<String, String> eventParams = new HashMap<>();
                eventParams.put("source", TAG);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.click_ask_doubt.getActionName(), eventParams);
                startActivity(intent);
                break;
            }
            case CLASS_PARAMS:
            default: {
                HashMap<String, String> eventParams = new HashMap<>();
                eventParams.put("class_name", content.getClassName());
                eventParams.put("title", content.getTitle());
                if (content.getActivityParams() != null)
                    eventParams.put("class_params", content.getActivityParams().toString());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.crash_course_dynamic_content_clicked.getActionName(), eventParams);
                CommonMethods.navigateFromActivityConfig(this, content.getNavigationType(), content.getClassName(), content.getActivityParams());
                break;
            }
        }
    }

    private void handleSubjectNavigation(PreExamSubjectResponse subject) {
        Intent intent = new Intent(this, ChapterListByBatchActivity.class);
        ActivityNavigationConfig navigationConfig = MyApplication.getBottomNavActivityConfig();
        String title = null;
        if (navigationConfig != null && navigationConfig.getParams() != null) {
            title = (String) navigationConfig.getParams().getOrDefault("title", getString(R.string.lectures));
        }
        intent.putExtra("SubjectName", subject.getName());
        intent.putExtra("SubjectId", subject.getSubjectId());
        intent.putExtra("SubjectCode", subject.getSubjectCode());
        intent.putExtra(PreferenceKeys.BOARD_CODE.getKey(), subject.getBoardCode());
        intent.putExtra("title", StringUtils.isEmpty(title) ? getString(R.string.lectures) : title);
        intent.putExtra("preExamPreparation", true);
        intent.putExtra("Screen", TAG);
        Map<String, String> params = new HashMap<>();
        params.put("SubjectName", subject.getName());
        params.put("SubjectId", subject.getSubjectId());
        params.put("Screen", TAG);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.subject_click.getActionName(), params);
        startActivity(intent);
    }

    private void showLiveUpcomingDialog(ChapterPopUpDto chapterPopUpDto, String chapterStatus) {
        try {
            if (chapterPopUpDto == null)
                return;
            dismissLiveUpcomingDialog();
            lectureLiveUpcomingDialog = CommonMethods.showGenericPopupInformation(this, chapterPopUpDto.getTitle(), chapterPopUpDto.getSubTitle(), chapterPopUpDto.getBtnName(),chapterStatus);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showLockDialog(SubscriptionLockPopupDto subscriptionLockPopupDto) {
        try {
            Log.d(TAG, "----> Dialog Lock: ");
            if (subscriptionLockPopupDto == null)
                subscriptionLockPopupDto = new SubscriptionLockPopupDto();
            dismissLockPopUpDialog();
            lockPopUpDialog = CommonMethods.showLockPopupDialog(this, subscriptionLockPopupDto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void playVideo(TodayClassItemResponse item) {
        Intent intent = new Intent(this, MicrolectureListActivity.class);
        intent.putExtra("chapterId", item.getChapterCode() != null ? item.getChapterCode() : item.getChapterId());
        intent.putExtra("chapterName", item.getTitle() != null ? item.getTitle() : item.getChapterName());
        intent.putExtra("hideTapTarget", true);
        intent.putExtra("microLectureId", item.getMicroLectureId());
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), item.getMicroLectureId());
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);
        intent.putExtra("openVideo", true);
        intent.putExtra("preExamPreparation", true);
        MyApplication.saveData(item.getTitle() + PreferenceKeys.TYPE.getKey(), 0);
        startActivity(intent);
    }

    private void playVideoCompetitive(HalfYearlyItem item) {
        Intent intent = new Intent(this, MicrolectureListActivity.class);
        intent.putExtra("chapterId", StringUtils.isNotEmpty(item.getChapterCode()) ? item.getChapterCode() : item.getChapterId());
        intent.putExtra("chapterName", StringUtils.isNotEmpty(item.getTitle()) ? item.getTitle() : item.getChapterName());
        intent.putExtra("hideTapTarget", true);
        intent.putExtra("microLectureId", item.getMicroLectureId());
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), item.getMicroLectureId());
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);
        intent.putExtra("openVideo", true);
        intent.putExtra("preExamPreparation", true);
        MyApplication.saveData(item.getTitle() + PreferenceKeys.TYPE.getKey(), 0);
        startActivity(intent);
    }

    public void openPdf(String url, String notesType, String subjectName, String title) {
        try {
            PdfUtils.openPdfViewer(this, url, title, notesType, subjectName, false, "", "");
//            Intent target = new Intent(Intent.ACTION_VIEW);
//            target.setDataAndType(Uri.parse(url), "application/pdf");
//            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//
//            if (target.resolveActivity(requireActivity().getPackageManager()) != null) {
//                Intent intent = Intent.createChooser(target, "Open File");
//                startActivity(intent);
//            } else {
//                playStoreDialog = new PlayStoreDialog(getActivity());
//                playStoreDialog.showPlayStoreDialog();
//            }
        } catch (ActivityNotFoundException e) {
            Log.d("openPdf:", e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Status update handled elsewhere for new API
    }

    @Override
    public void onHighlightsClick(int position) {
        try {
            if (highlistModelList != null && position < highlistModelList.size()) {
                HighlistModel highlistModel = highlistModelList.get(position);
                VideoOrientationType videoType = VideoOrientationType.fromString(highlistModel.getVideoType());
                Intent intent;
                switch (videoType) {
                    case REELS:
                    case PORTRAIT:
                        intent = new Intent(this, ReelsActivity.class);
                        intent.putExtra("viral_video_url", highlistModel.getVideoUrl());
                        break;
                    case VIDEO:
                    case LANDSCAPE:
                    case HORIZONTAL:
                    default:
                        intent = new Intent(this, ExoPlayerActivity.class);
                        intent.putExtra(KEY_VIDEO_URI, highlistModel.getVideoUrl());
                        break;
                }
                startActivity(intent);
                Map<String, String> params = new HashMap<>();
                params.put("videoUrl", highlistModel.getVideoUrl());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_highlight_section.getActionName(), params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void highlightExtraction() {
        // Initialize RecyclerView & Adapter once
        setUpViralVideo();
        // Show shimmer before loading
        shimmerHighlights.startShimmer();
        shimmerHighlights.setVisibility(View.VISIBLE);
        recyclerViewhighlight.setVisibility(View.GONE);
        List<HighlistModel> cachedHighlights =
                MyApplication.getHighlistData(HomeScreenCacheData.AV_HIGHLIGHT_CACHE_KEY_HALF_YEARLY.name());
        if (cachedHighlights != null && !cachedHighlights.isEmpty()) {
            Log.d("HighlightExtraction", "Using cached highlights.");
            highlistModelList.clear();
            highlistModelList.addAll(cachedHighlights);
            highlistAdapter.notifyDataSetChanged();

            recyclerViewhighlight.setVisibility(View.VISIBLE);
            shimmerHighlights.stopShimmer();
            shimmerHighlights.setVisibility(View.GONE);
        }

        if (MyApplication.getFirebaseRemoteConfig().getBoolean("half_yearly_screen_viral_video_new_api_enabled")) {
            // Prepare API request params
            Map<String, Object> map = new HashMap<>();
//        map.put("batchId", MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey()));
            map.put("type", BatchVideoType.CRASH_COURSE.name());
            Log.d(TAG, "Viral Videos Request: " + map.toString());
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(AV_DASHBOARD_VIDEO, map),
                    null,
                    response -> {
                        try {
                            Log.d(TAG, "Viral Videos Response: " + response);
                            JSONArray arrayData = response.getJSONArray("data");
                            Gson gson = new Gson();
                            TypeToken<List<VideoResponse>> token = new TypeToken<List<VideoResponse>>() {
                            };
                            List<VideoResponse> videoList = gson.fromJson(arrayData.toString(), token.getType());
                            Log.d("HighlightExtraction", "API CALL Highlights: " + videoList + " --map " + map);

                            shimmerHighlights.stopShimmer();
                            shimmerHighlights.setVisibility(View.GONE);

                            if (videoList == null || videoList.isEmpty()) {
                                recyclerViewhighlight.setVisibility(View.GONE);
                                viral_video_section.setVisibility(View.GONE);
                                return;
                            }

                            List<HighlistModel> list = new ArrayList<>();
                            for (VideoResponse video : videoList) {
                                HighlistModel model = new HighlistModel();
                                model.setId(video.getId() != null ? String.valueOf(video.getId()) : null);
                                model.setImageUrl(video.getImageUrl());
                                model.setVideoUrl(video.getVideoUrl());
                                model.setVideoType(video.getOrientation());
                                list.add(model);
                            }

                            MyApplication.saveHighlightsData(HomeScreenCacheData.AV_HIGHLIGHT_CACHE_KEY_HALF_YEARLY.name(), list);
                            highlistModelList.clear();
                            highlistModelList.addAll(list);
                            highlistAdapter.notifyDataSetChanged();

                            recyclerViewhighlight.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            Log.e("HighlightExtraction", "Error parsing response", e);
                            shimmerHighlights.stopShimmer();
                            shimmerHighlights.setVisibility(View.GONE);
                        }
                    },
                    new VolleyGlobalErrorListener(this) {
                        @Override
                        public void postError(VolleyError error) {
                            shimmerHighlights.stopShimmer();
                            shimmerHighlights.setVisibility(View.GONE);
                            CommonMethods.showNegativeSnackbar(scrollView, error.getMessage());
                        }
                    });

            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } else {
            Map<String, Object> map = new HashMap<>();
            String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            map.put(PreferenceKeys.USER_ID.getKey(), userId);
            map.put("forNewUser", true);
            map.put(PreferenceKeys.BOARD_CODE.getKey(), MyApplication.getStringData(PreferenceKeys.BOARD_CODE.getKey()));
            map.put("screenName", TAG);

            Log.d(TAG, "Viral Video Old Request: " + map.toString());
            MyJsonArrayRequest myJsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(GET_HIGHLIGHT_WEEK, map),
                    null,
                    response -> {
                        Log.d(TAG, "Viral Video Old Response: " + response.toString());
                        TypeToken<List<HighlistModel>> token = new TypeToken<List<HighlistModel>>() {
                        };
                        Gson gson = new Gson();
                        List<HighlistModel> list = gson.fromJson(response.toString(), token.getType());
                        Log.d("HighlightExtraction", "API CALL Highlights: " + list + " --map " + map);

                        shimmerHighlights.stopShimmer();
                        shimmerHighlights.setVisibility(View.GONE);

                        if (list == null || list.isEmpty()) {
                            recyclerViewhighlight.setVisibility(View.GONE);
                            viral_video_section.setVisibility(View.GONE);
                            return;
                        }
                        MyApplication.saveHighlightsData(HomeScreenCacheData.AV_HIGHLIGHT_CACHE_KEY_HALF_YEARLY.name(), list);
                        highlistModelList.clear();
                        highlistModelList.addAll(list);
                        highlistAdapter.notifyDataSetChanged();

                        recyclerViewhighlight.setVisibility(View.VISIBLE);
                    },
                    new VolleyGlobalErrorListener(this) {
                        @Override
                        public void postError(VolleyError error) {
                            shimmerHighlights.stopShimmer();
                            shimmerHighlights.setVisibility(View.GONE);
                            CommonMethods.showNegativeSnackbar(scrollView, error.getMessage());
                        }
                    });
            myJsonArrayRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonArrayRequest);
        }
    }

    private void setTalkToExpert() {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getTalkToExpert() {

        try {
            String jsonString = MyApplication.getFirebaseRemoteConfig()
                    .getString("half_yearly_talk_to_expert");

            if (StringUtils.isEmpty(jsonString)) {
                return "";
            }
            JSONObject root = new JSONObject(jsonString);

            String courseId = MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey());
            String languageCode = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

            if (StringUtils.isEmpty(courseId)) {
                return "";
            }

            if (StringUtils.isEmpty(languageCode)) {
                languageCode = ArivihanLanguageEnum.ENGLISH.getCode();
            }

            JSONObject courseObject = root.optJSONObject(courseId);
            if (courseObject == null) {
                return "";
            }

            return courseObject.optString(languageCode, "");

        } catch (Exception e) {
            return "";
        }
    }

    private String getBoardCode() {
        String boardCode = "";
        try {
            FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();

            String json = remoteConfig.getString("board_code_for_half_yearly_lecture");

            if (json == null || json.trim().isEmpty()) {
                Log.w("BoardCode", "Remote Config JSON is empty");
                boardCode = "";
            }

            JSONObject jsonObject = new JSONObject(json);

            String courseId = MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey());

            if (courseId == null || courseId.trim().isEmpty()) {
                Log.w("BoardCode", "Selected Course ID is null or empty");
                boardCode = "";
            }

            boardCode = jsonObject.optString(courseId, "");

            Log.d("BoardCode", "Fetched boardCode = " + boardCode);

        } catch (Exception e) {
            Log.e("BoardCode", "Failed to parse JSON", e);
            boardCode = "";
        }
        return boardCode;
    }

    private void dismissLiveUpcomingDialog() {
        if (lectureLiveUpcomingDialog != null && lectureLiveUpcomingDialog.isShowing()) {
            lectureLiveUpcomingDialog.dismiss();
            lectureLiveUpcomingDialog = null;
        }
    }

    private void dismissLockPopUpDialog() {
        if (lockPopUpDialog != null && lockPopUpDialog.isShowing()) {
            lockPopUpDialog.dismiss();
            lockPopUpDialog = null;
        }
    }

    @Override
    public void hideContinueSection() {

    }

    @Override
    public void hideBadgeView() {

    }

    @Override
    public void onAnimatedCardTagClicked(String tag) {

    }

    @Override
    public void onDataFetched(TodayTaskFragment source, int size) {

    }

}