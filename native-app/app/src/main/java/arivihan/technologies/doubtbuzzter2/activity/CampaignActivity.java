package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SUBSCRIPTION_V2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.V2_GET_SUBSCRIPTION;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.RazorpayPaymentGatewayActivity;
import arivihan.technologies.doubtbuzzter2.ReelsActivity;
import arivihan.technologies.doubtbuzzter2.adapter.CampaignFeatureAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.RewardsTopPerformersAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ToppersAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.fragments.LeaderboardFragment;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.RewardsPerformerDto;
import arivihan.technologies.doubtbuzzter2.model.RewardsScreenDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlans;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPostModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPreModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionRenewalReward;
import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class CampaignActivity extends BaseActivity
        implements ToppersAdapter.OnTopperClickListener,
        RewardsTopPerformersAdapter.OnPerformerVideoClickListener,
        Player.Listener, EdgeToEdgeHelper.SelfManagedInsets {

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final String BUNDLE_SUBSCRIPTION_DATA = "subscription_lock_data";
    private static final String TAG = CampaignActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 100;
    private static final String KEY_VIDEO_URI = "video_uri";
    private static final String KEY_PAYMENT_TYPE = "key_payment_type";
    private static final String PAYMENT_QR = "QR";
    private static final String PAYMENT_GATEWAY = "GATEWAY";

    // ── Layout views ──────────────────────────────────────────────────────────
    private ConstraintLayout parentLayout;
    private ConstraintLayout bottomLayout;
    private ShimmerFrameLayout imageShimmer;
    private ShimmerFrameLayout dataShimmer;
    private LinearLayout dataLayout;
    private LinearLayout finalPriceLayout;

    // ── Video views ───────────────────────────────────────────────────────────
    private PlayerView videoPlayer;
    private ImageView thumbnail;
    private ImageView volume;
    private ImageView unnati_icon;
    private ProgressBar spinnerVideoDetails;

    // ── Subscription / plan views ─────────────────────────────────────────────
    private TextView selectedCostPrice;
    private TextView selectedDiscountPrice;
    private TextView selectedSubscriptionTitle;
    private CardView joinNow;
    private TextView campaignTitle;

    // ── Features RecyclerView ─────────────────────────────────────────────────
    private RecyclerView featuresRv;
    private CampaignFeatureAdapter subscriptionFeatureAdapter;
    private ArrayList<String> subscriptionFeatures;

    // ── Toppers / performers ──────────────────────────────────────────────────
    private RecyclerView toppersRv;
    private ToppersAdapter toppersAdapter;
    private final ArrayList<ToppersHomeScreenModel> toppersList = new ArrayList<>();

    private RecyclerView rewardsPerformersRv;
    private RewardsTopPerformersAdapter rewardsPerformersAdapter;

    private TextView toppersLayoutTitle;
    private TextView toppersLayoutSubTitle;
    private TextView leaderboard_title;

    // ── State ─────────────────────────────────────────────────────────────────
    private SubscriptionModel subscriptionModel;
    private SubscriptionPlans selectedSubscription;
    private SubscriptionRenewalReward subscriptionRenewalReward;
    private Gson gson;
    private String selectedPlanId;
    private String videoUrl;
    private String thumbnailCacheUrl;
    private long videoStartingTime;
    private boolean videoEnded;
    private boolean videoPlayed;
    private boolean dataLoaded;
    private boolean isPayingThroughQRCode;
    private boolean isFirstResume = true;
    private RedirectionSource redirectionSource;

    // ── Player ────────────────────────────────────────────────────────────────
    private ExoPlayer player;
    private Handler handler;

    // ── Misc ──────────────────────────────────────────────────────────────────
    private IntentFilter intentFilter;
    private NetworkStatusLiveData networkStatusLiveData;
    private ActivityResultLauncher<Intent> qrPaymentLauncher;

    // =========================================================================
    // Lifecycle
    // =========================================================================


    private static final String PREFS_PRE_CALL = "pre_call_prefs";
    private static final String KEY_PRE_RESPONSE = "pre_call_response_";   // + userId_planId
    private static final String KEY_PRE_TIMESTAMP = "pre_call_timestamp_"; // + userId_planId
    private static final long PRE_CALL_CACHE_MS = 24 * 60 * 60 * 1000L;



    /** The banner fills the top of the window, so only the bottom bar and the mute toggle move. */
    private void applyEdgeToEdgeInsets() {
        EdgeToEdgeHelper.applyArtworkEdgeToEdge(
                findViewById(R.id.main),
                new View[] {findViewById(R.id.bottom_layout)},
                new View[] {findViewById(R.id.volume)});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign);
        applyEdgeToEdgeInsets();

        gson = new Gson();
        subscriptionModel = new SubscriptionModel();

        Object sourceExtra = getIntent().getSerializableExtra(SubscriptionNavigationHelper.EXTRA_SOURCE);
        redirectionSource = null;
        if (sourceExtra instanceof RedirectionSource) {
            redirectionSource = (RedirectionSource) sourceExtra;
        }

        initializeViews();
        setSubscriptionFeatures();
//        setupToppersRecyclerView();
        setupRewardsPerformersRecyclerView();
        handleShowPopupIntent();
        registerQrPaymentLauncher();

        intentFilter = new IntentFilter(BroadcastStringForAction);

        getPlanData();
        getRewardsScreenData();
        setupJoinNowButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstResume) {
            isFirstResume = false;
            return;
        }

        if (MyApplication.getBoolData("other_payment")) {
            MyApplication.saveData("other_payment", false);
            isPayingThroughQRCode = false;
            safeCallBuyNowPreCall();
        } else if (videoPlayer.getVisibility() == View.VISIBLE) {
            resumePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) handler.removeCallbacksAndMessages(null);
        pausePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================

    private void initializeViews() {
        parentLayout = findViewById(R.id.main);
        bottomLayout = findViewById(R.id.bottom_layout);
        spinnerVideoDetails = findViewById(R.id.spinnerVideoDetails);
        imageShimmer = findViewById(R.id.image_shimmer);
        dataShimmer = findViewById(R.id.data_shimmer);
        dataLayout = findViewById(R.id.data_layout);
        finalPriceLayout = findViewById(R.id.final_price_layout);

        imageShimmer.startShimmer();
        dataShimmer.startShimmer();

        // Video
        thumbnail = findViewById(R.id.thumbnail_image);
        thumbnail.setOnClickListener(v -> openPlayerFullscreen());

        videoPlayer = findViewById(R.id.player_view);
        videoPlayer.setOnClickListener(v -> openPlayerFullscreen());

        volume = findViewById(R.id.volume);
        unnati_icon = findViewById(R.id.unnati_icon);
        volume.setOnClickListener(v -> toggleVolume());

        // Subscription info
        selectedCostPrice = findViewById(R.id.selected_cost_price);
        selectedDiscountPrice = findViewById(R.id.selected_discount_price);
        selectedSubscriptionTitle = findViewById(R.id.selected_subscription_title);
        joinNow = findViewById(R.id.join_campaign_button);
        campaignTitle = findViewById(R.id.unnati_titles);

        // Features
        featuresRv = findViewById(R.id.feature_rv);

        // Toppers / performers labels
        toppersLayoutTitle = findViewById(R.id.toppers_title);
        toppersLayoutSubTitle = findViewById(R.id.toppers_sub_title);
        leaderboard_title = findViewById(R.id.leaderboard_title);

        // Topper horizontal list (existing ToppersAdapter)
        toppersRv = findViewById(R.id.toppers_rv);

        // Rewards performers horizontal list (new adapter)
        rewardsPerformersRv = findViewById(R.id.rewards_performers_rv);

        setupNetworkObserver();
    }

    private void setupNetworkObserver() {
        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData("connectionConnected")) {
                    CommonMethods.showPositiveSnackbar(parentLayout,
                            getString(R.string.InternetConnection));
                    MyApplication.saveData("connectionConnected", true);
                    MyApplication.saveData("connectionDisconnected", false);
                    if (!dataLoaded) getPlanData();
                    if (!videoPlayed && spinnerVideoDetails.getVisibility() == View.VISIBLE)
                        startPlayer();
                }
            } else {
                if (!MyApplication.getBoolData("connectionDisconnected")) {
                    CommonMethods.showNegativeSnackbar(parentLayout,
                            getString(R.string.NoInternetConnection));
                    MyApplication.saveData("connectionConnected", false);
                    MyApplication.saveData("connectionDisconnected", true);
                }
            }
        });
    }

    private void setSubscriptionFeatures() {
        subscriptionFeatures = new ArrayList<>();
        subscriptionFeatureAdapter = new CampaignFeatureAdapter(subscriptionFeatures, this);
        featuresRv.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        featuresRv.setAdapter(subscriptionFeatureAdapter);
    }

    private void setupToppersRecyclerView() {
        toppersAdapter = new ToppersAdapter(this, toppersList, this);
        toppersRv.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        toppersRv.setAdapter(toppersAdapter);
    }

    private void setupRewardsPerformersRecyclerView() {
        rewardsPerformersAdapter = new RewardsTopPerformersAdapter(this, this, new ArrayList<>());
        rewardsPerformersRv.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rewardsPerformersRv.setAdapter(rewardsPerformersAdapter);
    }

    private void handleShowPopupIntent() {
        boolean showPopup = getIntent().getBooleanExtra(
                "show_course_information_pop_up_on_home_screen", false);
        if (showPopup) {
            MyApplication.saveData("show_course_information_pop_up_on_home_screen", true);
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.wclm_sheet_join_karein_btn_click.getActionName());
        }
    }

    private void registerQrPaymentLauncher() {
        qrPaymentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        isPayingThroughQRCode = false;
                        MyApplication.saveData("other_payment", false);
                        safeCallBuyNowPreCall();
                    }
                });
    }

    // =========================================================================
    // API — Rewards screen (features + performers + leaderboard + video header)
    // =========================================================================

