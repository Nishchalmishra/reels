package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_DEFAULT_ORDER;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_SYLLABUS_ORDER;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.ui.main.SectionsPagerAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.InstructionBodyAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.databinding.ActivityEditRoutineBinding;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.GenerateSyllabusDto;
import arivihan.technologies.doubtbuzzter2.model.RoutineDefaultOrder;
import arivihan.technologies.doubtbuzzter2.model.UserSyllabusOrder;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: EditRoutineActivity Copyright 23/08/23, 12:49 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/08/23, 12:49 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class EditRoutineActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    private ActivityEditRoutineBinding binding;
    private List<String> tabsString = new ArrayList<>();
    private ProgressDialog dialog;
    private Dialog dialog2;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private Map<String, List<UserSyllabusOrder>> datas = new HashMap<>();
    private Gson gson;
    private boolean newUser;
    private Dialog progressDialog;
    private LottieAnimationView lottieAnimationView;
    private DatePickerDialog datePickerDialog;
    private ShimmerFrameLayout editActivityShimmer;
    static boolean activtyActive = false;

    MaterialCardView skipCard;

    RelativeLayout parentLayout;
    RoutineDefaultOrder routineDefaultOrder;

    Handler handler;
    Runnable runnable;

    boolean isDialog2Shown;
    LinearLayout backButtonLayout;

    private Timer todoListNotificationCheckTimer;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    protected void onStart() {
        super.onStart();
        activtyActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        activtyActive = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditRoutineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_OK);
                finish();
            }
        });

        if (null != getIntent()) {
            newUser = getIntent().getBooleanExtra("newUser", false);
        }
        parentLayout = findViewById(R.id.parent_layout);
        editActivityShimmer = findViewById(R.id.edit_act_shimmer);
        progressDialog = new Dialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.arivihan_dialog);
        progressDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        lottieAnimationView = progressDialog.findViewById(R.id.lottie);
        lottieAnimationView.setAnimation(R.raw.edit_routine_lottie_star);
        backButtonLayout = findViewById(R.id.back_button_layout);
        backButtonLayout.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
//        earlier loading_jsno
        ExtendedFloatingActionButton updateSyllabus = binding.updateSyllabus;

