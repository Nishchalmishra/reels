package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.BUY_SUBSCRIPTION_V2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CAMPAIGN_SUBSCRIPTION_DATA;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_DASHBOARD_VIDEO;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SUBSCRIPTION_V2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.V2_BUY_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.V2_GET_SUBSCRIPTION;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import com.facebook.shimmer.ShimmerFrameLayout;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
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

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.RazorpayPaymentGatewayActivity;
import arivihan.technologies.doubtbuzzter2.ReelsActivity;
import arivihan.technologies.doubtbuzzter2.adapter.CampaignFeatureAdapter;
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
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.BuySubscriptionModel;
import arivihan.technologies.doubtbuzzter2.model.CampaignSubscriptionDto;
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
import arivihan.technologies.doubtbuzzter2.model.SubscriptionRenewalReward;
import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.model.VideoResponse;
import arivihan.technologies.doubtbuzzter2.enums.batch.videos.BatchVideoType;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CampaignSubscriptionActivity extends BaseActivity implements ToppersAdapter.OnTopperClickListener,
        Player.Listener, CampaignSubscriptionPlanAdapter.OnSubscriptionCardClickListener
        , ContactSectionBottomSheetDialog.ContactSectionBottomSheetDialogListner, EdgeToEdgeHelper.SelfManagedInsets {

    public static final String BUNDLE_SUBSCRIPTION_DATA = "subscription_lock_data";
    private String navigationFrom;
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
    RecyclerView featuresRv;
    RecyclerView subscriptionCardsRv;
    CampaignSubscriptionPlanAdapter subscriptionCardAdapter;
    CampaignFeatureAdapter subscriptionFeatureAdapter;
    ArrayList<String> subscriptionFeatures;
    TextView selectedCostPrice;
    TextView selectedDiscountPrice;
    TextView selectedSubscriptionTitle;
    ConstraintLayout joinNow;
    ToppersAdapter toppersAdapter;
    RecyclerView toppersRv;
    ArrayList<ToppersHomeScreenModel> toppersList = new ArrayList<>();
    TextView discountPercentage, renewDiscountText, campaignTitle;
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

    private LinearLayout subscriptionPlansLayout;
    private MaterialCardView cvApplyCouponCard;
    EditText couponCodeEditText;
    String selectedPlanId, couponPlaceholderText;
    BuySubscriptionModel buySubscriptionModel;
    TextView tv_coupon_code_applied, tv_discount_layout_text;
    private MaterialCardView cv_subscription_plan_pdf;
    private CardView cv_subscription_pdf;
    String subscriptionBatchInfoPdf;
    private static final String TAG = CampaignSubscriptionActivity.class.getSimpleName();
    private boolean isCouponProcessing = false;


    private MaterialCardView mcv_contact_us;

    private boolean isFirstResume = true;

    NestedScrollView scrollView;
    private ContactSectionBottomSheetDialog bottomSheetDialog;

    private boolean isBottomSheetOpened = false;

    private RedirectionSource redirectionSource;
    private String analyticsSource;

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
    private TextView tv_need_help, text_2_need_help, text_1_need_help,tv_limited_time_offer;
    private FrameLayout fl_limited_time_offer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign_subscription);

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });

        EdgeToEdgeHelper.padForSystemBars(findViewById(R.id.main));
        initializeViews();
        setSubscriptionFeatures();

        Object sourceExtra = getIntent().getSerializableExtra(SubscriptionNavigationHelper.EXTRA_SOURCE);
        redirectionSource = null;
        if (sourceExtra instanceof RedirectionSource) {
            redirectionSource = (RedirectionSource) sourceExtra;
            navigationFrom = redirectionSource.getFirebaseName();
        } else if (sourceExtra instanceof String) {
            navigationFrom = (String) sourceExtra;
        }

        if (StringUtils.isEmpty(navigationFrom) && getIntent().getData() != null) {
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
            analyticsSource = AnalyticsEvent.NEET_SUBSCRIPTION_SCREEN.name();
        }

        if (savedInstanceState == null) {
            ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.NEET_SUBSCRIPTION_SCREEN.name(), analyticsSource);

            Map<String, String> subNavParams = new HashMap<>();
            subNavParams.put("source", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.sub_screen_opened.getActionName(), subNavParams);
        }

        boolean showPopup = getIntent().getBooleanExtra(
                PreferenceKeys.SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN.getKey(), false);

        Log.d("CampaignSubscription", "Intent extra show_course_information_pop_up_on_home_screen: " + showPopup);

        if (getIntent().getBooleanExtra(PreferenceKeys.SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN.getKey(), false)) {
            MyApplication.saveData(PreferenceKeys.SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN.getKey(), true);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.wclm_sheet_join_karein_btn_click.getActionName(), withNavFrom(null));
        }
        intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastStringForAction);
        subscriptionModel = new SubscriptionModel();
        getPlanData(null);
        setSubscriptionPlanAdapter();
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
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.on_back_btn_click_or_swipe.getActionName(), withNavFrom(null));
    }

    private void triggerBottomSheet() {
        if (campaignSubscriptionScreenContactDto != null && MyApplication.isNetworkAvailable()) {
            ContactSectionBottomSheetDialog existingSheet =
                    (ContactSectionBottomSheetDialog) getSupportFragmentManager()
                            .findFragmentByTag("ContactBottomSheet");

            if (existingSheet != null && existingSheet.isAdded()) {
                return;
            }
            stopVideoForBottomSheet();
            bottomSheetDialog = ContactSectionBottomSheetDialog.newInstance(campaignSubscriptionScreenContactDto);
//                    new ContactSectionBottomSheetDialog(
//                            campaignSubscriptionScreenContactDto,
//                            this
//                    );
            bottomSheetDialog.show(
                    getSupportFragmentManager(),
                    "ContactBottomSheet"
            );
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.contact_us_popup_view.getActionName(), withNavFrom(null));
            isBottomSheetOpened = true;
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
            if (dto != null) {
                CommonDialogs.showLockPopupDialog(this, dto);

                Map<String, String> params = new HashMap<>();
                params.put("title", dto.getTitle() != null ? dto.getTitle() : "");
                params.put("subTitle", dto.getSubTitle() != null ? dto.getSubTitle() : "");
                params.put("navigation_from", navigationFrom);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_screen_lock_pop_up_viewed.getActionName(), params);
            }
        } catch (Exception ignored) {
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
        Log.d(TAG, "getScreenData: "+metaData);
        try {
            if (metaData != null) {
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

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            String remoteKey = "Limited_time_offer"+"_"+lang;
            String remoteValue = FirebaseRemoteConfig.getInstance().getString(remoteKey);
            if (!StringUtils.isEmpty(remoteValue)) {
                Log.d("LimitedTimeOffer", "Showing limited time offer");
                fl_limited_time_offer.setVisibility(View.VISIBLE);
                tv_limited_time_offer.setText(StyledTextBuilder.fromJson(this, remoteValue));
            } else {
                Log.d("LimitedTimeOffer", "Hiding limited time offer - remoteKey is empty");

                fl_limited_time_offer.setVisibility(View.GONE);
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

    /**
     * Updates the subscription features displayed in the UI.
     *
     * @param featuresList List of subscription features fetched from the API.
     */


    private void updateSubscriptionFeaturesFromApi(ArrayList<String> featuresList) {
        Log.d("featured log", "updateSubscriptionFeaturesFromApi: ");
        subscriptionFeatures.clear();

        if (featuresList != null) {
            for (String feature : featuresList) {
                subscriptionFeatures.add(feature.trim());
            }
        }

        subscriptionFeatureAdapter.notifyDataSetChanged();
    }

    /**
     * Updates the campaign title displayed in the UI.
     *
     * @param screenText HTML-formatted text fetched from the API.
     */
    private void updateCampaignTitleFromApi(String screenText) {
        Spanned formattedText;
        if (screenText == null) screenText = "";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            formattedText = Html.fromHtml(screenText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            formattedText = Html.fromHtml(screenText);
        }

        campaignTitle.setText(formattedText);
    }


    private void setSubscriptionPlanAdapter() {
        subscriptionPlans = new ArrayList<>();
        subscriptionCardAdapter = new CampaignSubscriptionPlanAdapter(this, subscriptionPlans, this);
        subscriptionCardsRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        subscriptionCardsRv.setAdapter(subscriptionCardAdapter);
    }

    private void initializeViews() {
        spinnerVideoDetails = findViewById(R.id.spinnerVideoDetails);
        tv_limited_time_offer =findViewById(R.id.tv_limited_time_offer);
        fl_limited_time_offer=findViewById(R.id.fl_limited_time_offer);
        subscriptionPlansLayout = findViewById(R.id.subscription_plans_layout);
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
        cv_subscription_plan_pdf = findViewById(R.id.cv_click_here);
        cv_subscription_pdf = findViewById(R.id.cv_subscription_pdf);
        cv_subscription_plan_pdf.setOnClickListener(view -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.batch_ki_jankari_click.getActionName(), withNavFrom(null));
            MyApplication.openPdfFromUrl(this,
                    subscriptionBatchInfoPdf);
        });

        NestedScrollView scrollView = findViewById(R.id.scroll_view);

        floatingButton = findViewById(R.id.floating_call_button_container);

        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY,
                                       int oldScrollX, int oldScrollY) {

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
        featuresRv = findViewById(R.id.feature_rv);
        subscriptionCardsRv = findViewById(R.id.subscription_cards_rv);
        selectedCostPrice = findViewById(R.id.selected_cost_price);
        selectedDiscountPrice = findViewById(R.id.selected_discount_price);
        selectedSubscriptionTitle = findViewById(R.id.selected_subscription_title);
        joinNow = findViewById(R.id.join_campaign_button);
        TextView enrollButtonText = joinNow.findViewById(R.id.join_text);
        ImageView enrollButtonIcon = joinNow.findViewById(R.id.join_icon);

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
        if (!MyApplication.isCompetitiveProfile()) {
            joinNow.setBackgroundResource(enrollBackgrounds[0]);
            startButtonAnimation(joinNow, enrollButtonText, enrollButtonIcon, enrollTexts, enrollIcons, enrollBackgrounds, 2000);
        } else {
            joinNow.setBackgroundResource(enrollBackgrounds[1]);
        }
        joinNow.setEnabled(false);
        campaignTitle = findViewById(R.id.unnati_titles);
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
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void checkCouponCodeValidity() {
        dataLoaded = false;
        Map<String, Object> params = new HashMap<>();
        params.put("planId", selectedPlanId);
        params.put("couponCode", couponId);
        params.put("showDayPlans", true);
        Log.d("checkCouponCodeValidity", "checkCouponCodeValidity: " + params.toString());
        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(V2_BUY_SUBSCRIPTION, params), null,
                response -> {
                    try {
                        Gson gson = new Gson();
                        Log.d("response", "checkCocheckCouponCodeValidityuponCodeValidity: " + response);
                        dataLoaded = true;
                        TypeToken<BuySubscriptionModel> token = new TypeToken<>() {
                        };
                        buySubscriptionModel = gson.fromJson(response.toString(), token.getType());
                        if (StringUtils.isEmpty(couponId) || buySubscriptionModel.getPricingBreakdown().isCouponCodeValid()) {
                            updateFinalPriceAfterCouponCodeApplied(buySubscriptionModel.getPricingBreakdown());
                            setUpDefaultDiscountLayoutAfterCouponApplied(buySubscriptionModel.getPricingBreakdown());
                        } else {
                            couponId = "";
                            Toast.makeText(CampaignSubscriptionActivity.this, getString(R.string.coupon_code_is_invalid), Toast.LENGTH_LONG).show();
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
        selectedDiscountPrice.setText(String.format(getString(R.string.price_format), subscriptionPlan.getTotal()));
        selectedSubscription.setFinalPrice(subscriptionPlan.getTotal());
    }

    private void setUpDefaultDiscountLayoutAfterCouponApplied(PriceBreakdownModel pricingBreakdown) {
        tv_coupon_code_applied.setVisibility(View.VISIBLE);
        String couponCode = couponPlaceholderText != null ? couponPlaceholderText : pricingBreakdown.getCouponCode();
        String message = getString(R.string.Discount_ARIVIHAN_Coupon_code_applied, pricingBreakdown.getCouponCodeValue() + "%", couponCode);
        discountLayout.setVisibility(View.VISIBLE);

        SpannableString spannable = new SpannableString(message);

        int start = message.indexOf(couponCode);
        int end = start + couponCode.length();

        spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_discount_layout_text.setText(spannable);
    }

    private void openPlayer() {
        if (StringUtils.isNotEmpty(videoUrl)) {
            Intent intent = new Intent(CampaignSubscriptionActivity.this, ExoPlayerActivity.class);
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
            subscriptionRenewalReward = subscriptionModel.getSubscriptionRenewalReward();
            if (subscriptionRenewalReward.getCouponCodeText() != null
                    && !subscriptionRenewalReward.getCouponCodeText().isEmpty()) {
                String couponCode = subscriptionRenewalReward.getCouponCode();
                String discountPercent = subscriptionRenewalReward.getDiscountPercentage() + "%";

                String fullText = getString(R.string.Discount_ARIVIHAN_Coupon_code_applied, discountPercent, couponCode);

                SpannableString spannable = new SpannableString(fullText);

                int start = fullText.indexOf(couponCode);
                int end = start + ((couponCode != null) ? couponCode.length() : 0);

                spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_discount_layout_text.setText(spannable);
                discountLayout.setVisibility(View.VISIBLE);
                tv_coupon_code_applied.setVisibility(View.VISIBLE);
                couponCodeEditText.setText(couponCode);

                couponId = couponCode;

                int discount = subscriptionRenewalReward.getDiscountPercentage();
                int finalPrice = Integer.parseInt(subscriptionPlan.getFinalPrice());

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
    }

//    private void setUpDefaultDiscountLayout(SubscriptionPlans subscriptionPlan){
//
//        selectedDiscountPrice.setText(String.format(getString(R.string.price_format), subscriptionPlan.getFinalPrice()));
//        int discountAmount = Integer.parseInt(subscriptionPlan.getCostPrice()) - Integer.parseInt(subscriptionPlan.getFinalPrice());
//        int discount = (discountAmount * 100) / (Integer.parseInt(subscriptionPlan.getCostPrice()));
//        discountPercentage.setText(String.valueOf(discount));
//        discountLayout.setVisibility(View.VISIBLE);
//    }

    private void setSubscriptionFeatures() {
        subscriptionFeatures = new ArrayList<>();
        subscriptionFeatureAdapter = new CampaignFeatureAdapter(subscriptionFeatures, this);
        featuresRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        featuresRv.setAdapter(subscriptionFeatureAdapter);
    }

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
        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(V2_GET_SUBSCRIPTION, params), null,
                response -> {
                    TypeToken<SubscriptionModel> token = new TypeToken<SubscriptionModel>() {
                    };
                    Log.d(TAG, "getPlanData: "+response);
                    subscriptionModel = gson.fromJson(response.toString(), token.getType());
                    dataLoaded = true;
                    Log.d(TAG, "getPlanData: "+subscriptionModel.getPlanMetaData());
                    updateSubscriptionFeaturesFromApi(subscriptionModel.getFeatures());
                    updateCampaignTitleFromApi(subscriptionModel.getPlanMetaData() != null ? subscriptionModel.getPlanMetaData().getTitle() : null);
                    for (SubscriptionPlans plan : subscriptionModel.getPlans()) {
                        if (plan.isSelected()) {
                            Log.d("priceIssue", "getPlanData: " + plan);
                            updateFinalPrice(plan);
                            selectedSubscription = plan;
                            selectedPlanId = selectedSubscription.getPlanId();
                        }
                    }
                    if (subscriptionModel.isToShowSubscriptionPlanList()) {
                        subscriptionPlansLayout.setVisibility(View.VISIBLE);
                    } else subscriptionPlansLayout.setVisibility(View.GONE);
                    dataShimmer.setVisibility(View.GONE);
                    dataShimmer.stopShimmer();
                    dataLayout.setVisibility(View.VISIBLE);
                    if(subscriptionModel.getPlanMetaData()!=null) {
                        Log.d(TAG, "getPlanData: " + subscriptionModel.getPlanMetaData());
                        getScreenData(subscriptionModel.getPlanMetaData());
                    }
                    if (subscriptionModel.getSubscriptionRenewalReward() != null) {
//                        getCouponAppliedPlanData(subscriptionModel.getOverview().getPlanId(),
//                                subscriptionRenewalReward);
                    } else {
                        discountLayout.setVisibility(View.GONE);
                    }

                    String incomingCoupon = getIntent().getStringExtra("couponCode");
                    Log.d("incomingCoupon", "From Intent Extra: " + incomingCoupon);

                    if (incomingCoupon == null) {
                        Uri data = getIntent().getData();
                        Log.d("incomingCoupon", "Full DeepLink URI: " + data);

                        if (data != null) {
                            Log.d("incomingCoupon", "Query: " + data.getQuery());
                            incomingCoupon = data.getQueryParameter("couponCode");
                            Log.d("incomingCoupon", "Extracted couponCode: " + incomingCoupon);
                        }
                    }

                    if (MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_DETECTED.getKey())) {
                        Log.d(TAG, "Referral flow activated.");
                        couponId = MyApplication.getStringDataFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_CODE.getKey());

                        couponPlaceholderText = MyApplication.getFirebaseRemoteConfig()
                                .getString("subscription_activity_coupon_code_placeholder_text");
                        checkForAutoAppliedCoupon(couponId, couponPlaceholderText);

                    } else if (StringUtils.isNotEmpty(incomingCoupon)) {
                        couponId = incomingCoupon;
                        checkForAutoAppliedCoupon(couponId, incomingCoupon);
                    } else {
                        Log.d(TAG, "No referral/intent coupon detected. Using default flow.");
                    }


                    finalPriceLayout.setVisibility(View.VISIBLE);
                    joinNow.setEnabled(true);
                    bottom_layout.setVisibility(View.VISIBLE);
                }, new VolleyGlobalErrorListener(this) {

            @Override
            public void postError(VolleyError error) {
                Toast.makeText(CampaignSubscriptionActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        jsonObjectRequest.setTag("PLAN_DATA_REQUEST");
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void checkForAutoAppliedCoupon(String couponId, String couponName) {
        try {
            long timeInMillis = MyApplication.getFirebaseRemoteConfig().getLong("subscription_auto_applied_timer");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                couponCodeEditText.getText().clear();
                couponCodeEditText.getText().append(couponName);
                checkCouponCodeValidity();
            }, timeInMillis);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    public void getCouponAppliedPlanData(String planId, SubscriptionRenewalReward subscriptionRenewalReward) {
//        dataLoaded = false;
//        Map<String, Object> params = new HashMap<>();
//        params.put("planId", planId);
//        params.put("couponCode", subscriptionRenewalReward.getCouponCode());
//        params.put("showDayPlans", true);
//        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
//                CommonMethods.getUrlParam(BUY_SUBSCRIPTION_V2, params), null,
//                response -> {
//                    dataLoaded = true;
//                    TypeToken<BuySubscriptionModel> token = new TypeToken<>() {
//                    };
//                    BuySubscriptionModel buySubscriptionModel = gson.fromJson(response.toString(), token.getType());
//                    subscriptionModel.getOverview().setCostPrice(buySubscriptionModel.getPricingBreakdown().getSubscriptionPrice());
//                    subscriptionModel.getOverview().setFinalPrice(buySubscriptionModel.getPricingBreakdown().getTotal());
//                    if (subscriptionRenewalReward.getCouponCodeText() != null && !subscriptionRenewalReward.getCouponCodeText().isEmpty()) {
//                        discount_text.setText(HtmlCompat.fromHtml(subscriptionRenewalReward.getCouponCodeText(), HtmlCompat.FROM_HTML_MODE_LEGACY));
//                        discount_layout.setVisibility(View.VISIBLE);
//                        coupon_code.setText(subscriptionRenewalReward.getCouponCode());
//                        updateFinalPriceAfterCouponCodeApplied(buySubscriptionModel);
//                    } else {
//                        discount_layout.setVisibility(View.GONE);
//                    }
//                    changePlanLevelData();
//
//                }, new VolleyGlobalErrorListener(this) {
//
//            @Override
//            public void postError(VolleyError error) {
//                //Toast.makeText(BuySubscriptionActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//        jsonObjectRequest.setTag("PLAN_DATA_REQUEST");
//        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
//    }

    private void updateSubscriptionPlans(ArrayList<SubscriptionPlans> plans) {
        subscriptionPlans.clear();
        subscriptionPlans.addAll(plans);

        if (!CollectionUtils.isEmpty(subscriptionPlans)) {
            if (subscriptionCardAdapter != null) {
                subscriptionCardAdapter.notifyDataSetChanged();
            }
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

        Gson gson = new Gson();
        MyJsonObjectRequest toppersRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_DASHBOARD_VIDEO, toppersRequestParams), null,
                response -> {
                    try {
                        JSONArray toppersArray = response.getJSONArray("data");
                        TypeToken<List<VideoResponse>> token = new TypeToken<List<VideoResponse>>() {};
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
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.d("toppersExtraction", "API error: " + error.getMessage());
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
        joinNow.setOnClickListener(v -> {

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.bottom_join_karein_btn_click.getActionName(), withNavFrom(null));

            try {
                if (isSubscriptionAmountIsZero()) {
                    if (CommonMethods.isOnline(this)) {
                        buyNowPreCall();
                    } else {
                        CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.error_no_internet_connection));
                    }
                } else {

                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.subscription_payment_option_dialog_layout);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);

                    dialog.findViewById(R.id.close_btn).setOnClickListener(dv -> {

                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.dialog_close_btn_click.getActionName(), withNavFrom(null));

                        dialog.dismiss();
                    });

                    dialog.findViewById(R.id.pay_with_qr_btn).setOnClickListener(dv -> {
                        isPayingThroughQRCode = true;
                        dialog.dismiss();
                        try {
                            buyNowPreCall();
                            MyApplication.saveData(KEY_PAYMENT_TYPE, PAYMENT_QR);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    });
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
            int amount = Integer.parseInt(selectedSubscription.getFinalPrice());
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

        Map<String, Object> map = new HashMap<>();
        SubscriptionPreModel subscriptionPreModel = new SubscriptionPreModel();

        subscriptionPreModel.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        Log.d("BUY_NOW_FLOW", "UserId: " + subscriptionPreModel.getUserId());

        // Coupon Handling
        if (StringUtils.isNoneEmpty(couponId)) {
            Log.d("BUY_NOW_FLOW", "Coupon Applied: " + couponId);

            CouponDto couponDto = new CouponDto();
            couponDto.setCode(couponId);
            subscriptionPreModel.setCoupon(couponDto);
        } else {
            Log.d("BUY_NOW_FLOW", "No Coupon Applied");
        }

        subscriptionPreModel.setSubscriptionPlanId(selectedSubscription.getPlanId());
        subscriptionPreModel.setOrderId(getAlphaNumericString(8));
        subscriptionPreModel.setTotalAmount(Long.parseLong(selectedSubscription.getFinalPrice()));
        subscriptionPreModel.setRedirectionFrom(analyticsSource != null ? analyticsSource : "UNKNOWN");
        Log.d("SubscriptionSourceTrack", "Source: " + subscriptionPreModel.getRedirectionFrom() + " | Plan: " + selectedSubscription.getPlanId() + " (CampaignSubscriptionActivity)");

        gson = new Gson();

        if (BuildConfig.DEBUG) {
            Log.d("BUY_NOW_FLOW", "Generated OrderId: " + subscriptionPreModel.getOrderId());
            Log.d("BUY_NOW_FLOW", "PlanId: " + selectedSubscription.getPlanId());
            Log.d("BUY_NOW_FLOW", "Total Amount: " + subscriptionPreModel.getTotalAmount());
            Log.d("BUY_NOW_FLOW", "PreCall Body: " + gson.toJson(subscriptionPreModel));
        }

        Log.d("BUY_NOW_FLOW", "Sending Subscription Pre API Request...");

        MyJsonObjectRequest myJsonObjectRequest2 =
                new MyJsonObjectRequest(Request.Method.POST,
                        CommonMethods.getUrlParam(USER_SUBSCRIPTION, map),
                        new JSONObject(gson.toJson(subscriptionPreModel)),
                        response -> {
                            if (isFinishing() || isDestroyed()) return;
                            Log.d("BUY_NOW_FLOW", "API Response Received: " + response);

                            TypeToken<UserSubscriptionDto> token =
                                    new TypeToken<UserSubscriptionDto>() {
                                    };

                            try {
                                UserSubscriptionDto userSubscriptionDto =
                                        gson.fromJson(response.getJSONObject("data").toString(), token.getType());

                                Log.d("BUY_NOW_FLOW", "Parsed UserSubscriptionDto Successfully");
                                Log.d("BUY_NOW_FLOW", "OrderId from Response: " + userSubscriptionDto.getOrderId());

                                // Free Subscription Flow
                                if (isSubscriptionAmountIsZero()) {

                                    Log.d("BUY_NOW_FLOW", "Subscription Amount is ZERO. Calling buyNowPostCall...");

                                    buyNowPostCall(getAlphaNumericString(8), userSubscriptionDto);

                                } else {

                                    Log.d("BUY_NOW_FLOW", "Paid Subscription Flow Started...");

                                    if (isPayingThroughQRCode) {

                                        Log.d("BUY_NOW_FLOW", "Payment Mode: QR Code");

                                        Intent intent = new Intent(
                                                CampaignSubscriptionActivity.this,
                                                QRCodePaymentActivity.class
                                        );

                                        intent.putExtra("orderId", userSubscriptionDto.getOrderId());
                                        intent.putExtra("subscriptionPlanId", selectedSubscription.getPlanId());
                                        intent.putExtra("transactionId", "");
                                        intent.putExtra("costPrice", selectedSubscription.getCostPrice());
                                        intent.putExtra("planName", selectedSubscription.getName());
                                        intent.putExtra("userSubscriptionDto", userSubscriptionDto);
                                        intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, redirectionSource);

                                        intent.putExtra("paymentAmount",
                                                Integer.parseInt(selectedSubscription.getFinalPrice()));

                                        Log.d("BUY_NOW_FLOW", "Launching QRCodePaymentActivity...");

                                        logFirebaseEventsForPaymentTransaction(
                                                "QRCodePaymentActivity",
                                                CommonMethods.convertToStringMap(
                                                        CommonMethods.getIntentParams(intent))
                                        );

                                        qrPaymentLauncher.launch(intent);
                                    } else {
                                        Log.d("BUY_NOW_FLOW", "Payment Mode: Direct Payment Gateway");
                                        Log.d("BUY_NOW_FLOW", "Calling doPayment()...");
                                        doPayment(
                                                Integer.parseInt(selectedSubscription.getFinalPrice()),
                                                userSubscriptionDto.getOrderId(),
                                                userSubscriptionDto
                                        );
                                    }
                                }

                            } catch (Exception e) {
                                Log.e("BUY_NOW_FLOW", "Error while parsing response: " + e.getMessage(), e);
                                Toast.makeText(this,
                                        getString(R.string.server_error),
                                        Toast.LENGTH_SHORT).show();
                            }

                        },
                        new VolleyGlobalErrorListener(this) {

                            @Override
                            public void postError(VolleyError error) {

                                Log.e("BUY_NOW_FLOW", "API Error Occurred!");

                                if (error.getMessage() != null) {
                                    Log.e("BUY_NOW_FLOW", "Error Message: " + error.getMessage());
                                }

                                Toast.makeText(
                                        CampaignSubscriptionActivity.this,
                                        "Payment Failed: " + error.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

        Log.d("BUY_NOW_FLOW", "Adding request to Volley Queue...");
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
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

        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.PUT,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), new JSONObject(gson.toJson(subscriptionPostModel)),
                response -> {
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

            if (bottomSheetDialog == null && !isBottomSheetOpened) {

                Log.d("CampaignSubscription_BOTTOMSHEET",
                        "BottomSheet state → NULL instance & not opened yet. Triggering now.");

                triggerBottomSheet();

            } else {

                Log.d("CampaignSubscription_BOTTOMSHEET",
                        "BottomSheet state → Already exists or previously opened. Skipping trigger.");
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

    private void startButtonAnimation(ConstraintLayout buttonLayout, TextView txt, ImageView icon,
                                      String[] texts, Integer[] icons, Integer[] backgrounds, long interval) {

        Handler handler = new Handler(Looper.getMainLooper());
        final int[] index = {1};
        Runnable r = new Runnable() {
            @Override
            public void run() {
                txt.animate()
                        .translationY(-txt.getHeight())
                        .setDuration(200)
                        .start();
                if (icon != null) {
                    icon.animate()
                            .translationY(-icon.getHeight())
                            .setDuration(200)
                            .start();
                }

                // Change background after slide-out
                handler.postDelayed(() -> {
                    // Change text and icon
                    txt.setText(texts[index[0]]);
                    txt.setTranslationY(txt.getHeight()); // reset position

                    if (icon != null) {
                        icon.setImageResource(icons[index[0]]);
                        icon.setTranslationY(icon.getHeight()); // reset position
                    }

                    // Animate background change
                    Drawable newBg = ContextCompat.getDrawable(buttonLayout.getContext(), backgrounds[index[0]]);
                    Drawable oldBg = buttonLayout.getBackground();

                    if (oldBg == null) {
                        buttonLayout.setBackground(newBg);
                    } else if (newBg != null) {
                        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{oldBg, newBg});
                        buttonLayout.setBackground(transitionDrawable);
                        transitionDrawable.startTransition(200); // smooth transition
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

                }, 200); // wait for slide-out animation
            }
        };

        handler.postDelayed(r, interval);
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
            joinNow.performClick();
        } else if (MyApplication.getStringData(KEY_PAYMENT_TYPE).equals(PAYMENT_QR)) {
            isPayingThroughQRCode = true;
            try {
                buyNowPreCall();
            } catch (JSONException e) {
                throw new RuntimeException(e);
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
            Log.d("NeedHelpPopup", "Remote JSON received: " + remoteJson);

            if (remoteJson == null || remoteJson.trim().isEmpty()) {
                Log.e("NeedHelpPopup", "Remote JSON is EMPTY!");
                return;
            }

            JSONObject root = new JSONObject(remoteJson);
            Log.d("NeedHelpPopup", "Root JSON parsed successfully");

            // Direct extraction (no courseId, no language)
            String t1 = root.optString("text_1", "");
            String t2 = root.optString("text_2", "");
            String tvHelp = root.optString("tv_need_help", "");

            Log.d("NeedHelpPopup", "Extracted text_1: " + t1);
            Log.d("NeedHelpPopup", "Extracted text_2: " + t2);
            Log.d("NeedHelpPopup", "Extracted tv_need_help: " + tvHelp);

            setRichText(text_1_need_help, t1,
                    R.string.Half_yearly_ke_practice_test_kese_dein);

            setRichText(text_2_need_help, t2,
                    R.string.Our_experts_can_answer_all_your_exam_related_questions);

            setRichText(tv_need_help, tvHelp,
                    R.string.talk_to_us);

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


}
