package arivihan.technologies.doubtbuzzter2.activity.competitive;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.ui.PlayerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewByUrlActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;

public class FeatureInfoVideoActivity extends AppCompatActivity implements Player.Listener {

    private ExoPlayer player;
    private PlayerView videoFullScreenPlayer;
    private String videoUrl;
    private CardView close, startNow;
    private View progressFill;
    private TextView bottomButtonText;
    private RelativeLayout rl_parent_layout;
    private LinearProgressIndicator progressIndicator;
    private NetworkStatusLiveData networkStatusLiveData;
    private RelativeLayout rl_loading;

    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private final int UPDATE_INTERVAL = 100; // ms
    private boolean isProgressUpdating = false;
    private boolean videoCompleted = false;
    private int playCount = 0;

    private SharedPreferences prefs;
    private static final String PREF_NAME = "FeatureVideoPrefs";
    private static final String KEY_PLAY_COUNT = "play_count";
    private int backPressCount = 0;
    boolean isVideoCompleted = false;
    private  RelativeLayout bottomLayout;
    private boolean hideBottomLayout=false;

    private boolean tracked25 = false;
    private boolean tracked50 = false;
    private boolean tracked75 = false;
    private int lastWatchedPercent = 0;
    private String subjectId;
    private String subjectName;

    private String navigationFrom;
    public static enum NavigationFromEnum {
        QR_SCREEN,
        RAZORPAY_SCREEN,
        NEET_HOME_SCREEN,
        SUBSCRIPTION_PAYMENT_SCREEN
    }

