package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_DASHBOARD_VIDEO;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.V2_BUY_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.V2_GET_SUBSCRIPTION;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.RazorpayPaymentGatewayActivity;
import arivihan.technologies.doubtbuzzter2.ReelsActivity;
import arivihan.technologies.doubtbuzzter2.adapter.CampaignSubscriptionPlanAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ToppersAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonDialogs;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.ContactSectionBottomSheetDialog;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.enums.batch.videos.BatchVideoType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.BuySubscriptionModel;
import arivihan.technologies.doubtbuzzter2.model.CampaignSubscriptionScreenContactDto;
import arivihan.technologies.doubtbuzzter2.model.CouponDto;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.PriceBreakdownModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlanMetaData;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlans;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPostModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPreModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionOverview;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionRenewalReward;
import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.model.VideoResponse;
import arivihan.technologies.doubtbuzzter2.model.subscription.RewardCoins;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class ProPlanSubscriptionActivity extends AppCompatActivity implements ToppersAdapter.OnTopperClickListener,
        Player.Listener, CampaignSubscriptionPlanAdapter.OnSubscriptionCardClickListener
        , ContactSectionBottomSheetDialog.ContactSectionBottomSheetDialogListner {

    public static final String BUNDLE_SUBSCRIPTION_DATA = "subscription_lock_data";
    public static final String BUNDLE_COIN_FLOW = "coinFlow";
    private static final String KEY_COUPON_CODE = "couponCode";

    // Intent params — extracted once, retained across config changes
    private String navigationFrom;
    private String incomingCouponCode;
    private boolean showCourseInfoPopup;
    ConstraintLayout parentLayout, bottom_layout;
    private PlayerView videoPlayer;
    private ImageView thumbnail;
    private long videoStartingTime;
    private boolean videoEnded;
    private ExoPlayer player;
    private Handler handler;
    ImageView volume;
    String couponId = "";
    IntentFilter intentFilter;
    ArrayList<SubscriptionPlans> subscriptionPlans;
    SubscriptionModel subscriptionModel;
    TextView selectedCostPrice;
    TextView selectedDiscountPrice;
    TextView selectedSubscriptionTitle;
    ToppersAdapter toppersAdapter;
    RecyclerView toppersRv;
    ArrayList<ToppersHomeScreenModel> toppersList = new ArrayList<>();
    TextView discountPercentage, renewDiscountText;
    Gson gson;
    SubscriptionPlans selectedSubscription;
    boolean isPayingThroughQRCode;
    ShimmerFrameLayout imageShimmer;
    ShimmerFrameLayout dataShimmer;
    LinearLayout dataLayout;
    LinearLayout discountLayout;
    LinearLayout finalPriceLayout;
    boolean dataLoaded;
    String url;
    String videoUrl;
    ProgressBar spinnerVideoDetails;
    private static final int REQUEST_CODE = 100;
    CharSequence formattedText = null;
    SubscriptionRenewalReward subscriptionRenewalReward = null;
    LinearLayout renewDiscountLayout;
    boolean videoPlayed = false;
    private static final String KEY_VIDEO_URI = "video_uri";
    NetworkStatusLiveData networkStatusLiveData;
    private TextView toppersLayoutTitle, toppersLayoutSubTitle;
    private ActivityResultLauncher<Intent> qrPaymentLauncher;

    private MaterialCardView cvApplyCouponCard;
    EditText couponCodeEditText;
    String selectedPlanId, couponPlaceholderText;

    // Payment Summary views
    private MaterialCardView cvPaymentSummary;
    private TextView tvPsTotalPrice, tvPsTotalAmount, tvPsCouponLabel, tvPsCouponValue, tvPsCoinValue;
    private LinearLayout llPsCouponRow, llPsCoinRow;
    private boolean coinFlow = false;
    BuySubscriptionModel buySubscriptionModel;
    TextView tv_coupon_code_applied, tv_discount_layout_text;
    private MaterialCardView cv_subscription_plan_pdf;
    private CardView cv_subscription_pdf;
    String subscriptionBatchInfoPdf;
    private static final String TAG = ProPlanSubscriptionActivity.class.getSimpleName();
    private boolean isCouponProcessing = false;

    // Bottom bar — coin breakdown & discount badge
    private LinearLayout llCoinBreakdown;
    private TextView tvCoinBreakdownPrefix, tvCoinBreakdownValue;
    private MaterialCardView cvDiscountBadge;
    private TextView tvDiscountBadge;

    private MaterialCardView mcv_contact_us;

    private boolean isFirstResume = true;

    NestedScrollView scrollView;
    private ContactSectionBottomSheetDialog bottomSheetDialog;

    private boolean isBottomSheetOpened = false;


    private static final String PREF_NAME = "payment_pref";
    private static final String KEY_PAYMENT_TYPE = "key_payment_type";

    private static final String PAYMENT_QR = "QR";
    private static final String PAYMENT_GATEWAY = "GATEWAY";
    private TextView tv_call_to_sir;
    private CampaignSubscriptionScreenContactDto campaignSubscriptionScreenContactDto;
    private LinearLayout call_back;
    private CardView cv_call;
    private View floatingButton;

    private MaterialCardView cv_contact_us;
    private TextView tv_need_help, text_2_need_help, text_1_need_help;


    //new view
    ImageView ivBack;
    MaterialCardView joinEnrolled;
    ImageView proPlanSubCom;
    private String completedVideoUrl = "";
    private ImageView iv_share, iv_back;

    private boolean isReturningFromPayment = false;

    private RedirectionSource redirectionSource;
    private String analyticsSource;

    private static final String PREFS_PRE_CALL = "pre_call_prefs";
    private static final String KEY_RESPONSE = "pre_response_";
    private static final String KEY_TIMESTAMP = "pre_timestamp_";
    private static final long CACHE_DURATION_MS = 24 * 60 * 60 * 1000L;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set dark and light theme
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_plan_subscription);

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });

        initializeViews();
        extractIntentParams(savedInstanceState);

        if (savedInstanceState == null) {
            Map<String, String> subNavParams = new HashMap<>();
            subNavParams.put("source", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.sub_screen_opened.getActionName(), subNavParams);
        }

        boolean showPopup = getIntent().getBooleanExtra(
                PreferenceKeys.SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN.getKey(), false);

        if (showCourseInfoPopup) {
            MyApplication.saveData(PreferenceKeys.SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN.getKey(), true);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.wclm_sheet_join_karein_btn_click.getActionName(), withNavFrom(null));
        }
        intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastStringForAction);
        subscriptionModel = new SubscriptionModel();

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(view -> handleBackPress());

        getPlanData(null);
        toppersExtraction();
        startPayment();
        triggerSubscriptionPopUp();

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
    }

    private void handleBackPress() {
        if (!isBottomSheetOpened && campaignSubscriptionScreenContactDto != null) {
            isBottomSheetOpened = true;
            if (bottomSheetDialog == null) {
                triggerBottomSheet();
            }
        } else {
            finish();
        }
    }

    /**
     * Extracts all incoming params from intent extras or deeplink URI.
     * On config change, restores from savedInstanceState instead.
     */
    private void extractIntentParams(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore from saved state (config change / process death)
            navigationFrom = savedInstanceState.getString("navigationFrom", "unknown");
            coinFlow = savedInstanceState.getBoolean(BUNDLE_COIN_FLOW, false);
            incomingCouponCode = savedInstanceState.getString(KEY_COUPON_CODE);
            showCourseInfoPopup = savedInstanceState.getBoolean(PreferenceKeys.SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN.getKey(), false);
            return;
        }

        Intent intent = getIntent();
        Uri deepLinkUri = intent.getData();

        // Navigation source
        Object sourceExtra = intent.getSerializableExtra(SubscriptionNavigationHelper.EXTRA_SOURCE);
        redirectionSource = null;
        if (sourceExtra instanceof RedirectionSource) {
            redirectionSource = (RedirectionSource) sourceExtra;
            navigationFrom = redirectionSource.getFirebaseName();
        } else if (sourceExtra instanceof String) {
            navigationFrom = (String) sourceExtra;
        }

        if (StringUtils.isEmpty(navigationFrom) && deepLinkUri != null) {
            navigationFrom = "deeplink";
        }
        if (StringUtils.isEmpty(navigationFrom)) {
            navigationFrom = "unknown";
        }

        if (redirectionSource != null) {
            analyticsSource = redirectionSource.getFormattedValue();
        } else if (sourceExtra instanceof String) {
            analyticsSource = "REDIRECTED_FROM_" + ((String) sourceExtra).toUpperCase();
        } else {
            analyticsSource = AnalyticsEvent.SUBSCRIPTION_PRO_PLAN_SCREEN.name();
        }

        if (savedInstanceState == null) {
            ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.SUBSCRIPTION_PRO_PLAN_SCREEN.name(), analyticsSource);
        }

        // Coin flow flag
        coinFlow = intent.getBooleanExtra(BUNDLE_COIN_FLOW, false);

        // Course info popup flag
        showCourseInfoPopup = intent.getBooleanExtra(
                PreferenceKeys.SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN.getKey(), false);

        // Coupon code: intent extra first, then deeplink query param
        incomingCouponCode = intent.getStringExtra(KEY_COUPON_CODE);
        if (incomingCouponCode == null && deepLinkUri != null) {
            incomingCouponCode = deepLinkUri.getQueryParameter(KEY_COUPON_CODE);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("navigationFrom", navigationFrom);
        outState.putBoolean(BUNDLE_COIN_FLOW, coinFlow);
        outState.putString(KEY_COUPON_CODE, incomingCouponCode);
        outState.putBoolean(
                PreferenceKeys.SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN.getKey(), showCourseInfoPopup);
    }


    //set view
    private void setView(SubscriptionPlanMetaData metaData) {
        findViewById(R.id.layout_pro_plan_tag_line).setVisibility(View.VISIBLE);
        completedVideoUrl = metaData.getSubscriptionCompletedVideoUrl();
        iv_share = findViewById(R.id.iv_share);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
//            params.put("Has Purchased:", );
            params.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.pro_plan_back_btn_click.getActionName(), params);
            handleBackPress();
        });
        iv_share.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put("Channel", "Whatsapp Channel");
            params.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.pro_plan_share_btn_click.getActionName(), params);
            shareAppContent();
        });
        TextView premiumTextView = findViewById(R.id.pro_plan_tag_line);
        premiumTextView.setText(metaData.getTitle());
        applyGradientToText(premiumTextView);
        findViewById(R.id.layout_plan_validity).setVisibility(View.VISIBLE);
        TextView planValidity = findViewById(R.id.plan_validity);
        planValidity.setText(metaData.getPlanValidateText());
        applyGradientToText(planValidity);
        proPlanSubCom = findViewById(R.id.pro_plan_subscription_com);
        if (metaData.getPlanBenefitsImageUrl() != null) {
            Map<String, String> params = new HashMap<>();
            params.put("previous_screen", "subscription screen");
            params.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.pro_plan_benefits_click.getActionName(), params);
            Glide.with(this)
                    .load(metaData.getPlanBenefitsImageUrl())
                    .override(Target.SIZE_ORIGINAL)
                    .fitCenter()
                    .into(proPlanSubCom);
        } else {

            String lang;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
            } else {
                lang = getResources().getConfiguration().locale.getLanguage();
            }
            int imageRes;
            if (lang.equals("hi")) imageRes = R.drawable.pro_plan_subscription_com_hi_2;
            else imageRes = R.drawable.pro_plan_subscription_com_en_2;
            proPlanSubCom.setImageResource(imageRes);
        }
    }


    //apply text gradient golden
    private void applyGradientToText(final TextView textView) {
        if (textView == null) return;

        textView.post(() -> {
            TextPaint paint = textView.getPaint();

            Shader shader = new LinearGradient(
                    0f, 0f,
                    textView.getWidth(), textView.getTextSize(),
                    new int[]{0xFFEF9F0B, 0xFFFBD950, 0xFFFEEC77, 0xFFFBD950, 0xFFEA9C0B},
                    new float[]{0.1484f, 0.3647f, 0.5228f, 0.6656f, 0.8516f},
                    Shader.TileMode.CLAMP
            );

            paint.setShader(shader);
            textView.setTextColor(Color.WHITE);
            textView.invalidate();
        });
    }


    private void triggerBottomSheet() {
        if (isFinishing() || isDestroyed()) return;
        if (getSupportFragmentManager().isStateSaved()) return;
        if (campaignSubscriptionScreenContactDto != null && MyApplication.isNetworkAvailable()) {
            ContactSectionBottomSheetDialog existingSheet =
                    (ContactSectionBottomSheetDialog) getSupportFragmentManager()
                            .findFragmentByTag("ContactBottomSheet");

            if (existingSheet != null && existingSheet.isAdded()) return;

            stopVideoForBottomSheet();
            bottomSheetDialog = ContactSectionBottomSheetDialog.newInstance(campaignSubscriptionScreenContactDto);

            try {
                bottomSheetDialog.show(getSupportFragmentManager(), "ContactBottomSheet");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.contact_us_popup_view.getActionName(), withNavFrom(null));
                isBottomSheetOpened = true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to show contact bottom sheet", e);
            }
        }
    }

    private void stopVideoForBottomSheet() {

        Log.d("CampaignSubscription", "stopVideoForBottomSheet() called");

        if (player == null) {
            Log.w("CampaignSubscription", "Player is null, nothing to stop");
            return;
        }

        try {
            Log.d("CampaignSubscription", "Pausing video playback...");

            // Stop playback
            player.setPlayWhenReady(false);
            player.pause();

            Log.d("CampaignSubscription", "Playback paused successfully");

            // Update UI visibility
            videoPlayer.setVisibility(View.GONE);
            Log.d("CampaignSubscription", "VideoPlayer view hidden");

            volume.setVisibility(View.GONE);
            Log.d("CampaignSubscription", "Volume icon hidden");

            thumbnail.setVisibility(View.VISIBLE);
            Log.d("CampaignSubscription", "Thumbnail shown");

            Log.d("CampaignSubscription", "Video paused due to BottomSheet expansion");

        } catch (Exception e) {
            Log.e("CampaignSubscription", "Error while stopping video for BottomSheet", e);
        }
    }

    private void triggerSubscriptionPopUp() {
        try {
            SubscriptionLockPopupDto dto =
                    (SubscriptionLockPopupDto) getIntent().getSerializableExtra(BUNDLE_SUBSCRIPTION_DATA);
            Log.d(TAG, "triggerSubscriptionPopUp: " + getIntent().getSerializableExtra(BUNDLE_SUBSCRIPTION_DATA));
            if (dto != null) {
                CommonDialogs.showPremiumLockPopupDialog(this, dto);

                Map<String, String> params = new HashMap<>();
                params.put("title", dto.getTitle() != null ? dto.getTitle() : "");
                params.put("subTitle", dto.getSubTitle() != null ? dto.getSubTitle() : "");
                params.put("navigation_from", navigationFrom);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_screen_lock_pop_up_viewed.getActionName(), params);
            }
        } catch (Exception ignored) {
            HashMap<String, String> map =
                    (HashMap<String, String>) getIntent().getSerializableExtra(BUNDLE_SUBSCRIPTION_DATA);
            Log.d(TAG, "triggerSubscriptionPopUp: " + getIntent().getSerializableExtra(BUNDLE_SUBSCRIPTION_DATA));
            if (map != null) {

                SubscriptionLockPopupDto dto = SubscriptionLockPopupDto.builder().title(map.get("title")).subTitle(map.get("subTitle")).btnName(map.get("btnName")).build();
                CommonDialogs.showPremiumLockPopupDialog(this, dto);

                Map<String, String> params = new HashMap<>();
                params.put("title", dto.getTitle() != null ? dto.getTitle() : "");
                params.put("subTitle", dto.getSubTitle() != null ? dto.getSubTitle() : "");
                params.put("navigation_from", navigationFrom);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_screen_lock_pop_up_viewed.getActionName(), params);
            }
        }
    }


    private Map<String, String> withNavFrom(Map<String, String> params) {
        if (params == null) params = new HashMap<>();
        params.put("navigation_from", navigationFrom);
        return params;
    }

    private void logFirebaseEventsForPaymentTransaction(String modeOfPayment, HashMap<String, String> params) {
        try {
            params.put("mode_of_payment", modeOfPayment);
            params.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.initiate_payment_transaction.getActionName(), params);
        } catch (Exception ignored) {
        }
    }


    /**
     * Fetches screen data from the CAMPAIGN_SUBSCRIPTION_DATA API
     * and updates UI elements accordingly.
     */
    private void getScreenData(SubscriptionPlanMetaData metaData) {
        Log.d(TAG, "getScreenData: " + metaData);
        try {
            if (metaData != null) {
                setView(metaData);
                downloadThumbnail(metaData.getThumbnailUrl());
                videoUrl = metaData.getVideoUrl();
                if (metaData.getBatchPdfDetails() != null &&
                        StringUtils.isNotEmpty(metaData.getBatchPdfDetails().getPdfUrl())) {
                    subscriptionBatchInfoPdf =
                            metaData.getBatchPdfDetails().getPdfUrl();
                } else {
                    cv_subscription_pdf.setVisibility(View.GONE);
                }

                if (metaData.getCampaignSubscriptionScreenContactDto() != null) {

                    campaignSubscriptionScreenContactDto =
                            metaData.getCampaignSubscriptionScreenContactDto();

                    setUpContactusFloatingActionButton(
                            campaignSubscriptionScreenContactDto.getFloatingActionButtonText(),
                            campaignSubscriptionScreenContactDto.getContactSection()
                    );
                }

                if (!videoPlayed) {
                    initializePlayer();
                    startHandler();
                    videoPlayed = true;
                }
            }

        } catch (Exception e) {
            Log.e("uidata", "Error parsing screen data", e);
        }
    }

    private void setUpContactusFloatingActionButton(String floatingActionButtonText, String contactSection) {
        floatingButton.setVisibility(View.VISIBLE);
        cv_contact_us.setVisibility(View.VISIBLE);
        tv_call_to_sir.setText(
                StyledTextBuilder.fromJson(
                        this, floatingActionButtonText)
        );
        setNeedHelpContent(contactSection);
    }


    private void initializeViews() {
        spinnerVideoDetails = findViewById(R.id.spinnerVideoDetails);
        cv_contact_us = findViewById(R.id.cv_contact_us);
        text_1_need_help = findViewById(R.id.text_1_need_help);
        text_2_need_help = findViewById(R.id.text_2_need_help);
        tv_need_help = findViewById(R.id.tv_need_help);
        parentLayout = findViewById(R.id.main);
        mcv_contact_us = findViewById(R.id.mcv_contact_us);
        call_back = findViewById(R.id.floating_call_button);
        cv_call = findViewById(R.id.cv_call);

        tv_call_to_sir = findViewById(R.id.tv_call_to_sir);
        cv_call.setOnClickListener(v -> {
            Map<String, String> params = new HashMap<>();
            params.put("previous_screen", "pro_plan_subscription");
            params.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.pro_plan_call_support_click.getActionName(), params);
            if (campaignSubscriptionScreenContactDto != null) {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.contact_us_float_btn_click.getActionName(), withNavFrom(null));
                openDialerPad(campaignSubscriptionScreenContactDto.getPhoneNumber());
            }
        });
        bottom_layout = findViewById(R.id.bottom_layout);
        thumbnail = findViewById(R.id.thumbnail_image);
        thumbnail.setOnClickListener(view -> openPlayer());
        cvApplyCouponCard = findViewById(R.id.cv_apply_card);
        tv_coupon_code_applied = findViewById(R.id.tv_coupon_code_applied);
        tv_discount_layout_text = findViewById(R.id.discount_text);

        // Payment Summary
        cvPaymentSummary = findViewById(R.id.cv_payment_summary);
        tvPsTotalPrice = findViewById(R.id.tv_ps_total_price);
        tvPsTotalAmount = findViewById(R.id.tv_ps_total_amount);
        tvPsCouponLabel = findViewById(R.id.tv_ps_coupon_label);
        tvPsCouponValue = findViewById(R.id.tv_ps_coupon_value);
        tvPsCoinValue = findViewById(R.id.tv_ps_coin_value);
        llPsCouponRow = findViewById(R.id.ll_ps_coupon_row);
        llPsCoinRow = findViewById(R.id.ll_ps_coin_row);
        cv_subscription_plan_pdf = findViewById(R.id.cv_click_here);
        cv_subscription_pdf = findViewById(R.id.cv_subscription_pdf);
        cv_subscription_plan_pdf.setOnClickListener(view -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.pro_plan_pdf_click.getActionName(), withNavFrom(null));
            MyApplication.openPdfFromUrl(this,
                    subscriptionBatchInfoPdf);
        });

        NestedScrollView scrollView = findViewById(R.id.scroll_view);

        floatingButton = findViewById(R.id.floating_call_button_container);

        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

            if (scrollY > oldScrollY) {
                floatingButton.animate()
                        .translationY(300f)
                        .alpha(0f)
                        .setDuration(200)
                        .start();

            } else if (scrollY < oldScrollY) {
                floatingButton.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(200)
                        .start();
            }
        });
        couponCodeEditText = findViewById(R.id.coupon_code);
        videoPlayer = findViewById(R.id.player_view);
        videoPlayer.setOnClickListener(view -> openPlayer());
        volume = findViewById(R.id.volume);
        volume.setOnClickListener(view -> {
            if (player != null) {
                if (player.getVolume() == 0f) {
                    volume.setImageDrawable(getResources().getDrawable(R.drawable.volume));
                    player.setVolume(1f);
                } else {
                    volume.setImageDrawable(getResources().getDrawable(R.drawable.mute));
                    player.setVolume(0f);
                }
            }
        });
        cvApplyCouponCard.setOnClickListener(view -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.pro_plan_apply_code_click.getActionName(), withNavFrom(null));

            if (isCouponProcessing) return;

            String couponCode = couponCodeEditText.getText().toString().trim();
            if (!couponCode.isEmpty()) {

                setCouponProcessing(true);
                couponId = couponCode;
                checkCouponCodeValidity();
            } else {
                Toast.makeText(this, "Please enter a coupon code", Toast.LENGTH_SHORT).show();
            }
        });


        mcv_contact_us.setOnClickListener(view -> {
            if (campaignSubscriptionScreenContactDto != null) {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.contact_us_call_click.getActionName(), withNavFrom(null));
                openDialerPad(campaignSubscriptionScreenContactDto.getPhoneNumber());
            }
        });
        selectedCostPrice = findViewById(R.id.selected_cost_price);
        selectedDiscountPrice = findViewById(R.id.selected_discount_price);
        selectedSubscriptionTitle = findViewById(R.id.selected_subscription_title);

        // Bottom bar coin/discount views
        llCoinBreakdown = findViewById(R.id.ll_coin_breakdown);
        tvCoinBreakdownPrefix = findViewById(R.id.tv_coin_breakdown_prefix);
        tvCoinBreakdownValue = findViewById(R.id.tv_coin_breakdown_value);
        cvDiscountBadge = findViewById(R.id.cv_discount_badge);
        tvDiscountBadge = findViewById(R.id.tv_discount_badge);
        joinEnrolled = findViewById(R.id.join_content_layout);
        renewDiscountLayout = findViewById(R.id.renew_discount_layout);
        toppersRv = findViewById(R.id.toppers_rv);
