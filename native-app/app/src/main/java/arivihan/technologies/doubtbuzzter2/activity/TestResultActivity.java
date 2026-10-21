package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CHECK_ATL_TEST_STATUS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SOLUTIONS_SCREEN;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.fragments.ASATBottomFragment;
import arivihan.technologies.doubtbuzzter2.fragments.ASATVideoFragment;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.asat.ASATScreenProcessInfo;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class TestResultActivity extends AppCompatActivity {

    private LottieAnimationView confetti1, confetti2, confetti3;
    private MediaPlayer mediaPlayer;
    private boolean isResultScreen = false;
    private static final String ASAT_SCREEN_PROCESS_INFO = "asat_screen_process_info";
    private static final String ASAT_SCREEN_TYPE_RESULT = "asat_screen_type_result";
    private static final String ASAT_RULES = "asat_rules";
    private static final String ASAT_SYLLABUS = "ASAT_SYLLABUS";
    private static final String ASAT_TOPPERS_LIST = "asat_toppers_list";
    private TextView titles,tv_user_score,tv_total_score;
    LinearLayout ll_score_card;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_result_layout);
        CardView cv_view_result=findViewById(R.id.cv_view_result);
        titles=findViewById(R.id.titles);
        tv_user_score=findViewById(R.id.tv_user_score);
        tv_total_score=findViewById(R.id.tv_total_score);
        ll_score_card=findViewById(R.id.ll_score_card);

        cv_view_result.setOnClickListener(v->{
            goToTestResult(MyApplication.getFirebaseRemoteConfig().getString("atl_result_link"));
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(TestResultActivity.this, HomeScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        try {
            getTestSeriesScoreCard();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void goToTestResult(String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            if (intent.resolveActivity(this.getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Unable to redirect", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("DeeplinkError", "Failed to open deeplink", e);
        }
    }

    private void getTestSeriesScoreCard() throws JSONException {
        if (isFinishing()) return;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        String jsonString = MyApplication.getFirebaseRemoteConfig().getString("atl_test_series_categoryId");
        JSONObject jsonObject = new JSONObject(jsonString);
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

        JSONObject langObject = jsonObject.has(lang)
                ? jsonObject.getJSONObject(lang)
                : jsonObject.optJSONObject("en");
        if (langObject == null) {
            Log.e("navigateToTestSeries", "Language object not found for: " + lang);
            return;
        }
        String categoryId = langObject.optString("testSeriesCategroyId", "");

        if (categoryId.isEmpty()) {
            Log.e("navigateToTestSeries", "Category ID not found in JSON for language: " + lang);
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("testCategoryId", categoryId);
        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(GET_SOLUTIONS_SCREEN,params),
                null,
                response -> {
                    try {
                        Log.d("issueinresponse", "getTestSeriesScoreCard: "+response);
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        boolean success = response.optBoolean("success", false);
                        if (success) {
                            JSONObject data = response.optJSONObject("data");
                            String videoUrl = data.optString("videoUrl");
                            String thumbnail = data.optString("thumbnail");
                            Gson gson = new Gson();
                            ASATScreenProcessInfo dummyInfo = gson.fromJson(data.toString(), ASATScreenProcessInfo.class);
                            JSONObject userAsatInfo = data.optJSONObject("userAsatInfo");
                            if (userAsatInfo != null) {
                                JSONObject testInfo = userAsatInfo.optJSONObject("testInfo");
                                if (testInfo != null) {
                                    int marks = testInfo.optInt("marks", 0);
                                    int totalMarks = testInfo.optInt("totalMarks", 0);
                                    tv_user_score.setText(String.valueOf(marks));
                                    tv_total_score.setText(String.valueOf(totalMarks));
                                } else {
                                    tv_user_score.setText("0");
                                    tv_total_score.setText("0");
                                }
                            }
                            MyApplication.saveData(PreferenceKeys.TO_SHOW_NEW_BADGE_ATL.getKey(),true);
                            ll_score_card.setVisibility(View.VISIBLE);
                            setBottomFragment(dummyInfo, false);
                            setScoreCardSection(dummyInfo);
                            setVideoFragment(videoUrl, thumbnail, false);
                            initConfettiAnimations();
                            Log.d("TestSeriesScoreCard", "API success");
                        } else {
                            Toast.makeText(this, "Failed to load test series data", Toast.LENGTH_SHORT).show();
                            Log.e("TestSeriesScoreCard", "API returned success=false");
                        }
                    } catch (Exception e) {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        Log.e("TestSeriesScoreCard", "Error parsing response", e);
                    }
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("TestSeriesScoreCard", "VolleyError: " + error.getMessage(), error);
                    }
                }
        );

        request.setTag("TestSeriesSection");
        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void setScoreCardSection(ASATScreenProcessInfo asatScreenProcessInfo) {
        titles.setText(StyledTextBuilder.fromJson(this, asatScreenProcessInfo.getText2()));
    }
    private void initConfettiAnimations() {
        confetti1 = findViewById(R.id.confetti1);
        confetti2 = findViewById(R.id.confetti2);
        confetti3 = findViewById(R.id.confetti3);

        confetti1.playAnimation();
        confetti2.playAnimation();
        confetti3.playAnimation();
        mediaPlayer = MediaPlayer.create(this, R.raw.ding);
        mediaPlayer.start();
    }

    private void setVideoFragment(String url, String thumbnail, boolean isBack) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("thumbnail", thumbnail);
        bundle.putBoolean(ASAT_SCREEN_TYPE_RESULT, true);

        ASATVideoFragment asatVideoFragment = new ASATVideoFragment();
        asatVideoFragment.setArguments(bundle);

        startTransitionVideo(asatVideoFragment, isBack);
    }

    private void setBottomFragment(ASATScreenProcessInfo asatScreenProcessInfo, boolean isBack) {
        Gson gson = new Gson();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ASAT_SCREEN_PROCESS_INFO, asatScreenProcessInfo);
        bundle.putBoolean(ASAT_SCREEN_TYPE_RESULT, true);
        bundle.putBoolean("hide_contact_section",true);
        bundle.putString(ASAT_RULES, asatScreenProcessInfo.getRules());
        bundle.putString(ASAT_TOPPERS_LIST, gson.toJson(asatScreenProcessInfo.getToppersList()));

        ASATBottomFragment asatBottomFragment = new ASATBottomFragment();
        asatBottomFragment.setArguments(bundle);
        startTransitionBottom(asatBottomFragment, isBack);
    }

    /**
     * Handles fragment transition for Video
     */
    private void startTransitionVideo(ASATVideoFragment fragment, boolean isBack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Add animations if needed
        transaction.replace(R.id.fragment_container_view_video, fragment);
        if (!isBack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    /**
     * Handles fragment transition for Bottom
     */
    private void startTransitionBottom(ASATBottomFragment fragment, boolean isBack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_view_bottom, fragment);
        if (!isBack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void dismissDialogSafely() {
        if (progressDialog != null && progressDialog.isShowing() && !isFinishing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialogSafely();
    }
}