//    private void getRewardsScreenData() {
//        MyJsonObjectRequest request = new MyJsonObjectRequest(
//                Request.Method.GET,
//                UrlRoutes.GET_REWARDS_SCREEN_DATA,
//                null,
//                response -> {
//                    try {
//                        JSONObject data = response.getJSONObject("data");
//                        RewardsScreenDto dto = gson.fromJson(data.toString(), RewardsScreenDto.class);
//                        if (dto == null) return;
//
//                        applyHeaderSection(dto.getHeaderSection());
//                        applyFeatureSection(dto.getFeatureSection());
//                        applyTopPerformersSection(dto.getTopPerformersSection());
//                        applyLeaderboardSection(dto.getLeaderboardSection());
//
//                    } catch (Exception e) {
//                        Log.e(TAG, "getRewardsScreenData parse error: " + e.getMessage());
//                    }
//                },
//                new VolleyGlobalErrorListener(this) {
//                    @Override
//                    public void postError(VolleyError error) {
//                        Log.e(TAG, "getRewardsScreenData error: " + error.getMessage());
//                    }
//                });
//        request.setTag("REWARDS_SCREEN_DATA");
//        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
//    }


    private void getRewardsScreenData() {
        Log.d(TAG, "getRewardsScreenData: initiating request to " + UrlRoutes.GET_REWARDS_SCREEN_DATA);

        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                UrlRoutes.GET_REWARDS_SCREEN_DATA,
                null,
                response -> {
                    Log.d(TAG, "getRewardsScreenData: response received");
                    try {
                        JSONObject data = response.getJSONObject("data");
                        Log.d(TAG, "getRewardsScreenData: raw data = " + data);

                        RewardsScreenDto dto = gson.fromJson(data.toString(), RewardsScreenDto.class);
                        if (dto == null) {
                            Log.w(TAG, "getRewardsScreenData: dto is null after parsing, aborting");
                            return;
                        }
                        Log.d(TAG, "getRewardsScreenData: dto parsed successfully");

                        Log.d(TAG, "getRewardsScreenData: applying header section");
                        applyHeaderSection(dto.getHeaderSection());

                        Log.d(TAG, "getRewardsScreenData: applying feature section");
                        applyFeatureSection(dto.getFeatureSection());

                        Log.d(TAG, "getRewardsScreenData: applying top performers section");
                        applyTopPerformersSection(dto.getTopPerformersSection());

                        Log.d(TAG, "getRewardsScreenData: applying leaderboard section");
                        applyLeaderboardSection(dto.getLeaderboardSection());

                        Log.d(TAG, "getRewardsScreenData: all sections applied successfully");

                    } catch (Exception e) {
                        Log.e(TAG, "getRewardsScreenData: parse error: " + e.getMessage(), e);
                    }
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "getRewardsScreenData: volley error: " + error.getMessage(), error);
                    }
                });

        request.setTag("REWARDS_SCREEN_DATA");
        Log.d(TAG, "getRewardsScreenData: adding request to queue with tag REWARDS_SCREEN_DATA");
        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void applyHeaderSection(RewardsScreenDto.HeaderSection h) {
        if (h == null || !h.isVisible()) return;
        updateCampaignTitle(h.getTitleText());
        Glide.with(this).load(h.getTitleIconUrl()).into(unnati_icon);
        loadThumbnail(h.getThumbnailUrl());
        videoUrl = h.getVideoUrl();
        if (!videoPlayed) {
            initializePlayer();
            startHandler();
            videoPlayed = true;
        }
    }

    private void applyFeatureSection(RewardsScreenDto.FeatureSection f) {
        if (f == null || !f.isVisible()) return;
        subscriptionFeatures.clear();
        if (f.getFeaturesList() != null) {
            for (String feature : f.getFeaturesList()) {
                subscriptionFeatures.add(feature.trim());
            }
        }
        subscriptionFeatureAdapter.notifyDataSetChanged();
    }

    private void applyTopPerformersSection(RewardsScreenDto.TopPerformersSection tp) {
        if (tp == null || !tp.isVisible()) return;

        toppersLayoutTitle.setText(tp.getTitleText());
        toppersLayoutSubTitle.setText(tp.getSubTitleText());

        if (tp.getPerformers() != null) {
            rewardsPerformersAdapter.updateData(tp.getPerformers());
            rewardsPerformersRv.setVisibility(tp.getPerformers().isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void applyLeaderboardSection(RewardsScreenDto.LeaderboardSection lb) {
        Log.d(TAG, "applyLeaderboardSection: " + lb);
        if (lb == null || !lb.isVisible()) return;

        LeaderboardFragment frag = (LeaderboardFragment)
                getSupportFragmentManager().findFragmentById(R.id.toppers_fragment_container);
        leaderboard_title.setText(lb.getTitleText());

        if (frag == null) {
            Log.d(TAG, "applyLeaderboardSection: fragment not found, creating new instance");
            frag = LeaderboardFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.toppers_fragment_container, frag)
                    .commitNow();
        } else {
            Log.d(TAG, "applyLeaderboardSection: reusing existing fragment instance");
        }

        frag.updateLeaderboard(lb);
    }


    // =========================================================================
    // API — Subscription plan
    // =========================================================================

    private void getPlanData() {
        Map<String, Object> params = new HashMap<>();
        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(V2_GET_SUBSCRIPTION, params),
                null,
                response -> {
                    Log.d(TAG, "getPlanData: " + response);
                    subscriptionModel = gson.fromJson(response.toString(),
                            new TypeToken<SubscriptionModel>() {
                            }.getType());
                    dataLoaded = true;

                    if (subscriptionModel.getPlans() != null) {
                        for (SubscriptionPlans plan : subscriptionModel.getPlans()) {
                            if (plan.isSelected()) {
                                selectedSubscription = plan;
                                selectedPlanId = plan.getPlanId();
                                renderPlanPrice(plan);
                                break;
                            }
                        }
                    }

                    dataShimmer.stopShimmer();
                    dataShimmer.setVisibility(View.GONE);
                    dataLayout.setVisibility(View.VISIBLE);
                    finalPriceLayout.setVisibility(View.VISIBLE);
                    joinNow.setEnabled(true);
                    bottomLayout.setVisibility(View.VISIBLE);
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Toast.makeText(CampaignActivity.this,
                                error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        request.setTag("PLAN_DATA_REQUEST");
        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    // =========================================================================
    // UI helpers
    // =========================================================================

    private void updateCampaignTitle(String html) {
        if (html == null) html = "";
        Spanned formatted = android.os.Build.VERSION.SDK_INT
                >= android.os.Build.VERSION_CODES.N
                ? Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
                : Html.fromHtml(html);
        campaignTitle.setText(formatted);
    }

    private void loadThumbnail(String url) {
        thumbnailCacheUrl = url;
        if (StringUtils.isNotEmpty(url) && !isFinishing() && !isDestroyed()) {
            Glide.with(this).load(url).into(thumbnail);
            imageShimmer.stopShimmer();
            imageShimmer.setVisibility(View.GONE);
            thumbnail.setVisibility(View.VISIBLE);
        }
    }

    private void renderPlanPrice(SubscriptionPlans plan) {
        // Apply renewal coupon discount if present
        if (subscriptionModel.getSubscriptionRenewalReward() != null) {
            subscriptionRenewalReward = subscriptionModel.getSubscriptionRenewalReward();
            String couponText = subscriptionRenewalReward.getCouponCodeText();
            if (couponText != null && !couponText.isEmpty()) {
                int discount = subscriptionRenewalReward.getDiscountPercentage();
                int base = Integer.parseInt(plan.getFinalPrice());
                int discounted = base - ((base * discount) / 100);
                plan.setFinalPrice(String.valueOf(discounted));
            }
        }

        selectedCostPrice.setText(
                String.format(getString(R.string.price_format), plan.getCostPrice()));
        selectedCostPrice.setPaintFlags(
                selectedCostPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        selectedDiscountPrice.setText(
                String.format(getString(R.string.price_format), plan.getFinalPrice()));
        selectedSubscriptionTitle.setText(plan.getName());
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

    // =========================================================================
    // Video player
    // =========================================================================

    private void startHandler() {
        handler = new Handler();
        handler.postDelayed(this::startPlayer, 2000);
    }

    private void startPlayer() {
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

            case Player.STATE_ENDED:
                releasePlayer();
                videoPlayer.setVisibility(View.GONE);
                volume.setVisibility(View.GONE);
                thumbnail.setVisibility(View.VISIBLE);
                break;

            case Player.STATE_IDLE:
                videoStartingTime = player.getCurrentPosition();
                break;

            default:
                break;
        }
    }

    private void resumePlayer() {
        if (player != null) player.setPlayWhenReady(true);
    }

    private void pausePlayer() {
        if (player != null) player.setPlayWhenReady(false);
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
        Intent intent = new Intent(this, ExoPlayerActivity.class);
        intent.putExtra("video_uri", videoUrl);
        intent.putExtra("video_starting",
                player != null ? player.getCurrentPosition()
                        : videoEnded ? 0L : videoStartingTime);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            videoStartingTime = data.getLongExtra("video_starting", 0);
            videoEnded = data.getBooleanExtra("video_ended", false);
        }
        if (requestCode == REQUEST_CODE && player != null) {
            if (player.getCurrentMediaItem() == null && StringUtils.isNotEmpty(videoUrl))
                buildMediaSource(Uri.parse(videoUrl));
            else
                player.seekTo(0);
        }
    }

    // =========================================================================
    // Topper click callbacks
    // =========================================================================

    /**
     * Called by ToppersAdapter (existing competitive toppers strip).
     */
    @Override
    public void onTopperClick(String videoUrl) {
        openReelsActivity(videoUrl, "UnnatiScreen");
    }

    /**
     * Called by RewardsTopPerformersAdapter — interface implemented here
     * so the adapter stays decoupled from Activity internals.
     */
    @Override
    public void onPerformerVideoClick(String videoUrl) {
        openReelsActivity(videoUrl, "RewardsScreen");
    }

    private void openReelsActivity(String videoUrl, String openFrom) {
        if (StringUtils.isEmpty(videoUrl)) return;
        try {
            Intent intent = new Intent(this, ReelsActivity.class);
            intent.putExtra(KEY_VIDEO_URI, videoUrl);
            Map<String, String> params = new HashMap<>();
            params.put("video_url", videoUrl);
            params.put("open_from", openFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.champions_of_arivihan_clicked.getActionName(), params);
            startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    // =========================================================================
    // Payment flow
    // =========================================================================

    private void setupJoinNowButton() {
        joinNow.setOnClickListener(v -> {
            try {
                if (isFreeSubscription()) {
                    if (CommonMethods.isOnline(this)) {
                        safeCallBuyNowPreCall();
                    } else {
                        CommonMethods.showNegativeSnackbar(parentLayout,
                                getString(R.string.error_no_internet_connection));
                    }
                } else {
                    showPaymentOptionsDialog();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void showPaymentOptionsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.subscription_payment_option_dialog_layout);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawable(
                getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        dialog.findViewById(R.id.close_btn).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.pay_with_qr_btn).setOnClickListener(v -> {
            isPayingThroughQRCode = true;
            dialog.dismiss();
            MyApplication.saveData(KEY_PAYMENT_TYPE, PAYMENT_QR);
            safeCallBuyNowPreCall();
        });

        dialog.findViewById(R.id.pay_with_payment_gateway_btn).setOnClickListener(v -> {
            isPayingThroughQRCode = false;
            dialog.dismiss();
            MyApplication.saveData(KEY_PAYMENT_TYPE, PAYMENT_GATEWAY);
            safeCallBuyNowPreCall();
        });

        dialog.show();
    }

    private void safeCallBuyNowPreCall() {
        try {
            buyNowPreCall();
        } catch (Exception e) {
            Log.e(TAG, "buyNowPreCall error", e);
            Toast.makeText(this,
                    "An error occurred from our end, please try again later",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean isFreeSubscription() {
        try {
            return Integer.parseInt(selectedSubscription.getFinalPrice()) == 0;
        } catch (Exception ex) {
            return false;
        }
    }


    private void buyNowPreCall() throws JSONException {
        if (selectedSubscription == null) {
            Log.e(TAG, "selectedSubscription is null — cannot proceed");
            return;
        }

        String userId = MyApplication.getStringData("userId");
        String planId = selectedSubscription.getPlanId();
        String cacheKey = userId + "_" + planId;

        String cachedJson = MyApplication.getStringData(KEY_PRE_RESPONSE + cacheKey);
        long   cachedTime = MyApplication.getLongData(KEY_PRE_TIMESTAMP + cacheKey);
        boolean isValid   = cachedJson != null
                && (System.currentTimeMillis() - cachedTime) < PRE_CALL_CACHE_MS;

        if (true) {
            Log.i(TAG, "buyNowPreCall: using cached response for " + cacheKey);
            try {
                handlePreCallResponse(new JSONObject(cachedJson));
            } catch (Exception e) {
                Log.w(TAG, "Cache parse failed, falling through to network: " + e.getMessage());
                MyApplication.removeString(KEY_PRE_RESPONSE + cacheKey);
                MyApplication.removeString(KEY_PRE_TIMESTAMP + cacheKey);
                buyNowPreCall();
            }
            return;
        }

        SubscriptionPreModel pre = new SubscriptionPreModel();
        pre.setUserId(userId);
        pre.setSubscriptionPlanId(planId);
        pre.setOrderId(getAlphaNumericString(8)); // frozen in cache on success
        pre.setTotalAmount(Long.parseLong(selectedSubscription.getFinalPrice()));
        pre.setRedirectionFrom(redirectionSource != null ? redirectionSource.getFormattedValue() : "UNKNOWN");

        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.POST,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, new HashMap<>()),
                new JSONObject(gson.toJson(pre)),
                response -> {
                    if (isFinishing() || isDestroyed()) return;

                    MyApplication.saveData(KEY_PRE_RESPONSE + cacheKey, response.toString());
                    MyApplication.saveData(KEY_PRE_TIMESTAMP + cacheKey, System.currentTimeMillis());

                    try {
                        handlePreCallResponse(response);
                    } catch (Exception e) {
                        Log.e(TAG, "buyNowPreCall response error: " + e.getMessage(), e);
                        Toast.makeText(this, getString(R.string.server_error),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Toast.makeText(CampaignActivity.this,
                                "Payment Failed: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handlePreCallResponse(JSONObject response) throws Exception {
        Log.i("CAMPAIGN_ACTICITY", "handlePreCallResponse: " + response.getJSONObject("data"));
        UserSubscriptionDto userSub = gson.fromJson(
                response.getJSONObject("data").toString(),
                new TypeToken<UserSubscriptionDto>() {}.getType());

        if (isFreeSubscription()) {
            buyNowPostCall(getAlphaNumericString(8), userSub);
        } else if (isPayingThroughQRCode) {
            launchQrPayment(userSub);
        } else {
            launchRazorpay(Integer.parseInt(selectedSubscription.getFinalPrice()),
                    userSub.getOrderId(), userSub);
        }
    }

    private void buyNowPostCall(String transactionId,
                                UserSubscriptionDto userSub) throws JSONException {
        SubscriptionPostModel post = new SubscriptionPostModel();
        post.setPaymentStatus(PaymentStatus.SUCCESS.name());
        post.setInvoiceId(userSub.getInvoiceId());
        post.setTransactionId(transactionId);
        post.setSubscriptionId(userSub.getId());
        post.setInternalMetrics(true);
        post.setSecretKey(MyApplication.getFirebaseRemoteConfig()
                .getString("internal_metrics_secret_key"));

        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.PUT,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, new HashMap<>()),
                new JSONObject(gson.toJson(post)),
                response -> {
                    try {
                        if ((boolean) response.get("success")) {
                            new Handler().postDelayed(() -> finishWithResult(true), 1000);
                        } else {
                            CommonMethods.showNegativeSnackbar(parentLayout,
                                    "Sorry we faced some technical problem, contact us.");
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.server_error),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        CommonMethods.showNegativeSnackbar(parentLayout,
                                error.getMessage() != null
                                        ? error.getMessage()
                                        : getString(R.string.something_went_wrong));
                    }
                });
        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void launchQrPayment(UserSubscriptionDto userSub) {
        Intent intent = new Intent(this, QRCodePaymentActivity.class);
        intent.putExtra("orderId", userSub.getOrderId());
        intent.putExtra("subscriptionPlanId", selectedSubscription.getPlanId());
        intent.putExtra("transactionId", "");
        intent.putExtra("costPrice", selectedSubscription.getCostPrice());
        intent.putExtra("planName", selectedSubscription.getName());
        intent.putExtra("userSubscriptionDto", userSub);
        intent.putExtra("paymentAmount", Integer.parseInt(selectedSubscription.getFinalPrice()));
        intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, redirectionSource);
        logPaymentEvent("QRCodePaymentActivity",
                CommonMethods.convertToStringMap(CommonMethods.getIntentParams(intent)));
        qrPaymentLauncher.launch(intent);
    }

    private void launchRazorpay(int amount, String rzOrderId, UserSubscriptionDto userSub) {
        if (selectedSubscription == null) return;
        Intent intent = new Intent(this, RazorpayPaymentGatewayActivity.class);
        intent.putExtra("planId", selectedSubscription.getPlanId());
        intent.putExtra("orderId", rzOrderId);
        intent.putExtra("amount", String.valueOf(amount));
        intent.putExtra("userSubscriptionDto", userSub);
        intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, redirectionSource);
        logPaymentEvent("RazorpayPaymentGatewayActivity",
                CommonMethods.convertToStringMap(CommonMethods.getIntentParams(intent)));
        startActivity(intent);
    }

    private void finishWithResult(boolean isSubscribed) {
        Intent result = new Intent();
        result.putExtra("isSubscribed", isSubscribed);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private void logPaymentEvent(String mode, HashMap<String, String> params) {
        try {
            params.put("mode_of_payment", mode);
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.initiate_payment_transaction.getActionName(), params);
        } catch (Exception ignored) {
        }
    }

    // =========================================================================
    // Utility
    // =========================================================================

    public static String getAlphaNumericString(int n) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++)
            sb.append(chars.charAt((int) (chars.length() * Math.random())));
        return sb.toString();
    }
}