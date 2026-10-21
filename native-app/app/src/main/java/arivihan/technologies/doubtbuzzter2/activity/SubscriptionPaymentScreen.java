package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PAYMENT_INFO_RAZORPAY;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.RAZORPAY_CHECK_QR_PAYMENT_STATUS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.RAZORPAY_CREATE_QR_CODE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.V2_GET_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
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
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.RazorpayPaymentGatewayActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.FeatureInfoVideoActivity;
import arivihan.technologies.doubtbuzzter2.adapter.RewardsTopPerformersAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ToppersAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonDialogs;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.SubscriptionFailedDialog;
import arivihan.technologies.doubtbuzzter2.customView.ContactBottomSheet;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.BranchAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.SubscriptionQrLoadHelper;
import arivihan.technologies.doubtbuzzter2.enums.SubscriptionPopupType;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.CropQRTransformation;
import arivihan.technologies.doubtbuzzter2.model.CampaignSubscriptionScreenContactDto;
import arivihan.technologies.doubtbuzzter2.model.CouponDto;
import arivihan.technologies.doubtbuzzter2.model.PaymentDetails;
import arivihan.technologies.doubtbuzzter2.model.PaymentInfo;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.RazorpayQRCreateResponse;
import arivihan.technologies.doubtbuzzter2.model.RazorpayQRPaymentResponse;
import arivihan.technologies.doubtbuzzter2.model.RewardsScreenDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionBenefitsModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlanMetaData;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPostModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPreModel;
import arivihan.technologies.doubtbuzzter2.model.TransactionType;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

/**
 * SubscriptionPaymentScreen
 * <p>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ FLOW — First visit (no cached QR)                                        │
 * │  1. fetchSubscriptionDetails()  GET plan → planId + amount               │
 * │  2. subscriptionPreCall()       POST /user-subscription → orderId + DTO  │
 * │  3. fetchQRCode()               GET Razorpay QR (orderId from step 2)    │
 * │     └─ saves image, qrId, orderId, DTO to SharedPrefs (5 keys)           │
 * │  4. Poll checkStatusOfQRPayment() every 5 s                              │
 * │  5. buyNowPostCall()            PUT /user-subscription → navigate         │
 * ├──────────────────────────────────────────────────────────────────────────┤
 * │ FLOW — Second visit (QR cached in SharedPrefs)                           │
 * │  loadQrCodeImage() restores razorpayQRCreateResponse + generatedOrderId  │
 * │  + userSubscriptionDto BEFORE subscriptionPreCall() returns.             │
 * │  subscriptionPreCall() detects the cached QR and discards its fresh DTO  │
 * │  so the original session is used in buyNowPostCall() → no "Txn not       │
 * │  found" error.                                                           │
 * ├──────────────────────────────────────────────────────────────────────────┤
 * │ RACE-CONDITION GUARD                                                     │
 * │  If the poller detects a payment while subscriptionPreCall() is still    │
 * │  in-flight (generatedOrderId = temp string), the paymentId is parked in  │
 * │  pendingPaymentId and flushed by runPostPreCall() once the pre-call       │
 * │  settles and generatedOrderId holds the real server orderId.             │
 * └──────────────────────────────────────────────────────────────────────────┘
 * <p>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ BACK-BUTTON POPUP & RETURNING-USER VIDEO                                 │
 * │                                                                          │
 * │ Condition 1 — Back pressed (any session after first launch):             │
 * │   onBackPressed() → triggerBackButtonBottomSheet()                       │
 * │   Saves KEY_BACK_POPUP_SHOWN = true in SharedPrefs.                      │
 * │   Popup buttons:                                                         │
 * │     Primary  "Join at ₹51"               → dismiss, resume video         │
 * │     Secondary "कोई समस्या है? संपर्क करें" → open board-specific WhatsApp  │
 * │     X (cross)                            → dismiss + navigate home       │
 * │                                                                          │
 * │ Condition 2 — App killed & reopened with flag set:                       │
 * │   onResume() (isFirstResume=true, KEY_BACK_POPUP_SHOWN=true)             │
 * │   → isReturningUserVideoMode = true                                      │
 * │   → applyHeaderSection() plays returningUserVideoUrl instead of normal   │
 * │   → badge images hidden while video plays and after it ends              │
 * │   → on video end: badge hidden, flags reset, Condition 1 re-armed        │
 * └──────────────────────────────────────────────────────────────────────────┘
 * <p>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ VIDEO VISIT LOGIC                                                        │
 * │                                                                          │
 * │ First visit  → poster only, player never starts.                        │
 * │               KEY_FIRST_VISIT_DONE written to SharedPrefs.              │
 * │ 2nd visit+   → poster shown, then video plays after subscription popup  │
 * │               auto-closes (deferred via isVideoStartPending flag).       │
 * │ videoUrl     → always resolved from planMetaData.getVideoUrl()           │
 * │               with subscriptionModel.getVideoUrl() as fallback.         │
 * └──────────────────────────────────────────────────────────────────────────┘
 * <p>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ AUDIO / VIDEO STATE RULES                                                │
 * │                                                                          │
 * │ • Subscription lock popup visible → video start deferred until popup     │
 * │   auto-closes (isSubscriptionPopupVisible gate + isVideoStartPending).  │
 * │ • Contact bottom sheet opens (any trigger) → pausePlayer().             │
 * │ • "Join at ₹51" primary button → dismiss sheet + resumePlayer().        │
 * │ • X close button → navigate home (player released with activity).       │
 * │ • Condition 2 returning-user video follows the same pause/resume rules. │
 * └──────────────────────────────────────────────────────────────────────────┘
 */
