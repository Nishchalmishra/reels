package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_SOLUTION_FEEDBACK;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.audio.AudioRendererEventListener;
import androidx.media3.exoplayer.metadata.MetadataOutput;
import androidx.media3.exoplayer.text.TextOutput;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.exoplayer.upstream.BandwidthMeter;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.exoplayer.video.VideoRendererEventListener;
import androidx.media3.ui.PlayerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import androidx.media3.ui.PlayerControlView;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.VideoPlayerConfig;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class ExoPlayerActivity extends LocaleActivity implements Player.Listener,
        EdgeToEdgeHelper.SelfManagedInsets {
    public static final String TAG = ExoPlayerActivity.class.getSimpleName();
    public static final String CLASS_NAME = ExoPlayerActivity.class.getName();
    public static final String KEY_VIDEO_URI = "video_uri";

    private PlayerView videoFullScreenPlayer;
    private ProgressBar spinnerVideoDetails;
    private ImageView imageViewExit;
    private ImageView badgeImageView;
    private ImageView badgeImageOutside;
    private boolean isFromChapterDetail;
    private long videoStartingTime;
    private long videoEndingTime;



    /*@BindView(R.id.videoFullScreenPlayer)
    PlayerView videoFullScreenPlayer;
    @BindView(R.id.spinnerVideoDetails)
    ProgressBar spinnerVideoDetails;
    @BindView(R.id.imageViewExit)
    ImageView imageViewExit;*/

    String videoUri;
    String doubt, doubtId;
    ExoPlayer player;
    Handler mHandler;
    Runnable mRunnable;
    private static final long BACK_PRESS_THRESHOLD = 2000;
    private long backPressedTime;

    ConstraintLayout parentLayout;
    long resumePosition = 0;
    NetworkStatusLiveData networkStatusLiveData;

    public static Intent getStartIntent(Context context, String videoUri) {
        Intent intent = new Intent(context, ExoPlayerActivity.class);
        intent.putExtra(KEY_VIDEO_URI, videoUri);
        return intent;
    }

    @androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideSystemBarsForPlayback();
        setContentView(R.layout.activity_exo_player);
//        ButterKnife.bind(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + BACK_PRESS_THRESHOLD > System.currentTimeMillis()) {
                    Intent data = new Intent();
                    data.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), player.getCurrentPosition());
                    MyApplication.saveData(PreferenceKeys.VIDEO_STARTING.getKey(), player.getCurrentPosition());
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    Toast.makeText(ExoPlayerActivity.this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        });

        parentLayout = findViewById(R.id.parent_layout);
        if (getIntent() != null) {
            isFromChapterDetail = getIntent().getBooleanExtra("isFromChapterDetail", false);
        }
        if (isFromChapterDetail) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        videoFullScreenPlayer = findViewById(R.id.videoFullScreenPlayer);
        videoFullScreenPlayer.setKeepScreenOn(true);

        // The controller root stays full bleed so the bottom bar background spans the whole
        // screen; the insets are applied to the bar's contents and to the badge instead.
        View bottomControlBar = videoFullScreenPlayer.findViewById(R.id.bottom_control_bar);
        View badgeView = videoFullScreenPlayer.findViewById(R.id.badge_image_view);
        int barBasePaddingLeft = bottomControlBar != null ? bottomControlBar.getPaddingLeft() : 0;
        int barBasePaddingRight = bottomControlBar != null ? bottomControlBar.getPaddingRight() : 0;
        int barBasePaddingBottom = bottomControlBar != null ? bottomControlBar.getPaddingBottom() : 0;
        ViewGroup.MarginLayoutParams badgeParams =
                badgeView != null ? (ViewGroup.MarginLayoutParams) badgeView.getLayoutParams() : null;
        int badgeBaseMarginEnd = badgeParams != null ? badgeParams.getMarginEnd() : 0;
        int badgeBaseMarginBottom = badgeParams != null ? badgeParams.bottomMargin : 0;
        ViewCompat.setOnApplyWindowInsetsListener(parentLayout, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                            | WindowInsetsCompat.Type.displayCutout());
            if (bottomControlBar != null) {
                bottomControlBar.setPadding(barBasePaddingLeft + bars.left,
                        bottomControlBar.getPaddingTop(),
                        barBasePaddingRight + bars.right,
                        barBasePaddingBottom + bars.bottom);
            }
            if (badgeParams != null) {
                badgeParams.setMarginEnd(badgeBaseMarginEnd + bars.right);
                badgeParams.bottomMargin = badgeBaseMarginBottom + bars.bottom;
                badgeView.setLayoutParams(badgeParams);
            }
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(parentLayout);
        spinnerVideoDetails = findViewById(R.id.spinnerVideoDetails);
        imageViewExit = (ImageView) findViewById(R.id.imageViewExit);
        badgeImageView = findViewById(R.id.badge_image_view);
        badgeImageOutside = findViewById(R.id.badge_image_outside);

        mHandler = new Handler();


        imageViewExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String badgeImageUrl = getIntent().getStringExtra("badge_image_url");
        if (badgeImageUrl != null && !badgeImageUrl.isEmpty()) {
            // Inside controller layout: visible behind the controls when controller is shown
            badgeImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(badgeImageUrl).into(badgeImageView);
            // Outside controller: visible on top of video when controller is hidden
            Glide.with(this).load(badgeImageUrl).into(badgeImageOutside);
            videoFullScreenPlayer.setControllerVisibilityListener(
                    (PlayerControlView.VisibilityListener) visibility ->
                            badgeImageOutside.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE)
            );
        }

        if (getIntent().hasExtra(KEY_VIDEO_URI)) {
            videoUri = getIntent().getStringExtra(KEY_VIDEO_URI);
        }
        if (getIntent().hasExtra("doubt")) {
            doubt = getIntent().getStringExtra("doubt");
        }

        if (getIntent().hasExtra("doubtId")) {
            doubtId = getIntent().getStringExtra("doubtId");
        }
        if (getIntent().hasExtra(PreferenceKeys.VIDEO_STARTING.getKey())) {
            videoStartingTime = getIntent().getLongExtra(PreferenceKeys.VIDEO_STARTING.getKey(), 0);
            resumePosition = videoStartingTime;
            Log.i("VDIEORESPONSEOPTION", "RESUMTTIME :: " + resumePosition);
        }
        if (getIntent().hasExtra("video_ending")) {
            videoEndingTime = getIntent().getLongExtra("video_ending", 0);
        }

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    spinnerVideoDetails.setVisibility(View.GONE);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    CommonMethods.showPositiveSnackbar(parentLayout, getString(R.string.InternetConnectionVideo));
                    setUp();
                }
            }
            else{
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.NoInternetConnectionVideo));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

        setUp();

        startHandler();

    }

    /** Keep landscape playback immersive after rotation or a transient system-bar reveal. */
    private void hideSystemBarsForPlayback() {
        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
        insetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBarsForPlayback();
        }
    }

    private void setUp() {
        initializeButton();
        initializePlayer();
        if (videoUri == null) {
            return;
        }
        buildMediaSource(Uri.parse(videoUri));
    }

    private void startHandler() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (videoEndingTime != 0L && player.getCurrentPosition() >= videoEndingTime) {
                    Toast.makeText(getApplicationContext(), "Hope your doubt solved.", Toast.LENGTH_LONG).show();
                    player.stop();
                } else {
                    mHandler.postDelayed(mRunnable, 200);
                }
            }
        };

        mHandler.postDelayed(mRunnable, 200);
    }

    private void initializeButton() {
        ImageButton likeButton = findViewById(R.id.like);
        ImageButton dislikeButton = findViewById(R.id.dislike);
        RelativeLayout doubtChatButton = findViewById(R.id.doubt_chat_btn);


        if (StringUtils.isEmpty(doubt)) {
            likeButton.setVisibility(View.GONE);
            dislikeButton.setVisibility(View.GONE);
            doubtChatButton.setVisibility(View.GONE);
        }

        likeButton.setOnClickListener((view -> {
            updateFeedback(doubtId, true);
            Toast.makeText(this, "Thanks for your feedback.", Toast.LENGTH_SHORT).show();
        }));

        String whatsapp_number = MyApplication.getFirebaseRemoteConfig().getString("whatsapp_number");
        String whatsapp_message = MyApplication.getFirebaseRemoteConfig().getString("whatsapp_message_template");
        dislikeButton.setOnClickListener((view -> {
            updateFeedback(doubtId, false);
            if (!getIntent().hasExtra("doubtId")) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("disliked", true);
                returnIntent.putExtra("response_id", getIntent().getStringExtra("response_id"));
                setResult(501, returnIntent);
                finish();
            } else {
                openWhatsApp(whatsapp_number, whatsapp_message.replace("{}", doubt));
            }
        }));

        doubtChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().hasExtra("doubtId")) {
                    Intent intent = new Intent(ExoPlayerActivity.this, ChatSupportActivity.class);
                    intent.putExtra("doubtId", doubtId);
                    intent.putExtra("doubt", doubt);
                    intent.putExtra("type", "new_chat");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(ExoPlayerActivity.this, DoubtChatScreen.class);
                    intent.putExtra("chatSessionId", getIntent().getStringExtra("chat_session_id"));
                    startActivity(intent);
                }
            }
        });
    }

    private void updateFeedback(String doubtId, boolean liked) {
        Map<String, Object> map = new HashMap<>();
        map.put("doubtId", doubtId);
        map.put("liked", liked);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(DOUBT_SOLUTION_FEEDBACK, map), null,
                response -> {

                }, new VolleyGlobalErrorListener(ExoPlayerActivity.this) {
            @Override
            public void postError(VolleyError error) {
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }


    private void openWhatsApp(String number, String message) {

        try {
            //PackageManager pm = getPackageManager();

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            sendIntent.putExtra("jid", number + "@s.whatsapp.net"); //phone number without "+" prefix
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
//            if (sendIntent.resolveActivity(pm) != null) {
//               // startActivity(sendIntent);
//            }else {
//                Toast.makeText(this, "No Whatsapp Found", Toast.LENGTH_SHORT).show();
//            }
        } catch (Exception e) {
            Toast.makeText(this, "No Whatsapp Found", Toast.LENGTH_SHORT).show();
        }

    }

/*    @OnClick(R.id.imageViewExit)
    public void onViewClicked() {
        finish();
    }*/


    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            videoFullScreenPlayer.setPlayer(player);
        }

    }

    private void buildMediaSource(Uri mUri) {
        MediaItem mediaItem = new MediaItem.Builder().setUri(mUri).build();
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
        player.addListener(this);
    }

    private void buildHlsMediaSource(Uri uri) {
        // Measures bandwidth during playback. Can be null if not required.
//        // Uri videoPath = RawResourceDataSource.buildRawResourceUri(R.raw.demo_lecture);
//        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        // Produces DataSource instances through which media data is loaded.
//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
//                Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter);
//        // This is the MediaSource representing the media to be played.
//        HlsMediaSource.Factory factory = new HlsMediaSource.Factory(dataSourceFactory);
//        HlsMediaSource mediaSource = factory.createMediaSource(uri);
//        // Prepare the player with the source.
//        player.prepare(mediaSource);
//        player.setPlayWhenReady(true);
//        player.addListener(this);
//        player.seekTo(resumePosition);
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    private void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        resumePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        reportPersonalisedPathProgress();
        releasePlayer();
    }

    /**
     * Report the watched percentage of this lecture to the personalised learning path.
     * No-op unless this activity was launched from the path (nodeId extra present).
     */
    private void reportPersonalisedPathProgress() {
        try {
            if (player != null) {
                arivihan.technologies.doubtbuzzter2.helper.PersonalisedPathProgressManager
                        .reportVideoFromActivity(this, player.getCurrentPosition(), player.getDuration());
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying ) {
        switch (player.getPlaybackState()) {
            case Player.STATE_BUFFERING:
                spinnerVideoDetails.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
                if(getIntent().getBooleanExtra("gotoToDoListCreation",false)) {
//                    startActivity(new Intent(this, ToDoListSteps.class));
                    MyApplication.navigateToTodoSteps(this);
                }else{
                    Intent intent = new Intent();
                    intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), player.getCurrentPosition());
                    intent.putExtra("video_ended", true);
                    setResult(RESULT_OK, intent);
                    finish();
                    // Activate the force enable
                }
                break;
            case Player.STATE_IDLE:
                resumePosition = player.getCurrentPosition();
                break;
            case Player.STATE_READY:
                spinnerVideoDetails.setVisibility(View.GONE);
                if(resumePosition>0){
                  player.seekTo(resumePosition);
                  resumePosition=-1;
                }
                break;
            default:
                // status = PlaybackStatus.IDLE;
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }


    // onBackPressed logic moved to OnBackPressedCallback in onCreate

}
