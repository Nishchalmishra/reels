package arivihan.technologies.doubtbuzzter2.activity.video_screens.pdf;

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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.PdfViewerActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;

public class MockDemoActivity extends AppCompatActivity
        implements Player.Listener, EdgeToEdgeHelper.SelfManagedInsets {
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

    String pdfUrl, pdfUrlForOffline;
    String title;
    String pdfPath;
    String notesType, subjectName, noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_demo);
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


        videoUrl = MyApplication.getFirebaseRemoteConfig().getString("notes_instructions_video");
        pdfUrl = getIntent().getStringExtra("pdfUrl");
        pdfUrlForOffline = getIntent().getStringExtra("pdfUrlForOffline");
        pdfPath = getIntent().getStringExtra("pdfPath");
        title = getIntent().getStringExtra("pdfTitle");
        notesType = getIntent().getStringExtra("notesType");
        subjectName = getIntent().getStringExtra("subjectName");
        noteId = getIntent().getStringExtra("noteId");


        Log.d("MockDemoActivity", "videoUrl: " + videoUrl);
        Log.d("MockDemoActivity", "pdfUrl: " + pdfUrl);
        Log.d("MockDemoActivity", "pdfPath: " + pdfPath);
        Log.d("MockDemoActivity", "title: " + title);


        videoFullScreenPlayer = findViewById(R.id.video_player);
        progressBar = findViewById(R.id.progress_bar);
        close = findViewById(R.id.btn_close);
        close.setOnClickListener(view -> {
            // Close Clicked Event created
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.mock_demo_close_clicked.getActionName());
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
            Map<String, String> params = new HashMap<>();
            params.put("isMuted", String.valueOf(muted));
            // Volume Button Clicked Event created
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.mock_demo_volume_clicked.getActionName(), params);
        });
        startNow = findViewById(R.id.start_now);
        startNow.setVisibility(View.VISIBLE);
        testId = getIntent().getStringExtra("Id");
        category = getIntent().getStringExtra("category");
        isAttempted = getIntent().getBooleanExtra("isAttempted", false);
        startNow.setOnClickListener(v -> {

            // Start Now Button Clicked Event created
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.mock_demo_start_now_clicked.getActionName());
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
//        if (StringUtils.isNotEmpty(pdfUrl)) {
            this.startActivity(
                    new Intent(this, PdfViewerActivity.class)
                            .putExtra("pdfUrl", pdfUrl)
                            .putExtra("pdfPath", pdfPath)
                            .putExtra("pdfUrlForOffline", pdfUrlForOffline)
                            .putExtra("notesType", notesType)
                            .putExtra("subjectName", subjectName)
                            .putExtra("pdfTitle", title != null ? title : this.getString(R.string.pdf_document))
                            .putExtra("noteId", noteId)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );
            finish();
//        }
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
