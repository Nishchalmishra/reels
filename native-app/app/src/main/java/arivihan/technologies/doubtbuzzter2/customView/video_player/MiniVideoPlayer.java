package arivihan.technologies.doubtbuzzter2.customView.video_player;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.ui.PlayerView;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.model.player.MiniPlayerPosition;
import arivihan.technologies.doubtbuzzter2.model.player.MiniVideoPlayerConfig;

public class MiniVideoPlayer extends FrameLayout {

    private final static String TAG = MiniVideoPlayer.class.getSimpleName();
    private PlayerView video_player;
    private ExoPlayer exoPlayer;
    private ProgressBar pb_progress_bar;
    private CardView cv_close, cv_volume, cv_resize, cv_start_now;
    private ImageView iv_volume, iv_resize;
    private FrameLayout mini_video_player_container;

    // Exo Player Controller
    private long startVideoPosition = 0;

    // Other
    private float dX, dY;
    private int screenWidth, screenHeight;
    private boolean isMuted = false;
    private boolean isFullScreen = false;
    private int originalWidthInPx;
    private int originalHeightInPx;

    public MiniVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.mini_video_player_layout, this, true);
        mini_video_player_container = findViewById(R.id.mini_video_player_container);
        video_player = findViewById(R.id.video_player);
        pb_progress_bar = findViewById(R.id.pb_progress_bar);
        cv_close = findViewById(R.id.cv_close);
        cv_volume = findViewById(R.id.cv_volume);
        cv_resize = findViewById(R.id.cv_resize);
        iv_volume = findViewById(R.id.iv_volume);
        iv_resize = findViewById(R.id.iv_resize);
        cv_start_now = findViewById(R.id.cv_start_now);

        mini_video_player_container.setVisibility(VISIBLE);
        cv_close.setVisibility(VISIBLE);
        cv_volume.setVisibility(VISIBLE);
        cv_start_now.setVisibility(GONE);
        cv_resize.setVisibility(VISIBLE);

        setupParentScreenSize(context);
        setupListeners();
    }

    private void setupParentScreenSize(Context context) {
        setScreenRatio(context);
        post(()->{
            setScreenRatio(context);
        });
    }

    private void setScreenRatio(Context context) {
        View parent = (View) getParent();
        if (parent != null && parent.getWidth() != 0 && parent.getHeight() != 0) {
            screenWidth = parent.getWidth();
            screenHeight = parent.getHeight();
        } else {
            setupScreenSize(context);
        }
    }

    private void setupScreenSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    private void setupListeners() {
        cv_close.setOnClickListener(v -> {
            try {
                closePlayer();

                Map<String, String> param = new HashMap<>();
                param.put("close_clicked", "true");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.to_do_mini_video.getActionName(), param);
            } catch (Exception ignored) {}
        });

        cv_volume.setOnClickListener(v -> {
            isMuted = !isMuted;
            if (exoPlayer != null) {
                exoPlayer.setVolume(isMuted ? 0f : 1f);
            }
            iv_volume.setImageResource(isMuted ? R.drawable.volume_off : R.drawable.volume_on);
        });

        cv_resize.setOnClickListener(v -> {
            int targetWidthPx, targetHeightPx;

            if (isFullScreen) {
                // Revert to original size
                targetWidthPx = originalWidthInPx;
                targetHeightPx = originalHeightInPx;
                iv_resize.setImageResource(R.drawable.maximize_video);
//                enableDrag(true); // ✅ Enable dragging in collapsed mode
//                restoreMargins(); // ✅ Restore original margins
            } else {
                // Fullscreen to parent size
                View parent = (View) getParent();
                targetWidthPx = parent.getWidth();
                targetHeightPx = parent.getHeight();
                iv_resize.setImageResource(R.drawable.minimize_video);
//                enableDrag(false); // 🚫 Disable drag in fullscreen
//                removeMargins(); // 🚫 Remove margins in fullscreen
            }

            animateResize(this, targetWidthPx, targetHeightPx);
//            scaleButtons(targetWidthPx);
            isFullScreen = !isFullScreen;
        });

        this.setOnTouchListener(this::handleDrag);
    }

    // method override needed to remove the warning on setOnTouchListener
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private boolean handleDrag(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                view.performClick();
                return true;

            case MotionEvent.ACTION_MOVE:
                float newX = event.getRawX() + dX;
                float newY = event.getRawY() + dY;

                int viewWidth = view.getWidth();
                int viewHeight = view.getHeight();

                int maxX = screenWidth - viewWidth;
                int maxY = screenHeight - viewHeight;

                newX = Math.max(0, Math.min(newX, maxX));
                newY = Math.max(0, Math.min(newY, maxY));

                view.setX(newX);
                view.setY(newY);

                return true;

            default:
                return false;
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void playVideo(MiniVideoPlayerConfig miniVideoPlayerConfig, String parentTag) {

        try {
//        setupParentScreenSize(getContext());
            int widthDp = miniVideoPlayerConfig.getWidthDp();
            int heightDp = miniVideoPlayerConfig.getHeightDp();
            String url = miniVideoPlayerConfig.getVideoUrl();

            cv_close.setVisibility(miniVideoPlayerConfig.getShowCloseButton() ? VISIBLE : GONE);
            cv_volume.setVisibility(miniVideoPlayerConfig.getShowVolumeButton() ? VISIBLE : GONE);
            cv_resize.setVisibility(miniVideoPlayerConfig.getShowResizeButton() ? VISIBLE : GONE);

//        setLayoutMarginGravity(miniVideoPlayerConfig);
            Log.d(TAG, "DTO : Width:" + widthDp + ", Height:" + heightDp);
            Log.d(TAG, "Parent : Width:" + screenWidth + ", Height:" + screenHeight);
            int widthPx, heightPx;

            if (widthDp > 0 && heightDp > 0) {
                widthPx = CommonMethods.dpToPx(getContext(), widthDp);
                heightPx = CommonMethods.dpToPx(getContext(), heightDp);
            } else {
                widthPx = widthDp > 0 ? CommonMethods.dpToPx(getContext(), widthDp) : (int) screenWidth / 3;
                heightPx = (int) (widthPx * (16f / 9f));
            }

            originalWidthInPx = widthPx;
            originalHeightInPx = heightPx;

            Log.d(TAG, "Original : Width:" + widthPx + ", Height:" + heightPx);
            ViewGroup.LayoutParams params = this.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(widthPx, heightPx);
            } else {
                params.width = widthPx;
                params.height = heightPx;
            }
            this.setLayoutParams(params);
            this.setVisibility(VISIBLE);

            scaleButtons(widthPx);
            pb_progress_bar.setVisibility(VISIBLE);

            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(getContext())
                    .setEnableDecoderFallback(true)
                    .setMediaCodecSelector(MediaCodecSelector.DEFAULT);

            exoPlayer = new ExoPlayer.Builder(getContext(), renderersFactory).build();
            video_player.setPlayer(exoPlayer);
            video_player.setUseController(false);

            DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(getContext());
            HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(url));

            exoPlayer.setMediaSource(mediaSource);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    switch (state) {
                        case Player.STATE_BUFFERING -> {

                        }
                        case Player.STATE_READY -> {
                            video_player.setVisibility(View.VISIBLE);
                            pb_progress_bar.setVisibility(View.GONE);
                        }
                        case Player.STATE_ENDED -> {
                            closePlayer();
                        }
                        case Player.STATE_IDLE -> {
                            startVideoPosition = exoPlayer.getCurrentPosition();
                            pb_progress_bar.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    closePlayer();
                }
            });

            exoPlayer.seekTo(startVideoPosition);

            Map<String, String> param = new HashMap<>();
            param.put("fragment_name", parentTag);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.to_do_mini_video.getActionName(), param);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void pauseVideo() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    public void resumeVideo() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    private void animateResize(final View view, int targetWidth, int targetHeight) {
        ViewGroup.LayoutParams params = view.getLayoutParams();

        int startWidth = view.getWidth();
        int startHeight = view.getHeight();

        int endWidth = targetWidth == ViewGroup.LayoutParams.MATCH_PARENT
                ? ((View) view.getParent()).getWidth()
                : targetWidth;

        int endHeight = targetHeight == ViewGroup.LayoutParams.MATCH_PARENT
                ? ((View) view.getParent()).getHeight()
                : targetHeight;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();

            params.width = (int) (startWidth + (endWidth - startWidth) * fraction);
            params.height = (int) (startHeight + (endHeight - startHeight) * fraction);

            view.setLayoutParams(params);
            scaleButtons(params.width);
        });
        animator.start();
    }

    private void scaleButtons(int widthPx) {
        try {
            int baseWidth = 360;
            float scaleFactor = Math.max(0.6f, Math.min(1.0f, widthPx / (float) baseWidth)); // range: 0.6 to 1.0

            int baseVerticalMargin = CommonMethods.dpToPx(getContext(), 20);
            int verticalMargin = isFullScreen ? baseVerticalMargin : (int) (scaleFactor * CommonMethods.dpToPx(getContext(), 4));

            int baseHorizontalMargin = CommonMethods.dpToPx(getContext(), 10);
            int horizontalMargin = isFullScreen ? baseHorizontalMargin : (int) (scaleFactor * CommonMethods.dpToPx(getContext(), 4));

            scaleButton(cv_close, scaleFactor);
            scaleButton(cv_volume, scaleFactor);
            scaleButton(cv_resize, scaleFactor);

            setButtonMargin(cv_close, verticalMargin, verticalMargin);
            setButtonMargin(cv_volume, horizontalMargin, verticalMargin);
            setButtonMargin(cv_resize, horizontalMargin, verticalMargin);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void scaleButton(View button, float scaleFactor) {
        button.setScaleX(scaleFactor);
        button.setScaleY(scaleFactor);
    }

    private void setButtonMargin(View button, int horizontalMargin, int verticalMargin) {

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
        layoutParams.setMargins(0, verticalMargin, horizontalMargin, 1);
        button.setLayoutParams(layoutParams);
    }

    private void removeMargins() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        setLayoutParams(params);
    }

    private void restoreMargins() {
        // adjust as per original design margins
        int margin = CommonMethods.dpToPx(getContext(), 8);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
        params.setMargins(margin, margin, margin, margin);
        setLayoutParams(params);
    }

    private void enableDrag(boolean enable) {
        this.setOnTouchListener(enable ? this::handleDrag : null);
    }

    public void closePlayer() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
        this.setVisibility(GONE);
    }

    public void destroy() {
        closePlayer();
    }

    public static ViewGroup.LayoutParams getPositionedLayoutParams(
            ViewGroup parent, @NonNull MiniPlayerPosition position,
            int marginLeftPx, int marginTopPx, int marginRightPx, int marginBottomPx
    ) {
        if (parent instanceof RelativeLayout) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            switch (position) {
                case TOP_START:
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    params.addRule(RelativeLayout.ALIGN_PARENT_START);
                    break;
                case TOP_END:
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    params.addRule(RelativeLayout.ALIGN_PARENT_END);
                    break;
                case BOTTOM_START:
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    params.addRule(RelativeLayout.ALIGN_PARENT_START);
                    break;
                case BOTTOM_END:
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    params.addRule(RelativeLayout.ALIGN_PARENT_END);
                    break;
                case CENTER:
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    break;
            }

            params.setMargins(marginLeftPx, marginTopPx, marginRightPx, marginBottomPx);
            return params;

        } else if (parent instanceof FrameLayout) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int gravity = Gravity.NO_GRAVITY;
            switch (position) {
                case TOP_START:
                    gravity = Gravity.TOP | Gravity.START;
                    break;
                case TOP_END:
                    gravity = Gravity.TOP | Gravity.END;
                    break;
                case BOTTOM_START:
                    gravity = Gravity.BOTTOM | Gravity.START;
                    break;
                case BOTTOM_END:
                    gravity = Gravity.BOTTOM | Gravity.END;
                    break;
                case CENTER:
                    gravity = Gravity.CENTER;
                    break;
            }

            params.gravity = gravity;
            params.setMargins(marginLeftPx, marginTopPx, marginRightPx, marginBottomPx);
            return params;

        } else if (parent instanceof ConstraintLayout) {
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            params.leftMargin = marginLeftPx;
            params.topMargin = marginTopPx;
            params.rightMargin = marginRightPx;
            params.bottomMargin = marginBottomPx;

            // Clear all constraints first
            params.startToStart = ConstraintLayout.LayoutParams.UNSET;
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            params.topToTop = ConstraintLayout.LayoutParams.UNSET;
            params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
            params.leftToLeft = ConstraintLayout.LayoutParams.UNSET;
            params.rightToRight = ConstraintLayout.LayoutParams.UNSET;

            switch (position) {
                case TOP_START:
                    params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    break;
                case TOP_END:
                    params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    break;
                case BOTTOM_START:
                    params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    break;
                case BOTTOM_END:
                    params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    break;
                case CENTER:
                    params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    break;
            }

            return params;
        }

        // Fallback
        return new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

}
