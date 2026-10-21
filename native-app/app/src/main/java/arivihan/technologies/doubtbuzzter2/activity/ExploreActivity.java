package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MICRO_LECTURE_BY_ID;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.text.HtmlCompat;
import androidx.media3.common.util.UnstableApi;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.customView.CustomStepView;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.ExploreData;
import arivihan.technologies.doubtbuzzter2.model.ExploreModel;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.StepType;
import arivihan.technologies.doubtbuzzter2.model.StepViewModel;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class ExploreActivity extends BaseActivity {

    ImageView close;
    TextView stepText;
    TextView title;
    TextView titleBold;
    TextView subtitle;
    private boolean syllabusGenerated;
    TextView subtitleQuote;
    TextView btnText;
    ImageView gif;
    MaterialCardView prev;
    MaterialCardView next;
    MaterialCardView goTo;
    List<StepViewModel> list;
    List<ExploreModel> exploreModels;
    ExploreData endData;
    Gson gson;
    int current = 0;
    ImageView prevImage;
    ImageView nextImage;
    ExploreData currentData;
    JSONObject lectureCodeObject;
    String lectureCode;

    View stepsLayout;
    CustomStepView step1;
    CustomStepView step2;
    CustomStepView step3;
    CustomStepView step4;
    CustomStepView step5;
    LinearProgressIndicator linearProgressIndicator;
    int progress = 0;
    private ProgressDialog routineLoading;

    ShimmerFrameLayout shimmerFrameLayout;
    LinearLayout detailLayout;
    LinearLayout buttonLayout;
    ProgressBar progressBar;
    MicroLectureContentDto microLectureContentDto;

    ProgressDialog progressDialog;
    private final String TAG = ExploreActivity.class.getSimpleName();
    private ValueEventListener valueEventListener;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_explore);
        setUpViews();
        gson = new Gson();
        getData();
    }

    private void setUpViews() {
        close = findViewById(R.id.btn_close);
        stepText = findViewById(R.id.step_text);
        title = findViewById(R.id.title);
        titleBold = findViewById(R.id.title_bold);
        gif = findViewById(R.id.gif_image);
        subtitle = findViewById(R.id.subtitle);
        subtitleQuote = findViewById(R.id.subtitle_quote);
        btnText = findViewById(R.id.btn_text);
        list = new ArrayList<>();
        exploreModels = new ArrayList<>();
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        prevImage = findViewById(R.id.prev_image);
        nextImage = findViewById(R.id.next_image);
        goTo = findViewById(R.id.goTo);
            goTo.setOnClickListener(v -> {
                if (currentData != null && currentData.getType() != null
                        && StringUtils.isNotEmpty(currentData.getType())) {
                    goToPage(StepType.valueOf(currentData.getType()));
                }
            });
        next.setOnClickListener(v -> {
            try {
                if (current < exploreModels.size() - 1) {
                    current++;
                    updateStepsList(current);
                    updateSteps();
                    currentData = exploreModels.get(current).getData();
                    setData(currentData);
                    prev.setStrokeColor(getResources().getColor(R.color.button_dark));
                    prevImage.setImageDrawable(getResources().getDrawable(R.drawable.prev_dark));
                } else if (current == exploreModels.size() - 1) {
                    current++;
                    updateStepsList(current);
                    showEndScreen();
                    next.setStrokeColor(getResources().getColor(R.color.button_light));
                    nextImage.setImageDrawable(getResources().getDrawable(R.drawable.next_light));
                    stepsLayout.setVisibility(View.INVISIBLE);
                    stepText.setVisibility(View.GONE);
                }
                updateButtons();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        prev.setOnClickListener(v -> {
            try {
                if (current == exploreModels.size()) {
                    current--;
                    stepsLayout.setVisibility(View.VISIBLE);
                    stepText.setVisibility(View.VISIBLE);
                    removeEndScreen();
                    updateStepsList(current);
                } else if (current > 0) {
                    current--;
                    currentData = exploreModels.get(current).getData();
                    setData(currentData);
                    updateStepsList(current);
                    updateSteps();
                }
                updateButtons();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });

        close.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        stepsLayout = findViewById(R.id.steps);
        step1 = findViewById(R.id.step_1);
        step2 = findViewById(R.id.step_2);
        step3 = findViewById(R.id.step_3);
        step4 = findViewById(R.id.step_4);
        step5 = findViewById(R.id.step_5);
        linearProgressIndicator = findViewById(R.id.steps_progress);
        step1.setOnClickListener(v -> updateAll(0));
        step2.setOnClickListener(v -> updateAll(1));
        step3.setOnClickListener(v -> updateAll(2));
        step4.setOnClickListener(v -> updateAll(3));
        step5.setOnClickListener(v -> updateAll(4));

        shimmerFrameLayout = findViewById(R.id.shimmer_layout);
        shimmerFrameLayout.startShimmer();
        detailLayout = findViewById(R.id.detail_layout);
        buttonLayout = findViewById(R.id.btn_layout);
        progressBar = findViewById(R.id.progress);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        routineLoading = new ProgressDialog(this);
        routineLoading.setMessage(getString(R.string.preparing_your_test));
        routineLoading.setCanceledOnTouchOutside(true);

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    CommonMethods.showPositiveSnackbar(detailLayout, getString(R.string.InternetConnection));
                }
            }
            else{
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(detailLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

    }

    private void updateAll(int position) {
        try {
            current = position;
            currentData = exploreModels.get(position).getData();
            setData(currentData);
            updateStepsList(position);
            updateSteps();
            updateButtons();

            if (position == 4) {
                MyApplication.saveData(PreferenceKeys.EXPLORE_SCREEN_VISIT_COUNT.getKey(), 10);
                MyApplication.saveData(PreferenceKeys.EXPLORE_SCREEN_COMPLETED.getKey(), true);
            }
        } catch (Exception ignored){}
    }

    private void updateButtons() {
        if (current == 0) {
            prev.setStrokeColor(getResources().getColor(R.color.button_light));
            prevImage.setImageDrawable(getResources().getDrawable(R.drawable.prev_arrow_light));
        } else if (current == list.size()) {
            next.setStrokeColor(getResources().getColor(R.color.button_light));
            nextImage.setImageDrawable(getResources().getDrawable(R.drawable.next_light));
        } else {
            prev.setStrokeColor(getResources().getColor(R.color.button_dark));
            prevImage.setImageDrawable(getResources().getDrawable(R.drawable.prev_dark));
            next.setStrokeColor(getResources().getColor(R.color.button_dark));
            nextImage.setImageDrawable(getResources().getDrawable(R.drawable.next_arrow_dark));
        }

        if (current == 4) {
            MyApplication.saveData(PreferenceKeys.EXPLORE_SCREEN_VISIT_COUNT.getKey(), 10);
            MyApplication.saveData(PreferenceKeys.EXPLORE_SCREEN_COMPLETED.getKey(), true);
        }
    }

    private void removeEndScreen() {
        stepText.setVisibility(View.VISIBLE);
        subtitleQuote.setVisibility(View.GONE);
        currentData = exploreModels.get(current).getData();
        setData(currentData);
    }

    private void showEndScreen() {
        stepText.setVisibility(View.GONE);
        subtitleQuote.setVisibility(View.VISIBLE);
        currentData = endData;
        setData(endData);
    }
    private void getAPIDataByPhoneData(boolean checkInRoutine, String phoneNumber) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(phoneNumber)) {
            String PhoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
            map.put("phoneNumber", PhoneNumber);
        } else {
            map.put("phoneNumber", phoneNumber);
        }

        map.put("key", BuildConfig.API_KEY);
        map.put(PreferenceKeys.BOARD.getKey(), MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map), null,
                response -> {
                    Gson gson = new Gson();
                    Log.i("Api called", "Profile Fragment");
                    TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                    };
                    ApiWrappedResponse<UserDetailsResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                    UserDetailsResponse user = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                    syllabusGenerated = user != null && user.getUserDto() != null && Boolean.TRUE.equals(user.getUserDto().getSyllabusGenerated());
                    if (checkInRoutine && StringUtils.isEmpty(phoneNumber)) {
                        routineLoading.dismiss();
                        if (syllabusGenerated) {
                            observeForRoutine();
                        } else {
                            MyApplication.navigateToTodoSteps(this);
//                            Intent intent = new Intent(this, ToDoListSteps.class);
//                            intent.putExtra(PreferenceKeys.GO_TO_EXPLORE_SCREEN.getKey(), true);
//                            startActivity(intent);
                        }
                    } else if (StringUtils.isNotEmpty(phoneNumber)) {
                        String userId = (user != null && user.getUserDto() != null) ? user.getUserDto().getUserId() : "";
                        if (syllabusGenerated) {
                            Intent intent = new Intent(this, AdminToDoListMain.class);
                            intent.putExtra("UserPhoneNumber", userId);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(this, AdminEditRoutineActivity.class);
                            intent.putExtra("UserPhoneNumber", userId);
                            startActivity(intent);
                        }
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                if (checkInRoutine) {
                    routineLoading.dismiss();
                }
            }
        });
        myJsonObjectRequest2.setTag("ProfileFragment");
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }
    private void observeForRoutine() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists()) {
                        if (valueEventListener != null)
                            MyApplication.getReferenceForUserRoutine().removeEventListener(valueEventListener);

                        MyApplication.navigateToTodoSteps(ExploreActivity.this);
//                        Intent intent = new Intent(ExploreActivity.this, ToDoListSteps.class);
//                        intent.putExtra(PreferenceKeys.GO_TO_PROFILE_FRAGMENT.getKey(), true);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);

                    } else {
                        if (valueEventListener != null)
                            MyApplication.getReferenceForUserRoutine().removeEventListener(valueEventListener);
                        Intent intent = new Intent(ExploreActivity.this, ToDoListMain.class);
                        intent.putExtra("redirect_to_Explore", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        MyApplication application = (MyApplication) getApplication();
                        HomeScreenViewModel homeScreenViewModel = application.getHomeScreenViewModel();
                        homeScreenViewModel.getStudentProgressData(true);
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        MyApplication.getReferenceForUserRoutine().addValueEventListener(valueEventListener);

    }

    private void goToPage(StepType type) {
        switch (type) {
            case ULTIMATE_PLANNER:
                getAPIDataByPhoneData(true, null);
                break;
            case INTERACTIVE_LECTURES:
                progressDialog.show();
                if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())) {
                    lectureCode = MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                } else {
                    lectureCode = MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                }
                getLectureCodes(lectureCode);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.explore_micro_lecture_clicked.getActionName());
                break;
            case PERFORMANCE_ANALYSIS:
                Intent intent = new Intent(ExploreActivity.this, AnalyticsActivity.class);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.explore_analytics_clicked.getActionName());
                startActivity(intent);
                break;
            case DOUBT_SOLVING:
                Intent intent1 = new Intent(this, ChatScreenChooseSubjectForChat.class);
                intent1.putExtra("allowBack", true);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.explore_ask_doubt_clicked.getActionName());
                startActivity(intent1);
                break;
            case PRACTICE_PROBLEMS:
                progressDialog.show();
                openMockTest();
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.explore_mock_test_clicked.getActionName());
                break;
            case END_SCREEN:
                openWhatsApp();
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.explore_open_whatsApp_clicked.getActionName());
                break;
        }
    }

    private void openMockTest() {
        String value = MyApplication.getFirebaseRemoteConfig().getString("mock_test_codes_for_explore_screen");
        try {
            JSONObject jsonObject = new JSONObject(value);
            JSONObject mock;
            if (!MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())) {
                mock = jsonObject.getJSONObject(MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            } else {
                mock = jsonObject.getJSONObject("board_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            }
            CommonNavigator.navigateToTestSeriesListActivity(this,
                    mock.getString("code"), mock.getString("title"),
                    null, false, null, false, false, TAG);
        } catch (JSONException e) {
            progressDialog.dismiss();
            //throw new RuntimeException(e);
        }
    }

    private void getLectureCodes(String key) {
        String values = MyApplication.getFirebaseRemoteConfig().getString("lecture_code_for_explore_screen");
        try {
            JSONObject jsonObject = new JSONObject(values);
            getMicrolectureById(jsonObject.getString(key));
        } catch (JSONException e) {
//            throw new RuntimeException(e);
        }
    }

    private void getData() {
        String key = "explore_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey());
        String value = MyApplication.getFirebaseRemoteConfig().getString(key);
        if (StringUtils.isNotEmpty(value)) {
            shimmerFrameLayout.setVisibility(View.GONE);
            close.setVisibility(View.VISIBLE);
            stepsLayout.setVisibility(View.VISIBLE);
            detailLayout.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.VISIBLE);
            try {
                JSONObject jsonObject = new JSONObject(value);
                TypeToken<List<ExploreModel>> token = new TypeToken<>() {
                };
                exploreModels = gson.fromJson(jsonObject.getJSONArray("list").toString(), token.getType());
                TypeToken<ExploreData> tokenEndData = new TypeToken<ExploreData>() {
                };
                endData = gson.fromJson(jsonObject.getJSONObject("end").toString(), tokenEndData.getType());
                currentData = exploreModels.get(current).getData();

                for (ExploreModel exploreModel : exploreModels) {
                    Log.d("GIFStepWise", exploreModel.getData().getGif());
                }

                setData(currentData);
                setStepViewList(exploreModels.size());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setStepViewList(int size) {
        list.add(StepViewModel.builder().current(true).build());
        list.add(StepViewModel.builder().current(false).next(true).build());
        for (int i = 2; i < 5; i++) {
            list.add(new StepViewModel());
        }
        updateSteps();
    }

    private void setData(ExploreData exploreData) {
        progressBar.setVisibility(View.VISIBLE);
        if (StringUtils.isNotEmpty(exploreData.getStep_text()))
            stepText.setText(HtmlCompat.fromHtml(exploreData.getStep_text(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        if (StringUtils.isNotEmpty(exploreData.getTitle()))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                title.setText(Html.fromHtml(exploreData.getTitle(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                title.setText(Html.fromHtml(exploreData.getTitle()));
            }
        if (StringUtils.isNotEmpty(exploreData.getBold_title()))
            titleBold.setText(HtmlCompat.fromHtml(exploreData.getBold_title(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        if (StringUtils.isNotEmpty(exploreData.getSubtitle()))
            subtitle.setText(HtmlCompat.fromHtml(exploreData.getSubtitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        if (StringUtils.isNotEmpty(exploreData.getButton_text()))
            btnText.setText(HtmlCompat.fromHtml(exploreData.getButton_text(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        if (StringUtils.isNotEmpty(exploreData.getSubtitle_quote()))
            subtitleQuote.setText(HtmlCompat.fromHtml(exploreData.getSubtitle_quote(), HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (exploreData.getGif().equals("https://d2ztt6so6c3jo0.cloudfront.net/do_not_delete/explore_gifs/Step+1.gif")) {
            progressBar.setVisibility(View.GONE);
            Glide.with(this).load(AppCompatResources.getDrawable(this, R.drawable.step_one_explore)).into(gif);
        } else if (exploreData.getGif().equals("https://d2ztt6so6c3jo0.cloudfront.net/do_not_delete/explore_gifs/Step+2.gif")) {
            progressBar.setVisibility(View.GONE);
            Glide.with(this).load(AppCompatResources.getDrawable(this, R.drawable.step_two_explore)).into(gif);
        } else {
            Glide.with(this).asGif().load(exploreData.getGif()).listener(new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).diskCacheStrategy(DiskCacheStrategy.ALL).into(gif);
        }

    }

    private void updateStepsList(int position) {
        for (int i = 0; i < position; i++) {
            list.get(i).setDone(true);
        }
        for (int i = position; i < list.size(); i++) {
            list.get(i).setDone(false);
            list.get(i).setNext(false);
            list.get(i).setCurrent(false);
        }
        if (position < list.size())
            list.get(position).setCurrent(true);
        if (position < list.size() - 1) {
            list.get(position + 1).setNext(true);
        }

    }

    private void getMicrolectureById(String microLectureId) {
        Map<String, Object> map = new HashMap<>();
        map.put("microLectureId", microLectureId);
        Log.d(TAG, "getMicrolectureById: "+map.toString());
        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(MICRO_LECTURE_BY_ID, map), null,
                response -> {
                    progressDialog.dismiss();
                    TypeToken<MicroLectureContentDto> token = new TypeToken<MicroLectureContentDto>() {
                    };
                    microLectureContentDto = gson.fromJson(response.toString(), token.getType());
                    microLectureContentDto.setId(microLectureId.replace("HINDI", "").trim());
                    @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(this, VideoPlayerActivity.class);
                    intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
                    intent.putExtra("shouldNotResume", true);
                    intent.putExtra("doubtVideo", false);
                    intent.putExtra("isAskDoubtActivity", false);
                    intent.putExtra("video_uri", microLectureContentDto.getVideoUrl());
                    intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), microLectureContentDto.getDefaultStartTime());
                    intent.putExtra("video_ending", microLectureContentDto.getVideoCompletionEndPos());
                    startActivity(intent);
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                progressDialog.dismiss();
                CommonMethods.showPositiveSnackbar(detailLayout, error.getMessage());
                Log.e("Error", "postError : " + error.getMessage());
            }
        });
        jsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void updateSteps() {
        step1.changeState(list.get(0));
        step2.changeState(list.get(1));
        step3.changeState(list.get(2));
        step4.changeState(list.get(3));
        step5.changeState(list.get(4));
        progress = (100 * current) / 4;
        linearProgressIndicator.setProgress(progress);
    }

    private void openWhatsApp() {
        try {
            String url = "https://api.whatsapp.com/send?phone=" + MyApplication.getPhoneNumberWith91();
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.whatsapp");
            intent.setData(uri);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No Whatsapp Found", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        super.onDestroy();
    }

}