package arivihan.technologies.doubtbuzzter2.activity;

import android.app.Dialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.LeaderboardMockTestAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.Configuration;
import arivihan.technologies.doubtbuzzter2.model.ConfigurationResult;
import arivihan.technologies.doubtbuzzter2.model.LeaderboardMockTestModel;
import arivihan.technologies.doubtbuzzter2.model.RankListDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CONFIGURATION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.OVERALL_USER_RANK_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_RANK_LIST;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MockTestLeaderBoard extends AppCompatActivity {

    RecyclerView rankerRecyclerview;
    List<LeaderboardMockTestModel> arrayList = new ArrayList<>();
    List<LeaderboardMockTestModel> arrayListCategoryRanks = new ArrayList<>();
    LeaderboardMockTestAdapter adapter;
    LottieAnimationView celebration, celebration2;
    RelativeLayout parentLayout;
    ShimmerFrameLayout Shimmerrank;
    TextView firstName,secondName,thirdName;
    TextView firstScore, secondScore, thirdScore;
    TextView firstTime, secondTime, thirdTime;
    ImageView firstImage, secondImage, thirdImage, intro_info;
    TextView showText, hideText;
    String TestId = "";
    CardView card;
    boolean daily = true;
    boolean dailyCall = false;
    Gson gson = new Gson();
    boolean isTopTen = false;
    CountDownTimer countDownTimer;
    Dialog dialogHome;
    String url;

    String snackBarTextDaily = "";
    String snackBarTextWeekly = "";
    boolean dailySnackbar = true, weeklySnackbar =true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_test_leader_board);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                countDownTimer.cancel();
                finish();
            }
        });

        TestId = getIntent().getStringExtra("TestId");
       // Log.d("MockTestLeader", TestId);

        if (StringUtils.isEmpty(TestId)) {
            TestId = "";
        }




        firstName = findViewById(R.id.firstName);
        firstScore = findViewById(R.id.firstPoint);
        firstTime = findViewById(R.id.firstTime);
        secondName = findViewById(R.id.secondName);
        secondScore = findViewById(R.id.secondPoint);
        secondTime = findViewById(R.id.secondTime);
        thirdName = findViewById(R.id.thirdName);
        thirdScore = findViewById(R.id.thirdPoint);
        thirdTime = findViewById(R.id.thirdTime);
        firstImage = findViewById(R.id.firstImage);
        secondImage = findViewById(R.id.secondImage);
        thirdImage = findViewById(R.id.thirdImage);
        parentLayout = findViewById(R.id.ParentLayout);
        Shimmerrank = findViewById(R.id.shimmer_rank);
        card = findViewById(R.id.weeklyCard);
        showText = findViewById(R.id.shownData);
        hideText = findViewById(R.id.hideData);
        intro_info = findViewById(R.id.intro_info);
        intro_info.setVisibility(View.GONE);

        intro_info.setOnClickListener(view -> showPopupDialog(url));

        rankerRecyclerview = findViewById(R.id.userPerformaceRecyclerview);
        celebration = findViewById(R.id.celebration);
        celebration2 = findViewById(R.id.celebration2);
        rankerRecyclerview.setLayoutManager(new LinearLayoutManager(MockTestLeaderBoard.this, LinearLayoutManager.VERTICAL ,false));

        adapter = new LeaderboardMockTestAdapter(arrayList,this);
        rankerRecyclerview.setAdapter(adapter);


        if(TestId.length() > 0) {
            fetchRankData();
        }
        else {
            fetchOverallRankDataDaily("WEEKLY");
            getConfigurationResult();

        }

        Animation fadeIn = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        String link = generateReferralLink("arivihan.technologies.doubtbuzzter2", "referralCode");
        Log.d("Referral", "Generated Link: " + link);



        countDownTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) { }
            public void onFinish() {
               celebration2.setVisibility(View.GONE);
            }
        };

        card.setOnClickListener(v -> {
            if (daily) {
                daily = false;
                showText.setText(R.string.daily);
                showText.startAnimation(fadeIn);
                hideText.startAnimation(fadeIn);
                hideText.setText(R.string.weekly);
                Shimmerrank.setVisibility(View.VISIBLE);
                fetchOverallRankDataDaily("DAILY");

                if (StringUtils.isNotEmpty(snackBarTextDaily) && dailySnackbar) {
                    dailySnackbar = false;
                    CommonMethods.showPositiveSnackbar(card, snackBarTextDaily);
                }


            } else {
                daily = true;
                showText.setText(R.string.weekly);
                showText.startAnimation(fadeIn);
                hideText.startAnimation(fadeIn);
                hideText.setText(R.string.daily);
                Shimmerrank.setVisibility(View.VISIBLE);
                fetchOverallRankDataDaily("WEEKLY");

                if (StringUtils.isNotEmpty(snackBarTextWeekly) && weeklySnackbar) {
                    weeklySnackbar = false;
                    CommonMethods.showPositiveSnackbar(card, snackBarTextWeekly);
                }

            }
        });


    }


    public String generateReferralLink(String packageName, String baseCode) {
        String randomPart = UUID.randomUUID().toString().substring(0, 8);
        String referralCode = baseCode + "-" + randomPart;

        String encodedCode = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            encodedCode = URLEncoder.encode(referralCode, StandardCharsets.UTF_8);
        }

        // Build Play Store link
        return "https://play.google.com/store/apps/details?id="
                + packageName
                + "&referrer="
                + encodedCode;
    }


    private void getConfigurationResult() {
        final Map<String, Object> map = new HashMap<>();
        map.put("featureKey", "OVERALL_RANKING_FINAL");
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
                            String enabled = keyValues.get("OVERALL_RANKING_SWITCH_ENABLED");

                            if ("true".equalsIgnoreCase(enabled)) {
                                card.setVisibility(View.VISIBLE);
                            } else {
                                card.setVisibility(View.VISIBLE);
                            }

                            url = keyValues.get("OVERALL_RANKING_INFO");

                            if (StringUtils.isNotEmpty(url)) {
                                intro_info.setVisibility(View.VISIBLE);
                            } else {
                                intro_info.setVisibility(View.GONE);

                            }
                            String myLang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                            String daily = keyValues.get("SNACK_BAR_TEXT_DAILY" + myLang);
                            String weekly = keyValues.get("SNACK_BAR_TEXT_WEEKLY" + myLang);


                            if (StringUtils.isNotEmpty(daily)) {
                                snackBarTextDaily = daily;
                            } else {
                                snackBarTextDaily = keyValues.get("SNACK_BAR_TEXT_DAILY");
                            }
                            if (StringUtils.isNotEmpty(weekly)) {
                                snackBarTextWeekly = weekly;
                            } else {
                                snackBarTextWeekly = keyValues.get("SNACK_BAR_TEXT_WEEKLY");
                                if (StringUtils.isNotEmpty(snackBarTextWeekly) && weeklySnackbar) {
                                    weeklySnackbar = false;
                                    CommonMethods.showPositiveSnackbar(card, snackBarTextWeekly);
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

    private void showPopupDialog(String url) {

        dialogHome = new Dialog(MockTestLeaderBoard.this);
        dialogHome.setContentView(R.layout.custom_home_dialogue);
        dialogHome.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialogHome.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogHome.setCancelable(false);


        CardView cancel = dialogHome.findViewById(R.id.cancel);
        ImageView image = dialogHome.findViewById(R.id.image);


        MyApplication.saveData(PreferenceKeys.POPUP_STORE_TIME.getKey(), System.currentTimeMillis() + 43200000);


        cancel.setOnClickListener(v -> dialogHome.dismiss());
    }


    private void fetchOverallRankDataDaily(String category) {

        Map<String, Object> map = new HashMap<>();
        map.put("rankCategory",category);

        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(OVERALL_USER_RANK_LIST, map), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        arrayListCategoryRanks.clear();
                        TypeToken<List<LeaderboardMockTestModel>> token = new TypeToken<List<LeaderboardMockTestModel>>() {};
                        arrayListCategoryRanks = gson.fromJson(response.toString(), token.getType());

                        for(int i = 0; i< arrayListCategoryRanks.size(); i++){
                            Log.d("notifyDataSetChanged", arrayListCategoryRanks.get(i).toString());
                            if(arrayListCategoryRanks.get(i).self && arrayListCategoryRanks.get(i).getRank() < 10){
                                if(arrayListCategoryRanks.get(i).getRank() == 1){
                                    if(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()) != null) {
                                        Glide.with(getApplicationContext()).load(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey())).into(firstImage);
                                    }
                                }
                                else{
                                    Glide.with(getApplicationContext()).load(R.drawable.avatar).into(firstImage);
                                }
                                if(arrayListCategoryRanks.get(i).getRank() == 2){
                                    if(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()) != null) {
                                        Glide.with(getApplicationContext()).load(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey())).into(secondImage);
                                    }
                                }
                                else{
                                    Glide.with(getApplicationContext()).load(R.drawable.avatar).into(firstImage);
                                }
                                if(arrayListCategoryRanks.get(i).getRank() == 3){
                                    if(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()) != null) {
                                        Glide.with(getApplicationContext()).load(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey())).into(thirdImage);
                                    }
                                }
                                else{
                                    Glide.with(getApplicationContext()).load(R.drawable.avatar).into(firstImage);
                                }
                                isTopTen = true;
                            }
                        }

                        try {

                            if (arrayListCategoryRanks.size() > 0) {
                                firstName.setText(arrayListCategoryRanks.get(0).getUserName());
                                firstScore.setText("" + arrayListCategoryRanks.get(0).getPoints());
                            }

                            if (arrayListCategoryRanks.size() > 1) {
                                secondName.setText(arrayListCategoryRanks.get(1).getUserName());
                                secondScore.setText("" + arrayListCategoryRanks.get(1).getPoints());
                            }

                            if (arrayListCategoryRanks.size() > 2) {
                                thirdName.setText(arrayListCategoryRanks.get(2).getUserName());
                                thirdScore.setText("" + arrayListCategoryRanks.get(2).getPoints());
                            }


                            arrayListCategoryRanks.remove(0);

                            if (arrayListCategoryRanks.size() >= 2) {
                                arrayListCategoryRanks.remove(0);
                                arrayListCategoryRanks.remove(0);
                            } else if (arrayListCategoryRanks.size() >= 1) {
                                arrayListCategoryRanks.remove(0);
                            }

                            if (isTopTen) {
//                                celebration.setVisibility(View.VISIBLE);
//                                celebration2.setVisibility(View.VISIBLE);
//                                celebration.animate();
//                                celebration2.animate();
                                countDownTimer.start();
                            }

                            Shimmerrank.setVisibility(View.GONE);
                            rankerRecyclerview.setVisibility(View.VISIBLE);



                            dailyCall = true;
                        } catch (Exception e) {
                            Log.e("notifyDataSetChanged", e.getMessage());
                        }

                        adapter = new LeaderboardMockTestAdapter(arrayListCategoryRanks, getApplicationContext());
                        rankerRecyclerview.setAdapter(adapter);
                    }
                }, new VolleyGlobalErrorListener(MockTestLeaderBoard.this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(parentLayout,error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);



    }

    private void fetchRankData() {
        arrayList = new ArrayList<>();
    
        Map<String, Object> map = new HashMap<>();
        map.put("testId",TestId);
        Log.d("MockTestLeaderAPI", TestId);

        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(USER_RANK_LIST, map), null,
                response -> {
                    arrayList.clear();
                    TypeToken<RankListDto> token = new TypeToken<RankListDto>() {};
                    RankListDto rankListDto = gson.fromJson(response.toString(), token.getType());
                    arrayList = rankListDto.getRankDtoList();

                    for(int i=0;i<arrayList.size();i++){
                        if(arrayList.get(i).self && arrayList.get(i).getRank() < 10){
                            if(arrayList.get(i).getRank() == 1){
                                if(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()) != null) {
                                    Glide.with(getApplicationContext()).load(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey())).into(firstImage);
                                }
                            }
                            else{
                                Glide.with(getApplicationContext()).load(R.drawable.avatar).into(firstImage);
                            }
                            if(arrayList.get(i).getRank() == 2){
                                if(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()) != null) {
                                    Glide.with(getApplicationContext()).load(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey())).into(secondImage);
                                }
                            }
                            else{
                                Glide.with(getApplicationContext()).load(R.drawable.avatar).into(firstImage);
                            }
                            if(arrayList.get(i).getRank() == 3){
                                if(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()) != null) {
                                    Glide.with(getApplicationContext()).load(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey())).into(thirdImage);
                                }
                            }
                            else{
                                Glide.with(getApplicationContext()).load(R.drawable.avatar).into(firstImage);
                            }
                            isTopTen = true;
                        }

                    }

                    firstName.setText(arrayList.get(0).getUserName());
                    firstScore.setText(""+arrayList.get(0).getPoints());

                    if (arrayList.size() > 1) {
                        secondName.setText(arrayList.get(1).getUserName());
                        secondScore.setText(""+arrayList.get(1).getPoints());
                    }

                    if (arrayList.size() > 2) {
                        thirdName.setText(arrayList.get(2).getUserName());
                        thirdScore.setText(""+arrayList.get(2).getPoints());
                    }


                    arrayList.remove(0);

                    if (arrayList.size() >= 2) {
                        arrayList.remove(0);
                        arrayList.remove(0);
                    } else if (arrayList.size() >= 1) {
                        arrayList.remove(0);
                    }

                    if(isTopTen) {
//                        celebration.setVisibility(View.VISIBLE);
//                        celebration2.setVisibility(View.VISIBLE);
//                        celebration.animate();
//                        celebration2.animate();
                        countDownTimer.start();
                    }

                    Shimmerrank.setVisibility(View.GONE);
                    rankerRecyclerview.setVisibility(View.VISIBLE);


                    adapter = new LeaderboardMockTestAdapter(arrayList,getApplicationContext());
                    rankerRecyclerview.setAdapter(adapter);


                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(parentLayout,error.getMessage());

            }
        });

        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }
}