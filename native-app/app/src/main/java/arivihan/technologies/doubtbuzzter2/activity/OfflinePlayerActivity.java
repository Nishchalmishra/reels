package arivihan.technologies.doubtbuzzter2.activity;

import android.app.Dialog;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.offline.DownloadHelper;
import androidx.media3.exoplayer.offline.DownloadRequest;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.CrashUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.VideoSpeedPlaybackAdapter;
import arivihan.technologies.doubtbuzzter2.customView.CustomTimeBar;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.manager.DemoUtil;
import arivihan.technologies.doubtbuzzter2.manager.DownloadTracker;
import arivihan.technologies.doubtbuzzter2.model.ConfirmOption;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionAnsResponseDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionsAnsDto;
import arivihan.technologies.doubtbuzzter2.model.TimeBarThumbData;
import arivihan.technologies.doubtbuzzter2.model.VideoPlaybackSpeedDto;


@UnstableApi
public class OfflinePlayerActivity extends BaseActivity implements Player.Listener,
        VideoSpeedPlaybackAdapter.VideoSpeedChangeListener,
        EdgeToEdgeHelper.SelfManagedInsets {

    private static final String TAG = OfflinePlayerActivity.class.getSimpleName();
    private ExoPlayer player;
    private long playbackPosition = 0L;
    private boolean playWhenReady = true;
    @UnstableApi
    private DownloadTracker downloadTracker;
    private PlayerView playerView;
    private String VIDEO_URL;
    private final List<TimeBarThumbData> thumbDataList = new ArrayList<>();
    private boolean interactiveQuestionActive;
    private CustomTimeBar customTimeBar;
    private MicroLectureQuestionsAnsDto currentInteractiveQuestion;
    private MediaEntity mediaEntity;
    private Runnable positionUpdatingRunnable;
    private boolean runnableStarted;
    private int endPosition, fallbackPosition;
    private boolean disableControllerAction;
    private Handler handler;
    private final int SEEK_TIMER = 10000;
    private final int TIME_BUFFER_DIFFERENCE = 500;
    protected LinearLayout successCelebration;
    protected LottieAnimationView celebration1, celebration2, celebration3;
    protected MediaPlayer confettiSound;
    protected Handler confettiHandler;
    protected Runnable confettiRunnable;
    private ImageButton rewind, forward;
    protected ImageView iv_active_interactive_question;
    private TextView duration;
    private ImageView arivihanLogo, videoPlayerSettings;
    private RecyclerView playback_speed_recycler_view;
    private CardView playbackCard;
    private VideoSpeedPlaybackAdapter videoSpeedPlaybackAdapter;
    protected List<VideoPlaybackSpeedDto> playbackSpeeds = new ArrayList<>();


    private String selectedPlaybackSpeed = "1";
    private MaterialCardView longPressSpeedCard;

    //on touch
    private final Handler tapHandler = new Handler(Looper.getMainLooper());
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private final Handler seekResetHandler = new Handler(Looper.getMainLooper());
    private Runnable skipFadeRunnable;

    private Runnable singleTapRunnable;
    private Runnable longPressRunnable;
    private Runnable seekResetRunnable;

    private static final long TAP_DELAY = 250L;
    private static final long LONG_PRESS_DELAY = 400L;
    private static final long SEEK_RESET_DELAY = 700L;

    private boolean isSeekMode = false;
    private int seekCount = 0;
    private long lastTapTime = 0L;
    private boolean lastTapWasLeft = false;
    private boolean lastAppliedTapWasLeft = false;

    private boolean isLongPressActive = false;

    //controller
    private TextView rewindLabel, forwardLabel;
    private ImageView rewindArrow, forwardArrow;

    private long longPressStartTime = 0L;


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_player);
        enableImmersiveMode();
        secureScreen();
        mediaEntity = (MediaEntity) getIntent().getSerializableExtra("mediaEntity");
        assert mediaEntity != null;

        // In case of assert is ignored or removed by release build
        if (mediaEntity == null) {
            Log.e(TAG, "MediaEntity is null");
            Toast.makeText(this, getString(R.string.media_entity_null_cannot_play_video), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        VIDEO_URL = mediaEntity.getUrl();
        playerView = findViewById(R.id.videoFullScreenPlayer);

        View offlineRoot = findViewById(R.id.offline_player_root);
        View controllerRoot = playerView.findViewById(R.id.offline_controller_root);
        ViewCompat.setOnApplyWindowInsetsListener(offlineRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                            | WindowInsetsCompat.Type.displayCutout());
            if (controllerRoot != null) {
                controllerRoot.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            }
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(offlineRoot);

        playback_speed_recycler_view = playerView.findViewById(R.id.playback_speed_recycler_view);
        playbackCard=playerView.findViewById(R.id.card_speed_container);
        videoPlayerSettings = playerView.findViewById(R.id.setting);
        videoPlayerSettings.setOnClickListener(view -> closeSettingLayout());
        rewind = playerView.findViewById(R.id.rewind);
        forward = playerView.findViewById(R.id.forward);
        customTimeBar = playerView.findViewById(R.id.exo_progress);
        duration = playerView.findViewById(R.id.custom_exo_duration);

        rewindLabel = findViewById(R.id.rewind_label);
        forwardLabel = findViewById(R.id.forward_label);
        rewindArrow = findViewById(R.id.rewind_arrow);
        forwardArrow = findViewById(R.id.forward_arrow);
        longPressSpeedCard = findViewById(R.id.long_press_speed_card);
        
        
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            if (visibility == View.GONE) {
                playback_speed_recycler_view.setVisibility(View.GONE);
            }
        });


        playerView.setOnTouchListener((v, event) -> {
            float x = event.getX();
            boolean isLeft = x < playerView.getWidth() / 2f;
            long now = System.currentTimeMillis();


            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    longPressRunnable = () -> {
                        if (player.isPlaying()) {
                            isLongPressActive = true;
                            set2xPlayerSpeed(); //speed 2x
                        }
                    };
                    longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_DELAY);
                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    longPressHandler.removeCallbacks(longPressRunnable);

                    if (isLongPressActive) {
                        isLongPressActive = false;
                        resetPlaybackSpeed(); //speed reset
                        return true;
                    }


                    //double tap
                    boolean sameSideSingleTap = (isLeft == lastTapWasLeft);
                    boolean sameSideAction = (isLeft == lastAppliedTapWasLeft);
                    boolean withinGap = (now - lastTapTime) < TAP_DELAY;
                    boolean inSeekMode = isSeekMode && sameSideAction && (now - lastTapTime) < SEEK_RESET_DELAY;

                    if (!sameSideAction || !sameSideSingleTap) {
                        seekCount = 0;
                        rewindArrow.setVisibility(View.INVISIBLE);
                        rewindLabel.setVisibility(View.INVISIBLE);
                        rewind.setVisibility(View.VISIBLE);

                        forwardArrow.setVisibility(View.INVISIBLE);
                        forwardLabel.setVisibility(View.INVISIBLE);
                        forward.setVisibility(View.VISIBLE);
                    }
                    lastTapWasLeft = isLeft;


                    if ((withinGap && sameSideAction && sameSideSingleTap) || inSeekMode) {
                        tapHandler.removeCallbacks(singleTapRunnable);
                        isSeekMode = true;

                        if (!disableControllerAction) {
                            int cumulativeSeekSeconds = (((seekCount + 1) * 10) / 2);

                            if (isLeft) {
                                triggerSkipAnimation(rewindLabel, rewind, rewindArrow, true);
                                player.seekTo(Math.max(player.getCurrentPosition() - SEEK_TIMER, 0));
                                Map<String, String> params = new HashMap<>();
                                params.put("chapterId", mediaEntity.getChapterId());
                                params.put("chapterName", mediaEntity.getChapterTitle());
                                params.put("microlectureId", mediaEntity.getId());
                                params.put("microlectureName", mediaEntity.getTitle());
                                params.put("skipSeconds", String.valueOf(cumulativeSeekSeconds));
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_offline_backward_seek_click.getActionName(), params);
                            } else {
                                triggerSkipAnimation(forwardLabel, forward, forwardArrow, false);
                                player.seekTo(player.getCurrentPosition() + SEEK_TIMER);
                                Map<String, String> params = new HashMap<>();
                                params.put("chapterId", mediaEntity.getChapterId());
                                params.put("chapterName", mediaEntity.getChapterTitle());
                                params.put("microlectureId", mediaEntity.getId());
                                params.put("microlectureName", mediaEntity.getTitle());
                                params.put("skipSeconds", String.valueOf(cumulativeSeekSeconds));
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_offline_forward_seek_click.getActionName(), params);
                            }
                            // reset cumulative burst tracking
//                            seekResetHandler.removeCallbacksAndMessages(null);
//                            seekResetRunnable = () -> seekCount = 0;
//                            seekResetHandler.postDelayed(seekResetRunnable, SEEK_RESET_DELAY);
                        }

                        seekResetHandler.removeCallbacks(seekResetRunnable);
                        seekResetRunnable = () -> {
                            isSeekMode = false;
                            seekCount = 0;
                        };
                        seekResetHandler.postDelayed(seekResetRunnable, SEEK_RESET_DELAY);

                        lastTapTime = now;
                        lastAppliedTapWasLeft = isLeft;
                        return true;
                    }


                    //single tap
                    singleTapRunnable = () -> {
                        isSeekMode = false;
                        seekCount = 0;
                        Map<String, String> params = new HashMap<>();
                        params.put("chapterId", mediaEntity.getChapterId());
                        params.put("chapterName", mediaEntity.getChapterTitle());
                        params.put("microlectureId", mediaEntity.getId());
                        params.put("microlectureName", mediaEntity.getTitle());
                        params.put("tapSide", isLeft ? "left" : "right");
                        params.put("skipSeconds", String.valueOf(10));
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_offline_single_tap.getActionName(), params);
                        v.performClick();
                    };
                    tapHandler.postDelayed(singleTapRunnable, TAP_DELAY);

                    lastTapTime = now;
                    lastAppliedTapWasLeft = isLeft;
                    return true;
                }


                case MotionEvent.ACTION_CANCEL: {
                    longPressHandler.removeCallbacks(longPressRunnable);
                    if (isLongPressActive) {
                        isLongPressActive = false;
                        resetPlaybackSpeed(); //default speed
                    }
                    return true;
                }
            }

            return false;
        });
        
        
        downloadTracker = DemoUtil.getDownloadTracker(this);
        initializeCelebrationView();
        controllerClickListener();
        setDurationText(mediaEntity.getEndPos()-mediaEntity.getStartPos());
        initializePlaybackSpeed();
    }

    /** Keep offline landscape playback immersive after rotation or a transient system-bar reveal. */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveMode();
        }
    }

    private void closeSettingLayout() {
        if (View.VISIBLE == playback_speed_recycler_view.getVisibility()) {
            playback_speed_recycler_view.setVisibility(View.GONE);
            playbackCard.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "closeSettingLayout: ");
            playback_speed_recycler_view.setVisibility(View.VISIBLE);
            playbackCard.setVisibility(View.VISIBLE);
        }
    }

    private void initializePlaybackSpeed(){
        playback_speed_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        videoSpeedPlaybackAdapter = new VideoSpeedPlaybackAdapter(getPlayBackSpeedData(), this);
        playback_speed_recycler_view.setAdapter(videoSpeedPlaybackAdapter);
    }

    public List<VideoPlaybackSpeedDto> getPlayBackSpeedData() {
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("0.25x").speed("0.25").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("0.5x").speed("0.5").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("0.75x").speed("0.75").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(true).speedText(getString(R.string.Normal)).speed("1").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("1.25x").speed("1.25").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("1.5x").speed("1.5").build());
//        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("1.75x").speed("1.75").build()); //scope changed product
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("2x").speed("2").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("3x").speed("3").build());
        return playbackSpeeds;
    }



    private void triggerSkipAnimation(TextView label, ImageButton btn, ImageView arrow, boolean isLeft) {
        seekCount += isLeft ? -1 : 1;
        label.setText(String.format("%+ds", seekCount * 10));

        //restart
        if (skipFadeRunnable != null) {
            label.removeCallbacks(skipFadeRunnable);
        }
        label.animate().cancel();
        arrow.animate().cancel();

        // show views
        btn.setVisibility(View.INVISIBLE);
        label.setVisibility(View.VISIBLE);
        label.setAlpha(1f);
        arrow.setVisibility(View.VISIBLE);
        arrow.setAlpha(1f);

        label.setScaleX(0.8f);
        label.setScaleY(0.8f);

        label.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(120)
                .withEndAction(() ->
                        label.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(120)
                                .start()
                )
                .start();

        arrow.setTranslationX(isLeft ? 30f : -30f);
        arrow.animate().translationX(isLeft ? -30f : 30f).setDuration(350).start();

        skipFadeRunnable = () -> label.postDelayed(() -> {
            arrow.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        arrow.setVisibility(View.INVISIBLE);
                        arrow.setTranslationX(0f);
                        arrow.setAlpha(1f);
                    })
                    .start();

            label.animate()
                    .alpha(0f)
                    .setDuration(260)
                    .withEndAction(() -> {
                        label.setVisibility(View.INVISIBLE);
                        btn.setVisibility(View.VISIBLE);
                    })
                    .start();
        }, 2000);

        label.postDelayed(skipFadeRunnable, 700);
    }


    private void set2xPlayerSpeed() {
        longPressSpeedCard.setVisibility(View.VISIBLE);

        PlaybackParameters params = new PlaybackParameters(Float.parseFloat("2"), 1f);

        Map<String, String> nparams = new HashMap<>();
        nparams.put("pressSide", lastTapWasLeft ? "left" : "right");
        nparams.put("previousSpeed", selectedPlaybackSpeed + "x");
        nparams.put("chapterId", mediaEntity.getChapterId());
        nparams.put("chapterName", mediaEntity.getChapterTitle());
        nparams.put("microlectureId", mediaEntity.getId());
        nparams.put("microlectureName", mediaEntity.getTitle());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_offline_long_press_start.getActionName(), nparams);
        longPressStartTime = System.currentTimeMillis();

        player.setPlaybackParameters(params);
    }

    private void resetPlaybackSpeed() {
        longPressSpeedCard.setVisibility(View.GONE);

        long duration = (System.currentTimeMillis() - longPressStartTime) / 1000;
        Map<String, String> nparams = new HashMap<>();
        nparams.put("previousSpeed", selectedPlaybackSpeed + "x");
        nparams.put("holdDurationSec", String.valueOf(duration));
        nparams.put("chapterId", mediaEntity.getChapterId());
        nparams.put("chapterName", mediaEntity.getChapterTitle());
        nparams.put("microlectureId", mediaEntity.getId());
        nparams.put("microlectureName", mediaEntity.getTitle());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_offline_long_press_release.getActionName(), nparams);

        PlaybackParameters params = new PlaybackParameters(Float.parseFloat(selectedPlaybackSpeed), 1f);
        player.setPlaybackParameters(params);
    }


    private void controllerClickListener() {
        rewind.setOnClickListener(view -> {
            if (disableControllerAction) {
                Toast.makeText(this, getString(R.string.cannot_go_backward), Toast.LENGTH_SHORT).show();
            } else {
                if (player != null) {
                    triggerSkipAnimation(rewindLabel, rewind, rewindArrow, true);
                    long currentPosition = player.getCurrentPosition();
                    player.seekTo(currentPosition - SEEK_TIMER);
                }
            }
        });

        forward.setOnClickListener(view -> {
            if (disableControllerAction) {
                Toast.makeText(this, getString(R.string.cannot_go_forward), Toast.LENGTH_SHORT).show();
            } else if (player != null) {
                triggerSkipAnimation(forwardLabel, forward, forwardArrow, false);
                long currentPosition = player.getCurrentPosition();
                player.seekTo(currentPosition + SEEK_TIMER);
            }
        });
    }

    private void setDurationText(long totalDuration) {
        Log.d(TAG, "setDurationText: "+totalDuration);
        if (totalDuration > 0) {
            String formattedDuration = formatDuration(totalDuration);
            duration.setText(formattedDuration);
        } else {
            duration.setText("--:--");
        }
    }

    private String formatDuration(long durationMillis) {
        long totalSeconds = durationMillis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds);
        }
    }

    public void initializeCelebrationView() {

        iv_active_interactive_question = findViewById(R.id.iv_active_interactive_question);

        successCelebration = findViewById(R.id.success_celebration);
        celebration1 = findViewById(R.id.celebration1);
        celebration2 = findViewById(R.id.celebration2);
        celebration3 = findViewById(R.id.celebration3);
//        confettiSound = MediaPlayer.create(this, R.raw.confetti_sound);
        confettiHandler = new Handler(Looper.getMainLooper());

        try {
            confettiSound = new MediaPlayer();
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.confetti_sound);
            if (afd != null) {
                confettiSound.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                confettiSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
                confettiSound.prepare(); // synchronous prepare
            }
        } catch (Exception e) {
            e.printStackTrace();
            confettiSound = null;
        }

    }

    private void updatePlayerForInteraction() {
        if(!CollectionUtils.isEmpty(mediaEntity.getQuestionsAnsList()) && !CollectionUtils.isEmpty(mediaEntity.getQuestionsAnsResponseList())) {
            for (int i = 0; i < mediaEntity.getQuestionsAnsList().size(); i++) {
                MicroLectureQuestionsAnsDto questionsAnsDto = mediaEntity.getQuestionsAnsList().get(i);
                TimeBarThumbData thumbData = TimeBarThumbData.builder().completed(false)
                        .position(questionsAnsDto.getStartPosition() + 600).questionId(questionsAnsDto.getId())
                        .build();
                thumbDataList.add(thumbData);
                int finalI = i;
                player.createMessage(
                                (messageType, payload) -> {
                                    currentInteractiveQuestion = (MicroLectureQuestionsAnsDto) payload;
                                    player.pause();
                                    interactiveQuestionActive = true;
//                                    buildSingleChoice(mediaEntity.getQuestionsAnsResponseList().get(finalI).getConfirmOptionList(),
//                                            finalI,
//                                            currentInteractiveQuestion.getId());

                                    if(interactiveQuestionActive) {
                                        for (TimeBarThumbData timeBarThumbData : thumbDataList) {
                                            timeBarThumbData.setElevation(0);
                                            timeBarThumbData.setActive(false);
                                            if (timeBarThumbData.getQuestionId().equals(currentInteractiveQuestion.getId())) {
                                                timeBarThumbData.setActive(true);
                                                timeBarThumbData.setElevation(1);
                                            }
                                        }
                                        customTimeBar.setAdMarkerPositions(thumbDataList, player.getDuration(), 0);
                                    }

                                    animateMarkerIconToCenter(() -> {
                                        MicroLectureQuestionAnsResponseDto microLectureQuestionAnsResponseDto = mediaEntity.getQuestionsAnsResponseList().get(finalI);
                                        buildSingleChoiceV2(microLectureQuestionAnsResponseDto.getQuestionText(), microLectureQuestionAnsResponseDto.getQuestionImage(), microLectureQuestionAnsResponseDto.getConfirmOptionList(), currentInteractiveQuestion.getId());
                                    }, customTimeBar.getActiveMarkerScreenPosition());

                                })
                        .setLooper(Looper.getMainLooper())
                        .setPosition(0, questionsAnsDto.getStartPosition() + 600)
                        .setPayload(questionsAnsDto)
                        .setDeleteAfterDelivery(false)
                        .send();
            }
            if (mediaEntity.getEndPos() > 0) {
                customTimeBar.setAdMarkerPositions(thumbDataList, mediaEntity.getEndPos(), mediaEntity.getEndPos());
            }
        }
    }

    private void animateMarkerIconToCenter(Runnable onAnimationEnd, int[] markerPos) {
//        int[] markerPos = customTimeBar.getActiveMarkerScreenPosition();

//        Log.d(TAG, " animateMarkerIconToCenter -----> "+markerPos);
        if (markerPos == null) return;

        int markerScreenX = markerPos[0];
        int markerScreenY = markerPos[1];
        int iconSizePx    = markerPos[2];

        iv_active_interactive_question.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.img_interactivity_question_answer_active));

        ViewGroup rootView = (ViewGroup) getWindow().getDecorView();

        int[] rootLocation = new int[2];
        rootView.getLocationOnScreen(rootLocation);

        float startX = markerScreenX - rootLocation[0] - iconSizePx / 2f;
        float startY = markerScreenY - rootLocation[1] - iconSizePx / 2f;

        iv_active_interactive_question.setVisibility(View.VISIBLE);
        iv_active_interactive_question.setScaleX(1f);
        iv_active_interactive_question.setScaleY(1f);
        iv_active_interactive_question.setX(startX);
        iv_active_interactive_question.setY(startY);

        ViewGroup.LayoutParams lp = iv_active_interactive_question.getLayoutParams();
        lp.width  = iconSizePx;
        lp.height = iconSizePx;
        iv_active_interactive_question.setLayoutParams(lp);

        float screenCenterX = rootView.getWidth()  / 2f - iconSizePx / 2f;
        float screenCenterY = rootView.getHeight() / 2f - iconSizePx / 2f;

        iv_active_interactive_question.animate()
                .x(screenCenterX)
                .y(screenCenterY)
                .scaleX(8f)
                .scaleY(8f)
                .setDuration(700)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    iv_active_interactive_question.setVisibility(View.GONE);
                    iv_active_interactive_question.setScaleX(1f);
                    iv_active_interactive_question.setScaleY(1f);

                    onAnimationEnd.run();
                })
                .start();
    }

    public void buildSingleChoice(List<ConfirmOption> confirmOptionList, int index, String microLectureQId) {
        if (CollectionUtils.isEmpty(confirmOptionList)) {
            return;
        }

        Collections.shuffle(confirmOptionList);

        Dialog optionDialog = new Dialog(this);
        optionDialog.setContentView(R.layout.video_player_options_layout);
        optionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        optionDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this,
                R.drawable.bachground_custom_dialogu_typeactivity));
        optionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        optionDialog.setCancelable(false);
        TextView optionA = optionDialog.findViewById(R.id.optionA);
        TextView optionB = optionDialog.findViewById(R.id.optionB);
        TextView optionC = optionDialog.findViewById(R.id.optionC);
        TextView optionD = optionDialog.findViewById(R.id.optionD);
        TextView alertTitle = optionDialog.findViewById(R.id.alert_title);

        if (index == 0) {
            alertTitle.setText(R.string.here_are_option);
        }
        optionA.setVisibility(View.GONE);
        optionB.setVisibility(View.GONE);
        optionC.setVisibility(View.GONE);
        optionD.setVisibility(View.GONE);
        Log.d("download_issue ", "buildSingleChoice: "+confirmOptionList);

        if (!CollectionUtils.isEmpty(confirmOptionList)) {
            optionA.setText(confirmOptionList.get(0).getText());
            if (StringUtils.isNotEmpty(optionA.getText())) {
                optionA.setVisibility(View.VISIBLE);
            }

            if (confirmOptionList.size() > 1) {
                optionB.setText(confirmOptionList.get(1).getText());
                if (StringUtils.isNotEmpty(optionB.getText())) {
                    optionB.setVisibility(View.VISIBLE);
                }
            }
            if (confirmOptionList.size() > 2) {
                optionC.setText(confirmOptionList.get(2).getText());
                if (StringUtils.isNotEmpty(optionC.getText())) {
                    optionC.setVisibility(View.VISIBLE);
                }
            }
            if (confirmOptionList.size() > 3) {
                optionD.setText(confirmOptionList.get(3).getText());
                if (StringUtils.isNotEmpty(optionD.getText())) {
                    optionD.setVisibility(View.VISIBLE);
                }
            }
        }

        optionA.setOnClickListener(v -> {
            if (!CollectionUtils.isEmpty(confirmOptionList)) {
                ConfirmOption confirmOption = confirmOptionList.get(0);
                validateAPIResponse(confirmOption.getSeekTo(),
                        confirmOption.getTrackerCompletionEndPos(),
                        confirmOption.getFallbackPos(),
                        confirmOption.isCorrect(), confirmOption.getText(), microLectureQId, false, optionA.getText().toString());
                optionDialog.dismiss();
//                isDialogShowing = false;
            }
        });
        optionB.setOnClickListener(v -> {
            if (confirmOptionList.size() > 1) {
                ConfirmOption confirmOption = confirmOptionList.get(1);
                validateAPIResponse(confirmOption.getSeekTo(),
                        confirmOption.getTrackerCompletionEndPos(),
                        confirmOption.getFallbackPos(),
                        confirmOption.isCorrect(), confirmOption.getText(), microLectureQId, false, optionA.getText().toString());
                optionDialog.dismiss();
            }
        });
        optionC.setOnClickListener(v -> {
            if (confirmOptionList.size() > 2) {
                ConfirmOption confirmOption = confirmOptionList.get(2);
                validateAPIResponse(confirmOption.getSeekTo(),
                        confirmOption.getTrackerCompletionEndPos(),
                        confirmOption.getFallbackPos(),
                        confirmOption.isCorrect(), confirmOption.getText(), microLectureQId, false, optionA.getText().toString());
                optionDialog.dismiss();
            }
        });
        optionD.setOnClickListener(v -> {
            if (confirmOptionList.size() > 3) {
                ConfirmOption confirmOption = confirmOptionList.get(3);
                validateAPIResponse(confirmOption.getSeekTo(),
                        confirmOption.getTrackerCompletionEndPos(),
                        confirmOption.getFallbackPos(),
                        confirmOption.isCorrect(), confirmOption.getText() , microLectureQId, false, optionA.getText().toString());
                optionDialog.dismiss();
//                isDialogShowing = false;
            }
        });

        if (!isFinishing()) {
            optionDialog.show();
        }
    }

    public void buildSingleChoiceV2(String questionText,
                                    String questionImage,
                                    List<ConfirmOption> confirmOptionList,
                                    String microLectureQId) {

        if(confirmOptionList==null) {
            return;
        }
        if(StringUtils.isEmpty(questionText)) {
            Collections.shuffle(confirmOptionList);
        }

        Dialog optionDialog = new Dialog(this);
        optionDialog.setContentView(R.layout.video_player_options_layout_v2);
        optionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        optionDialog.getWindow().setBackgroundDrawable(
                AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
        optionDialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        optionDialog.setCancelable(false);

        TextView tv_subtitle = optionDialog.findViewById(R.id.tv_subtitle);
//        ImageView iv_question_image = optionDialog.findViewById(R.id.iv_question_image);

        tv_subtitle.setText(
                StringUtils.isNotEmpty(questionText)
                        ? questionText
                        : getString(R.string.please_sahi_option_select_karein)
        );

        MaterialCardView[] cardViews = {
                optionDialog.findViewById(R.id.cv_option_a),
                optionDialog.findViewById(R.id.cv_option_b),
                optionDialog.findViewById(R.id.cv_option_c),
                optionDialog.findViewById(R.id.cv_option_d)
        };

        TextView[] textViews = {
                optionDialog.findViewById(R.id.tv_option_a_text),
                optionDialog.findViewById(R.id.tv_option_b_text),
                optionDialog.findViewById(R.id.tv_option_c_text),
                optionDialog.findViewById(R.id.tv_option_d_text)
        };

        TextView[] textOptionViews = {
                optionDialog.findViewById(R.id.tv_option_a),
                optionDialog.findViewById(R.id.tv_option_b),
                optionDialog.findViewById(R.id.tv_option_c),
                optionDialog.findViewById(R.id.tv_option_d)
        };

        ImageView[] imageViews = {
                optionDialog.findViewById(R.id.iv_option_a),
                optionDialog.findViewById(R.id.iv_option_b),
                optionDialog.findViewById(R.id.iv_option_c),
                optionDialog.findViewById(R.id.iv_option_d)
        };

        if(StringUtils.isEmpty(questionText)) {
            for(TextView textView : textOptionViews) {
                textView.setVisibility(View.GONE);
            }
        }

//        iv_question_image.setVisibility(StringUtils.isNotEmpty(questionImage)? View.VISIBLE : View.GONE);
//        if(StringUtils.isNotEmpty(questionImage)) {
//            Glide.with(this).load(questionImage).into(iv_question_image);
//        }

        // Hide all initially
        for (MaterialCardView card : cardViews) {
            card.setVisibility(View.GONE);
        }

        for (int i = 0; i < confirmOptionList.size() && i < cardViews.length; i++) {

            ConfirmOption confirmOption = confirmOptionList.get(i);
            String text = confirmOption.getText();

            if (StringUtils.isNotEmpty(text)) {
                textViews[i].setText(text);
                cardViews[i].setVisibility(View.VISIBLE);

                final int position = i;

                cardViews[i].setOnClickListener(v -> {

                    resetAllIcons(imageViews);

                    imageViews[position].setImageResource(
                            R.drawable.img_video_options_selected);

                    validateAPIResponse(
                            confirmOption.getSeekTo(),
                            confirmOption.getTrackerCompletionEndPos(),
                            confirmOption.getFallbackPos(),
                            confirmOption.isCorrect(),
                            confirmOption.getText(),
                            microLectureQId,
                            false,
                            textViews[position].getText().toString()
                    );

                    optionDialog.dismiss();
                });
            }
        }

//        optionDialog.setOnDismissListener(v -> isDialogShowing = false);

        if (!isFinishing()) {
//            isDialogShowing = true;
            optionDialog.show();
        }
    }

    private void resetAllIcons(ImageView[] imageViews) {
        for (ImageView imageView : imageViews) {
            imageView.setImageResource(R.drawable.img_video_options_unselected);
        }
    }

    public void validateAPIResponse(long seekToPosition, Integer endPosition, Integer fallbackPosition,
                                    boolean correct, String userAnswer, String microLectureQId, boolean speech, String clickAnswer) {
        for (TimeBarThumbData thumbData : thumbDataList) {
            thumbData.setActive(false);
            if (thumbData.getQuestionId().equals(currentInteractiveQuestion.getId())) {
                thumbData.setCompleted(true);
                thumbData.setCorrect(correct);
            }
        }
        customTimeBar.setAdMarkerPositions(thumbDataList, player.getDuration(), 0);
        interactiveQuestionActive = false;
        if (correct) {
            player.seekTo(seekToPosition);
            player.play();
            playCelebrationAnimation();
        } else {
            this.endPosition = endPosition != null ? endPosition : (int) seekToPosition;
            this.fallbackPosition = fallbackPosition != null ? fallbackPosition : (int) seekToPosition;
            disableControllerAction = true;
            customTimeBar.setVisibility(View.GONE);
            player.seekTo(seekToPosition);
            player.play();
        }
    }

    public void playCelebrationAnimation() {
        try {
            successCelebration.setVisibility(View.VISIBLE);
            celebration1.setVisibility(View.VISIBLE);
            celebration1.playAnimation();
            celebration2.setVisibility(View.VISIBLE);
            celebration2.playAnimation();
            celebration3.setVisibility(View.VISIBLE);
            celebration3.playAnimation();

            confettiSound.start();

            confettiRunnable = () -> {
                celebration1.setVisibility(View.GONE);
                celebration1.cancelAnimation();
                celebration2.setVisibility(View.GONE);
                celebration2.cancelAnimation();
                celebration3.setVisibility(View.GONE);
                celebration3.cancelAnimation();
            };

            confettiHandler.postDelayed(confettiRunnable, 1300);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runPositionUpdateRunnable() {
        if (runnableStarted) {
            return;
        }
        positionUpdatingRunnable = new Runnable() {
            @Override
            public void run() {
                long currentPosition = player.getCurrentPosition();
                long endPos = mediaEntity.getEndPos();
                customTimeBar.setDuration(endPos);

                if (disableControllerAction) {
                    if (currentPosition >= endPosition) {
                        //Log.d(TAG, "seeking to fallback " + fallbackPosition);
                        player.seekTo(fallbackPosition);
                        disableControllerAction = false;
                        customTimeBar.setVisibility(View.VISIBLE);
                    }
                    handler.postDelayed(this, TIME_BUFFER_DIFFERENCE);
                } else if (player != null && player.getMediaItemCount() == 1 && currentPosition <
                        mediaEntity.getEndPos()) {
                    handler.postDelayed(this, TIME_BUFFER_DIFFERENCE);
                } else if (player != null && player.getMediaItemCount() == 1 && currentPosition >=
                        mediaEntity.getEndPos()) {
                    player.pause();
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        runnableStarted = true;
        positionUpdatingRunnable.run();
    }


    @OptIn(markerClass = UnstableApi.class)
    private void initPlayer() {
        player = new ExoPlayer.Builder(this).build();
        player.setPlayWhenReady(true);
        playerView.setPlayer(player);

        Uri videoUri = Uri.parse(VIDEO_URL);
        DownloadRequest downloadRequest = downloadTracker.getDownloadRequest(videoUri);
        MediaSource mediaSource=null;
        if(downloadRequest!=null) {
            mediaSource = DownloadHelper.createMediaSource(
                    Objects.requireNonNull(downloadRequest), DemoUtil.getDataSourceFactory(this));
        }


        if (mediaSource != null) {
            player.setMediaSource(mediaSource);
        }

        player.seekTo(playbackPosition);
        player.setPlayWhenReady(playWhenReady);
        player.prepare();
        handler = new Handler(Looper.getMainLooper());
        runPositionUpdateRunnable();
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            playWhenReady = player.getPlayWhenReady();
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
    protected void onResume() {
        super.onResume();
        if(player==null) {
            initPlayer();
            updatePlayerForInteraction();
        }else if(!interactiveQuestionActive){
            resumePlayer();
            if (handler != null && positionUpdatingRunnable != null) {
                runPositionUpdateRunnable();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayer();
        if (handler != null && positionUpdatingRunnable != null) {
            runnableStarted = false;
            handler.removeCallbacks(positionUpdatingRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        if (confettiSound != null) {
            confettiSound.release();
            confettiSound = null;
        }
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void speedChanged(int position) {
        VideoPlaybackSpeedDto speedDto = playbackSpeeds.get(position);

        for (int i = 0; i < playbackSpeeds.size(); i++) {
            playbackSpeeds.get(i).setSelected(false);
        }
        playbackSpeeds.get(position).setSelected(true);
        videoSpeedPlaybackAdapter.notifyDataSetChanged();
        selectedPlaybackSpeed = speedDto.getSpeed(); //save
        PlaybackParameters params = new PlaybackParameters(Float.parseFloat(speedDto.getSpeed()), 1f);
        player.setPlaybackParameters(params);
        playback_speed_recycler_view.setVisibility(View.GONE);
        playbackCard.setVisibility(View.GONE);
    }

    @Override
    public void closeSpeedDialog() {

    }
}
