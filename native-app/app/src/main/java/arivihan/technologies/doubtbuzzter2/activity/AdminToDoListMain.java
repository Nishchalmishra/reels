
package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_TO_DO_LIST;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.SortSubjectNameAnalyticAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ui.main.SectionsPagerAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.MyTabLayout;
import arivihan.technologies.doubtbuzzter2.databinding.ActivityAdminToDoListMainBinding;
import arivihan.technologies.doubtbuzzter2.model.RoutineSyllabus;
import arivihan.technologies.doubtbuzzter2.model.ToDoResponse;
import arivihan.technologies.doubtbuzzter2.model.UserDailyRoutineDto;
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

public class AdminToDoListMain extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = AdminToDoListMain.class.getSimpleName();
    private ActivityAdminToDoListMainBinding binding;
    private final Gson gson = new Gson();
    private CardView generate;
    private TextView type;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private List<String> dates = new ArrayList<>();
    private Dialog subjectDialog;
    private List<List<UserDailyRoutineDto>> data = new ArrayList<>();
    private SortSubjectNameAnalyticAdapter dialogSubjectName;
    private ArrayList<String> activities = new ArrayList<>();
    private ProgressDialog dialog;
    private ViewPager viewPager;
    private MyTabLayout tabs;
    private ShimmerFrameLayout toDoListShimmer;
    private FloatingActionButton loadNext, loadPrev;
    private LinearLayout ll_progress_container;
    Date startDate, endDate;
    private ToDoResponse storeToDoResponse;
    private String triggeredAction = "";
    LinearLayout backButtonLayout;
    String userPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminToDoListMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                if (getIntent().getBooleanExtra("redirect_to_home", false)) {
                    Intent intent = new Intent(AdminToDoListMain.this, HomeScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });

        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(),
                dates, data);
        viewPager = binding.viewPagerAdminTodo;
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        tabs = findViewById(R.id.tabs);
        loadNext = findViewById(R.id.load_next);
        loadPrev = findViewById(R.id.load_prev);
        backButtonLayout = findViewById(R.id.back_button_layout);
        backButtonLayout.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });


        type = binding.type;
        RelativeLayout selection = binding.spinnerRelativeLayout;
        ImageView edit = binding.edit;
        edit.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminEditRoutineActivity.class);
            intent.putExtra("UserPhoneNumber", userPhoneNumber);
            startActivity(intent);
        });
        activities.add(getString(R.string.daily));
        activities.add(getString(R.string.weekly));

        Date currentDate = new Date();
        startDate = currentDate;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        endDate = calendar.getTime();
        // durationSpinner = findViewById(R.id.duration);

        ArrayList<String> types = new ArrayList<>();
        types.add("Daily");
        userPhoneNumber = getIntent().getStringExtra("UserPhoneNumber");


        toDoListShimmer = findViewById(R.id.to_do_list_shimmer);
        subjectDialog = new Dialog(this);
        subjectDialog.setContentView(R.layout.custom_dialogue_statusselection);
        subjectDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this,
                R.drawable.bachground_custom_dialogu_typeactivity));
        subjectDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogSubjectName = new SortSubjectNameAnalyticAdapter(activities, types, this::onsubjectSortClick, -1);
        RecyclerView recyclerView = subjectDialog.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(dialogSubjectName);

        selection.setOnClickListener(view -> {
            subjectDialog.show();
        });

        ll_progress_container = findViewById(R.id.ll_progress_container);
        renderProgressSegments(6, 10);

        tabs.setupWithViewPager(viewPager);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        loadNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loadStartDate = null;
                if (storeToDoResponse != null) {
                    loadStartDate = storeToDoResponse.getData().getNextWeekStartDate();
                    try {
                        endDate = simpleDateFormat.parse(loadStartDate);
                    } catch (Exception e) {
                        loadStartDate = simpleDateFormat.format(endDate);
                    }
                } else {
                    loadStartDate = simpleDateFormat.format(endDate);
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                startDate = endDate;
                endDate = calendar.getTime();
                String loadEndDate = simpleDateFormat.format(calendar.getTime());
                if (storeToDoResponse != null) {
                    getRoutineData("DAILY", "next", storeToDoResponse.getData().getNextWeekStartDate(),
                            null);
                } else {
                    getRoutineData("DAILY", "next", loadStartDate, loadEndDate);
                }
                triggeredAction = "next";

                Bundle clickBtnBundle = new Bundle();
                clickBtnBundle.putString("start_date", loadStartDate);
                clickBtnBundle.putString("end_date", loadEndDate);
                MyApplication.logFirebaseEvent("click_todolist_next", clickBtnBundle);
            }
        });

        loadPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loadEndDate = null;
                if (storeToDoResponse != null) {
                    loadEndDate = storeToDoResponse.getData().getPrevWeekEndDate();
                    try {
                        endDate = simpleDateFormat.parse(loadEndDate);
                    } catch (Exception e) {
                        loadEndDate = simpleDateFormat.format(endDate);
                    }
                } else {
                    loadEndDate = simpleDateFormat.format(endDate);
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                endDate = startDate;
                startDate = calendar.getTime();
                String loadStartDate = simpleDateFormat.format(calendar.getTime());
                if (storeToDoResponse != null) {
                    getRoutineData("DAILY", "prev", storeToDoResponse.getData().getPrevWeekEndDate(),
                            null);
                } else {
                    getRoutineData("DAILY", "prev", loadStartDate, loadEndDate);
                }
                triggeredAction = "prev";

                Bundle clickBtnBundle = new Bundle();
                clickBtnBundle.putString("start_date", loadStartDate);
                clickBtnBundle.putString("end_date", loadEndDate);
                MyApplication.logFirebaseEvent("click_todolist_prev", clickBtnBundle);
            }
        });

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == data.size() - 1) {
                    loadNext.setVisibility(View.VISIBLE);
                } else {
                    loadNext.setVisibility(View.GONE);
                }
                if (tab.getPosition() == 0) {
                    loadPrev.setVisibility(View.VISIBLE);
                } else {
                    loadPrev.setVisibility(View.GONE);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        getRoutineData("DAILY");

    }

    private void onsubjectSortClick(String s, String s1, int i) {
        if (i < activities.size()) {
            subjectDialog.hide();
            type.setText(activities.get(i));
            getRoutineData(s.toUpperCase());
        }
    }

    @Override
    protected void onResume() {
        // getRoutineData("DAILY");
        super.onResume();
    }

    private void getRoutineData(String type) {

        toDoListShimmer.startShimmer();
        toDoListShimmer.setVisibility(View.VISIBLE);
        updateData(null);
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("customSupported", true);
        map.put(PreferenceKeys.USER_ID.getKey(), userPhoneNumber);
        map.put("key", BuildConfig.API_KEY);
        map.put(PreferenceKeys.BOARD.getKey(), MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));


        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_TO_DO_LIST, map), null,
                response -> {
                    TypeToken<ToDoResponse> token = new TypeToken<ToDoResponse>() {
                    };
                    ToDoResponse toDoResponse = gson.fromJson(response.toString(), token.getType());
                    storeToDoResponse = toDoResponse;
                    if (toDoResponse.getData() == null ||
                            toDoResponse.getData().getAllUserRoutine() == null ||
                            toDoResponse.getData().getAllUserRoutine().isEmpty()) {
                        Intent intent = new Intent(this, AdminEditRoutineActivity.class);
                        intent.putExtra("UserPhoneNumber", userPhoneNumber);
                        startActivity(intent);
                    } else {
                        updateData(toDoResponse.getData());
                        toDoListShimmer.stopShimmer();
                        toDoListShimmer.setVisibility(View.INVISIBLE);
                        tabs.selectTab(tabs.getTabAt(storeToDoResponse.getData().getSelection()));
                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(AdminToDoListMain.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                toDoListShimmer.stopShimmer();
                toDoListShimmer.setVisibility(View.INVISIBLE);

            }
        });
        myJsonObjectRequest2.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void getRoutineData(String type, String action, String routineStartDate, String routineEndDate) {

        toDoListShimmer.startShimmer();
        toDoListShimmer.setVisibility(View.VISIBLE);
        updateData(null);
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("startDate", routineStartDate);
        map.put("endDate", routineEndDate);
        map.put("customSupported", true);
        map.put("key", BuildConfig.API_KEY);
        map.put(PreferenceKeys.USER_ID.getKey(), userPhoneNumber);
        map.put(PreferenceKeys.BOARD.getKey(), MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_TO_DO_LIST, map), null,
                response -> {
                    TypeToken<ToDoResponse> token = new TypeToken<ToDoResponse>() {
                    };
                    ToDoResponse toDoResponse = gson.fromJson(response.toString(), token.getType());
                    if (toDoResponse.getData().getDates().isEmpty()) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_more_data), Toast.LENGTH_LONG).show();
                        updateData(storeToDoResponse.getData());
                        toDoListShimmer.stopShimmer();
                        toDoListShimmer.setVisibility(View.GONE);
                    } else {
                        storeToDoResponse = toDoResponse;
                        Log.d("TOTODLISTCHECK", "COUNT :: " + toDoResponse.getData().getDates().size());
                        updateData(toDoResponse.getData());
                        toDoListShimmer.stopShimmer();
                        toDoListShimmer.setVisibility(View.GONE);
                        tabs.selectTab(tabs.getTabAt(storeToDoResponse.getData().getSelection()));

                        if (toDoResponse.getData().getDates().size() < 7) {
                            if (action.equals("next")) {
                                loadNext.setVisibility(View.GONE);
                            } else if (action.equals("prev")) {
                                loadPrev.setVisibility(View.GONE);
                            }

                        }
                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(MyApplication.getInstance(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (routineSyllabus != null) {
            dates.clear();
            dates.addAll(routineSyllabus.getDates());
            data.clear();

            Map<String, List<UserDailyRoutineDto>> allUserRoutine = routineSyllabus.getAllUserRoutine();
            for (String key : allUserRoutine.keySet()) {
                if (allUserRoutine.get(key) != null) {
                    data.add(allUserRoutine.get(key));
                }
            }

        } else {
            dates.clear();
            data.clear();
        }

        sectionsPagerAdapter.notifyDataSetChanged();

    }

    private void renderProgressSegments(int completed, int total) {
        Log.d(TAG, "----> Todo list");
        // Clear existing views
        ll_progress_container.removeAllViews();

        // Avoid divide by zero
        if (total <= 0) return;

        // Margins between segments
        int margin = dpToPx(2);

        for (int i = 0; i < total; i++) {
            View segment = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dpToPx(8), 1f);
            params.setMargins(margin, 0, margin, 0);
            segment.setLayoutParams(params);

            // Set color
            int colorRes = (i < completed) ? ContextCompat.getColor(this, R.color.subscription_highlight_color) : ContextCompat.getColor(this, R.color.todo_incomplete_color);
            segment.setBackgroundColor(colorRes);

            ll_progress_container.addView(segment);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            getRoutineData("DAILY");
        } else {
            getRoutineData("WEEKLY");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}