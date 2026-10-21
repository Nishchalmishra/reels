package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.BUY_SUBSCRIPTION_V2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SUBSCRIPTION_V2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.SUBSCRIPTION;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.RazorpayPaymentGatewayActivity;
import arivihan.technologies.doubtbuzzter2.adapter.SubscriptionFeatureAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SubscriptionOptionAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SubscriptionPlanAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SubscriptionTypeAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.CustomFullScreenLoader;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.BuySubscriptionModel;
import arivihan.technologies.doubtbuzzter2.model.CouponDto;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.PlanLevel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionBenefitsModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionOptions;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionOverview;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlans;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPostModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPreModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionRenewalReward;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SubscriptionActivityNew extends BaseActivity implements
        SubscriptionTypeAdapter.TypeClickListener,
        SubscriptionOptionAdapter.OptionClickListener,
        SubscriptionPlanAdapter.PlanClickListener {

    private final String TAG = SubscriptionActivityNew.class.getSimpleName();
    private final String SUBSCRIPTION_PLAN_TAG = "SUBSCRIPTION_PLAN_TAG";
    String planLevel, couponCode;
    PlanLevel planLevelEnum;
    boolean boardSelected;
    boolean comboSelected = false;
    String selectedPlanId = "";
    LinearLayout backButton;
    RecyclerView planTypeRv;
    SubscriptionTypeAdapter subscriptionTypeAdapter;
    ArrayList<SubscriptionType> subscriptionTypes;
    RecyclerView planOptionsRv;
    SubscriptionOptionAdapter subscriptionOptionAdapter;
    ArrayList<SubscriptionOptions> subscriptionOptions;
    RecyclerView featuresRv;
    SubscriptionFeatureAdapter subscriptionFeatureAdapter;
    ArrayList<String> subscriptionFeatures;
    RecyclerView plansRv;
    SubscriptionPlanAdapter subscriptionPlanAdapter;
    ArrayList<SubscriptionPlans> subscriptionPlans;
    TextView selectedMonths;
    TextView costPrice;
    TextView finalPrice;
    TextView overallCostPrice;
    TextView overallFinalPrice;
    TextView selectedPlanName;
    ConstraintLayout enrollButton;
    SubscriptionModel subscriptionModel;
    SubscriptionRenewalReward subscriptionRenewalReward = null;
    TextView countDownText;
    TextView limitedOfferText;
    CountDownTimer countDownTimer;
    Gson gson;
    ScrollView scrollView;
    ShimmerFrameLayout shimmerLayout;
    LottieAnimationView featureAnimation;
    boolean dataLoaded;
    ConstraintLayout ParentLayout;
    IntentFilter intentFilter;
    TextView talkToUs;
    MaterialCardView cv_call_back;

    ProgressDialog progressDialog;
    LinearLayout talkToUsLayout;
    NetworkStatusLiveData networkStatusLiveData;

    Map<String, SubscriptionModel> modelMap = new HashMap<>();
    boolean fromUnlockText;
    boolean fromMySubscription;
    int duration;
    private LinearLayout discount_layout;
    private TextView discount_text;
    private HowToVideoView howToVideoView;
    private View how_to_video_view;
    private CardView how_to_card;
    private MaterialCardView bottom_layout;
    CustomFullScreenLoader screenLoader;
    MaterialCardView cv_subscription_options;
    LinearLayout ll_offer_timer_layout;

    // couponCode
    private EditText coupon_code;
    private MaterialCardView cv_apply_card, cv_coupon_price_breakdown;
    private LinearLayout lv_coupon_discount_layout, tnc_checkbox_layout;
    private TextView tv_subscription_price, total_price, discount_percent, discount_price;
    private TextView applied_text, apply_code_text, tnc, privacy_policy;
    private MaterialCardView tnc_checkbox;
    String couponId;

    String couponPlaceholderText;

    BuySubscriptionModel buySubscriptionModel;
    SubscriptionPlans selectedSubscription;
    boolean isPayingThroughQRCode;
    private ActivityResultLauncher<Intent> qrPaymentLauncher;
    private boolean tncChecked = false;

    UserSubscriptionDto userSubscriptionDto;
    private RedirectionSource redirectionSource;
    private String analyticsSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_new);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (howToVideoView != null && howToVideoView.videoView != null
                        && howToVideoView.videoView.getVisibility() == View.VISIBLE && howToVideoView.expanded) {
                    howToVideoView.resize();
                } else {
                    destroyHowTo();
                    finish();
                }
            }
        });

        bottom_layout = findViewById(R.id.bottom_layout);
        backButton = findViewById(R.id.back_button_layout);
        ll_offer_timer_layout = findViewById(R.id.offer_timer_layout);
        //coupon code view

        coupon_code = findViewById(R.id.coupon_code);
        cv_apply_card = findViewById(R.id.apply_card);
        cv_apply_card.setOnClickListener(view -> {
            String couponCode = coupon_code.getText().toString().trim();
            if (!couponCode.isEmpty()) {
                couponId = couponCode;
                checkCouponCodeValidity();
            } else {
                Toast.makeText(this, "Please enter a coupon code", Toast.LENGTH_SHORT).show();
            }
        });
        cv_coupon_price_breakdown = findViewById(R.id.cv_coupon_price_breakdown);
        lv_coupon_discount_layout = findViewById(R.id.coupon_discount_layout);
        tv_subscription_price = findViewById(R.id.subscription_price);
        total_price = findViewById(R.id.total_price);
        discount_percent = findViewById(R.id.discount_percent);
        discount_price = findViewById(R.id.discount_price);

        applied_text = findViewById(R.id.applied_text);
        apply_code_text = findViewById(R.id.apply_code_text);

        tnc_checkbox_layout = findViewById(R.id.tnc_checkbox_layout);
        tnc_checkbox = findViewById(R.id.tnc_checkbox);

        tnc = findViewById(R.id.tnc);
        privacy_policy = findViewById(R.id.privacy_policy);
        backButton.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        how_to_card = findViewById(R.id.how_to_card);
        if (MyApplication.getFirebaseRemoteConfig().getBoolean("hide_how_to_card_subscription") ||
                MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos"))
            how_to_card.setVisibility(View.GONE);
        how_to_video_view = findViewById(R.id.how_to_video_view);
        discount_layout = findViewById(R.id.discount_layout);
        cv_subscription_options = findViewById(R.id.cv_subscription_options);
        discount_text = findViewById(R.id.discount_text);
        planTypeRv = findViewById(R.id.plan_type_rv);
        planOptionsRv = findViewById(R.id.plan_options_rv);
        featuresRv = findViewById(R.id.feature_rv);
        plansRv = findViewById(R.id.plans_rv);
        overallCostPrice = findViewById(R.id.overall_cost_price);
        overallFinalPrice = findViewById(R.id.overall_final_price);
        selectedPlanName = findViewById(R.id.selected_plan_name);
        enrollButton = findViewById(R.id.join_campaign_button);
        TextView enrollButtonText = enrollButton.findViewById(R.id.join_text);
        ImageView enrollButtonIcon = enrollButton.findViewById(R.id.join_icon);

        String[] enrollTexts = {
                getString(R.string.join_now_text_1),
                getString(R.string.join_now_text_2)
        };

        Integer[] enrollIcons = {
                R.drawable.join_now_icon_2,
                R.drawable.join_now_icon_1
        };

        Integer[] enrollBackgrounds = {
                R.drawable.join_now_gradient_forward,
                R.drawable.join_now_gradient_backward
        };

        enrollButtonText.setText(enrollTexts[0]);
        enrollButtonIcon.setImageResource(enrollIcons[0]);
        if(!MyApplication.isCompetitiveProfile()){
            enrollButton.setBackgroundResource(enrollBackgrounds[0]);
            startButtonAnimation(enrollButton, enrollButtonText, enrollButtonIcon, enrollTexts, enrollIcons, enrollBackgrounds, 2000);
        }else {
            enrollButton.setBackgroundResource(enrollBackgrounds[1]);
        }
        selectedMonths = findViewById(R.id.selected_months);
        costPrice = findViewById(R.id.overview_cost_price);
        finalPrice = findViewById(R.id.overview_final_price);
        countDownText = findViewById(R.id.countdown_text);
        limitedOfferText = findViewById(R.id.limited_offer_text);
        scrollView = findViewById(R.id.scroll_view);
        shimmerLayout = findViewById(R.id.shimmer_layout);
        featureAnimation = findViewById(R.id.feature_animation);
        ParentLayout = findViewById(R.id.parent_layout);
        talkToUs = findViewById(R.id.talk_to_us);
        cv_call_back = findViewById(R.id.cv_call_back);
        TextView tv_talk_to_us_sub_title = findViewById(R.id.tv_talk_to_us_sub_title);
        TextView tv_talk_to_us_title = findViewById(R.id.tv_talk_to_us_title);
        ImageView iv_talk_to_us = findViewById(R.id.iv_talk_to_us);
        setUptextoTalkToUslayout();
        cv_call_back.setOnClickListener(v -> {
            openWhatsapp();
        });
        talkToUsLayout = findViewById(R.id.talk_to_us_layout);
        if (MyApplication.getFirebaseRemoteConfig().getBoolean("show_talk_to_us_in_subscription")) {
            talkToUsLayout.setVisibility(View.VISIBLE);
        } else {
            talkToUsLayout.setVisibility(View.GONE);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        screenLoader = new CustomFullScreenLoader(this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastStringForAction);

//        enrollButton.setOnClickListener(v -> {
//
//            if (CollectionUtils.isEmpty(subscriptionPlans)) {
//                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
//            }
//            String selectedPlanId = "";
//            for (SubscriptionPlans subscriptionPlan : subscriptionPlans) {
//                if (subscriptionPlan.isSelected()) {
//                    selectedPlanId = subscriptionPlan.getPlanId();
//                }
//            }
//
//            Intent intent = new Intent(this, BuySubscriptionActivity.class);
//            intent.putExtra("planId", selectedPlanId);
//            if (subscriptionRenewalReward != null) {
//                intent.putExtra("couponCode", subscriptionRenewalReward.getCouponCode());
//                intent.putExtra("couponCodeText", subscriptionRenewalReward.getCouponCodeText());
//            }
//            Map<String, String> params = new HashMap<>();
//            params.put("Plan_Id", selectedPlanId);
//            MyApplication.logFirebaseBundleCreation("Enroll_subscription_button_clicked", params);
//            destroyHowTo();
//            startActivity(intent);
//        });

        Uri data = getIntent().getData();

        Object sourceExtra = getIntent().getSerializableExtra(SubscriptionNavigationHelper.EXTRA_SOURCE);
        redirectionSource = null;
        if (sourceExtra instanceof RedirectionSource) {
            redirectionSource = (RedirectionSource) sourceExtra;
        }

        if (redirectionSource != null) {
            analyticsSource = redirectionSource.getFormattedValue();
        } else if (sourceExtra instanceof String) {
            analyticsSource = "REDIRECTED_FROM_" + ((String) sourceExtra).toUpperCase();
        } else {
            analyticsSource = AnalyticsEvent.NEET_SUBSCRIPTION_SCREEN.name();
        }

        if (savedInstanceState == null) {
            ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.NEET_SUBSCRIPTION_SCREEN.name(), analyticsSource);

            Map<String, String> subNavParams = new HashMap<>();
            String sourceName = "unknown";
            if (redirectionSource != null) {
                sourceName = redirectionSource.getFirebaseName();
            } else if (sourceExtra instanceof String) {
                sourceName = (String) sourceExtra;
            }
            subNavParams.put("source", sourceName);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.sub_screen_opened.getActionName(), subNavParams);
        }

        if (data != null) {
            // From deep link
            planLevel = data.getQueryParameter("plan_level");

            Log.i("COUPON_CODE","CODE :: " + couponId);

            fromUnlockText = data.getBooleanQueryParameter("fromUnlockText", false);
            fromMySubscription = data.getBooleanQueryParameter("fromMySubscription", false);

            // For integer (duration), you must manually parse
            String durationParam = data.getQueryParameter("duration");
            duration = (durationParam != null && CommonMethods.isInteger(durationParam)) ? Integer.parseInt(durationParam) : 0;

            boardSelected = data.getBooleanQueryParameter("boardSelected", true);
            comboSelected = data.getBooleanQueryParameter("comboSelected", false);
            couponCode = data.getQueryParameter("couponCode");

        } else {
            // From intent extras
            planLevel = getIntent().getStringExtra("plan_level");
            fromUnlockText = getIntent().getBooleanExtra("fromUnlockText", false);
            fromMySubscription = getIntent().getBooleanExtra("fromMySubscription", false);
            duration = getIntent().getIntExtra("duration", 0);
            boardSelected = getIntent().getBooleanExtra("boardSelected", true);
            comboSelected = getIntent().getBooleanExtra("comboSelected", false);
            couponCode = getIntent().getStringExtra("couponCode");
        }
        Log.d(TAG,"---> "+planLevel+", "+couponCode);
        if (StringUtils.isNotEmpty(planLevel)) {
            planLevelEnum = PlanLevel.valueOf(planLevel);
        }
        setFeatureAnimation();
        Log.d("courseidissue", "onViewCreated activitySubscription: " + MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey()));
        setSubscriptionFeatures();
        setSubscriptionPlans();
        setSubscriptionOptions();
        setTimer();

        gson = new Gson();
        subscriptionModel = new SubscriptionModel();
        createNewRequest();
        if ((fromUnlockText || fromMySubscription) && (!MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()) || !boardSelected)) {
            subscriptionModel.setBoardSelected(false);
            boardSelected = false;
        }
        shimmerLayout.startShimmer();
        shimmerLayout.setVisibility(View.VISIBLE);
        getPlanData();
        setTermsAndCondition();
        startPayment();
        qrPaymentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            isPayingThroughQRCode = false;
