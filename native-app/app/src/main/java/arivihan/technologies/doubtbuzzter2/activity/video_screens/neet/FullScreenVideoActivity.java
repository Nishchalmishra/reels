package arivihan.technologies.doubtbuzzter2.activity.video_screens.neet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.ui.PlayerView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.PdfViewerActivity;
import arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewNeetWeakTopicSelectionActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;

public class FullScreenVideoActivity extends AppCompatActivity
        implements Player.Listener, EdgeToEdgeHelper.SelfManagedInsets {

    private static final String TAG = FullScreenVideoActivity.class.getSimpleName();
    ExoPlayer player;
    private PlayerView videoFullScreenPlayer;
    String videoUrl;
    CardView close, volumeCard, startNow;
    ImageView volume;
    boolean muted;
    String testId;
    String category;
    boolean isAttempted;
    ProgressBar progressBar;
    long startPos = 0;
    NetworkStatusLiveData networkStatusLiveData;
    private boolean closeActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_video);
        EdgeToEdgeHelper.applyArtworkEdgeToEdge(
                findViewById(android.R.id.content),
                new View[] {findViewById(R.id.start_now)},
                new View[] {findViewById(R.id.btn_close), findViewById(R.id.btn_volume)});
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                redirectToNewActivity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        videoUrl = getIntent().getStringExtra("videoUrl");
        closeActivity = getIntent().getBooleanExtra("close", true);

        Log.d("MockDemoActivity", "videoUrl: " + videoUrl);

        if(StringUtils.isEmpty(videoUrl)) {
            videoUrl = MyApplication.getFirebaseRemoteConfig().getString("neet_weak_topic_selection_video_url");
        }

        videoFullScreenPlayer = findViewById(R.id.video_player);
        progressBar = findViewById(R.id.progress_bar);
        close = findViewById(R.id.btn_close);
        close.setOnClickListener(view -> {
            redirectToNewActivity();
        });
        volumeCard = findViewById(R.id.btn_volume);
        volume = findViewById(R.id.volume);
        volumeCard.setOnClickListener(v -> {
            if (player != null) {
                if (muted) {
                    volume.setImageDrawable(getResources().getDrawable(R.drawable.volume_on));
                    player.setVolume(1f);
                } else {
                    volume.setImageDrawable(getResources().getDrawable(R.drawable.volume_off));
                    player.setVolume(0f);
                }
            }
            muted = !muted;
        });
        startNow = findViewById(R.id.start_now);
        startNow.setVisibility(View.VISIBLE);
        testId = getIntent().getStringExtra("Id");
        category = getIntent().getStringExtra("category");
        isAttempted = getIntent().getBooleanExtra("isAttempted", false);
        startNow.setOnClickListener(v -> {
            redirectToNewActivity();
        });
        videoFullScreenPlayer.setKeepScreenOn(true);

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

        setUp();
    }


    private void launch() {
        if (getIntent() != null && getIntent().getBooleanExtra("startTest", false))
            redirectToNewActivity();
        else
            getOnBackPressedDispatcher().onBackPressed();
    }


    private void redirectToNewActivity() {

        if (!closeActivity) {
            this.startActivity(
                    new Intent(this, WebViewNeetWeakTopicSelectionActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );
        }
        finish();
    }

    private void setUp() {
        initializePlayer();
        if (videoUrl == null) {
            return;
        }
        buildMediaSource(Uri.parse(videoUrl));
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            videoFullScreenPlayer.setPlayer(player);
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
        player.addListener(this);
        player.seekTo(startPos);
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        switch (player.getPlaybackState()) {

            case Player.STATE_BUFFERING:
                break;
            case Player.STATE_ENDED:
                redirectToNewActivity();
                break;
            case Player.STATE_IDLE:
                startPos = player.getCurrentPosition();
                progressBar.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_READY:
                videoFullScreenPlayer.setVisibility(View.VISIBLE);
                if (category != null)
                    startNow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayer();
    }

    private void pausePlayer() {
        if (player != null) {
            startPos = player.getCurrentPosition();
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (player == null) {
            setUp();
        } else {
            if (player.getPlaybackState() == Player.STATE_ENDED ||
                    player.getPlaybackState() == Player.STATE_IDLE) {
                buildMediaSource(Uri.parse(videoUrl)); // re-load media
            }
            player.setPlayWhenReady(true);
        }
    }

}