    public static final String bundleNavigationFrom = "navigationFrom";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feature_info_video_activity_layout);
        rl_parent_layout = findViewById(R.id.rl_parent_layout);
        playCount = MyApplication.getIntDataFromDoNotDeleteSharedPreference(PreferenceKeys.FEATURE_INFO_VIDEO_COUNT_PREFIX.getKey() + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        Log.d("playCount", "onCreate: " + playCount);
        videoFullScreenPlayer = findViewById(R.id.video_player);
        bottomLayout=findViewById(R.id.Bottom_layout);
        hideBottomLayout=getIntent().getBooleanExtra("hide_bottom_layout",false);
        if(hideBottomLayout){
            bottomLayout.setVisibility(View.GONE);
        }else {
            bottomLayout.setVisibility(View.VISIBLE);
        }
        close = findViewById(R.id.btn_close);
        startNow = findViewById(R.id.start_now);
        progressFill = findViewById(R.id.progress_fill);
        rl_loading = findViewById(R.id.rl_loading);

        if(hideBottomLayout){
            bottomLayout.setVisibility(View.GONE);
            close.setVisibility(View.GONE);
        }else {
            bottomLayout.setVisibility(View.VISIBLE);
            close.setVisibility(playCount == -1 ? View.GONE : View.VISIBLE);
        }
        videoUrl = getIntent().getStringExtra("videoUrl");
        if (StringUtils.isEmpty(videoUrl)) {
            videoUrl = MyApplication.getFirebaseRemoteConfig().getString("feature_info_video_url_v2");
        }

        navigationFrom = getIntent().getStringExtra(bundleNavigationFrom);

        subjectId = getIntent().getStringExtra("subject_id");
        subjectName = getIntent().getStringExtra("subject_name");
        Log.d("videoUrl", "onCreate: "+videoUrl);
        close.setOnClickListener(v -> {
            navigateToNextScreen();
        });
        bottomButtonText = findViewById(R.id.bottom_button_text);
        progressIndicator = findViewById(R.id.progress_indicator);
//        startNow.setClickable(false);
        progressIndicator.setProgress(0);

        setupNetworkObserver();
        setUp();

        // Handle back press behavior
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleCustomBackPress();
            }
        });

        close.setOnClickListener(view -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_close_click.getActionName());
            navigateToNextScreen();
        });

        startNow.setOnClickListener(v -> {
            CommonMethods.showPositiveSnackbar(rl_parent_layout, getString(R.string.video_completion_message), 2000);
        });

        HashMap<String, String> params = new HashMap<>();
        //params.put("purchase_type", "51");
        //params.put("destination_screen", "welcome_reel_screen");
        params.put("previousScreen", navigationFrom);
        params.put("videoURL", videoUrl);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_viewed.getActionName(), params);
    }

    private void handleCustomBackPress() {
        Log.d("playcount", "handleCustomBackPress: " + backPressCount);
        backPressCount++;

        if (backPressCount == 1) {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_snackbar_show.getActionName());
            CommonMethods.showNegativeSnackbar(
                    rl_parent_layout,
                    "Wapas jaane ke liye do baar back button par click karein"
            );

        } else if (backPressCount == 2) {

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_exit.getActionName());
            boolean hideBottomLayout = getIntent().getBooleanExtra("hide_bottom_layout", false);

            if (hideBottomLayout) {
                Intent intent = new Intent(this, HomeScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (playCount == -1) {
                finish();

            } else {
                navigateToNextScreen();
            }

            String key = PreferenceKeys.FEATURE_INFO_VIDEO_COUNT_PREFIX.getKey()
                    + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());

            MyApplication.saveDataForDoNotDeleteSharedPreference(
                    key,
                    MyApplication.getIntDataFromDoNotDeleteSharedPreference(key) + 1
            );

        } else {
            navigateToNextScreen();
        }
    }


    private void navigateToNextScreen() {

        if (!isVideoCompleted) {
            HashMap<String, String> params = new HashMap<>();
            params.put("previousScreen", navigationFrom);
            params.put("watch_Percent", String.valueOf(lastWatchedPercent));
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_progress_milestone.getActionName(), params);
        }
        HashMap<String, String> params = new HashMap<>();

        if(hideBottomLayout){
            Intent intent = new Intent(this, HomeScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            params.put("previousScreen", navigationFrom);
            params.put("navigatedScreen", "home_screen");
            startActivity(intent);
            finish();
        }
        else{
            MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.FEATURE_INFO_VIDEO_COUNT_PREFIX.getKey() + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()), 2);
            if (isVideoCompleted) {
                MyApplication.saveData(PreferenceKeys.HIDE_MINI_VIDEO_IN_ADAPTIVE_SCREEN.getKey(), true);
            }
            Intent intent = new Intent(this, WebViewByUrlActivity.class);
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            params.put("previousScreen", navigationFrom);
            params.put("subjectId", subjectId);
            params.put("subjectName", subjectName);
            params.put("navigatedScreen", "personalized_path");
            startActivity(intent);
            finish();
        }
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_navigated_screen.getActionName(),params);
    }

    /**
     * NETWORK MONITOR
     **/
    private void setupNetworkObserver() {
        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    CommonMethods.showPositiveSnackbar(startNow, getString(R.string.InternetConnectionVideo));
                    setUp();
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(startNow, getString(R.string.NoInternetConnectionVideo));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });
    }

    /**
     * PLAYER SETUP
     **/
    private void setUp() {
        initializePlayer();
        if (videoUrl != null) buildMediaSource(Uri.parse(videoUrl));
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            videoFullScreenPlayer.setPlayer(player);
            player.addListener(this);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void buildMediaSource(Uri mUri) {
        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(mUri));
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
        videoFullScreenPlayer.setVisibility(View.GONE);
        rl_loading.setVisibility(View.VISIBLE);
    }

    /**
     * SYNC PROGRESS WITH VIDEO
     **/
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                long currentPos = player.getCurrentPosition();
                long duration = player.getDuration();

                if (duration > 0) {
                    int progressPercent = (int) ((currentPos * 1000) / duration);
                    lastWatchedPercent = (int) ((currentPos * 100) / duration);

                    if (!tracked25) {

                        if (lastWatchedPercent >= 25) {
                            tracked25 = true;
                            HashMap<String, String> params = new HashMap<>();
                            params.put("previousScreen", navigationFrom);
                            params.put("milestone", "25");
                            //params.put("screen_name", "welcome_reel_screen");
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_progress_milestone.getActionName(), params);
                        }
                    } else if (!tracked50) {

                        if (lastWatchedPercent >= 50) {
                            tracked50 = true;
                            HashMap<String, String> params = new HashMap<>();
                            params.put("previousScreen", navigationFrom);
                            params.put("milestone", "50");
                            //params.put("screen_name", "welcome_reel_screen");
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_progress_milestone.getActionName(), params);
                        }
                    } else if (!tracked75) {

                        if (lastWatchedPercent >= 75) {
                            tracked75 = true;
                            HashMap<String, String> params = new HashMap<>();
                            params.put("previousScreen", navigationFrom);
                            params.put("milestone", "75");
                            //params.put("screen_name", "welcome_reel_screen");
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_progress_milestone.getActionName(), params);
                        }
                    }
                    progressIndicator.setMax(1000);
                    progressIndicator.setProgress(progressPercent);

                    int totalWidth = startNow.getWidth();
                    if (totalWidth > 0) {
                        progressFill.getLayoutParams().width = (int) (totalWidth * (progressPercent / 1000f));
                        progressFill.requestLayout();
                    }
                }
            }
            if (isProgressUpdating) {
                progressHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        }
    };

    private void startProgressUpdates() {
        if (!isProgressUpdating) {
            isProgressUpdating = true;
            progressHandler.post(progressRunnable);
        }
    }

    private void stopProgressUpdates() {
        isProgressUpdating = false;
        progressHandler.removeCallbacks(progressRunnable);
    }

    /**
     * PLAYER LISTENER
     **/
    @Override
    public void onPlaybackStateChanged(int state) {
        switch (state) {
            case Player.STATE_READY:
                videoFullScreenPlayer.setVisibility(View.VISIBLE);
                rl_loading.setVisibility(View.GONE);
                HashMap<String, String> params = new HashMap<>();
                params.put("previousScreen", navigationFrom);
                //params.put("screen_name", "welcome_reel_screen");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_auto_started.getActionName(), params);
                startProgressUpdates();
                break;

            case Player.STATE_BUFFERING:
                stopProgressUpdates();
                break;

            case Player.STATE_ENDED:
                stopProgressUpdates();
                progressIndicator.setProgress(progressIndicator.getMax());
                progressFill.getLayoutParams().width = startNow.getWidth();
                progressFill.requestLayout();
//                startNow.setClickable(true);
                isVideoCompleted = true;
                params = new HashMap<>();
                params.put("previousScreen", navigationFrom);
                //params.put("screen_name", "welcome_reel_screen");
                params.put("video_completed", "true");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_completed.getActionName(), params);
                startNow.setAlpha(1f);
                navigateToNextScreen();
                close.setVisibility(View.VISIBLE);
                break;
        }
    }


    private void savePlayCount() {
        int newCount = playCount + 1;
        prefs.edit().putInt(KEY_PLAY_COUNT, newCount).apply();
        playCount = newCount;
    }

    /**
     * LIFECYCLE
     **/
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.setPlayWhenReady(false);
        stopProgressUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player == null) setUp();
        else player.setPlayWhenReady(true);
        startProgressUpdates();
    }

    @Override
    protected void onDestroy() {
        HashMap<String, String> params = new HashMap<>();
        params.put("previousScreen", navigationFrom);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.feat_info_va_reel_destroy.getActionName(),params);
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        stopProgressUpdates();
    }
}