public class SubscriptionPaymentScreen extends AppCompatActivity
        implements Player.Listener,
        ContactBottomSheet.ContactBottomSheetListner {

    private static final String TAG = "SubscriptionPaymentScreen";
    private static final String TAG_SHARE = "ShareQR";
    private static final String TAG_QR = "QRCODEPAYMENT";
    private static final long POLL_INTERVAL = 5_000L;
    private static final int ORDER_ID_LEN = 8;

    // ── SharedPrefs keys ──────────────────────────────────────────────────────
    private static final String KEY_BACK_POPUP_SHOWN = "sub_payment_back_popup_shown";
    private static final String KEY_FIRST_VISIT_DONE = "sub_payment_first_visit_done";

    // ── Firebase Remote Config keys ───────────────────────────────────────────
    private static final String RC_BACK_POPUP_POSTER = "back_popup_poster_image_url";
    private static final String RC_BACK_POPUP_AUDIO = "back_popup_audio_url";
    private static final String RC_RETURNING_USER_VIDEO_URL = "back_popup_returning_video_url";

    // ── Back-button popup + returning-user video ──────────────────────────────
    private boolean isBackButtonBottomSheet = false;
    private boolean isReturningUserVideoMode = false;
    private String returningUserVideoUrl = "";

    // ── Subscription popup / video-start deferral ─────────────────────────────
    /**
     * Set true while the subscription lock popup is on screen.
     * Cleared (and pending video flushed) after the popup auto-closes.
     */
    private boolean isSubscriptionPopupVisible = false;

    /**
     * Set true when applyHeaderSection() wants to start the video but
     * isSubscriptionPopupVisible is still true.  Flushed by the post-popup
     * handler in triggerSubscriptionPopUp().
     */
    private boolean isVideoStartPending = false;
    // ─────────────────────────────────────────────────────────────────────────

    private ImageView qrCodeImage;
    private ImageView middleBadgeImageVideo;
    private MaterialCardView otherPaymentOption;
    private MaterialCardView shareQrBtn;
    private ImageView heroBannerImage;
    private ImageView middleBadgeImage;

    private ShimmerFrameLayout shimmerFrameLayout;
    private ShimmerFrameLayout shimmerContainer;
    private LinearLayout contentContainer;
    private TextView planCostPrice;
    private TextView planFinalPrice;
    private TextView validityText;
    private TextView featureTitleView;

    private final Gson gson = new Gson();

    private RazorpayQRCreateResponse razorpayQRCreateResponse;
    private UserSubscriptionDto userSubscriptionDto;
    private SubscriptionModel subscriptionModel;

    private String planId;
    private String generatedOrderId;
    private int resolvedAmount;
    private String couponId;

    private boolean shouldCropQR = true;
    private volatile Uri cachedShareUri = null;

    private boolean isPreCallInProgress = false;
    private String pendingPaymentId = null;

    // ── Bottom Sheet State ────────────────────────────────────────────────────
    private boolean isBottomSheetOpened = false;
    private boolean isFirstResume = true;
    private boolean isReturningFromPayment = false;

    private ContactBottomSheet bottomSheetDialog;
    private CampaignSubscriptionScreenContactDto campaignSubscriptionScreenContactDto;

    // ── Handlers / Runnables / Timer ─────────────────────────────────────────
    private Handler handler = new Handler(Looper.getMainLooper());
    private Handler imageRetryHandler;
    private Runnable imageRetryRunnable;
    private Runnable pollRunnable;
    private CountDownTimer uiCountDownTimer;

    private String lastLoadedQrUrl = null;
    private String completedVideoUrl = "";
    private String navigationFrom;
    private TextView qrSectionTitle;
    private TextView qrSectionSubtitle;

    private static final int REQUEST_CODE = 100;

    private String videoUrl;
    private String thumbnailCacheUrl;
    private long videoStartingTime;
    private boolean videoEnded;
    private boolean videoPlayed;

    private ShimmerFrameLayout imageShimmer;

    // ── Video views ───────────────────────────────────────────────────────────
    private PlayerView videoPlayer;
    private ImageView thumbnail;
    private ImageView volume;
    private ProgressBar spinnerVideoDetails;

    // ── Player ────────────────────────────────────────────────────────────────
    private ExoPlayer player;
    private Handler handlerPlayer;
    private Runnable runnablePlayer;
    private boolean wasPlayerPlaying = false;
    private boolean hasShownBackPopup = false;
    private RedirectionSource redirectionSource;
    private String analyticsSource;
    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_screen_layout);
        EdgeToEdgeHelper.padForSystemBars(findViewById(R.id.payment_root));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!hasShownBackPopup && campaignSubscriptionScreenContactDto != null) {
                    hasShownBackPopup = true;          // one-shot: never re-arm
                    isBottomSheetOpened = true;
                    isBackButtonBottomSheet = true;
                    MyApplication.saveData(KEY_BACK_POPUP_SHOWN, true);
                    triggerBackButtonBottomSheet();
                } else {
                    finish();               // 2nd back → actually go back
                }
            }
        });

        Object sourceExtra = getIntent().getSerializableExtra(SubscriptionNavigationHelper.EXTRA_SOURCE);
        redirectionSource = null;
        if (sourceExtra instanceof RedirectionSource) {
            redirectionSource = (RedirectionSource) sourceExtra;
            navigationFrom = redirectionSource.getFirebaseName();
        } else if (sourceExtra instanceof String) {
            navigationFrom = (String) sourceExtra;
        }

        if (StringUtils.isEmpty(navigationFrom) && getIntent().getData() != null)
            navigationFrom = "deeplink";

        if (StringUtils.isEmpty(navigationFrom))
            navigationFrom = "unknown";

        if (redirectionSource != null) {
            analyticsSource = redirectionSource.getFormattedValue();
        } else if (sourceExtra instanceof String) {
            analyticsSource = "REDIRECTED_FROM_" + ((String) sourceExtra).toUpperCase();
        } else {
            analyticsSource = AnalyticsEvent.SUBSCRIPTION_PLUS_PLAN_SCREEN.name();
        }

        if (savedInstanceState == null) {
            Log.d(TAG, "Screen visited: " + AnalyticsEvent.SUBSCRIPTION_PLUS_PLAN_SCREEN.name() + " | contentType: " + analyticsSource + " | redirectionSource: " + (redirectionSource != null ? redirectionSource.name() : "null") + " | stringSource: " + (sourceExtra instanceof String ? sourceExtra : "null"));
            ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.SUBSCRIPTION_PLUS_PLAN_SCREEN.name(), analyticsSource);

            Map<String, String> subNavParams = new HashMap<>();
            subNavParams.put("source", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.sub_screen_opened.getActionName(), subNavParams);
        }

        couponId = getIntent().getStringExtra(PreferenceKeys.COUPON_ID.getKey());

        bindViews();
        setupClickListeners();

        pollRunnable = new Runnable() {
            @Override
            public void run() {
                checkStatusOfQRPayment();
                handler.postDelayed(this, POLL_INTERVAL);
            }
        };

        clearQrCodeIfExpired();
        fetchSubscriptionDetails();
    }

    private void setFailedBottomPopup(String imageUrl) {
        SubscriptionFailedDialog dialog = new SubscriptionFailedDialog(this, imageUrl);
        dialog.setPaymentActionListener(
                new SubscriptionFailedDialog.PaymentActionListener() {
                    @Override
                    public void onGooglePayClick() {
                    }

                    @Override
                    public void onPhonePeClick() {
                    }

                    @Override
                    public void onShareQrClick() {
                    }

                    @Override
                    public void onOtherPaymentClick() {
                    }

                    @Override
                    public void onHelpContactClick() {
                    }
                });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isFirstResume) {
            Log.d(TAG, "onResume() → First launch resume.");
            isFirstResume = false;

            if (MyApplication.getBoolData(KEY_BACK_POPUP_SHOWN)) {
                isReturningUserVideoMode = true;
                isBottomSheetOpened = false;
                Log.d(TAG, "onResume() → Condition 2: returning-user video mode enabled.");
            }

        } else {
            Log.d(TAG, "onResume() → Subsequent resume. Evaluating BottomSheet state.");
            if (isReturningFromPayment) {
                isReturningFromPayment = false;
                if (bottomSheetDialog == null && !isBottomSheetOpened) {
                    Log.d(TAG, "BottomSheet → Returning from payment. Triggering now.");
                    triggerBottomSheet();
                } else {
                    Log.d(TAG, "BottomSheet → Already exists or previously opened. Skipping.");
                }
            } else {
                Log.d(TAG, "BottomSheet → Not returning from payment. Skipping.");
            }
        }

        loadQrCodeImage();
        handler.removeCallbacks(pollRunnable);
        handler.post(pollRunnable);

        if (wasPlayerPlaying
                && !isFirstResume
                && !isReturningFromPayment
                && !isSubscriptionPopupVisible
                && !isBottomSheetOpened) {
            resumePlayer();
            wasPlayerPlaying = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(pollRunnable);
        if (handlerPlayer != null) handlerPlayer.removeCallbacks(runnablePlayer);

        wasPlayerPlaying = player != null && player.isPlaying();
        pausePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollRunnable);
        if (handlerPlayer != null) handlerPlayer.removeCallbacks(runnablePlayer);
        if (imageRetryHandler != null && imageRetryRunnable != null)
            imageRetryHandler.removeCallbacks(imageRetryRunnable);
        if (uiCountDownTimer != null)
            uiCountDownTimer.cancel();
    }

    private void bindViews() {
        planCostPrice = findViewById(R.id.cost_price);
        planFinalPrice = findViewById(R.id.final_price);
        qrSectionTitle = findViewById(R.id.qr_section_title);
        qrSectionSubtitle = findViewById(R.id.qr_section_subtitle);
        validityText = findViewById(R.id.validity_text);
        qrCodeImage = findViewById(R.id.qr_code_payment_image);
        shimmerFrameLayout = findViewById(R.id.qr_code_shimmer);
        shimmerContainer = findViewById(R.id.shimmer_container);
        contentContainer = findViewById(R.id.content_container);
        middleBadgeImageVideo = findViewById(R.id.middle_badge_image_video);
        otherPaymentOption = findViewById(R.id.other_payment_option);
        shareQrBtn = findViewById(R.id.share_qr_btn);
        featureTitleView = findViewById(R.id.feature_card_title);
        spinnerVideoDetails = findViewById(R.id.spinnerVideoDetails);
        imageShimmer = findViewById(R.id.image_shimmer);

        thumbnail = findViewById(R.id.thumbnail_image);
        thumbnail.setOnClickListener(v -> openPlayerFullscreen());

        videoPlayer = findViewById(R.id.player_view);
        videoPlayer.setOnClickListener(v -> openPlayerFullscreen());

        volume = findViewById(R.id.volume);
        volume.setOnClickListener(v -> toggleVolume());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Header / video section
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * loadHeroBanner — videoUrl sourced from planMetaData (Change 2).
     */

    // TODO — Remove after dev-testing
    private void loadHeroBanner() {
        SubscriptionPlanMetaData planMetaData = subscriptionModel.getPlanMetaData();
        if (planMetaData == null) return;
        String videoUrl = StringUtils.isNotEmpty(planMetaData.getVideoUrl())
                ? planMetaData.getVideoUrl()
                : "";
        applyHeaderSection(planMetaData.getThumbnailUrl(), videoUrl);
    }

    /**
     * applyHeaderSection — three-branch logic:
     * <p>
     * Branch A  Condition 2 (returning-user video):
     * Play returningUserVideoUrl; hide badges.
     * If subscription popup is still visible, defer via isVideoStartPending.
     * <p>
     * Branch B  First visit (KEY_FIRST_VISIT_DONE not set):
     * Poster only — player never started.
     * Marks KEY_FIRST_VISIT_DONE = true.
     * <p>
     * Branch C  Second visit onwards:
     * If subscription popup visible → set isVideoStartPending = true.
     * Otherwise → doStartVideo() immediately.
     */
    private void applyHeaderSection(String thumbnailUrl, String videoUrl) {
        Glide.with(this).load(thumbnailUrl).into(thumbnail);

        if (isReturningUserVideoMode && StringUtils.isNotEmpty(returningUserVideoUrl)) {
            Log.d(TAG, "applyHeaderSection → Condition 2 returning-user video.");
            middleBadgeImageVideo.setVisibility(View.GONE);
            if (!videoPlayed) {
                this.videoUrl = returningUserVideoUrl;
                thumbnailCacheUrl = thumbnailUrl;
                videoPlayed = true;
                if (isSubscriptionPopupVisible) {
                    isVideoStartPending = true;
                    Log.d(TAG, "applyHeaderSection → returning-user video deferred (popup visible).");
                } else {
                    doStartVideo();
                }
            }

        } else if (!MyApplication.getBoolData(KEY_FIRST_VISIT_DONE)) {
            // ── Branch B: first visit — poster only ───────────────────────────
            Log.d(TAG, "applyHeaderSection → first visit, poster only.");
            this.videoUrl = videoUrl;
            thumbnailCacheUrl = thumbnailUrl;
            MyApplication.saveData(KEY_FIRST_VISIT_DONE, true);

        } else {
            // ── Branch C: second visit onwards ────────────────────────────────
            Log.d(TAG, "applyHeaderSection → returning visit.");
            this.videoUrl = videoUrl;
            thumbnailCacheUrl = thumbnailUrl;
            if (!videoPlayed) {
                videoPlayed = true;
                if (isSubscriptionPopupVisible) {
                    isVideoStartPending = true;
                    Log.d(TAG, "applyHeaderSection → video deferred (popup visible).");
                } else {
                    doStartVideo();
                }
            }
        }
    }

    // =========================================================================
    // Video player helpers
    // =========================================================================

    /**
     * Initialises the player and schedules playback start after a 2 s delay.
     * Single entry point so the popup-deferral flush and the normal path both
     * go through the same code.
     */
    private void doStartVideo() {
        Log.d(TAG, "doStartVideo() called.");
        initializePlayer();
        startHandler();
    }

    private void startHandler() {
        handlerPlayer = new Handler();
        runnablePlayer = this::startPlayer;
        handlerPlayer.postDelayed(runnablePlayer, 2000);
    }

    private void startPlayer() {
        if (isSubscriptionPopupVisible || isBottomSheetOpened) {
            isVideoStartPending = true;
            return;
        }
        if (StringUtils.isNotEmpty(videoUrl)) buildMediaSource(Uri.parse(videoUrl));
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            videoPlayer.setPlayer(player);
        }
    }

    private void buildMediaSource(Uri uri) {
        try {
            if (player == null) initializePlayer();
            player.setMediaItem(new MediaItem.Builder().setUri(uri).build());
            player.prepare();
            player.setPlayWhenReady(true);
            player.addListener(this);
        } catch (Exception ignored) {
        }
    }

    /**
     * Player state callbacks.
     * <p>
     * STATE_ENDED for Condition 2 returning-user video:
     * • Badges stay GONE.
     * • isReturningUserVideoMode reset to false.
     * • isBottomSheetOpened reset so Condition 1 re-arms.
     */
    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        if (player == null) return;
        switch (player.getPlaybackState()) {
            case Player.STATE_BUFFERING:
                spinnerVideoDetails.setVisibility(View.VISIBLE);
                videoPlayer.setVisibility(View.GONE);
                volume.setVisibility(View.GONE);
                thumbnail.setVisibility(View.VISIBLE);
                middleBadgeImageVideo.setVisibility(View.GONE);
                break;

            case Player.STATE_READY:
                spinnerVideoDetails.setVisibility(View.GONE);
                thumbnail.setVisibility(View.GONE);
                middleBadgeImageVideo.setVisibility(View.VISIBLE);
                videoPlayer.setVisibility(View.VISIBLE);
                volume.setVisibility(View.VISIBLE);
                if (videoStartingTime > 0) {
                    player.seekTo(videoStartingTime);
                    videoStartingTime = -1;
                }
                break;

            case Player.STATE_ENDED:
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.unnati_video_completed.getActionName());
                releasePlayer();
                videoPlayer.setVisibility(View.GONE);
                volume.setVisibility(View.GONE);
                thumbnail.setVisibility(View.VISIBLE);
                middleBadgeImageVideo.setVisibility(View.GONE);
                if (isReturningUserVideoMode) {
                    isReturningUserVideoMode = false;
                    isBottomSheetOpened = false;
                    isBackButtonBottomSheet = false;
                    Log.d(TAG, "Returning-user video ended. Badges hidden. Condition 1 re-armed.");
                }
                break;

            case Player.STATE_IDLE:
                videoStartingTime = player.getCurrentPosition();
                break;

            default:
                break;
        }
    }

    private void resumePlayer() {
        if (player != null) {
            Log.d(TAG, "resumePlayer()");
            player.setPlayWhenReady(true);
        }
    }

    private void pausePlayer() {
        if (player != null) {
            Log.d(TAG, "pausePlayer()");
            player.setPlayWhenReady(false);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            videoPlayer.setPlayer(null);
            player.release();
            player = null;
        }
    }

    private void openPlayerFullscreen() {
        if (StringUtils.isEmpty(videoUrl)) return;
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.unnati_video_expand_click.getActionName());
        pausePlayer();
        Intent intent = new Intent(this, ExoPlayerActivity.class);
        intent.putExtra("video_uri", videoUrl);
        intent.putExtra("video_starting",
                player != null ? player.getCurrentPosition()
                        : videoEnded ? 0L : videoStartingTime);
        if (subscriptionModel != null && subscriptionModel.getBadgeImageUrl() != null) {
            intent.putExtra("badge_image_url", subscriptionModel.getBadgeImageUrl());
        }
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onBottomSheetDismissed() {
        try {
            bottomSheetDialog = null;
        } catch (Exception ignored) {}
        isBottomSheetOpened     = false;      // re-arm back-button gate if needed
        isBackButtonBottomSheet = false;
        resumePlayer();
        Log.d(TAG, "onBottomSheetDismissed (outside tap) → video resumed.");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            videoStartingTime = data.getLongExtra("video_starting", 0);
            videoEnded = data.getBooleanExtra("video_ended", false);
        }

        if (requestCode != REQUEST_CODE) return;

        if (videoEnded) {
            releasePlayer();
            videoPlayer.setVisibility(View.GONE);
            volume.setVisibility(View.GONE);
            spinnerVideoDetails.setVisibility(View.GONE);
            thumbnail.setVisibility(View.VISIBLE);
            middleBadgeImageVideo.setVisibility(View.GONE);

            if (isReturningUserVideoMode) {

                isReturningUserVideoMode = false;
                isBottomSheetOpened      = false;
                isBackButtonBottomSheet  = false;
            }
            return;
        }

        if (player == null) {
            initializePlayer();
        }

        if (player.getCurrentMediaItem() == null && StringUtils.isNotEmpty(videoUrl)) {
            buildMediaSource(Uri.parse(videoUrl));
        } else {
            // Item still loaded — just seek and resume.
            player.seekTo(videoStartingTime);
            player.setPlayWhenReady(true);
        }

        spinnerVideoDetails.setVisibility(View.GONE);
        thumbnail.setVisibility(View.GONE);
        middleBadgeImageVideo.setVisibility(View.VISIBLE);
        videoPlayer.setVisibility(View.VISIBLE);
        volume.setVisibility(View.VISIBLE);
    }

    private void toggleVolume() {
        if (player == null) return;
        if (player.getVolume() == 0f) {
            volume.setImageDrawable(getResources().getDrawable(R.drawable.volume));
            player.setVolume(1f);
        } else {
            volume.setImageDrawable(getResources().getDrawable(R.drawable.mute));
            player.setVolume(0f);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Click listeners
    // ─────────────────────────────────────────────────────────────────────────

    private void setupClickListeners() {
        otherPaymentOption.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.subscription_other_payment_click.getActionName(),
                    withNavFrom(null));
            handleOtherPaymentClicked();
        });
        shareQrBtn.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.subscription_share_qr_click.getActionName(),
                    withNavFrom(null));
            shareQRCode();
        });
    }

    private void handleOtherPaymentClicked() {
        if (isPreCallInProgress) {
            Toast.makeText(this, "Please wait…", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userSubscriptionDto != null) {
            doPayment(resolvedAmount, userSubscriptionDto.getOrderId(), userSubscriptionDto);
        } else {
            subscriptionPreCall(() -> {
                if (userSubscriptionDto != null) {
                    doPayment(resolvedAmount, userSubscriptionDto.getOrderId(), userSubscriptionDto);
                } else {
                    // preCall succeeded but DTO is still null (parse error or server error path)
                    Log.e(TAG, "handleOtherPaymentClicked: userSubscriptionDto null after preCall");
                    Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void doPayment(int amount, String rzOrderId, UserSubscriptionDto dto) {
        Intent intent = new Intent(this, RazorpayPaymentGatewayActivity.class);
        intent.putExtra("planId", planId);
        intent.putExtra("orderId", rzOrderId);
        intent.putExtra("completedVideoUrl", completedVideoUrl);
        intent.putExtra(PreferenceKeys.COUPON_ID.getKey(), couponId);
        intent.putExtra("amount", String.valueOf(amount));
        intent.putExtra("userSubscriptionDto", dto);
        logFirebaseEventsForPaymentTransaction(
                "RazorpayPaymentGatewayActivity",
                CommonMethods.convertToStringMap(CommonMethods.getIntentParams(intent)));
        isReturningFromPayment = true;
        startActivity(intent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Network calls
    // ─────────────────────────────────────────────────────────────────────────

    private void fetchSubscriptionDetails() {
        showPageShimmer(true);

        MyJsonObjectRequest req = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(V2_GET_SUBSCRIPTION, new HashMap<>()),
                null,
                response -> {
                    if (!isActivityAlive()) return;
                    Log.d(TAG, "fetchSubscriptionDetails response: " + response);

                    subscriptionModel = gson.fromJson(
                            response.toString(),
                            new TypeToken<SubscriptionModel>() {
                            }.getType());

                    Log.d(TAG, "fetchSubscriptionDetails — meta data: "
                            + subscriptionModel.getPlanMetaData());

                    if (subscriptionModel != null && subscriptionModel.getOverview() != null) {
                        planId = subscriptionModel.getOverview().getPlanId();
                        if (planId == null || planId.isEmpty())
                            planId = getIntent().getStringExtra("subscriptionPlanId");

                        resolvedAmount = Integer.parseInt(
                                subscriptionModel.getOverview().getFinalPrice());
                        if (resolvedAmount <= 0)
                            resolvedAmount = getIntent().getIntExtra("paymentAmount", 0);

                        if (subscriptionModel.getPlanMetaData() != null) {
                            completedVideoUrl =
                                    subscriptionModel.getPlanMetaData()
                                            .getSubscriptionCompletedVideoUrl();
                            setQrSectionTexts(subscriptionModel.getPlanMetaData());
                            try {
                                String metaUrl = subscriptionModel.getPlanMetaData().getVideoUrl();
                                if (StringUtils.isNotEmpty(metaUrl))
                                    returningUserVideoUrl = metaUrl;
                            } catch (Exception ignored) {
                            }
                            if (StringUtils.isEmpty(returningUserVideoUrl)) {
                                returningUserVideoUrl = MyApplication.getFirebaseRemoteConfig()
                                        .getString(RC_RETURNING_USER_VIDEO_URL);
                            }
                            Log.d(TAG, "returningUserVideoUrl: " + returningUserVideoUrl);

                            if (subscriptionModel.getPlanMetaData()
                                    .getCampaignSubscriptionScreenContactDto() != null) {
                                campaignSubscriptionScreenContactDto =
                                        subscriptionModel.getPlanMetaData()
                                                .getCampaignSubscriptionScreenContactDto();
                            }
                        }

                        triggerSubscriptionPopUp(getIntent().getStringExtra("navigatedFromScreen"));
                        loadHeroBanner();
                        loadBadgeImage();
                        populateFeaturesFromModel();
                    } else {
                        Log.e(TAG, "fetchSubscriptionDetails: null model, Intent fallback");
                        planId = getIntent().getStringExtra("subscriptionPlanId");
                        resolvedAmount = getIntent().getIntExtra("paymentAmount", 0);
                        populateFeaturesFromIntent();
                    }

                    showPageShimmer(false);

                    if (MyApplication.getStringData(
                            PreferenceKeys.QR_CODE_IMAGE.getKey()) == null) {
                        generatedOrderId = getAlphaNumericString(ORDER_ID_LEN);
                        Log.i(TAG, "Temp orderId before preCall: " + generatedOrderId);
                    } else {
                        Log.i(TAG, "Cached QR — keeping restored orderId: " + generatedOrderId);
                    }

                    subscriptionPreCall(null);
                },
                error -> {
                    Log.e(TAG, "fetchSubscriptionDetails error: " + error.toString());
                    planId = getIntent().getStringExtra("subscriptionPlanId");
                    resolvedAmount = getIntent().getIntExtra("paymentAmount", 0);
                    if (MyApplication.getStringData(
                            PreferenceKeys.QR_CODE_IMAGE.getKey()) == null) {
                        generatedOrderId = getAlphaNumericString(ORDER_ID_LEN);
                    }
                    populateFeaturesFromIntent();
                    showPageShimmer(false);
                    subscriptionPreCall(null);
                });

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void subscriptionPreCall(@Nullable Runnable onSuccessOverride) {
        isPreCallInProgress = true;
        Log.d(TAG, "subscriptionPreCall — planId=" + planId
                + "  amount=" + resolvedAmount
                + "  orderId=" + generatedOrderId);

        SubscriptionPreModel preModel = new SubscriptionPreModel();
        preModel.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        preModel.setSubscriptionPlanId(planId);
        preModel.setOrderId(getAlphaNumericString(ORDER_ID_LEN));
        preModel.setTotalAmount((long) resolvedAmount);
        preModel.setRedirectionFrom(analyticsSource != null ? analyticsSource : "UNKNOWN");
        Log.d("SubscriptionSourceTrack", "Source: " + preModel.getRedirectionFrom() + " | Plan: " + planId + " (SubscriptionPaymentScreen)");

        if (couponId != null && !couponId.isEmpty()) {
            CouponDto couponDto = new CouponDto();
            couponDto.setCode(couponId);
            preModel.setCoupon(couponDto);
            Log.d(TAG, "Coupon applied: " + couponId);
        }

        JSONObject body;
        try {
            body = new JSONObject(gson.toJson(preModel));
            Log.d(TAG, "subscriptionPreCall body: " + body);
        } catch (JSONException e) {
            Log.e(TAG, "subscriptionPreCall JSON error — skipping to QR", e);
            isPreCallInProgress = false;
            runPostPreCall(onSuccessOverride);
            return;
        }

        MyJsonObjectRequest req = new MyJsonObjectRequest(
                Request.Method.POST,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, new HashMap<>()),
                body,
                response -> {
                    Log.d(TAG, "subscriptionPreCall response: " + response);
                    try {
                        UserSubscriptionDto freshDto = gson.fromJson(
                                response.getJSONObject("data").toString(),
                                new TypeToken<UserSubscriptionDto>() {
                                }.getType());

                        String cachedQrImage = MyApplication.getStringData(
                                PreferenceKeys.QR_CODE_IMAGE.getKey());

                        if (cachedQrImage == null) {
                            userSubscriptionDto = freshDto;
                            if (userSubscriptionDto != null) {
                                generatedOrderId = userSubscriptionDto.getOrderId();
                                Log.i(TAG, "Fresh session — orderId: " + generatedOrderId);
                            }
                        } else {
                            Log.i(TAG, "Cached QR — ignoring new orderId="
                                    + (freshDto != null ? freshDto.getOrderId() : "null")
                                    + "  keeping cached orderId=" + generatedOrderId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse userSubscriptionDto: " + e.getMessage(), e);
                    }
                    isPreCallInProgress = false;
                    runPostPreCall(onSuccessOverride);
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "subscriptionPreCall error: " + error.getMessage());
                        logPaymentFailure("USER_SUBSCRIPTION_PRE", error.getMessage());
                        isPreCallInProgress = false;
                        runPostPreCall(onSuccessOverride);
                    }
                });

        MyVolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void runPostPreCall(@Nullable Runnable onSuccessOverride) {
        if (onSuccessOverride != null) {
            onSuccessOverride.run();
            return;
        }

        if (pendingPaymentId != null) {
            String pid = pendingPaymentId;
            pendingPaymentId = null;
            Log.i(TAG, "Flushing deferred payment — paymentId=" + pid
                    + "  orderId=" + generatedOrderId);
            buyNowPostCall(pid);
            return;
        }

        String cachedImage =
                MyApplication.getStringData(PreferenceKeys.QR_CODE_IMAGE.getKey());
        if (cachedImage == null) {
            if (SubscriptionQrLoadHelper.isLoading()) {
                showShimmer(true);
                SubscriptionQrLoadHelper.addReadyListener(() ->
                        runOnUiThread(this::loadQrCodeImage));
            } else {
                fetchQRCode();
            }
        } else {
            Log.i(TAG, "QR cache hit — keeping restored session. orderId=" + generatedOrderId);
            loadQrCodeImage();
        }
    }

    private void fetchQRCode() {
        showShimmer(true);

        Log.i(TAG_QR, "Requesting QR — amount=" + resolvedAmount
                + "  planId=" + planId
                + "  orderId=" + generatedOrderId);

        Map<String, Object> params = new HashMap<>();
        params.put("orderId", generatedOrderId);
        params.put("subscriptionPlanId", planId);
        params.put("paymentAmount", resolvedAmount);
        params.put("transactionId",
                getIntent().getStringExtra("transactionId") != null
                        ? getIntent().getStringExtra("transactionId") : "");

        MyJsonObjectRequest req = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(RAZORPAY_CREATE_QR_CODE, params),
                null,
                jsonObject -> {
                    razorpayQRCreateResponse = gson.fromJson(
                            jsonObject.toString(),
                            new TypeToken<RazorpayQRCreateResponse>() {
                            }.getType());
                    Log.i(TAG_QR, razorpayQRCreateResponse.toString());

                    initiatePaymentInDb(resolvedAmount, generatedOrderId);

                    if (MyApplication.getStringData(
                            PreferenceKeys.QR_CODE_IMAGE.getKey()) == null) {
                        MyApplication.saveData(PreferenceKeys.QR_CODE_IMAGE.getKey(),
                                razorpayQRCreateResponse.getImage_url());
                        MyApplication.saveData(PreferenceKeys.QR_CODE_IMAGE_TIMESTAMP.getKey(),
                                String.valueOf(System.currentTimeMillis()));
                        MyApplication.saveData(PreferenceKeys.QR_CODE_ID.getKey(),
                                razorpayQRCreateResponse.getId());
                        MyApplication.saveData(PreferenceKeys.QR_CODE_ORDER_ID.getKey(),
                                generatedOrderId);
                        if (userSubscriptionDto != null)
                            MyApplication.saveData(
                                    PreferenceKeys.QR_CODE_SUBSCRIPTION_DTO.getKey(),
                                    gson.toJson(userSubscriptionDto));
                        loadQrCodeImage();
                    }
                },
                error -> {
                    Log.e(TAG_QR, error.toString());
                    logPaymentFailure("RAZORPAY_CREATE_QR_CODE", error.toString());
                });

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void loadQrCodeImage() {
        String cachedUrl =
                MyApplication.getStringData(PreferenceKeys.QR_CODE_IMAGE.getKey());
        Log.i(TAG, "loadQrCodeImage: " + cachedUrl);
        if (cachedUrl == null) return;

        if (razorpayQRCreateResponse == null) {
            String cachedQrId =
                    MyApplication.getStringData(PreferenceKeys.QR_CODE_ID.getKey());
            if (cachedQrId != null && !cachedQrId.isEmpty()) {
                razorpayQRCreateResponse = new RazorpayQRCreateResponse();
                razorpayQRCreateResponse.setId(cachedQrId);
                razorpayQRCreateResponse.setImage_url(cachedUrl);
                Log.i(TAG_QR, "Restored razorpayQRCreateResponse — ID: " + cachedQrId);
            }
        }

        String cachedOrderId =
                MyApplication.getStringData(PreferenceKeys.QR_CODE_ORDER_ID.getKey());
        if (cachedOrderId != null && !cachedOrderId.isEmpty()) {
            generatedOrderId = cachedOrderId;
            Log.i(TAG, "Restored generatedOrderId from cache: " + generatedOrderId);
        }

        if (userSubscriptionDto == null) {
            String cachedDtoJson = MyApplication.getStringData(
                    PreferenceKeys.QR_CODE_SUBSCRIPTION_DTO.getKey());
            if (cachedDtoJson != null && !cachedDtoJson.isEmpty()) {
                try {
                    userSubscriptionDto = gson.fromJson(cachedDtoJson,
                            new TypeToken<UserSubscriptionDto>() {
                            }.getType());
                    Log.i(TAG, "Restored userSubscriptionDto — orderId: "
                            + (userSubscriptionDto != null
                            ? userSubscriptionDto.getOrderId() : "null"));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to restore userSubscriptionDto", e);
                }
            }
        }

        if (cachedUrl.equals(lastLoadedQrUrl)) {
            Log.d(TAG, "loadQrImage: same url, skip");
            return;
        }

        lastLoadedQrUrl = cachedUrl;

        GlideUrl url = new GlideUrl(cachedUrl);
        Glide.with(getApplicationContext())
                .load(url)
                .apply(shouldCropQR
                        ? com.bumptech.glide.request.RequestOptions
                        .bitmapTransform(new CropQRTransformation())
                        : new com.bumptech.glide.request.RequestOptions())
                .listener(buildGlideListener())
                .into(qrCodeImage);
    }

    private void initiatePaymentInDb(long paymentAmount, String orderId) {
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());

        PaymentDetails paymentDetails = PaymentDetails.builder()
                .amount(paymentAmount * 100)
                .orderId(orderId)
                .type(TransactionType.SUBSCRIPTION.name())
                .paymentMode("RAZORPAY_QR")
                .qrCodeId(razorpayQRCreateResponse.getId())
                .userId(userId)
                .build();

        try {
            MyJsonObjectRequest req = new MyJsonObjectRequest(
                    Request.Method.POST,
                    PAYMENT_INFO_RAZORPAY,
                    new JSONObject(gson.toJson(paymentDetails)),
                    response -> {
                        PaymentInfo info = gson.fromJson(
                                response.toString(),
                                new TypeToken<PaymentInfo>() {
                                }.getType());
                        if (info != null) {
                            Log.i("ORDERIDRAZORPAY", "orderId: " + info.getOrderId());
                            Log.i("ORDERIDRAZORPAY", "amount : " + paymentAmount);
                        }
                    },
                    new VolleyGlobalErrorListener(this) {
                        @Override
                        public void postError(VolleyError error) {
                            Map<String, String> p = new HashMap<>();
                            p.put("Subscription_plan_Id",
                                    getIntent().getStringExtra("subscriptionPlanId"));
                            p.put("amount", getIntent().getStringExtra("amount"));
                            p.put("action", "PaymentError QR = PAYMENT_INFO_RAZORPAY "
                                    + error.getMessage());
                            p.put("navigation_from", navigationFrom);
                            MyApplication.logFirebaseBundleCreation(
                                    FirebaseEvents.Payment_failed.getActionName(), p);
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.encountered_some_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            MyVolleySingleton.getInstance(this).addToRequestQueue(req);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.encountered_some_error), Toast.LENGTH_SHORT).show();
        }
    }

    private RequestListener<Drawable> buildGlideListener() {
        return new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                        Target<Drawable> target, boolean isFirstResource) {
                if (e != null)
                    Log.e(TAG_QR, "QR image load failed: " + e.getMessage(), e);
                if (imageRetryHandler == null)
                    imageRetryHandler = new Handler(Looper.getMainLooper());
                if (imageRetryRunnable != null)
                    imageRetryHandler.removeCallbacks(imageRetryRunnable);
                imageRetryRunnable = () -> loadQrCodeImage();
                imageRetryHandler.postDelayed(imageRetryRunnable, 1_000);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model,
                                           Target<Drawable> target,
                                           DataSource dataSource, boolean isFirstResource) {
                showShimmer(false);
                if (isActivityAlive()) {
                    try {
                        Glide.with(SubscriptionPaymentScreen.this)
                                .asBitmap()
                                .load(MyApplication.getStringData(
                                        PreferenceKeys.QR_CODE_IMAGE.getKey()))
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap bmp,
                                                                @Nullable Transition<? super Bitmap> t) {
                                        if (!isActivityAlive()) return;
                                        cachedShareUri = saveBitmapAndGetUri(bmp);
                                        Log.d(TAG_SHARE, "Share URI cached: " + cachedShareUri);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable p) {
                                    }
                                });
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG, "buildGlideListener: activity destroyed before QR bitmap cache");
                    }
                }
                return false;
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shimmer helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void showPageShimmer(boolean show) {
        if (show) {
            shimmerContainer.startShimmer();
            shimmerContainer.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.GONE);
        } else {
            shimmerContainer.stopShimmer();
            shimmerContainer.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showShimmer(boolean show) {
        if (show) {
            shimmerFrameLayout.startShimmer();
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            qrCodeImage.setVisibility(View.GONE);
        } else {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            qrCodeImage.setVisibility(View.VISIBLE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QR payment polling
    // ─────────────────────────────────────────────────────────────────────────

    private void checkStatusOfQRPayment() {
        Log.d(TAG_QR, "checkStatusOfQRPayment() called");

        if (razorpayQRCreateResponse == null) {
            Log.w(TAG_QR, "QR Create Response is null. Skipping status check.");
            return;
        }

        String qrCodeId = razorpayQRCreateResponse.getId();
        Log.d(TAG_QR, "QR Code ID: " + qrCodeId);

        Map<String, Object> params = new HashMap<>();
        params.put("qrCodeId", qrCodeId);

        String finalUrl = CommonMethods.getUrlParam(RAZORPAY_CHECK_QR_PAYMENT_STATUS, params);

        MyJsonObjectRequest req = new MyJsonObjectRequest(
                Request.Method.GET,
                finalUrl,
                null,
                jsonObject -> {
                    try {
                        Log.d(TAG_QR, "Raw API Response: " + jsonObject);

                        RazorpayQRPaymentResponse resp = gson.fromJson(
                                jsonObject.toString(),
                                new TypeToken<RazorpayQRPaymentResponse>() {
                                }.getType());
                        Log.i(TAG_QR, "Parsed: " + resp);

                        if (resp == null) {
                            Log.e(TAG_QR, "Parsed response is null");
                            return;
                        }
                        if (resp.getItems() == null) {
                            Log.e(TAG_QR, "Items list is null");
                            return;
                        }
                        if (resp.getItems().isEmpty()) {
                            Log.w(TAG_QR, "Payment not completed yet.");
                            return;
                        }

                        String paymentId = resp.getItems().get(0).getId();
                        Log.i(TAG_QR, "Payment SUCCESS. paymentId=" + paymentId);

                        handler.removeCallbacks(pollRunnable);
                        clearQrSession();

                        if (isPreCallInProgress) {
                            Log.w(TAG, "Payment detected while preCall in-flight — "
                                    + "deferring. paymentId=" + paymentId);
                            pendingPaymentId = paymentId;
                        } else {
                            buyNowPostCall(paymentId);
                        }

                    } catch (Exception e) {
                        Log.e(TAG_QR, "Exception parsing response: " + e.getMessage(), e);
                    }
                },
                error -> {
                    Log.e(TAG_QR, "API Error checking QR status: " + error.toString());
                    logPaymentFailure("RAZORPAY_CHECK_QR_PAYMENT_STATUS", error.toString());
                });

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void buyNowPostCall(String transactionId) {
        Log.d(TAG, "buyNowPostCall() — transactionId=" + transactionId
                + "  orderId=" + generatedOrderId);

        if (userSubscriptionDto == null) {
            Log.e(TAG, "userSubscriptionDto is NULL — cannot proceed");
            Toast.makeText(this, "Session error. Please restart the screen.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        SubscriptionPostModel model = new SubscriptionPostModel();
        model.setPaymentStatus(PaymentStatus.SUCCESS.name());
        model.setInvoiceId(userSubscriptionDto.getInvoiceId());
        model.setTransactionId(transactionId);
        model.setSubscriptionId(userSubscriptionDto.getId());
        model.setOrderId(generatedOrderId);

        JSONObject body;
        try {
            body = new JSONObject(gson.toJson(model));
        } catch (JSONException e) {
            Log.e(TAG, "buyNowPostCall JSON error", e);
            return;
        }

        MyJsonObjectRequest req = new MyJsonObjectRequest(
                Request.Method.PUT,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, new HashMap<>()),
                body,
                response -> {
                    Log.d(TAG, "buyNowPostCall success: " + response);
                    handleBuyNowSuccess(response, model, transactionId);
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "API Error: " + error.getMessage(), error);
                        logPaymentFailure("USER_SUBSCRIPTION_PUT", error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                "Sorry, we faced a technical problem. Please contact us.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void handleBuyNowSuccess(JSONObject response,
                                     SubscriptionPostModel model,
                                     String transactionId) {
        Log.d(TAG, "handleBuyNowSuccess()");
        try {
            if (!response.getBoolean("success")) {
                Toast.makeText(this,
                        "Sorry, we faced some technical problem. Please contact us.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = resolvedAmount;
            facebookLogPurchaseEvent(model.getSubscriptionId(), amount);

            boolean showBenefits = MyApplication.getFirebaseRemoteConfig()
                    .getBoolean(getString(R.string.show_subscription_benefits));
            if (!showBenefits)
                Toast.makeText(this, "Congratulations! You have subscribed.",
                        Toast.LENGTH_SHORT).show();

            Map<String, String> ap = new HashMap<>();
            ap.put("Subscription_plan_Id", planId);
            ap.put("amount", String.valueOf((int) amount));
            ap.put("modeOfPayment", "RAZOR PAY QR CODE");
            ap.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation("payment_successful_" + amount, ap);
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.Payment_successful.getActionName(), ap);
            MyApplication.purchaseFirebaseEvent(
                    planId, transactionId, amount, "RAZOR PAY QR CODE");
            BranchAnalyticsHelper.logPurchase(this, planId, transactionId, amount, "RAZOR PAY QR CODE");

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (MyApplication.getFirebaseRemoteConfig()
                        .getBoolean(getString(R.string.show_subscription_benefits))) {
                    navigateToCongratulations(response);
                } else {
                    navigateToHome();
                }
            }, 2_000);

        } catch (Exception e) {
            Log.e(TAG, "handleBuyNowSuccess error", e);
            Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bottom Sheet — regular contact sheet (returning from payment)
    // Pauses video when shown; video resumes only if user stays (onJoinNowClick).
    // ─────────────────────────────────────────────────────────────────────────

    private void triggerBottomSheet() {
        if (isFinishing() || isDestroyed()) return;
        if (getSupportFragmentManager().isStateSaved()) return;
        if (campaignSubscriptionScreenContactDto == null
                || !MyApplication.isNetworkAvailable()) return;
        ContactBottomSheet existing =
                (ContactBottomSheet) getSupportFragmentManager()
                        .findFragmentByTag("ContactBottomSheet");
        if (existing != null && existing.isAdded()) return;

        pausePlayer();

        bottomSheetDialog = ContactBottomSheet
                .newInstance(campaignSubscriptionScreenContactDto);
        try {
            bottomSheetDialog.show(getSupportFragmentManager(), "ContactBottomSheet");
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.contact_us_popup_view.getActionName(), withNavFrom(null));
            isBottomSheetOpened = true;
            Log.d(TAG, "triggerBottomSheet: contact bottom sheet shown, video paused.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to show contact bottom sheet", e);
            resumePlayer(); // show failed — restore playback
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bottom Sheet — back-button popup (Condition 1)
    // Pauses video when shown; video resumes only if user taps "Join at ₹51".
    // ─────────────────────────────────────────────────────────────────────────

    private void triggerBackButtonBottomSheet() {
        if (isFinishing() || isDestroyed()) return;
        if (getSupportFragmentManager().isStateSaved()) return;
        if (!MyApplication.isNetworkAvailable()) return;

        ContactBottomSheet existing =
                (ContactBottomSheet) getSupportFragmentManager()
                        .findFragmentByTag("BackButtonContactBottomSheet");
        if (existing != null && existing.isAdded()) return;

        String joinNowText = null;
        if (campaignSubscriptionScreenContactDto != null)
            joinNowText = campaignSubscriptionScreenContactDto.getJoinNowText();
        if (StringUtils.isEmpty(joinNowText)) {
            joinNowText = getString(R.string.join_now_text);
        }

        String whatsappNumber = campaignSubscriptionScreenContactDto.getPhoneNumber();

        String posterUrl = MyApplication.getFirebaseRemoteConfig().getString(RC_BACK_POPUP_POSTER);
        if (StringUtils.isEmpty(posterUrl) && campaignSubscriptionScreenContactDto != null)
            posterUrl = campaignSubscriptionScreenContactDto.getPosterImage();

        String audioUrl = MyApplication.getFirebaseRemoteConfig().getString(RC_BACK_POPUP_AUDIO);
        if (StringUtils.isEmpty(audioUrl) && campaignSubscriptionScreenContactDto != null)
            audioUrl = campaignSubscriptionScreenContactDto.getAudioUrl();


        CampaignSubscriptionScreenContactDto backPopupDto =
                CampaignSubscriptionScreenContactDto.builder()
                        .phoneNumber(whatsappNumber)
                        .posterImage(posterUrl)
                        .audioUrl(audioUrl)
                        .joinNowText(joinNowText)
                        .floatingActionButtonText("")
                        .callBackText(campaignSubscriptionScreenContactDto.getCallBackText())
                        .build();

        pausePlayer();

        bottomSheetDialog = ContactBottomSheet.newInstance(backPopupDto);
        try {
            bottomSheetDialog.show(
                    getSupportFragmentManager(), "BackButtonContactBottomSheet");
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.contact_us_popup_view.getActionName(), withNavFrom(null));
            Log.d(TAG, "triggerBackButtonBottomSheet: back-button popup shown, video paused.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to show back-button bottom sheet", e);
            resumePlayer();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ContactBottomSheet.ContactBottomSheetListner
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onPaymentOptionClick() {
        try {
          onJoinNowClick();
        } catch (Exception ignored) {}
        resumePlayer();
        Log.d(TAG, "onPaymentOptionClick → bottom sheet dismissed, video resumed.");
    }

    /**
     * X (cross) button tapped — dismiss and navigate home.
     * Player is released with the activity; no explicit resume needed.
     */
    @Override
    public void onCloseButtonClick() {
        isBackButtonBottomSheet = false;
        try {
            if (bottomSheetDialog != null) {
                bottomSheetDialog.dismiss();
                bottomSheetDialog = null;
            }
        } catch (Exception ignored) {
        }
        Log.d(TAG, "onCloseButtonClick → navigating home.");
        navigateToHome();
    }

    /**
     * Primary button tapped ("Join at ₹51" / "₹51 में जॉइन करें").
     * <p>
     * Dismisses the back-button popup and resumes the video so audio/video
     * are in sync again after the bottom-sheet audio finishes.
     */
    public void onJoinNowClick() {
        if (isBackButtonBottomSheet) {
            isBackButtonBottomSheet = false;
            isBottomSheetOpened = false; // allow Condition 1 to fire again on next back press
        }
        try {
            if (bottomSheetDialog != null) {
                bottomSheetDialog.dismiss();
                bottomSheetDialog = null;
            }
        } catch (Exception ignored) {}
        resumePlayer();
        Log.d(TAG, "onJoinNowClick: bottom sheet dismissed, video resumed.");
    }

    /**
     * Secondary / contact button tapped ("कोई समस्या है? संपर्क करें").
     * <p>
     * Back-button popup → open WhatsApp.  Video stays paused while WhatsApp
     * is open; it will resume when the user returns and taps "Join at ₹51".
     */
    public void onContactButtonClick(String phoneNumber) {
        if (isBackButtonBottomSheet) {
            Log.d(TAG, "onContactButtonClick: back popup → opening WhatsApp: " + phoneNumber);
            openWhatsApp(phoneNumber);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WhatsApp helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String getBackPopupWhatsAppNumber() {
        String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
        if (StringUtils.isNotEmpty(examCode)) {
            String rcKey = "back_popup_whatsapp_" + examCode.toLowerCase();
            String rcNumber = MyApplication.getFirebaseRemoteConfig().getString(rcKey);
            if (StringUtils.isNotEmpty(rcNumber)) return rcNumber;
        }
        if (examCode != null) {
            switch (examCode.toUpperCase()) {
                case "RJ":
                    return "911156699";
                case "UP":
                    return "9111566998";
                default:
                    return "9111566686";
            }
        }
        return "9111566686";
    }

    private void openWhatsApp(String phoneNumber) {
        if (StringUtils.isEmpty(phoneNumber)) return;
        String normalised = phoneNumber.replaceAll("[^0-9]", "");
        String url = "https://wa.me/91" + normalised;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.whatsapp");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception ex) {
                Toast.makeText(this, "WhatsApp not available.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Image loading
    // ─────────────────────────────────────────────────────────────────────────

    private void loadBadgeImage() {
        if (!isActivityAlive() || subscriptionModel == null) return;
        String url = subscriptionModel.getBadgeImageUrl();
        if (url == null || url.isEmpty()) return;
        try {
            Glide.with(this).load(url)
                    .placeholder(R.drawable.batch_details)
                    .error(R.drawable.batch_details)
                    .into(middleBadgeImageVideo);
        } catch (Exception e) {
            Log.e(TAG, "loadBadgeImage error", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Features population
    // ─────────────────────────────────────────────────────────────────────────

    private void populateFeaturesFromModel() {
        if (subscriptionModel == null) {
            populateFeaturesFromIntent();
            return;
        }

        String title = subscriptionModel.getFeatureTitle();
        if (title != null && !title.isEmpty() && featureTitleView != null)
            featureTitleView.setText(title);

        ArrayList<String> features = subscriptionModel.getFeatures();
        if (features == null || features.isEmpty()) {
            populateFeaturesFromIntent();
            return;
        }

        int[] ids = {
                R.id.feature_batch_title, R.id.feature_live_classes,
                R.id.feature_topper_notes, R.id.feature_free_rewards,
                R.id.feature_student_community, R.id.feature_recorded_lectures,
                R.id.feature_doubt_support
        };
        for (int i = 0; i < ids.length; i++) {
            if (i < features.size()) {
                setFeatureText(ids[i], features.get(i));
            } else {
                View v = findViewById(ids[i]);
                if (v != null) v.setVisibility(View.GONE);
            }
        }
    }

    private void populateFeaturesFromIntent() {
        String batch = getIntent().getStringExtra("planName");
        setFeatureText(R.id.feature_batch_title, batch != null ? batch : "Batch Title");
        setFeatureText(R.id.feature_live_classes, "Live Classes");
        setFeatureText(R.id.feature_topper_notes, "Topper Notes");
        setFeatureText(R.id.feature_free_rewards, "Free Rewards");
        setFeatureText(R.id.feature_student_community, "Student Community");
        setFeatureText(R.id.feature_recorded_lectures, "Recorded Lectures");
        setFeatureText(R.id.feature_doubt_support, "Doubt Support");
    }

    private void setFeatureText(int includedLayoutId, String text) {
        View v = findViewById(includedLayoutId);
        if (v == null) return;
        TextView label = v.findViewById(R.id.feature_label);
        if (label != null) label.setText(text);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QR share
    // ─────────────────────────────────────────────────────────────────────────

    private void shareQRCode() {
        String raw = (subscriptionModel != null
                && subscriptionModel.getPlanMetaData() != null
                && subscriptionModel.getPlanMetaData().getShareQrCodeMessage() != null)
                ? subscriptionModel.getPlanMetaData().getShareQrCodeMessage()
                : "Abhi QR Scan karo ";

        String caption = raw
                .replaceAll("<b>(.*?)</b>", "*$1*")
                .replaceAll("<i>(.*?)</i>", "_$1_")
                .replaceAll("<[^>]+>", "");

        if (cachedShareUri != null) {
            launchShareIntent(cachedShareUri, caption);
            return;
        }

        if (razorpayQRCreateResponse == null) {
            Toast.makeText(this, "QR not ready yet, please wait.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isActivityAlive()) return;

        final String finalCaption = caption;
        try {
            Glide.with(this).asBitmap()
                    .load(razorpayQRCreateResponse.getImage_url())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap bmp,
                                                    @Nullable Transition<? super Bitmap> t) {
                            Uri uri = saveBitmapAndGetUri(bmp);
                            if (uri != null) {
                                cachedShareUri = uri;
                                launchShareIntent(uri, finalCaption);
                            } else {
                                Toast.makeText(SubscriptionPaymentScreen.this,
                                        "Could not share QR.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable p) {
                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "shareQRCode: activity destroyed");
        }
    }

    private void launchShareIntent(Uri imageUri, String caption) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.putExtra(Intent.EXTRA_TEXT, caption);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share Payment QR"));
    }

    private Uri saveBitmapAndGetUri(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "qr_share.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", file);
        } catch (Exception e) {
            Log.e(TAG_SHARE, "saveBitmapAndGetUri failed", e);
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────────────────────────────────

    private void navigateToCongratulations(JSONObject response) {
        Log.d(TAG, "navigateToCongratulations()");
        try {
            if (StringUtils.isNotEmpty(completedVideoUrl)) {
                Intent intent = new Intent(this, FeatureInfoVideoActivity.class);
                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("hide_bottom_layout", true);
                intent.putExtra("videoUrl", completedVideoUrl);
                intent.putExtra(FeatureInfoVideoActivity.bundleNavigationFrom,
                        FeatureInfoVideoActivity.NavigationFromEnum
                                .SUBSCRIPTION_PAYMENT_SCREEN.name());
                startActivity(intent);
                finish();
            } else {
                navigateToHome();
            }
        } catch (Exception e) {
            Log.e(TAG, "navigateToCongratulations error", e);
            Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QR session cache
    // ─────────────────────────────────────────────────────────────────────────

    private void clearQrCodeIfExpired() {
        String ts = MyApplication.getStringData(
                PreferenceKeys.QR_CODE_IMAGE_TIMESTAMP.getKey());
        if (ts == null) return;
        long expiryMs = MyApplication.getFirebaseRemoteConfig().getLong("qr_code_expiry_ms");
        try {
            if (System.currentTimeMillis() - Long.parseLong(ts) > expiryMs) {
                Log.i(TAG, "QR session expired — clearing cache");
                clearQrSession();
            }
        } catch (NumberFormatException ignored) {
            clearQrSession();
        }
    }

    private void clearQrSession() {
        MyApplication.removeString(PreferenceKeys.QR_CODE_IMAGE.getKey());
        MyApplication.removeString(PreferenceKeys.QR_CODE_IMAGE_TIMESTAMP.getKey());
        MyApplication.removeString(PreferenceKeys.QR_CODE_ID.getKey());
        MyApplication.removeString(PreferenceKeys.QR_CODE_ORDER_ID.getKey());
        MyApplication.removeString(PreferenceKeys.QR_CODE_SUBSCRIPTION_DTO.getKey());
        Log.d(TAG, "QR session cache cleared");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Subscription popup (lock screen)
    // Video start is deferred while the popup is visible, then flushed after
    // the popup's autoCloseDialogTimer elapses + 500 ms buffer.
    // ─────────────────────────────────────────────────────────────────────────

    private void triggerSubscriptionPopUp(String screenName) {
        try {
            Log.d(TAG, "triggerSubscriptionPopUp: " + screenName);
            SubscriptionLockPopupDto dto = getSubscriptionPopUpDtoDetails(screenName);
            if (dto != null) {
                isSubscriptionPopupVisible = true;
                Map<String,String> map=new HashMap<>();
                map.put("source",screenName);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_locked_popup_view.getActionName(),map);
                CommonDialogs.showLockPopupDialog(this, dto,
                        new CommonDialogs.SubscriptionPopupListener() {

                            @Override
                            public void onPopupClosed() {

                                isSubscriptionPopupVisible = false;

                                Log.d(TAG, "Popup closed callback");

                                if (isVideoStartPending) {
                                    isVideoStartPending = false;
                                    doStartVideo();
                                }
                            }

                            @Override
                            public void onJoinNowClicked() {

                                isSubscriptionPopupVisible = false;

                                Log.d(TAG, "Join now clicked");

                                if (isVideoStartPending) {
                                    isVideoStartPending = false;
                                    doStartVideo();
                                }
                            }
                        });

                Map<String, String> params = new HashMap<>();
                params.put("title", dto.getTitle() != null ? dto.getTitle() : "");
                params.put("subTitle", dto.getSubTitle() != null ? dto.getSubTitle() : "");
                params.put("navigation_from", screenName);
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.subscription_screen_lock_pop_up_viewed.getActionName(),
                        params);

                long popupDurationMs = dto.getAutoCloseDialogTimer() > 0
                        ? dto.getAutoCloseDialogTimer() : 4_000L;
                handler.postDelayed(() -> {
                    isSubscriptionPopupVisible = false;
                    Log.d(TAG, "Subscription popup closed. isVideoStartPending="
                            + isVideoStartPending);
                    if (isVideoStartPending) {
                        isVideoStartPending = false;
                        doStartVideo();
                    }
                }, popupDurationMs + 500L);

            } else {
                // No popup — ensure the gate is clear in case it was somehow left set.
                isSubscriptionPopupVisible = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "triggerSubscriptionPopUp: failed for screen=" + screenName, e);
            isSubscriptionPopupVisible = false;
        }
    }

    private SubscriptionLockPopupDto getSubscriptionPopUpDtoDetails(
            String navigatedFromScreen) {
        try {
            SubscriptionPopupType popupType = getPopupTypeFromScreen(navigatedFromScreen);
            if (popupType == SubscriptionPopupType.UNKNOWN) return null;

            String classType = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());
            String language = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()).toLowerCase();

            String jsonString = FirebaseRemoteConfig.getInstance()
                    .getString("subscription_popup_config_v2");
            if (StringUtils.isEmpty(jsonString)) return null;

            JSONObject rootJson = new JSONObject(jsonString);
            String typeKey = popupType.getValue();
            if (!rootJson.has(typeKey)) return null;
            JSONObject typeJson = rootJson.getJSONObject(typeKey);

            String combinedKey = classType + "_" + language;
            Log.d(TAG, "getSubscriptionPopUpDtoDetails-combinedKey: " + combinedKey);
            JSONObject dataJson = resolveWithFallback(typeJson, combinedKey);
            if (dataJson == null) return null;

            return SubscriptionLockPopupDto.builder()
                    .title(dataJson.optString("title", ""))
                    .subTitle(dataJson.optString("subTitle", ""))
                    .btnName(dataJson.optString("btnName", "Join at ₹51"))
                    .autoCloseDialogTimer(4000)
                    .build();

        } catch (Exception e) {
            Log.e(TAG, "getSubscriptionPopUpDtoDetails: failed for screen="
                    + navigatedFromScreen, e);
            return null;
        }
    }

    @Nullable
    private JSONObject resolveWithFallback(JSONObject parent, String key) {
        try {
            if (!StringUtils.isEmpty(key) && parent.has(key))
                return parent.getJSONObject(key);
            if (parent.has("default")) return parent.getJSONObject("default");
        } catch (JSONException e) {
            Log.e(TAG, "resolveWithFallback: parse error for key=" + key, e);
        }
        return null;
    }

    private SubscriptionPopupType getPopupTypeFromScreen(String screenName) {
        if (StringUtils.isEmpty(screenName)) return SubscriptionPopupType.UNKNOWN;
        try {
            return SubscriptionPopupType.valueOf(screenName.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return SubscriptionPopupType.UNKNOWN;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Analytics / utilities
    // ─────────────────────────────────────────────────────────────────────────

    private void facebookLogPurchaseEvent(String subscriptionId, double amount) {
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "INR");
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product");
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, subscriptionId);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT,
                "[{'id': '" + subscriptionId + "', 'quantity': 1}]");
        logger.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, amount, params);
    }

    private Map<String, String> withNavFrom(@Nullable Map<String, String> params) {
        if (params == null) params = new HashMap<>();
        params.put("navigation_from", navigationFrom);
        return params;
    }

    private void logFirebaseEventsForPaymentTransaction(String screen,
                                                        Map<String, String> params) {
        if (params == null) params = new HashMap<>();
        params.put("screen", screen);
        params.put("navigation_from", navigationFrom);
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.payment_transaction_initiated.getActionName(), params);
    }

    private void logPaymentFailure(String source, String reason) {
        Map<String, String> p = new HashMap<>();
        p.put("Subscription_plan_Id", planId);
        p.put("amount", String.valueOf(resolvedAmount));
        p.put("action", "PaymentError QR = " + source + " | " + reason);
        p.put("navigation_from", navigationFrom);
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.Payment_failed.getActionName(), p);
        BranchAnalyticsHelper.logPaymentFailed(this, planId, resolvedAmount, "RAZOR PAY QR CODE",
                "PaymentError QR = " + source + " | " + reason);
    }

    private boolean isActivityAlive() {
        return !isDestroyed() && !isFinishing();
    }

    public static String getAlphaNumericString(int n) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++)
            sb.append(chars.charAt((int) (chars.length() * Math.random())));
        return sb.toString();
    }

    private void setQrSectionTexts(SubscriptionPlanMetaData meta) {
        String language = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String title = null;
        String subtitle = null;

        boolean hasApiTitle = meta.getQrCodeSectionTitle() != null
                && !meta.getQrCodeSectionTitle().trim().isEmpty();
        boolean hasApiSubtitle = meta.getQrCodeSectionSubTitle() != null
                && !meta.getQrCodeSectionSubTitle().trim().isEmpty();

        if (hasApiTitle) title = meta.getQrCodeSectionTitle();
        if (hasApiSubtitle) subtitle = meta.getQrCodeSectionSubTitle();

        if (title == null || subtitle == null) {
            try {
                String remoteJson = FirebaseRemoteConfig.getInstance()
                        .getString("qr_section_detail_" + language);
                if (!remoteJson.trim().isEmpty()) {
                    JSONObject json = new JSONObject(remoteJson);
                    if (title == null) {
                        String rt = json.optString("title", "");
                        if (!rt.trim().isEmpty()) title = rt;
                    }
                    if (subtitle == null) {
                        String rs = json.optString("subtitle", "");
                        if (!rs.trim().isEmpty()) subtitle = rs;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (title == null) title = getString(R.string.scan_qr_code_and_pay);
        if (subtitle == null) subtitle = getString(R.string.scan_qr_code_and_pay_subtitle);

        qrSectionTitle.setText(title);
        qrSectionSubtitle.setText(subtitle);
    }
}