package arivihan.technologies.doubtbuzzter2.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.asat.ASATActivity;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ASATVideoFragment extends Fragment implements Player.Listener {

    private static final String TAG = ASATVideoFragment.class.getSimpleName();

    private ExoPlayer player;
    private static final int REQUEST_CODE = 100;
    private long videoStartingTime;
    private boolean videoEnded;
    private ActivityResultLauncher<Intent> playerLauncher;
    private Handler handler;
    // UI
    ShimmerFrameLayout imageShimmer;
    private PlayerView videoPlayer;
    private ImageView thumbnail;
    ImageView volume;
    ProgressBar spinnerVideoDetails;

    // Data
    String thumbnailUrl, url;
    boolean isResultScreen = false;
    private boolean hasPreviewPlaybackStarted = false;
    private long previewVideoBufferingStartTime = -1;
    private long BUFFERING_THRESHOLD_MS = 4000; // 4 seconds
    private Handler bufferHandler = new Handler(Looper.getMainLooper());
    private Runnable checkBufferingRunnable;

    public ASATVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        videoStartingTime = data.getLongExtra(PreferenceKeys.VIDEO_STARTING.getKey(), 0);
                        videoEnded = data.getBooleanExtra("video_ended", false);

                        if (player != null) {
                            if (player.getCurrentMediaItem() == null) {
                                if (StringUtils.isNotEmpty(url))
                                    buildMediaSource(Uri.parse(url));
                            } else {
                                player.seekTo(0);
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_a_s_a_t_video, container, false);

        url = getArguments().getString("url","");
        thumbnailUrl = getArguments().getString("thumbnail","");
        isResultScreen = getArguments().getBoolean(ASATActivity.ASAT_SCREEN_TYPE_RESULT, false);

        long timeOutThreshold = MyApplication.getFirebaseRemoteConfig().getLong("ASAT_VIDEO_BUFFERING_THRESHOLD_MS");
        BUFFERING_THRESHOLD_MS = timeOutThreshold!=0?timeOutThreshold:4000;

        initializeViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        resumePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        pausePlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer();
    }
    void initializeViews(View view) {
        spinnerVideoDetails = view.findViewById(R.id.spinnerVideoDetails);
        imageShimmer = view.findViewById(R.id.image_shimmer);
        thumbnail = view.findViewById(R.id.thumbnail_image);
        volume = view.findViewById(R.id.volume);
        videoPlayer = view.findViewById(R.id.player_view);

        thumbnail.setOnClickListener(v -> openPlayer());
        videoPlayer.setOnClickListener(v -> openPlayer());

        volume.setOnClickListener(v -> {
            if (player != null) {
                if (player.getVolume()==0f) {
                    volume.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.volume, null));
                    player.setVolume(1f);
                } else {
                    volume.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.mute, null));
                    player.setVolume(0f);
                }
            }
        });

        imageShimmer.startShimmer();
        setVideo();
    }

    private void openPlayer(){
        if(StringUtils.isNotEmpty(url)) {
            Intent intent = new Intent(requireContext(), ExoPlayerActivity.class);
            intent.putExtra("video_uri", url);
            if (player != null)
                intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), player.getCurrentPosition());
            else if (videoEnded)
                intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), 0);
            else
                intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), videoStartingTime);

            playerLauncher.launch(intent);

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_video_click.getActionName());
        }
    }

    private void setVideo() {
        if (StringUtils.isNotEmpty(thumbnailUrl)) {
            Glide.with(thumbnail)
                    .load(thumbnailUrl)
                    .error(R.drawable.microlecture_default_thumbnail)
                    .timeout((int) BUFFERING_THRESHOLD_MS)
                    .into(thumbnail);
            imageShimmer.stopShimmer();
            imageShimmer.setVisibility(View.GONE);
            thumbnail.setVisibility(View.VISIBLE);

            if(StringUtils.isNotEmpty(url)) {
                startHandler();
            }
        }
    }

    private void startHandler() {
        handler = new Handler();
        Runnable runnable = this::startPlayer;
        handler.postDelayed(runnable, 2000);
    }

    private void startPlayer() {
        if (StringUtils.isNotEmpty(url))
            buildMediaSource(Uri.parse(url));
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(requireContext()).build();
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
        } catch (Exception e) {

        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        switch (player.getPlaybackState()) {
            case Player.STATE_BUFFERING:

                if (!hasPreviewPlaybackStarted) {
                    // First-time buffering before playback starts
                    thumbnail.setVisibility(View.VISIBLE);
                    videoPlayer.setVisibility(View.GONE);
                    volume.setVisibility(View.GONE);
                } else {
                    // Buffering after playback started
                    thumbnail.setVisibility(View.GONE);
                    videoPlayer.setVisibility(View.VISIBLE);
                    volume.setVisibility(View.VISIBLE);
                }
                spinnerVideoDetails.setVisibility(View.VISIBLE);
//                videoPlayer.setVisibility(View.GONE);
//                volume.setVisibility(View.GONE);
//                thumbnail.setVisibility(View.VISIBLE);

                previewVideoBufferingStartTime = System.currentTimeMillis();
                startBufferingTimeoutCheck();
                break;
            case Player.STATE_ENDED:
                releasePlayer();
                videoPlayer.setVisibility(View.GONE);
                volume.setVisibility(View.GONE);
                thumbnail.setVisibility(View.VISIBLE);

                previewVideoBufferingStartTime = -1;
                stopBufferingTimeoutCheck();
                break;
            case Player.STATE_IDLE:
                videoStartingTime = player.getCurrentPosition();

                previewVideoBufferingStartTime = -1;
                stopBufferingTimeoutCheck();
                break;
            case Player.STATE_READY:
                hasPreviewPlaybackStarted = true;
                spinnerVideoDetails.setVisibility(View.GONE);
                thumbnail.setVisibility(View.GONE);
                videoPlayer.setVisibility(View.VISIBLE);
                volume.setVisibility(View.VISIBLE);
                if(videoStartingTime>0) {
                    player.seekTo(videoStartingTime);
                    videoStartingTime=-1;
                }

                previewVideoBufferingStartTime = -1;
                stopBufferingTimeoutCheck();
                break;
            default:
                break;
        }
    }

    private void startBufferingTimeoutCheck() {
        stopBufferingTimeoutCheck();
        checkBufferingRunnable = () -> {
            if (previewVideoBufferingStartTime > 0) {
                long elapsed = System.currentTimeMillis() - previewVideoBufferingStartTime;
                if (elapsed >= BUFFERING_THRESHOLD_MS) {
                    releasePlayer();
                    spinnerVideoDetails.setVisibility(View.GONE);
                    videoPlayer.setVisibility(View.GONE);
                    volume.setVisibility(View.GONE);
                    thumbnail.setVisibility(View.VISIBLE);
                }
            }
        };
        bufferHandler.postDelayed(checkBufferingRunnable, BUFFERING_THRESHOLD_MS);
    }

    private void stopBufferingTimeoutCheck() {
        if (checkBufferingRunnable != null) {
            bufferHandler.removeCallbacks(checkBufferingRunnable);
            checkBufferingRunnable = null;
        }
    }

    private void releasePlayer() {
        if (player != null) {
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

}