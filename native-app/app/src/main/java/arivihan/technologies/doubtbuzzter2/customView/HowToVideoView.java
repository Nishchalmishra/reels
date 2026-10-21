package arivihan.technologies.doubtbuzzter2.customView;

import static com.facebook.internal.Utility.logd;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MICRO_LECTURE_BY_ID;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.cardview.widget.CardView;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.ui.PlayerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
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

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.VideoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE;
import arivihan.technologies.doubtbuzzter2.model.HowToDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import lombok.Getter;
import lombok.Setter;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class HowToVideoView implements Player.Listener {

    @Setter
    public View videoView;
    private final DisplayMetrics displayMetrics;
    private Context context;
    public ExoPlayer player;
    private PlayerView playerView;
    private String videoUrl, localVideoUrl;
    private CardView close, volume, resize;
    private ImageView volumeImage, closeImage, resizeImage;
    private boolean isPaused;
    private boolean muted;
    public boolean expanded;
    private ProgressBar progressBar;
    private HOW_TO_TYPE howToType;
    private long startPos = 0;
    private float dX, dY;
    private float lastX, lastY;
    private float downX, downY;
    private boolean isDragging;
    private CardView bottom_button;
    private TextView bottom_button_text;
    private CountDownTimer countDownTimer;
    private ArrayList<HowToDto> list;
    private HowToDto howToDto;
    private int position;
    private static HowToVideoView howToVideoView;
    private ProgressDialog progressDialog;
    
    @Getter
    private RemoveListener removeListener;
    
    private int screenWidth;
    private int screenHeight;
    private int collapsedWidth;
    private int collapsedHeight;
    private boolean isInitialized = false;
    @Setter
    @Getter
    public boolean isBottomSheetVisibleForTestList;
    private boolean removed = false;


    public static HowToVideoView getInstance(Context context) {
        if (howToVideoView == null)
            howToVideoView = new HowToVideoView(context);

        return howToVideoView;
    }

    public HowToVideoView(Context context) {
        displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        }
    }

    public void setUrl(String url) {
        localVideoUrl = url;
    }

    public void setView(Context context, View view) {
        if (view == null) {
            Log.e("HowToVideoView", "setView: view is null — skipping");
            return;
        }
        if (this.videoView != view) {
            releasePlayer();
            this.videoView = view;
            this.context = context;
        }
    }

    public void setRemoveListener(RemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setType(HOW_TO_TYPE howToType, boolean expand) {
        Log.d("HowToVideoView", "setType: called with howToType=" + howToType + " | expand=" + expand);
        Log.d("HowToVideoView", "setType: isInitialized=" + isInitialized + " | this.howToType=" + this.howToType + " | videoView=" + videoView);
        Log.d("HowToVideoView", "setType: bottom_button=" + bottom_button + " | resizeImage=" + resizeImage);

        if (isInitialized && this.howToType == howToType && expand) {
            Log.d("HowToVideoView", "setType: shortcut path triggered — calling resize() directly");
            resize();
            return;
        }

        boolean hideHowToVideos = MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos");
        Log.d("HowToVideoView", "setType: hideHowToVideos=" + hideHowToVideos);

        if (!hideHowToVideos) {
            Log.d("HowToVideoView", "setType: proceeding — removed=false, releasing player");
            removed = false;
            releasePlayer();
            this.howToType = howToType;

            if (expand) {
                Log.d("HowToVideoView", "setType: calling showExpandedView");
                showExpandedView(howToType);
            } else {
                Log.d("HowToVideoView", "setType: calling showCollapsedView");
                showCollapsedView();
            }

            isInitialized = true;
            Log.d("HowToVideoView", "setType: isInitialized set to true");
        } else {
            Log.w("HowToVideoView", "setType: blocked by hideHowToVideos flag — nothing shown");
        }
    }

    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    public void showCollapsedView() {
        if (videoView != null) {
            videoView.setOnTouchListener(dragListener);
            initializeViews();
            setCollapsedViewParams();
            collapseCardViews();
            videoUrl = getUrlBasedOnType(howToType);
            bottom_button.setVisibility(View.GONE);
            if (StringUtils.isNotEmpty(videoUrl)) {
                videoView.setVisibility(View.VISIBLE);
            }
            setUp();
        }
    }

    public void setButtonText(String buttonText) {
        if (bottom_button_text != null) {
            if (StringUtils.isNotEmpty(buttonText)) {
                bottom_button_text.setText(buttonText);
                bottom_button_text.setVisibility(View.VISIBLE);
            } else {
                bottom_button_text.setVisibility(View.GONE);
            }
        }
    }

    public void hideResizeButton(boolean hide) {
        if (resize != null) {
            resize.setVisibility(hide ? View.GONE : View.VISIBLE);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void showExpandedView(HOW_TO_TYPE how_to_type) {
        Log.d("howtovideodebugging", "showExpandedView:  called");
        initializeViews();
        resizeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.minimize_video));
        videoView.setOnTouchListener(null);
        expanded = false;
        resize();
        videoUrl = getUrlBasedOnType(howToType);
        bottom_button.setVisibility(View.VISIBLE);
        countDownTimer = null;
        setUp();
    }

    private void initializeViews() {
        playerView = videoView.findViewById(R.id.video_player);
        bottom_button = videoView.findViewById(R.id.start_now);
        bottom_button_text = videoView.findViewById(R.id.bottom_button_text);
        volume = videoView.findViewById(R.id.btn_volume);
        volumeImage = videoView.findViewById(R.id.volume);
        volumeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.volume_on));
        resize = videoView.findViewById(R.id.resize);
        resize.setVisibility(View.VISIBLE);
        resizeImage = videoView.findViewById(R.id.resize_icon);
        close = videoView.findViewById(R.id.btn_close);
        closeImage = videoView.findViewById(R.id.close_image);
        progressBar = videoView.findViewById(R.id.progress_bar);
        videoView.setClickable(true);
        videoView.setFocusable(true);
        videoView.setFocusableInTouchMode(true);
        videoView.setOnClickListener(view -> {

            Map<String, String> params = new HashMap<>();
            params.put("source_screen", howToType != null ? howToType.name() : "unknown");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.how_to_video_view.getActionName(),params);

            if (player != null) {
                if (isPaused) {
                    pausePlayer();
                } else {
                    resumePlayer();
                }
            }
            isPaused = !isPaused;
        });
        volume.setOnClickListener(v -> {

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.how_to_video_volume_btn_click.getActionName());

            if (player != null) {
                if (muted) {
                    volumeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.volume_on));
                    player.setVolume(1f);
                } else {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_mini_video_mute_click.getActionName());
                    volumeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.volume_off));
                    player.setVolume(0f);
                }
            }
            muted = !muted;
            if (removeListener != null)
                removeListener.onVolumeToggle(muted);
        });
        close.setOnClickListener(view -> {

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.how_to_video_close_btn_click.getActionName());

            if (removeListener != null)
                removeListener.onClose();
            if (expanded) {
                resize();
            } else {
                if (videoView != null)
                    animateRemove();
            }
        });

        bottom_button.setOnClickListener(view -> {
            if (removeListener != null)
                removeListener.onButtonClick();
            if (howToType == HOW_TO_TYPE.HOME_SCREEN) {
                remove();
                onClick(howToDto);
            } else if (videoView != null)
                animateRemove();
        });
        resize.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.how_to_video_resize_click.getActionName());
            if (player != null) {
                resize();
            }
        });
    }

    private final View.OnTouchListener dragListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    downX = event.getRawX();
                    downY = event.getRawY();
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;
                    newX = Math.max(0, Math.min(newX, displayMetrics.widthPixels - v.getWidth()));
                    newY = Math.max(0, Math.min(newY, displayMetrics.heightPixels - v.getHeight()));
                    v.setX(newX);
                    v.setY(newY);
                    float moveX = event.getRawX();
                    float moveY = event.getRawY();
                    float deltaX = Math.abs(moveX - downX);
                    float deltaY = Math.abs(moveY - downY);

                    if (deltaX > 10 || deltaY > 10) {
                        isDragging = true;
                        v.setX(moveX - v.getWidth() / 2);
                        v.setY(moveY - v.getHeight() / 2);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!isDragging) {
                        v.performClick();
                    }
                    return true;
                default:
                    return false;
            }
        }
    };

    public void resize() {
        if (videoView != null) {
            if (!expanded) {
                if (removeListener != null)
                    removeListener.onExpand();
                videoView.setOnTouchListener(null);
                expandView();
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_mini_video_expand_click.getActionName());
                expandCardViews();
            } else {
                if (removeListener != null)
                    removeListener.onCollapse();
                videoView.setOnTouchListener(dragListener);
                collapseView();
                collapseCardViews();
            }
            expanded = !expanded;
        }
    }

    private void expandView() {
        bottom_button.setVisibility(View.VISIBLE);
        resizeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.minimize_video));
        if (videoView != null)
            animateResize(true);
    }

    private void collapseView() {
        bottom_button.setVisibility(View.GONE);
        resizeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.maximize_video));
        if (videoView != null)
            animateResize(false);
    }

    private void expandCardViews() {
        int size = dpToPx(32);

        ViewGroup.MarginLayoutParams closeImageParams = (ViewGroup.MarginLayoutParams) closeImage.getLayoutParams();
        closeImageParams.height = size;
        closeImageParams.width = size;
        closeImage.setLayoutParams(closeImageParams);

        ViewGroup.MarginLayoutParams closeParams = (ViewGroup.MarginLayoutParams) close.getLayoutParams();
        closeParams.setMargins(0, CommonMethods.dpToPx(context, 20), CommonMethods.dpToPx(context, 20), 0);
        close.setLayoutParams(closeParams);

        ViewGroup.MarginLayoutParams resizeImageParams = (ViewGroup.MarginLayoutParams) resizeImage.getLayoutParams();
        resizeImageParams.height = size;
        resizeImageParams.width = size;
        resizeImage.setLayoutParams(resizeImageParams);

        ViewGroup.MarginLayoutParams resizeParams = (ViewGroup.MarginLayoutParams) resize.getLayoutParams();
        resizeParams.setMargins(0, CommonMethods.dpToPx(context, 20), CommonMethods.dpToPx(context, 20), 0);
        resize.setLayoutParams(resizeParams);

        ViewGroup.MarginLayoutParams volumeImageParams = (ViewGroup.MarginLayoutParams) volumeImage.getLayoutParams();
        volumeImageParams.height = size;
        volumeImageParams.width = size;
        volumeImage.setLayoutParams(volumeImageParams);

        ViewGroup.MarginLayoutParams volumeParams = (ViewGroup.MarginLayoutParams) volume.getLayoutParams();
        volumeParams.setMargins(0, CommonMethods.dpToPx(context, 20), CommonMethods.dpToPx(context, 20), 0);
        volume.setLayoutParams(volumeParams);
    }

    private void collapseCardViews() {
        int size = dpToPx(25);

        ViewGroup.MarginLayoutParams closeImageParams = (ViewGroup.MarginLayoutParams) closeImage.getLayoutParams();
        closeImageParams.height = size;
        closeImageParams.width = size;
        closeImage.setLayoutParams(closeImageParams);

        ViewGroup.MarginLayoutParams closeParams = (ViewGroup.MarginLayoutParams) close.getLayoutParams();
        closeParams.setMargins(0, 10, 10, 0);
        close.setLayoutParams(closeParams);

        ViewGroup.MarginLayoutParams resizeImageParams = (ViewGroup.MarginLayoutParams) resizeImage.getLayoutParams();
        resizeImageParams.height = size;
        resizeImageParams.width = size;
        resizeImage.setLayoutParams(resizeImageParams);

        ViewGroup.MarginLayoutParams resizeParams = (ViewGroup.MarginLayoutParams) resize.getLayoutParams();
        resizeParams.setMargins(0, 10, 10, 0);
        resize.setLayoutParams(resizeParams);

        ViewGroup.MarginLayoutParams volumeImageParams = (ViewGroup.MarginLayoutParams) volumeImage.getLayoutParams();
        volumeImageParams.height = size;
        volumeImageParams.width = size;
        volumeImage.setLayoutParams(volumeImageParams);

        ViewGroup.MarginLayoutParams volumeParams = (ViewGroup.MarginLayoutParams) volume.getLayoutParams();
        volumeParams.setMargins(0, 10, 10, 0);
        volume.setLayoutParams(volumeParams);
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void animateResize(final boolean expand) {

        if (videoView == null) {
            Log.e("HowToVideoView", "videoView is null. Skipping animation.");
            return;
        }

        videoView.setVisibility(View.VISIBLE);
        videoView.post(() -> {

            int startWidth = videoView.getWidth();
            int startHeight = videoView.getHeight();
            float startX = videoView.getX();
            float startY = videoView.getY();

            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            int endWidth = expand ? screenWidth : screenWidth / 3;
            int endHeight = expand ? screenHeight : screenHeight / 3;

            if (howToType == HOW_TO_TYPE.LECTURE && expand)
                endHeight = endHeight + dpToPx(50);

            if (expand) {
                lastX = startX;
                lastY = startY;
            }

            float startEndX = startX + startWidth;
            float startEndY = startY + startHeight;

            float endX = expand ? (screenWidth - endWidth) : startEndX - endWidth - 20;
            float endY = expand ? (screenHeight - endHeight) : startEndY - endHeight - 20;


            if (howToType == HOW_TO_TYPE.LECTURE && expand)
                endY = endY + dpToPx(50);

            if ((isBottomSheetVisibleForTestList ||
                    howToType == HOW_TO_TYPE.SUBSCRIPTION ||
                    howToType == HOW_TO_TYPE.QUESTION_TYPE_MATCH_PRACTICE_ANALYSIS || howToType == HOW_TO_TYPE.QUESTION_TYPE_SAV_PRACTICE_ATTEMPT ||
                    howToType == HOW_TO_TYPE.QUESTION_TYPE_A_ANALYSIS_PRACTICE ||
                    howToType == HOW_TO_TYPE.QUESTION_TYPE_A_ATTEMPT_PRACTICE ||
                    howToType == HOW_TO_TYPE.QUESTION_TYPE_SAV_PRACTICE_ANALYSIS || howToType == HOW_TO_TYPE.QUESTION_TYPE_MATCH_PRACTICE_ATTEMPT ||
                    howToType == HOW_TO_TYPE.QUESTION_TYPE_MATCH_TEST_ANALYSIS || howToType == HOW_TO_TYPE.QUESTION_TYPE_SAV_TEST_ATTEMPT ||
                    howToType == HOW_TO_TYPE.QUESTION_TYPE_A_ATTEMPT_TEST ||
                    howToType == HOW_TO_TYPE.QUESTION_TYPE_A_ANALYSIS ||
                    howToType == HOW_TO_TYPE.QUESTION_TYPE_SAV_ANALYSIS_TEST || howToType == HOW_TO_TYPE.QUESTION_TYPE_MATCH_TEST_ATTEMPT)
                    && !expand)
                endY = endY - dpToPx(80);

            ValueAnimator widthAnimator = ValueAnimator.ofInt(startWidth, endWidth);
            ValueAnimator heightAnimator = ValueAnimator.ofInt(startHeight, endHeight);
            ValueAnimator xAnimator = ValueAnimator.ofFloat(startX, endX);
            ValueAnimator yAnimator = ValueAnimator.ofFloat(startY, endY);

            widthAnimator.addUpdateListener(animation -> {
                if (videoView != null) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoView.getLayoutParams();
                    if (expand) params.setMargins(0, 0, 0, 0);
                    else params.setMargins(20, 20, 20, 20);
                    params.width = (int) animation.getAnimatedValue();
                    videoView.setLayoutParams(params);
                }
            });

            heightAnimator.addUpdateListener(animation -> {
                if (videoView != null) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoView.getLayoutParams();
                    if (expand) params.setMargins(0, 0, 0, 0);
                    else params.setMargins(20, 20, 20, 20);
                    params.height = (int) animation.getAnimatedValue();
                    videoView.setLayoutParams(params);
                }
            });

            xAnimator.addUpdateListener(animation -> {
                if (videoView != null) {
                    videoView.setX((float) animation.getAnimatedValue());
                }
            });

            yAnimator.addUpdateListener(animation -> {
                if (videoView != null) {
                    videoView.setY((float) animation.getAnimatedValue());
                }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(widthAnimator, heightAnimator, xAnimator, yAnimator);
            animatorSet.setDuration(300);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.start();

        });
    }

    private void setCollapsedViewParams() {
        expanded = false;
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        collapsedWidth = screenWidth / 3;
        collapsedHeight = screenHeight / 3;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoView.getLayoutParams();
        params.width = collapsedWidth;
        params.height = collapsedHeight;

        if (howToType == HOW_TO_TYPE.SUBSCRIPTION ||
                howToType == HOW_TO_TYPE.QUESTION_TYPE_MATCH_PRACTICE_ANALYSIS || howToType == HOW_TO_TYPE.QUESTION_TYPE_SAV_PRACTICE_ATTEMPT ||
                howToType == HOW_TO_TYPE.QUESTION_TYPE_A_ANALYSIS_PRACTICE ||
                howToType == HOW_TO_TYPE.QUESTION_TYPE_A_ATTEMPT_PRACTICE ||
                howToType == HOW_TO_TYPE.QUESTION_TYPE_SAV_PRACTICE_ANALYSIS || howToType == HOW_TO_TYPE.QUESTION_TYPE_MATCH_PRACTICE_ATTEMPT ||
                howToType == HOW_TO_TYPE.QUESTION_TYPE_MATCH_TEST_ANALYSIS || howToType == HOW_TO_TYPE.QUESTION_TYPE_SAV_TEST_ATTEMPT ||
                howToType == HOW_TO_TYPE.QUESTION_TYPE_A_ATTEMPT_TEST ||
                howToType == HOW_TO_TYPE.QUESTION_TYPE_A_ANALYSIS ||
                howToType == HOW_TO_TYPE.QUESTION_TYPE_SAV_ANALYSIS_TEST || howToType == HOW_TO_TYPE.QUESTION_TYPE_MATCH_TEST_ATTEMPT) {
            params.setMargins(0, 0, 20, dpToPx(80));
        } else {
            params.setMargins(0, 0, 20, 20);
        }
        videoView.setLayoutParams(params);
    }

    private void setExpandedViewParams() {
        expanded = true;
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoView.getLayoutParams();
        params.width = screenWidth;
        if (howToType == HOW_TO_TYPE.HOME_SCREEN) {
            params.height = screenHeight - 180;
            params.setMargins(0, 0, 0, 180);
        } else {
            params.height = screenHeight;
            params.setMargins(0, 0, 0, 0);
        }
        videoView.setLayoutParams(params);
    }

    public String getUrlBasedOnType(HOW_TO_TYPE howToType) {
        Log.d("issuedebug", "getUrlBasedOnType: "+howToType);
        try {
            String videoUrlKey = "";
            switch (howToType) {
                case TEST_JEE ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_JEE) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_JEE_CHAPETR ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_JEE_CHAPTER) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_NEET ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_NEET) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_NEET_CHAPETR ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_NEET_CHAPTER) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_BOARD ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_BOARD) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_BOARD_CHAPETR ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_BOARD_CHAPTER) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_JEE_ANALYSIS ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_JEE_ANALYSIS) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_JEE_CHAPETR_ANALYSIS ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_JEE_CHAPTER_ANALYSIS) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_NEET_ANALYSIS ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_NEET_ANALYSIS) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_NEET_CHAPETR_ANALYSIS ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_NEET_CHAPTER_ANALYSIS) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_BOARD_ANALYSIS ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_BOARD_ANALYSIS) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TEST_BOARD_CHAPETR_ANALYSIS ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TEST_BOARD_CHAPTER_ANALYSIS) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_A_ANALYSIS ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_A_ANALYSIS) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_SAV_ANALYSIS_TEST ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_SAV_ANALYSIS_TEST) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_MATCH_TEST_ATTEMPT ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_MATCH_TEST_ATTEMPT) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_MATCH_PRACTICE_ATTEMPT ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_MATCH_PRACTICE_ATTEMPT) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_A_ATTEMPT_PRACTICE ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_A_ATTEMPT_PRACTICE) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_A_ANALYSIS_PRACTICE ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_A_ANALYSIS_PRACTICE) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_PRACTICE_LIST ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_PRACTICE_LIST) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case SUBSCRIPTION ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_SUBSCRIPTION) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case DOUBT_CHAT ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_DOUBT_CHAT) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case LECTURE ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_LECTURE) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case HOME_SCREEN -> {
                    String homeUrl = getHomeScreenMiniVideoUrl();
                    if (StringUtils.isNotEmpty(homeUrl)) {
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.how_to_video_view.getActionName());
                        return homeUrl;
                    } else {
                        return null;
                    }
                }
                case QUESTION_TYPE_SAV_PRACTICE_ATTEMPT ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_SAV_PRACTICE_ATTEMPT) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_A_ATTEMPT_TEST ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_A_ATTEMPT_TEST) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_SAV_TEST_ATTEMPT ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_SAV_TEST_ATTEMPT) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_SAV_PRACTICE_ANALYSIS ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_SAV_PRACTICE_ANALYSIS) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_MATCH_PRACTICE_ANALYSIS ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_MATCH_PRACTICE_ANALYSIS) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case QUESTION_TYPE_MATCH_TEST_ANALYSIS ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_QUESTION_TYPE_MATCH_TEST_ANALYSIS) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                case TOPPERS_NOTES -> {
                    String topperUrl = getVideoUrlForTopperNotes();
                    if (StringUtils.isNotEmpty(topperUrl)) {
                        return topperUrl; // <-- direct return
                    } else {
                        return localVideoUrl;
                    }
                }
                case TODO_MAIN_SCREEN ->
                        videoUrlKey = context.getString(R.string.HOW_TO_TYPE_TODO_MAIN_SCREEN)
                                + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            }

            if(StringUtils.isEmpty(videoUrlKey) && StringUtils.isNotEmpty(localVideoUrl)) {
                return localVideoUrl;
            }
            return MyApplication.getFirebaseRemoteConfig().getString(videoUrlKey);
        } catch (Exception e) {
            Log.e("VideoUrlError", "Error getting video URL", e);
            return null;
        }
    }

    private String getHomeScreenMiniVideoUrl() {
        try {
            Log.d("MiniVideoUrl", "Fetching remote config value...");

            String fireBaseKey = FirebaseRemoteConfig.getInstance()
                    .getString("home_screen_mini_video_v1");

            Log.d("MiniVideoUrl", "Firebase raw JSON: " + fireBaseKey);

            JSONObject remoteJsonObject = new JSONObject(fireBaseKey);
            Log.d("MiniVideoUrl", "JSON parsed successfully");

            String batchCode = MyApplication.getStringData(
                    PreferenceKeys.BATCH_CODE.getKey()
            );

            Log.d("MiniVideoUrl", "Current batchCode: " + batchCode);

            String url = remoteJsonObject.optString(batchCode, null);

            Log.d("MiniVideoUrl", "Fetched URL from JSON: " + url);

            if (url == null || url.isEmpty()) {
                Log.w("MiniVideoUrl", "No URL found for batchCode: " + batchCode + " — trying DEFAULT");
                url = remoteJsonObject.optString("DEFAULT", null);
                Log.d("MiniVideoUrl", "DEFAULT fallback URL: " + url);
            }

            if (url == null || url.isEmpty()) {
                Log.w("MiniVideoUrl", "No URL found for batchCode: " + batchCode + " or DEFAULT");
                return null;
            }

            Log.d("MiniVideoUrl", "Returning URL: " + url);
            return url;

        } catch (Exception e) {
            Log.e("MiniVideoUrl", "Error fetching mini video URL", e);
            return null;
        }
    }

    private String getVideoUrlForTopperNotes() {
        try {
            Log.d("DD_LOGS", "🏁 [TopperNotes] Starting to fetch video URL from Remote Config");

            // Fetch JSON from Firebase Remote Config
            String jsonString = MyApplication.getFirebaseRemoteConfig()
                    .getString("topper_notes_how_to_video");

            if (StringUtils.isEmpty(jsonString)) {
                Log.w("DD_LOGS", "⚠️ [TopperNotes] Empty JSON string for key: topper_notes_how_to_video");
                return "";
            }

            Log.d("DD_LOGS", "📦 [TopperNotes] Remote Config JSON: " + jsonString);
            JSONObject root = new JSONObject(jsonString);

            // Get exam code and language
            String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
            String languageCode = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

            if (StringUtils.isEmpty(languageCode)) {
                languageCode = "en";
                Log.w("DD_LOGS", "⚠️ [TopperNotes] Language code not found — falling back to 'en'");
            }

            Log.d("DD_LOGS", "🎯 [TopperNotes] Exam Code: " + examCode);
            Log.d("DD_LOGS", "🌐 [TopperNotes] Selected Language: " + languageCode);

            // Get the exam object (e.g., "NEET" or "MPBSE"), with "default" fallback
            JSONObject courseObject = root.optJSONObject(examCode);
            if (courseObject == null) {
                courseObject = root.optJSONObject("default");
            }
            if (courseObject == null) {
                Log.w("DD_LOGS", "⚠️ [TopperNotes] No entry found for examCode=" + examCode);
                return "";
            }

            // Get the URL for the selected language
            String videoUrl = courseObject.optString(languageCode, "");

            if (StringUtils.isEmpty(videoUrl)) {
                Log.w("DD_LOGS", "⚠️ [TopperNotes] No URL found for language=" + languageCode + " under examCode=" + examCode);
            } else {
                Log.d("DD_LOGS", "✅ [TopperNotes] Extracted video URL: " + videoUrl);
            }

            Log.d("DD_LOGS", "🏁 [TopperNotes] Finished fetching video URL successfully");
            return videoUrl.trim();

        } catch (JSONException e) {
            Log.e("DD_LOGS", "❌ [TopperNotes] JSON parsing error: " + e.getMessage(), e);
            return "";
        } catch (Exception e) {
            Log.e("DD_LOGS", "❌ [TopperNotes] Unexpected error: " + e.getMessage(), e);
            return "";
        }
    }

    public static String getHowToPosterUrl(String screen) {
        try {
            if (screen == null || screen.isEmpty()) {
                Log.w("PosterUrlError", "Screen parameter is null or empty");
                return null;
            }
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            if (batchCode == null || batchCode.isEmpty()) {
                Log.w("PosterUrlError", "Batch code is null or empty");
                return null;
            }
            String jsonString = FirebaseRemoteConfig
                    .getInstance()
                    .getString("How_To_Poster_configuration");
            Log.d("PosterJson", jsonString);

            if (jsonString.isEmpty()) {
                Log.w("PosterUrlError", "Remote config JSON is empty");
                return null;
            }

            JSONObject mainObject = new JSONObject(jsonString);

            JSONObject screenObject = mainObject.optJSONObject(screen); // Renamed: "chidlObject" → "screenObject"
            if (screenObject == null) {
                Log.w("PosterUrlError", "No config found for screen: " + screen);
                return null;
            }

            String url = screenObject.optString(batchCode, null); // optString avoids exception if key missing
            if (url == null || url.isEmpty()) {
                Log.w("PosterUrlError", "No URL found for batchCode: " + batchCode);
                return null;
            }

            return url;

        } catch (JSONException e) {
            Log.e("PosterUrlError", "JSON parsing error", e);
            return null;
        }
    }


    public void remove() {
        Log.d("removeCalled", "removecalled: ");
        howToVideoView = null;
        if (videoView != null) {
            if (countDownTimer != null)
                countDownTimer.cancel();
            pausePlayer();
            releasePlayer();
            videoView.setVisibility(View.GONE);
            videoView = null;
        }
        if (removeListener != null)
            removeListener.onRemove();
        removeListener = null;
    }


    private void animateRemove() {
        MyApplication.saveData(PreferenceKeys.HOW_TO_VIDEO_VISIBLE.getKey(), false);
        if (removed) {
            return;
        }
        removed = true;
        if (videoView != null) {
            int startWidth = videoView.getWidth();
            int startHeight = videoView.getHeight();
            float startX = videoView.getX();
            float startY = videoView.getY();

            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            int endWidth = dpToPx(1);
            int endHeight = dpToPx(1);

            ValueAnimator widthAnimator = ValueAnimator.ofInt(startWidth, endWidth);
            ValueAnimator heightAnimator = ValueAnimator.ofInt(startHeight, endHeight);
            ValueAnimator xAnimator = ValueAnimator.ofFloat(startX, (float) screenWidth);
            ValueAnimator yAnimator = ValueAnimator.ofFloat(startY, (float) screenHeight);

            widthAnimator.addUpdateListener(animation -> {
                if (videoView != null) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoView.getLayoutParams();
                    params.width = (int) animation.getAnimatedValue();
                    videoView.setLayoutParams(params);
                }
            });

            heightAnimator.addUpdateListener(animation -> {
                if (videoView != null) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoView.getLayoutParams();
                    params.height = (int) animation.getAnimatedValue();
                    videoView.setLayoutParams(params);
                }
            });

            xAnimator.addUpdateListener(animation -> {
                if (videoView != null) {
                    videoView.setX((float) animation.getAnimatedValue());
                }
            });
            yAnimator.addUpdateListener(animation -> {
                if (videoView != null) {
                    videoView.setY((float) animation.getAnimatedValue());
                }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(widthAnimator, heightAnimator, xAnimator, yAnimator);
            animatorSet.setDuration(200);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.start();

            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animator) {

                }

                @Override
                public void onAnimationEnd(@NonNull Animator animator) {
                    howToVideoView = null;
                    if (videoView != null) {
                        if (countDownTimer != null)
                            countDownTimer.cancel();
                        pausePlayer();
                        releasePlayer();
                        videoView.setVisibility(View.GONE);
                        videoView = null;
                    }
                    if (removeListener != null)
                        removeListener.onRemove();
                    removeListener = null;
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animator) {

                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animator) {

                }
            });
        }

    }

    public void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    public void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    private void setUp() {
        Log.d("debugcount", "setUp:");
        initializePlayer();
        if (StringUtils.isEmpty(videoUrl)) {
            Log.d("debugcount", "setUp: video url is null");
            return;
        }
        buildMediaSource(Uri.parse(videoUrl));
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {
        if (player == null) {
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context)
                    .setEnableDecoderFallback(true)
                    .setMediaCodecSelector(MediaCodecSelector.DEFAULT);

            player = new ExoPlayer.Builder(context, renderersFactory).build();
            playerView.setPlayer(player);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void buildMediaSource(Uri mUri) {
        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);
        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(mUri));

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
//        MediaItem mediaItem = new MediaItem.Builder().setUri(mUri).setMimeType(MimeTypes.APPLICATION_M3U8).build();
//        player.setMediaItem(mediaItem);
//        player.prepare();
//        player.setPlayWhenReady(true);
        player.addListener(this);
        player.seekTo(startPos);
        videoView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        Log.e("ExoPlayer Error", "Error: " + error.getMessage());
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        Log.d("issueonPLayerBack", "onIsPlayingChanged: " +
                player.getPlaybackState());
        switch (player.getPlaybackState()) {
            case Player.STATE_IDLE:
                startPos = player.getCurrentPosition();
                progressBar.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_READY:
                playerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                if (howToType == HOW_TO_TYPE.HOME_SCREEN && countDownTimer == null)
                    startTimer();
                break;
            case Player.STATE_ENDED:
                Log.d("issueonVideoPLayer", "onIsPlayingChanged: ");
                if (videoView != null && howToType == HOW_TO_TYPE.HOME_SCREEN) {
                    Log.d("issueonVideoPLayer", "homescreen : ");
                    removeListener.onClose();
                }
                animateRemove();
                break;
            default:
                break;
        }
    }

    private void startTimer() {
        try {
            String key = "HOW_TO_HOME_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())+"_"+MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
            String text = MyApplication.getFirebaseRemoteConfig().getString(key);
            list = getList(text);
            if (!CollectionUtils.isEmpty(list)) {
                howToDto = list.get(position);
                setButtonText(howToDto.getButtonText());
                long duration = player.getDuration();
                countDownTimer = new CountDownTimer(duration, 500) {
                    @Override
                    public void onTick(long l) {
                        try {
                            if (player == null || howToDto == null) {
                                return;
                            }
                            long currentPosition = player.getCurrentPosition();
                            if (Math.abs(currentPosition - howToDto.getTimestamp()) < 500) {
                                position = position + 1;
                                if (position < list.size()) {
                                    howToDto = list.get(position);
                                    setButtonText(howToDto.getButtonText());
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onFinish() {

                    }
                };
                countDownTimer.start();
            }

        } catch (Exception e) {
            Log.e("VideoUrlError", "Error getting video URL", e);

        }

    }

    public static ArrayList<HowToDto> getList(String text) {
        try {
            Gson gson = new Gson();
            TypeToken<List<HowToDto>> typeToken = new TypeToken<>() {
            };
            List<HowToDto> list = gson.fromJson(text, typeToken.getType());
            return new ArrayList<>(list);
        } catch (Exception e) {
            logd("getList: ", e.getMessage());

        }
        return null;
    }

    public void onClick(HowToDto howToDto) {
        try {
            if (howToDto != null) {
                Intent intent = new Intent(context, ActivityLauncher.resolveClass(howToDto.getClassName()));
                if (howToDto.getActivityParams() != null) {
                    for (String key : howToDto.getActivityParams().keySet()) {
                        Object value = howToDto.getActivityParams().get(key);
                        if (value instanceof String) {
                            intent.putExtra(key, (String) howToDto.getActivityParams().get(key));
                        } else if (value instanceof Integer) {
                            intent.putExtra(key, (Integer) howToDto.getActivityParams().get(key));
                        } else if (value instanceof Boolean) {
                            intent.putExtra(key, (Boolean) howToDto.getActivityParams().get(key));
                        }
                    }
                }
                if (howToDto.getClassName().contains("VideoPlayerActivity")) {
                    getMicrolectureById(intent.getStringExtra("microLectureId"));
                } else {
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            logd("onGoToClick: ", e.getMessage());
        }
    }

    private void getMicrolectureById(String microLectureId) {
        Gson gson = new Gson();
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        Map<String, Object> map = new HashMap<>();
        map.put("microLectureId", microLectureId);
        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(MICRO_LECTURE_BY_ID, map), null,
                response -> {
                    progressDialog.dismiss();
                    TypeToken<MicroLectureContentDto> token = new TypeToken<MicroLectureContentDto>() {
                    };
                    MicroLectureContentDto microLectureContentDto = gson.fromJson(response.toString(), token.getType());
                    microLectureContentDto.setId(microLectureId);
                    @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra(context.getResources().getString(R.string.passed_obj), microLectureContentDto);
                    intent.putExtra("shouldNotResume", true);
                    intent.putExtra("doubtVideo", false);
                    intent.putExtra("isAskDoubtActivity", false);
                    intent.putExtra("video_uri", microLectureContentDto.getVideoUrl());
                    intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), microLectureContentDto.getDefaultStartTime());
                    intent.putExtra("video_ending", microLectureContentDto.getVideoCompletionEndPos());
                    context.startActivity(intent);
                }, new VolleyGlobalErrorListener(context) {
            @Override
            public void postError(VolleyError error) {
                progressDialog.dismiss();
                Log.e("Error", "postError : " + error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public interface RemoveListener {
        default void onRemove() {
        }

        ;

        default void onExpand() {
        }

        ;

        default void onCollapse() {
        }

        ;

        default void onClose() {
        }

        ;

        default void onButtonClick() {
        }

        ;
        
        default void onVolumeToggle(boolean muted) {
        }
    }
}
