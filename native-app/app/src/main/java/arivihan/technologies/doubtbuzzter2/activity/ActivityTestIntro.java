package arivihan.technologies.doubtbuzzter2.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.InstructionHeaderAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.offline.OfflineTestManager;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyTaskStatus;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesSetDto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesTypeDto;
import arivihan.technologies.doubtbuzzter2.model.offline.OfflineTestSeriesModel;
import arivihan.technologies.doubtbuzzter2.model.offline.OfflineTestTypes;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TestSeriesConfig;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TestSeriesWarningConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.HALF_YEARLY_USER_TASK_UPDATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_QUESTION;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: ActivityTestIntro Copyright 8/1/22 3:11 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 8/1/22 3:11 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class ActivityTestIntro extends BaseActivity {
    private static final String TAG = ActivityTestIntro.class.getSimpleName();
    private Gson gson = new Gson();
    private CardView enter;
    boolean dataLoaded = false;
    LinearLayout back;
    private TestSeriesSetDto set;
    private TextView noOfQuestion, time, title, btnText, totalMarks;
    ArrayList<TestSeriesQuestionDto> testSeriesQuestionList = null;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    private String id, testId, subject, initialSnackMessage;
    int position;
    private ProgressDialog dialog;
    private long timeToAttempt;
    List<List<String>> instructionList = new ArrayList<>();
    InstructionHeaderAdapter instructionHeaderAdapter;
    String Type = "";
    private boolean attempted;
    RelativeLayout ParentLayout;
    private boolean isCombined;
    private JSONObject limitMap;
    private Dialog lockDialog;
    TextView personCount, personText;
    boolean finish;
    String asatId, processInfoId;
    boolean isAsat;
    ImageView image1, image2, image3;
    FrameLayout userImages;
    String testTitle;
    NetworkStatusLiveData networkStatusLiveData;
    Dialog attemptDialogConfirmation;
    TestSeriesConfig testSeriesConfig;
    private Dialog attemptWarningDialog;
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        dismissAttemptDialogConfirmation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_layout_1);
        setLockDialog();
        enter = findViewById(R.id.enter);
        noOfQuestion = findViewById(R.id.no_of_qs);
        btnText = findViewById(R.id.btn_text);
        Type = getIntent().getStringExtra(PreferenceKeys.TYPE.getKey());
        time = findViewById(R.id.time);
        recyclerView = findViewById(R.id.instruction);
        progressBar = findViewById(R.id.progress);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        back = findViewById(R.id.back_arrow);
        title = findViewById(R.id.MockTestTitle);
        testId = getIntent().getStringExtra("testId");
        id = getIntent().getStringExtra("Id");
        position = getIntent().getIntExtra("position", 0);
        subject = getIntent().getStringExtra("subject");
        testTitle = getIntent().getStringExtra("testTitle");
        isCombined = getIntent().getBooleanExtra("combined", false);
        processInfoId = getIntent().getStringExtra("processInfoId");
        asatId = getIntent().getStringExtra("asatId");
        isAsat = getIntent().getBooleanExtra("isAsat", false);
        initialSnackMessage = getIntent().getStringExtra("initialSnackMessage");

        if(isAsat && StringUtils.isNotEmpty(initialSnackMessage)) {
            CommonMethods.showNegativeSnackbar(ParentLayout, initialSnackMessage, 3000);
        }

        if(isAsat) {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_test_instruction_view.getActionName());
        }

        Log.d("AsatID" , "Id = " +asatId);

        String firebaseConfigJsonStr = MyApplication.getFirebaseRemoteConfig()
                .getString("testseries_config_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        Gson gson = new Gson();
        testSeriesConfig = gson.fromJson(firebaseConfigJsonStr, TestSeriesConfig.class);

        Uri deeplink = getIntent().getData();
        if (deeplink != null) {
            Type = deeplink.getQueryParameter("type");
            testId = deeplink.getQueryParameter("testId");
            id = deeplink.getQueryParameter("id");
            try {
                position = Integer.parseInt(deeplink.getQueryParameter("position"));
            } catch (Exception e) {}
            subject = deeplink.getQueryParameter("subject");
            isCombined = deeplink.getBooleanQueryParameter("combined", true);

            // ASAT
            processInfoId = deeplink.getQueryParameter("processInfoId");
            asatId = deeplink.getQueryParameter("asatId");
            isAsat = deeplink.getBooleanQueryParameter("isAsat", false);

            testTitle = deeplink.getQueryParameter("testTitle");
            initialSnackMessage = deeplink.getQueryParameter("initialSnackMessage");
        }

        totalMarks = findViewById(R.id.total_marks);
        personCount = findViewById(R.id.person_count);
        personText = findViewById(R.id.person_text);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        userImages = findViewById(R.id.user_images);
        MyApplication.updateUserIdForCrashlytics();
        title.setText(subject);

        back.setOnClickListener(view -> finish());

        dialog = new ProgressDialog(ActivityTestIntro.this);
        dialog.setCanceledOnTouchOutside(false);

        if ("RESPONSE".equals(Type)) {
            btnText.setText(getString(R.string.YourResponse));
            dialog.setMessage(getString(R.string.analyzing_your_test));
        } else if ("REATTEMPT".equals(Type)) {
            btnText.setText(getString(R.string.ReAttempt));
            dialog.setMessage(getString(R.string.preparing_your_test));
        }


        dialog.show();


        enter.setOnClickListener(v -> {
            MyApplication.saveData(PreferenceKeys.RELOAD_TEST_LIST_DATA.getKey(), true);
            if (CollectionUtils.isEmpty(testSeriesQuestionList)) {
                CommonMethods.showNegativeSnackbar(enter, getString(R.string.encountered_some_error));
            } else if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                CommonMethods.showNegativeSnackbar(enter, getString(R.string.NoInternetConnection));
            } else {
                if (set.isLock()) {
                    lockDialog.show();
                    return;
                }

                //Checking for any offline test saved with same id
                if(OfflineTestManager.getModelById(testId)!=null) {
                    WeakReference<Activity> activityRef = new WeakReference<>(ActivityTestIntro.this);
                    OfflineTestManager offlineTestManager = OfflineTestManager.getInstance();
                    offlineTestManager.syncOfflineTest(this, OfflineTestManager.getModelById(testId), null);
                    Toast.makeText(this, (testSeriesConfig!=null && testSeriesConfig.getSyncingLastOfflineTestMsg()!=null)?testSeriesConfig.getSyncingLastOfflineTestMsg():getString(R.string.please_wait_while_we_sync_your_last_submission), Toast.LENGTH_SHORT).show();
                    return;
                }

                //Checking for any offline ASAT test - as each time ASAT tests are shuffled - so testId won't work here
                if(asatId!=null && isAsat) {
                    OfflineTestManager offlineTestManager = OfflineTestManager.getInstance();
                    List<OfflineTestSeriesModel> asatModels = OfflineTestManager.getModelsByTestType(OfflineTestTypes.ASAT);
                    if(!asatModels.isEmpty()) {
                        WeakReference<Activity> activityRef = new WeakReference<>(ActivityTestIntro.this);
                        for(OfflineTestSeriesModel model : asatModels) {
                            offlineTestManager.syncOfflineTest(this, model, new OfflineTestManager.OfflineTestSubmissionListener() {
                                @Override
                                public void onStart(boolean success, Object object) {
                                    try{
                                        Activity activity = activityRef.get();
                                        if (activity != null &&
                                                !activity.isFinishing() && !activity.isDestroyed()) {
                                            activity.runOnUiThread(() -> {
                                                enter.setEnabled(false);
                                            });
                                        }
                                    } catch (Exception ignored) {}
                                }

                                @Override
                                public void onSuccess(boolean success, Object object) {
                                    try{
                                        Activity activity = activityRef.get();
                                        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                                            activity.runOnUiThread(() -> {
                                                finish();
                                            });
                                        }
                                    } catch (Exception ignored) {}
                                }

                                @Override
                                public void onError(boolean success, Object object) {
                                    try{
                                        Activity activity = activityRef.get();
                                        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                                            activity.runOnUiThread(() -> {
                                                enter.setEnabled(true);
                                            });
                                        }
                                    } catch (Exception ignored) {}
                                }
                            });
                        }
                        Toast.makeText(this,
                                (testSeriesConfig!=null &&
                                        testSeriesConfig.getSyncingLastOfflineTestMsg()!=null)?testSeriesConfig.getSyncingLastOfflineTestMsg():getString(R.string.please_wait_while_we_sync_your_last_submission), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if(set.isSingleAttempt() && set.isAttempted()) {
                    Toast.makeText(this,
                            (testSeriesConfig!=null && testSeriesConfig.getTestAttemptExceedMsg()!=null)?
                                    testSeriesConfig.getTestAttemptExceedMsg():getString(R.string.already_attempted),
                            Toast.LENGTH_SHORT).show();
                } else {
                    if(set.isSingleAttempt()) {
                        if(set.isTimeRestrictedEntry()&&!canEnterTest(set)){
                                return;
                        }
                        showAttemptDialogConfirmation(dialog->{
                            navigateToTestScreen();
                        });
                    } else {
                        navigateToTestScreen();
                    }
                }

            }
        });

//        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!dataLoaded) {
                    dataLoaded = true;
                    getAndLaunchTest(subject);
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
                getAndLaunchTest(subject);
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

        getAndSettUserImages();
    }
    private boolean canEnterTest(TestSeriesSetDto testSeriesSetDto) {
        if(byPassTestCondition()){
          return true;
        }
//        Log.d(TAG, "canEnterTest: " + testSeriesSetDto);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDate testDate = LocalDate.parse(testSeriesSetDto.getTestDate());
        LocalTime startTime = LocalTime.parse(testSeriesSetDto.getTestStartTime());
        LocalTime endTime = LocalTime.parse(testSeriesSetDto.getTestEndTime());
        int bufferMinutes = testSeriesSetDto.getAllowEntryWithinMinutes() != null ? testSeriesSetDto.getAllowEntryWithinMinutes() : 0;

        // Get app language
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()); // e.g. "en" or "hi"
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag(lang));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.forLanguageTag(lang));

        String formattedDate = testDate.format(dateFormatter);
        String formattedTime = formatTimeLocalized(startTime, Locale.forLanguageTag(lang));

        if(set.getWaitingDialogContent()==null){
            set.setWaitingDialogContent(new HashMap<>());
        }
        if(set.getTestExpiredDialogContent()==null){
            set.setTestExpiredDialogContent(new HashMap<>());
        }

        // CASE 1: Test scheduled for future date/time
        if (testDate.isAfter(today) ||
                (testDate.isEqual(today) && now.isBefore(startTime.minusMinutes(bufferMinutes)))) {
            String title = getString(R.string.wait_message, formattedTime);
            @SuppressLint("StringFormatMatches")
            String subtitle = getString(R.string.wait_message_subtitle, formattedDate, formattedTime);
            showAttemptWarningDialog(testSeriesSetDto.getWaitingDialogContent(),title, subtitle);
            return false;
        }

        // CASE 2: Today but before start time
        if (testDate.isEqual(today) && now.isBefore(startTime)) {
            String title = getString(R.string.wait_message, formattedTime);
            @SuppressLint("StringFormatMatches")
            String subtitle = getString(R.string.wait_message_subtitle, formattedDate, formattedTime);
            showAttemptWarningDialog(testSeriesSetDto.getWaitingDialogContent(), title, subtitle);
            return false;
        }


        // CASE 3: After allowed entry window
        LocalTime entryDeadline = startTime.plusMinutes(bufferMinutes);
        Log.d(TAG, "canEnterTest: "+entryDeadline+"nowTiminig"+now);
        if (testDate.isBefore(today) || testDate.isEqual(today) && now.isAfter(entryDeadline)){
            String title = getString(R.string.time_up);
            String subtitle = getString(R.string.time_up_subtitle);
            showAttemptWarningDialog(testSeriesSetDto.getTestExpiredDialogContent(),title, subtitle);
            return false;
        }
        return true;
    }

    private boolean byPassTestCondition() {
        String allowToByPassForUsers = MyApplication.getFirebaseRemoteConfig().
                getString("allow_to_by_pass_for_users");
        if (StringUtils.isNotEmpty(allowToByPassForUsers)) {
            String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
            String[] allowedUsers = allowToByPassForUsers.split(",");
            boolean isPhoneNumberAllowed = false;
            for (String item : allowedUsers) {
                if (item.trim().equals(phoneNumber)) {
                    isPhoneNumberAllowed = true;
                    break;
                }
            }
            if (isPhoneNumberAllowed) {
                return true;
            }
        }
            return false;
    }

    private String formatTimeLocalized(LocalTime time, Locale locale) {
        String formatted = time.format(DateTimeFormatter.ofPattern("h:mm a", locale));
        formatted = formatted.replace(":00", "");
        return formatted.trim();
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();

        dismissAttemptDialogConfirmation();
    }

    private void navigateToTestScreen() {
        try {
            Intent intent = new Intent(ActivityTestIntro.this, MockQuestionFInal.class);
            intent.putExtra("testId", testId);
            intent.putExtra("Id", id);
            intent.putExtra("timeToAttempt", timeToAttempt);
            intent.putExtra("subject", subject);
            intent.putExtra("attempted", attempted);
            intent.putExtra("combined", isCombined);
            intent.putExtra("processInfoId", processInfoId);
            intent.putExtra("asatId", asatId);
            intent.putExtra("isAsat", isAsat);
            intent.putExtra(PreferenceKeys.BOARD.getKey(), set != null && set.isBoard());
            intent.putExtra("testSeriesName", set.getTestSeriesName());
            intent.putExtra("fromGlobalSearch", getIntent().getBooleanExtra("fromGlobalSearch", false));
            intent.putExtra("fromHalfYearly", getIntent().getBooleanExtra("fromHalfYearly", false));
            intent.putExtra("testTitle", testTitle);
            intent.putExtra("singleAttempt", set.isSingleAttempt());
            intent.putExtra("timeRestrictedEntry", set.isTimeRestrictedEntry());

            if (!attempted) {
                MyApplication.saveData(PreferenceKeys.RESPONSE.getKey(), "ATTEMPT");
                intent.putExtra(PreferenceKeys.RESPONSE.getKey(), "ATTEMPT");
                finish = true;
                MyApplication.saveData(id + "_" + subject, position);
                if (getIntent().getStringExtra("routineId") != null) {
                    updateStatus();
                    intent.putExtra("fromHalfYearly", true);
                }
            } else {
                if (Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                    MyApplication.saveData(PreferenceKeys.RESPONSE.getKey(), PreferenceKeys.RESPONSE.getKey());
                    intent.putExtra(PreferenceKeys.RESPONSE.getKey(), PreferenceKeys.RESPONSE.getKey());
                } else {
                    MyApplication.saveData(id + "_" + subject, position);
                    finish = true;
                    if (set != null && set.isBoard()) {
                        MyApplication.saveData(PreferenceKeys.RESPONSE.getKey(), "ATTEMPT");
                        intent.putExtra(PreferenceKeys.RESPONSE.getKey(), "ATTEMPT");
                        if (getIntent().getStringExtra("routineId") != null) {
                            intent.putExtra("fromHalfYearly", true);
                            updateStatus();
                        }
                    } else {
                        MyApplication.saveData(PreferenceKeys.RESPONSE.getKey(), "REATTEMPT");
                        intent.putExtra(PreferenceKeys.RESPONSE.getKey(), "REATTEMPT");
                    }

                    for (TestSeriesQuestionDto testSeriesQuestionDto : testSeriesQuestionList) {
                        testSeriesQuestionDto.setUserAnswer(null);
                    }
                }
            }
            MyApplication.saveTestSeriesAnalytics("testQuestionResult", null);
            MyApplication.saveTestSeriesQuestions("questions", testSeriesQuestionList);
            if (limitMap != null) {
                intent.putExtra("limit", limitMap.toString());
            }
            String instruction = CommonMethods.convertToString(instructionList);
            intent.putExtra("instruction", instruction);

            Map<String, String> params = new HashMap<>();
            params.put("testSeriesId", testId);
            params.put("testSeriesName", set.getTestSeriesName());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.Test_series_attempt.getActionName(), params);

            if(isAsat) {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_test_attempt_click.getActionName(), params);
            }

            startActivity(intent);
            if (finish)
                finish();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatus() {
        if (!Objects.equals(getIntent().getStringExtra("routineStatus"), HalfYearlyTaskStatus.COMPLETED.name())) {
            Map<String, Object> map = new HashMap<>();
            map.put("routineId", getIntent().getStringExtra("routineId"));
            map.put("routineStatus", HalfYearlyTaskStatus.COMPLETED.name());
            map.put("videoProgress", 0);
            MyJsonObjectRequest taskUpdateRequest = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(HALF_YEARLY_USER_TASK_UPDATE, map), null,
                    response -> {
                        Log.d(PreferenceKeys.UPDATE_STATUS.getKey(), "success");
                        MyApplication.saveData(PreferenceKeys.UPDATE_STATUS.getKey(), true);
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    CommonMethods.showNegativeSnackbar(back, error.getMessage());
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(taskUpdateRequest);
        }
    }

    public void setLockDialog() {
        lockDialog = new Dialog(this);
        lockDialog.setContentView(R.layout.custom_dialog_lock_box);
        lockDialog.getWindow().setBackgroundDrawable(this.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        lockDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lockDialog.setCancelable(false);
        TextView title = lockDialog.findViewById(R.id.header);

        title.setText(getString(R.string.OOpsThisTestIsLocked));
        MaterialCardView card = lockDialog.findViewById(R.id.subscriptionLayout);
        card.setOnClickListener(v -> {
            lockDialog.dismiss();
            SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.TEST_SERIES);
//            Intent intent = new Intent(this, CampaignSubscriptionActivity.class);
////            intent.putExtra("plan_level", "COURSE");
//            startActivity(intent);

        });

        ImageView cut = lockDialog.findViewById(R.id.cancel);
        cut.setOnClickListener(v -> lockDialog.dismiss());
    }

    private void getAndSettUserImages() {
        String value = MyApplication.getFirebaseRemoteConfig().getString("random_images_of_users");
        if (StringUtils.isNotEmpty(value)) {
            TypeToken<ArrayList<String>> token = new TypeToken<ArrayList<String>>() {
            };
            ArrayList<String> list = gson.fromJson(value, token.getType());
            if (!CollectionUtils.isEmpty(list)) {
                Collections.shuffle(list);
                Glide.with(this).load(list.get(0)).into(image1);
                Glide.with(this).load(list.get(1)).into(image2);
                Glide.with(this).load(list.get(2)).into(image3);
            }
        }
    }

    private void getAndLaunchTest(String subject) {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        map.put("id", testId);
        map.put("combined", isCombined);
        map.put("reviewMode", false);
        map.put("testSeriesId", id);
        map.put("boldFormatting", true);
        Log.d(TAG,"Test Request: "+map.toString());
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(TEST_SERIES_QUESTION, map), null,
                response -> {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    userImages.setVisibility(View.VISIBLE);
                    TypeToken<TestSeriesSetDto> token = new TypeToken<>() {
                    };
                    set = gson.fromJson(response.toString(), token.getType());
                    Log.d(TAG, "getAndLaunchTest: "+set.toString());
                    List<TestSeriesTypeDto> testSeriesTypes = set.getTestSeriesTypes();
                    instructionList.addAll(set.getInstructions());
                    progressBar.setVisibility(View.GONE);
                    if (isCombined) {
                        updateCombinedView(testSeriesTypes);
                    } else {
                        updateSeparatedView(testSeriesTypes);
                    }
                    personCount.setText(set.getTotalTestGiven());
                    personText.setText(" " + getString(R.string.students));
                    totalMarks.setText(String.valueOf(set.getTotalScore()));

                    instructionHeaderAdapter = new InstructionHeaderAdapter(instructionList, getApplicationContext());
                    recyclerView.setAdapter(instructionHeaderAdapter);

                    if (CollectionUtils.isEmpty(testSeriesQuestionList)) {
                        testSeriesQuestionList = new ArrayList<>();
                        CommonMethods.showNegativeSnackbar(enter, getString(R.string.encountered_some_error));
                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                CommonMethods.showNegativeSnackbar(enter, getString(R.string.encountered_some_error));

            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void updateSeparatedView(List<TestSeriesTypeDto> testSeriesTypes) {

        for (TestSeriesTypeDto testSeriesType : testSeriesTypes) {
            Map<String, Integer> limit = testSeriesType.getLimit();
            if (limit != null && !limit.isEmpty()) {
                try {
                    limitMap = new JSONObject(limit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (testSeriesType.getSubject().equalsIgnoreCase(subject)) {
                attempted = testSeriesType.isAttempted();

                testSeriesQuestionList = (ArrayList<TestSeriesQuestionDto>) testSeriesType.getTestSeriesQuestionList();
                updateView(testSeriesType.getSubject(), testSeriesQuestionList.size(), testSeriesType.getTotalTimeToAttempt());
                break;
            }
        }
    }

    private void updateCombinedView(List<TestSeriesTypeDto> testSeriesTypes) {
        Map<String, Integer> map = new HashMap<>();
        long totalTime = 0;
        int size = 0;
        for (TestSeriesTypeDto testSeriesType : testSeriesTypes) {
            if (!attempted) {
                attempted = testSeriesType.isAttempted();
            }
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
            testSeriesQuestionList.addAll(datas);
            size += datas.size();
            totalTime += testSeriesType.getTotalTimeToAttempt();
        }

        try {
            if (map != null && !map.isEmpty()) {
                limitMap = new JSONObject(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateView(set.getTestSeriesName(), size, totalTime);
    }

    private void updateView(String subject, int size, long totalTimeToAttempt) {
        timeToAttempt = totalTimeToAttempt;
        title.setText(subject);
        noOfQuestion.setText(String.valueOf(size));
        if ("RESPONSE".equals(Type)) {
            time.setText(CommonMethods.getTimeInHrAndMin(timeToAttempt, this));
        } else {
            time.setText(CommonMethods.getTimeInHrAndMin(totalTimeToAttempt, this));
        }
    }

    private void showAttemptDialogConfirmation(DialogInterface.OnDismissListener listener) {
        try {
            dismissAttemptDialogConfirmation();
            attemptDialogConfirmation = new Dialog(this);
            attemptDialogConfirmation.setContentView(R.layout.dialog_test_series_warning);
            attemptDialogConfirmation.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            attemptDialogConfirmation.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            attemptDialogConfirmation.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            attemptDialogConfirmation.setCancelable(false);

            String firebaseJsonStr = MyApplication.getFirebaseRemoteConfig()
                    .getString("testseries_attempt_warning_dialog_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

            LinearProgressIndicator progressIndicator = attemptDialogConfirmation.findViewById(R.id.progress_indicator);
            CardView cv_cancel = attemptDialogConfirmation.findViewById(R.id.cv_cancel);
            TextView tv_title = attemptDialogConfirmation.findViewById(R.id.tv_title);
            TextView tv_subtitle = attemptDialogConfirmation.findViewById(R.id.tv_subtitle);
            LottieAnimationView lottie_alert = attemptDialogConfirmation.findViewById(R.id.lottie_alert);

            Gson gson = new Gson();
            TestSeriesWarningConfig testSeriesWarningConfig = gson.fromJson(firebaseJsonStr, TestSeriesWarningConfig.class);

            if(StringUtils.isNotEmpty(testSeriesWarningConfig.getTitle())) {
                tv_title.setText(StyledTextBuilder.fromJson(this, testSeriesWarningConfig.getTitle()));
            }
            if(StringUtils.isNotEmpty(testSeriesWarningConfig.getSubTitle())) {
                tv_subtitle.setText(StyledTextBuilder.fromJson(this, testSeriesWarningConfig.getSubTitle()));
            }
            lottie_alert.setProgress(0f);
            lottie_alert.playAnimation();

            final boolean[] isCancelled = {false};

            int limit = (int) testSeriesWarningConfig.getLoaderTime() != 0? (int)testSeriesWarningConfig.getLoaderTime() : 4000;
            progressIndicator.setMax(limit);
            progressIndicator.setProgress(0);

            ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
                    progressIndicator,
                    "progress",
                    0,
                    limit
            );
            progressAnimator.setDuration(limit);
            progressAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
            progressAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isCancelled[0]) {
                        listener.onDismiss(null);
                    }
                }
            });

            cv_cancel.setOnClickListener(v->{
                isCancelled[0] = true;
                if (progressAnimator != null && progressAnimator.isRunning()) {
                    progressAnimator.cancel();
                }
                dismissAttemptDialogConfirmation();
            });

            attemptDialogConfirmation.setOnDismissListener(l->{
                lottie_alert.cancelAnimation();
                lottie_alert.clearAnimation();
                isCancelled[0] = true;
                if (progressAnimator != null && progressAnimator.isRunning()) {
                    progressAnimator.cancel();
                }
            });

            attemptDialogConfirmation.show();
            progressAnimator.start();
        } catch (Exception ex){
            ex.printStackTrace();

        }
    }

    private void dismissAttemptDialogConfirmation() {
        if(attemptDialogConfirmation!=null && attemptDialogConfirmation.isShowing()) {
            attemptDialogConfirmation.dismiss();
            attemptDialogConfirmation = null;
        }
    }

    private void showAttemptWarningDialog(Map<String, String> dialogContent, String dialogTitleText, String dialogSubtitleText) {
        try {
            dismissAttemptWarningDialog();

            attemptWarningDialog = new Dialog(this);
            attemptWarningDialog.setContentView(R.layout.dialog_test_series_context_warning);
            attemptWarningDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            attemptWarningDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            attemptWarningDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            attemptWarningDialog.setCancelable(false);

            ImageView iv_cancel = attemptWarningDialog.findViewById(R.id.iv_cancel);
            TextView tv_title = attemptWarningDialog.findViewById(R.id.tv_title);
            TextView tv_subtitle = attemptWarningDialog.findViewById(R.id.tv_subtitle);
            LottieAnimationView lottie_alert = attemptWarningDialog.findViewById(R.id.lottie_alert);

            dialogTitleText=dialogContent!=null&&dialogContent.containsKey("title")?dialogContent.get("title"):dialogTitleText;
            dialogSubtitleText=dialogContent!=null&&dialogContent.containsKey("subtitle")?dialogContent.get("subtitle"):dialogSubtitleText;

            tv_title.setText(CommonMethods.isValidJson(dialogTitleText)?StyledTextBuilder.fromJson(this,dialogTitleText):dialogTitleText);

            tv_subtitle.setText(CommonMethods.isValidJson(dialogSubtitleText)?StyledTextBuilder.fromJson(this, dialogSubtitleText):dialogSubtitleText);


            lottie_alert.setProgress(0f);
            lottie_alert.playAnimation();

            iv_cancel.setOnClickListener(v->{
                dismissAttemptWarningDialog();
            });

            attemptWarningDialog.show();
        } catch (Exception ex){
            ex.printStackTrace();

        }
    }
    private void dismissAttemptWarningDialog() {
        if(attemptWarningDialog!=null && attemptWarningDialog.isShowing()) {
            attemptWarningDialog.dismiss();
            attemptWarningDialog = null;
        }
    }

}