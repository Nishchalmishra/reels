package arivihan.technologies.doubtbuzzter2.customView;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyPopupData;

public class HalfYearlyBottomSheetDialog extends BottomSheetDialogFragment implements Player.Listener {

    ImageView thumbnailView;
    PlayerView bottomSheetPlayerView;
    ExoPlayer player;
    CardView startNow;
    ImageView close;
    String thumbnail;
    String videoUrl;
    Context context;
    long videoStartingTime;
    ProgressBar spinnerVideoDetails;
    Handler mHandler = new Handler();
    Runnable mRunnable;
    ShimmerFrameLayout imageShimmer;
    HalfYearlyBottomSheetListener bottomSheetListener;
    int ongoingDay;
    TextView title;
    TextView text1;
    TextView text2;
    TextView text3;
    TextView text4;
    boolean fromHomeScreen;
    HalfYearlyPopupData popupData;

    public HalfYearlyBottomSheetDialog(Context context, HalfYearlyBottomSheetListener bottomSheetListener,
                                       String thumbnailUrlKey, String videoUrlKey, int ongoingDay, boolean fromHomeScreen,
                                       HalfYearlyPopupData popupData) {
        this.bottomSheetListener = bottomSheetListener;
        this.context = context;
        this.thumbnail = thumbnailUrlKey;
        this.videoUrl = videoUrlKey;
        this.ongoingDay=ongoingDay;
        this.fromHomeScreen= fromHomeScreen;
        this.popupData = popupData;
    }

    public HalfYearlyBottomSheetDialog() {
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
        View view = inflater.inflate(R.layout.hal_yearly_bottom_sheet, container, false);
        title = view.findViewById(R.id.title);
        text1 = view.findViewById(R.id.text1);
        text2 = view.findViewById(R.id.text2);
        text3 = view.findViewById(R.id.text3);
        text4 = view.findViewById(R.id.text4);
        spinnerVideoDetails = view.findViewById(R.id.spinnerVideoDetails);
        startNow = view.findViewById(R.id.create_now_card);
        thumbnailView = view.findViewById(R.id.thumbnail);
        bottomSheetPlayerView = view.findViewById(R.id.video_player);
        close = view.findViewById(R.id.close_btn);
        close.setOnClickListener(v -> {
            bottomSheetListener.onCloseButtonClick();
        });
        imageShimmer = view.findViewById(R.id.image_shimmer);
        imageShimmer.startShimmer();
        if(popupData!=null) {
            if(startNow!=null) {
                startNow.setOnClickListener(view1 -> bottomSheetListener.onStartNowClick(popupData.getClassName(), popupData.getActivityParams()));
            }
            if (fromHomeScreen)
                changeText();
            else
                title.setText(String.format(popupData.getTitle(), ongoingDay - 1));

            downloadThumbnail();
            initializePlayer();
            mRunnable = this::startPlayer;
            mHandler.postDelayed(mRunnable, 2000);
        }
        return view;
    }

    private void changeText(){
        if(popupData!=null) {
            title.setText(popupData.getTitle());
            text1.setText(popupData.getMessageOne());
            text2.setVisibility(View.GONE);
            text3.setText(popupData.getMessageThree());
            text4.setText(popupData.getMessageFour());
        }
    }

    private void downloadThumbnail() {
        if (StringUtils.isNotEmpty(popupData.getThumbnail())) {
            Glide.with(context)
                    .load(popupData.getThumbnail())
                    .into(thumbnailView);
            imageShimmer.stopShimmer();
            imageShimmer.setVisibility(View.GONE);
            thumbnailView.setVisibility(View.VISIBLE);
        }
    }

    private void startPlayer() {
        if (StringUtils.isNotEmpty(popupData.getVideoUrl())) {
            if (popupData.getVideoUrl() != null && player != null)
                buildMediaSource(Uri.parse(popupData.getVideoUrl()));
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

    @Override
    public void onResume() {
        super.onResume();
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
        releasePlayer();
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
    }

    public interface HalfYearlyBottomSheetListener {
        void onCloseButtonClick();
        void onStartNowClick(String className, String activityParams);
    }

}
