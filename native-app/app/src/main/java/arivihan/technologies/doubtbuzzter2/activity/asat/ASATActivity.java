package arivihan.technologies.doubtbuzzter2.activity.asat;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_ASAT_PROCESS_INFO;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_ASAT_STEP;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.RazorpayPaymentGatewayActivity;
import arivihan.technologies.doubtbuzzter2.activity.ActivityTestIntro;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.QRCodePaymentActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionCongatulationsActivity;
import arivihan.technologies.doubtbuzzter2.adapter.AsatAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.ResultInfoStatus;
import arivihan.technologies.doubtbuzzter2.enums.asat.ASATStep;
import arivihan.technologies.doubtbuzzter2.fragments.ASATBottomFragment;
import arivihan.technologies.doubtbuzzter2.fragments.ASATTopFragment;
import arivihan.technologies.doubtbuzzter2.fragments.ASATVideoFragment;
import arivihan.technologies.doubtbuzzter2.helper.offline.OfflineTestManager;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.CouponDto;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.ResultInfo;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionBenefitsModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlans;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPostModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPreModel;
import arivihan.technologies.doubtbuzzter2.model.UserAsatInfo;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.model.asat.ASATScreenProcessInfo;
import arivihan.technologies.doubtbuzzter2.model.asat.ASATStepsInfo;
import arivihan.technologies.doubtbuzzter2.model.asat.CallBackResponse;
import arivihan.technologies.doubtbuzzter2.model.asat.UploadMarksheetResponse;
import arivihan.technologies.doubtbuzzter2.model.offline.OfflineTestSeriesModel;
import arivihan.technologies.doubtbuzzter2.model.offline.OfflineTestTypes;
import arivihan.technologies.doubtbuzzter2.model.remote_config.AsatConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ASATActivity extends BaseActivity implements AsatAdapter.AsatListener {

    private static final String TAG = ASATActivity.class.getSimpleName();
    private Bundle savedInstanceStateBundle;
    public static final String ASAT_OPEN_RESULT = "openResult";
    public static final String ASAT_BACK_TO_MAIN_SCREEN_RESTRICTED = "mainRestricted";
    public static final String ASAT_SCREEN_PROCESS_INFO = "asat_screen_process_info";
    public static final String ASAT_SCREEN_TYPE_RESULT = "asat_screen_type_result";
    public static final String ASAT_UPLOAD_MARKSHEET = "asat_upload_marksheet";
    public static final String ASAT_TOPPERS_LIST = "asat_toppers_list";
    public static final String ASAT_SYLLABUS = "asat_syllabus";
    public static final String ASAT_RULES = "asat_rules";
    Gson gson;
    SubscriptionPlans selectedSubscription;
    boolean isPayingThroughQRCode;
    // UI Variables
    private ShimmerFrameLayout shimmer_layout;
    private RelativeLayout rl_main_layout, rl_parent_layout;
    private LinearLayout ll_bottom_layout, ll_discount_layout, ll_expiry_layout;
    private MaterialCardView cv_main_screen_bottom_control, cv_result_screen_bottom_control;
    private TextView tv_upload_result, tv_cost_price, tv_discount_price, tv_subscription_title, tv_expiry_timer, tv_discount_text;
    private CardView cv_take_test, cv_upload_result, cv_join_now;
    private LottieAnimationView confetti1, confetti2, confetti3;
    // Data
    private CountDownTimer countDownTimer;
    private MediaPlayer mediaPlayer;
    private Handler confettiHandler;
    private long expiryTimeMillis;
    private ASATScreenProcessInfo asatScreenProcessInfo;
    private int dataUpdateCounter = 0;
    private boolean isResultScreen, isBackToMainScreenRestricted;
    private boolean isConfettiDone;
    private Dialog progressDialog;
    private AsatConfig asatConfig;
    private RedirectionSource redirectionSource;

    ActivityResultLauncher<Intent> uploadResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            UploadMarksheetResponse uploadMarksheetResponse = (UploadMarksheetResponse) data.getSerializableExtra(ASAT_UPLOAD_MARKSHEET);
                            if (uploadMarksheetResponse != null) {
                                String message = uploadMarksheetResponse.getMessage() != null ? uploadMarksheetResponse.getMessage() : getString(R.string.something_went_wrong);
                                if (uploadMarksheetResponse.isSuccess()) {
                                    CommonMethods.showPositiveSnackbar(rl_main_layout, message, 3000);
                                } else {
                                    CommonMethods.showNegativeSnackbar(rl_main_layout, message, 3000);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asatactivity);
        savedInstanceStateBundle = savedInstanceState;

        Uri deepLinkData = getIntent().getData();
        if (deepLinkData != null) {
            isResultScreen = deepLinkData.getBooleanQueryParameter(ASAT_OPEN_RESULT, false);
            isBackToMainScreenRestricted = deepLinkData.getBooleanQueryParameter(ASAT_BACK_TO_MAIN_SCREEN_RESTRICTED, false);
        } else {
            isResultScreen = getIntent().getBooleanExtra(ASAT_OPEN_RESULT, false);
            isBackToMainScreenRestricted = getIntent().getBooleanExtra(ASAT_BACK_TO_MAIN_SCREEN_RESTRICTED, false);
        }

        String jsonString = MyApplication.getFirebaseRemoteConfig().getString("asat_coupon_controller");
        asatConfig = new Gson().fromJson(jsonString, AsatConfig.class);

        shimmer_layout = findViewById(R.id.shimmer_layout);
        rl_main_layout = findViewById(R.id.rl_main_layout);
        rl_parent_layout = findViewById(R.id.rl_parent_layout);
        ll_bottom_layout = findViewById(R.id.ll_bottom_layout);
        cv_result_screen_bottom_control = findViewById(R.id.cv_result_screen_bottom_control);
        cv_main_screen_bottom_control = findViewById(R.id.cv_main_screen_bottom_control);
        ll_discount_layout = findViewById(R.id.ll_discount_layout);
        ll_expiry_layout = findViewById(R.id.ll_expiry_layout);
        tv_upload_result = findViewById(R.id.tv_upload_result);
        cv_take_test = findViewById(R.id.cv_take_test);
        cv_upload_result = findViewById(R.id.cv_upload_result);

        confetti1 = findViewById(R.id.confetti1);
        confetti2 = findViewById(R.id.confetti2);
        confetti3 = findViewById(R.id.confetti3);

        tv_cost_price = findViewById(R.id.tv_cost_price);
        tv_discount_price = findViewById(R.id.tv_discount_price);
        tv_subscription_title = findViewById(R.id.tv_subscription_title);
        tv_expiry_timer = findViewById(R.id.tv_expiry_timer);
        tv_discount_text = findViewById(R.id.tv_discount_text);
        cv_join_now = findViewById(R.id.cv_join_now);

        rl_parent_layout.setVisibility(View.GONE);
        ll_bottom_layout.setVisibility(View.GONE);

        cv_result_screen_bottom_control.setVisibility(View.GONE);
        cv_main_screen_bottom_control.setVisibility(View.GONE);

        cv_take_test.setOnClickListener(cardView -> {
            stepsClicked(ASATStep.COMPLETE_TEST.index());
        });
        cv_upload_result.setOnClickListener(cardView -> {

            try {
                List<ASATStepsInfo> list = asatScreenProcessInfo.getAsatStepsInfo();
                if (list.get(ASATStep.TEST_RESULT.index()).isDone() || list.get(ASATStep.COMPLETE_TEST.index()).isDone()) {
                    stepsClicked(ASATStep.TEST_RESULT.index());
                } else if (list.get(ASATStep.UPLOAD_MARKSHEET.index()).isDone()) {
                    stepsClicked(ASATStep.COMPLETE_TEST.index());
                } else {
                    stepsClicked(ASATStep.UPLOAD_MARKSHEET.index());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

//            if(asatScreenProcessInfo!=null) {
//                stepsClicked(3);
//            }
        });

        cv_join_now.setOnClickListener(cardView -> {
            try {
                if (StringUtils.isNotEmpty(asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getFinalAmount())) {
//                    if(System.currentTimeMillis()<=asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getExpiryTime()) {
                    if (Integer.parseInt(selectedSubscription.getFinalPrice()) == 0) {
                        try {
                            showProgressDialog();
                            buyNowPreCall();
                        } catch (JSONException e) {
                            Log.e("Error in ", e.getMessage(), e);
                            dismissProgressDialog();
                        }
                    } else {
                        startPayment();
                    }
//                    } else {
//                        CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.coupon_expired), 3000);
//                    }

                    Map<String, String> params = new HashMap<>();
                    params.put("asat_id", asatScreenProcessInfo.getUserAsatInfo().getId());
                    params.put(PreferenceKeys.DISCOUNT.getKey(), asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getPercentDiscount() + "");
                    params.put("amount", isCouponExpired()? asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getOfferAmount() : asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getFinalAmount());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_join_now_click.getActionName(), params);
                } else {
                    CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.something_went_wrong), 3000);
                }
            } catch (Exception ignored) {
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (isResultScreen && !isBackToMainScreenRestricted) {
                    isResultScreen = false;
                    dataUpdateCounter = 1;
                    setAsatFragments(true);
//                    getAsatProcessInfo(true);
                } else {
                    finish();
                }

            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        Object sourceExtra = getIntent().getSerializableExtra(SubscriptionNavigationHelper.EXTRA_SOURCE);
        redirectionSource = null;
        if (sourceExtra instanceof RedirectionSource) {
            redirectionSource = (RedirectionSource) sourceExtra;
        }

        ll_expiry_layout.setVisibility(asatConfig.isExpiryTimerHidden() ? View.GONE : View.VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();

//        Log.d(TAG, "--> ResultScreen: "+isResultScreen);
        getAsatProcessInfo(false);
        startCountdown(expiryTimeMillis);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        isResultScreen = getIntent().getBooleanExtra(ASAT_OPEN_RESULT, false);
        if (isResultScreen) {
            dataUpdateCounter = 0;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (confettiHandler != null)
            confettiHandler.removeCallbacksAndMessages(null);

        confetti1.cancelAnimation();
        confetti1.clearAnimation();
        confetti1.setProgress(0f);
        confetti2.cancelAnimation();
        confetti2.clearAnimation();
        confetti2.setProgress(0f);
        confetti3.cancelAnimation();
        confetti3.clearAnimation();
        confetti3.setProgress(0f);

        dismissProgressDialog();
    }

    @Override
    public void stepsClicked(int step) {
        try {
            if (asatScreenProcessInfo != null) {
                UserAsatInfo userAsatInfo = asatScreenProcessInfo.getUserAsatInfo();
                if (step == ASATStep.UPLOAD_MARKSHEET.index()) {
                    // Upload Mark-sheet
                    if (userAsatInfo != null && (userAsatInfo.getMarksheetInfo() == null || !userAsatInfo.getMarksheetInfo().isUploaded()) && (userAsatInfo.getTestInfo() == null || !userAsatInfo.getTestInfo().isAttempted())) {
                        try {

                            //trigger event of asat marksheet clicked
                            Map<String, String> params = new HashMap<>();
                            params.put("screen", ASATActivity.class.getSimpleName());
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_upload_marksheet_click.getActionName(), params);

                            Intent intent = new Intent(this, UploadMarksheetActivity.class);
                            intent.putExtra("scholarShipInfoId", asatScreenProcessInfo.getId());
                            uploadResultLauncher.launch(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (userAsatInfo != null && (userAsatInfo.getMarksheetInfo() == null || !userAsatInfo.getMarksheetInfo().isUploaded())) {
                            CommonMethods.showPositiveSnackbar(rl_main_layout, getString(R.string.not_allowed), 3000);
                        } else if (userAsatInfo != null && userAsatInfo.getTestInfo() != null && userAsatInfo.getTestInfo().isAttempted()) {
                            CommonMethods.showPositiveSnackbar(rl_main_layout, getString(R.string.step1_complete_ho_gaya_hain), 3000);
                        } else if (userAsatInfo != null && userAsatInfo.getTestInfo() == null) {
                            CommonMethods.showPositiveSnackbar(rl_main_layout, getString(R.string.step1_complete_ho_gaya_hain_ab_test_dein), 3000);
                        } else {
                            CommonMethods.showPositiveSnackbar(rl_main_layout, getString(R.string.not_allowed), 3000);
                        }
                    }
                } else if (step == ASATStep.COMPLETE_TEST.index()) {
                    if (CommonMethods.isOnline(this)) {
                        // Test
                        if (userAsatInfo != null && (userAsatInfo.getTestInfo() == null || !userAsatInfo.getTestInfo().isAttempted())) {
                            if (!OfflineTestManager.getModelsByTestType(OfflineTestTypes.ASAT).isEmpty()) {
                                checkForOfflineASATTest();
                                return;
                            }
                            // Open Test
                            String initialSnackMessage = "";
                            if (userAsatInfo.getMarksheetInfo() == null || !userAsatInfo.getMarksheetInfo().isUploaded()) {
                                initialSnackMessage = getString(R.string.test_submit_ke_baad_marksheet_upload_nahi_hogi);
                            }

                            //trigger event of asat test clicked
                            Map<String, String> param = new HashMap<>();
                            param.put("screen", TAG);
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_test_click.getActionName(), param);

                            Intent intent = new Intent(this, ActivityTestIntro.class);
                            intent.putExtra("Id", asatScreenProcessInfo.getTestGroupId());
                            intent.putExtra("testId", asatScreenProcessInfo.getTestId());
                            intent.putExtra("subject", asatScreenProcessInfo.getTestTitle());
                            intent.putExtra("processInfoId", asatScreenProcessInfo.getId());
                            intent.putExtra("asatId", userAsatInfo.getId());
                            intent.putExtra("isAsat", true);
                            intent.putExtra("combined", true);
//                        intent.putExtra("startTest", true);
                            intent.putExtra(PreferenceKeys.TYPE.getKey(), "ATTEMPT");
                            intent.putExtra("testSeriesListId", asatScreenProcessInfo.getTestGroupId());
                            intent.putExtra("testTitle", asatScreenProcessInfo.getTestTitle());
                            intent.putExtra("initialSnackMessage", initialSnackMessage);
                            startActivity(intent);
                        } else {
                            if (userAsatInfo != null) {
                                CommonMethods.showPositiveSnackbar(rl_main_layout, getString(R.string.step2_complete_ho_gaya_hain_ab_result_check_karein), 3000);
                            }
                        }
                    } else {
                        CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.internet_failure_error));
                    }
                } else if (step == ASATStep.TEST_RESULT.index()) {
                    // Result

                    if (userAsatInfo != null && (userAsatInfo.getResultInfo() != null || userAsatInfo.getTestInfo() != null)) {
                        isResultScreen = true;
                        dataUpdateCounter = 1;
                        setAsatFragments(false);

                        //trigger event of asat result clicked
                        Map<String, String> params = new HashMap<>();
                        params.put("screen", ASATActivity.class.getSimpleName());
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_test_result_click.getActionName(), params);

                    } else {
                        CommonMethods.showPositiveSnackbar(rl_main_layout, getString(R.string.pehle_test_complete_karein), 3000);
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setAsatFragments(boolean isBack) {

        try {
            if (!isResultScreen) {
                //Main Screen
                cv_main_screen_bottom_control.setVisibility(View.VISIBLE);
                cv_result_screen_bottom_control.setVisibility(View.GONE);
                setTopFragment(asatScreenProcessInfo, isBack);
                if (dataUpdateCounter == 1) {
                    // initial setter
                    setVideoFragment(asatScreenProcessInfo.getVideoUrl(), asatScreenProcessInfo.getThumbnail(), isBack);
                    setBottomFragment(asatScreenProcessInfo, isBack);
                }

                List<ASATStepsInfo> list = asatScreenProcessInfo.getAsatStepsInfo();
                if (list.get(2).isDone() || list.get(1).isDone()) {
                    cv_take_test.setVisibility(View.GONE);
                    tv_upload_result.setText(getString(R.string.view_result));
                } else if (list.get(0).isDone()) {
                    cv_take_test.setVisibility(View.GONE);
                    tv_upload_result.setText(getString(R.string.Take_Test));
                }
            } else {
                UserAsatInfo userAsatInfo = asatScreenProcessInfo.getUserAsatInfo();
                cv_main_screen_bottom_control.setVisibility(View.GONE);
//                cv_result_screen_bottom_control.setVisibility(View.VISIBLE);
                if (userAsatInfo.getTestInfo() != null && userAsatInfo.getTestInfo().isAttempted() && userAsatInfo.getResultInfo() != null) {

                    // Updating Result as viewed by API
                    if (asatScreenProcessInfo.getAsatStepsInfo() != null && !asatScreenProcessInfo.getAsatStepsInfo().get(ASATStep.TEST_RESULT.index()).isDone()) {
                        updateResultStep();
                    }

                    ResultInfo resultInfo = userAsatInfo.getResultInfo();
                    setTopFragment(asatScreenProcessInfo, isBack);
                    if (dataUpdateCounter == 1) {
                        // initial setter
                        if (resultInfo.getInfoStatus().equals(ResultInfoStatus.PENDING.name())) {
                            String imgUrl = resultInfo.getImageUrl();
                            setVideoFragment("", StringUtils.isNotEmpty(imgUrl) ? imgUrl : asatScreenProcessInfo.getThumbnail(), isBack);
                        } else {
                            setVideoFragment(StringUtils.isNotEmpty(asatScreenProcessInfo.getResultVideoUrl()) ? asatScreenProcessInfo.getResultVideoUrl() : asatScreenProcessInfo.getVideoUrl(), asatScreenProcessInfo.getThumbnail(), isBack);
                        }
                        setBottomFragment(asatScreenProcessInfo, isBack);
                    }

                    if ((resultInfo.getInfoStatus().equals(ResultInfoStatus.REJECTED.name())
                            || resultInfo.getInfoStatus().equals(ResultInfoStatus.VERIFIED.name())) && !asatScreenProcessInfo.getUserAsatInfo().isSubscribed()) {
                        tv_cost_price.setText(String.format(getString(R.string.price_format), resultInfo.getPrincipalAmount()));
                        tv_discount_price.setText(String.format(getString(R.string.price_format), resultInfo.getFinalAmount()));
                        tv_subscription_title.setText(resultInfo.getSubscriptionName());
                        tv_cost_price.setPaintFlags(tv_cost_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        expiryTimeMillis = resultInfo.getExpiryTime();
                        startCountdown(expiryTimeMillis);

                        if (resultInfo.getPrincipalAmount().equals(resultInfo.getFinalAmount())) {
                            tv_cost_price.setVisibility(View.GONE);
                        }

                        String couponText = resultInfo.getCouponText();
                        if (couponText == null) {
                            couponText = "";
                            ll_discount_layout.setVisibility(View.GONE);
                        }

                        if(!isCouponExpired()) {
                            tv_discount_text.setText(HtmlCompat.fromHtml(couponText, HtmlCompat.FROM_HTML_MODE_LEGACY));
                        }

                        if (resultInfo.getInfoStatus().equals(ResultInfoStatus.VERIFIED.name()) && !isConfettiDone) {
                            confettiHandler = new Handler();
                            Runnable runnable = this::startAnimAndSound;
                            confettiHandler.postDelayed(runnable, 800);
                        }

                        selectedSubscription = new SubscriptionPlans();
                        selectedSubscription.setPlanId(resultInfo.getSubscriptionId());
                        selectedSubscription.setFinalPrice(isCouponExpired()?resultInfo.getOfferAmount():resultInfo.getFinalAmount());
                        selectedSubscription.setCostPrice(resultInfo.getPrincipalAmount());
                        selectedSubscription.setName(resultInfo.getSubscriptionName());

                        cv_result_screen_bottom_control.setVisibility(userAsatInfo.isSubscriptionPurchased() ? View.GONE : View.VISIBLE);
                        if (resultInfo.getCouponName() == null && resultInfo.getCouponText() == null && resultInfo.getSubscriptionName() == null) {
                            cv_result_screen_bottom_control.setVisibility(View.GONE);
                        }
                    } else {
                        cv_result_screen_bottom_control.setVisibility(View.GONE);
                    }

                    if ((resultInfo.getInfoStatus().equals(ResultInfoStatus.REJECTED.name())
                            || resultInfo.getInfoStatus().equals(ResultInfoStatus.VERIFIED.name()))) {
                        MyApplication.getASATStatusDatabaseReference().removeValue();
                    }

                    if (asatConfig.isCouponHTMLTextHidden()) {
                        ll_discount_layout.setVisibility(View.GONE);
                    }
                    if (asatConfig.isBottomBuyOptionHidden()) {
                        cv_result_screen_bottom_control.setVisibility(View.GONE);
                    }
                } else {
                    isResultScreen = false;
                    dataUpdateCounter = 1;
                    setAsatFragments(isBack);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        dataUpdateCounter++;
    }

    private void updateResultStep() {

        UserAsatInfo userAsatInfo = asatScreenProcessInfo.getUserAsatInfo();
        if (userAsatInfo == null) {
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("userAsatId", userAsatInfo.getId());
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                CommonMethods.getUrlParam(UPDATE_ASAT_STEP, map), null,
                response -> {
                    TypeToken<CallBackResponse> token = new TypeToken<CallBackResponse>() {
                    };
                    Gson gson = new Gson();
                    CallBackResponse callBackResponse = gson.fromJson(response.toString(), token.getType());

                    if (callBackResponse.isSuccess()) {
                        asatScreenProcessInfo.getAsatStepsInfo().get(ASATStep.TEST_RESULT.index()).setDone(true);
                    }
                    Log.d(TAG, "Response: " + response.toString());
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "Error updating video selection: " + error.getMessage());
                    }
                });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void getAsatProcessInfo(boolean isBack) {
        shimmer_layout.setVisibility(View.VISIBLE);
        shimmer_layout.startShimmer();

        if (isResultScreen) {
            rl_parent_layout.setVisibility(View.GONE);
            ll_bottom_layout.setVisibility(View.GONE);
        }

        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        map.put("viewed", true);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_ASAT_PROCESS_INFO, map), null,
                response -> {
            Log.d(TAG, "Asat Response: "+response.toString());
                    TypeToken<ASATScreenProcessInfo> token = new TypeToken<ASATScreenProcessInfo>() {
                    };
                    asatScreenProcessInfo = gson.fromJson(response.toString(), token);

//                    Log.d(TAG, "HideASAT: " + asatScreenProcessInfo.isHideAsat());
//                    Log.d(TAG, "UserAsat: " + asatScreenProcessInfo.getUserAsatInfo().toString());
                    if (asatScreenProcessInfo == null || asatScreenProcessInfo.isHideAsat() || (asatScreenProcessInfo.getUserAsatInfo() != null &&
                            asatScreenProcessInfo.getUserAsatInfo().isSubscribed() && !asatScreenProcessInfo.getUserAsatInfo().isSubscriptionPurchased())) {
                        navigateToHome();
                        return;
                    }

                    try {
                        isResultScreen = isResultScreen || (asatScreenProcessInfo.getUserAsatInfo() != null && (asatScreenProcessInfo.getUserAsatInfo().isSubscriptionPurchased() || asatScreenProcessInfo.getUserAsatInfo().getResultInfo() != null));
                        if (isResultScreen) {
                            isBackToMainScreenRestricted = true;
                        }
                    } catch (Exception ignored) {
                    }

                    dataUpdateCounter++;
                    shimmer_layout.stopShimmer();
                    shimmer_layout.setVisibility(View.GONE);
                    rl_parent_layout.setVisibility(View.VISIBLE);
                    ll_bottom_layout.setVisibility(View.VISIBLE);

                    MyApplication.saveData(PreferenceKeys.HIDE_ASAT_NEW_BADGE.getKey(), true);

                    setAsatFragments(isBack);
                    Log.d(TAG, "Response: " + response.toString());
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "Error updating video selection: " + error.getMessage());
                        shimmer_layout.stopShimmer();
                        shimmer_layout.setVisibility(View.GONE);
                        Toast.makeText(ASATActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void checkForOfflineASATTest() {
        OfflineTestManager offlineTestManager = OfflineTestManager.getInstance();
        List<OfflineTestSeriesModel> asatModels = OfflineTestManager.getModelsByTestType(OfflineTestTypes.ASAT);
        if (!asatModels.isEmpty()) {
            for (OfflineTestSeriesModel model : asatModels) {
                offlineTestManager.syncOfflineTest(this, model, new OfflineTestManager.OfflineTestSubmissionListener() {
                    @Override
                    public void onStart(boolean success, Object object) {
                        Toast.makeText(ASATActivity.this, getString(R.string.please_wait_while_we_sync_your_last_submission), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(boolean success, Object object) {
                        getAsatProcessInfo(false);
                    }

                    @Override
                    public void onError(boolean success, Object object) {
                        Toast.makeText(ASATActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void startAnimAndSound() {
        confetti1.playAnimation();
        confetti2.playAnimation();
        confetti3.playAnimation();
        mediaPlayer = MediaPlayer.create(this, R.raw.ding);
        mediaPlayer.start();
        isConfettiDone = true;
    }
    private void startPayment() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.subscription_payment_option_dialog_layout);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

        dialog.findViewById(R.id.close_btn).setOnClickListener(dv -> {
            dialog.dismiss();
        });

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
    }

    private void buyNowPreCall() throws JSONException {
        if (selectedSubscription != null) {
            Map<String, Object> map = new HashMap<>();
            SubscriptionPreModel subscriptionPreModel = new SubscriptionPreModel();
            subscriptionPreModel.setCoupon(getCoupon(asatScreenProcessInfo));
            subscriptionPreModel.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
            subscriptionPreModel.setSubscriptionPlanId(selectedSubscription.getPlanId());
            subscriptionPreModel.setOrderId(CampaignSubscriptionActivity.getAlphaNumericString(8));
            subscriptionPreModel.setTotalAmount(Long.parseLong(selectedSubscription.getFinalPrice()));
            subscriptionPreModel.setRedirectionFrom(redirectionSource != null ? redirectionSource.getFormattedValue() : "UNKNOWN");
            Log.d("SubscriptionSourceTrack", "Source: " + subscriptionPreModel.getRedirectionFrom() + " | Plan: " + selectedSubscription.getPlanId() + " (ASATActivity)");
            gson = new Gson();
            if (BuildConfig.DEBUG) Log.d(TAG, "buyNowPreCall Body: " + gson.toJson(subscriptionPreModel));
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), new JSONObject(gson.toJson(subscriptionPreModel)),
                    response -> {
                        TypeToken<UserSubscriptionDto> token = new TypeToken<UserSubscriptionDto>() {
                        };
                        UserSubscriptionDto userSubscriptionDto;
                        try {
                            userSubscriptionDto = gson.fromJson(response.getJSONObject("data").toString(), token.getType());

                            if (Integer.parseInt(selectedSubscription.getFinalPrice()) == 0) {
                                try {
                                    buyNowPostCall(CampaignSubscriptionActivity.getAlphaNumericString(8), userSubscriptionDto);
                                } catch (JSONException e) {
                                    Log.e("Error in ", e.getMessage(), e);
                                }
                            } else {
                                if (isPayingThroughQRCode) {
                                    if (selectedSubscription != null) {
                                        Intent intent = new Intent(this, QRCodePaymentActivity.class);
                                        intent.putExtra("orderId", userSubscriptionDto.getOrderId());
                                        intent.putExtra("subscriptionPlanId", selectedSubscription.getPlanId());
                                        intent.putExtra("transactionId", "");
                                        intent.putExtra("costPrice", selectedSubscription.getCostPrice());
                                        intent.putExtra("planName", selectedSubscription.getName());
                                        intent.putExtra("userSubscriptionDto", userSubscriptionDto);
                                        intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, redirectionSource);

                                        intent.putExtra("paymentAmount", Integer.parseInt(selectedSubscription.getFinalPrice()));
                                        startActivity(intent);
                                    }
                                } else {
                                    doPayment(Integer.parseInt(selectedSubscription.getFinalPrice()), userSubscriptionDto.getOrderId(), userSubscriptionDto);
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                            dismissProgressDialog();
                        }
                    }
                    , new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Log.i(TAG, error.getMessage());
                    Toast.makeText(ASATActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    dismissProgressDialog();
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
        } else {
            dismissProgressDialog();
        }
    }

    private CouponDto getCoupon(ASATScreenProcessInfo asatScreenProcessInfo) {
        if (asatScreenProcessInfo != null && asatScreenProcessInfo.getUserAsatInfo() != null &&
                asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getCouponName() != null) {
            return CouponDto.builder().code(asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getCouponName()).build();
        }
        return null;
    }


    private void buyNowPostCall(String transactionId, UserSubscriptionDto userSubscriptionDto) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        SubscriptionPostModel subscriptionPostModel = new SubscriptionPostModel();
        subscriptionPostModel.setPaymentStatus(PaymentStatus.SUCCESS.name());
        subscriptionPostModel.setInvoiceId(userSubscriptionDto.getInvoiceId());
        subscriptionPostModel.setTransactionId(transactionId);
        subscriptionPostModel.setSubscriptionId(userSubscriptionDto.getId());
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.PUT,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), new JSONObject(gson.toJson(subscriptionPostModel)),
                response -> {
                    try {
                        dismissProgressDialog();
                        boolean success = (boolean) response.get("success");
                        if (success) {
                            new Handler().postDelayed(() -> {
                                if (MyApplication.getFirebaseRemoteConfig().getBoolean(getString(R.string.show_subscription_benefits))) {
                                    try {
                                        Type listType = new TypeToken<List<SubscriptionBenefitsModel>>() {
                                        }.getType();
                                        ArrayList<SubscriptionBenefitsModel> list = gson.fromJson(String.valueOf(response.get("data")), listType);
                                        Intent intent = new Intent(this, SubscriptionCongatulationsActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putParcelableArrayListExtra("benefits", list);
                                        intent.putExtra("congratulationHeader", (response.has("congratulationHeader") && !response.isNull("congratulationHeader")) ? response.getString("congratulationHeader") : "");
                                        intent.putExtra("congratulationSubHeader", (response.has("congratulationSubHeader") && !response.isNull("congratulationSubHeader")) ? response.getString("congratulationSubHeader") : "");
                                        intent.putExtra("planId", selectedSubscription.getPlanId());
                                        startActivity(intent);
                                        finish();
                                    } catch (JSONException e) {
                                        Log.d(TAG, e.getMessage());
                                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Intent intent = new Intent(this, HomeScreen.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1000);

//                            new Handler().postDelayed(() -> this.finish(), 1000);
                        } else {
                            CommonMethods.showNegativeSnackbar(rl_main_layout, "Sorry we faced some technical problem, contact us.");
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        dismissProgressDialog();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(rl_main_layout, error.getMessage().toString());
                dismissProgressDialog();
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    void doPayment(int amount, String rzOrderId, UserSubscriptionDto userSubscriptionDto) {
        if (selectedSubscription != null) {
            Intent paymentGatewayIntent = new Intent(this, RazorpayPaymentGatewayActivity.class);
            paymentGatewayIntent.putExtra("planId", selectedSubscription.getPlanId());
            paymentGatewayIntent.putExtra("orderId", rzOrderId);
            paymentGatewayIntent.putExtra("amount", amount + "");
            paymentGatewayIntent.putExtra("userSubscriptionDto", userSubscriptionDto);
            paymentGatewayIntent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, redirectionSource);
            startActivity(paymentGatewayIntent);
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean isCouponExpired() {
        long currentTimeMillis = System.currentTimeMillis();
        long millisUntilFinished = expiryTimeMillis - currentTimeMillis;
        return millisUntilFinished <= 0;
    }
    private void setExpiryOfCoupon(long expiryTimeMillis) {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            long millisUntilFinished = expiryTimeMillis - currentTimeMillis;

            if (millisUntilFinished <= 0) {
                ll_expiry_layout.setVisibility(View.GONE);
                ll_discount_layout.setBackground(ContextCompat.getDrawable(this, R.drawable.red_white_gradient));
                tv_discount_text.setText(HtmlCompat.fromHtml(asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getExpiredCouponText(), HtmlCompat.FROM_HTML_MODE_LEGACY));
//                tv_discount_text.setText(asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getExpiredCouponText());
                tv_discount_text.setTextColor(ContextCompat.getColor(this, R.color.white));
                tv_cost_price.setText(String.format(getString(R.string.price_format), asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getPrincipalAmount()));
                tv_discount_price.setText(String.format(getString(R.string.price_format), asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getOfferAmount()));

            } else {
                ll_expiry_layout.setVisibility((asatConfig!=null && !asatConfig.isExpiryTimerHidden()) ? View.VISIBLE : View.GONE);
                ll_discount_layout.setBackground(ContextCompat.getDrawable(this, R.drawable.yellow_gradient_5));
                tv_discount_text.setText(HtmlCompat.fromHtml(asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getCouponText(), HtmlCompat.FROM_HTML_MODE_LEGACY));
//                tv_discount_text.setText(asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getCouponText());
                tv_discount_text.setTextColor(ContextCompat.getColor(this, R.color.black));
                tv_cost_price.setText(String.format(getString(R.string.price_format), asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getPrincipalAmount()));
                tv_discount_price.setText(String.format(getString(R.string.price_format), asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getFinalAmount()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void startCountdown(long expiryTimeMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        long millisUntilFinished = expiryTimeMillis - currentTimeMillis;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (millisUntilFinished <= 0) {
            tv_expiry_timer.setText("00:00:00");
            if(expiryTimeMillis!=0) {
                setExpiryOfCoupon(expiryTimeMillis);
            }
            return;
        }

        countDownTimer = new CountDownTimer(millisUntilFinished, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;

                String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
                tv_expiry_timer.setText(time);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "Timer On Finish");
                tv_expiry_timer.setText("00:00:00");
                setExpiryOfCoupon(expiryTimeMillis);
            }
        }.start();
    }

    private void showProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            return;
        }

        progressDialog = UploadMarksheetActivity.getProgressDialog(this, getString(R.string.please_wait));
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void setBottomFragment(ASATScreenProcessInfo asatScreenProcessInfo, boolean isBack) {
        Gson gson = new Gson();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ASAT_SCREEN_PROCESS_INFO, asatScreenProcessInfo);
        bundle.putBoolean(ASAT_SCREEN_TYPE_RESULT, isResultScreen);
        bundle.putString(ASAT_RULES, asatScreenProcessInfo.getRules());
        bundle.putString(ASAT_SYLLABUS, asatScreenProcessInfo.getSyllabus());
        bundle.putString(ASAT_TOPPERS_LIST, gson.toJson(asatScreenProcessInfo.getToppersList()));
        ASATBottomFragment asatBottomFragment = new ASATBottomFragment();
        asatBottomFragment.setArguments(bundle);
        startTransitionBottom(asatBottomFragment, isBack);
    }

    private void setTopFragment(ASATScreenProcessInfo asatScreenProcessInfo, boolean isBack) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ASAT_SCREEN_PROCESS_INFO, asatScreenProcessInfo);
        bundle.putBoolean(ASAT_SCREEN_TYPE_RESULT, isResultScreen);
        ASATTopFragment asatTopFragment = new ASATTopFragment();
        asatTopFragment.setArguments(bundle);
        asatTopFragment.setAsatListener(this);
        startTransitionTop(asatTopFragment, isBack);
    }

    private void setVideoFragment(String url, String thumbnail, boolean isBack) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("thumbnail", thumbnail);
        bundle.putBoolean(ASAT_SCREEN_TYPE_RESULT, isResultScreen);
        ASATVideoFragment asatVideoFragment = new ASATVideoFragment();
        asatVideoFragment.setArguments(bundle);
        startTransitionVideo(asatVideoFragment, isBack);
    }

    private void startTransitionVideo(Fragment fragment, boolean isBack) {
        if (savedInstanceStateBundle == null) {
            if (dataUpdateCounter == 1) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(
                        R.anim.fade_in_slide_in,
                        R.anim.fade_out_slide_out,
                        isBack ? R.anim.slide_out_right : R.anim.slide_in_left,
                        isBack ? R.anim.slide_in_left : R.anim.slide_out_right
                );
                fragmentTransaction.replace(R.id.fragment_container_view_video, fragment);
                fragmentTransaction.commitAllowingStateLoss();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view_video, fragment).commitAllowingStateLoss();
            }
        }
    }

    private void startTransitionTop(Fragment fragment, boolean isBack) {
        if (savedInstanceStateBundle == null) {
            if (dataUpdateCounter == 1) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(
                        R.anim.fade_in_slide_in,
                        R.anim.fade_out_slide_out,
                        isBack ? R.anim.slide_out_right : R.anim.slide_in_left,
                        isBack ? R.anim.slide_in_left : R.anim.slide_out_right
                );
                fragmentTransaction.replace(R.id.fragment_container_view_top, fragment);
                fragmentTransaction.commitAllowingStateLoss();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view_top, fragment).commitAllowingStateLoss();
            }
        }
    }

    private void startTransitionBottom(Fragment fragment, boolean isBack) {
        if (savedInstanceStateBundle == null) {
            if (dataUpdateCounter == 1) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(
                        R.anim.fade_in_slide_in,
                        R.anim.fade_out_slide_out,
                        isBack ? R.anim.slide_out_right : R.anim.slide_in_left,
                        isBack ? R.anim.slide_in_left : R.anim.slide_out_right
                );
                fragmentTransaction.replace(R.id.fragment_container_view_bottom, fragment);
                fragmentTransaction.commitAllowingStateLoss();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view_bottom, fragment).commitAllowingStateLoss();
            }
        }
    }
}