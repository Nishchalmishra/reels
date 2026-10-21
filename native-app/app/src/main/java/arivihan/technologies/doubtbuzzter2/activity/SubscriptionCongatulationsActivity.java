package arivihan.technologies.doubtbuzzter2.activity;

import static com.facebook.internal.Utility.logd;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION_BENEFITS;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.SubscriptionBenefitAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionBenefitsModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class SubscriptionCongatulationsActivity extends AppCompatActivity implements SubscriptionBenefitAdapter.BenefitClickListener {

    private static final String TAG = SubscriptionCongatulationsActivity.class.getSimpleName();
    LinearLayout backButton;
    MaterialCardView getStarted;
    RecyclerView recyclerView;
    LottieAnimationView confetti1;
    LottieAnimationView confetti2;
    LottieAnimationView confetti3;
    MediaPlayer mediaPlayer;
    TextView congrats_text1, congrats_text2;
    SubscriptionBenefitAdapter subscriptionBenefitAdapter;
    List<SubscriptionBenefitsModel> subscriptionBenefitsModelList;
    Gson gson = new Gson();
    String planId, congratulationHeader, congratulationSubHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_congatulations);
        initializeViews();
        getBenefits();
        //getBenefit("2c9f95937fd918a3017fd95801f90001");
        startAnimAndSound();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(!getIntent().getBooleanExtra("fromMySubscriptions",false)){
                    goToHome();
                }else {
                    finish();
                }

            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button_layout);
        getStarted = findViewById(R.id.get_started);
        recyclerView = findViewById(R.id.benefits_rv);
        recyclerView.setNestedScrollingEnabled(false);
        confetti1 = findViewById(R.id.confetti1);
        confetti2 = findViewById(R.id.confetti2);
        confetti3 = findViewById(R.id.confetti3);

        congrats_text1 = findViewById(R.id.congrats_text1);
        congrats_text2 = findViewById(R.id.congrats_text2);

        backButton.setOnClickListener(view -> goToHome());
        getStarted.setOnClickListener(view -> goToHome());

    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void getBenefits() {

        Uri deepLink = getIntent().getData();
        if (deepLink != null) {
            Log.d("fetchTransactionHistory:", deepLink.toString());
            planId = deepLink.getQueryParameter("planId");
            congratulationHeader = deepLink.getQueryParameter("congratulationHeader");
            congratulationSubHeader = deepLink.getQueryParameter("congratulationSubHeader");
            fetchBenefits(planId);
        } else {
            planId = getIntent().getStringExtra("planId");
            congratulationHeader = getIntent().getStringExtra("congratulationHeader");
            congratulationSubHeader = getIntent().getStringExtra("congratulationSubHeader");
            if (getIntent().getParcelableArrayListExtra("benefits")!=null) {
                subscriptionBenefitsModelList = getIntent().getParcelableArrayListExtra("benefits");
                initializeRecyclerView();
            }
            if((subscriptionBenefitsModelList ==null || subscriptionBenefitsModelList.isEmpty()) && planId!=null) {
                fetchBenefits(planId);
            }
        }

        setHeaders();
        //Removing the Firebase RDB Pending Subscription Status
        if(!StringUtils.isEmpty(planId)){
            MyApplication.getPendingSubscriptionDatabaseReference(planId).removeValue();
        }

    }

    private void setHeaders() {
        if(!StringUtils.isEmpty(congratulationHeader)) {
            congrats_text1.setText(congratulationHeader);
        }
        if(!StringUtils.isEmpty(congratulationSubHeader)) {
            congrats_text2.setText(congratulationSubHeader);
        }
    }

    private void fetchBenefits(String planId) {
        Map<String, Object> map = new HashMap<>();
        map.put("planId", planId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION_BENEFITS, map), null,
                response -> {
                    try {
                        Log.d("fetchTransactionHistory:", response.toString());
                        Type listType = new TypeToken<List<SubscriptionBenefitsModel>>() {
                        }.getType();
                        ArrayList<SubscriptionBenefitsModel> list = gson.fromJson(String.valueOf(response.get("data")), listType);
                        if (list != null) {
                            subscriptionBenefitsModelList = list;
                            congratulationHeader = (response.has("congratulationHeader") && !response.isNull("congratulationHeader"))
                                    ? response.getString("congratulationHeader") : "";
                            congratulationSubHeader = (response.has("congratulationSubHeader") && !response.isNull("congratulationSubHeader"))
                                    ? response.getString("congratulationSubHeader") : "";
                            setHeaders();
                            initializeRecyclerView();
                            Log.d("fetchTransactionHistory:", "Fetched " + list.size() + " benefits");

                        }
                    }catch (Exception e) {
                        Log.e("fetchTransactionHistory:", "Parsing error: " + e.getMessage(), e);
                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.e("fetchTransactionHistory:", "Volley error: " + error.getMessage(), error);
                Toast.makeText(MyApplication.getInstance().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }


    private void initializeRecyclerView() {
        if (subscriptionBenefitsModelList != null) {
            subscriptionBenefitAdapter = new SubscriptionBenefitAdapter(this, new ArrayList<>(subscriptionBenefitsModelList), this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(subscriptionBenefitAdapter);
        }
    }

    private void startAnimAndSound() {
        confetti1.playAnimation();
        confetti2.playAnimation();
        confetti3.playAnimation();
        mediaPlayer = MediaPlayer.create(this, R.raw.ding);
        mediaPlayer.start();
    }

    @Override
    public void onGoToClick(SubscriptionBenefitsModel subscriptionBenefitsModel) {
        try {
            Intent intent = new Intent(this, ActivityLauncher.resolveClass(subscriptionBenefitsModel.getClassName()));
            if (subscriptionBenefitsModel.getActivityParams() != null) {
                for (String key : subscriptionBenefitsModel.getActivityParams().keySet()) {
                    Object value = subscriptionBenefitsModel.getActivityParams().get(key);
                    if (value instanceof String) {
                        intent.putExtra(key, (String) subscriptionBenefitsModel.getActivityParams().get(key));
                    } else if (value instanceof Integer) {
                        intent.putExtra(key, (Integer) subscriptionBenefitsModel.getActivityParams().get(key));
                    } else if (value instanceof Boolean) {
                        intent.putExtra(key, (Boolean) subscriptionBenefitsModel.getActivityParams().get(key));
                    }
                }

            }
            startActivity(intent);
        } catch (Exception e) {
            logd("onGoToClick: ", e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}