//        discountPercentage = findViewById(R.id.disount_percentage);
        renewDiscountText = findViewById(R.id.renew_discount);
        imageShimmer = findViewById(R.id.image_shimmer);
        imageShimmer.startShimmer();
        dataShimmer = findViewById(R.id.data_shimmer);
        dataShimmer.startShimmer();
        dataLayout = findViewById(R.id.data_layout);
        discountLayout = findViewById(R.id.discount_layout);

        finalPriceLayout = findViewById(R.id.final_price_layout);
        toppersLayoutTitle = findViewById(R.id.toppers_title);
        toppersLayoutSubTitle = findViewById(R.id.toppers_sub_title);

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(parentLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    if (!dataLoaded)
                        getPlanData(null);
                    if (CollectionUtils.isEmpty(toppersList))
                        toppersExtraction();
                    if (spinnerVideoDetails.getVisibility() == View.VISIBLE) {
                        startPlayer();
                    }
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });
    }

    private void setCouponProcessing(boolean isProcessing) {
        isCouponProcessing = isProcessing;
        cvApplyCouponCard.setEnabled(!isProcessing);
        cvApplyCouponCard.setAlpha(isProcessing ? 0.6f : 1f);
    }


    private void openDialerPad(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No dialer app found on this device: " + e.getMessage());
            Toast.makeText(this, "No phone app found on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCouponCodeValidity() {
        dataLoaded = false;
        Map<String, Object> params = new HashMap<>();
        params.put("planId", selectedPlanId);
        params.put("couponCode", couponId);
        params.put("showDayPlans", true);
        params.put("coinFlow", coinFlow);
        Log.d("checkCouponCodeValidity", "checkCouponCodeValidity: " + params.toString());
        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(V2_BUY_SUBSCRIPTION, params), null,
                response -> {
                    if (!isActivityAlive()) return;
                    try {
                        Gson gson = new Gson();
                        Log.d("response", "checkCocheckCouponCodeValidityuponCodeValidity: " + response);
                        dataLoaded = true;
                        TypeToken<BuySubscriptionModel> token = new TypeToken<>() {
                        };
                        buySubscriptionModel = gson.fromJson(response.toString(), token.getType());
                        if (StringUtils.isEmpty(couponId) ||
                                buySubscriptionModel.getPricingBreakdown().isCouponCodeValid()) {
                            updateFinalPriceAfterCouponCodeApplied(buySubscriptionModel.getPricingBreakdown());
                            setUpDefaultDiscountLayoutAfterCouponApplied(buySubscriptionModel.getPricingBreakdown());
                        } else {
                            couponId = "";
                            Toast.makeText(ProPlanSubscriptionActivity.this, getString(R.string.coupon_code_is_invalid), Toast.LENGTH_LONG).show();
                        }

                        setCouponProcessing(false);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        setCouponProcessing(false);
                    }
                }, new VolleyGlobalErrorListener(this) {

            @Override
            public void postError(VolleyError error) {

                setCouponProcessing(false);
                //Toast.makeText(BuySubscriptionActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        jsonObjectRequest.setTag("PLAN_DATA_REQUEST");
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void updateFinalPriceAfterCouponCodeApplied(PriceBreakdownModel subscriptionPlan) {
        selectedSubscription.setFinalPrice(subscriptionPlan.getTotal());
        updatePaymentSummary(selectedSubscription, subscriptionPlan);
        updateBottomBar(selectedSubscription, subscriptionPlan);
    }

    private void setUpDefaultDiscountLayoutAfterCouponApplied(PriceBreakdownModel pricingBreakdown) {
        tv_coupon_code_applied.setVisibility(View.VISIBLE);
        String couponCode = couponPlaceholderText != null ? couponPlaceholderText : pricingBreakdown.getCouponCode();
        String message = getString(R.string.Discount_ARIVIHAN_Coupon_code_applied, pricingBreakdown.getCouponCodeValue() + "%", couponCode);
        discountLayout.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv_discount_layout_text.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_discount_layout_text.setText(Html.fromHtml(message));
        }
    }

    /**
     * Populates and shows the Payment Summary card.
     *
     * @param plan    the currently selected subscription plan (always non-null)
     * @param pricing pricing breakdown after coupon validation; null if no coupon applied yet
     */
    private void updatePaymentSummary(SubscriptionPlans plan, @Nullable PriceBreakdownModel pricing) {
        try {
            if (cvPaymentSummary == null) return;

            // Resolve effective pricing: param > subscriptionModel.pricingBreakdown
            PriceBreakdownModel effectivePricing = pricing;
            if (effectivePricing == null && subscriptionModel != null) {
                effectivePricing = subscriptionModel.getPricingBreakdown();
            }

            PriceBreakdownModel.PaymentSummary summary =
                    (effectivePricing != null) ? effectivePricing.getPaymentSummary() : null;

            if (summary != null) {
                cvPaymentSummary.setVisibility(View.VISIBLE);

                // Base Price (MRP)
                tvPsTotalPrice.setText(String.format(getString(R.string.price_format),
                        String.valueOf(summary.getBasePrice())));

                // Coupon / Discount row
                if (summary.getTotalDiscountAmount() > 0) {
                    llPsCouponRow.setVisibility(View.VISIBLE);
                    String discountLabel = getString(R.string.ps_coupon_discount_label);
                    if (summary.getTotalDiscountPercentage() > 0) {
                        discountLabel += " (" + summary.getTotalDiscountPercentage() + "%)";
                    }
                    tvPsCouponLabel.setText(discountLabel);
                    tvPsCouponValue.setText(String.format(getString(R.string.price_format),
                            String.valueOf(summary.getTotalDiscountAmount())));
                } else {
                    llPsCouponRow.setVisibility(View.GONE);
                }

                // Coin Discount row
                if (summary.getRewardCoins() > 0) {
                    llPsCoinRow.setVisibility(View.VISIBLE);
                    tvPsCoinValue.setText(String.valueOf(summary.getRewardCoins()));
                } else {
                    llPsCoinRow.setVisibility(View.GONE);
                }

                // Total Payable Amount
                tvPsTotalAmount.setText(String.format(getString(R.string.price_format),
                        String.valueOf(summary.getTotalPayableAmount())));
            } else {
                // Fallback: no PaymentSummary — use plan-level values
                cvPaymentSummary.setVisibility(View.VISIBLE);
                tvPsTotalPrice.setText(String.format(getString(R.string.price_format), plan.getCostPrice()));
                llPsCouponRow.setVisibility(View.GONE);
                llPsCoinRow.setVisibility(View.GONE);
                tvPsTotalAmount.setText(String.format(getString(R.string.price_format), plan.getFinalPrice()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Updates the bottom bar price section.
     * With coins: ₹249 / ₹9999 (₹999 - 🪙 750)
     * Without coins: ₹999 / ₹9999  [75% OFF]
     */
    private void updateBottomBar(SubscriptionPlans plan, @Nullable PriceBreakdownModel pricing) {
        try {
            if (llCoinBreakdown == null || cvDiscountBadge == null) return;

            // Hide plan name
            selectedSubscriptionTitle.setVisibility(View.GONE);

            PriceBreakdownModel effectivePricing = pricing;
            if (effectivePricing == null && subscriptionModel != null) {
                effectivePricing = subscriptionModel.getPricingBreakdown();
            }

            PriceBreakdownModel.PaymentSummary summary =
                    (effectivePricing != null) ? effectivePricing.getPaymentSummary() : null;

            if (summary != null) {
                // Base price (strikethrough MRP)
                selectedCostPrice.setText(String.format(getString(R.string.price_format),
                        String.valueOf(summary.getBasePrice())));
                selectedCostPrice.setPaintFlags(selectedCostPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                // Final payable amount
                selectedDiscountPrice.setText(String.format(getString(R.string.price_format),
                        String.valueOf(summary.getTotalPayableAmount())));

                // Coin breakdown row
                if (summary.getRewardCoins() > 0) {
                    tvCoinBreakdownPrefix.setText("(₹" + summary.getTotalPayableAmountWithoutRewardCoins() + "  −");
                    tvCoinBreakdownValue.setText(summary.getRewardCoins() + ")");
                    llCoinBreakdown.setVisibility(View.VISIBLE);
                    cvDiscountBadge.setVisibility(View.GONE);
                } else {
                    llCoinBreakdown.setVisibility(View.GONE);
                    // Discount badge
                    if (summary.getTotalDiscountPercentage() > 0) {
                        tvDiscountBadge.setText(summary.getTotalDiscountPercentage() + "% OFF");
                        cvDiscountBadge.setVisibility(View.VISIBLE);
                    } else {
                        cvDiscountBadge.setVisibility(View.GONE);
                    }
                }
            } else {
                // Fallback: no PaymentSummary — use plan-level values
                selectedCostPrice.setText(String.format(getString(R.string.price_format), plan.getCostPrice()));
                selectedCostPrice.setPaintFlags(selectedCostPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                selectedDiscountPrice.setText(String.format(getString(R.string.price_format), plan.getFinalPrice()));
                llCoinBreakdown.setVisibility(View.GONE);
                cvDiscountBadge.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "updateBottomBar error", e);
        }
    }

    private void openPlayer() {
        if (StringUtils.isNotEmpty(videoUrl)) {
            Intent intent = new Intent(ProPlanSubscriptionActivity.this, ExoPlayerActivity.class);
            intent.putExtra("video_uri", videoUrl);
            if (player != null)
                intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), player.getCurrentPosition());
            else if (videoEnded)
                intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), 0);
            else
                intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), videoStartingTime);

            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            videoStartingTime = data.getLongExtra(PreferenceKeys.VIDEO_STARTING.getKey(), 0);
            videoEnded = data.getBooleanExtra("video_ended", false);
        }
        if (requestCode == REQUEST_CODE && player != null) {
            if (player.getCurrentMediaItem() == null) {
                if (StringUtils.isNotEmpty(videoUrl))
                    buildMediaSource(Uri.parse(videoUrl));
            } else
                player.seekTo(0);
        }
    }

    private void updateFinalPrice(SubscriptionPlans subscriptionPlan) {
        if (subscriptionModel.getSubscriptionRenewalReward() != null) {
            Log.d("subscriptionReward", "updateFinalPrice: " +
                    subscriptionModel.getSubscriptionRenewalReward());
            subscriptionRenewalReward = (subscriptionModel.getPricingBreakdown()!=null && subscriptionModel.getPricingBreakdown().getRenewalReward()!=null) ? subscriptionModel.getPricingBreakdown().getRenewalReward() : subscriptionModel.getSubscriptionRenewalReward();
            SubscriptionRenewalReward renewalReward = subscriptionModel.getSubscriptionRenewalReward();
            if (renewalReward.getCouponCodeText() != null
                    && !renewalReward.getCouponCodeText().isEmpty()) {
                String couponCode = renewalReward.getCouponCode();
                String discountPercent = renewalReward.getDiscountPercentage() + "%";

                String fullText = getString(
                        R.string.Discount_ARIVIHAN_Coupon_code_applied,
                        discountPercent,
                        couponCode
                );

                SpannableString spannable = new SpannableString(fullText);

                int start = fullText.indexOf(couponCode);
                int end = start + ((couponCode != null) ? couponCode.length() : 0);

                spannable.setSpan(
                        new StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                tv_discount_layout_text.setText(spannable);
                discountLayout.setVisibility(View.VISIBLE);
                tv_coupon_code_applied.setVisibility(View.VISIBLE);
                couponCodeEditText.setText(couponCode);

                couponId = couponCode;

                int discount = renewalReward.getDiscountPercentage();
                int finalPrice = (int) parseLongSafe(subscriptionPlan.getFinalPrice(), 0);

                int discountedPrice = finalPrice - ((finalPrice * discount) / 100);

                String discountAmount = String.valueOf(discountedPrice);

                selectedDiscountPrice.setText(
                        String.format(getString(R.string.price_format), discountAmount)
                );

                subscriptionPlan.setFinalPrice(discountAmount);
            } else {
                discountLayout.setVisibility(View.GONE);
//                setUpDefaultDiscountLayout(subscriptionPlan);
            }
        } else {
            renewDiscountLayout.setVisibility(View.GONE);
//            setUpDefaultDiscountLayout(subscriptionPlan);
        }
        selectedCostPrice.setText(String.format(getString(R.string.price_format), subscriptionPlan.getCostPrice()));
        selectedCostPrice.setPaintFlags(selectedCostPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        selectedDiscountPrice.setText(String.format(getString(R.string.price_format), subscriptionPlan.getFinalPrice()));
        selectedSubscriptionTitle.setText(subscriptionPlan.getName());
        updatePaymentSummary(subscriptionPlan, null);
        updateBottomBar(subscriptionPlan, null);
    }

//    private void setUpDefaultDiscountLayout(SubscriptionPlans subscriptionPlan){
//
//        selectedDiscountPrice.setText(String.format(getString(R.string.price_format), subscriptionPlan.getFinalPrice()));
//        int discountAmount = Integer.parseInt(subscriptionPlan.getCostPrice()) - Integer.parseInt(subscriptionPlan.getFinalPrice());
//        int discount = (discountAmount * 100) / (Integer.parseInt(subscriptionPlan.getCostPrice()));
//        discountPercentage.setText(String.valueOf(discount));
//        discountLayout.setVisibility(View.VISIBLE);
//    }


    private void downloadThumbnail(String thumbnailUrl) {
        url = thumbnailUrl;
        if (StringUtils.isNotEmpty(url) && !isFinishing() && !isDestroyed()) {
            Glide.with(this)
                    .load(url)
                    .into(thumbnail);
            imageShimmer.stopShimmer();
            imageShimmer.setVisibility(View.GONE);
            thumbnail.setVisibility(View.VISIBLE);
        }
    }

    private void startHandler() {
        handler = new Handler();
        Runnable runnable = this::startPlayer;
        handler.postDelayed(runnable, 2000);
    }

    private void startPlayer() {
        if (StringUtils.isNotEmpty(videoUrl))
            buildMediaSource(Uri.parse(videoUrl));
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            videoPlayer.setPlayer(player);
        }
    }

    private void buildMediaSource(Uri mUri) {
        try {
            MediaItem mediaItem = new MediaItem.Builder().setUri(mUri).build();
            if (player == null) {
                initializePlayer();
            }
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);
            player.addListener(this);

            if (isBottomSheetOpened) {
                stopVideoForBottomSheet();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        if (player == null) return;
        switch (player.getPlaybackState()) {
            case Player.STATE_BUFFERING:
                spinnerVideoDetails.setVisibility(View.VISIBLE);
                videoPlayer.setVisibility(View.GONE);
                volume.setVisibility(View.GONE);
                thumbnail.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
                releasePlayer();
                videoPlayer.setVisibility(View.GONE);
                volume.setVisibility(View.GONE);
                thumbnail.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_IDLE:
                videoStartingTime = player.getCurrentPosition();
                break;
            case Player.STATE_READY:
                spinnerVideoDetails.setVisibility(View.GONE);
                thumbnail.setVisibility(View.GONE);
                videoPlayer.setVisibility(View.VISIBLE);
                volume.setVisibility(View.VISIBLE);
                if (videoStartingTime > 0) {
                    player.seekTo(videoStartingTime);
                    videoStartingTime = -1;
                }
                break;
            default:
                break;
        }
    }

    private void releasePlayer() {
        if (player != null) {
            videoPlayer.setPlayer(null);
            player.release();
            player = null;
        }
    }

    private void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }


    /**
     * Fetches subscription plans and the list of toppers from the API.
     *
     * @param planId The ID of the plan to fetch, or null to fetch default plans.
     */
    private void getPlanData(String planId) {
        Gson gson = new Gson();
        discountLayout.setVisibility(View.GONE);
        Map<String, Object> params = new HashMap<>();
        params.put("planLevel", "COURSE");
        params.put("boardSelected", true);
        params.put("comboSelected", false);
        params.put("couponCode", couponId);
        params.put("hideMonthlyPlans", true);
        if (planId != null) {
            params.put("selectedPlanId", planId);
        }
        params.put("fromCampaignSubscription", true);
        params.put("coinFlow", coinFlow);

        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(V2_GET_SUBSCRIPTION, params), null,
                response -> {
                    if (!isActivityAlive()) return;
                    TypeToken<SubscriptionModel> token = new TypeToken<SubscriptionModel>() {
                    };
                    Log.d(TAG, "getPlanData: " + response);
                    subscriptionModel = gson.fromJson(response.toString(), token.getType());
                    dataLoaded = true;
                    Log.d(TAG, "getPlanData: " + subscriptionModel.getPlanMetaData());

                    for (SubscriptionPlans plan : subscriptionModel.getPlans()) {
                        if (plan.isSelected()) {
                            Log.d("priceIssue", "getPlanData: " + plan);
                            updateFinalPrice(plan);
                            selectedSubscription = plan;
                            selectedPlanId = selectedSubscription.getPlanId();
                        }
                    }

                    // If server provided pricingBreakdown, use PaymentSummary for all display
                    if (selectedSubscription != null && subscriptionModel.getPricingBreakdown() != null) {
                        PriceBreakdownModel planPricing = subscriptionModel.getPricingBreakdown();
                        updatePaymentSummary(selectedSubscription, planPricing);
                        updateBottomBar(selectedSubscription, planPricing);
                    }

                    dataShimmer.setVisibility(View.GONE);
                    dataShimmer.stopShimmer();
                    if (subscriptionModel.getPlanMetaData() != null) {
                        Log.d(TAG, "getPlanData: " + subscriptionModel.getPlanMetaData());
                        getScreenData(subscriptionModel.getPlanMetaData());
                    }
//                    }else{
//                        subscriptionModel.setPlanMetaData(getDummyData());
//                        getScreenData(subscriptionModel.getPlanMetaData());
//                    }
                    dataLayout.setVisibility(View.VISIBLE);
                    if (subscriptionModel.getSubscriptionRenewalReward() != null) {
//                        getCouponAppliedPlanData(subscriptionModel.getOverview().getPlanId(),
//                                subscriptionRenewalReward);
                    } else {
                        discountLayout.setVisibility(View.GONE);
                    }

                    // Use already-extracted coupon (from intent extra or deeplink)
                    if (MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_DETECTED.getKey())) {
                        Log.d(TAG, "Referral flow activated.");
                        couponId = MyApplication.getStringDataFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_CODE.getKey());

                        couponPlaceholderText = MyApplication.getFirebaseRemoteConfig()
                                .getString("subscription_activity_coupon_code_placeholder_text");
                        checkForAutoAppliedCoupon(couponId, couponPlaceholderText);

                    } else if (StringUtils.isNotEmpty(incomingCouponCode)) {
                        couponId = incomingCouponCode;
                        checkForAutoAppliedCoupon(couponId, incomingCouponCode);
                    } else {
                        Log.d(TAG, "No referral/intent coupon detected. Using default flow.");
                    }


                    finalPriceLayout.setVisibility(View.VISIBLE);
                    bottom_layout.setVisibility(View.VISIBLE);
                }, new VolleyGlobalErrorListener(this) {

            @Override
            public void postError(VolleyError error) {
                Toast.makeText(ProPlanSubscriptionActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        jsonObjectRequest.setTag("PLAN_DATA_REQUEST");
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void checkForAutoAppliedCoupon(String couponId, String couponName) {
        try {
            long timeInMillis = MyApplication.getFirebaseRemoteConfig().getLong("subscription_auto_applied_timer");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isActivityAlive()) return;
                couponCodeEditText.getText().clear();
                couponCodeEditText.getText().append(couponName);
                checkCouponCodeValidity();
            }, timeInMillis);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Fetches and extracts the list of toppers from the API.
     */
    private void toppersExtraction() {
        setToppersLayoutText();
        toppersAdapter = new ToppersAdapter(this, toppersList, this);
        toppersRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        toppersRv.setAdapter(toppersAdapter);
        Map<String, Object> toppersRequestParams = new HashMap<>();
        try {
            toppersRequestParams.put("type", BatchVideoType.CHAMPIONS_OF_ARIVIHAN.name());
        } catch (Exception ignored) {
        }

        MyJsonObjectRequest toppersRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_DASHBOARD_VIDEO, toppersRequestParams), null,
                response -> {
                    if (!isActivityAlive()) return;
                    try {
                        Log.d(TAG, "toppersExtraction: " + response);
                        JSONArray toppersArray = response.getJSONArray("data");
                        TypeToken<List<VideoResponse>> token = new TypeToken<List<VideoResponse>>() {
                        };
                        Gson gson = new Gson();
                        List<VideoResponse> videoList = gson.fromJson(toppersArray.toString(), token.getType());

                        if (videoList == null || videoList.isEmpty()) {
                            toppersRv.setVisibility(View.GONE);
                            return;
                        }
                        List<ToppersHomeScreenModel> list = new ArrayList<>();
                        for (VideoResponse video : videoList) {
                            ToppersHomeScreenModel model = new ToppersHomeScreenModel();
                            model.setPosterUrl(video.getImageUrl());
                            model.setVideoUrl(video.getVideoUrl());
                            list.add(model);
                        }
                        toppersList.clear();
                        toppersList.addAll(list);
                        toppersAdapter.notifyDataSetChanged();
                        toppersRv.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.d("toppersExtraction", "Parsing error: " + e.getMessage());
                    }

                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                    }
                });

        toppersRequest.setTag("TOPPERS_API_TAG");
        MyVolleySingleton.getInstance(this).addToRequestQueue(toppersRequest);
    }

    private void setToppersLayoutText() {
        String language = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
        String gradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());

        String remoteJson = MyApplication.getFirebaseRemoteConfig().getString("toppers_layout_batch_text_v2");

        try {
            JSONObject root = new JSONObject(remoteJson);

            JSONObject gradeObj = (gradeCode != null && !gradeCode.isEmpty() && root.has(gradeCode))
                    ? root.getJSONObject(gradeCode)
                    : root.optJSONObject("default");
            JSONObject examObj = (gradeObj != null && gradeObj.has(examCode))
                    ? gradeObj.getJSONObject(examCode)
                    : (gradeObj != null ? gradeObj.optJSONObject("default") : null);

            if (examObj != null && examObj.has(language)) {
                JSONObject langObj = examObj.getJSONObject(language);

                String title = langObj.optString("title", "");
                String subtitle = langObj.optString("subtitle", "");

                Log.d("TopperLayout", "Title: " + title);
                Log.d("TopperLayout", "Subtitle: " + subtitle);
                toppersLayoutTitle.setText(title);
                toppersLayoutSubTitle.setText(subtitle);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("TopperLayout", "Invalid JSON format in toppers_layout_batch_text_v2");
        }
    }

    private void startPayment() {
        joinEnrolled.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.join_karein_cta_click.getActionName(), withNavFrom(null));
            try {
                if (isSubscriptionAmountIsZero()) {
                    if (CommonMethods.isOnline(this)) {
                        buyNowPreCall();
                    } else {
                        CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.error_no_internet_connection));
                    }
                } else {
                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.pro_plan_subscription_payment_option_dialog_layout);
                    if (dialog.getWindow() != null) {
                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    }
                    dialog.setCancelable(false);

                    dialog.findViewById(R.id.close_btn).setOnClickListener(dv -> {
                        dialog.dismiss();
                    });

                    dialog.findViewById(R.id.pay_with_qr_btn).setOnClickListener(dv -> {
                        isPayingThroughQRCode = true;
                        dialog.dismiss();
                        try {
                            buyNowPreCall();
                            MyApplication.saveData(KEY_PAYMENT_TYPE, PAYMENT_QR);
                        } catch (Exception e) {
                            Log.e(TAG, "Error initiating QR payment", e);
                            Toast.makeText(this, "An error occurred from our end, please try again later", Toast.LENGTH_LONG).show();
                        }

                    });

                    applyGradientToText(dialog.findViewById(R.id.pay_with_payment_gateway_btn_text));

                    dialog.findViewById(R.id.pay_with_payment_gateway_btn).setOnClickListener(dv -> {
                        isPayingThroughQRCode = false;
                        dialog.dismiss();
                        try {
                            buyNowPreCall();
                            MyApplication.saveData(KEY_PAYMENT_TYPE, PAYMENT_GATEWAY);
                        } catch (Exception e) {
                            Toast.makeText(this, "An error occurred from our end, please try again later", Toast.LENGTH_LONG).show();
                        }
                    });

                    dialog.show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private boolean isSubscriptionAmountIsZero() {
        try {
            PriceBreakdownModel pricing = (buySubscriptionModel != null)
                    ? buySubscriptionModel.getPricingBreakdown()
                    : (subscriptionModel != null ? subscriptionModel.getPricingBreakdown() : null);

            long amount;

            if (pricing != null && pricing.getPaymentSummary() != null) {
                amount = pricing.getPaymentSummary().getTotalPayableAmount();
            } else if (pricing != null && pricing.getTotal() != null) {
                amount = parseLongSafe(pricing.getTotal(), 0);
            } else {
                amount = parseLongSafe(selectedSubscription.getFinalPrice(), 0);
            }

            Log.d(TAG, "---> Subscription zero check: totalPayableAmount: " + amount + " <--");
            return amount == 0;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void buyNowPreCall() throws JSONException {

        Log.d("BUY_NOW_FLOW", "buyNowPreCall() started...");

        if (selectedSubscription == null) {
            Log.e("BUY_NOW_FLOW", "Selected subscription is NULL. Cannot proceed.");
            return;
        }

        Log.d("BUY_NOW_FLOW", "Subscription Selected: " + selectedSubscription.getName());
        Log.d("BUY_NOW_FLOW", "Final Price: " + selectedSubscription.getFinalPrice());

        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        String planId = selectedSubscription.getPlanId();
        String cacheKey = userId + "_" + planId;

        String cached = MyApplication.getStringData(KEY_RESPONSE + cacheKey);
        long savedAt = MyApplication.getLongData(KEY_TIMESTAMP + cacheKey);
        boolean isValid = cached != null
                && (System.currentTimeMillis() - savedAt) < CACHE_DURATION_MS;

//        if (isValid) {
//            Log.d("BUY_NOW_FLOW", "Cache hit — skipping network call for key: " + cacheKey);
//            try {
//                Log.i("BUY_NOW_FLOW", "buyNowPreCall: " + cached);
//                handlePreCallResponse(new JSONObject(cached));
//            } catch (Exception e) {
//                Log.w("BUY_NOW_FLOW", "Cached JSON corrupted, retrying network: " + e.getMessage());
//                MyApplication.removeString(KEY_RESPONSE + cacheKey);
//                MyApplication.removeString(KEY_TIMESTAMP + cacheKey);
//                buyNowPreCall();
//            }
//            return;
//        }

        Map<String, Object> map = new HashMap<>();
        SubscriptionPreModel subscriptionPreModel = new SubscriptionPreModel();
        subscriptionPreModel.setUserId(userId);

        if (StringUtils.isNoneEmpty(couponId)) {
            Log.d("BUY_NOW_FLOW", "Coupon Applied: " + couponId);
            CouponDto couponDto = new CouponDto();
            couponDto.setCode(couponId);
            subscriptionPreModel.setCoupon(couponDto);
        } else {
            Log.d("BUY_NOW_FLOW", "No Coupon Applied");
        }

        subscriptionPreModel.setSubscriptionPlanId(planId);
        subscriptionPreModel.setOrderId(getAlphaNumericString(8)); // frozen into cache on success
        subscriptionPreModel.setRedirectionFrom(analyticsSource != null ? analyticsSource : "UNKNOWN");

        // Determine totalAmount to send (see pricing scenarios A-G)
        long amountToSend;
        if (subscriptionRenewalReward != null && subscriptionRenewalReward.getNetAfterRenewal() > 0) {
            // Scenario B: renewal reward with netAfterRenewal
            amountToSend = (long) subscriptionRenewalReward.getNetAfterRenewal();
        } else {
            // All other scenarios: use overview.finalPrice (latest from buy-plan or /plan)
            SubscriptionOverview ov = null;
            if (buySubscriptionModel != null && buySubscriptionModel.getOverview() != null) {
                ov = buySubscriptionModel.getOverview();
            } else if (subscriptionModel != null && subscriptionModel.getOverview() != null) {
                ov = subscriptionModel.getOverview();
            }
            amountToSend = (ov != null)
                    ? parseLongSafe(ov.getFinalPrice(), 0)
                    : parseLongSafe(selectedSubscription.getFinalPrice(), 0);

        }
        subscriptionPreModel.setTotalAmount(amountToSend);

        // Set coins applied if available
        PriceBreakdownModel latestPricing = (buySubscriptionModel != null)
                ? buySubscriptionModel.getPricingBreakdown()
                : (subscriptionModel != null ? subscriptionModel.getPricingBreakdown() : null);
        if (latestPricing != null && latestPricing.getRewardCoins() != null) {
            subscriptionPreModel.setCoinsApplied(latestPricing.getRewardCoins().getCoinsApplied());
        }

//        Log.d("BUY_NOW_FLOW", "Generated OrderId : " + subscriptionPreModel.getOrderId());
//        Log.d("BUY_NOW_FLOW", "PlanId            : " + planId);
//        Log.d("BUY_NOW_FLOW", "Total Amount      : " + subscriptionPreModel.getTotalAmount());
//        Log.d("BUY_NOW_FLOW", "Coins Applied     : " + subscriptionPreModel.getCoinsApplied());
//        Log.d("SubscriptionSourceTrack", "Source: " + subscriptionPreModel.getRedirectionFrom() + " | Plan: " + planId + " (ProPlanSubscriptionActivity)");
//        Log.d("BUY_NOW_FLOW", "PreCall Body      : " + gson.toJson(subscriptionPreModel));
//        Log.d("BUY_NOW_FLOW", "Sending Subscription Pre API Request...");

        gson = new Gson();

        map.put("coinFlow", coinFlow);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(
                Request.Method.POST,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map),
                new JSONObject(gson.toJson(subscriptionPreModel)),
                response -> {
                    if (!isActivityAlive()) return;
                    Log.d("BUY_NOW_FLOW", "API Response Received: " + response);

                    MyApplication.saveData(KEY_RESPONSE + cacheKey, response.toString());
                    MyApplication.saveData(KEY_TIMESTAMP + cacheKey, System.currentTimeMillis());


                    try {
                        handlePreCallResponse(response);
                    } catch (Exception e) {
                        Log.e("BUY_NOW_FLOW", "Error while parsing response: " + e.getMessage(), e);
                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e("BUY_NOW_FLOW", "API Error: " + (error.getMessage() != null ? error.getMessage() : "unknown"));
                        Toast.makeText(ProPlanSubscriptionActivity.this,
                                "Payment Failed: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        Log.d("BUY_NOW_FLOW", "Adding request to Volley Queue...");
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void handlePreCallResponse(JSONObject response) throws Exception {
        gson = new Gson();
        TypeToken<UserSubscriptionDto> token = new TypeToken<UserSubscriptionDto>() {
        };
        UserSubscriptionDto userSubscriptionDto =
                gson.fromJson(response.getJSONObject("data").toString(), token.getType());

        Log.d("BUY_NOW_FLOW", "Parsed UserSubscriptionDto — OrderId: " + userSubscriptionDto.getOrderId());

        if (isSubscriptionAmountIsZero()) {
            Log.d("BUY_NOW_FLOW", "Free subscription — calling buyNowPostCall...");
            buyNowPostCall(getAlphaNumericString(8), userSubscriptionDto);

        } else if (isPayingThroughQRCode) {
            Log.d("BUY_NOW_FLOW", "Payment Mode: QR Code — launching QRCodePaymentActivity...");
            Intent intent = new Intent(ProPlanSubscriptionActivity.this, QRCodePaymentActivity.class);
            intent.putExtra("orderId", userSubscriptionDto.getOrderId());
            intent.putExtra("subscriptionPlanId", selectedSubscription.getPlanId());
            intent.putExtra("completedVideoUrl", completedVideoUrl);
            intent.putExtra("transactionId", "");
            intent.putExtra("costPrice", selectedSubscription.getCostPrice());
            intent.putExtra("planName", selectedSubscription.getName());
            intent.putExtra("userSubscriptionDto", userSubscriptionDto);
            intent.putExtra("paymentAmount", (int) parseLongSafe(selectedSubscription.getFinalPrice(), 0));
            intent.putExtra(QRCodePaymentActivity.BUNDLE_COIN_FLOW, coinFlow);
            // When coinFlow is active, pass the actual payable amount (after coin deduction) for display
            if (coinFlow) {
                PriceBreakdownModel qrPricing = (buySubscriptionModel != null)
                        ? buySubscriptionModel.getPricingBreakdown()
                        : (subscriptionModel != null ? subscriptionModel.getPricingBreakdown() : null);
                if (qrPricing != null && qrPricing.getRewardCoins() != null
                        && qrPricing.getRewardCoins().getCoinsApplied() > 0) {
                    intent.putExtra(QRCodePaymentActivity.BUNDLE_DISPLAY_PAYABLE_AMOUNT,
                            qrPricing.getRewardCoins().getNetPayable());
                }
            }
            logFirebaseEventsForPaymentTransaction(
                    "QRCodePaymentActivity",
                    CommonMethods.convertToStringMap(CommonMethods.getIntentParams(intent)));
            isReturningFromPayment = true;
            qrPaymentLauncher.launch(intent);

        } else {
            Log.d("BUY_NOW_FLOW", "Payment Mode: Direct Gateway — calling doPayment()...");
//            doPayment((int) parseLongSafe(selectedSubscription.getFinalPrice(), 0),
//                    userSubscriptionDto.getOrderId(), userSubscriptionDto);

            doPayment((int) parseLongSafe(String.valueOf(userSubscriptionDto.getAmountPaid().intValue()), 0),
                    userSubscriptionDto.getOrderId(), userSubscriptionDto);
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

        map.put("coinFlow", coinFlow);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.PUT,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), new JSONObject(gson.toJson(subscriptionPostModel)),
                response -> {
                    if (!isActivityAlive()) return;
                    try {
                        boolean success = (boolean) response.get("success");
                        if (success) {
                            new Handler().postDelayed(() -> finishActivity(success), 1000);
                        } else {
                            CommonMethods.showNegativeSnackbar(parentLayout, "Sorry we faced some technical problem, contact us.");
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(parentLayout, error.getMessage() != null ? error.getMessage().toString() : getString(R.string.something_went_wrong));
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void finishActivity(boolean isSubscribed) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("isSubscribed", isSubscribed);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private boolean isActivityAlive() {
        return !isFinishing() && !isDestroyed();
    }

    private static long parseLongSafe(String value, long fallback) {
        try {
            if (value == null || value.trim().isEmpty()) return fallback;
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
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
            paymentGatewayIntent.putExtra("completedVideoUrl", completedVideoUrl);
            paymentGatewayIntent.putExtra(PreferenceKeys.COUPON_ID.getKey(), couponId);
            paymentGatewayIntent.putExtra("amount", amount + "");
            paymentGatewayIntent.putExtra("userSubscriptionDto", userSubscriptionDto);
            paymentGatewayIntent.putExtra(RazorpayPaymentGatewayActivity.BUNDLE_COIN_FLOW, coinFlow);
            logFirebaseEventsForPaymentTransaction("RazorpayPaymentGatewayActivity", CommonMethods.convertToStringMap
                    (CommonMethods.getIntentParams(paymentGatewayIntent)));
            startActivity(paymentGatewayIntent);
        }
    }

    public static SubscriptionLockPopupDto getSubscriptionLockPopupDto(String type) {

        try {
            String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
            String gradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());
            String yearCode = MyApplication.getStringData(PreferenceKeys.YEAR_CODE.getKey());

            String specificKey = "subscription_lock_popup_config_" + examCode + "_" + gradeCode + "_" + yearCode;
            String defaultKey = "subscription_lock_popup_config_default";

            String json = MyApplication.getFirebaseRemoteConfig().getString(specificKey);

            // Fallback to default if specific key is not configured
            if (json == null || json.trim().isEmpty() || json.trim().equals("{}")) {
                json = MyApplication.getFirebaseRemoteConfig().getString(defaultKey);
            }

            ObjectMapper mapper = new ObjectMapper();

            Map<String, List<SubscriptionLockPopupDto>> configMap =
                    mapper.readValue(
                            json,
                            new TypeReference<Map<String, List<SubscriptionLockPopupDto>>>() {
                            }
                    );

            List<SubscriptionLockPopupDto> configs = configMap.get(type);

            if (configs == null || configs.isEmpty()) return null;

            return configs.get(0);

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CampaignSubscription", "onResume called");
        Log.d("CampaignSubscription", "getScreenData() triggered");
        if (isFirstResume) {

            Log.d("CampaignSubscription_LIFECYCLE",
                    "onResume() → First launch resume detected. Skipping BottomSheet trigger.");

            isFirstResume = false;

        } else {

            Log.d("CampaignSubscription_LIFECYCLE",
                    "onResume() → Subsequent resume detected. Evaluating BottomSheet state.");

            if (isReturningFromPayment) {
                isReturningFromPayment = false;

                if (bottomSheetDialog == null && !isBottomSheetOpened) {
                    Log.d("CampaignSubscription_BOTTOMSHEET",
                            "BottomSheet state → Returning from payment. Triggering now.");
                    triggerBottomSheet();
                } else {
                    Log.d("CampaignSubscription_BOTTOMSHEET",
                            "BottomSheet state → Already exists or previously opened. Skipping trigger.");
                }

            } else {
                Log.d("CampaignSubscription_BOTTOMSHEET",
                        "BottomSheet state → Not returning from payment. Skipping trigger.");
            }
        }

        if (MyApplication.getBoolData(PreferenceKeys.OTHER_PAYMENT.getKey())) {
            Log.d("CampaignSubscription", "other_payment flag TRUE → Processing payment flow");

            isPayingThroughQRCode = false;
            MyApplication.saveData(PreferenceKeys.OTHER_PAYMENT.getKey(), false);

            try {
                buyNowPreCall();
                Log.d("CampaignSubscription", "buyNowPreCall() triggered successfully");
            } catch (Exception e) {
                Log.e("CampaignSubscription", "Error in buyNowPreCall()", e);
                Toast.makeText(this,
                        "An error occurred from our end, please try again later",
                        Toast.LENGTH_LONG).show();
            }
        } else if (videoPlayer.getVisibility() == View.VISIBLE) {

            Log.d("CampaignSubscription", "Video visible → Resuming player");
            resumePlayer();

        } else {
            Log.d("CampaignSubscription", "No payment flow & video not visible");
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        pausePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void onTopperClick(String videoUrl) {
        try {
            if (StringUtils.isNotEmpty(videoUrl)) {
                Intent intent = new Intent(this, ReelsActivity.class);
                intent.putExtra(KEY_VIDEO_URI, videoUrl);
                Map<String, String> params = new HashMap<>();
                params.put("video_url", videoUrl);
                params.put("open_from", "UnnatiScreen");
                params.put("navigation_from", navigationFrom);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.champions_of_arivihan_clicked.getActionName(), params);
                startActivity(intent);
            }
        } catch (Exception ignored) {
        }

    }


    @Override
    public void onCardClick(int position, SubscriptionPlans subscriptionPlan) {
        if (subscriptionPlan != null) {
            selectedSubscription = subscriptionPlan;
            getPlanData(subscriptionPlan.getPlanId());
        }
    }

    @Override
    public void onPaymentOptionClick() {
        if (StringUtils.isEmpty(MyApplication.getStringData(KEY_PAYMENT_TYPE))) {
            joinEnrolled.performClick();
        } else if (MyApplication.getStringData(KEY_PAYMENT_TYPE).equals(PAYMENT_QR)) {
            isPayingThroughQRCode = true;
            try {
                buyNowPreCall();
            } catch (Exception e) {
                Log.e(TAG, "Error initiating QR payment from option click", e);
//                Toast.makeText(this, "An error occurred from our end, please try again later", Toast.LENGTH_LONG).show();
            }
        } else {
            isPayingThroughQRCode = false;
            try {
                buyNowPreCall();
            } catch (Exception e) {
                Toast.makeText(this, "An error occurred from our end, please try again later", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCloseButtonClick() {
        try {
            if (bottomSheetDialog != null) {
                bottomSheetDialog.dismiss();
                bottomSheetDialog = null;
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void openAndroidDialerPad(String phoneNumber) {
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.contact_us_popup_call_section_click.getActionName(), withNavFrom(null));
        openDialerPad(phoneNumber);
    }


    private void setNeedHelpContent(String remoteJson) {
        try {
            if (remoteJson == null || remoteJson.trim().isEmpty()) {
                Log.e("NeedHelpPopup", "Remote JSON is EMPTY!");
                return;
            }

            JSONObject root = new JSONObject(remoteJson);

            // Direct extraction (no courseId, no language)
            String t1 = root.optString("text_1", "");
            String t2 = root.optString("text_2", "");
            String tvHelp = root.optString("tv_need_help", "");

            setRichText(text_1_need_help, t1, R.string.pro_plan_help_text_1);
            setRichText(text_2_need_help, t2, R.string.pro_plan_help_text_2);
            setRichText(tv_need_help, tvHelp, R.string.talk_to_us);

            Log.d("NeedHelpPopup", "Need Help Content set successfully!");

        } catch (Exception e) {
            Log.e("NeedHelpPopup", "Exception in setNeedHelpContent()", e);
        }
    }

    private void setRichText(TextView tv, String remoteValue, int fallbackRes) {
        if (CommonMethods.isValidJson(remoteValue)) {
            tv.setText(StyledTextBuilder.fromJson(this, remoteValue));
        } else {
            tv.setText(getString(fallbackRes));
        }
    }

    private void shareAppContent() {
        try {
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            String remoteJson = MyApplication.getFirebaseRemoteConfig()
                    .getString("subscription_share_config");
            Log.d(TAG, "shareAppContent=remoteJson: " + remoteJson);

            String shareMessage = "";
            String deepLink = "https://arivihan.com/home?openSubscription=true";

            if (StringUtils.isNotEmpty(remoteJson)) {
                try {
                    JSONObject jsonObject = new JSONObject(remoteJson.trim());
                    String messageKey = "hi".equals(lang) ? "message_hi" : "message_en";

                    String rawMessage = jsonObject.optString(messageKey, "");
                    if (StringUtils.isNotEmpty(rawMessage)) {
                        shareMessage = htmlToWhatsAppMarkdown(rawMessage);
                    }

                    String configDeepLink = jsonObject.optString("deep_link", "");
                    if (StringUtils.isNotEmpty(configDeepLink)) {
                        deepLink = configDeepLink;
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "shareAppContent: Failed to parse subscription_share_config", e);
                }
            }

            if (TextUtils.isEmpty(shareMessage)) {
                String fallback = "hi".equals(lang)
                        ? "मैं अरिविहान से पढ़ाई कर रहा हूँ। <b>लाइव क्लासेस</b>, <b>टॉपर्स के नोट्स</b>, <b>रिकॉर्डेड लेक्चर्स</b> और <b>डाउट सॉल्विंग</b> सब एक ही जगह मिल रहा है। तुम भी <b>जॉइन करो</b> और अपनी पढ़ाई स्टार्ट करो 👇"
                        : "Main Arivihan se padhai kar raha hoon. <b>Live classes</b>, <b>toppers ke notes</b>, <b>recorded lectures</b> aur <b>doubt solving</b> sab ek hi jagah mil raha hai. Tum bhi <b>join karo</b> aur apni padhai start karo 👇";
                shareMessage = htmlToWhatsAppMarkdown(fallback);
            }

            String shareText = shareMessage + "\n" + deepLink;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share via"));

        } catch (Exception e) {
            Log.e(TAG, "shareAppContent: Error while sharing", e);
            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private String htmlToWhatsAppMarkdown(String html) {
        return html
                .replaceAll("(?i)<b>", "*")       // <b> → WhatsApp bold
                .replaceAll("(?i)</b>", "*")       // </b> → WhatsApp bold
                .replaceAll("(?i)<br\\s*/?>", "\n") // <br> → newline
                .replaceAll("<[^>]+>", "")          // strip any other HTML tags
                .trim();
    }


}