//        if (newUser) {
//            updateSyllabus.setText(getString(R.string.create));
//        } else {
//            updateSyllabus.setText(getString(R.string.update));
//        }

        updateSyllabus.setOnClickListener((v) -> {
            showDatePickerDialog();
        });

        ImageView info = binding.info;

        setInfoDialog();
        info.setOnClickListener(view -> {
            if (!isFinishing() && !isDestroyed()) {
                dialog2.show();
            }
        });

        handler = new Handler();
        runnable = () -> {
            if (activtyActive && !isFinishing() && !isDestroyed()) {
                dialog2.show();
            }
        };

        skipCard = findViewById(R.id.skip);
        if (newUser)
            skipCard.setVisibility(View.VISIBLE);
        else
            info.setVisibility(View.VISIBLE);

        skipCard.setOnClickListener(v -> {
//            if (newUser)
            startActivity(new Intent(EditRoutineActivity.this, HomeScreen.class));
        });

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(parentLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    if (routineDefaultOrder == null)
                        getDefaultOrder();
                    if (!isDialog2Shown) {
                        handler.postDelayed(runnable, 4000);
                        isDialog2Shown = true;
                    }
                }
            }
            else{
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(getString(R.string.loading_data));
        sectionsPagerAdapter = new SectionsPagerAdapter(this,
                getSupportFragmentManager(), tabsString, datas);
        viewPager = binding.viewPagerRoutine;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        gson = new Gson();
        getDefaultOrder();
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        datePickerDialog = new DatePickerDialog(
                this, R.style.DialogTheme, EditRoutineActivity.this, year, month, day);
        if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())) {
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
            datePickerDialog.getDatePicker().updateDate(
                    (int) MyApplication.getFirebaseRemoteConfig().getLong("default_challenge_target_year"),
                    (int) MyApplication.getFirebaseRemoteConfig().getLong("default_challenge_target_month"),
                    (int) MyApplication.getFirebaseRemoteConfig().getLong("default_challenge_target_date"));
        } else {
            c.add(Calendar.MONTH, 3);
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
        }
        View titleView = getLayoutInflater().inflate(R.layout.custom_title_layout, null);
        datePickerDialog.setCustomTitle(titleView);
        datePickerDialog.show();

        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setText(getString(R.string.create));
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setText(getString(R.string.close));
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("DiscouragedPrivateApi")
    private void setDatePickerHeaderBackgroundColor(DatePickerDialog dpd, int color) {
        try {
            Field mDatePickerField;
            mDatePickerField = DatePickerDialog.class.getDeclaredField("mDatePicker");
            mDatePickerField.setAccessible(true);
            final DatePicker mDatePicker = (DatePicker) mDatePickerField.get(dpd);

            int headerId = Resources.getSystem().getIdentifier("day_picker_selector_layout", "id", "android");
            final View header;
            if (mDatePicker != null) {
                header = mDatePicker.findViewById(headerId);
                header.setBackgroundColor(color);
                header.setTag("Set Your Target Date");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setInfoDialog() {

        dialog2 = new Dialog(EditRoutineActivity.this);
        dialog2.setContentView(R.layout.custom_dialog_instruction_edit_routine);
        dialog2.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.getWindow().setDimAmount(0.9f);


        ImageView closeBtn = dialog2.findViewById(R.id.close_btn);
        RecyclerView recyclerView = dialog2.findViewById(R.id.dialog_recycler_view);
        List<String> items = new ArrayList<>();
        items.add(getString(R.string.to_do_list_instruction_1));
        items.add(getString(R.string.to_do_list_instruction_2));
        items.add(getString(R.string.to_do_list_instruction_3));
        items.add(getString(R.string.to_do_list_instruction_4));
        recyclerView.setLayoutManager(new LinearLayoutManager(dialog2.getContext(), LinearLayoutManager.VERTICAL, false));
        InstructionBodyAdapter instructionBodyAdapter = new InstructionBodyAdapter(this, items, true);
        recyclerView.setAdapter(instructionBodyAdapter);

//        CardView confirm = dialog2.findViewById(R.id.ask_points);
//        CardView cancel = dialog2.findViewById(R.id.revise_concept);
//        TextView text = dialog2.findViewById(R.id.text);
//        CardView cut = dialog2.findViewById(R.id.cancel);
//
//        TextView msg = dialog2.findViewById(R.id.doubt_text);
//
//        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
//        String appendText = lang;
//        if ("en".equalsIgnoreCase(lang)) {
//            appendText = "";
//        }
//
//        String referDescription = MyApplication.getFirebaseRemoteConfig().getString("terms_and_condition"+ appendText);
//        if (StringUtils.isEmpty(referDescription)) {
//            referDescription = MyApplication.getFirebaseRemoteConfig().getString("terms_and_condition");
//        }
//        msg.setText(referDescription.replace("\\n", "\n"));

//        confirm.setVisibility(View.GONE);
//        cancel.setVisibility(View.GONE);
//
//        ImageView closeBtn = dialog2.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog2 != null && dialog2.isShowing()) {
                        dialog2.dismiss();
                }
            }
        });
    }

    private void showAnimationView() {
        datePickerDialog.dismiss();
        lottieAnimationView.playAnimation();

        CardView button = progressDialog.findViewById(R.id.btn_dialog_a);

        TextView textView = progressDialog.findViewById(R.id.text_dialog);
        textView.setText(R.string.syllabus_in_progress);

        TextView heading = progressDialog.findViewById(R.id.heading);
        heading.setText(R.string.creating_syllabus);

        button.setOnClickListener(view -> {
            progressDialog.dismiss();
            editActivityShimmer.stopShimmer();
            editActivityShimmer.setVisibility(View.INVISIBLE);
            lottieAnimationView.pauseAnimation();
            if (newUser) {
                Intent intent = new Intent(this, HomeScreen.class);
                startActivity(intent);
            } else{
                Intent intent = new Intent(EditRoutineActivity.this, HomeScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        if (activtyActive && !isFinishing() && !isDestroyed()) {
            progressDialog.show();
        }

//        editActivityShimmer.startShimmer();
//        editActivityShimmer.setVisibility(View.VISIBLE);


        todoListNotificationCheckTimer = new Timer();
        todoListNotificationCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handleTodolistReady();
            }
        }, 0, 200);
    }

    private void handleTodolistReady() {
        if (MyApplication.getBoolData(PreferenceKeys.IS_TODOLIST_READY.getKey())) {
            progressDialog.dismiss();
//            finish();
            MyApplication.saveData(PreferenceKeys.IS_TODOLIST_READY.getKey(), false);
            todoListNotificationCheckTimer.cancel();
        }
    }


    private void callGenerateApi(Map<String, List<UserSyllabusOrder>> datas, String examDate) {
        editActivityShimmer.startShimmer();
        editActivityShimmer.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.BOARD.getKey(), MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));

        MyJsonObjectRequest myJsonObjectRequest2 = null;
        GenerateSyllabusDto generateSyllabusDto = GenerateSyllabusDto.builder().datas(datas).examDate(examDate).build();
        try {
            myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(UPDATE_SYLLABUS_ORDER, map), new JSONObject(gson.toJson(generateSyllabusDto)),
                    response -> {

//                        dialog.hide();
                        editActivityShimmer.stopShimmer();
                        editActivityShimmer.setVisibility(View.GONE);
                        showAnimationView();
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.toDo_list_created.getActionName());

                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.hide();
                    editActivityShimmer.stopShimmer();
                    editActivityShimmer.setVisibility(View.GONE);
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_SHORT).show();
            if (activtyActive && !isFinishing() && !isDestroyed()) {
                dialog.show();
            }

        }

    }

    private void getDefaultOrder() {
        if (isFinishing()) {
            return;
        }
//        dialog.show();
        editActivityShimmer.startShimmer();
        editActivityShimmer.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.BOARD.getKey(), MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_DEFAULT_ORDER, map), null,
                response -> {
                    TypeToken<RoutineDefaultOrder> token = new TypeToken<RoutineDefaultOrder>() {
                    };
                    routineDefaultOrder = gson.fromJson(response.toString(), token.getType());
                    Set<String> strings = routineDefaultOrder.getData().keySet();
                    datas = routineDefaultOrder.getData();
                    updateSelection(datas);
                    tabsString.clear();
                    tabsString.addAll(strings);
                    sectionsPagerAdapter = new SectionsPagerAdapter(this,
                            getSupportFragmentManager(), tabsString, datas);
                    viewPager.setAdapter(sectionsPagerAdapter);

//                    dialog.hide();
                    editActivityShimmer.stopShimmer();
                    editActivityShimmer.setVisibility(View.INVISIBLE);

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                //Toast.makeText(getApplicationContext(), getString(R.string.somethingWentWrong), Toast.LENGTH_SHORT).show();
                dialog.hide();

            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);


    }

    private void goToHomePage() {
//        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//        homeIntent.addCategory(Intent.CATEGORY_HOME);
//        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(homeIntent);
        Intent intent = new Intent(this, HomeScreen.class);
        intent.putExtra(PreferenceKeys.SELECTED_TAB.getKey(), "1");
        startActivity(intent);
    }

    private void updateSelection(Map<String, List<UserSyllabusOrder>> datas) {
        for (String key : datas.keySet()) {
            List<UserSyllabusOrder> userSyllabusOrders = datas.get(key);
            if (!CollectionUtils.isEmpty(userSyllabusOrders)) {
                for (UserSyllabusOrder userSyllabusOrder : userSyllabusOrders) {
                    userSyllabusOrder.setSelected(true);
                }
            }
        }

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        month = month + 1;
        MyApplication.saveData(PreferenceKeys.EDIT_ROUTINE_COMPLETED.getKey(), true);
        callGenerateApi(datas, "" + day + "-" + month + "-" + year);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!isFinishing() && !isDestroyed() && dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (!isFinishing() && dialog2 != null && dialog2.isShowing()) {
            dialog2.dismiss();
        }

        if (!isFinishing() && progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (!isFinishing() && datePickerDialog != null && datePickerDialog.isShowing()) {
            datePickerDialog.dismiss();
        }
    }

}