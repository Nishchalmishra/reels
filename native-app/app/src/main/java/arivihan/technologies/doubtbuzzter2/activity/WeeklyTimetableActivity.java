package arivihan.technologies.doubtbuzzter2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.TimeTableAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.SubscriptionPopupType;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.ApiWrappedTimeTableData;
import arivihan.technologies.doubtbuzzter2.model.UiSession;
import arivihan.technologies.doubtbuzzter2.model.UiTimetableDay;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class WeeklyTimetableActivity extends AppCompatActivity {

    private static final String TAG = "WeeklyTimetable";
    private static final String REQUEST_TAG = "WEEKLY_TIMETABLE";

    private final Gson gson = new Gson();

    // ── Views ─────────────────────────────────────────────────────────────────
    private TimeTableAdapter adapter;
    private TextView toolbarTitle;
    private RecyclerView recyclerView;
    private LinearLayout wtt_l_shimmer;
    private ShimmerFrameLayout wtt_s_shimmer;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_timetable);

        bindViews();
        setupToolbar();
        setupRecyclerView();

        loadTimetable();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // View binding
    // ─────────────────────────────────────────────────────────────────────────

    private void bindViews() {
        toolbarTitle = findViewById(R.id.toolbar_title);
        wtt_l_shimmer = findViewById(R.id.wtt_l_shimmer);
        wtt_s_shimmer = findViewById(R.id.wtt_s_shimmer);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupToolbar() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.weekly_timetable_back_btn_click.getActionName());
            getOnBackPressedDispatcher().onBackPressed();
        });
        String title = getIntent().getStringExtra("toolbar_title");
        toolbarTitle.setText((title != null && !title.isEmpty()) ? title : "Weekly Time Table");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API  (DEBUG → fake data; RELEASE → real Volley call)
    // ─────────────────────────────────────────────────────────────────────────

    private void loadTimetable() {
        showLoading();


        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(UrlRoutes.GET_WEEKLY_TIMETABLE, new HashMap<>()),
                null,
                response -> {
                    // (B) Guard: response may arrive after activity is finishing/destroyed
                    if (isFinishing() || isDestroyed()) return;
                    try {
                        // (A) gson.fromJson() can return null — guard before dereferencing
                        ApiWrappedTimeTableData apiResponse = gson.fromJson(response.toString(), ApiWrappedTimeTableData.class);
                        if (apiResponse == null) {
                            showError(getString(R.string.something_went_wrong));
                            return;
                        }

                        if (!apiResponse.isSuccess()) {
                            showError(apiResponse.getMessage());
                            return;
                        }

                        List<UiTimetableDay> rawItems = (apiResponse.getData() != null
                                && apiResponse.getData().getDays() != null)
                                ? apiResponse.getData().getDays()
                                : new ArrayList<>();

                        // (A2) Filter out days that have no scheduled sessions to avoid empty cards
                        List<UiTimetableDay> items = new ArrayList<>();
                        for (UiTimetableDay day : rawItems) {
                            if (day != null && day.getSessions() != null && !day.getSessions().isEmpty()) {
                                items.add(day);
                            }
                        }

                        if (items.isEmpty()) {
                            showEmpty();
                        } else {
                            markToday(items);
                            showContent(items);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "loadTimetable parse error: " + e.getMessage());
                        showError("Could not read timetable data.");
                    }
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        // (B) Guard: error callback also fires after onStop in race window
                        if (isFinishing() || isDestroyed()) return;
                        Log.e(TAG, "loadTimetable volley error: " + error.getMessage());
                        String msg = (error.networkResponse != null)
                                ? "Server error (" + error.networkResponse.statusCode + "). Try again."
                                : "No internet connection. Check your network.";
                        showError(msg);
                    }
                }
        );

        request.setTag(REQUEST_TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Marks isToday on each day before sending to adapter
    // ─────────────────────────────────────────────────────────────────────────

    private void markToday(List<UiTimetableDay> items) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        for (UiTimetableDay day : items) {
            // (C) day or day.getDate() can be null from server deserialization
            if (day == null || day.getDate() == null) continue;
            day.isToday = day.getDate().equals(today);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI states
    // ─────────────────────────────────────────────────────────────────────────

    private void showLoading() {
        wtt_l_shimmer.setVisibility(View.VISIBLE);
        wtt_s_shimmer.setVisibility(View.VISIBLE);
        wtt_s_shimmer.startShimmer();
        recyclerView.setVisibility(View.GONE);
        // (NEW) hide until the API tells us whether to show it, avoids a visibility flash
    }

    private void showContent(List<UiTimetableDay> items) {
        stopShimmer();
        recyclerView.setVisibility(View.VISIBLE);
        setupAdapter(items);
    }

    private void showEmpty() {
        stopShimmer();
        recyclerView.setVisibility(View.GONE);
    }

    private void showError(String message) {
        stopShimmer();
        recyclerView.setVisibility(View.GONE);
        // (NEW) no valid data on error → hide the missed-classes card
        // (G) surface the error to the user instead of silently discarding the message
//        if (message != null && !message.isEmpty()) {
//            showSnackbar(message);
//        }
    }


    // (H) centralised Snackbar helper — safe even if recyclerView is temporarily detached
    private void showSnackbar(String message) {
        try {
            View anchor = recyclerView.isAttachedToWindow() ? recyclerView : findViewById(android.R.id.content);
            Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "showSnackbar failed: " + e.getMessage());
        }
    }

    private void stopShimmer() {
        wtt_s_shimmer.stopShimmer();
        wtt_l_shimmer.setVisibility(View.GONE);
        wtt_s_shimmer.setVisibility(View.GONE);
    }

    private void setupAdapter(List<UiTimetableDay> uiItems) {
        adapter = new TimeTableAdapter(this, uiItems);

        adapter.setOnItemClickListener(new TimeTableAdapter.OnItemClickListener() {
            @Override
            public void onLockedClick(UiTimetableDay day, UiSession session, int index) {
                openSubscriptionScreen();
            }

            @Override
            public void onItemClick(UiTimetableDay day, UiSession session, int index) {
                Log.d(TAG, "onItemClick-session: " + session);
                if (session == null) return;



                // ── Locked ────────────────────────────────────────────────────
                if (session.isLocked()) {
                    openSubscriptionScreen();
                    return;
                }

                // ── Status null-guard ─────────────────────────────────────────
                if (session.getStatus() == null) {
                    Log.w(TAG, "onItemClick: session status is null, ignoring click");
                    return;
                }

                switch (session.getStatus()) {
                    case LIVE_SCHEDULED, UPCOMING ,RESCHEDULED: {
                        if (session.getChapterLiveDto() == null
                                || session.getChapterLiveDto().getUpcomingStatusPopUp() == null) {
                            showSnackbar("Class is not live yet. Check back soon.");
                            return;
                        }
                        String title    = session.getChapterLiveDto().getUpcomingStatusPopUp().getTitle();
                        String subTitle = session.getChapterLiveDto().getUpcomingStatusPopUp().getSubTitle();
                        String btnName  = session.getChapterLiveDto().getUpcomingStatusPopUp().getBtnName();
                        CommonMethods.showGenericPopupInformation(
                                WeeklyTimetableActivity.this,
                                title    != null ? title    : "",
                                subTitle != null ? subTitle : "",
                                btnName  != null ? btnName  : "",session.getStatus().name());
                        break;
                    }

                    case CONVERTING:
                        CommonMethods.showPositiveSnackbar(
                                recyclerView,
                                getString(R.string.aapka_lecture_thodi_der_aa_jayega),
                                3000);
                        break;

                    case PENDING:
                        // Live class ended; recording is still being processed.
                        // chapterLiveDto is null for PENDING — do NOT call openRecordedClass().
                        CommonMethods.showPositiveSnackbar(
                                recyclerView,
                                "Recording will be available shortly.",
                                3000);
                        break;

                    case LIVE: {
                        if (session.getChapterLiveDto() == null) {
                            showSnackbar("Live class details are unavailable right now.");
                            return;
                        }
                        CommonNavigator.navigateToLiveClassWebView(
                                WeeklyTimetableActivity.this,
                                session.getTitle()        != null ? session.getTitle()        : "",
                                session.getSubject()      != null ? session.getSubject()      : "",
                                "",
                                session.getChapterLiveDto().getChapterId(),
                                session.getChapterLiveDto().getChapterCode(),
                                session.getThumbnailUrl() != null ? session.getThumbnailUrl() : "",
                                session.getTeacherName()  != null ? session.getTeacherName()  : "",
                                TAG,
                                CommonMethods.mapToBundle(session.getLiveNavigationBundle()));
                        break;
                    }

                    case RECORDED:
                        openRecordedClass(session);
                        break;

                    default:
                        Log.w(TAG, "onItemClick: unhandled status " + session.getStatus());
                        break;
                }
            }

            @Override
            public void onPlayClick(UiTimetableDay day, UiSession session, int index) {
                if (session == null) return;
                openRecordedClass(session);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────────────────────────────────

    private void openSubscriptionScreen() {
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, SubscriptionPopupType.LECTURES.name(), RedirectionSource.TIMETABLE_LOCKED_POPUP);
        startActivity(intent);
    }

    private void openRecordedClass(UiSession session) {
        // (D) chapterLiveDto is null for sessions that have no recorded chapter info
        if (session == null || session.getChapterLiveDto() == null) {
            Log.w(TAG, "openRecordedClass: chapterLiveDto is null, cannot open recorded class");
//            showSnackbar(getString(R.string.something_went_wrong));
            return;
        }
        Intent intent = new Intent(this, MicrolectureListActivity.class);
        intent.putExtra("chapterName", session.getTitle() != null ? session.getTitle() : "");
        intent.putExtra("subjectCode", session.getSubject() != null ? session.getSubject() : "");
        intent.putExtra("chapterId", session.getChapterLiveDto().getChapterId());
        startActivity(intent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle — cancel in-flight request when the screen is destroyed
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onStop() {
        super.onStop();
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(REQUEST_TAG);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    public static String getDayOnly(String dateStr) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr);
            return new SimpleDateFormat("dd", Locale.getDefault()).format(d);
        } catch (Exception e) {
            return "";
        }
    }
}