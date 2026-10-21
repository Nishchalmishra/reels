
package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_TO_DO_LIST;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TODO_MAIN_SCREEN;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TOPPERS_NOTES;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.todo.DateTabAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.todo.TaskPagerAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.model.RoutineSyllabus;
import arivihan.technologies.doubtbuzzter2.model.ToDoResponse;
import arivihan.technologies.doubtbuzzter2.model.TodoDateSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TodoMessageConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: ToDoListMain Copyright 19/07/23, 1:45 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 19/07/23, 1:45 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class ToDoListMain extends BaseActivity{

    private static final String TAG = ToDoListMain.class.getSimpleName();
    private final Gson gson = new Gson();
    private List<TodoDateSelectionModel> dates = new ArrayList<>();
    private LinearLayout ll_progress_container, ll_daily_progress_marker;
    private TextView tv_daily_progress, tv_title;
    private CardView cv_info;
    private ViewPager2 view_pager;
    private RecyclerView rv_date_range;
    private ShimmerFrameLayout toDoListShimmer;
    private FloatingActionButton loadNext, loadPrev;
    Date startDate, endDate;
    private RoutineSyllabus routineSyllabus;
    private DateTabAdapter dateTabAdapter;
    private TaskPagerAdapter taskPagerAdapter;
    LinearLayout backButtonLayout;
    private MaterialCardView cv_download_timetable;
    private LinearLayout ll_no_data;
    private LottieAnimationView lottie_no_data;
    private TextView tv_no_data_header, tv_no_data_body;
    private HowToVideoView howToVideoView;
    private View how_to_video_view;
    public static enum TodoType {
        DAILY,
        WEEKLY,
        MONTHLY
    }
    private String timeTableUrl;
    boolean isUserScroll = false;
    private TodoMessageConfig todoMessageConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list_main);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getIntent().getBooleanExtra("redirect_to_home", false)) {
                    Intent intent = new Intent(ToDoListMain.this, HomeScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else if (getIntent().getBooleanExtra("redirect_to_Explore", false)) {
                    Intent intent = new Intent(ToDoListMain.this, ExploreActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    finish();
                }
            }
        });

        loadNext = findViewById(R.id.load_next);
        loadPrev = findViewById(R.id.load_prev);
        toDoListShimmer = findViewById(R.id.to_do_list_shimmer);
        tv_title = findViewById(R.id.tv_title);
        backButtonLayout = findViewById(R.id.back_button_layout);
        ll_daily_progress_marker = findViewById(R.id.ll_daily_progress_marker);
        ll_progress_container = findViewById(R.id.ll_progress_container);
        tv_daily_progress = findViewById(R.id.tv_daily_progress);
        rv_date_range = findViewById(R.id.rv_date_range);
        view_pager = findViewById(R.id.view_pager_todo);
        cv_download_timetable = findViewById(R.id.cv_download_timetable);
        cv_info = findViewById(R.id.cv_info);

        ll_no_data = findViewById(R.id.ll_no_data);
        lottie_no_data = findViewById(R.id.lottie_no_data);
        tv_no_data_header = findViewById(R.id.tv_no_data_header);
        tv_no_data_body = findViewById(R.id.tv_no_data_body);

        how_to_video_view=findViewById(R.id.how_to_video_view);

        ll_no_data.setVisibility(View.GONE);
        rv_date_range.setVisibility(View.GONE);
        ll_daily_progress_marker.setVisibility(View.GONE);
        cv_download_timetable.setVisibility(View.GONE);

        String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("todo_msg_config_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        Gson gson = new Gson();
        todoMessageConfig = gson.fromJson(jsonStr, TodoMessageConfig.class);

        tv_no_data_header.setText((todoMessageConfig!=null && StringUtils.isNotEmpty(todoMessageConfig.getTodoRestrictionMessageHeader()))?todoMessageConfig.getTodoRestrictionMessageHeader():getString(R.string.time_table_not_created));
        tv_no_data_body.setText((todoMessageConfig!=null && StringUtils.isNotEmpty(todoMessageConfig.getTodoRestrictionMessageBody()))?todoMessageConfig.getTodoRestrictionMessageBody():getString(R.string.aapka_time_table_abhi_nai_bana_he));

        cv_info.setVisibility(MyApplication.getFirebaseRemoteConfig().getBoolean("todo_main_screen_info_visibility")?View.VISIBLE:View.GONE);
        backButtonLayout.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        cv_download_timetable.setOnClickListener(cardView->{
            if(StringUtils.isNotEmpty(timeTableUrl)) {
                MyApplication.openPdfFromUrl(this, timeTableUrl);
            }
        });

        cv_info.setOnClickListener(v->{
            try{
                howToVideoView = HowToVideoView.getInstance(this);

                if (howToVideoView.videoView == null || howToVideoView.videoView != how_to_video_view) {
                    howToVideoView.setView(this, how_to_video_view);
                }

                if (howToVideoView.videoView.getVisibility() != View.VISIBLE) {
                    howToVideoView.setType(TODO_MAIN_SCREEN, false);
                    howToVideoView.setButtonText(getString(R.string.start_now));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getRoutineData(TodoType.DAILY.name());
//
//        try{
//            int currentItem = view_pager.getCurrentItem();
//            Fragment fragment = taskPagerAdapter.getFragment(currentItem);
//            if (fragment instanceof TodoListFragment) {
//                ((TodoListFragment) fragment).onResume();
//            }
//        } catch (Exception ex){
//            ex.printStackTrace();
//        }
    }

    @Override
    protected void onDestroy() {

        if(lottie_no_data!=null) {
            lottie_no_data.cancelAnimation();
            lottie_no_data.clearAnimation();
        }
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        destroyHowTo();
        super.onDestroy();
    }

    private void setupNavigation() {
        loadNext.setOnClickListener(v->{
            try {
                int current = view_pager.getCurrentItem();
                if (current < dates.size() - 1) {
                    view_pager.setCurrentItem(current + 1, true);
                    updateCurrentMonth(dates.get(current+1));
                }
            } catch (Exception ignored){}
        });
        loadPrev.setOnClickListener(v->{
            try {
                int current = view_pager.getCurrentItem();
                if (current > 0) {
                    view_pager.setCurrentItem(current - 1, true);
                    updateCurrentMonth(dates.get(current-1));
                }
            } catch (Exception ignored){}
        });
    }
    private void setupDateTabs(TodoDateSelectionModel selectedModel, int selectedPosition) {
        try {
            dateTabAdapter = new DateTabAdapter(this, dates, (selectedDate, position) -> {
                view_pager.setCurrentItem(position, true);
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            rv_date_range.setLayoutManager(layoutManager);
            rv_date_range.setAdapter(dateTabAdapter);
            rv_date_range.setVisibility(View.VISIBLE);

            taskPagerAdapter = new TaskPagerAdapter(this, dates);
            view_pager.setAdapter(taskPagerAdapter);
            view_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrollStateChanged(int state) {}

                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    dateTabAdapter.selectDate(dates.get(position));
                    rv_date_range.smoothScrollToPosition(position);
                    updateCurrentMonth(dates.get(position));
                    toggleNavigationButtons(position);
                }
            });

            new Handler(Looper.getMainLooper()).post(() -> {
                if (selectedPosition >= 0 && selectedPosition < dates.size()) {
                    view_pager.setCurrentItem(selectedPosition, false);
                }
            });
            scrollToSelectedWeek(dates, selectedModel.getId(), rv_date_range);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void toggleNavigationButtons(int position) {
        loadPrev.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        loadNext.setVisibility(position == dates.size() - 1 ? View.GONE : View.VISIBLE);
    }

    private void getRoutineData(String type) {

        toDoListShimmer.startShimmer();
        toDoListShimmer.setVisibility(View.VISIBLE);
//        updateData(null);
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("startDate", getCurrentDate());
        map.put("endDate", getCurrentDate());
        map.put("callingAfter15July", true);
        map.put("todo90DaysPlan", true);

        Log.d(TAG, "Request: "+map.toString());
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_TO_DO_LIST, map), null,
                response -> {
                    TypeToken<ToDoResponse> token = new TypeToken<ToDoResponse>() {
                    };
                    ToDoResponse toDoResponse = gson.fromJson(response.toString(), token.getType());
                    routineSyllabus = toDoResponse.getData();
                    Log.d(TAG, "ToDoResponse: "+toDoResponse.getData().toString());
                    if (toDoResponse.getData() == null ||
                            toDoResponse.getData().getAllUserRoutine() == null ||
                            toDoResponse.getData().getAllUserRoutine().isEmpty()) {
                        toDoListShimmer.stopShimmer();
                        toDoListShimmer.setVisibility(View.INVISIBLE);
                        ll_no_data.setVisibility(View.VISIBLE);
                        lottie_no_data.playAnimation();
                    } else {
                        updateData(toDoResponse.getData());
                        toDoListShimmer.stopShimmer();
                        toDoListShimmer.setVisibility(View.INVISIBLE);
                        ll_no_data.setVisibility(View.GONE);
                        tv_title.setText(StringUtils.isNotEmpty(toDoResponse.getData().getTitleText())?toDoResponse.getData().getTitleText():"");
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(ToDoListMain.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                toDoListShimmer.stopShimmer();
                toDoListShimmer.setVisibility(View.INVISIBLE);

            }
        });
        myJsonObjectRequest2.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void updateData(RoutineSyllabus routineSyllabus) {
        try {
            if (routineSyllabus != null) {
                if (StringUtils.isNotEmpty(routineSyllabus.getTimeTableDownloadUrl())) {
                    timeTableUrl = routineSyllabus.getTimeTableDownloadUrl();
                    cv_download_timetable.setVisibility(View.VISIBLE);
                }

                dates.clear();
                dates.addAll(routineSyllabus.getDateCardsData());

                TodoDateSelectionModel model = null;
                int i = 0;
                for (TodoDateSelectionModel todoDateSelectionModel : routineSyllabus.getDateCardsData()) {
                    if (todoDateSelectionModel != null && routineSyllabus.getSelectedDateId() != null && todoDateSelectionModel.getId().equals(routineSyllabus.getSelectedDateId())) {
                        Log.d(TAG, " --> Selected Model: " + todoDateSelectionModel.toString());
                        model = todoDateSelectionModel;
                        break;
                    }
                    i++;
                }
                updateTodaysTracker(routineSyllabus, model);
                setupDateTabs(model, i);
//            setupViewPager(model, i);
                setupNavigation();
            } else {
                dates.clear();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateTodaysTracker(RoutineSyllabus routineSyllabus, TodoDateSelectionModel todoDateSelectionModel) {
        try {
            tv_daily_progress.setText(MessageFormat.format(getString(R.string.today_task_completed_0_1), routineSyllabus.getCompletedTasksOfToday(), routineSyllabus.getTotalTasksOfToday()));
            renderProgressSegments(routineSyllabus.getCompletedTasksOfToday(), routineSyllabus.getTotalTasksOfToday());

            updateCurrentMonth(todoDateSelectionModel);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateCurrentMonth(TodoDateSelectionModel todoDateSelectionModel) {
        if(routineSyllabus!=null && StringUtils.isEmpty(routineSyllabus.getTitleText()) && StringUtils.isNotEmpty(todoDateSelectionModel.getMonth())) {
            tv_title.setText(todoDateSelectionModel.getMonth());
        }
    }

    private void renderProgressSegments(int completed, int total) {

        ll_progress_container.removeAllViews();

        if (total <= 0 || completed > total) {
            ll_daily_progress_marker.setVisibility(View.GONE);
            return;
        }

        ll_daily_progress_marker.setVisibility(View.VISIBLE);
        int margin = dpToPx(3);

        for (int i = 0; i < total; i++) {
            CardView card = new CardView(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dpToPx(10), 1f);
            params.setMargins(margin, 0, margin, 0);
            card.setLayoutParams(params);

            card.setCardElevation(0f);
            card.setRadius(dpToPx(8));
            card.setUseCompatPadding(false);
            card.setPreventCornerOverlap(false);
            card.setCardBackgroundColor(i < completed
                    ? ContextCompat.getColor(this, R.color.subscription_highlight_color)
                    : ContextCompat.getColor(this, R.color.todo_incomplete_color));

            View filler = new View(this);
            filler.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            card.addView(filler);

            ll_progress_container.addView(card);
        }

    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    public void scrollToSelectedWeek(List<TodoDateSelectionModel> dateList, String selectedId, RecyclerView recyclerView) {
        if (dateList == null || dateList.isEmpty() || selectedId == null) return;

        // Parse selectedId to LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(selectedId, formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Find Monday and Sunday of selected date's week
        LocalDate weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // Get indexes of weekStart and weekEnd in the list
        int startIndex = -1;
        for (int i = 0; i < dateList.size(); i++) {
            TodoDateSelectionModel model = dateList.get(i);
            if (model.getId() == null) continue;

            try {
                LocalDate modelDate = LocalDate.parse(model.getId(), formatter);
                if ((modelDate.isEqual(weekStart) || modelDate.isAfter(weekStart)) &&
                        (modelDate.isEqual(weekEnd) || modelDate.isBefore(weekEnd))) {

                    // First valid item in the week
                    if (startIndex == -1) startIndex = i;
                }

                // If week is complete (7 days), stop early
                if (startIndex != -1 && i - startIndex + 1 == 7) break;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG," --> Start Index: "+startIndex);

        // Scroll to that week start index
        if (startIndex != -1) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(startIndex, 0);
            }
//            recyclerView.scrollToPosition(startIndex);
        }
    }

    private void destroyHowTo(){
        how_to_video_view.setVisibility(View.GONE);
        if(howToVideoView!=null)
            howToVideoView.remove();
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }
}