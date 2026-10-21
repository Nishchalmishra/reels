package arivihan.technologies.doubtbuzzter2.customView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.apache.commons.lang3.StringUtils;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.ToDoListSteps;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ToDoBottomSheetDialog extends BottomSheetDialogFragment implements Player.Listener {

    ImageView thumbnailView;
    PlayerView bottomSheetPlayerView;
    ExoPlayer player;
    CardView createNow;
    ToDoBottomSheetListener bottomSheetListener;
    ImageView close;
    String thumbnail;
    String videoUrl;
    Context context;
    long videoStartingTime;
    ProgressBar spinnerVideoDetails;
    Handler mHandler = new Handler();
    Runnable mRunnable;
    ShimmerFrameLayout imageShimmer;


    public ToDoBottomSheetDialog(Context context, ToDoBottomSheetListener bottomSheetListener, String thumbnailUrlKey, String videoUrlKey) {
        this.bottomSheetListener = bottomSheetListener;
        this.context = context;
        this.thumbnail = thumbnailUrlKey;
        this.videoUrl = videoUrlKey;
        MyApplication.saveData(PreferenceKeys.VIDEO_STARTING.getKey(),0);
    }

    public ToDoBottomSheetDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.to_do_list_popup, container, false);
        spinnerVideoDetails = view.findViewById(R.id.spinnerVideoDetails);
        createNow = view.findViewById(R.id.create_now_card);
        thumbnailView = view.findViewById(R.id.thumbnail);
        bottomSheetPlayerView = view.findViewById(R.id.video_player);
        createNow.setOnClickListener(view1 -> bottomSheetListener.onCreateNowClick());
        close = view.findViewById(R.id.close_btn);
        thumbnailView.setOnClickListener(v -> {
            openFullscreenPlayer();
        });
        bottomSheetPlayerView.setOnClickListener(v -> {
            openFullscreenPlayer();
        });
        close.setOnClickListener(v -> {
            dismissAll();
            bottomSheetListener.onCloseClick();
        });
        imageShimmer = view.findViewById(R.id.image_shimmer);
        imageShimmer.startShimmer();
        downloadThumbnail();
        initializePlayer();
        mRunnable = this::startPlayer;
        mHandler.postDelayed(mRunnable, 2000);
        return view;
    }

    private void openFullscreenPlayer(){
        if(StringUtils.isNotEmpty(videoUrl)) {
            Intent intent = new Intent(context, ExoPlayerActivity.class);
            intent.putExtra("video_uri", videoUrl);
            if (player != null)
                videoStartingTime = player.getCurrentPosition();
            intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), videoStartingTime);
            intent.putExtra("gotoToDoListCreation", true);
            startActivity(intent);
        }
        pausePlayer();
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
    }



    private void downloadThumbnail() {
        if (StringUtils.isNotEmpty(thumbnail)) {
            Glide.with(context)
                    .load(thumbnail)
                    .into(thumbnailView);
            imageShimmer.stopShimmer();
            imageShimmer.setVisibility(View.GONE);
            thumbnailView.setVisibility(View.VISIBLE);
        }
    }

    private void startPlayer() {
        if (StringUtils.isNotEmpty(videoUrl)) {
            if (videoUrl != null && player != null)
                buildMediaSource(Uri.parse(videoUrl));
        }
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(context).build();
            bottomSheetPlayerView.setPlayer(player);
        }
    }

    private void buildMediaSource(Uri hlsUri) {
        MediaItem mediaItem = new MediaItem.Builder().setUri(hlsUri).setMimeType(MimeTypes.APPLICATION_M3U8).build();
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
        player.addListener(this);
        player.seekTo(videoStartingTime);
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        switch (player.getPlaybackState()) {
            case Player.STATE_BUFFERING:
                spinnerVideoDetails.setVisibility(View.VISIBLE);
                bottomSheetPlayerView.setVisibility(View.GONE);
                thumbnailView.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
                pausePlayer();
                bottomSheetPlayerView.setVisibility(View.GONE);
                thumbnailView.setVisibility(View.VISIBLE);
                gotoToDoListCreation();
                break;
            case Player.STATE_IDLE:
                videoStartingTime = player.getCurrentPosition();
                break;
            case Player.STATE_READY:
                spinnerVideoDetails.setVisibility(View.GONE);
                thumbnailView.setVisibility(View.GONE);
                bottomSheetPlayerView.setVisibility(View.VISIBLE);
            default:
                break;
        }
    }

    private void gotoToDoListCreation(){
        dismiss();
        MyApplication.navigateToTodoSteps(context);
//        context.startActivity(new Intent(context, ToDoListSteps.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        try{
            videoStartingTime = MyApplication.getLongData(PreferenceKeys.VIDEO_STARTING.getKey());
            if(videoStartingTime>0)
                player.seekTo(videoStartingTime);
        }catch (Exception ignored){}
        resumePlayer();
    }

    private void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    public void releasePlayer() {
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

    @Override
    public void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dismissAll();
    }

    public void dismissAll(){
        releasePlayer();
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
    }

    public interface ToDoBottomSheetListener {
        void onCloseClick();

        void onCreateNowClick();
    }

}