//                            Intent data = result.getData();
//                            int paymentAmount = data.getIntExtra("paymentAmount", 0);
//                            int finalAmount = data.getIntExtra("finalAmount", 0);
//                            boolean isFromBottomOfferSheet = data.getBooleanExtra("isFromBottomOfferSheet", false);
                            MyApplication.saveData(PreferenceKeys.OTHER_PAYMENT.getKey(), false);
                            try {
                                buyNowPreCall();
                            } catch (Exception e) {
                                Toast.makeText(this, "An error occurred from our end, please try again later", Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
        );

        how_to_card.setOnClickListener(view -> {
            logFirebaseEvent();
            howToVideoView = HowToVideoView.getInstance(this);
            howToVideoView.setView(this, how_to_video_view);
            howToVideoView.setType(SUBSCRIPTION, true);
            howToVideoView.setButtonText(getString(R.string.Join_Now));
        });
        howToVideoView = HowToVideoView.getInstance(this);
        howToVideoView.setView(this, how_to_video_view);
        int count = MyApplication.getIntData(SUBSCRIPTION.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        Log.d("debugcount", "getHowToVideoType: " + count + "type" + SUBSCRIPTION.name());
        if (count < 0)
            count = 0;
        if (count <= 1) {
            howToVideoView.setType(SUBSCRIPTION, false);
            howToVideoView.setButtonText(getString(R.string.Join_Now));
            MyApplication.saveData(SUBSCRIPTION.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()), count + 1);
        }
        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    if (!dataLoaded) {
                        getPlanData();
                    }
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });
    }

   private void setUptextoTalkToUslayout() {
        try {
            String jsonStr = MyApplication.getFirebaseRemoteConfig()
                    .getString("subscription_plan_screen_contact_us_section");
            Log.d(TAG, "Raw JSON from Remote Config: " + jsonStr);
            JSONObject root = new JSONObject(jsonStr);
            String sectionKey = MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey());
            JSONObject sectionObj = root.getJSONObject(sectionKey);
            Log.d(TAG, "Selected course ID: " + sectionObj);
            String langCode = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            JSONObject langObj = sectionObj.getJSONObject(langCode);
            // Extract data
            String title = langObj.getString("title");
            String subTitle = langObj.getString("sub_title");
            String imageUrl = langObj.getString("image_url");
            String buttonText = langObj.getString("buttonText");

            // Find views
            TextView tvTalkToUsTitle = findViewById(R.id.tv_talk_to_us_title);
            TextView tvTalkToUsSubTitle = findViewById(R.id.tv_talk_to_us_sub_title);
            ImageView ivTalkToUs = findViewById(R.id.iv_talk_to_us);
            talkToUs.setText(buttonText);
            Log.d(TAG, "Parsed values - title: " + title + ", subTitle: " + subTitle +
                    ", imageUrl: " + imageUrl + ", buttonText: " + buttonText);


            // Set values
            tvTalkToUsTitle.setText(title);
            tvTalkToUsSubTitle.setText(subTitle);

            Glide.with(this)
                    .load(imageUrl)
                    .error(R.drawable.laptop_guy) // fallback image
                    .into(ivTalkToUs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void checkCouponCodeValidity() {
        dataLoaded = false;
        Map<String, Object> params = new HashMap<>();
        params.put("planId", selectedPlanId);
        params.put("couponCode", couponId);
        params.put("showDayPlans", true);

        Log.i(TAG, "checkCouponCodeValidity: " + params);
        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(BUY_SUBSCRIPTION_V2, params), null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    Gson gson = new Gson();
                    Log.d("response", "checkCouponCodeValidity: " + response);
                    dataLoaded = true;
                    TypeToken<BuySubscriptionModel> token = new TypeToken<>() {
                    };
                    buySubscriptionModel = gson.fromJson(response.toString(), token.getType());
                    if (StringUtils.isEmpty(couponId) ||
                            (buySubscriptionModel.getPricingBreakdown() != null && buySubscriptionModel.getPricingBreakdown().isCouponCodeValid())) {
                        updateFinalPriceAfterCouponCodeApplied(buySubscriptionModel);
                    } else {
                        couponId = "";
                        Toast.makeText(SubscriptionActivityNew.this, getString(R.string.coupon_code_is_invalid), Toast.LENGTH_LONG).show();
                    }
                }, new VolleyGlobalErrorListener(this) {

            @Override
            public void postError(VolleyError error) {
            }
        });
        jsonObjectRequest.setTag("PLAN_DATA_REQUEST");
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    @SuppressLint("SetTextI18n")
    private void updateFinalPriceAfterCouponCodeApplied(BuySubscriptionModel buySubscriptionModel) {
        cv_coupon_price_breakdown.setVisibility(View.VISIBLE);
        tv_subscription_price.setText("₹" + buySubscriptionModel.getSubscriptionPlan().getFinalPrice());
        total_price.setText("₹" + buySubscriptionModel.getOverview().getFinalPrice());
        lv_coupon_discount_layout.setVisibility(View.VISIBLE);
        discount_price.setText("-₹" + buySubscriptionModel.getPricingBreakdown().getCouponCodeDeduction());
        applied_text.setVisibility(View.VISIBLE);
        discount_percent.setText(
                getString(R.string.coupon_code_text,
                        buySubscriptionModel.getPricingBreakdown().getCouponCodeValue() + "%")
        );
        overallFinalPrice.setText(String.format(getString(R.string.price_format),
                buySubscriptionModel.getOverview().getFinalPrice()));
        if (selectedSubscription != null) {
            selectedSubscription.setFinalPrice(buySubscriptionModel.getPricingBreakdown().getTotal());
        }
        discount_layout.setVisibility(View.VISIBLE);
        discount_text.setText(HtmlCompat.fromHtml(
                String.format(
                        getString(R.string.Discount_ARIVIHAN_Coupon_code_applied),
                        buySubscriptionModel.getPricingBreakdown().getCouponCodeValue() + "%",
                        couponPlaceholderText == null ? buySubscriptionModel.getPricingBreakdown().getCouponCode() : couponPlaceholderText
                ),
                HtmlCompat.FROM_HTML_MODE_LEGACY
        ));
    }

    private void logFirebaseEvent() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "subscription_how_to_card_click");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_how_to_card_click.getActionName(), params);
    }

    private void createNewRequest() {
//        comboSelected = false;
        subscriptionModel = new SubscriptionModel();
        subscriptionModel.setSelectedPlanId("");
        subscriptionModel.setPlanLevel(planLevel);
        subscriptionModel.setComboSelected(comboSelected);
        subscriptionModel.setBoardSelected(boardSelected);
    }

    private void openWhatsapp() {
        try {
            String url = "https://api.whatsapp.com/send?phone=" + MyApplication.getPhoneNumberWith91();
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.whatsapp");
            intent.setData(uri);
            destroyHowTo();
            startActivity(intent);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.whatsapp_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void setFeatureAnimation() {
        if (planLevelEnum != null) {
            if (planLevelEnum.equals(PlanLevel.COURSE)) {
                featureAnimation.setAnimation(R.raw.course);
                featureAnimation.setScaleX(1);
                featureAnimation.setScaleY(1);
            } else if (planLevelEnum.equals(PlanLevel.TEST_SERIES)) {
                featureAnimation.setAnimation(R.raw.test_series);
            } else if (planLevelEnum.equals(PlanLevel.DOUBT_SOLVING)) {
                featureAnimation.setScaleX(1.5f);
                featureAnimation.setScaleY(1.5f);
                featureAnimation.setAnimation(R.raw.doubt_chat);
            }
            featureAnimation.loop(true);
            featureAnimation.playAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        countDownTimer.start();
        if (howToVideoView != null && howToVideoView.videoView != null) {
            howToVideoView.resumePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        countDownTimer.cancel();
        if (howToVideoView != null && howToVideoView.videoView != null)
            howToVideoView.pausePlayer();
    }

    private void setTimer() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(calendar.getTime());
        limitedOfferText.setText(String.format(getString(R.string.limited_offer),
                calendar.get(Calendar.DAY_OF_MONTH),
                month_name));

//        LocalTime now;
//        int secondsPassed = 0;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            now = LocalTime.now(ZoneId.systemDefault());
//            secondsPassed = now.toSecondOfDay();
//        }

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        int secondsPassed = (hours * 3600) + (minutes * 60) + seconds;

        long totalSeconds = 3 * 60 * 60;
        long remainingSeconds;
        if (totalSeconds > secondsPassed)
            remainingSeconds = totalSeconds - secondsPassed;
        else
            remainingSeconds = totalSeconds - (secondsPassed % totalSeconds);

        if (remainingSeconds / (60) <= 5) {
            remainingSeconds = totalSeconds;
        }

        countDownTimer = new CountDownTimer(remainingSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countDownText.setText(CommonMethods.getTimeInHrMinSec(millisUntilFinished));
            }

            @Override
            public void onFinish() {
            }
        };
    }

    private void setSubscriptionTypes(SubscriptionModel subscriptionModel) {
        subscriptionTypes = new ArrayList<>();
        planTypeRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        if (subscriptionModel != null && subscriptionModel.getPlanLevels() != null) {
            for (PlanLevel planLevelStr : subscriptionModel.getPlanLevels()) {
                PlanLevel planLevel = PlanLevel.fromString(planLevelStr.toString());
                if (planLevel != null) {
                    String title = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()).equals("hi")
                            ? planLevel.getHindiTitle() : planLevel.getTitle();
                    subscriptionTypes.add(new SubscriptionType(title, false, planLevel.name()));
                }
            }
        }

        for (SubscriptionType type : subscriptionTypes) {
            PlanLevel level = PlanLevel.fromString(type.getType());
            if (level != null && level.equals(planLevelEnum)) {
                type.set_selected(true);
                break;
            }
        }

        Log.d(TAG, "setSubscriptionTypes: " + subscriptionTypes);
        subscriptionTypeAdapter = new SubscriptionTypeAdapter(subscriptionTypes, this, this);
        planTypeRv.setAdapter(subscriptionTypeAdapter);
    }


    private void setSubscriptionPlans() {
        subscriptionPlans = new ArrayList<>();
        subscriptionPlanAdapter = new SubscriptionPlanAdapter(subscriptionPlans, this, this);
        plansRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        plansRv.setAdapter(subscriptionPlanAdapter);
    }

    private void setSubscriptionFeatures() {
        subscriptionFeatures = new ArrayList<>();
        subscriptionFeatureAdapter = new SubscriptionFeatureAdapter(subscriptionFeatures, this);
        featuresRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        featuresRv.setAdapter(subscriptionFeatureAdapter);
    }

    private void setSubscriptionOptions() {
        subscriptionOptions = new ArrayList<>();
        subscriptionOptionAdapter = new SubscriptionOptionAdapter(subscriptionOptions, this, this, comboSelected);
        planOptionsRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        planOptionsRv.setAdapter(subscriptionOptionAdapter);
    }


    private void startPayment() {
        enrollButton.setOnClickListener(v -> {
            if (tncChecked) {
                try {
                    if (isSubscriptionAmountIsZero()) {
                        if(CommonMethods.isOnline(this)) {
                            buyNowPreCall();
                        } else {
                            CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.error_no_internet_connection));
                        }
                    } else {

                        Dialog dialog = new Dialog(this);
                        dialog.setContentView(R.layout.subscription_payment_option_dialog_layout);
                        if (dialog.getWindow() != null) {
                            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        }
                        dialog.setCancelable(false);

                        dialog.findViewById(R.id.close_btn).setOnClickListener(dv -> dialog.dismiss());

                        dialog.findViewById(R.id.pay_with_qr_btn).setOnClickListener(dv -> {
                            isPayingThroughQRCode = true;
                            dialog.dismiss();
                            try {
                                buyNowPreCall();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        dialog.findViewById(R.id.pay_with_payment_gateway_btn).setOnClickListener(dv -> {
                            isPayingThroughQRCode = false;
                            dialog.dismiss();
                            try {
                                buyNowPreCall();
                            } catch (Exception e) {
                                Toast.makeText(this, "An error occurred from our end, please try again later", Toast.LENGTH_LONG).show();
                            }
                        });
                        dialog.show();

                    }
                } catch (Exception ignored){}
            } else {
                Toast.makeText(this, getString(R.string.terms_condition), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isSubscriptionAmountIsZero() {
        try{
            int amount = Integer.parseInt(selectedSubscription.getFinalPrice());
            return amount==0;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    private void getPlanData() {
        shimmerLayout.setVisibility(View.VISIBLE);
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(SUBSCRIPTION_PLAN_TAG);
        dataLoaded = false;
        if (screenLoader.isShowing()) {
            screenLoader.hideLoader();
        }
        screenLoader.showLoader();
        Map<String, Object> params = new HashMap<>();
        params.put("planLevel", subscriptionModel.getPlanLevel() == null ?
                "COURSE" : subscriptionModel.getPlanLevel());
        params.put("boardSelected", subscriptionModel.isBoardSelected());
        params.put("comboSelected", subscriptionModel.isComboSelected());
        params.put("selectedPlanId", subscriptionModel.getSelectedPlanId());
        params.put("showDayPlans", true);
        params.put("hideMonthlyPlans", true);
        Log.d("SUBSCRIPTON_PLAN", "getPlanData: " + params);

        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_SUBSCRIPTION_V2, params), null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    discount_layout.setVisibility(View.GONE);
                    dataLoaded = true;
                    TypeToken<SubscriptionModel> token = new TypeToken<SubscriptionModel>() {
                    };
                    subscriptionModel = gson.fromJson(response.toString(), token.getType());
                    Log.i("SUBSCRIPTON_PLAN", response.toString());
                    setSubscriptionTypes(subscriptionModel);
                    if (subscriptionModel.isToShowSubscriptionPlanList()) {
                        updateSubscriptionOptions(subscriptionModel.getSubscriptionOptions());
                    } else {
                        cv_subscription_options.setVisibility(View.GONE);
                        plansRv.setVisibility(View.GONE);
                        ll_offer_timer_layout.setVisibility(View.GONE);
                    }
                    if (fromUnlockText && PlanLevel.valueOf(planLevel) == PlanLevel.TEST_SERIES)
                        updatePlanFromTestSeries(subscriptionModel.getPlans());
                    else if (fromMySubscription)
                        updatePlanFromMySubscriptions(subscriptionModel.getPlans());
                    else {
                        if (subscriptionModel.getSubscriptionRenewalReward() != null) {
                            subscriptionRenewalReward = subscriptionModel.getSubscriptionRenewalReward();
                            Log.d(TAG, "getPlanData: ");
                            getCouponAppliedPlanData(subscriptionModel.getOverview().getPlanId(),
                                    subscriptionRenewalReward);
                        } else {
                            subscriptionRenewalReward = null;
                            changePlanLevelData();
                        }
                    }

                    for (SubscriptionPlans plan : subscriptionModel.getPlans()) {
                        if (plan.isSelected()) {
                            selectedSubscription = plan;
                            selectedPlanId = selectedSubscription.getPlanId();
                        }
                    }
//                    MyApplication.removeString(PreferenceKeys.REFERRAL_CODE.getKey());
//                    MyApplication.removeString(PreferenceKeys.REFERRAL_DETECTED.getKey());

                    //for referral and deeplink
                    if (MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_DETECTED.getKey())) {
                        couponId = MyApplication.getStringDataFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_CODE.getKey());
                        couponPlaceholderText = MyApplication.getFirebaseRemoteConfig().getString("subscription_activity_coupon_code_placeholder_text");
                        checkForAutoAppliedCoupon(couponId, couponPlaceholderText);
                    }

                    if (MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_REDIRECTED_PENDING.getKey())) {
                        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_REDIRECTED_PENDING.getKey());
                    }

                    if(StringUtils.isNotEmpty(couponCode)){
                        String encodedCouponCodesForFreeSubscription = MyApplication.getFirebaseRemoteConfig().getString("encoded_coupon_codes_for_free_subscription");

                        if(StringUtils.isNotEmpty(encodedCouponCodesForFreeSubscription) && !Arrays.stream(encodedCouponCodesForFreeSubscription.split(",")).filter(item -> item.equals(couponCode)).toList().isEmpty()){
                            Base64.Decoder decoder = Base64.getDecoder();
                            byte[] decodedBytes = decoder.decode(couponCode);
                            String referralCode = new String(decodedBytes);
                            couponId = referralCode;
                            couponPlaceholderText = MyApplication.getFirebaseRemoteConfig().getString("subscription_activity_coupon_code_placeholder_text");
                        } else {
                            couponPlaceholderText = couponCode;
                            couponId = couponCode;
                        }
                        checkForAutoAppliedCoupon(couponId, couponPlaceholderText);
                    }

                    screenLoader.hideLoader();
                }, new VolleyGlobalErrorListener(this) {

            @Override
            public void postError(VolleyError error) {
                progressDialog.dismiss();
                screenLoader.hideLoader();
//                if (subscriptionModel != null) {
//                    if (subscriptionModel.getSubscriptionRenewalReward() != null) {
//                        subscriptionRenewalReward = subscriptionModel.getSubscriptionRenewalReward();
//                        getCouponAppliedPlanData(subscriptionModel.getOverview().getPlanId(), subscriptionRenewalReward);
//                    } else {
//                        subscriptionRenewalReward = null;
//                        changePlanLevelData();
//                    }
//                } else {
//                    Toast.makeText(SubscriptionActivityNew.this, error.getMessage(), Toast.LENGTH_LONG).show();
//                }
            }
        });
        jsonObjectRequest.setTag(SUBSCRIPTION_PLAN_TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void checkForAutoAppliedCoupon(String couponId, String couponName) {
        try {
            long timeInMillis = MyApplication.getFirebaseRemoteConfig().getLong("subscription_auto_applied_timer");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                coupon_code.setText(couponName);
                checkCouponCodeValidity();
            }, timeInMillis);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void getCouponAppliedPlanData(String planId, SubscriptionRenewalReward subscriptionRenewalReward) {

        dataLoaded = false;
        Map<String, Object> params = new HashMap<>();
        params.put("planId", planId);
        params.put("couponCode", subscriptionRenewalReward.getCouponCode());
        params.put("showDayPlans", true);
        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(BUY_SUBSCRIPTION_V2, params), null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.d(TAG, "getCouponAppliedPlanData: " + response);
                    dataLoaded = true;
                    TypeToken<BuySubscriptionModel> token = new TypeToken<>() {
                    };
                    BuySubscriptionModel buySubscriptionModel = gson.fromJson(response.toString(), token.getType());
                    if (buySubscriptionModel.getPricingBreakdown() != null) {
                        subscriptionModel.getOverview().setCostPrice(buySubscriptionModel.getPricingBreakdown().getSubscriptionPrice());
                        subscriptionModel.getOverview().setFinalPrice(buySubscriptionModel.getPricingBreakdown().getTotal());
                    }
                    if (subscriptionRenewalReward.getCouponCodeText() != null
                            && !subscriptionRenewalReward.getCouponCodeText().isEmpty()) {
                        coupon_code.setText(subscriptionRenewalReward.getCouponCode());
                        couponId = subscriptionRenewalReward.getCouponCode();
                        updateFinalPriceAfterCouponCodeApplied(buySubscriptionModel);
                    } else {
                        discount_layout.setVisibility(View.GONE);
                    }
                    changePlanLevelData();

                }, new VolleyGlobalErrorListener(this) {

            @Override
            public void postError(VolleyError error) {
                //Toast.makeText(BuySubscriptionActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        jsonObjectRequest.setTag("PLAN_DATA_REQUEST");
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void changePlanLevelData() {
        Log.d(TAG, "Calling change plan level data");
        scrollView.setVisibility(View.VISIBLE);
        shimmerLayout.setVisibility(View.GONE);
        shimmerLayout.stopShimmer();
        progressDialog.dismiss();
//        updateSubscriptionOptions(subscriptionModel.getSubscriptionOptions());
        updateSubscriptionFeatures(subscriptionModel.getFeatures());
        if (!CollectionUtils.isEmpty(subscriptionModel.getPlans())) {
            updateSubscriptionPlans(subscriptionModel.getPlans());
        }
        if (subscriptionModel.getOverview() != null) {
            updateOverview(subscriptionModel.getOverview());
            updatePrice(subscriptionModel.getOverview());
        }
        bottom_layout.setVisibility(View.VISIBLE);
    }

    private void updateOverview(SubscriptionOverview overview) {
        selectedMonths.setText(overview.getDurationLabel());
        costPrice.setText(String.format(getString(R.string.price_format), overview.getCostPrice()));
        costPrice.setPaintFlags(costPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        finalPrice.setText(String.format(getString(R.string.price_format), overview.getFinalPrice()));
    }

    private void updatePrice(SubscriptionOverview subscriptionOverview) {
        Log.d(TAG, "Calling update price");
        selectedPlanName.setText(subscriptionOverview.getName());
        overallCostPrice.setText(String.format(getString(R.string.price_format), subscriptionOverview.getCostPrice()));
        overallCostPrice.setPaintFlags(overallCostPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        overallFinalPrice.setText(String.format(getString(R.string.price_format), subscriptionOverview.getFinalPrice()));
    }

    private void updateSubscriptionPlans(ArrayList<SubscriptionPlans> subscriptionPlan) {
        subscriptionPlans.clear();
        subscriptionPlans.addAll(subscriptionPlan);
        subscriptionPlanAdapter.notifyDataSetChanged();
    }

    private void updatePlanFromTestSeries(ArrayList<SubscriptionPlans> subscriptionPlan) {
        Log.d(TAG, "updatePlanFromTestSeries: ");
        for (int i = 0; i < subscriptionPlan.size(); i++) {
            if (subscriptionPlan.get(i).getDuration().equals("6")) {
                selectedPlanId = subscriptionPlan.get(i).getPlanId();
                subscriptionModel.setBoardSelected(boardSelected);
                subscriptionModel.setComboSelected(comboSelected);
                subscriptionModel.setPlanLevel(planLevel);
                subscriptionModel.setSelectedPlanId(selectedPlanId);
                getPlanData();
                fromUnlockText = false;
                break;
            }
        }
    }

    private void updatePlanFromMySubscriptions(ArrayList<SubscriptionPlans> subscriptionPlan) {
        if (duration == 0)
            duration = 3;
        for (int i = 0; i < subscriptionPlan.size(); i++) {
            if (subscriptionPlan.get(i).getDuration().equals(String.valueOf(duration))) {
                selectedPlanId = subscriptionPlan.get(i).getPlanId();
                subscriptionModel.setBoardSelected(boardSelected);
                subscriptionModel.setComboSelected(comboSelected);
                subscriptionModel.setPlanLevel(planLevel);
                subscriptionModel.setSelectedPlanId(selectedPlanId);
                getPlanData();
                fromMySubscription = false;
                break;
            }
        }
        if (subscriptionModel != null && fromMySubscription) {
            fromMySubscription = false;
            if (subscriptionModel.getSubscriptionRenewalReward() != null) {
                subscriptionRenewalReward = subscriptionModel.getSubscriptionRenewalReward();
                getCouponAppliedPlanData(subscriptionModel.getOverview().getPlanId(), subscriptionRenewalReward);
            } else {
                subscriptionRenewalReward = null;
                changePlanLevelData();
            }
        }
    }

    private void updateSubscriptionFeatures(ArrayList<String> features) {
        subscriptionFeatures.clear();
        if (subscriptionFeatures != null && features != null) {
            subscriptionFeatures.addAll(features);
        }
        subscriptionFeatureAdapter.notifyDataSetChanged();
    }

    private void updateSubscriptionOptions(ArrayList<SubscriptionOptions> subscriptionOption) {
        subscriptionOptions.clear();
        subscriptionOptions.addAll(subscriptionOption);
        subscriptionOptionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTypeClick(int position) {
        // Resetting comboSelected to false to ensure that each time a new plan type  default selection is selected,
        // any previously selected combo option is cleared. This prevents carrying over state from
        // previous selections and ensures that missing or unavailable options in the new plan type
        // are handled appropriately.
        savePreviousPlanData();
        for (SubscriptionType type : subscriptionTypes) {
            type.set_selected(false);
        }
        resetCouponAndPricing();
        subscriptionTypes.get(position).set_selected(true);
        subscriptionTypeAdapter.notifyDataSetChanged();
        planLevel = subscriptionTypes.get(position).getType();
        planLevelEnum = PlanLevel.valueOf(planLevel);
        setFeatureAnimation();
        comboSelected = false;
        boardSelected = true;
        fetchPlanLevelData();

        Map<String, String> params = new HashMap<>();
        params.put("Subscription_type", subscriptionTypes.get(position).getType());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.Subscription_plans.getActionName(), params);


//        if (modelMap.containsKey(planLevel)) {
//            subscriptionModel = modelMap.get(planLevel);
//            subscriptionModel.setBoardSelected(true);
//            subscriptionModel.setComboSelected(true);
//            subscriptionModel.setComboSelected(true);
//            subscriptionOptionAdapter.setComboSelected(subscriptionModel.isComboSelected());
//            comboSelected=subscriptionModel.isComboSelected();
//            boardSelected=subscriptionModel.isBoardSelected();
//            changePlanLevelData();
//        } else {
//            fetchPlanLevelData();
//        }
    }

    private void savePreviousPlanData() {
        if (subscriptionModel != null) {
//            subscriptionModel.setSelectedPlanId(selectedPlanId);
//            subscriptionModel.setPlanLevel(planLevel);
//            subscriptionModel.setComboSelected(comboSelected);
//            subscriptionModel.setBoardSelected(boardSelected);
//            modelMap.put(planLevel, subscriptionModel);
        }
    }

    private void fetchPlanLevelData() {
        scrollView.setVisibility(View.GONE);
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll("PLAN_DATA_REQUEST");
        createNewRequest();
        getPlanData();
    }

    @Override
    public void onOptionClick(int position) {
        if (subscriptionOptions.get(position).isSelected() && !comboSelected) {
            return;
        }
        fromUnlockText = false;
        if (subscriptionOptions.get(position).getName().contains("Board")
                && comboSelected) {
            boardSelected = false;
        } else {
            boardSelected = true;
        }
        comboSelected = !comboSelected;
        subscriptionModel.setBoardSelected(boardSelected);
        subscriptionModel.setComboSelected(comboSelected);
        subscriptionModel.setSelectedPlanId(selectedPlanId);
        subscriptionModel.setPlanLevel(planLevel);
        getPlanData();
    }

    @Override
    public void onPlanClick(int position) {
        fromUnlockText = false;
        selectedPlanId = subscriptionPlans.get(position).getPlanId();
        subscriptionModel.setBoardSelected(boardSelected);
        subscriptionModel.setComboSelected(comboSelected);
        subscriptionModel.setPlanLevel(planLevel);
        subscriptionModel.setSelectedPlanId(selectedPlanId);
        getPlanData();
    }

    //payment Methods
    private void buyNowPreCall() throws JSONException {
        if (selectedSubscription != null) {
            screenLoader.showLoader();
            Map<String, Object> map = new HashMap<>();
            SubscriptionPreModel subscriptionPreModel = new SubscriptionPreModel();
            subscriptionPreModel.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
            if (StringUtils.isNoneEmpty(couponId)) {
                CouponDto couponDto = new CouponDto();
                couponDto.setCode(couponId);
                subscriptionPreModel.setCoupon(couponDto);
            }
            subscriptionPreModel.setSubscriptionPlanId(selectedSubscription.getPlanId());
            subscriptionPreModel.setOrderId(getAlphaNumericString(8));
            subscriptionPreModel.setTotalAmount(Long.parseLong(selectedSubscription.getFinalPrice()));
            subscriptionPreModel.setRedirectionFrom(analyticsSource != null ? analyticsSource : "UNKNOWN");
            Log.d("SubscriptionSourceTrack", "Source: " + subscriptionPreModel.getRedirectionFrom() + " | Plan: " + selectedSubscription.getPlanId() + " (SubscriptionActivityNew)");
            gson = new Gson();
            if (BuildConfig.DEBUG) Log.d(TAG, "buyNowPreCall Body: " + gson.toJson(subscriptionPreModel));
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), new JSONObject(gson.toJson(subscriptionPreModel)),
                    response -> {
                        if (isFinishing() || isDestroyed()) return;
                        TypeToken<UserSubscriptionDto> token = new TypeToken<UserSubscriptionDto>() {
                        };
                        screenLoader.hideLoader();
                        try {
                            userSubscriptionDto = gson.fromJson(response.getJSONObject("data").toString(), token.getType());

                            if (isSubscriptionAmountIsZero()) {
                                try {
                                    buyNowPostCall(getAlphaNumericString(8), userSubscriptionDto);
                                } catch (JSONException e) {
                                    Log.e("Error in ", e.getMessage(), e);
                                }
                            } else {
                                if (isPayingThroughQRCode) {
                                    if (selectedSubscription != null) {
                                        Intent intent = new Intent(SubscriptionActivityNew.this, QRCodePaymentActivity.class);
                                        intent.putExtra("orderId", userSubscriptionDto.getOrderId());
                                        intent.putExtra("subscriptionPlanId", selectedSubscription.getPlanId());
                                        intent.putExtra("transactionId", "");
                                        intent.putExtra("costPrice", selectedSubscription.getCostPrice());
                                        intent.putExtra("planName", selectedSubscription.getName());
                                        intent.putExtra("userSubscriptionDto", userSubscriptionDto);
                                        intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, redirectionSource);

                                        intent.putExtra("paymentAmount", Integer.parseInt(selectedSubscription.getFinalPrice()));
//
                                        qrPaymentLauncher.launch(intent);
                                    }
                                } else {
                                    doPayment(Integer.parseInt(selectedSubscription.getFinalPrice()), userSubscriptionDto.getOrderId(), userSubscriptionDto);
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }

                    }
                    , new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Log.i("USERSUBSCRIPTIONRES", error.getMessage());
                    Toast.makeText(SubscriptionActivityNew.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    screenLoader.hideLoader();
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
        }
    }

    private void buyNowPostCall(String transactionId, UserSubscriptionDto userSubscriptionDto) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        SubscriptionPostModel subscriptionPostModel = new SubscriptionPostModel();
        subscriptionPostModel.setPaymentStatus(PaymentStatus.SUCCESS.name());
        subscriptionPostModel.setInvoiceId(userSubscriptionDto.getInvoiceId());
        subscriptionPostModel.setTransactionId(transactionId);
        subscriptionPostModel.setSubscriptionId(userSubscriptionDto.getId());
        subscriptionPostModel.setInternalMetrics(true);
        subscriptionPostModel.setSecretKey(MyApplication.getFirebaseRemoteConfig().getString("internal_metrics_secret_key"));

        screenLoader.showLoader();
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.PUT,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), new JSONObject(gson.toJson(subscriptionPostModel)),
                response -> {
                    try {
                        screenLoader.hideLoader();
                        boolean success = (boolean) response.get("success");
                        if (success) {
                            new Handler().postDelayed(() -> {
                                if (MyApplication.getFirebaseRemoteConfig().getBoolean(getString(R.string.show_subscription_benefits))) {
                                    try {
                                        if(screenLoader.isShowing()){
                                            screenLoader.hideLoader();
                                        }
                                        Type listType = new TypeToken<List<SubscriptionBenefitsModel>>() {
                                        }.getType();
                                        ArrayList<SubscriptionBenefitsModel> list = gson.fromJson(String.valueOf(response.get("data")), listType);
                                        Intent intent = new Intent(SubscriptionActivityNew.this, SubscriptionCongatulationsActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putParcelableArrayListExtra("benefits", list);
                                        intent.putExtra("congratulationHeader", (response.has("congratulationHeader") && !response.isNull("congratulationHeader"))?response.getString("congratulationHeader"):"");
                                        intent.putExtra("congratulationSubHeader", (response.has("congratulationSubHeader") && !response.isNull("congratulationSubHeader"))?response.getString("congratulationSubHeader"):"");
                                        intent.putExtra("planId", userSubscriptionDto.getSubscriptionPlanId());
                                        startActivity(intent);
                                        finish();
                                    } catch (JSONException e) {
                                        //throw new RuntimeException(e);
                                        Log.d("Exception4", e.getMessage());
                                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Intent intent = new Intent(SubscriptionActivityNew.this, HomeScreen.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 500);
                        } else {
                            CommonMethods.showNegativeSnackbar(ParentLayout,
                                    "Sorry we faced some technical problem, contact us.");
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage().toString());
                screenLoader.hideLoader();
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    public static String getAlphaNumericString(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }

    void doPayment(int amount, String rzOrderId, UserSubscriptionDto userSubscriptionDto) {
        if (selectedSubscription != null) {
            Intent paymentGatewayIntent = new Intent(this, RazorpayPaymentGatewayActivity.class);
            paymentGatewayIntent.putExtra("planId", selectedSubscription.getPlanId());
            paymentGatewayIntent.putExtra("orderId", rzOrderId);
            paymentGatewayIntent.putExtra(PreferenceKeys.COUPON_ID.getKey(), couponId);
            paymentGatewayIntent.putExtra("amount", amount + "");
            paymentGatewayIntent.putExtra("userSubscriptionDto", userSubscriptionDto);
            paymentGatewayIntent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, redirectionSource);
            startActivity(paymentGatewayIntent);
        }
    }

    private void setTermsAndCondition() {
        tnc_checkbox = findViewById(R.id.tnc_checkbox);
        tnc_checkbox_layout = findViewById(R.id.tnc_checkbox_layout);

        tnc_checkbox_layout.setOnClickListener(view -> {
            tncChecked = !tncChecked;
            if (tncChecked) {
                tnc_checkbox.setBackground(getResources().getDrawable(R.drawable.checkmark));
                tnc_checkbox.setStrokeWidth(0);
            } else {
                tnc_checkbox.setBackground(getResources().getDrawable(R.color.white));
                tnc_checkbox.setStrokeWidth(4);
                tnc_checkbox.setStrokeColor(getResources().getColor(R.color.black));
            }
        });

        tnc_checkbox.setClickable(false);
        tnc_checkbox.setFocusable(false);
        tnc_checkbox.setFocusableInTouchMode(false);

        // T&C link
        tnc = findViewById(R.id.tnc);
        tnc.setOnClickListener(v -> {
            String tncUrl = MyApplication.getFirebaseRemoteConfig().getString("arivihan_terms_and_condition");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tncUrl));
            startActivity(intent);
        });

        privacy_policy = findViewById(R.id.privacy_policy);
        privacy_policy.setOnClickListener(v -> {
            String privacyPolicyUrl = MyApplication.getFirebaseRemoteConfig().getString("arivihan_privacy_policy");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
            startActivity(intent);
        });
    }

    private void resetCouponAndPricing() {
        couponId = "";
        coupon_code.setText(""); // clear entered coupon code
        cv_coupon_price_breakdown.setVisibility(View.GONE);
        lv_coupon_discount_layout.setVisibility(View.GONE);
        applied_text.setVisibility(View.GONE);
        discount_percent.setText("");
        discount_price.setText("");
        tv_subscription_price.setText("");
        total_price.setText("");
        overallFinalPrice.setText("");
        selectedSubscription = null; // clear selected plan reference
    }

    private void startButtonAnimation(ConstraintLayout buttonLayout, TextView txt, ImageView icon,
                                      String[] texts, Integer[] icons, Integer[] backgrounds, long interval) {

        Handler handler = new Handler(Looper.getMainLooper());
        final int[] index = {1};

        Runnable r = new Runnable() {
            @Override
            public void run() {
                txt.animate()
                        .translationY(-txt.getHeight())
                        .setDuration(100)
                        .start();

                if (icon != null) {
                    icon.animate()
                            .translationY(-icon.getHeight())
                            .setDuration(100)
                            .start();
                }

                handler.postDelayed(() -> {
                    // Change text and icon
                    txt.setText(texts[index[0]]);
                    txt.setTranslationY(txt.getHeight()); // reset position

                    if (icon != null) {
                        icon.setImageResource(icons[index[0]]);
                        icon.setTranslationY(icon.getHeight()); // reset position
                    }

                    Drawable newBg = ContextCompat.getDrawable(buttonLayout.getContext(), backgrounds[index[0]]);
                    Drawable oldBg = buttonLayout.getBackground();

                    if (oldBg == null) {
                        buttonLayout.setBackground(newBg);
                    } else if (newBg != null) {
                        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{oldBg, newBg});
                        buttonLayout.setBackground(transitionDrawable);
                        transitionDrawable.startTransition(100); // smooth transition
                    }

                    txt.animate()
                            .translationY(0)
                            .setDuration(200)
                            .start();

                    if (icon != null) {
                        icon.animate()
                                .translationY(0)
                                .setDuration(200)
                                .start();
                    }

                    // Move to next index
                    index[0] = (index[0] + 1) % texts.length;

                    // Repeat animation
                    handler.postDelayed(this, interval);

                }, 100); // wait for slide-out animation
            }
        };
        handler.postDelayed(r, interval);
    }


    // onBackPressed logic moved to OnBackPressedCallback in onCreate

    private void destroyHowTo() {
        how_to_video_view.setVisibility(View.GONE);
        if (howToVideoView != null)
            howToVideoView.remove();
    }

    @Override
    protected void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(SUBSCRIPTION_PLAN_TAG);
        super.onDestroy();
    }


}